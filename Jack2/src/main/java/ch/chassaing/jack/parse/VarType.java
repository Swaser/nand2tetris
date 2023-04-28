package ch.chassaing.jack.parse;

import org.jetbrains.annotations.NotNull;

public sealed interface VarType
{
    enum PrimitiveType
    {
        INT, CHAR, BOOLEAN
    }

    record Primitive(@NotNull PrimitiveType type)
            implements VarType {}

    record UserType(@NotNull String name)
            implements VarType {}
}
