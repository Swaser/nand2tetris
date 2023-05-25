package ch.chassaing.jack.lang;

import ch.chassaing.jack.lang.subroutine.SubroutineScope;
import ch.chassaing.jack.lang.type.Type;
import ch.chassaing.jack.lang.var.VarScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class SubroutineInfo
{
    private final @NotNull ClassInfo classInfo;
    private final @NotNull String name;
    private final @NotNull SubroutineScope scope;
    private final @Nullable Type returnType;
    private final @NotNull Map<String, VarInfo> vars;

    private int paramIdx;
    private int localIdx;
    private int labelNo;

    public SubroutineInfo(@NotNull ClassInfo classInfo,
                          @NotNull String name,
                          @NotNull SubroutineScope scope,
                          @Nullable Type returnType)
    {
        this.classInfo = classInfo;
        this.name = name;
        this.scope = scope;
        this.returnType = returnType;
        this.vars = new HashMap<>();
    }

    @NotNull
    public String name()
    {
        return name;
    }

    @NotNull
    public SubroutineScope scope() {return scope;}

    /**
     * A local variable can be added to the {@link SubroutineInfo} if it
     * isn't yet in the local variables.
     *
     * @return true if the variable can be added and false othervise.
     */
    public boolean addLocalVar(@NotNull String name,
                               @NotNull Type type)
    {
        if (vars.containsKey(name)) {
            return false;
        }
        vars.put(name, new VarInfo(name, type, VarScope.LOCAL, localIdx++));
        return true;
    }

    public boolean addParameter(@NotNull String name,
                                @NotNull Type type)
    {
        if (vars.containsKey(name)) {
            return false;
        }
        vars.put(name, new VarInfo(name, type, VarScope.PARAMETER, paramIdx++));
        return true;
    }

    @Nullable
    public VarInfo findVar(@NotNull String varName)
    {
        if (vars.containsKey(varName)) {
            return vars.get(varName);
        }
        return classInfo.findVar(varName);
    }

    @Override
    public String toString()
    {
        return "SubroutineInfo{" +
               "name='" + name + '\'' +
               ", scope=" + scope +
               ", returnType=" + returnType +
               ", vars=" + vars +
               '}';
    }

    public @Nullable Type returnType() {return returnType;}

    public String nextLabel()
    {
        return "%s.%s_%d".formatted(classInfo.name(), name, labelNo++);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (SubroutineInfo) obj;
        return Objects.equals(this.classInfo, that.classInfo) &&
               Objects.equals(this.name, that.name) &&
               Objects.equals(this.scope, that.scope) &&
               Objects.equals(this.returnType, that.returnType) &&
               Objects.equals(this.vars, that.vars);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(classInfo, name, scope, returnType, vars);
    }
}
