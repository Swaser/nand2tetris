package ch.chassaing.jack.lang;

import ch.chassaing.jack.lang.type.Type;
import ch.chassaing.jack.lang.var.VarScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public final class ClassInfo
{
    private final String name;
    private final Map<String, VarInfo> vars = new HashMap<>();
    private final Map<String, SubroutineInfo> subroutines = new HashMap<>();

    private int staticIdx;
    private int fieldIdx;

    public ClassInfo(@NotNull String name)
    {
        this.name = name;
    }

    @NotNull
    public String name()
    {
        return name;
    }

    public boolean addStaticVar(@NotNull String name,
                                @NotNull Type type)
    {
        if (vars.containsKey(name)) {
            return false;
        }
        vars.put(name, new VarInfo(name, type, VarScope.STATIC, staticIdx++));
        return true;
    }

    public boolean addFieldVar(@NotNull String name,
                               @NotNull Type type)
    {
        if (vars.containsKey(name)) {
            return false;
        }
        vars.put(name, new VarInfo(name, type, VarScope.FIELD, fieldIdx++));
        return true;
    }

    public boolean addSubroutine(@NotNull SubroutineInfo subroutineInfo)
    {
        if (subroutines.containsKey(subroutineInfo.name())) {
            return false;
        }
        subroutines.put(subroutineInfo.name(), subroutineInfo);
        return true;
    }

    @Nullable
    public VarInfo findVar(String varName)
    {
        return vars.get(varName);
    }

    public int numberOfFields() {

        int num = 0;
        for (VarInfo varInfo : vars.values()) {
            if (varInfo.scope() == VarScope.FIELD) {
                num++;
            }
        }
        return num;
    }
}
