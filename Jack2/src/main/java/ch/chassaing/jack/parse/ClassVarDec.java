package ch.chassaing.jack.parse;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record ClassVarDec(@NotNull Qualifier qualifier,
                          @NotNull VarDec varDec)
{
    @NotNull
    public String name()
    {
        return varDec().name();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassVarDec that = (ClassVarDec) o;
        return varDec.equals(that.varDec);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(varDec);
    }
}
