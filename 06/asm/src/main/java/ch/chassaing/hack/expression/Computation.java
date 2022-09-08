package ch.chassaing.hack.expression;

import static ch.chassaing.hack.expression.Bits.flip;
import static java.util.Objects.requireNonNull;

public enum Computation {

    // Most significant bit to least significant bit
    ZERO("0", false, new boolean[]{true, false, true, false, true, false}),
    ONE("1", false, new boolean[]{true, true, true, true, true, true}),
    MINUS_ONE("-1", false, new boolean[]{true, true, true, false, true, false}),
    D("D", false, new boolean[]{false, false, true, true, false, false}),
    A("A", false, new boolean[]{true, true, false, false, false, false}),
    M("M", true, new boolean[]{true, true, false, false, false, false}),
    NOT_D("!D", false, new boolean[]{false, false, true, true, false, true}),
    NOT_A("!A", false, new boolean[]{true, true, false, false, false, true}),
    NOT_M("!M", true, new boolean[]{true, true, false, false, false, true}),
    MINUS_D("-D", false, new boolean[]{false, false, true, true, true, true}),
    MINUS_A("-A", false, new boolean[]{true, true, false, false, true, true}),
    MINUS_M("-M", true, new boolean[]{true, true, false, false, true, true}),
    D_PLUS_ONE("D+1", false, new boolean[]{false, true, true, true, true, true}),
    A_PLUS_ONE("A+1", false, new boolean[]{true, true, false, true, true, true}),
    M_PLUS_ONE("M+1", false, new boolean[]{true, true, false, true, true, true}),
    D_MINUS_ONE("D-1", false, new boolean[]{false, false, true, true, true, false}),
    A_MINUS_ONE("A-1", false, new boolean[]{true, true, false, false, true, false}),
    M_MINUS_ONE("M-1", false, new boolean[]{true, true, false, false, true, false}),
    D_PLUS_A("D+A", false, new boolean[]{false, false, false, false, true, false}),
    D_PLUS_M("D+M", true, new boolean[]{false, false, false, false, true, false}),
    D_MINUS_A("D-A", false, new boolean[]{false, true, false, false, true, true}),
    D_MINUS_M("D-M", true, new boolean[]{false, true, false, false, true, true}),
    A_MINUS_D("A-D", false, new boolean[]{false, false, false, true, true, true}),
    M_MINUS_D("M-D", true, new boolean[]{false, false, false, true, true, true}),
    D_AND_A("D&A", false, new boolean[]{false, false, false, false, false, false}),
    D_AND_M("D&M", true, new boolean[]{false, false, false, false, false, false}),
    D_OR_A("D|A", false, new boolean[]{false, true, false, true, false, true}),
    D_OR_M("D|M", true, new boolean[]{false, true, false, true, false, true});

    public final String stringRep;
    public final boolean aBit;
    public final boolean[] cBits;

    Computation(String stringRep, boolean aBit, boolean[] cBits) {
        this.stringRep = stringRep;
        this.aBit = requireNonNull(aBit);
        this.cBits = flip(requireNonNull(cBits));
    }
}
