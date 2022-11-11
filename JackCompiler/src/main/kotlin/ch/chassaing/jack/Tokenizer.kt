package ch.chassaing.jack

import java.io.BufferedReader
import kotlin.text.StringBuilder

class Tokenizer(
    private val input: BufferedReader,
    var currentToken: Token? = null,
    private val buffer: CharArray = CharArray(128),
    private var nRead: Int = -1,
) {

    /**
     * Schreitet in der Eingabe zum nächsten Token voran und
     * macht dieses zum aktuellen Token, falls vorhanden.
     * @return Wahr, wenn noch ein Token gelesen werden konnte, sonst Falsch
     */
    fun advance(): Token? {

        var c: Char?
        do {
            c = nextChar()
        } while (c != null && isWhitespace(c))

        if (c == null) {
            // Ende erreicht
            currentToken = null
            return null
        }

        return readToken()
    }

    /**
     * Liest das nächste Token ein und macht es zum aktuellen Token. Wenn diese
     * Methode aufgerufen wird, dann muss das aktuelle Zeichen, das erste Zeichen
     * des nächsten Tokens sein.
     */
    private fun readToken(): Token {

        val c: Char = currentChar() ?: throw IllegalStateException("readToken() called without more input")

        if (isLetter(c) || c == '_') {
            return parseKeywordOrIdentifier()
        } else if (c == '"') {
            return parseStringConstant()
        }

        throw NotImplementedError("Noch nicht fertig implementiert")
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

    private fun parseStringConstant() : Token {

        // aktuelles Zeichen ist Anführungszeichen
        var c = nextChar() ?: throw

    }

    /**
     * Liest Zeichen aus dem BufferedReader solange es hat und sie dem
     * predicate entsprechen.
     * Vorbedingung: Das aktuelle Zeichen erfüllt die Bedingung
     * Nachbedingung: Das aktuelle Zeichen ist das letzte Zeichen aus der ununterbrochenen Kette,
     * das die Bedingung erfüllt
     */
    private fun slurpWhile(predicate: (c: Char) -> Boolean): String {

        var c = currentChar()
        if (c == null || !predicate.invoke(c)) {
            return ""
        }
        val sb = StringBuilder().append(c)
        while (true) {
            c = peekNext()
            if (c != null && predicate.invoke(c)) {
                sb.append(c)
                nextChar()
            } else {
                break
            }
        }
        return sb.toString()
    }


    /**
     * Macht das nächste Zeichen zum aktuellen Zeichen und gibt es zurück.
     * Falls kein nächstes Zeichen existiert wird das aktuelle Zeichen null und
     * es wird null zurückgegeben.
     */
    internal fun nextChar(): Char? {

        if (++currentIdx >= nRead) {
            currentIdx = 0
            nRead = input.read(buffer)
        }

        return currentChar()
    }

    /**
     * Schaut auf das nächste Zeichen und gibt es zurück.
     */
    internal fun peekNext(): Char? {

        var nextIdx: Int = currentIdx + 1
        if (nextIdx >= nRead) {
            val currentChar =
                currentChar() ?: throw IllegalStateException("peekNext() called without valid current char")
            buffer[0] = currentChar
            currentIdx = 0
            nextIdx = 1
            nRead = input.read(buffer, 1, buffer.size - 1) + 1
        }
        return if (nextIdx < nRead) buffer[nextIdx] else null
    }

    /**
     * Gibt das aktuelle Zeichen zurück
     */
    private fun currentChar(): Char? {
        return if (currentIdx < nRead) buffer[currentIdx] else null
    }

    companion object {

        /**
         * Zeigt auf das aktuell zu lesende Zeichen in buffer
         */
        private var currentIdx = 0

        private val whiteSpace = setOf('\r', '\n', '\t', ' ')

        private fun isWhitespace(c: Char): Boolean {
            return whiteSpace.contains(c)
        }

        private fun isAlphaNumeric(c: Char): Boolean {
            return c == '_' || isLetter(c) || isDigit(c)
        }

        private fun isLetter(c: Char): Boolean {
            return c in 'a'..'Z'
        }

        private fun isDigit(c: Char): Boolean {
            return c in '0'..'9'
        }

    }
}