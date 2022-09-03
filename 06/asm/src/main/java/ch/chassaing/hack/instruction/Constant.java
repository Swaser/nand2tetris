package ch.chassaing.hack.instruction;

import ch.chassaing.hack.MachineInstruction;
import ch.chassaing.hack.SymbolTable;

import java.math.BigInteger;

import static java.util.Objects.requireNonNull;

public record Constant(BigInteger value)
        implements AInstruction
{

    public Constant
    {
        requireNonNull(value);
    }

    @Override
    public MachineInstruction toMachineInstruction(SymbolTable symbolTable)
    {
        return MachineInstruction.fromBigInteger(value);
    }
}
