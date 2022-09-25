package ch.chassaing.hack.vm;

import ch.chassaing.hack.vm.command.Add;
import ch.chassaing.hack.vm.command.Command;
import ch.chassaing.hack.vm.command.Pop;
import ch.chassaing.hack.vm.command.Push;

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

    private List<String> instructions;

    @Override
    public List<String> write(Command command)
    {
        instructions = new LinkedList<>();
        if (command instanceof Push push) {
            generatePush(push);
        } else if (command instanceof Pop pop) {
            generatePop(pop);
        } else if (command instanceof Add) {
            generateAdd();
        }
        return Collections.unmodifiableList(instructions);
    }

    private void generatePush(Push push)
    {
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
    private void generatePop(Pop pop)
    {
        if (pop.segment() == Segment.CONSTANT) {
            System.out.println("Cannot pop unto CONSTANT segment");
            System.exit(3);
        }

        stackToD();
        dToSegment(pop.segment(), pop.value());
    }

    private void generateAdd()
    {
        stackToD();
        // store first stack value in R13
        add("@R13",
            "M=D");
        // second stack value goes to D
        stackToD();
        // read R13 into A and compute sum in D
        add("@R13",
            "A=M",
            "D=A+D");
        dToStack();
    }

    /**
     * uses register R13 and R14
     */
    private void dToSegment(Segment segment, int offset)
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

    /**
     * no additional register needed
     */
    private void dToStack()
    {
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
    private void stackToD()
    {
        add("@SP",
            "M=M-1", // Wert des SP reduzieren
            "A=M",
            "D=M");
    }

    private void add(String... someInstructions)
    {
        Collections.addAll(instructions, someInstructions);
    }
}
