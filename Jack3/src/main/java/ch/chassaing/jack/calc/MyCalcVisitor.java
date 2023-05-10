package ch.chassaing.jack.calc;

public class MyCalcVisitor
    extends CalcBaseVisitor<Integer>
{
    @Override
    public Integer visitAssign(CalcParser.AssignContext ctx)
    {
        String text = ctx.ID().getText();
        System.out.println(text);
        return -1;
    }
}
