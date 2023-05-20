package ch.chassaing.jack.lang;

import ch.chassaing.jack.lang.type.Type;
import ch.chassaing.jack.lang.var.VarScope;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record VarInfo(@NotNull String name,
                      @NotNull Type type,
                      @NotNull VarScope scope,
                      int order)
{
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VarInfo varInfo = (VarInfo) o;
        return name.equals(varInfo.name);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name);
    }
}
