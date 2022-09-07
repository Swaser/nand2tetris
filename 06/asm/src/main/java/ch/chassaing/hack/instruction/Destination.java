package ch.chassaing.hack.instruction;

import java.util.Objects;
import java.util.function.Predicate;

import static ch.chassaing.hack.instruction.Bits.flip;
import static java.util.Objects.requireNonNull;

public enum Destination
{
    // from most significant bit to least significant bit
    NONE(s -> !s.contains("="), new boolean[]{false, false, false}),
    M(s -> s.startsWith("M="), new boolean[]{false, false, true}),
    D(s -> s.startsWith("D="), new boolean[]{false, true, false}),
    DM(s -> s.startsWith("DM="), new boolean[]{false, true, true}),
    MD(s -> s.startsWith("MD="), new boolean[]{false, true, true}),
    A(s -> s.startsWith("A="), new boolean[]{true, false, false}),
    AM(s -> s.startsWith("AM="), new boolean[]{true, false, true}),
    MA(s -> s.startsWith("MA="), new boolean[]{true, false, true}),
    AD(s -> s.startsWith("AD="), new boolean[]{true, true, false}),
    DA(s -> s.startsWith("DA="), new boolean[]{true, true, false}),
    DAM(s -> s.startsWith("DAM="), new boolean[]{true, true, true}),
    AMD(s -> s.startsWith("AMD="), new boolean[]{true, true, true}),
    MDA(s -> s.startsWith("DMA="), new boolean[]{true, true, true}),
    ADM(s -> s.startsWith("ADM="), new boolean[]{true, true, true}),
    DMA(s -> s.startsWith("ADM="), new boolean[]{true, true, true}),
    MAD(s -> s.startsWith("ADM="), new boolean[]{true, true, true});

    public final Predicate<String> match;
    public final boolean[] bits;

    Destination(Predicate<String> match,
                boolean[] bits)
    {
        this.match = requireNonNull(match);
        this.bits = flip(requireNonNull(bits));
    }
}
