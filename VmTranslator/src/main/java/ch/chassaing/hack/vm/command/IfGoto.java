package ch.chassaing.hack.vm.command;

import ch.chassaing.hack.vm.Checks;

public record IfGoto(int line,
                     String label)
    implements Command
{
    public IfGoto
    {
        Checks.greaterEqualZero(line);
        Checks.notBlank(label);
    }
}
