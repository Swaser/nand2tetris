package ch.chassaing.jack

enum class SymbolType(val c : Char)  {

    LEFT_BRACE('{'), RIGHT_BRACE('}'),
    LEFT_PAREN('('), RIGHT_PAREN(')'),
    LEFT_BRAKET('['), RIGHT_BRAKET(']'),
    DOT('.'), COMMA(','), SEMICOLON(';'),
    PLUS('+'), MINUS('-'),
    STAR('*'), SLASH('/'),
    AMP('&'), PIPE('|'),
    LT('<'), GT('>'), EQUAL('='),
    NEG('~');
}