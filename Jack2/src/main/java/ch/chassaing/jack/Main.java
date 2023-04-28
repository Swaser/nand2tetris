package ch.chassaing.jack;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main
{
    public static void main(String[] args)
    {
        try (BufferedReader reader = Files.newBufferedReader(Path.of(args[0]))) {
            JackTokenizer analyzer = new JackTokenizer(reader.lines().iterator());
            CompilationEngine compilationEngine = new CompilationEngine(analyzer);
            while (compilationEngine.compileClass()) {
                ; // stuff is done in .compileClass()
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
