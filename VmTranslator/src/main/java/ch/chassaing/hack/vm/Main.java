package ch.chassaing.hack.vm;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

public class Main
{
    public static void main(String[] args)
    {
        List<Path> vmFilePaths = new LinkedList<>();
        for (String vmFilename : args) {
            if (!vmFilename.endsWith(".vm")) {
                System.out.println("File must end with .vm");
                System.exit(1);
            }
            String firstChar = vmFilename.substring(0, 1);
            if (!firstChar.equals(firstChar.toUpperCase())) {
                System.out.println("File must start with an uppercase letter");
                System.exit(1);
            }
            vmFilePaths.add(Path.of(vmFilename));
        }

        // name of the first input file determines the name of the assembly file
        Path outFile = Path.of(args[0].replace(".vm", ".asm"));
        ICodeWriter codeWriter = new HackWriter();

        try (OutputStreamWriter writer = openForWriting(outFile)) {
            int iCount = 0;
            for (Path vmFilePath : vmFilePaths) {
                IParser parser = new Parser(vmFilePath);
                codeWriter.setProgName(vmFilePath.getFileName()
                                                 .toString()
                                                 .replace(".vm", ""));
                int line;
                while ((line = parser.advance()) > 0) {
                    try {
                        codeWriter.add(parser.command());
                    } catch (Exception e) {
                        System.out.printf("%s - %d: %s%n", vmFilePath, line, e.getMessage());
                        System.exit(3);
                    }
                }
            }
            Iterable<String> instructions = codeWriter.getInstructions();
            for (String instruction : instructions) {
                instruction = StringUtils.trim(instruction);
                System.out.printf("%5d: %s\n", iCount++, instruction);
                writer.write(instruction);
                writer.write(System.lineSeparator());
            }
        } catch (IOException e) {
            System.err.println("Problem writing to file " + outFile);
            e.printStackTrace();
            System.exit(2);
        }
    }

    private static OutputStreamWriter openForWriting(Path asmFile) throws FileNotFoundException
    {
        return new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(asmFile.toFile())));
    }
}