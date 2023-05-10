package ch.chassaing.jack.lang;

public record UserType(String name)
        implements VarType
{
    @Override
    public String repr()
    {
        return name;
    }
}
