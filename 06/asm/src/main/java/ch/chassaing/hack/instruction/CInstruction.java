package ch.chassaing.hack.instruction;

import java.util.BitSet;

public record CInstruction(Destination dest,
                           Computation comp,
                           Jump jump)
        implements Instruction
{
    @Override
    public byte[] toMachineInstruction()
    {

        BitSet bitSet = new BitSet(16);
        int i=0;
        for (boolean bit : jump.bits) {
            bitSet.set(i++,bit);
        }
        for (boolean bit : dest.bits) {
            bitSet.set(i++,bit);
        }
        for (boolean bit : comp.cBits) {
            bitSet.set(i++,bit);
        }
        bitSet.set(i++, comp.aBit);
        bitSet.set(i++, true);
        bitSet.set(i++, true);
        bitSet.set(i++, true);

        return bitSet.toByteArray();
    }
}
