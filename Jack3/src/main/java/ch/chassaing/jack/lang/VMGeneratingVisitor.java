package ch.chassaing.jack.lang;

import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.HashMap;
import java.util.Map;

/**
 * One per class
 */
public class VMGeneratingVisitor
        extends JackBaseVisitor<Void> {

    private String className;
    private final Map<String,String> staticVars = new HashMap<>();
    private final Map<String,String> fieldVars = new HashMap<>();

    @Override
    public Void visitClass(JackParser.ClassContext ctx)
    {
        className = ctx.ID().getText();
        return visitChildren(ctx);
    }

    @Override
    public Void visitStaticVarDec(JackParser.StaticVarDecContext ctx)
    {

        if (ctx.varDec().type().getTokens(JackLexer.INT).isEmpty()) {}
        for (TerminalNode id : ctx.varDec().ID()) {
            String varName = id.getText();
        }
        return null;
    }

    public String getClassName() {

        return className;
    }
}
