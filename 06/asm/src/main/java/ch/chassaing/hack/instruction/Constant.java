package ch.chassaing.hack.instruction;

public record Constant(String value)
        implements AInstruction {

    @Override
    public byte[] toMachineInstruction() {
        return new byte[2];
    }
}
