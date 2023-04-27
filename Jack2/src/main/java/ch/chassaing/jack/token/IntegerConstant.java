package ch.chassaing.jack.token;

public final class IntegerConstant
    extends Token
{
    public final int value;

    public IntegerConstant(int lineNr, int value)
    {
        super(lineNr);
        this.value = value;
    }

    @Override
    public String toString()
    {
        return "IntegerConstant{" +
               "value=" + value +
               ", lineNr=" + lineNr +
               '}';
    }
}
