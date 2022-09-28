package ch.chassaing.hack.vm.command;

public record Not(int line)
    implements Unary
{
    @Override
    public String op()
    {
        return "!";
    }
}
