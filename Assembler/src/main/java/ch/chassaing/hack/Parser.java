package ch.chassaing.hack;

import ch.chassaing.hack.expression.Expression;
import io.vavr.control.Option;

public interface Parser
{
    /**
     * Parse one line of assembly code and return the result: Comments and
     * empty lines will return {@link Option.None} and a correct instruction will
     * return a {@link Option.Some}.
     */
    Option<Expression> parseLine(int lineNumber,
                                 String line);
}
