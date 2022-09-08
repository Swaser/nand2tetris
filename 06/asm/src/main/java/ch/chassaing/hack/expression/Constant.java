package ch.chassaing.hack.expression;

import ch.chassaing.hack.MachineInstruction;
import ch.chassaing.hack.SymbolTable;

import java.math.BigInteger;

import static java.util.Objects.requireNonNull;

public final class Constant
        extends Address
{
    private final BigInteger value;

    public Constant(int lineNumber,
                    String line,
                    BigInteger value)
    {
        super(lineNumber, line);
        this.value = requireNonNull(value);
    }

    @Override
    public MachineInstruction toMachineInstruction(SymbolTable symbolTable)
    {
        return MachineInstruction.fromBigInteger(value);
    }
}
