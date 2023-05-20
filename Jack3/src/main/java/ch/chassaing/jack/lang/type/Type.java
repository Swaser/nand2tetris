package ch.chassaing.jack.lang.type;

public sealed interface Type
        permits PrimitiveType, UserType, UnknownType
{
    String repr();
}
