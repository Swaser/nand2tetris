package ch.chassaing.hack.vm.command;

import org.apache.commons.lang3.StringUtils;

/**
 * Read value from segment and push it onto stack
 */
public record PushS(int line,
                    String value)
        implements Command
{
    public PushS
    {
        if (StringUtils.isBlank(value))
            throw new IllegalArgumentException(line + ": value must not be blank");
    }
}
