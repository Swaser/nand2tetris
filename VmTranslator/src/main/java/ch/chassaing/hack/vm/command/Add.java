package ch.chassaing.hack.vm.command;

public record Add(int line)
    implements Arithmetic
{
    @Override
    public String op() {

        return "+";
    }
}
