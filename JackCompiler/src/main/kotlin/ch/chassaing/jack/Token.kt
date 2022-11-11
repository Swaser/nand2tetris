package ch.chassaing.jack

sealed interface Token {

    data class Keyword(val type: KeywordType) : Token

    data class Symbol(val type: SymbolType) : Token

    data class Identifier(val label: String) : Token

    data class IntConstant(val value: Int) : Token

    data class StringConstant(val value: String) : Token
}