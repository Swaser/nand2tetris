package ch.chassaing.hack.vm.command;

public record And(int line)
    implements Binary
{
    @Override
    public String op()
    {
        return "&";
    }
}
