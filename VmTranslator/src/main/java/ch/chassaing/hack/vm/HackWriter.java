package ch.chassaing.hack.vm;

import ch.chassaing.hack.vm.command.*;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class HackWriter
        implements ICodeWriter
{
    private static final Map<Segment, String> BASE_ADDRESSES =
            Map.of(Segment.ARGUMENT, "@ARG",
                   Segment.LOCAL, "@LCL",
                   Segment.THIS, "@THIS",
                   Segment.THAT, "@THAT");

    private final List<String> instructions = new LinkedList<>();
    private final List<String> functions    = new LinkedList<>();
    private       int          contCounter  = 0;
    private       int          compCounter  = 0;

    public HackWriter()
    {
        // add eternal loop at end of program
        addf("(END)",
             "@END",
             "0;JEQ");
    }

    @Override
    public void add(Command command)
    {
        if (command instanceof Push push) {
            generatePush(push);
        } else if (command instanceof Pop pop) {
            generatePop(pop);
        } else if (command instanceof Binary binary) {
            generateBinary(binary.op());
        } else if (command instanceof Comparison comparison) {
            generateCompare(comparison.jumpInstruction());
        } else if (command instanceof Unary unary) {
            generateUnary(unary);
        }
    }

    @Override
    public Iterable<String> getInstructions()
    {
        return () -> Iterators.combine(instructions.iterator(),
                                       functions.iterator());
    }

    private void generatePush(Push push)
    {
        if (push.segment() == Segment.CONSTANT) {
            constToD(push.value());
        } else {
            segmentToD(push.segment(), push.value());
        }
        toStack("D");
    }

    /**
     * Take a value from the stack and put it into a segment
     */
    private void generatePop(Pop pop)
    {
        if (pop.segment() == Segment.CONSTANT) {
            System.out.println("Cannot pop unto CONSTANT segment");
            System.exit(3);
        }

        stackToD();
        dToSegment(pop.segment(), pop.value());
    }

    /**
     * Zweiter op Oberster
     */
    private void generateBinary(String op)
    {
        stackToD();            // Erster -> D
        stackToM();            // Zweiter -> M
        add("D=M" + op + "D"); // Zweiter op Erster -> D
        toStack("D");
    }

    /**
     * Zweiter - Erster; Sprung bei Bedingung
     */
    private void generateCompare(String jumpInstruction)
    {
        String contLabel = "CONTINUE." + contCounter++;
        String compLabel = "COMP." + compCounter++;

        stackToD();        // Erster -> D
        stackToM();        // Zweiter -> M
        add("D=M-D");      // Zweiter minus Erster -> D

        // testen und Sprung bei Erfolg
        add("@" + compLabel,
            "D;" + jumpInstruction);

        // kein Sprung: 0 (=false) auf Stack und ans Ende des Blocks springen
        toStack("0");
        add("@" + contLabel,
            "0;JEQ ");

        // Sprung: -1 (=true) auf Stack
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

    /**
     * uses register R13 and R14
     */
    private void dToSegment(Segment segment,
                            int offset)
    {
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
    private void constToD(int value)
    {
        add("@" + value,
            "D=A");
    }

    /**
     * no additional register needed
     */
    void segmentToD(Segment segment,
                    int offset)
    {
        add(BASE_ADDRESSES.get(segment),
            "D=M",         // base address in D
            "@" + offset,  // offset in A
            "A=D+A",       // A now has the address
            "D=M");        // D now has the value at the address
    }

    private void toStack(String what)
    {
        add("@SP",
            "A=M",
            "M=" + what,
            "@SP",
            "M=M+1");
    }

    /**
     * no additional register needed
     */
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

    private void addf(String... someInstructions)
    {
        Collections.addAll(functions, someInstructions);
    }
}
