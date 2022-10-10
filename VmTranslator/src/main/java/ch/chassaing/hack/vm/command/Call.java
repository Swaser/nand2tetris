package ch.chassaing.hack.vm.command;

public record Call(int line,
                   String function,
                   int nArgs)
    implements Command
{
}
