package ch.chassaing.hack.vm.command;

public record Gt(int line)
implements Comparison{
    @Override
    public String jumpInstruction() {

        return "JGT";
    }
}
