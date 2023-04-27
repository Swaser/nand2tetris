package ch.chassaing.jack.token;

import org.jetbrains.annotations.NotNull;

public final class Keyword
    extends Token
{
    @NotNull
    public final KeywordType type;

    public Keyword(int line,
                   @NotNull KeywordType type)
    {
        super(line);
        this.type = type;
    }

    @Override
    public String toString()
    {
        return "Keyword{" +
               "type=" + type +
               ", lineNr=" + lineNr +
               '}';
    }
}
