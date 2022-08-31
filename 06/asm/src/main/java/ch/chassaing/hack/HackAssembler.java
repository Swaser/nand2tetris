package ch.chassaing.hack;

import ch.chassaing.hack.instruction.Instruction;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.collection.Vector;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class HackAssembler
{
    public static void main(String[] args)
    {
        if (args.length != 1) {
            System.out.println("Usage: " + HackAssembler.class.getSimpleName() + " assemblerfile");
            System.exit(64);
        }

        String filename = args[0];
        if (!filename.endsWith(".asm")) {
            System.out.println("Filename must have an .asm ending");
            System.exit(64);
        }

        Seq<String> lines = readFile(filename);

        Seq<Instruction> instructions = translate(lines);

        String outFilename = filename.replace(".asm", ".hack");
        try (FileOutputStream fos = new FileOutputStream(outFilename, false)) {
            for (Instruction instruction : instructions) {
                byte[] bytes = instruction.toMachineInstruction();
                fos.write(bytes[0]); // 0-1 little endian
                fos.write(bytes[1]); // 1-0 big endian
            }
            fos.flush();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Read the contents of the file as a sequence of lines. Will terminate the process if
     * there is a problem reading the file.
     */
    private static Seq<String> readFile(String file)
    {
        try {
            BufferedInputStream is = new BufferedInputStream(new FileInputStream(file));
            return List.ofAll(IOUtils.readLines(is, StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.err.println("Problem reading file.");
            e.printStackTrace(System.err);
            System.exit(64);
            return List.empty(); // the compiler doesn't seem to know that System.exit() terminates the process
        }
    }

    /**
     * Take a bunch of lines and then translate them into instructions.
     */
    private static Seq<Instruction> translate(Seq<String> lines) {

        Objects.requireNonNull(lines);

        Seq<Tuple2<Result<Instruction>, Integer>> numberedInstructions =
                lines.map(Parser::parseLine).zipWithIndex();

        boolean hasErrors = false;
        Seq<Instruction> instructions = Vector.empty();
        for (Tuple2<Result<Instruction>, Integer> numberedInstruction : numberedInstructions) {
            switch (numberedInstruction._1) {
                case Result.None<Instruction> none -> {}
                case Result.Error<Instruction> error -> {
                    System.out.println("Error on line number " + numberedInstruction._2 + " : " + error.reason());
                    hasErrors = true;
                }
                case Result.Success<Instruction> success -> {
                    instructions = instructions.append(success.value());
                }
            }
        }

        if (hasErrors) {
            System.exit(128);
        }

        return instructions;
    }
}
