package ch.chassaing.jack.token;

import org.jetbrains.annotations.NotNull;

public record Symbol(@NotNull SymbolType type)
        implements Token
{
    public static final Symbol LEFT_BRACE = new Symbol(SymbolType.LEFT_BRACE);
    public static final Symbol RIGHT_BRACE = new Symbol(SymbolType.RIGHT_BRACE);
    public static final Symbol LEFT_PAREN = new Symbol(SymbolType.LEFT_PAREN);
    public static final Symbol RIGHT_PAREN = new Symbol(SymbolType.RIGHT_PAREN);

    public static final Symbol COMMA = new Symbol(SymbolType.COMMA);
    public static final Symbol SEMICOLON = new Symbol(SymbolType.SEMICOLON);
}
