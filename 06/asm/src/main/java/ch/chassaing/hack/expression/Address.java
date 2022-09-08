package ch.chassaing.hack.expression;

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
}
