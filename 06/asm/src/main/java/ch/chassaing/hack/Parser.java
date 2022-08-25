package ch.chassaing.hack;

import ch.chassaing.hack.instruction.*;
import org.apache.commons.lang3.StringUtils;

import static ch.chassaing.hack.Result.error;
import static ch.chassaing.hack.Result.success;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.removeStart;
import static org.apache.commons.lang3.StringUtils.trim;

public final class Parser
{
    private Parser() { /* do not instantiate */ }

    public static Result<Instruction> parseLine(String line)
    {
        requireNonNull(line);
        String trimmed = StringUtils.trim(line);
        if (StringUtils.isBlank(trimmed) || trimmed.startsWith("//")) {
            return null;
        }

        if (trimmed.startsWith("@")) {
            return success(new Constant(removeStart(trimmed, "@")));
        }

        // line is not blank
        // must be C-instruction

        // cut out the computation part ddd=ccc;jjj
        // but ddd= and ;jjj are both optional
        int equals = trimmed.indexOf('=');
        int semicolon = trimmed.indexOf(';');
        if (equals == -1 && semicolon == -1) {
            return Result.error("Line contains neither destination nor jump: " + line);
        }
        String compString = trimmed
                .substring(equals + 1, semicolon != -1 ? semicolon : trimmed.length()); // +1 because inclusive

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
        return null;
    }
}
