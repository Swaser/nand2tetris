package ch.chassaing.jack

enum class TokenType {

    KEYWORD,      // Reserviertes Wort
    SYMBOL,       // Ein Zeichen
    IDENTIFIER,   // Muss mit Buchstabe oder _ anfangen
    INT_CONST,    // 123
    STRING_CONST  // "ein Beispiel"
}