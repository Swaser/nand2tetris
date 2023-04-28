package ch.chassaing.jack.token;

import org.jetbrains.annotations.NotNull;

public record Identifier(@NotNull String value)
    implements Token
{}
