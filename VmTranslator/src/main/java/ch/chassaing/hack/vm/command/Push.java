package ch.chassaing.hack.vm.command;

import ch.chassaing.hack.vm.Segment;

public record Push(int line,
                   Segment segment,
                   int value)
    implements Command
{
}
