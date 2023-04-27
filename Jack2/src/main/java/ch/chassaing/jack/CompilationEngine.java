package ch.chassaing.jack;

import ch.chassaing.jack.token.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.Queue;

public final class CompilationEngine
{
    @NotNull
    private final Queue<Token> tokens = new LinkedList<>();

    public CompilationEngine(@NotNull Tokenizer tokenizer)
    {
        Token token;
        while ((token = tokenizer.advance()) != null) {
            tokens.add(token);
        }
    }

    /**
     * Compile the next class in the token stream. Returns true if
     * there was a class to compile or false, if there are no more
     * classes to compile.
     */
    public boolean compileClass()
    {
        Token token = tokens.poll();
        if (token == null) {
            // no more classes
            return false;
        }

        if (!(token instanceof Keyword keyword) ||
            keyword.type != KeywordType.CLASS) {

            throw reportError("Expected token 'class'", token);
        }

        token = tokens.poll();
        if (!(token instanceof Identifier)) {

            throw reportError("Expected token 'identifier'", token);
        }

        String className = ((Identifier) token).value;
        token = tokens.poll();
        if (!(token instanceof Symbol symbol) ||
            symbol.type != SymbolType.LEFT_BRACE) {

            throw reportError("Expected token '{'", token);
        }

        token = tokens.peek();

        return true;
    }

    private RuntimeException reportError(@NotNull String message,
                                         @Nullable Token token)
    {
        return new IllegalArgumentException("%s: %s".formatted(token, message));
    }
}
