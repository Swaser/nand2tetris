package ch.chassaing.jack.lang;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

public class JackC
{
    public static void main(String[] args) throws Exception {

        // create a CharStream that reads from standard input
        CharStream input = CharStreams.fromFileName(args[0]);

        // create a lexer that feeds off of input CharStream
        JackLexer lexer = new JackLexer(input);

        // create a buffer of tokens pulled from the lexer
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        // create a parser that feeds off the tokens buffer
        JackParser parser = new JackParser(tokens);

        var l = new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer,
                                    Object offendingSymbol,
                                    int line,
                                    int charPositionInLine,
                                    String msg,
                                    RecognitionException e)
            {
                String myMsg = "line %d:%d %s".formatted(line, charPositionInLine, msg);
                System.err.println(myMsg);
                throw new ParseCancellationException(myMsg);
            }
        };

        parser.removeErrorListeners();
        parser.addErrorListener(l);

        ParseTree tree = parser.class_();

        Writer writer = new BufferedWriter(new OutputStreamWriter(System.out, StandardCharsets.UTF_8));

        CompilerVisitor visitor = new CompilerVisitor(new NoOpVMWriter(writer));
        visitor.visit(tree);
        writer.flush();
    }
}
