package ch.chassaing.jack.lang.type;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public sealed interface Type
        permits PrimitiveType, UserType, UnknownType, Array
{
    boolean compatible(@Nullable Type other);

    @NotNull
    static Type of(@NotNull String name) {
        if ("Array".equals(name)) {
            return Array.INSTANCE;
        } else {
            return new UserType(name);
        }
    }
}
