package ch.chassaing.jack.token;

public abstract class Token
{
    public final int lineNr;

    protected Token(int lineNr)
    {
        this.lineNr = lineNr;
    }
}
