package ch.chassaing.jack.lang.type;

public record UserType(String name)
        implements Type
{
    @Override
    public String repr()
    {
        return name;
    }
}
