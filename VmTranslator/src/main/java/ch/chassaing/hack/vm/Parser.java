package ch.chassaing.hack.vm;

import ch.chassaing.hack.vm.command.Command;

public interface Parser
{
    /**
     * Read the next line and make it the current one. This method
     * will automatically skip comments and empty lines.
     * @return the line number of the current line or -1 if there are no more lines
     */
    int advance();

    Command command();
}
