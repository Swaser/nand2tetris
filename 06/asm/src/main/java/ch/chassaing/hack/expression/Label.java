package ch.chassaing.hack.expression;

import static java.util.Objects.requireNonNull;

public final class Label
    extends Expression
{
    public final String value;

    public Label(int lineNumber,
                 String line,
                 String value)
    {
        super(lineNumber, line);
        this.value = requireNonNull(value);
    }
}
