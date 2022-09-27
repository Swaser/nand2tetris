package ch.chassaing.hack.vm;

import ch.chassaing.hack.vm.command.*;

import java.util.*;

public final class HackWriter
        implements ICodeWriter {
    private static final Map<Segment, String> BASE_ADDRESSES =
            Map.of(Segment.ARGUMENT, "@ARG",
                   Segment.LOCAL, "@LCL",
                   Segment.THIS, "@THIS",
                   Segment.THAT, "@THAT");

    private final List<String> instructions = new LinkedList<>();
    private final List<String> functions = new LinkedList<>();
    private int contCounter = 0;
    private int compCounter = 0;

    public HackWriter() {
        // add eternal loop at end of program
        addf("(END)",
             "@END",
             "0;JEQ");
    }

    @Override
    public void add(Command command) {

        if (command instanceof Push push) {
            generatePush(push);
        } else if (command instanceof Pop pop) {
            generatePop(pop);
        } else if (command instanceof Arithmetic arithmetic) {
            generateArithmetic(arithmetic.op());
        } else if (command instanceof Comparison comparison) {
            generateCompare(comparison.jumpInstruction());
        }
    }

    @Override
    public Iterable<String> getInstructions() {

        return () -> Iterators.combine(instructions.iterator(),
                                       functions.iterator());
    }

    private void generatePush(Push push) {

        if (push.segment() == Segment.CONSTANT) {
            constToD(push.value());
        } else {
            segmentToD(push.segment(), push.value());
        }
        dToStack();
    }

    /**
     * Take a value from the stack and put it into a segment
     */
    private void generatePop(Pop pop) {

        if (pop.segment() == Segment.CONSTANT) {
            System.out.println("Cannot pop unto CONSTANT segment");
            System.exit(3);
        }

        stackToD();
        dToSegment(pop.segment(), pop.value());
    }

    /**
     * Zweiter op Oberster
     * Benutzt R13
     */
    private void generateArithmetic(String op) {

        // Erster -> R13
        stackToD();
        add("@R13",
            "M=D");

        // Zweiter -> D
        stackToD();

        // Erster -> M
        add("@R13");

        // Zweiter op Erster -> D
        add("D=D" + op + "M");
        dToStack();
    }

    /**
     * Zweiter - Erster; Sprung bei Bedingung
     * Benutzt R13
     */
    private void generateCompare(String jumpInstruction) {

        String contLabel = "CONTINUE." + contCounter++;
        String compLabel = "COMP." + compCounter++;

        // Erster -> R13
        stackToD();
        add("@R13",
            "M=D");

        // Zweiter -> D
        stackToD();

        // Erster -> M
        add("@R13");

        // Zweiter minus Erster -> D
        add("D=D-M");

        // testen und Sprung bei Erfolg
        add("@" + compLabel,
            "D;" + jumpInstruction);

        // kein Sprung: 0 (=false) auf Stack und ans Ende des Blocks springen
        add("@0",
            "D=A");
        dToStack();
        add("@" + contLabel,
            "0;JEQ");

        // Sprung: 1 (=true) auf Stack
        add("(" + compLabel + ")");
        add("@1",
            "D=A");
        dToStack();

        add("(" + contLabel + ")");
    }

    /**
     * uses register R13 and R14
     */
    private void dToSegment(Segment segment,
                            int offset) {
        // store D in R13
        add("@R13",
            "M=D");

        // store Address in R14
        add(BASE_ADDRESSES.get(segment),
            "D=M",
            "@" + offset,
            "D=D+A",
            "@R14",
            "M=D");

        // store R13 to (R14)
        add("@R13",
            "D=M",
            "@R14",
            "A=M",
            "M=D");
    }

    /**
     * no additional register needed
     */
    private void constToD(int value) {

        add("@" + value,
            "D=A");
    }

    /**
     * no additional register needed
     */
    void segmentToD(Segment segment,
                    int offset) {

        add(BASE_ADDRESSES.get(segment),
            "D=M",         // base address in D
            "@" + offset,  // offset in A
            "A=D+A",       // A now has the address
            "D=M");        // D now has the value at the address
    }

    /**
     * no additional register needed
     */
    private void dToStack() {

        add("@SP",
            "A=M",   // A enthält nun die Adresse auf die der SP zeigt
            "M=D",   // D wird an diese Adresse kopiert
            "D=A+1", // Die Adresse für den SP wird um 1 erhöht
            "@SP",
            "M=D");  // und der neue Wert wird in den SP zurückgeschrieben
    }

    /**
     * no additional register needed
     */
    private void stackToD() {

        add("@SP",
            "M=M-1", // Wert des SP reduzieren
            "A=M",
            "D=M");
    }

    private void add(String... someInstructions) {

        Collections.addAll(instructions, someInstructions);
    }

    private void addf(String... someInstructions) {

        Collections.addAll(functions, someInstructions);
    }
}
