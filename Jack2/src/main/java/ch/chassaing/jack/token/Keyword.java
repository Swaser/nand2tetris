package ch.chassaing.jack.token;

import org.jetbrains.annotations.NotNull;

public final class Keyword
    extends Token
{
    public Keyword(@NotNull String file, int line)
    {
        super(file, line);
    }
}
