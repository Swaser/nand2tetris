package ch.chassaing.jack.lang.type;

import org.jetbrains.annotations.Nullable;

public final class UnknownType
    implements Type
{
    public final static UnknownType INSTANCE = new UnknownType();

    private UnknownType() {}

    @Override
    public boolean compatible(@Nullable Type other) {

        return true;
    }
}
