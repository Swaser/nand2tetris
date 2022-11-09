package ch.chassaing.jack

import java.io.BufferedReader
import kotlin.text.StringBuilder

class Tokenizer(
    private val input: BufferedReader,
    private val buffer : CharArray = CharArray(128),
    private var currentToken: TokenType? = null
) {

    /**
     * Schreitet in der Eingabe zum nächsten Token voran und
     * macht dieses zum aktuellen Token, falls vorhanden.
     * @return Wahr, wenn noch ein Token gelesen werden konnte, sonst Falsch
     */
    fun advance(): Boolean {

        var c : Char?
        do {
            c = nextChar()
        } while (c != null && isWhitespace(c))

        if (c == null) {
            // Ende erreicht
            currentToken = null
            return false
        }

        readToken()
        return true
    }
    /**
     * Gibt den Typ des aktuellen Tokens aus.
     * @throws NoSuchElementException, falls kein aktuelles Token vorhanden ist
     */
    fun tokenType(): TokenType {
        return currentToken ?: throw NoSuchElementException("No current token")
    }

    /**
     * Liest das nächste Token ein und macht es zum aktuellen Token. Das aktuelle
     * Zeichen ist das nächste nicht-Whitespace.
     */
    private fun readToken() {

        var c : Char? = currentChar()
        if (c == null) {
            throw IllegalStateException("readToken() called without more input")
        }

        sb.clear()
        if (isLetter(c) || c == '_') {
            // entweder Keyword oder Identifier
            sb.append(c)
            while (true) {
                c = peekNext()
                if (c != null && isAlphaNumeric(c)) {
                    sb.append(c)
                    nextChar()
                }
            }
            // nun mit Keyword vergleichen
        }

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

    internal fun peekNext(): Char? {

        var nextIdx : Int = currentIdx + 1
        if (nextIdx >= nRead) {
            val currentChar = currentChar() ?: throw IllegalStateException("peekNext() called without valid current char")
            buffer[0] = currentChar
            currentIdx = 0
            nextIdx = 1
            nRead = input.read(buffer, 1, buffer.size-1) + 1
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

        private var nRead = -1
        private val sb = StringBuilder()

        /**
         * Zeigt auf das aktuell zu lesende Zeichen in buffer
         */
        private var currentIdx = 0

        private val whiteSpace = setOf('\r','\n','\t',' ')

        private fun isWhitespace(c : Char): Boolean {
            return whiteSpace.contains(c)
        }

        private fun isAlphaNumeric(c : Char) : Boolean {
            return c == '_' || isLetter(c) || isDigit(c)
        }

        private fun isLetter(c : Char): Boolean {
            return c in 'a'.. 'Z'
        }

        private fun isDigit(c : Char): Boolean {
            return c in '0'..'9'
        }

    }
}