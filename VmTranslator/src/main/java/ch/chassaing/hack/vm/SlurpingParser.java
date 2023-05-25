package ch.chassaing.hack.vm;

import ch.chassaing.hack.vm.command.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public final class SlurpingParser
        implements Parser
{
    private final ArrayList<String> lines;
    private       int               currentLine = 0;
    private       String[]          fields;

    public SlurpingParser(File file)
    {
        lines = new ArrayList<>();
        try (InputStream is = IOUtils.toBufferedInputStream(new FileInputStream(file))) {
            lines.addAll(IOUtils.readLines(is, StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.err.println("Problem reading file " + file.getName());
            e.printStackTrace();
            System.exit(2);
        }
    }

    @Override
    public int advance()
    {
        while (currentLine++ < lines.size()) {
            String nextLine = StringUtils.trim(lines.get(currentLine-1)); // lines[] ist 0-basiert
            if (nextLine.startsWith("//") || StringUtils.isBlank(nextLine)) {
                continue;
            }
            fields = StringUtils.split(nextLine);
            return currentLine;
        }
        return -1;
    }

    @Override
    public Command command()
    {
        return switch (fields[0]) {
            case "push" -> new Push(currentLine,
                                    Segment.valueOf(fields[1].toUpperCase()),
                                    Integer.parseInt(fields[2]));
            case "pop" -> new Pop(currentLine,
                                  Segment.valueOf(fields[1].toUpperCase()),
                                  Integer.parseInt(fields[2]));
            case "add" -> new Add(currentLine);
            case "sub" -> new Sub(currentLine);
            case "and" -> new And(currentLine);
            case "or" -> new Or(currentLine);
            case "eq" -> new Eq(currentLine);
            case "lt" -> new Lt(currentLine);
            case "gt" -> new Gt(currentLine);
            case "neg" -> new Neg(currentLine);
            case "not" -> new Not(currentLine);
            case "function" -> new Function(currentLine,
                                            fields[1],
                                            Integer.parseInt(fields[2]));
            case "return" -> new Return(currentLine);
            case "call" -> new Call(currentLine,
                                    fields[1],
                                    Integer.parseInt(fields[2]));
            case "label" -> new Label(currentLine, fields[1]);
            case "goto" -> new Goto(currentLine, fields[1]);
            case "if-goto" -> new IfGoto(currentLine, fields[1]);
            default -> throw new UnsupportedOperationException("Unknown command " + fields[0]);
        };
    }
}
