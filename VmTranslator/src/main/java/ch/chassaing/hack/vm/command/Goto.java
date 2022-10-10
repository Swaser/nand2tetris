package ch.chassaing.hack.vm.command;

import static ch.chassaing.hack.vm.Checks.greaterEqualZero;
import static ch.chassaing.hack.vm.Checks.notBlank;

public record Goto(int line,
                   String label)
    implements Command
{
    public Goto
    {
        greaterEqualZero(line);
        notBlank(label);
    }
}
