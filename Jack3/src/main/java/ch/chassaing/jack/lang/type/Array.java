package ch.chassaing.jack.lang.type;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Array
        implements Type {

    public static final Array INSTANCE = new Array();

    private Array() {}

    @Override
    public boolean compatible(@Nullable Type other) {

        return UnknownType.INSTANCE == other ||
               Array.INSTANCE == other ||
               PrimitiveType.INT == other; // this stupid lang allows assigning any base address to an Array type
    }
}
