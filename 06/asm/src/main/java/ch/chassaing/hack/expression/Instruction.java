package ch.chassaing.hack.expression;

import ch.chassaing.hack.MachineInstruction;
import ch.chassaing.hack.SymbolTable;

/**
 * An expression that will be converted into machine language
 */
public abstract class Instruction
        extends Expression
{
    protected Instruction(int lineNumber,
                          String line)
    {
        super(lineNumber, line);
    }

    public abstract MachineInstruction toMachineInstruction(SymbolTable symbolTable);
}
