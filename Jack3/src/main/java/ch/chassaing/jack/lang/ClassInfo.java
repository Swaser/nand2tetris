package ch.chassaing.jack.lang;

import ch.chassaing.jack.lang.type.Type;
import ch.chassaing.jack.lang.var.VarScope;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public record ClassInfo(@NotNull String name,
                        @NotNull Map<String, VarInfo> statics,
                        @NotNull Map<String, VarInfo> fields,
                        @NotNull Map<String, SubroutineInfo> subroutines)
{
    public ClassInfo(@NotNull String name)
    {
        this(name, new HashMap<>(), new HashMap<>(), new HashMap<>());
    }

    public boolean addStaticVar(@NotNull String name,
                                @NotNull Type type)
    {
        if (varIsDuplicate(name)) {
            return false;
        }
        VarInfo varInfo = new VarInfo(name, type, VarScope.STATIC, statics.size());
        statics.put(name, varInfo);
        return true;
    }

    public boolean addFieldVar(@NotNull String name,
                               @NotNull Type type)
    {
        if (varIsDuplicate(name)) {
            return false;
        }
        VarInfo varInfo = new VarInfo(name, type, VarScope.FIELD, fields.size());
        fields.put(name, varInfo);
        return true;
    }

    private boolean varIsDuplicate(@NotNull String varName)
    {
        return statics.containsKey(varName) || fields.containsKey(varName);
    }

    public boolean addSubroutine(@NotNull SubroutineInfo subroutineInfo)
    {
        if (subroutines.containsKey(subroutineInfo.name())) {
            return false;
        }
        subroutines.put(subroutineInfo.name(), subroutineInfo);
        return true;
    }
}
