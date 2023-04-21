package ch.chassaing.hack.vm.command;

public record Call(int line,
                   String function,
                   int nArgs)
    implements Command
{
    public Call
    {
        if (nArgs < 0) {
            throw new IllegalArgumentException("nArgs must be >= 0");
        }
    }
}
