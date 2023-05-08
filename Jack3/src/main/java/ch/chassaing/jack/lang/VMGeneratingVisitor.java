package ch.chassaing.jack.lang;

public class VMGeneratingVisitor
        extends JackBaseVisitor<Integer> {

    private String className;

    @Override
    public Integer visitClass(JackParser.ClassContext ctx) {

        className = ctx.className().ID().getText();
        return super.visitClass(ctx);
    }

    public String getClassName() {

        return className;
    }
}
