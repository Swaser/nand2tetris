package ch.chassaing.hack.vm;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.file.Path;
import java.util.List;

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

        IParser parser = new Parser(vmFile);
        ICodeWriter codeWriter = new HackWriter();

        try (OutputStreamWriter writer = openForWriting(asmFile)) {
            while (parser.advance()) {
                List<String> instructions = codeWriter.write(parser.command());
                for (String instruction : instructions) {
                    System.out.println(instruction);
                    writer.write(instruction);
                    writer.write(System.lineSeparator());
                }
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