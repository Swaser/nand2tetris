package ch.chassaing.hack;

import ch.chassaing.hack.instruction.*;
import org.apache.commons.lang3.StringUtils;

import java.math.BigInteger;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ch.chassaing.hack.Result.*;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.removeEnd;
import static org.apache.commons.lang3.StringUtils.removeStart;

public final class ParserImpl
        implements Parser
{
    public static final Pattern SYMBOL_PATTERN = Pattern.compile("[a-zA-z_.$:][a-zA-z0-9_.$:]*");
    public static final int MAX_ADDRESS_BITS = 15;

    @Override
    public Result<Instruction> parseLine(String line)
    {
        requireNonNull(line);
        String trimmed = StringUtils.trim(line);

        if (StringUtils.isBlank(trimmed) || trimmed.startsWith("//")) {
            // comment or empty line
            return none();
        }

        if (trimmed.startsWith("@")) {
            return parseAInstruction(line, trimmed);
        }

        if (trimmed.startsWith("(")) {
            return parseLInstruction(line, trimmed);
        }

        return parseCInstruction(line, trimmed);
    }

    private static Result<Instruction> parseAInstruction(String line,
                                                         String trimmed)
    {
        String chars = removeStart(trimmed, "@");

        if (StringUtils.isNumeric(chars)) {
            BigInteger value = new BigInteger(chars, 10);
            if (value.signum() == -1) {
                return error("Cannot process negative constants: " + line);
            }
            if (value.bitLength() > MAX_ADDRESS_BITS) {
                return error("Only values from 0 to 32767 are allowed: " + line);
            }
            return success(new Constant(value));
        }

        Matcher symbolMatcher = SYMBOL_PATTERN.matcher(chars);
        if (!symbolMatcher.matches()) {
            return error("Symbols must not start with a digit: " + line);
        }
        return success(new Symbol(chars));
    }

    private Result<Instruction> parseLInstruction(String line,
                                                  String trimmed)
    {
        if (!line.endsWith(")")) {
            return error("Malformed loop expression: " + line);
        }

        String loopName = removeEnd(removeStart(trimmed, "("), ")");

        return success(new LInstruction(loopName));
    }

    private Result<Instruction> parseCInstruction(String line,
                                                  String trimmed)
    {
        // cut out the computation part ddd=ccc;jjj
        // but ddd= and ;jjj are both optional
        int equals = trimmed.indexOf('=');
        int semicolon = trimmed.indexOf(';');
        if (equals == -1 && semicolon == -1) {
            return Result.error("Line contains neither destination nor jump: " + line);
        }
        String compString = trimmed
                .substring(equals + 1,  // +1 because inclusive and we don't want the =
                           semicolon != -1 ? semicolon : trimmed.length());

        return getComputation(compString)
                .flatMap(comp -> getDestination(line)
                        .flatMap(dest -> getJump(line)
                                .map(jump -> new CInstruction(dest, comp, jump))));
    }

    private Result<Destination> getDestination(String line)
    {
        for (Destination dest : Destination.values()) {
            if (dest.match.test(line)) {
                return Result.success(dest);
            }
        }

        return Result.error("Line doesn't conform to syntax (destination): " + line);
    }

    private Result<Jump> getJump(String line)
    {
        for (Jump jump : Jump.values()) {
            if (jump.matches.test(line)) {
                return Result.success(jump);
            }
        }

        return Result.error("Line doesn't conform to syntax (jump): " + line);
    }

    private Result<Computation> getComputation(String compString)
    {
        for (Computation comp : Computation.values()) {
            if (Objects.equals(comp.stringRep, compString)) {
                return Result.success(comp);
            }
        }
        return Result.error("Cannot determine computation in: " + compString);
    }
}
