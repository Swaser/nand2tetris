package ch.chassaing.hack;

import ch.chassaing.hack.expression.Expression;
import ch.chassaing.hack.expression.Instruction;
import ch.chassaing.hack.expression.Label;
import ch.chassaing.hack.expression.MalformedExpression;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import org.apache.commons.cli.*;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static java.util.Objects.requireNonNull;

public final class HackAssembler
        implements Assembler
{
    public static final Charset ENCODING = StandardCharsets.US_ASCII;

    private final Parser parser;
    private final Feedback feedback;

    public HackAssembler(Parser parser, Feedback feedback)
    {
        this.parser = parser;
        this.feedback = feedback;
    }

    public static void main(String[] args)
    {
        CommandLine commandLine;
        Options options = new Options();
        options.addOption("ascii", "Output binary instructions encoded in ASCII");
        try {
            commandLine = new DefaultParser().parse(options, args);
        } catch (ParseException e) {
            System.err.println("Problem parsing command line");
            throw new RuntimeException(e);
        }
        Seq<String> remainingArgs = List.ofAll(commandLine.getArgList());
        if (remainingArgs.isEmpty()) {
            new HelpFormatter().printHelp("Usage: java -jar asm.jar <asm file>", options);
            System.exit(64);
        }

        String filename = remainingArgs.head();
        if (!filename.endsWith(".asm")) {
            System.out.println("Filename must have an .asm ending");
            System.exit(64);
        }

        Feedback feedback = new SoutFeedback(SoutFeedback.Level.DEBUG);

        new HackAssembler(new ParserImpl(), feedback)
                .process(filename, commandLine.hasOption("ascii"));
    }

    private void process(String filename,
                         boolean ascii)
    {
        Seq<String> lines = readFile(filename);

        String outFilename = filename.replace(".asm", ".hack");
        try (FileOutputStream fos = new FileOutputStream(outFilename, false)) {
            boolean success = transform(lines,
                                        fos,
                                        feedback,
                                        ascii);

            if (!success) {
                feedback.onError("Generation of hack file failed.");
                System.exit(32);
            }

        } catch (IOException e) {
            feedback.onError("Error processing " + filename);
            // TODO remove System.err and replace with feedback
            e.printStackTrace(System.err);
            System.exit(128);
        }
    }

    @Override
    public boolean transform(Seq<String> lines,
                             OutputStream machineCodeOutput,
                             Feedback feedback,
                             boolean ascii)
            throws IOException
    {
        SymbolTable symbolTable = new SymbolTableImpl();
        Seq<Expression> expressions = translate(lines);

        // In the first pass, labels are handled. That means that
        // duplicate labels are detected and the address of the labels
        // are determined and put into the symbol table

        feedback.onInfo("==============================================");
        feedback.onInfo("============== Start first pass ==============");
        feedback.onInfo("==============================================");
        boolean hasErrors = false;
        int address = 0; // address points to the next instruction
        for (Expression expression : expressions) {
            if (expression instanceof MalformedExpression malformed) {
                hasErrors = true;
                feedback.onLineError(malformed.lineNumber, malformed.line, malformed.details);
            } else if (expression instanceof Label label) {
                if (symbolTable.hasSymbol(label.value)) {
                    hasErrors = true;
                    feedback.onLineError(label.lineNumber, label.line, "Duplicate label");
                } else {
                    symbolTable.putAddress(label.value, BigInteger.valueOf(address));
                }
            } else if (expression instanceof Instruction) {
                address++;
            }
        }
        feedback.onInfo("==============================================");
        feedback.onInfo("==============  End first pass  ==============");
        feedback.onInfo("==============================================");

        if (hasErrors) {
            return false;
        }

        // second pass: generate machine code
        for (Expression expression : expressions) {
            if (expression instanceof Instruction instruction) {

                if (ascii) {
                    String s = instruction.toAsciiInstruction(symbolTable);
                    machineCodeOutput.write(s.getBytes(ENCODING));
                    machineCodeOutput.write(System.lineSeparator().getBytes(ENCODING));
                } else {
                    MachineInstruction machineInstruction = instruction.toMachineInstruction(symbolTable);
                    // low byte first = little endian
                    // high byte first = big endian
                    machineCodeOutput.write(machineInstruction.loByte());
                    machineCodeOutput.write(machineInstruction.hiByte());
                }
            }
        }

        feedback.onInfo("==============================================");
        feedback.onInfo("============== Generation done  ==============");
        feedback.onInfo("==============================================");

        machineCodeOutput.flush();

        return true;
    }

    /**
     * Read the contents of the file as a sequence of lines. Will terminate the process if
     * there is a problem reading the file.
     */
    private Seq<String> readFile(String file)
    {

        try {
            BufferedInputStream is = new BufferedInputStream(new FileInputStream(file));
            return List.ofAll(IOUtils.readLines(is, StandardCharsets.UTF_8));
        } catch (IOException e) {
            feedback.onError("Problem reading file.");
            // TODO replace with feedback
            e.printStackTrace(System.err);
            System.exit(64);
            return List.empty(); // the compiler doesn't seem to know that System.exit() terminates the process
        }
    }

    private Seq<Expression> translate(Seq<String> lines)
    {

        requireNonNull(lines);
        return lines
                .zipWithIndex()
                .flatMap(tuple -> parser.parseLine(tuple._2, tuple._1));
    }
}
