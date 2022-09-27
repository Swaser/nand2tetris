package ch.chassaing.hack.vm;

import ch.chassaing.hack.vm.command.Command;

public interface ICodeWriter
{
    void add(Command command);

    Iterable<String> getInstructions();
}
