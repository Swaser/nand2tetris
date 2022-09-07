package ch.chassaing.hack;

import java.math.BigInteger;

import static java.util.Objects.requireNonNull;

public record MachineInstruction(byte loByte,
                                 byte hiByte)
{
    public static MachineInstruction fromBigInteger(BigInteger address)
    {
        requireNonNull(address);
        byte[] bytes = address.toByteArray(); // BigInteger.toByteArray() is big endian
        if (bytes.length > 2) {
            throw new IllegalArgumentException("Address must not be longer than 16 bit");
        } else if (bytes.length == 2) {
            return new MachineInstruction(bytes[1], bytes[0]);
        } else if (bytes.length == 1) {
            return new MachineInstruction(bytes[0], (byte) 0);
        } else {
            return new MachineInstruction((byte) 0, (byte) 0);
        }
    }
}
