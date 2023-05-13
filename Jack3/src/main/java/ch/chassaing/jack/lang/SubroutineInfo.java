package ch.chassaing.jack.lang;

import ch.chassaing.jack.lang.subroutine.SubroutineScope;
import ch.chassaing.jack.lang.type.Type;
import ch.chassaing.jack.lang.var.VarScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class SubroutineInfo
{
    /**
     * The class this subroutine is part of.
     */
    @NotNull
    public final ClassInfo classInfo;

    @NotNull
    public final String name;

    @NotNull
    public final SubroutineScope scope;

    /**
     * The return type of the subroutine. Can be {@code null} if
     * the type is 'void'.
     */
    @Nullable
    public final Type returnType;

    @NotNull
    public final Map<String, VarInfo> parameters = new HashMap<>();

    @NotNull
    public final Map<String, VarInfo> locals = new HashMap<>();

    public SubroutineInfo(@NotNull ClassInfo classInfo,
                          @NotNull String name,
                          @NotNull SubroutineScope scope,
                          @Nullable Type returnType)
    {
        this.classInfo = classInfo;
        this.name = name;
        this.scope = scope;
        this.returnType = returnType;
    }

    @NotNull
    public String name() {return name;}

    /**
     * A local variable can be added to the {@link SubroutineInfo} if it
     * isn't yet in the local variables.
     *
     * @return true if the variable can be added and false othervise.
     */
    public boolean addLocalVar(@NotNull String name,
                               @NotNull Type type)
    {

        if (locals.containsKey(name)) {
            return false;
        }
        VarInfo varInfo = new VarInfo(name, type, VarScope.LOCAL, locals.size());
        locals.put(name, varInfo);
        return true;
    }

    public boolean addParameter(@NotNull String name,
                                @NotNull Type type)
    {
        if (parameters.containsKey(name)) {
            return false;
        }
        VarInfo varInfo = new VarInfo(name, type, VarScope.PARAMETER, parameters.size());
        parameters.put(name, varInfo);
        return true;
    }

    @Override
    public String toString()
    {
        return "  SubroutineInfo{" +
               "\n    name='" + name + '\'' +
               ", \n    scope=" + scope +
               ", \n    returnType=" + returnType +
               ", \n    parameters=" + parameters +
               ", \n    locals=" + locals +
               "\n  }";
    }
}
