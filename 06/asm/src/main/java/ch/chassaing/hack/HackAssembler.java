package ch.chassaing.hack;

import ch.chassaing.hack.instruction.Instruction;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class HackAssembler
{

    public static void main(String[] args)
    {
        if (args.length != 1) {
            System.out.println("Usage: " + HackAssembler.class.getSimpleName() + " assemblerfile");
            System.exit(64);
        }

        Seq<String> lines = List.empty();
        try {
            BufferedInputStream is = new BufferedInputStream(new FileInputStream(args[0]));
            lines = List.ofAll(IOUtils.readLines(is, StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.err.println("Problem reading file.");
            e.printStackTrace(System.err);
            System.exit(128);
        }

        Seq<Tuple2<Result<Instruction>, Integer>> numberedInstructions = lines
                .map(Parser::parseLine)
                .zipWithIndex();

        for (Tuple2<Result<Instruction>, Integer> numberedInstruction : numberedInstructions) {
            if (numberedInstruction._1 instanceof Result.Error<Instruction> error) {
                System.err.println("Error on line number " + numberedInstruction._2 + " : " + error.reason());
                System.exit(32);
            }
        }

        Seq<Instruction> instructions = numberedInstructions.flatMap(it -> it._1);
    }
}
