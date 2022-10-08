package ch.chassaing.hack.vm;

import ch.chassaing.hack.vm.command.*;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public final class HackWriter
        implements ICodeWriter
{
    private static final Map<Segment, String> BASE_ADDRESSES =
            Map.of(Segment.ARGUMENT, "@ARG",
                   Segment.LOCAL, "@LCL",
                   Segment.THIS, "@THIS",
                   Segment.THAT, "@THAT");

    private final List<String> instructions = new LinkedList<>();
    private       int          contCounter  = 0;
    private       int          compCounter  = 0;
    private       String       progName;

    @Override
    public void setProgName(String progName)
    {
        this.progName = progName;
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
        } else if (command instanceof Function function) {
            generateFunction(function);
        }
    }

    @Override
    public Iterable<String> getInstructions()
    {
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
        } else if (push.segment() == Segment.STATIC) {
            add(staticSymbol(push.value()),
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
        } else if (pop.segment() == Segment.STATIC) {
            stackToD();
            add(staticSymbol(pop.value()),
                "M=D");
        } else {
            // calc address and store it in R13
            add(BASE_ADDRESSES.get(pop.segment()),
                "D=M",
                "@" + pop.value(),
                "D=D+A",
                "@R13",
                "M=D");

            // get stack value and store it in (R13)
            stackToD();
            add("@R13",
                "A=M",
                "M=D");
        }
    }

    private String staticSymbol(int value)
    {
        if (StringUtils.isBlank(progName)) {
            throw new IllegalStateException("progName must be set");
        }
        return "@%s.%d".formatted(progName, value);
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
        String contLabel = "CONTINUE." + contCounter++;
        String compLabel = "COMP." + compCounter++;

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

    private void generateFunction(Function function) {
        add("(" + function.name() + ")");
        for (int i=0; i<function.nVars(); i++) {
            toStack("0");
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
        add(BASE_ADDRESSES.get(segment),
            "D=M",         // base address in D
            "@" + offset,  // offset in A
            "A=D+A",       // A now has the address
            "D=M");        // D now has the value at the address
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
}
