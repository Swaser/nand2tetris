package ch.chassaing.hack.expression;

import ch.chassaing.hack.MachineInstruction;
import ch.chassaing.hack.SymbolTable;

import java.util.BitSet;

import static java.util.Objects.requireNonNull;

public final class CInstruction
        extends Instruction
{
    public final Destination dest;

    public final Computation comp;

    public final Jump jump;

    public CInstruction(int lineNumber,
                        String line,
                        Destination dest,
                        Computation comp,
                        Jump jump)
    {
        super(lineNumber, requireNonNull(line));
        this.dest = requireNonNull(dest);
        this.comp = requireNonNull(comp);
        this.jump = requireNonNull(jump);
    }


    @Override
    public MachineInstruction toMachineInstruction(SymbolTable unused)
    {
        BitSet bitSet = calcBitSet();

        byte[] bytes = bitSet.toByteArray(); // BitSet.toByteArray() is little endian
        return new MachineInstruction(bytes[0], bytes[1]);
    }

    @Override
    public String toAsciiInstruction(SymbolTable symbolTable)
    {
        BitSet bitSet = calcBitSet();

        StringBuilder sb = new StringBuilder();
        for (int i = bitSet.length()-1; i>=0; i--) {
            boolean bit = bitSet.get(i);
            sb.append(bit ? "1" : 0);
        }
        return sb.toString();
    }

    private BitSet calcBitSet()
    {
        BitSet bitSet = new BitSet(16);
        int i = 0;
        for (boolean bit : jump.bits) {
            bitSet.set(i++, bit);
        }
        for (boolean bit : dest.bits) {
            bitSet.set(i++, bit);
        }
        for (boolean bit : comp.cBits) {
            bitSet.set(i++, bit);
        }
        bitSet.set(i++, comp.aBit);
        bitSet.set(i++, true);
        bitSet.set(i++, true);
        bitSet.set(i, true);
        return bitSet;
    }
}
