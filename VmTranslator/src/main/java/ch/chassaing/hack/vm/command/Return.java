package ch.chassaing.hack.vm.command;

import static ch.chassaing.hack.vm.Checks.greaterEqualZero;

public record Return(int line)
    implements Command
{
    public Return
    {
        greaterEqualZero(line);
    }
}
