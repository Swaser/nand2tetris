package ch.chassaing.hack.vm;

import ch.chassaing.hack.vm.command.Command;

import java.util.List;

public interface ICodeWriter
{
    List<String> write(Command command);
}
