package ch.chassaing.jack.lang.subroutine;

public enum SubroutineScope
{
    FUNCTION, CONSTRUCTOR, METHOD;

    public final boolean callFromLocal() {
        return switch (this) {
            case CONSTRUCTOR, METHOD -> true;
            default -> false;
        };
    }
}
