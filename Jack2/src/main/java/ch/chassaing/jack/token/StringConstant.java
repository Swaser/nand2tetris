package ch.chassaing.jack.token;

import org.jetbrains.annotations.NotNull;

public final class StringConstant
        extends Token
{
    @NotNull
    public final String value;

    public StringConstant(int lineNr,
                          @NotNull String value)
    {
        super(lineNr);
        this.value = value;
    }

    @Override
    public String toString()
    {
        return "StringConstant{" +
               "value='" + value + '\'' +
               ", lineNr=" + lineNr +
               '}';
    }
}
