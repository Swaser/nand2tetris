package ch.chassaing.hack.vm.command;

import static ch.chassaing.hack.vm.Checks.greaterEqualZero;
import static ch.chassaing.hack.vm.Checks.notBlank;

public record Function(int line,
                       String name,
                       int nVars)
        implements Command
{
    public Function
    {
        greaterEqualZero(line);
        notBlank(name);
        greaterEqualZero(nVars);
    }

    @Override
    public String toString()
    {
        return "line %4d: function %s (%d local variables)".formatted(line, name, nVars);
    }
}
