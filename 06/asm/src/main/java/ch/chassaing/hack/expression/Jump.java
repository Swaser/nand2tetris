package ch.chassaing.hack.expression;

import java.util.function.Predicate;

import static ch.chassaing.hack.expression.Bits.flip;

public enum Jump
{
    // msb to lsb, will be flipped in constructor
    NONE(s -> !s.contains(";"), new boolean[]{false, false, false}),
    JGT(s -> s.endsWith(";JGT"), new boolean[]{false, false, true}),
    JEQ(s -> s.endsWith(";JEQ"), new boolean[]{false, true, false}),
    JGE(s -> s.endsWith(";JGE"), new boolean[]{false, true, true}),
    JLT(s -> s.endsWith(";JLT"), new boolean[]{true, false, false}),
    JNE(s -> s.endsWith(";JNE"), new boolean[]{true, false, true}),
    JLE(s -> s.endsWith(";JLE"), new boolean[]{true, true, false}),
    JMP(s -> s.endsWith(";JMP"), new boolean[]{true, true, true});
    public final Predicate<String> matches;
    public final boolean[] bits;

    Jump(Predicate<String> matches, boolean[] bits)
    {
        this.matches = matches;
        this.bits = flip(bits);
    }
}
