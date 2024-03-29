package ch.chassaing.jack.lang.type;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class UserType
        implements Type {
    private final String name;

    UserType(@NotNull String name) {this.name = name;}

    @NotNull
    public String name() {return name;}

    @Override
    public boolean compatible(@Nullable Type other) {

        return UnknownType.INSTANCE == other ||
               (other instanceof UserType otherTyped && otherTyped.name.equals(this.name));
    }
}
