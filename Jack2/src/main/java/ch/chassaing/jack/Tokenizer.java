package ch.chassaing.jack;

import ch.chassaing.jack.token.Token;
import org.jetbrains.annotations.Nullable;

public interface Tokenizer
{
    @Nullable Token advance();
}
