package ch.chassaing.jack.calc;

/***
 * Excerpted from "The Definitive ANTLR 4 Reference",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/tpantlr2 for more book information.
***/
// import ANTLR's runtime libraries

import ch.chassaing.jack.arrayinit.ArrayInitLexer;
import ch.chassaing.jack.arrayinit.ArrayInitParser;
import ch.chassaing.jack.arrayinit.ShortToUnicodeString;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

public class CalcInit
{
    public static void main(String[] args) throws Exception {
        // create a CharStream that reads from standard input
        CharStream input = CharStreams.fromStream(System.in);

        // create a lexer that feeds off of input CharStream
        CalcLexer lexer = new CalcLexer(input);

        // create a buffer of tokens pulled from the lexer
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        // create a parser that feeds off the tokens buffer
        CalcParser parser = new CalcParser(tokens);

        ParseTree tree = parser.prog(); // begin parsing at init rule

        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(new Calculator(), tree);
    }
}
