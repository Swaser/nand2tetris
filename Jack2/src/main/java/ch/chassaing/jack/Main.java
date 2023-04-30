package ch.chassaing.jack;

import ch.chassaing.jack.token.Token;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main
{
    public static void main(String[] args)
    {
        Token token;

        try (BufferedReader reader = Files.newBufferedReader(Path.of(args[0]))) {
            JackTokenizer analyzer = new JackTokenizer(reader.lines().iterator());
            CompilationEngine compilationEngine = new CompilationEngine(analyzer);
            while ((token = analyzer.advance()) != null) {
                compilationEngine.compileClass(token);
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
