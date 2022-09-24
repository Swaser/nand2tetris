package org.example;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;

public final class Parser
        implements IParser
{
    private final ArrayList<String> lines;
    private       int               currentIndex = -1;
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
        while (++currentIndex < lines.size()) {
            String nextLine = StringUtils.trim(lines.get(currentIndex));
            if (nextLine.startsWith("//") || StringUtils.isBlank(nextLine)) {
                continue;
            }
            fields = StringUtils.split(nextLine);
            return true;
        }
        return false;
    }

    @Override
    public CommandType commandType()
    {
        return switch (fields[0]) {
            case "push": yield CommandType.PUSH;
            case "pop": yield CommandType.POP;
            case "add":
            case "sub":
            case "neg":
                yield CommandType.ARITHMETIC;

        }
    }

    @Override
    public String firstArgument()
    {
        return null;
    }

    @Override
    public int secondArgument()
    {
        return 0;
    }
}
