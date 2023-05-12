package ch.chassaing.jack.lang;

import ch.chassaing.jack.lang.type.Type;
import ch.chassaing.jack.lang.var.VarScope;
import org.jetbrains.annotations.NotNull;

public record VarInfo(@NotNull String name,
                      @NotNull Type type,
                      @NotNull VarScope scope)
{
}
