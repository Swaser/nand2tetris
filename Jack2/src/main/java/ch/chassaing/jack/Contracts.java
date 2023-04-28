package ch.chassaing.jack;

public final class Contracts
{
    private Contracts() {}

    public static void precondition(boolean precondition) {
        if (!precondition) {
            throw new IllegalStateException("Precondition failed");
        }
    }
}
