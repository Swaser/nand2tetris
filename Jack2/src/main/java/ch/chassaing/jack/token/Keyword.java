package ch.chassaing.jack.token;

import org.jetbrains.annotations.NotNull;

public record Keyword(@NotNull KeywordType type)
    implements Token
{
    public static final Keyword CLAZZ = new Keyword(KeywordType.CLASS);

    public static final Keyword STATIC = new Keyword(KeywordType.STATIC);
    public static final Keyword FIELD = new Keyword(KeywordType.FIELD);

    public static final Keyword CONSTRUCTOR = new Keyword(KeywordType.CONSTRUCTOR);
    public static final Keyword FUNCTION = new Keyword(KeywordType.FUNCTION);
    public static final Keyword METHOD = new Keyword(KeywordType.METHOD);

    public static final Keyword VOID = new Keyword(KeywordType.VOID);
    public static final Keyword INT = new Keyword(KeywordType.INT);
    public static final Keyword CHAR = new Keyword(KeywordType.CHAR);
    public static final Keyword BOOLEAN = new Keyword(KeywordType.BOOLEAN);
}
