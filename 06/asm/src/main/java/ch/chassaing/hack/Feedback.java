package ch.chassaing.hack;

public interface Feedback
{
    void onDebug(String text);

    void onInfo(String text);

    void onError(String text);

    default void onLineDebug(int lineNumber, String line, String details)
    {
        onDebug(formatLine(lineNumber, line, details));
    }

    default void onLineInfo(int lineNumber, String line, String details)
    {
        onInfo(formatLine(lineNumber, line, details));
    }

    default void onLineError(int lineNumber, String line, String details)
    {
        onError(formatLine(lineNumber, line, details));
    }

    default String formatLine(int lineNumber, String line, String details)
    {
        return String.format("Line %6d: %s - %s%n", lineNumber, line, details);
    }

}
