package ch.chassaing.jack.token;

import org.jetbrains.annotations.NotNull;

public record Symbol(@NotNull SymbolType type)
        implements Token
{
    public static final Symbol LEFT_BRACE = new Symbol(SymbolType.LEFT_BRACE);
    public static final Symbol RIGHT_BRACE = new Symbol(SymbolType.RIGHT_BRACE);
    public static final Symbol LEFT_PAREN = new Symbol(SymbolType.LEFT_PAREN);
    public static final Symbol RIGHT_PAREN = new Symbol(SymbolType.RIGHT_PAREN);
    public static final Symbol LEFT_BRACKET = new Symbol(SymbolType.LEFT_BRACKET);
    public static final Symbol RIGHT_BRACKET = new Symbol(SymbolType.RIGHT_BRACKET);

    public static final Symbol COMMA = new Symbol(SymbolType.COMMA);
    public static final Symbol SEMICOLON = new Symbol(SymbolType.SEMICOLON);

    public static final Symbol EQUAL = new Symbol(SymbolType.EQUAL);
    public static final Symbol PLUS = new Symbol(SymbolType.PLUS);
    public static final Symbol MINUS = new Symbol(SymbolType.MINUS);
    public static final Symbol STAR = new Symbol(SymbolType.STAR);
    public static final Symbol SLASH = new Symbol(SymbolType.SLASH);
}
