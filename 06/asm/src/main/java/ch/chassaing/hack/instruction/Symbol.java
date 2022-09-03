package ch.chassaing.hack.instruction;

import ch.chassaing.hack.MachineInstruction;
import ch.chassaing.hack.SymbolTable;

import static java.util.Objects.requireNonNull;

public record Symbol(String chars)
        implements AInstruction
{
    public Symbol
    {
        requireNonNull(chars);
    }

    @Override
    public MachineInstruction toMachineInstruction(SymbolTable symbolTable)
    {
        return MachineInstruction.fromBigInteger(symbolTable.symbolAddress(chars));
    }
}
