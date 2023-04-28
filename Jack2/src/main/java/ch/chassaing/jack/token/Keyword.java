package ch.chassaing.jack.token;

import org.jetbrains.annotations.NotNull;

public record Keyword(@NotNull KeywordType type)
    implements Token
{
    public static final Keyword CLAZZ = new Keyword(KeywordType.CLASS);
    public static final Keyword STATIC = new Keyword(KeywordType.STATIC);
    public static final Keyword FIELD = new Keyword(KeywordType.FIELD);
}
