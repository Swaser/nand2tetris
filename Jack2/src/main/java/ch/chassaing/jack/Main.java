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
        try (BufferedReader reader = Files.newBufferedReader(Path.of(args[0]))) {
            JackTokenizer analyzer = new JackTokenizer(reader.lines().iterator());
            Token token;
            while ((token = analyzer.advance()) != null) {
                System.out.println(token);
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
