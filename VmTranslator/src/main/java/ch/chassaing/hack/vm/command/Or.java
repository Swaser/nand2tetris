package ch.chassaing.hack.vm.command;

public record Or(int line)
    implements Binary
{
    @Override
    public String op()
    {
        return "|";
    }
}
