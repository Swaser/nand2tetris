package ch.chassaing.hack.vm;

import ch.chassaing.hack.vm.command.Command;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Main
{
    public static void main(String[] args)
    {
        if (args.length != 1) {
            System.out.println("Need exactly one argument (VM file or folder)");
            System.exit(1);
            return;
        }

        File inFile = Paths.get(args[0]).toFile();

        Path outPath;
        List<File> inPaths = new LinkedList<>();
        if (inFile.isDirectory()) {
            // code for directories
            outPath = Path.of(args[0], StringUtils.removeEnd(args[0], "/") + ".asm");
            File[] files = inFile.listFiles(file -> file.isFile() && file.getName().endsWith(".vm"));
            if (files == null) {
                System.err.println("Problem reading the folder " + inFile);
                System.exit(1);
                return;
            }
            inPaths.addAll(Arrays.asList(files));
            if (inPaths.stream().noneMatch(file -> file.getName().equals("Sys.vm"))) {
                System.err.println("A complete program must have a Sys.vm module");
                System.exit(1);
                return;
            }
        } else if (inFile.isFile() && args[0].endsWith(".vm")) {
            outPath = Path.of(args[0].replace(".vm", ".asm"));
            inPaths.add(inFile);
        } else {
            System.err.println("Need exactly one argument (VM file or folder)");
            System.exit(1);
            return;
        }

        List<Command> commands = new LinkedList<>();
        CodeWriter codeWriter = new HackWriter(inFile.isDirectory());

        try (OutputStreamWriter writer = openForWriting(outPath)) {
            int iCount = 0;
            for (File vmFilePath : inPaths) {
                String filename = vmFilePath.getName().replace(".vm", "");
                Parser parser = new SlurpingParser(filename, vmFilePath);
                int line;
                while ((line = parser.advance()) > 0) {
                    try {
                        commands.add(parser.command());
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.printf("%s - %d: %s%n", vmFilePath, line, e.getMessage());
                        System.exit(3);
                    }
                }
            }
            Iterable<String> instructions = codeWriter.getInstructions(commands);
            for (String instruction : instructions) {
                instruction = StringUtils.trim(instruction);
                System.out.printf("%5d: %s\n", iCount++, instruction);
                writer.write(instruction);
                writer.write(System.lineSeparator());
            }
        } catch (IOException e) {
            System.err.println("Problem writing to file " + outPath);
            e.printStackTrace();
            System.exit(2);
        }
    }

    private static OutputStreamWriter openForWriting(Path asmFile) throws FileNotFoundException
    {
        return new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(asmFile.toFile())));
    }
}