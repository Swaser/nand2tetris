package ch.chassaing.hack.instruction;

import ch.chassaing.hack.MachineInstruction;
import ch.chassaing.hack.SymbolTable;

import java.util.BitSet;

public record CInstruction(Destination dest,
                           Computation comp,
                           Jump jump)
        implements Instruction
{
    @Override
    public MachineInstruction toMachineInstruction(SymbolTable unused)
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
        bitSet.set(i, true);

        byte[] bytes = bitSet.toByteArray(); // BitSet.toByteArray() is little endian
        return new MachineInstruction(bytes[0],bytes[1]);
    }
}
