package ch.chassaing.jack

import java.io.BufferedReader
import kotlin.text.StringBuilder

class Tokenizer(
    private val input: BufferedReader,
) {
    var currentToken: Token? = null
        private set

    /**
     * Schreitet in der Eingabe zum nächsten Token voran und
     * macht dieses zum aktuellen Token, falls vorhanden.
     * @return Wahr, wenn noch ein Token gelesen werden konnte, sonst Falsch
     */
    fun advance(): Token? {

        var c = nextChar()
        while (c != null) {
            if (c.isWhitespace()) {
                c = nextChar()
                continue
            } else if (c == '/') {
                val next = peekNext()
                if (next == '/') {
                    // Zeilenkommentar, d.h. Rest der Zeile wird ignoriert
                    nextLine()
                    c = currentChar()
                    continue
                } else if (next == '*') {
                    var endFound = false
                    while (!endFound) {
                        slurpWhile { it != '*' }
                        nextChar() // akutelles Zeichen ist nun *
                        if (peekNext() == '/') {
                            nextChar()
                            endFound = true
                        }
                    }
                    c = nextChar()
                    continue
                }
            }

            if (isLetter(c) || c == '_') {
                return parseKeywordOrIdentifier()
            } else if (c == '"') {
                return parseStringConstant()
            } else if (isDigit(c)) {
                return parseIntegerConstant()
            }

            // Die anderen Möglichkeiten sind erschöpft --> Symbol
            return parseSymbol()
        }

        // Hier ist c == null, d.h. es gibt keine Zeichen mehr
        return null
    }

    /**
     * Vorbedingung: Das aktuelle Zeichen ist ein Buchstabe oder ein Unterstrich.
     * Dann liest diese Methode alle Zeichen die zu einem Keyword oder Identifier
     * passen und bestimmt danach das Token.
     * Nachbedingung: Das Token ist gefunden und das aktuelle Zeichen ist das letzte
     * Zeichen des Tokens.
     */
    private fun parseKeywordOrIdentifier(): Token {

        val content = slurpWhile { isAlphaNumeric(it) }

        for (keyword in KeywordType.values()) {
            if (keyword.name.lowercase() == content) {
                currentToken = Token.Keyword(keyword)
                return currentToken!!
            }
        }
        currentToken = Token.Identifier(content)
        return currentToken!!
    }

    private fun parseStringConstant(): Token {

        // Konsumiere das nächste Zeichen
        val c = currentChar()
        if (c == null || c != '"') {
            throw IllegalStateException("Nächstes Zeichen muss ein Anführungszeichen sein: $c")
        }

        val content = slurpWhile { it != '"' }
        nextChar() // wir konsumieren auch noch das hintere Anführungszeichen
        return Token.StringConstant(content)
    }

    private fun parseIntegerConstant(): Token {

        val content = slurpWhile { isDigit(it) }
        return Token.IntConstant(content.toInt(10))
    }

    private fun parseSymbol(): Token {

        val c = currentChar() ?: throw IllegalStateException("Kein nächstes Zeichen vorhanden")
        for (symbol in SymbolType.values()) {
            if (c == symbol.c) {
                return Token.Symbol(symbol)
            }
        }
        throw IllegalStateException("$c ist kein Symbol")
    }

    /**
     * Liest Zeichen ein, solange sie der Bedingung entsprechen, angefangen beim aktuellen Zeichen.
     * Nachbedingung: Das aktuelle Zeichen ist das letzte Zeichen aus der ununterbrochenen Kette,
     * das die Bedingung erfüllt
     */
    private fun slurpWhile(predicate: (c: Char) -> Boolean): String {

        var c = currentChar()
        if (c == null || !predicate.invoke(c)) {
            return ""
        }
        val sb = StringBuilder().append(c)
        c = peekNext()

        while (c != null && predicate.invoke(c)) {
            sb.append(c)
            nextChar()
            c = peekNext()
        }
        return sb.toString()
    }

    /**
     * Die aktuelle Zeile, die vom Tokenizer verarbeitet wird
     */
    private var line: String? = null
    private var index: Int = -1

    /**
     * Macht das nächste Zeichen zum aktuellen Zeichen und gibt es zurück.
     * Falls kein nächstes Zeichen existiert wird das aktuelle Zeichen null und
     * es wird null zurückgegeben.
     */
    internal fun nextChar(): Char? {

        if (++index >= (line?.length ?: 0)) {
            nextLine()
        }

        return currentChar()
    }

    private fun nextLine() {
        line = input.readLine()?.trim()
        index = 0
    }

    /**
     * Gibt das aktuelle Zeichen zurück, oder null, falls
     *
     */
    private fun currentChar(): Char? {

        if (index < 0) {
            return '\n'
        } else if (line == null) {
            return null
        } else if (line!!.isEmpty()) {
            return '\n'
        }
        return line!![index]
    }

    /**
     * Schaut auf das nächste Zeichen und gibt es zurück.
     */
    internal fun peekNext(): Char? {

        return if (line == null) {
            null
        } else if (index + 1 >= line!!.length) {
            '\n'
        } else {
            line!![index + 1]
        }
    }

    companion object {

        private val whiteSpace = setOf('\r', '\n', '\t', ' ')

        private fun isWhitespace(c: Char): Boolean {
            return whiteSpace.contains(c)
        }

        private fun isAlphaNumeric(c: Char): Boolean {
            return c == '_' || isLetter(c) || isDigit(c)
        }

        private fun isLetter(c: Char): Boolean {
            return c.isLetter()
        }

        private fun isDigit(c: Char): Boolean {
            return c in '0'..'9'
        }
    }
}