package ch.chassaing.hack;

import java.math.BigInteger;

public final class SymbolTableImpl
    implements SymbolTable
{
    @Override
    public void putAddress(String symbol, BigInteger address)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public BigInteger symbolAddress(String symbol)
    {
        throw new UnsupportedOperationException();
    }
}
