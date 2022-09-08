package ch.chassaing.hack;

import ch.chassaing.hack.expression.*;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.control.Either;
import io.vavr.control.Option;
import org.apache.commons.lang3.StringUtils;

import java.math.BigInteger;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.removeEnd;
import static org.apache.commons.lang3.StringUtils.removeStart;

public final class ParserImpl
        implements Parser
{
    public static final Pattern SYMBOL_PATTERN = Pattern.compile("[a-zA-z_.$:][a-zA-z0-9_.$:]*");
    public static final int MAX_ADDRESS_BITS = 15;

    @Override
    public Option<Expression> parseLine(int lineNumber,
                                        String line)
    {
        requireNonNull(line);

        int comment = line.indexOf("//");
        if (comment > 0) {
            // cut away the comment
            line = line.substring(0, comment);
        }

        String trimmed = StringUtils.trim(line);

        if (StringUtils.isBlank(trimmed) || trimmed.startsWith("//")) {
            // comment or empty line
            return Option.none();
        }

        if (trimmed.startsWith("@")) {
            return Option.some(parseAInstruction(lineNumber, line, trimmed));
        }

        if (trimmed.startsWith("(")) {
            return Option.some(parseLInstruction(lineNumber, line, trimmed));
        }

        return Option.some(parseCInstruction(lineNumber, line, trimmed));
    }

    private static Expression parseAInstruction(int lineNumber,
                                                String line,
                                                String trimmed)
    {
        String chars = removeStart(trimmed, "@");

        if (StringUtils.isNumeric(chars)) {
            BigInteger value = new BigInteger(chars, 10);
            if (value.signum() == -1) {
                return new MalformedExpression(lineNumber, line,
                                               "Cannot process negative constants");
            }
            if (value.bitLength() > MAX_ADDRESS_BITS) {
                return new MalformedExpression(lineNumber, line,
                                               "Only values from 0 to 32767 are allowed");
            }
            return new Constant(lineNumber, line, value);
        }

        Matcher symbolMatcher = SYMBOL_PATTERN.matcher(chars);
        if (!symbolMatcher.matches()) {
            return new MalformedExpression(lineNumber, line,
                         "Symbols must not start with a digit");
        }
        return new Symbol(lineNumber, line, chars);
    }

    private Expression parseLInstruction(int lineNumber,
                                         String line,
                                         String trimmed)
    {
        if (!line.endsWith(")")) {
            return new MalformedExpression(lineNumber,
                                           line,
                                           "Malformed loop expression");
        }

        String label = removeEnd(removeStart(trimmed, "("), ")");
        return new Label(lineNumber, line, label);
    }

    private Expression parseCInstruction(int lineNumber,
                                         String line,
                                         String trimmed)
    {
        // cut out the computation part ddd=ccc;jjj
        // but ddd= and ;jjj are both optional
        int equals = trimmed.indexOf('=');
        int semicolon = trimmed.indexOf(';');
        if (equals == -1 && semicolon == -1) {
            return new MalformedExpression(lineNumber,
                                           line,
                                           "Line contains neither destination nor jump");
        }
        String compString = trimmed
                .substring(equals + 1,  // +1 because inclusive and we don't want the =
                           semicolon != -1 ? semicolon : trimmed.length());

        Either<String, Destination> destinationEither = getDestination(trimmed);
        Either<String, Computation> computationEither = getComputation(compString);
        Either<String, Jump> jumpEither = getJump(trimmed);

        Seq<String> errors = collectErrors(destinationEither,
                                           computationEither,
                                           jumpEither);

        if (errors.nonEmpty()) {
            return new MalformedExpression(lineNumber, line,
                                           errors.mkString(" & "));
        }

        return new CInstruction(lineNumber, line,
                                destinationEither.get(),
                                computationEither.get(),
                                jumpEither.get());
    }

    @SafeVarargs
    private Seq<String> collectErrors(Either<String, ?>... eithers)
    {

        return List.of(eithers)
                .filter(Either::isLeft)
                .map(Either::getLeft);
    }

    private Either<String, Destination> getDestination(String line)
    {
        for (Destination dest : Destination.values()) {
            if (dest.match.test(line)) {
                return Either.right(dest);
            }
        }

        return Either.left("Cannot determine destination");
    }

    private Either<String, Jump> getJump(String line)
    {
        for (Jump jump : Jump.values()) {
            if (jump.matches.test(line)) {
                return Either.right(jump);
            }
        }

        return Either.left("Malformed jump part");
    }

    private Either<String, Computation> getComputation(String compString)
    {
        for (Computation comp : Computation.values()) {
            if (Objects.equals(comp.stringRep, compString)) {
                return Either.right(comp);
            }
        }
        return Either.left("Cannot determine computation");
    }
}
