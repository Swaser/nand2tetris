package ch.chassaing.hack.vm.command;

public record Eq(int line)
        implements Comparison
{
    @Override
    public String jumpInstruction() {

        return "JEQ";
    }
}
