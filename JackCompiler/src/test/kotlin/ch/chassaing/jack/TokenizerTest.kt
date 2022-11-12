package ch.chassaing.jack

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.BufferedReader
import java.io.StringReader

internal class TokenizerTest {

    @Test
    fun peekOnExhaustedBuffer() {

        val reader = BufferedReader(StringReader("gaga"))
        val tokenizer = Tokenizer(reader, buffer = CharArray(2))

        assertEquals('g', tokenizer.nextChar())
        assertEquals('a', tokenizer.peekNext())
        assertEquals('a', tokenizer.nextChar())
        assertEquals('g', tokenizer.peekNext())
        assertEquals('g', tokenizer.nextChar())
        assertEquals('a', tokenizer.peekNext())
        assertEquals('a', tokenizer.nextChar())
        assertNull(tokenizer.peekNext())
        assertNull(tokenizer.nextChar())
        assertNull(tokenizer.nextChar())
    }

    @Test
    fun aProgram() {
        val text = """
            Zlet c = 1
            let d = 2
            if (c = 2) {
              blueWhale(33)
            }
        """.trimIndent()

        val sut = Tokenizer(BufferedReader(StringReader(text)))
        var token : Token?
        do {
            token = sut.advance()
            if (token != null) {
                println(token)
            }
        } while (token != null)
    }
}