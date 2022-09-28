package ch.chassaing.hack.vm.command;

public record Neg(int line)
    implements Unary
{
    @Override
    public String op()
    {
        return "-";
    }
}
