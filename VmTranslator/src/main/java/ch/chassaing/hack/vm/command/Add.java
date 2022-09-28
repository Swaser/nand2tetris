package ch.chassaing.hack.vm.command;

public record Add(int line)
        implements Binary
{
    @Override
    public String op() {

        return "+";
    }
}
