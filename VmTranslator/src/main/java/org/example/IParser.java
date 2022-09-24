package org.example;

public interface IParser
{
    /**
     * Read the next line and make it the current one. This method
     * will automatically skip comments and empty lines.
     * Return false if there are no next lines.
     */
    boolean advance();

    /**
     * Return a constant representing the type of command
     * the current line refers to.
     */
    CommandType commandType();

    /**
     * Return the first argument of the current command.
     */
    String firstArgument();

    /**
     * Return the second argument of the current command.
     */
    int secondArgument();
}
