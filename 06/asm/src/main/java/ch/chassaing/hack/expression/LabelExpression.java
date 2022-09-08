package ch.chassaing.hack.expression;

import ch.chassaing.hack.MachineInstruction;
import ch.chassaing.hack.SymbolTable;

import static java.util.Objects.requireNonNull;

public final class LabelExpression
    extends Expression
{
    public final String label;

    public LabelExpression(int lineNumber,
                           String line,
                           String label)
    {
        super(lineNumber, line);
        this.label = requireNonNull(label);
    }
}
