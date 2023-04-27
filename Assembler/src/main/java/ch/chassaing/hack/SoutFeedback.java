package ch.chassaing.hack;

class SoutFeedback implements Feedback
{
    enum Level
    {
        DEBUG, INFO, ERROR;
    }

    private final Level level;

    SoutFeedback(Level level)
    {
        this.level = level;
    }

    @Override
    public void onDebug(String text)
    {
        if (level.ordinal() <= Level.DEBUG.ordinal()) {
            doSout("DEBUG", text);
        }
    }

    @Override
    public void onInfo(String text)
    {
        if (level.ordinal() <= Level.INFO.ordinal()) {
            doSout("INFO", text);
        }
    }

    @Override
    public void onError(String text)
    {
        if (level.ordinal() <= Level.ERROR.ordinal()) {
            doSout("ERROR", text);
        }
    }

    private void doSout(String tag, String text)
    {
        System.out.printf("%6s %s%n", tag, text);
    }
}
