package ch.chassaing.jack.token;

import org.jetbrains.annotations.NotNull;

public abstract class Token
{
    @NotNull public final String file;
    public final int line;

    protected Token(@NotNull String file, int line)
    {
        this.file = file;
        this.line = line;
    }
}
