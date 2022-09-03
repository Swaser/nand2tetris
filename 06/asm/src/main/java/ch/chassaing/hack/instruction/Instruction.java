package ch.chassaing.hack.instruction;

import ch.chassaing.hack.MachineInstruction;
import ch.chassaing.hack.SymbolTable;

public interface Instruction
{
    MachineInstruction toMachineInstruction(SymbolTable symbolTable);
}
