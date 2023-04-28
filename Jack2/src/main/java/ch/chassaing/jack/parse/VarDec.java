package ch.chassaing.jack.parse;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record VarDec(@NotNull VarType type,
                     @NotNull String name)
{
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VarDec that = (VarDec) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name);
    }
}
