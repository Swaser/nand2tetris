package ch.chassaing.jack

@JvmInline
value class Bytecode(val code: String)

class CompilationEngine(private var tokenizer: Tokenizer) {

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

        val result = mutableListOf<Bytecode>()
        compileClass(result)
        return result
    }

    /**
     * Vorbedingung: das aktuelle Token ist ein Keyword(KeywordType.CLASS) Token.
     */
    internal fun compileClass(mutInstructions : MutableList<Bytecode>) {

        val currentToken = tokenizer.currentToken
        if (currentToken == null ||
            currentToken !is Token.Keyword ||
            currentToken.type != KeywordType.CLASS) {

            throw IllegalStateException("Not a class")
        }

        mutInstructions.add(Bytecode("pop 3"))
    }
}