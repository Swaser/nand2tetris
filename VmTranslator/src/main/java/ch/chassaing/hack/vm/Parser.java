package ch.chassaing.hack.vm;

import ch.chassaing.hack.vm.command.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;

public final class Parser
        implements IParser
{
    private final ArrayList<String> lines;
    private       int               currentLine = -1;
    private       String[]          fields;

    public Parser(Path file)
    {
        lines = new ArrayList<>();
        try (InputStream is = IOUtils.toBufferedInputStream(new FileInputStream(file.toFile()))) {
            lines.addAll(IOUtils.readLines(is, StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.err.println("Problem reading file " + file);
            e.printStackTrace();
            System.exit(2);
        }
    }

    @Override
    public boolean advance()
    {
        while (++currentLine < lines.size()) {
            String nextLine = StringUtils.trim(lines.get(currentLine));
            if (nextLine.startsWith("//") || StringUtils.isBlank(nextLine)) {
                continue;
            }
            fields = StringUtils.split(nextLine);
            return true;
        }
        return false;
    }

    @Override
    public Command command()
    {
        Segment segment;
        int position;
        return switch (fields[0]) {
            case "push":
                segment = Segment.valueOf(fields[1].toUpperCase());
                position = Integer.parseInt(fields[2]);
                yield new Push(currentLine, segment, position);
            case "pop":
                segment = Segment.valueOf(fields[1].toUpperCase());
                position = Integer.parseInt(fields[2]);
                yield new Pop(currentLine, segment, position);
            case "add":
                yield new Add(currentLine);
            case "sub":
                yield new Sub(currentLine);
            case "eq":
                yield new Eq(currentLine);
            case "lt":
                yield new Lt(currentLine);
            case "gt":
                yield new Gt(currentLine);

            default:
                throw new UnsupportedOperationException("Unknown command " + fields[0]);
        };
    }
}
