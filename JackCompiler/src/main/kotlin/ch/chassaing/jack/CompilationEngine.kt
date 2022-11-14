package ch.chassaing.jack

@JvmInline
value class Bytecode(val code: String)

class CompilationEngine(private var tokenizer: Tokenizer) {

    private var bytecode = mutableListOf<Bytecode>()

    /**
     * Geht durch die Folge von Tokens und erstellt daraus eine Liste
     * von Jack Bytecode Instruktionen. Dabei wird immer bei einer Klasse
     * gestartet. D.h. die Tokens müssen eine Klasse beschreiben.
     * @return Eine Liste von Bytecode Instruktionen
     */
    fun compile(): List<Bytecode> {

        val firstToken = tokenizer.advance()
        if (firstToken == null) {
            return emptyList()
        }

        compileClass()
        return bytecode
    }

    internal fun compileClass() {

        processKeyword(KeywordType.CLASS)
        val className = processIdentifier()
        println("<class name=$className>")

        println("</class>")
    }

    private fun processKeyword(keywordType: KeywordType) {

        val token = tokenizer.advance() ?: throw IllegalStateException("No more tokens")
        if (token is Token.Keyword && token.type == keywordType) {
            return
        }
        throw IllegalStateException("Not a ${keywordType.name} keyword: $token")
    }

    private fun processIdentifier() : String {

        val token = tokenizer.advance() ?: throw IllegalStateException("No more tokens")
        if (token is Token.Identifier) {
            return token.label
        }
        throw IllegalStateException("Not an indentifier: $token")
    }
}