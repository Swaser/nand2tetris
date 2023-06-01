package ch.chassaing.jack.lang.type;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum PrimitiveType
        implements Type
{
    INT, CHAR, BOOLEAN;

    @Override
    public boolean compatible(@Nullable Type other) {

        return switch (this) {

            case INT, CHAR -> UnknownType.INSTANCE == other ||
                              INT == other ||
                              CHAR == other;
            case BOOLEAN -> UnknownType.INSTANCE == other ||
                            BOOLEAN == other;
        };
    }
}
