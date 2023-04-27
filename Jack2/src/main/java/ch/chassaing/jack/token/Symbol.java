package ch.chassaing.jack.token;

import org.jetbrains.annotations.NotNull;

public final class Symbol
    extends Token
{
    @NotNull
    public final SymbolType type;

    public Symbol(int lineNr,
                     @NotNull SymbolType type)
    {
        super(lineNr);
        this.type = type;
    }

    @Override
    public String toString()
    {
        return "Symbol{" +
               "type=" + type +
               ", lineNr=" + lineNr +
               '}';
    }
}
