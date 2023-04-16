package ch.chassaing.hack.vm;

import ch.chassaing.hack.vm.command.Command;

import java.util.List;

public interface CodeWriter
{
    /**
     * Will output all instructions added to it plus the necessary bootstrap code.
     */
    List<String> getInstructions(List<Command> commands);
}
