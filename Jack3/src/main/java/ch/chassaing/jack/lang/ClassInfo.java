package ch.chassaing.jack.lang;

import ch.chassaing.jack.lang.type.Type;
import ch.chassaing.jack.lang.var.VarScope;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class ClassInfo
{
    private final String name;
    private final Map<String, VarInfo> statics = new HashMap<>();
    private final Map<String, VarInfo> fields = new HashMap<>();
    private final Map<String, SubroutineInfo> subroutines = new HashMap<>();

    public ClassInfo(@NotNull String name)
    {
        this.name = name;
    }

    @NotNull
    public String getName() {return name;}

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

    @Override
    public String toString()
    {
        String sr = subroutines
                .values()
                .stream()
                .map(Object::toString)
                .collect(Collectors.joining("\n"));

        return "ClassInfo{" +
               "\n  name='" + name + '\'' +
               ", \n  statics=" + statics +
               ", \n  fields=" + fields +
               ", \n" + sr +
               "\n}";
    }
}
