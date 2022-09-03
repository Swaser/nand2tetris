package ch.chassaing.hack;

import java.math.BigInteger;

public interface SymbolTable
{
    void putAddress(String symbol, BigInteger address);

    BigInteger symbolAddress(String symbol);
}
