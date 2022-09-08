package ch.chassaing.hack.expression;

import ch.chassaing.hack.MachineInstruction;
import ch.chassaing.hack.SymbolTable;

import static java.util.Objects.requireNonNull;

public final class Symbol
        extends Address
{
    private final String chars;

    public Symbol(int lineNumber,
                  String line,
                  String chars)
    {
        super(lineNumber, line);
        this.chars = requireNonNull(chars);
    }

    @Override
    public MachineInstruction toMachineInstruction(SymbolTable symbolTable)
    {
        return MachineInstruction.fromBigInteger(symbolTable.symbolAddress(chars));
    }
}
