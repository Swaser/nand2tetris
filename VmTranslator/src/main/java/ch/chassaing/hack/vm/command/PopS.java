package ch.chassaing.hack.vm.command;


import org.apache.commons.lang3.StringUtils;

/**
 * Take a value from the stack and put it to the address given by segment and value
 */
public record PopS(int line,
                   String value)
    implements Command
{
    public PopS
    {
        if (StringUtils.isBlank(value))
            throw new IllegalArgumentException(line + ": value must not be blank");
    }
}
