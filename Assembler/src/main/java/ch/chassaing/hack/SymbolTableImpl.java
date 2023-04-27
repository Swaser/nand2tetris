package ch.chassaing.hack;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import static java.lang.System.out;
import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

/**
 * NOT threadsafe
 */
public final class SymbolTableImpl
    implements SymbolTable
{
    private final Map<String, BigInteger> symbolMap = new HashMap<>();
    private long nextFree = 16;

    public static final long SCREEN_ADDRESS = 16384L;

    public static final long KEYBOARD_ADDRESS = SCREEN_ADDRESS + 8192;

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

        putAddress("SP", BigInteger.valueOf(0));
        putAddress("LCL", BigInteger.valueOf(1));
        putAddress("ARG", BigInteger.valueOf(2));
        putAddress("THIS", BigInteger.valueOf(3));
        putAddress("THAT", BigInteger.valueOf(4));

        putAddress("SCREEN", BigInteger.valueOf(SCREEN_ADDRESS));
        putAddress("KBD", BigInteger.valueOf(KEYBOARD_ADDRESS));
    }

    @Override
    public void putAddress(String symbol, BigInteger address)
    {
        requireNonNull(address);
        if (symbolMap.containsKey(requireNonNull(symbol))) {
            throw new IllegalStateException("Duplicate symbol");
        }
        symbolMap.put(symbol, address);
        out.printf("%s got address %d%n", symbol, address.longValue());
    }

    @Override
    public BigInteger symbolAddress(String symbol)
    {
        BigInteger result = symbolMap.get(requireNonNull(symbol));
        if (isNull(result)) {
            if (nextFree >= SCREEN_ADDRESS) {
                throw new IllegalStateException("Too many symbols");
            }
            result = BigInteger.valueOf(nextFree++);
            putAddress(symbol, result);
        }
        return result;
    }

    @Override
    public boolean hasSymbol(String symbol)
    {
        return symbolMap.containsKey(requireNonNull(symbol));
    }
}
