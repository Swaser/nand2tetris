package ch.chassaing.hack.instruction;

public record CInstruction(Destination dest,
                           Computation comp,
                           Jump jump)
        implements Instruction
{
    @Override
    public byte[] toMachineInstruction()
    {
        return new byte[2];
    }
}
