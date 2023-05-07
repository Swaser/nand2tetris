package ch.chassaing.jack.lang;

import ch.chassaing.jack.arrayinit.ArrayInitBaseListener;
import ch.chassaing.jack.arrayinit.ArrayInitParser;

public class ShortToUnicodeString
        extends ArrayInitBaseListener
{
    @Override
    public void enterInit(ArrayInitParser.InitContext ctx)
    {
        System.out.print('"');
    }

    @Override
    public void exitInit(ArrayInitParser.InitContext ctx)
    {
        System.out.print('"');
    }

    @Override
    public void enterValue(ArrayInitParser.ValueContext ctx)
    {
        Integer value = Integer.valueOf(ctx.INT().getText());
        System.out.printf("\\u%04x", value);
    }
}
