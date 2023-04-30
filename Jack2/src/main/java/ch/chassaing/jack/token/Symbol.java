package ch.chassaing.jack.token;

public enum Symbol
    implements Token
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

    Symbol(char repr) {this.repr = repr;}
}
