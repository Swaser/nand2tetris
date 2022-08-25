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
    A(s -> s.startsWith("A="), new boolean[]{true, false, false}),
    AM(s -> s.startsWith("AM="), new boolean[]{true, false, true}),
    AD(s -> s.startsWith("AD="), new boolean[]{true, true, false}),
    ADM(s -> s.startsWith("ADM="), new boolean[]{true, true, true});

    public final Predicate<String> match;
    public final boolean[] bits;

    Destination(Predicate<String> match,
                boolean[] bits)
    {
        this.match = requireNonNull(match);
        this.bits = flip(requireNonNull(bits));
    }
}
