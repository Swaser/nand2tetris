package ch.chassaing.hack.vm.command;

import ch.chassaing.hack.vm.Segment;

/**
 * Read value from segment and push it onto stack
 */
public record Push(int line,
                   Segment segment,
                   int value,
                   String filename)
    implements Command
{
    public Push
    {
        if (value < 0)
            throw new IllegalArgumentException(line + ": value must not be negative");
        if (segment == Segment.POINTER && value > 1)
            throw new IllegalArgumentException(line + ": value must not be > 1 for segment pointer");
        if (segment == Segment.TEMP && value > 7)
            throw new IllegalArgumentException(line + ": value must not be > 7 for segment temp");
    }
}
