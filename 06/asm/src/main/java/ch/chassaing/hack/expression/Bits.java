package ch.chassaing.hack.expression;

import static java.util.Objects.requireNonNull;

public final class Bits {

    private Bits() { /* dont instantiate */ }

    public static boolean[] flip(boolean[] in) {

        requireNonNull(in);
        int n = in.length;
        boolean[] out = new boolean[n];
        if (n > 0) {
            for (int i = 0; i < n; i++) {
                out[i] = in[n - 1 - i];
            }
        }
        return out;
    }
}
