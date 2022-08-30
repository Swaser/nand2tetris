package ch.chassaing.hack.instruction;

import java.math.BigInteger;
import java.util.Arrays;

public record Constant(String value)
        implements AInstruction {

    @Override
    public byte[] toMachineInstruction() {

        BigInteger bigInteger = new BigInteger(value, 10);
        byte[] bytes = bigInteger.toByteArray();
        return Arrays.copyOf(bytes, 2);
    }
}
