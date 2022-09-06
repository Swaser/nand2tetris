package ch.chassaing.hack;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static java.util.Objects.isNull;

/**
 * NOT threadsafe
 */
public final class SymbolTableImpl
    implements SymbolTable
{
    private final Map<String, BigInteger> symbolMap = new HashMap<>();
    private long nextFree = 16;

    public static final long SCREEN_ADDRESS = 16384L;
    {
        putAddress("R0", BigInteger.ZERO);
        putAddress("R1", BigInteger.ONE);
        putAddress("R2", BigInteger.valueOf(2));
        putAddress("R3", BigInteger.valueOf(3));
        putAddress("R4", BigInteger.valueOf(4));
        putAddress("R5", BigInteger.valueOf(5));
        putAddress("R6", BigInteger.valueOf(6));
        putAddress("R7", BigInteger.valueOf(7));
        putAddress("R8", BigInteger.valueOf(8));
        putAddress("R9", BigInteger.valueOf(9));
        putAddress("R10", BigInteger.valueOf(10));
        putAddress("R11", BigInteger.valueOf(11));
        putAddress("R12", BigInteger.valueOf(12));
        putAddress("R13", BigInteger.valueOf(13));
        putAddress("R14", BigInteger.valueOf(14));
        putAddress("R15", BigInteger.valueOf(15));
        putAddress("SCREEN", BigInteger.valueOf(SCREEN_ADDRESS));
        putAddress("KBD", BigInteger.valueOf(16384 + 8192));
    }

    @Override
    public void putAddress(String symbol, BigInteger address)
    {
        if (symbolMap.containsKey(symbol)) {
            throw new IllegalStateException("Duplicate symbol");
        }
        symbolMap.put(symbol, address);
    }

    @Override
    public BigInteger symbolAddress(String symbol)
    {
        BigInteger result = symbolMap.get(symbol);
        if (isNull(result)) {
            if (nextFree >= SCREEN_ADDRESS) {
                throw new IllegalStateException("Too many symbols");
            }
            result = BigInteger.valueOf(nextFree++);
            symbolMap.put(symbol, result);
        }
        return result;
    }
}
