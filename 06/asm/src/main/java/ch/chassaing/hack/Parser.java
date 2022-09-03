package ch.chassaing.hack;

import ch.chassaing.hack.instruction.Instruction;

public interface Parser
{
    /**
     * Parse one line of assembly code and return the result: Comments and
     * empty lines will return {@link Result.None}. A correct instruction will
     * return a {@link Result.Success} and an incorrect one will return a
     * {@link Result.Error}.
     */
    Result<Instruction> parseLine(String line);
}
