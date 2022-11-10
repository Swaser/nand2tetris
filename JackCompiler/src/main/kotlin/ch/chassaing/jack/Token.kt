package ch.chassaing.jack

sealed interface Token {

    class Keyword(val type: KeywordType) : Token

    class Symbol(val type: SymbolType) : Token

    class Identifier(val label: String) : Token

    class IntConstant(val value: Int) : Token

    class StringConstant(val value: String) : Token
}