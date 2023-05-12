package ch.chassaing.jack.lang.type;

public enum PrimitiveType
        implements Type
{
    INT, CHAR, BOOLEAN;

    @Override
    public String repr()
    {
        return name().toLowerCase();
    }
}
