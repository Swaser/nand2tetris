package ch.chassaing.hack.expression;

import java.util.Objects;

public final class MalformedExpression
        extends Expression
{
    public final String details;

    public MalformedExpression(int lineNumber,
                               String line,
                               String details)
    {
        super(lineNumber, line);
        this.details = Objects.requireNonNull(details);
    }
}
