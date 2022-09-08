package ch.chassaing.hack.expression;

import ch.chassaing.hack.MachineInstruction;
import ch.chassaing.hack.SymbolTable;

public class Expression
{
    public final int lineNumber;

    public final String line;

    protected Expression(int lineNumber,
                         String line)
    {
        this.lineNumber = lineNumber;
        this.line = line;
    }
}
