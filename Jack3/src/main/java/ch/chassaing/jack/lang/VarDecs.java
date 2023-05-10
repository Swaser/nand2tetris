package ch.chassaing.jack.lang;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

public record VarDecs(@NotNull VarType varType,
                      @NotNull Set<String> names)
{
}
