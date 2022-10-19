package ch.chassaing.hack.vm;

import ch.chassaing.hack.vm.command.Command;

public interface ICodeWriter
{
    /**
     * Static variables and function labels are dependend on the name of
     * the VM program. Therefore, the name of the program must be given before
     * these command can be used.
     * @param progName name of input file without the .vm ending
     */
    void setProgName(String progName);

    void add(Command command);

    /**
     * Will output all instructions added to it plus the necessary bootstrap code.
     */
    Iterable<String> getInstructions();
}
