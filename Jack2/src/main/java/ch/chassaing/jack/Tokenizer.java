package ch.chassaing.jack;

import ch.chassaing.jack.token.Token;
import org.jetbrains.annotations.Nullable;

public interface Tokenizer
{
    /** Retrieves the next token and makes it the current token */
    @Nullable Token advance();

    @Nullable Token peek();

    /** The line number of the current token */
    int lineNumber();
}
