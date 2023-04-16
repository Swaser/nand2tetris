package ch.chassaing.hack.vm;

import ch.chassaing.hack.vm.command.Command;
import ch.chassaing.hack.vm.command.Function;
import ch.chassaing.hack.vm.command.Pop;
import ch.chassaing.hack.vm.command.Push;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
class HackWriterTest {

    private CodeWriter sut;

    @org.junit.jupiter.api.BeforeEach
    void setUp()
    {
        sut = new HackWriter(false);
    }

    @org.junit.jupiter.api.Test
    void getInstructions()
    {
        int line = 2;
        List<Command> commands = List.of(
                new Function(line++, "mult", 2),
                new Push(line++, Segment.CONSTANT, 0),
                new Pop(line++, Segment.LOCAL, 0));
        List<String> instructions = sut.getInstructions(commands);
        instructions.forEach(System.out::println);
        assertFalse(instructions.isEmpty());
    }
}