package ch.chassaing.hack.instruction;

import ch.chassaing.hack.MachineInstruction;
import ch.chassaing.hack.SymbolTable;

import java.math.BigInteger;

import static java.util.Objects.requireNonNull;

public record LInstruction(String loopIndicator)
    implements Instruction
{
    public LInstruction
    {
        requireNonNull(loopIndicator);
    }

    @Override
    public MachineInstruction toMachineInstruction(SymbolTable symbolTable)
    {
        requireNonNull(symbolTable);
        BigInteger loopAddress = symbolTable.symbolAddress(loopIndicator);
        return MachineInstruction.fromBigInteger(loopAddress);
    }
}
