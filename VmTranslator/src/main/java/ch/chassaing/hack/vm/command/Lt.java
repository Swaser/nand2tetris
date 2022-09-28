package ch.chassaing.hack.vm.command;

public record Lt(int line)
implements Comparison{
    @Override
    public String jumpInstruction() {

        return "JLT";
    }
}
