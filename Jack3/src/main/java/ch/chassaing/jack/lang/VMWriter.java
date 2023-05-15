package ch.chassaing.jack.lang;

import java.io.IOException;

public interface VMWriter
{
    void writePush(Segment segment, int idx);

    void writePop(Segment segment, int idx);

    void writeArithmetic(Command command);

    void writeCall(String name, int nVars);
}
