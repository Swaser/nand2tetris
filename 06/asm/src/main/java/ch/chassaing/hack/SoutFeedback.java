package ch.chassaing.hack;

class SoutFeedback implements Assembler.Feedback
{
    @Override
    public void onDebug(int lineNumber, String line, String details)
    {
        doSout("DEBUG", lineNumber, line, details);
    }

    @Override
    public void onInfo(int lineNumber, String line, String details)
    {
        doSout("INFO", lineNumber, line, details);
    }

    @Override
    public void onError(int lineNumber, String line, String details)
    {
        doSout("ERROR", lineNumber, line, details);
    }

    @Override
    public void general(String text)
    {
        System.out.println(text);
    }

    private void doSout(String tag, int lineNumber, String line, String details) {

        System.out.printf("Line %6d %6s: %s - %s%n", lineNumber, tag, line, details);
    }
}
