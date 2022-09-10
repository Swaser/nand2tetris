package ch.chassaing.hack;

import io.vavr.collection.Seq;

import java.io.IOException;
import java.io.OutputStream;
import java.util.function.BiConsumer;

public interface Assembler
{
    interface Feedback
    {
        void onDebug(int lineNumber, String line, String details);

        void onInfo(int lineNumber, String line, String details);

        void onError(int lineNumber, String line, String details);

        void general(String text);
    }

    /**
     * Given a {@link Seq} of Strings which represent lines of assembly language
     * translates them into instructions and then writes them to the provided
     * {@link OutputStream}. If the assembly program contains errors, they
     * will be reported to the messageConsumer. If not, the resulting binary
     * instructions will be passed on tho the machineCodeOutput {@link OutputStream}.
     *
     * @param lines the lines of the assembly program
     * @return true if the transformation was successful and false otherwise
     * @throws IOException if there's a problem writing to the {@link OutputStream}
     */
    boolean transform(Seq<String> lines,
                      OutputStream machineCodeOutput,
                      Feedback feedback,
                      boolean ascii)
            throws IOException;
}
