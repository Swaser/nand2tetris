package ch.chassaing.jack.lang.writer;

import ch.chassaing.jack.lang.Command;
import ch.chassaing.jack.lang.Segment;

import java.io.IOException;

public interface VMWriter
{
    void writePush(Segment segment, int idx);

    void writePop(Segment segment, int idx);

    void writeArithmetic(Command command);

    void writeCall(String name, int nVars);

    void writeReturn();

    void writeLabel(String name);

    void writeGoto(String name);

    void writeIf(String name);

    void writeFunction(String name, int nVars);

    void close();
}
