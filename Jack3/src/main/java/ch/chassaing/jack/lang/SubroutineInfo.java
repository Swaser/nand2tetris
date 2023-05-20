package ch.chassaing.jack.lang;

import ch.chassaing.jack.lang.subroutine.SubroutineScope;
import ch.chassaing.jack.lang.type.Type;
import ch.chassaing.jack.lang.var.VarScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class SubroutineInfo
{
    private final @NotNull ClassInfo classInfo;
    private final @NotNull String name;
    private final @NotNull SubroutineScope scope;
    private final @Nullable Type returnType;
    private final @NotNull Map<String, VarInfo> parameters;
    private final @NotNull Map<String, VarInfo> locals;
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
        this.parameters = new HashMap<>();
        this.locals = new HashMap<>();
    }

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

    @NotNull
    public Optional<VarInfo> findVar(@NotNull String varName)
    {
        if (locals.containsKey(varName))
            return Optional.of(locals.get(varName));
        else if (parameters.containsKey(varName))
            return Optional.of(parameters.get(varName));
        else if (scope != SubroutineScope.FUNCTION && classInfo.fields().containsKey(varName))
            return Optional.of(classInfo.fields().get(varName));
        else if (classInfo.statics().containsKey(varName))
            return Optional.of(classInfo.statics().get(varName));
        else
            return Optional.empty();
    }

    @Override
    public String toString()
    {
        return "SubroutineInfo{" +
               "name='" + name + '\'' +
               ", scope=" + scope +
               ", returnType=" + returnType +
               ", parameters=" + parameters +
               ", locals=" + locals +
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
               Objects.equals(this.parameters, that.parameters) &&
               Objects.equals(this.locals, that.locals);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(classInfo, name, scope, returnType, parameters, locals);
    }

}
