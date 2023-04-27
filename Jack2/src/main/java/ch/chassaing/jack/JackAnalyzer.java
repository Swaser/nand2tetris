package ch.chassaing.jack;

import ch.chassaing.jack.token.Identifier;
import ch.chassaing.jack.token.Keyword;
import ch.chassaing.jack.token.KeywordType;
import ch.chassaing.jack.token.Token;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import static java.lang.Character.*;
import static java.util.Objects.requireNonNull;


/**
 * One Analyzer per File.
 * <br/>
 * Not Thread-safe.
 */
public class JackAnalyzer
{
    public static final LinkedList<Character> EMPTY_CHARACTERS = new LinkedList<>();

    @NotNull
    private final Iterator<String> lineProvider;
    @NotNull
    private Queue<Character> characters = EMPTY_CHARACTERS;
    @Nullable
    private Character current;
    private int lineNr = 0;

    public JackAnalyzer(@NotNull Iterator<String> lineProvider)
    {
        this.lineProvider = lineProvider;
    }

    @Nullable
    public Token advance()
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
                        current = requireNonNull(nextChar());
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

            if (isAlphabetic(current) || current == '_') {

                return keywordOrIdentifier();
            }

        }

        // no more characters and no token found so far
        return null;
    }

    /**
     * Precondition: current contains the first character of the keyword or identifier
     */
    @NotNull
    private Token keywordOrIdentifier()
    {
        StringBuilder sb = new StringBuilder();
        sb.append((char) requireNonNull(current));
        Character next;
        while ((next = peekChar()) != null) {
            if (!(isAlphabetic(next) || isDigit(next) || next == '_')) {
                break;
            }
            current = requireNonNull(nextChar());
            sb.append((char) current);
        }

        String value = sb.toString();
        for (KeywordType type : KeywordType.values()) {
            if (type.name().toLowerCase().equals(value)) {
                return new Keyword(lineNr, type);
            }
        }

        return new Identifier(lineNr, value);
    }

    /**
     * Returns and consumes the next character, if there is any or null otherwise.
     */
    @Nullable
    private Character nextChar()
    {
        if (characters.isEmpty()) {
            nextLine();
        }
        return characters.poll();
    }

    /**
     * Return the value of the next character, if there is any or null otherwise.
     * The character is not consumed.
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
