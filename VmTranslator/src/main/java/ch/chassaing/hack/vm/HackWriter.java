package ch.chassaing.hack.vm;

import ch.chassaing.hack.vm.command.*;

import java.util.*;

public final class HackWriter
        implements CodeWriter
{
    private static final Map<Segment, String> SEGMENT_SYMBOLS =
            Map.of(Segment.ARGUMENT, "@ARG",
                   Segment.LOCAL, "@LCL",
                   Segment.THIS, "@THIS",
                   Segment.THAT, "@THAT");

    private String functionName = "global";
    private int retCounter = 0;
    private int compCounter = 0;
    private int contCounter = 0;

    private final List<String> instructions = new LinkedList<>();

    /**
     * Startet einen neuen HackWriter und fügt den Bootstrap code ein,
     * falls es sich um ein volles Program (mit Sys Modul) handelt.
     */
    public HackWriter(boolean isComplete)
    {
        if (isComplete) {
            // SP auf 256 setzen
            add("@256", "D=A", "@SP", "M=D");
            // Sys.init aufrufen, aber da Sys.init nie zurückkehrt, braucht es keinen Call
            add("@Sys.init", "0;JEQ");
        }
    }

    @Override
    public List<String> getInstructions(List<Command> commands)
    {
        // wir benutzen cmdIdx damit wir lookahead machen können, wenn nötig
        int cmdIdx = -1;
        while (cmdIdx + 1 < commands.size()) {
            Command command = commands.get(++cmdIdx);
            if (command instanceof Push push) {
                generatePush(push);
            } else if (command instanceof PushS pushS) {
                add("@" + pushS.value(), "D=M");
                toStack("D");
            } else if (command instanceof Pop pop) {
                generatePop(pop);
            } else if (command instanceof PopS popS) {
                stackToD();
                add("@" + popS.value(), "M=D");
            } else if (command instanceof Binary binary) {
                generateBinary(binary.op());
            } else if (command instanceof Comparison comparison) {
                generateCompare(comparison.jumpInstruction());
            } else if (command instanceof Unary unary) {
                generateUnary(unary);
            } else if (command instanceof Function function) {
                generateFunction(function);
            } else if (command instanceof Return) {
                generateReturn();
            } else if (command instanceof Call call) {
                generateCall(call);
            } else if (command instanceof Label label) {
                add("(" + label.label() + ")");
            } else if (command instanceof Goto aGoto) {
                add("@" + aGoto.label(),
                    "0;JEQ");
            } else if (command instanceof IfGoto ifGoto) {
                generateIfGoto(ifGoto);
            } else {
                throw new UnsupportedOperationException("Unknown command: " + command);
            }
        }

        return instructions;
    }

    private void generatePush(Push push)
    {
        if (push.segment() == Segment.CONSTANT) {
            constToD(push.value());
        } else if (push.segment() == Segment.POINTER) {
            add(push.value() == 0 ? "@THIS" : "@THAT",
                "D=M");
        } else if (push.segment() == Segment.TEMP) {
            add("@R" + (5 + push.value()),
                "D=M");
        } else {
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
        } else if (pop.segment() == Segment.TEMP) {
            stackToD();
            add("@R" + (5 + pop.value()),
                "M=D");
        } else {
            popToSymbol(SEGMENT_SYMBOLS.get(pop.segment()),
                        pop.value());
        }
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
        String contLabel = functionName + "$cont." + contCounter++;
        String compLabel = functionName + "$comp." + compCounter++;

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
        add("(" + function.name() + ")"); // label hinzufügen
        // Die lokalen Variablen mit 0 initialisieren
        if (function.nVars() > 0) {
            add("@SP");
            for (int i = 0; i < function.nVars(); i++) add("A=M", "M=0", "@SP", "M=M+1");
        }
    }

    private void generateCall(Call call)
    {
        if (call.nArgs() == 0) {
            // add increase stack to make space for return value
            add("@SP", "M=M+1");
        }

        // current function
        String returnLabel = functionName + "$ret." + retCounter++;

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

        // ARG = SP - 5 - nArgs; but we have to take into account, that
        // we need space for the return value, even when nArgs == 0
        // thus we must handle the code, as if nArgs >= 1
        add("@SP", "D=M", "@5", "D=D-A");
        add("@" + Math.max(1, call.nArgs()), "D=D-A");

        add("@ARG", "M=D");

        // LCL = SP
        add("@SP", "D=M", "@LCL", "M=D");

        // Sprung zur Funktion
        add("@" + call.function(), "0;JEQ");

        // nun das label
        add("(" + returnLabel + ")");
    }

    private void generateReturn()
    {
        // Frame in R13 speichern
        add("@LCL", "D=M", "@R13", "M=D");

        // Kopiert den return Wert nach ARG[0]
        popToSymbol("@ARG", 0);

        // Stackpointer auf ARG[1] setzen
        add("@ARG", "D=M+1", "@SP", "M=D");

        add("@R13", "M=M-1"); // R13 ist frame - 1
        add("A=M", "D=M", "@THAT", "M=D"); // THAT = *(frame - 1)
        add("@R13", "M=M-1"); // R13 ist frame - 2
        add("A=M", "D=M", "@THIS", "M=D"); // THIS = *(frame - 2)
        add("@R13", "M=M-1"); // R13 ist frame - 3
        add("A=M", "D=M", "@ARG", "M=D"); //  ARG = *(frame - 3)
        add("@R13", "M=M-1"); // R13 ist frame - 4
        add("A=M", "D=M", "@LCL", "M=D"); //  LCL = *(frame - 4)
        add("@R13", "M=M-1"); // R13 ist frame - 5
        add("A=M", "A=M;JMP"); // Sprung zur Rücksprungadresse = *(frame - 5)
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
        } else if (offset == 1) {
            stackToD();
            add(symbol,
                "A=M+1", // pointer dereferenzieren
                "M=D");
        } else {
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
        } else if (offset == 1) {
            add(symbol, "A=M+1", "D=M");
        } else if (offset == 2) {
            add(symbol, "A=M+1", "A=A+1", "D=M");
        } else {
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
        this.functionName = functionName;
        retCounter = 0;
        compCounter = 0;
        contCounter = 0;
    }
}
