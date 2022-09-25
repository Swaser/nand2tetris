package ch.chassaing.hack.vm;

import ch.chassaing.hack.vm.command.Command;

public interface IParser
{
    /**
     * Read the next line and make it the current one. This method
     * will automatically skip comments and empty lines.
     * Return false if there are no next lines.
     */
    boolean advance();

    Command command();
}
