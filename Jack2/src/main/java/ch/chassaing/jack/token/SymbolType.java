package ch.chassaing.jack.token;

public enum SymbolType
{
    LEFT_BRACE('{'), RIGHT_BRACE('}'),
    LEFT_PAREN('('), RIGHT_PAREN(')'),
    LEFT_BRACKET('['), RIGHT_BRACKET(']'),
    DOT('.'), COMMA(','), SEMICOLON(';'),
    PLUS('+'), MINUS('-'),
    STAR('*'), SLASH('/'),
    AMP('&'), PIPE('|'),
    LT('<'), GT('>'), EQUAL('='),
    NEG('~');

    public final char repr;

    SymbolType(char repr) {this.repr = repr;}
}
