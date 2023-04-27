package ch.chassaing.hack.expression;

import java.math.BigInteger;

/**
 * An Address Expression is used to denote an address that can either
 * point to RAM or ROM. It can also be used to input a Constant into
 * the Address Register.
 */
public abstract sealed class Address
        extends Instruction
        permits Constant, Symbol {

    protected Address(int lineNumber,
                      String line)
    {
        super(lineNumber, line);
    }

    protected static String convertToAscii(BigInteger aValue)
    {
        StringBuilder sb = new StringBuilder("0");
        for (int i=14; i>=0; i--) {
            boolean bit = aValue.testBit(i);
            sb.append(bit ? "1" : "0");
        }
        return sb.toString();
    }
}
