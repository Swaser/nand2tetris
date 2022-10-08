package ch.chassaing.hack.vm;

import org.apache.commons.lang3.StringUtils;

public final class Checks
{
    private Checks() { /* do not instantiate */ }

    public static int greaterEqualZero(int arg) {
        if (arg < 0) {
            throw new IllegalArgumentException("Must be >= 0: " + arg);
        }
        return arg;
    }

    public static String notBlank(String arg) {
        if (StringUtils.isBlank(arg)) {
            throw new IllegalArgumentException("Must not be blank: " + arg);
        }
        return arg;
    }
}
