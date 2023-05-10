package ch.chassaing.jack.lang;

public sealed interface VarType
        permits PrimitiveType, UserType
{
    String repr();
}
