package ch.chassaing.jack.lang;

public enum PrimitiveType
        implements VarType
{
    INT, CHAR, BOOLEAN;

    @Override
    public String repr()
    {
        return name().toLowerCase();
    }
}
