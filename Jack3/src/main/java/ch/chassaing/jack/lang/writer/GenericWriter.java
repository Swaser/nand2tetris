package ch.chassaing.jack.lang.writer;

import ch.chassaing.jack.lang.Command;
import ch.chassaing.jack.lang.Segment;

import java.io.IOException;
import java.io.Writer;

public class GenericWriter implements VMWriter
{
    public static class Exception extends RuntimeException {

        public Exception(String message)
        {
            super(message);
        }

        public Exception(String message, Throwable cause)
        {
            super(message, cause);
        }

        public Exception(Throwable cause)
        {
            super(cause);
        }
    }

    private final Writer writer;

    public GenericWriter(Writer writer) {this.writer = writer;}

    private void write(String s) {
        try {
            writer.write(s);
        }
        catch (IOException e) {
            throw new Exception(e);
        }
    }

    @Override
    public void writePush(Segment segment, int idx)
    {
        write("push %s %d%n".formatted(segment.name().toLowerCase(), idx));
    }

    @Override
    public void writePop(Segment segment, int idx)
    {
        write("pop %s %d%n".formatted(segment.name().toLowerCase(), idx));
    }

    @Override
    public void writeArithmetic(Command command)
    {
        write("%s%n".formatted(command.name().toLowerCase()));
    }

    @Override
    public void writeCall(String name, int nVars)
    {
        write("call %s %d%n".formatted(name, nVars));
    }

    @Override
    public void writeReturn()
    {
        write("return\n");
    }

    @Override
    public void writeLabel(String name)
    {
        write("label %s%n".formatted(name));
    }

    @Override
    public void writeGoto(String name)
    {
        write("goto %s%n".formatted(name));
    }

    @Override
    public void writeIf(String name)
    {
        write("if-goto %s%n".formatted(name));
    }

    @Override
    public void writeFunction(String name, int nVars)
    {
        write("function %s %d%n".formatted(name, nVars));
    }

    @Override
    public void close()
    {
        try {
            writer.close();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
