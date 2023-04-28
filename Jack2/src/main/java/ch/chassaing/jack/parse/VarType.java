package ch.chassaing.jack.parse;

import ch.chassaing.jack.token.Identifier;
import ch.chassaing.jack.token.Keyword;
import ch.chassaing.jack.token.Token;
import io.vavr.control.Option;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public sealed interface VarType
{
    enum PrimitiveType
    {
        INT, CHAR, BOOLEAN
    }

    VarType INT = new Primitive(PrimitiveType.INT);
    VarType CHAR = new Primitive(PrimitiveType.CHAR);
    VarType BOOLEAN = new Primitive(PrimitiveType.BOOLEAN);

    @Nullable
    static VarType fromToken(@Nullable Token token)
    {
        if (Keyword.INT.equals(token)) {
            return INT;
        } else if (Keyword.CHAR.equals(token)) {
            return CHAR;
        } else if (Keyword.BOOLEAN.equals(token)) {
            return BOOLEAN;
        } else if (token instanceof Identifier identifier) {
            return new UserType(identifier.value());
        }
        return null;
    }

    record Primitive(@NotNull PrimitiveType type)
            implements VarType
    {
        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Primitive primitive = (Primitive) o;
            return type == primitive.type;
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(type);
        }
    }

    record UserType(@NotNull String name)
            implements VarType
    {

        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            UserType userType = (UserType) o;
            return name.equals(userType.name);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(name);
        }
    }
}
