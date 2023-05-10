package ch.chassaing.jack.lang;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * One per class
 */
public class VMGeneratingVisitor
        extends JackBaseVisitor<Object>
{
    private String className;
    private final Map<String, VarType> staticVars = new HashMap<>();
    private final Map<String, VarType> fieldVars = new HashMap<>();
    private Map<String, VarType> localVars;

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder("Class ")
                .append(className)
                .append(System.lineSeparator());
        for (Map.Entry<String, VarType> entry : staticVars.entrySet()) {
            addVar(sb, "static", entry);
        }
        for (Map.Entry<String, VarType> entry : fieldVars.entrySet()) {
            addVar(sb, "field", entry);
        }
        return sb.toString();
    }

    private void addVar(StringBuilder sb, String scope, Map.Entry<String, VarType> varTypeEntry)
    {
        sb.append(scope).append(" ")
          .append(varTypeEntry.getValue().repr()).append(" ")
          .append(varTypeEntry.getKey())
          .append(System.lineSeparator());
    }

    private void raise(String message, ParserRuleContext ctx)
    {
        throw new IllegalArgumentException(message + ": " + ctx.getText());
    }

    @Override
    public Object visitClass(JackParser.ClassContext ctx)
    {
        className = ctx.ID().getText();
        return visitChildren(ctx);
    }

    private VarScope varScope;

    @Override
    public Object visitStaticVarDec(JackParser.StaticVarDecContext ctx)
    {
        varScope = VarScope.STATIC;
        return visitChildren(ctx);
    }

    @Override
    public Object visitFieldVarDec(JackParser.FieldVarDecContext ctx)
    {
        varScope = VarScope.FIELD;
        return visitChildren(ctx);
    }

    private VarType varType;

    @Override
    public Object visitVarDec(JackParser.VarDecContext ctx)
    {
        requireNonNull(varScope);
        super.visitVarDec(ctx);
        requireNonNull(varType);
        for (TerminalNode node : ctx.ID()) {
            String name = node.getText();
            boolean hasError = switch (varScope) {
                case STATIC, FIELD -> staticVars.containsKey(name) ||
                                      fieldVars.containsKey(name);
                case LOCAL -> localVars.containsKey(name);
            };
            if (hasError) {
                raise("Duplicate variable declaration " + name, ctx);
            }
            switch (varScope) {

                case STATIC -> staticVars.put(name, varType);
                case FIELD -> fieldVars.put(name, varType);
                case LOCAL -> localVars.put(name, varType);
            }
        }

        varScope = null;
        varType = null;
        return null;
    }

    @Override
    public Object visitIntType(JackParser.IntTypeContext ctx)
    {
        varType = PrimitiveType.INT;
        return null;
    }

    @Override
    public Object visitCharType(JackParser.CharTypeContext ctx)
    {
        varType = PrimitiveType.CHAR;
        return null;
    }

    @Override
    public Object visitBoolType(JackParser.BoolTypeContext ctx)
    {
        varType = PrimitiveType.BOOLEAN;
        return null;
    }

    @Override
    public Object visitUserType(JackParser.UserTypeContext ctx)
    {
        String typeName = requireNonNull(ctx.ID().getText());
        varType = new UserType(typeName);
        return null;
    }
}
