package ch.chassaing.jack

import org.junit.jupiter.api.Assertions.*
import java.io.BufferedReader
import java.io.StringReader
import kotlin.test.Test

internal class TokenizerTest {

    @Test
    fun peekOnExhaustedBuffer() {

        val reader = BufferedReader(StringReader("gaga"))
        val tokenizer = Tokenizer(reader, CharArray(2))

        assertEquals('g', tokenizer.nextChar())
        assertEquals('a', tokenizer.nextChar())
        assertEquals('g', tokenizer.peekNext())
        assertEquals('g', tokenizer.nextChar())
        assertEquals('a', tokenizer.nextChar())
        assertNull(tokenizer.peekNext())
        assertNull(tokenizer.nextChar())
        assertNull(tokenizer.nextChar())
    }
}