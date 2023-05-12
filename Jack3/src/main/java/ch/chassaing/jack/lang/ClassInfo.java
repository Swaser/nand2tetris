package ch.chassaing.jack.lang;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ClassInfo
{
    private final String name;
    private final Map<String, VarInfo> statics = new HashMap<>();
    private final Map<String, VarInfo> fields = new HashMap<>();
    private final Map<String, SubroutineInfo> subroutines = new HashMap<>();

    public ClassInfo(String name)
    {
        this.name = name;
    }

    public boolean addStaticVar(@NotNull VarInfo varInfo)
    {
        if (varIsDuplicate(varInfo.name())) {
            return false;
        }
        statics.put(varInfo.name(), varInfo);
        return true;
    }

    public boolean addFieldVar(@NotNull VarInfo varInfo)
    {
        if (varIsDuplicate(varInfo.name())) {
            return false;
        }
        fields.put(varInfo.name(), varInfo);
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
