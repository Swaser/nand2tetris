package ch.chassaing.hack.vm;

import ch.chassaing.hack.vm.command.*;

import java.util.*;

public final class HackWriter
        implements ICodeWriter
{
    private static final Map<Segment, String> SEGMENT_SYMBOLS =
            Map.of(Segment.ARGUMENT, "@ARG",
                   Segment.LOCAL, "@LCL",
                   Segment.THIS, "@THIS",
                   Segment.THAT, "@THAT");

    private final List<String> instructions = new LinkedList<>();

    private String currentFunction = "global";
    private int retCounter = 0;
    private int compCounter = 0;
    private int contCounter = 0;

    /**
     * Startet einen neuen HackWriter und fügt den Bootstrap code ein,
     * falls es sich um ein volles Program (mit Sys Modul) handelt.
     */
    public HackWriter(boolean isComplete)
    {
        if (isComplete) {
            // SP auf 256 setzen
            add("@256", "D=A", "@SP", "M=D");
            //generateCall(new Call(-1, "Sys.init", 0));
            add("@Sys.init", "0;JEQ");
        }
    }

    @Override
    public List<String> getInstructions(List<Command> commands)
    {
        for (Command command : commands) {
            if (command instanceof Push push) {
                generatePush(push);
            }
            else if (command instanceof Pop pop) {
                generatePop(pop);
            }
            else if (command instanceof Binary binary) {
                generateBinary(binary.op());
            }
            else if (command instanceof Comparison comparison) {
                generateCompare(comparison.jumpInstruction());
            }
            else if (command instanceof Unary unary) {
                generateUnary(unary);
            }
            else if (command instanceof Function function) {
                generateFunction(function);
            }
            else if (command instanceof Return) {
                generateReturn();
            }
            else if (command instanceof Call call) {
                generateCall(call);
            }
            else if (command instanceof Label label) {
                add("(" + label.label() + ")");
            }
            else if (command instanceof Goto aGoto) {
                add("@" + aGoto.label(),
                    "0;JEQ");
            }
            else if (command instanceof IfGoto ifGoto) {
                generateIfGoto(ifGoto);
            }
            else {
                throw new UnsupportedOperationException("Unknown command: " + command);
            }
        }

        return instructions;
    }

    private void generatePush(Push push)
    {
        if (push.segment() == Segment.CONSTANT) {
            constToD(push.value());
        }
        else if (push.segment() == Segment.POINTER) {
            add(push.value() == 0 ? "@THIS" : "@THAT",
                "D=M");
        }
        else if (push.segment() == Segment.TEMP) {
            add("@R" + (5 + push.value()),
                "D=M");
        }
        else if (push.segment() == Segment.STATIC) {
            add(staticSymbol(push.filename(), push.value()),
                "D=M");
        }
        else {
            segmentToD(push.segment(), push.value());
        }
        toStack("D");
    }

    private void generatePop(Pop pop)
    {
        if (pop.segment() == Segment.POINTER) {
            stackToD();
            add(pop.value() == 0 ? "@THIS" : "@THAT",
                "M=D");
        }
        else if (pop.segment() == Segment.TEMP) {
            stackToD();
            add("@R" + (5 + pop.value()),
                "M=D");
        }
        else if (pop.segment() == Segment.STATIC) {
            stackToD();
            add(staticSymbol(pop.filename(), pop.value()),
                "M=D");
        }
        else {
            popToSymbol(SEGMENT_SYMBOLS.get(pop.segment()),
                        pop.value());
        }
    }

    private String staticSymbol(String filename, int value)
    {
        return "@%s.%d".formatted(filename, value);
    }

    private void generateBinary(String op)
    {
        stackToD();            // first = y-> D
        stackToM();            // second = x -> M
        add("D=M" + op + "D"); // x op y -> D
        toStack("D");
    }

    /**
     * Zweiter - Erster; Sprung bei Bedingung
     */
    private void generateCompare(String jumpInstruction)
    {
        String contLabel = currentFunction + "$cont." + contCounter++;
        String compLabel = currentFunction + "$comp." + compCounter++;

        stackToD();        // first = y -> D
        stackToM();        // second = x -> M
        add("D=M-D");      // x - y -> D

        // compare and jump if condition holds
        add("@" + compLabel,
            "D;" + jumpInstruction);

        // no jump: put false (=0) on stack and jump to end of block
        toStack("0");
        add("@" + contLabel,
            "0;JEQ ");

        // jumped: put true (=-1) on stack
        add("(" + compLabel + ")");
        toStack("-1");

        add("(" + contLabel + ")");
    }

    private void generateUnary(Unary unary)
    {
        stackToM();
        add("D=" + unary.op() + "M");
        toStack("D");
    }

    private void generateFunction(Function function)
    {
        enterFunction(function.name());
        add("(" + function.name() + ")");
        // Die lokalen Variablen mit 0 initialisieren
        if (function.nVars() > 0) {
            add("@SP");
            for (int i = 0; i < function.nVars(); i++) add("A=M", "M=0", "@SP", "M=M+1");
        }
    }

    private void generateCall(Call call)
    {
        // current function
        String returnLabel = currentFunction + "$ret." + retCounter++;

        // Rücksprungadresse auf den Stack
        add("@" + returnLabel,
            "D=A");
        toStack("D");

        add("@LCL", "D=M");
        toStack("D");

        add("@ARG", "D=M");
        toStack("D");

        add("@THIS", "D=M");
        toStack("D");

        add("@THAT", "D=M");
        toStack("D");

        // ARG = SP - 5 - nArgs
        add("@SP", "D=M", "@5", "D=D-A");
        if (call.nArgs() > 0) {
            add("@" + call.nArgs(), "D=D-A");
        }
        add("@ARG", "M=D");

        // LCL = SP
        add("@SP", "D=M", "@LCL", "M=D");

        // Sprung zur Funktion
        add("@" + call.function(), "0;JEQ");

        add("(" + returnLabel + ")");

        /*
        SP zeigt jetzt auf 1 nach dem letzten Argument, da als Letztes die retAddr gepopt wurde
        ARG[0]
        ARG[1] <- SP sollte hier sein
        ...
        ARG[N]
        retAddr <- SP ist aber nach dem Rücksprung hier
        LCL'
        ARG'
        ...
        Nun soll der SP aber auf 1 nach dem return Wert (der in ARG[0] ist) zeigen --> SP = SP - nArgs + 1
         */
        int remaining = call.nArgs() - 1;
        if (remaining > 0) {
            add("@SP");
            do {
                add("M=M-1");
            } while (--remaining > 0);
        }
    }

    private void generateReturn()
    {
        // Kopiert den return Wert nach ARG[0]
        popToSymbol("@ARG", 0);

        /*
        Setzt den SP auf LCL[0], d.h. der Stack sieht dann so aus:
        ARG[0]
        ...
        ARG[N]
        retAddr
        LCL'
        ARG'
        THIS'
        THAT'
        LCL[0] <- SP
        ...
        LCL[M]
         */
        add("@LCL",
            "D=M",
            "@SP",
            "M=D");

        // popt THAT, THIS, ARG und LCL
        stackToD();
        add("@THAT", "M=D");
        stackToD();
        add("@THIS", "M=D");
        stackToD();
        add("@ARG", "M=D");
        stackToD();
        add("@LCL", "M=D");

        // popt die return Adresse nach M und macht den Sprung
        stackToM();
        add("A=M", "0;JEQ");
    }

    private void generateIfGoto(IfGoto ifGoto)
    {
        stackToD();
        add("@" + ifGoto.label(),
            "D;JNE");
    }

    private void popToSymbol(String symbol, int offset)
    {
        if (offset == 0) {
            stackToD();
            add(symbol,
                "A=M", // pointer dereferenzieren
                "M=D");
        }
        else if (offset == 1) {
            stackToD();
            add(symbol,
                "A=M+1", // pointer dereferenzieren
                "M=D");
        }
        else {
            add("@" + offset, "D=A"); // Offset in D
            add(symbol, "D=M+D");     // Adresse in D
            add("@R13", "M=D");       // Adresse in R13
            stackToD();
            add("@R13",
                "A=M", // pointer dereferenzieren
                "M=D");
        }
    }

    private void constToD(int value)
    {
        if (value < 0) {
            throw new IllegalArgumentException("Constant cannot be negative: " + value);
        }
        add("@" + value,
            "D=A");
    }

    private void segmentToD(Segment segment,
                            int offset)
    {
        String symbol = SEGMENT_SYMBOLS.get(segment);
        if (offset == 0) {
            add(symbol, "A=M", "D=M");
        }
        else if (offset == 1) {
            add(symbol, "A=M+1", "D=M");
        }
        else if (offset == 2) {
            add(symbol, "A=M+1", "A=A+1", "D=M");
        }
        else {
            add("@" + offset,
                "D=A",
                symbol,
                "A=M+D",
                "D=M");
        }
    }

    /**
     * @param what any of 0, -1, 1, or D
     */
    private void toStack(String what)
    {
        if (!Set.of("0", "-1", "1", "D").contains(what)) {
            throw new IllegalArgumentException("Only 0, -1, 1, or D allowed");
        }
        add("@SP",
            "A=M",
            "M=" + what,
            "@SP",
            "M=M+1");
    }

    private void stackToD()
    {
        stackToM();
        add("D=M");
    }

    private void stackToM()
    {
        add("@SP",
            "M=M-1", // reduce value in SP
            "A=M");
    }

    private void add(String... someInstructions)
    {
        Collections.addAll(instructions, someInstructions);
    }

    private void enterFunction(String functionName)
    {
        currentFunction = functionName;
        retCounter = 0;
        compCounter = 0;
        contCounter = 0;
    }
}
