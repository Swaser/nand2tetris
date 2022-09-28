package ch.chassaing.hack.vm.command;

public record Sub(int line)
    implements Arithmetic
{
    @Override
    public String op() {

        return "-";
    }
}
