package ch.chassaing.hack.vm.command;

import static ch.chassaing.hack.vm.Checks.greaterEqualZero;
import static ch.chassaing.hack.vm.Checks.notBlank;

public record Label(int line,
                    String label)
    implements Command
{
    public Label
    {
        greaterEqualZero(line);
        notBlank(label);
    }
}
