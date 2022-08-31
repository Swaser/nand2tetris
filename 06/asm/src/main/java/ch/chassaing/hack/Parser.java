package ch.chassaing.hack;

import ch.chassaing.hack.instruction.*;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ch.chassaing.hack.Result.*;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.removeStart;

public final class Parser
{

    public static final Pattern SYMBOL_PATTERN = Pattern.compile("[a-zA-z_.$:][a-zA-z0-9_.$:]*");
    public static final Pattern VIRTUAL_REGISTER = Pattern.compile("R(\\d\\d?)");

    private Parser() { /* do not instantiate */ }

    public static Result<Instruction> parseLine(String line)
    {
        requireNonNull(line);
        String trimmed = StringUtils.trim(line);
        if (StringUtils.isBlank(trimmed) || trimmed.startsWith("//")) {
            return none();
        }

        if (trimmed.startsWith("@")) {
            String chars = removeStart(trimmed, "@");
            if (StringUtils.isNumeric(chars)) {
                return success(new Constant(chars));
            } else {
                if (!SYMBOL_PATTERN.matcher(chars).matches()) {
                    return error("Symbols must not start with a digit.");
                }
                Matcher virtualRegisterMatcher = VIRTUAL_REGISTER.matcher(chars);
                if (virtualRegisterMatcher.matches()) {
                    int register = Integer.parseInt(virtualRegisterMatcher.group(1));
                    if (register <= 15) {
                        return success(new Constant(virtualRegisterMatcher.group(1)));
                    }
                }
                return switch (chars) {
                    case "SCREEN" -> success(new Constant("16348"));
                    case "KBD" -> success(new Constant("16348"));
                    default -> throw new IllegalStateException("Unexpected value: " + chars);
                };
            }
        }

        // must be C-instruction

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

    private static Result<Destination> getDestination(String line)
    {
        for (Destination dest : Destination.values()) {
            if (dest.match.test(line)) {
                return Result.success(dest);
            }
        }

        return Result.error("Line doesn't conform to syntax (destination): " + line);
    }

    private static Result<Jump> getJump(String line)
    {
        for (Jump jump : Jump.values()) {
            if (jump.matches.test(line)) {
                return Result.success(jump);
            }
        }

        return Result.error("Line doesn't conform to syntax (jump): " + line);
    }

    private static Result<Computation> getComputation(String compString)
    {
        for (Computation comp : Computation.values()) {
            if (Objects.equals(comp.stringRep,compString)) {
                return Result.success(comp);
            }
        }
        return Result.error("Cannot determine computation in: " + compString);
    }
}
