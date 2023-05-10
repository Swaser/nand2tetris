package ch.chassaing.jack.lang;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

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

        ParseTree tree = parser.class_();

        VMGeneratingVisitor visitor = new VMGeneratingVisitor();
        visitor.visit(tree);
        System.out.println(visitor.getClassName());
        System.out.println(visitor.getClassVars());
    }
}
