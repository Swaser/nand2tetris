package ch.chassaing.hack.vm;

import ch.chassaing.hack.vm.command.Command;

import java.util.List;

/**
 * A representation of a program in intermediate representation called
 * ByteCode. The ByteCode holds the individual {@link Command}s
 */
public interface ByteCode
{
    /**
     * Tells the ByteCode that command from a new VM file will be added.
     * This is important for static variables that are per VM file.
     * @param filename name of the file without .vm ending
     */
    void startVmFile(String filename);

    void add(Command command);

    /**
     * Returns an unmodifiable list of {@link Command}s
     */
    List<Command> commands();
}
