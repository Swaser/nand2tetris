package ch.chassaing.jack;

import ch.chassaing.jack.token.*;
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
public class JackTokenizer
    implements Tokenizer
{
    private static final LinkedList<Character> EMPTY_CHARACTERS = new LinkedList<>();

    @NotNull
    private final Iterator<String> lineProvider;
    @NotNull
    private Queue<Character> characters = EMPTY_CHARACTERS;
    @Nullable
    private Character current;
    private int lineNr = 0;

    private int currentTokenLineNumber;
    private Token nextToken;

    public JackTokenizer(@NotNull Iterator<String> lineProvider)
    {
        this.lineProvider = lineProvider;
    }

    @Override
    public @Nullable Token advance()
    {
        Token result;
        if (nextToken != null) {
            result = nextToken;
            nextToken = null;
        } else {
            result = doAdvance();
        }
        currentTokenLineNumber = lineNr;
        System.out.println("Handling " + result);
        return result;
    }

    @Override
    public @Nullable Token peek()
    {
        if (nextToken == null) {
            nextToken = doAdvance();
        }
        return nextToken;
    }

    @Override
    public int lineNumber()
    {
        return currentTokenLineNumber;
    }

    @Nullable
    private Token doAdvance()
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
                            next = peekChar();
                            if (next != null && next == '/') {
                                current = nextChar();
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
            else if (current == '"') {
                return stringConstant();
            }
            else if (isDigit(current)) {
                return integerConstant();
            }

            return symbol();
        }

        // no more characters and no token found so far
        return null;
    }

    @NotNull
    private Token symbol()
    {
        requireNonNull(current);
        for (Symbol type : Symbol.values()) {
            if (current == type.repr) {
                return type;
            }
        }
        throw new IllegalStateException("Unknown symbol " + current +
                                        " on line " + lineNr);
    }

    /**
     * Precondition: current is leftmost digit
     * Post condition: current is rightmost digit
     */
    @NotNull
    private Token integerConstant()
    {
        StringBuilder sb = new StringBuilder();
        sb.append((char) requireNonNull(current));
        Character next;
        while ((next = peekChar()) != null) {
            if (!isDigit(next)) {
                break;
            }
            current = requireNonNull(nextChar());
            sb.append((char) current);
        }
        return new IntegerConstant(Integer.parseInt(sb.toString(), 10));
    }

    /**
     * Precondition: current is the opening quote
     * Post condition: current is the closing quote
     */
    @NotNull
    private Token stringConstant()
    {
        StringBuilder sb = new StringBuilder();
        Character next;
        while ((next = peekChar()) != null) {
            if (next == '"') {
                current = nextChar();
                break;
            }
            current = requireNonNull(nextChar());
            sb.append((char) current);
        }
        return new StringConstant(sb.toString());
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
        for (Keyword keyword : Keyword.values()) {
            if (keyword.name().toLowerCase().equals(value)) {
                return keyword;
            }
        }

        return new Identifier(value);
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
