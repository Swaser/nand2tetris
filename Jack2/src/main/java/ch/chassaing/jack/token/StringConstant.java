package ch.chassaing.jack.token;

import org.jetbrains.annotations.NotNull;

public record StringConstant(@NotNull String value)
        implements Token
{
}
