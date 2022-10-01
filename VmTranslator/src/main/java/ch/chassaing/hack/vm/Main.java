package ch.chassaing.hack.vm;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main
{
    public static final Pattern LABEL_PATTERN  = Pattern.compile("\\(([\\w.:$_]+)\\)");
     public static final  Pattern SYMBOL_PATTERN = Pattern.compile("@([\\w.:$_]+)");


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

        while (parser.advance()) {
            codeWriter.add(parser.command());
        }

        try (OutputStreamWriter writer = openForWriting(asmFile)) {
            int line = 0;
            for (String instruction : removeLabels(codeWriter.getInstructions())) {
                instruction = StringUtils.trim(instruction);
                System.out.printf("%5d: %s\n", line++, instruction);
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

    private static Iterable<String> removeLabels(Iterable<String> instructions) {

        // we need to resolve labels as the CPU emulator is not capable of
        // doing this properly itself

        // first pass - build symbol table
        Iterator<String> instructionIterator = instructions.iterator();
        Map<String, Integer> symbolTable = new HashMap<>();
        int line = 0;
        while (instructionIterator.hasNext()) {
            String instruction = StringUtils.trim(instructionIterator.next());
            Matcher matcher = LABEL_PATTERN.matcher(instruction);
            if (matcher.matches()) {
                String label = matcher.group(1);
                if (symbolTable.containsKey(label)) {
                    throw new RuntimeException("label exists: " + label);
                } else {
                    symbolTable.put(label, line);
                    continue;
                }
            }
            line++; // increase the line number
        }

        // second pass - now suppress the labels and replace the symbols pointing to labels
        List<String> result = new LinkedList<>();
        instructionIterator = instructions.iterator();
        while (instructionIterator.hasNext()) {
            String instruction = StringUtils.trim(instructionIterator.next());
            if (LABEL_PATTERN.matcher(instruction).matches()) {
                continue;
            }
            Matcher symbolMatcher = SYMBOL_PATTERN.matcher(instruction);
            if (symbolMatcher.matches() && symbolTable.containsKey(symbolMatcher.group(1))) {
                result.add("@" + symbolTable.get(symbolMatcher.group(1)));
            } else {
                result.add(instruction);
            }
        }
        return result;
    }
}