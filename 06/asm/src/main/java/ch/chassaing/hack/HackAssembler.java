package ch.chassaing.hack;

import ch.chassaing.hack.expression.Expression;
import ch.chassaing.hack.expression.LabelExpression;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;

import static java.util.Objects.requireNonNull;

public final class HackAssembler
        implements Assembler {
    private final Parser parser;

    public HackAssembler(Parser parser) {

        this.parser = parser;
    }

    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println("Usage: " + HackAssembler.class.getSimpleName() + " assemblerfile");
            System.exit(64);
        }

        String filename = args[0];
        if (!filename.endsWith(".asm")) {
            System.out.println("Filename must have an .asm ending");
            System.exit(64);
        }

        new HackAssembler(new ParserImpl()).process(filename);
    }

    private void process(String filename) {

        Seq<String> lines = readFile(filename);

        String outFilename = filename.replace(".asm", ".hack");
        try (FileOutputStream fos = new FileOutputStream(outFilename, false)) {
            boolean success = transform(lines,
                                        fos,
                                        (severity, message) -> {
                                            if (severity == MessageSeverity.ERROR) {
                                                System.err.println(message);
                                            } else {
                                                System.out.println(message);
                                            }
                                        });

            if (!success) {
                System.out.println("Generation of hack file failed.");
                System.exit(32);
            }

        } catch (IOException e) {
            System.err.println("Error processing " + filename);
            e.printStackTrace(System.err);
            System.exit(128);
        }
    }

    @Override
    public boolean transform(Seq<String> lines,
                             OutputStream machineCodeOutput,
                             BiConsumer<MessageSeverity, CharSequence> messageConsumer)
            throws IOException {

        Seq<Result<Expression>> translateResult = translate(lines);

        SymbolTable symbolTable = new SymbolTableImpl();

        // first pass: detect errors and build symbol table
        boolean hasErrors = false;
        // address points to the next instruction
        int address = 0;
        for (Result<Expression> instructionResult : translateResult) {
            if (instructionResult instanceof Result.Error<Expression> error) {
                hasErrors = true;
                messageConsumer.accept(MessageSeverity.ERROR, error.reason());
            } else if (instructionResult instanceof Result.Success<Expression> success) {
                if (success.value() instanceof LabelExpression labelExpression) {
                    symbolTable.putAddress(labelExpression.loopIndicator(), BigInteger.valueOf(address));
                } else {
                    // A- and C-instructions advance the address
                    address++;
                }
            }
        }

        if (hasErrors) {
            return false;
        }

        // second pass: generate machine code
        for (Result<Expression> instructionResult : translateResult) {
            if (instructionResult instanceof Result.Success<Expression> success) {
                if (success.value() instanceof LabelExpression) {
                    continue;
                }
                Expression expression = success.value();
                MachineInstruction machineInstruction = expression.toMachineInstruction(symbolTable);
                // low byte first = little endian
                // high byte first = big endian
                machineCodeOutput.write(machineInstruction.loByte());
                machineCodeOutput.write(machineInstruction.hiByte());
            }
        }

        machineCodeOutput.flush();

        return true;
    }

    /**
     * Read the contents of the file as a sequence of lines. Will terminate the process if
     * there is a problem reading the file.
     */
    private Seq<String> readFile(String file) {

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

    private Seq<Result<Expression>> translate(Seq<String> lines) {

        requireNonNull(lines);
        Set<String> loopNames = new HashSet<>();
        Seq<Tuple2<Result<Expression>, Integer>> numberedInstructions = lines
                .zipWithIndex()
                .map(tuple -> parser.parseLine(tuple._2, tuple._1))
                .map(expressionResult -> {
                    // check for duplicate loop declarations
                    if (expressionResult instanceof Result.Success<Expression> success &&
                        success.value() instanceof LabelExpression lInstruction) {
                        if (loopNames.contains(lInstruction.loopIndicator())) {
                            return Tuple.of(Result.error("Duplicate loop declaration " + lInstruction.loopIndicator()), tuple._2);
                        } else {
                            loopNames.add(lInstruction.loopIndicator());
                        }
                    }
                    return tuple;
                });

        numberedInstructions
                .forEach(tuple -> {
                    if (tuple._1 instanceof Result.Error<Expression> error) {
                        System.out.println("" + tuple._2 + " : " + error.reason());
                    }
                });

        return numberedInstructions.map(Tuple2::_1);
    }
}
