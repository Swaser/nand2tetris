package ch.chassaing.hack.vm.command;

public record Sub(int line)
        implements Binary
{
    @Override
    public String op() {

        return "-";
    }
}
