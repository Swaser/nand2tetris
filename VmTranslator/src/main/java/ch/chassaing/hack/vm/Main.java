package ch.chassaing.hack.vm;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

public class Main
{
    public static void main(String[] args)
    {
        String vmFilename = args[0];
        if (!vmFilename.endsWith(".vm")) {
            System.out.println("File must end with .vm");
            System.exit(1);
        }
        String firstChar = vmFilename.substring(0, 1);
        if (!firstChar.equals(firstChar.toUpperCase())) {
            System.out.println("File must start with an uppercase letter");
            System.exit(1);
        }
        Path vmFile = Path.of(vmFilename);
        Path asmFile = Path.of(vmFilename.replace(".vm", ".asm"));

        boolean readingOk = true;
        List<String> lines = new LinkedList<>();
        try (InputStream is = new BufferedInputStream(new FileInputStream(vmFile.toFile())))
        {
            lines = IOUtils.readLines(is, StandardCharsets.UTF_8);
        }
        catch (IOException e)
        {
            System.out.println("Exception: " + e.getMessage());
            readingOk = false;
        }

        if (!readingOk) {
            System.exit(2);
        }

        IParser parser = new Parser(lines);
        ICodeWriter codeWriter = new HackWriter();

        int line;
        while ((line = parser.advance()) > 0) {
            try {
                codeWriter.add(parser.command());
            } catch (Exception e) {
                System.out.println(line + ": " + e.getMessage());
                System.exit(3);
            }
        }

        try (OutputStreamWriter writer = openForWriting(asmFile)) {
            int iCount = 0;
            Iterable<String> instructions = codeWriter.getInstructions();
            for (String instruction : instructions) {
                instruction = StringUtils.trim(instruction);
                System.out.printf("%5d: %s\n", iCount++, instruction);
                writer.write(instruction);
                writer.write(System.lineSeparator());
            }
        } catch (IOException e) {
            System.err.println("Problem writing to file " + asmFile);
            e.printStackTrace();
            System.exit(2);
        }
    }

    private static OutputStreamWriter openForWriting(Path asmFile) throws FileNotFoundException
    {
        return new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(asmFile.toFile())));
    }
}