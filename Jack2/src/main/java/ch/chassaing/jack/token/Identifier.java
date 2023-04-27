package ch.chassaing.jack.token;

import org.jetbrains.annotations.NotNull;

public final class Identifier
    extends Token
{
    @NotNull
    public final String value;

    public Identifier(int lineNr,
                      @NotNull String value)
    {
        super(lineNr);
        this.value = value;
    }

    @Override
    public String toString()
    {
        return "Identifier{" +
               "value='" + value + '\'' +
               ", lineNr=" + lineNr +
               '}';
    }
}
