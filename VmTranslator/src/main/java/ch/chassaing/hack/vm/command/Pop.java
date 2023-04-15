package ch.chassaing.hack.vm.command;

import ch.chassaing.hack.vm.Segment;

/**
 * Take a value from the stack and put it to the address given by segment and value
 */
public record Pop(int line,
                  Segment segment,
                  int value,
                  String filename)
    implements Command
{
    public Pop(int line, Segment segment, int value)
    {
        this(line, segment, value, null);
    }

    public Pop
    {
        if (value < 0)
            throw new IllegalArgumentException(line + ": value must not be negative");
        if (segment == Segment.POINTER && value > 1)
            throw new IllegalArgumentException(line + ": value must not be > 1 for segment pointer");
        if (segment == Segment.TEMP && value > 7)
            throw new IllegalArgumentException(line + ": value must not be > 7 for segment temp");
        if (segment == Segment.CONSTANT)
            throw new IllegalArgumentException(line + ": cannot pop into segment constant");
    }
}
