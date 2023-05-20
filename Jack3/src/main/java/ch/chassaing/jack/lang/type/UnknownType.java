package ch.chassaing.jack.lang.type;

/**
 * Ein Typ, der bei equals immer true zurück gibt
 */
public final class UnknownType
    implements Type
{
    public final static UnknownType INSTANCE = new UnknownType();

    private UnknownType() {}

    @Override
    public String repr()
    {
        return "unknown";
    }

    @Override
    public boolean equals(Object obj)
    {
        return obj != null && Type.class.isAssignableFrom(obj.getClass());
    }

    @Override
    public int hashCode()
    {
        return -1;
    }
}
