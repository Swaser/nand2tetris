package ch.chassaing.jack;

import ch.chassaing.jack.token.Token;
import io.vavr.collection.IndexedSeq;
import io.vavr.collection.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.function.Predicate;

import static java.lang.Character.isWhitespace;


/**
 * One Analyzer per File.
 * <br/>
 * Not Thread-safe.
 */
public class JackAnalyzer
{
    public static final LinkedList<Character> EMPTY_CHARACTERS = new LinkedList<>();

    private final Iterator<String> lineProvider;
    private Queue<Character> characters = EMPTY_CHARACTERS;
    private int lineNr = 0;
    private Character current;

    public JackAnalyzer(@NotNull Iterator<String> lineProvider)
    {
        this.lineProvider = lineProvider;
    }

    @NotNull
    public IndexedSeq<Token> tokenize()
    {
        while ((current = nextChar()) != null) {

            if (isWhitespace(current)) {
                continue;
            }

            if (current == '/') {
                Character next = peekChar();
                if (next != null && next == '/') {
                    // comment: ignore rest of line
                    characters = EMPTY_CHARACTERS;
                    nextLine();
                    continue;
                }

                if (next != null && next == '*') {
                    // multi line comment
                    // ignore everything to the next */ or till the end of the file
                    current = nextChar(); // consume the '*'
                    while (peekChar() != null) {
                         current = Objects.requireNonNull(nextChar());
                        if (current == '*') {
                            current = nextChar();
                            if (current != null && current == '/') {
                                break;
                            }
                        }
                    }
                    continue;
                }
            }



        }


        return Vector.empty();
    }

    @Nullable
    private Character nextChar()
    {

        if (characters.isEmpty()) {
            nextLine();
        }
        return characters.poll();
    }

    /**
     * Return the next character, if there is any or null otherwise.
     */
    @Nullable
    private Character peekChar()
    {
        if (characters.isEmpty()) {
            nextLine();
        }
        return characters.peek();
    }

    private void nextLine()
    {
        if (lineProvider.hasNext()) {
            characters = new LinkedList<>();
            lineProvider.next()
                        .chars()
                        .forEach(it -> characters.add((char) it));
            lineNr++;
        }
    }
}
