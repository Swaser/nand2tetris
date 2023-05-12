package ch.chassaing.jack.lang;

import ch.chassaing.jack.lang.subroutine.SubroutineScope;
import ch.chassaing.jack.lang.type.PrimitiveType;
import ch.chassaing.jack.lang.type.Type;
import ch.chassaing.jack.lang.type.UserType;
import ch.chassaing.jack.lang.var.VarScope;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

import static java.util.Objects.requireNonNull;

/**
 * One per class
 */
public class CompilerVisitor
        extends JackBaseVisitor<Object>
{
    public ClassInfo classInfo;
    private SubroutineInfo subroutineInfo;

    private VarScope varScope; // the scope of the variable being declared
    private Type type;   // the type according to the type rule

    private void raise(String message, ParserRuleContext ctx)
    {
        throw new IllegalArgumentException(message + " at " + ctx.getText());
    }

    @Override
    public Object visitClass(JackParser.ClassContext ctx)
    {
        classInfo = new ClassInfo(ctx.ID().getText());
        return visitChildren(ctx);
    }


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

    @Override
    public Object visitSubroutineDec(JackParser.SubroutineDecContext ctx)
    {
        requireNonNull(classInfo);
        requireNonNull(subroutineInfo);

        SubroutineScope scope;
        if (ctx.FUNCTION() != null) scope = SubroutineScope.FUNCTION;
        else if (ctx.CONSTRUCTOR() != null) scope = SubroutineScope.CONSTRUCTOR;
        else scope = SubroutineScope.METHOD;

        Type returnType = null;
        if (ctx.type() != null) {
            visitChildren(ctx.type());
            returnType = type;
            type = null;
        }

        String name = ctx.ID().getText();
        subroutineInfo = new SubroutineInfo(classInfo, name, scope, returnType);

        return super.visitSubroutineDec(ctx);
    }

    @Override
    public Object visitVarDec(JackParser.VarDecContext ctx)
    {
        requireNonNull(varScope);
        visitChildren(ctx);
        requireNonNull(type);
        for (TerminalNode node : ctx.ID()) {
            String name = node.getText();
            boolean hasError = switch (varScope) {
                case STATIC -> classInfo.addStaticVar(new VarInfo(name, type, VarScope.STATIC));
                case FIELD -> classInfo.addFieldVar(new VarInfo(name, type, VarScope.FIELD));
                case LOCAL -> requireNonNull(subroutineInfo)
                        .addLocalVar(new VarInfo(name, type, VarScope.LOCAL));
            };
            if (hasError) {
                raise("Duplicate variable declaration " + name, ctx);
            }
        }

        varScope = null;
        type = null;
        return null;
    }

    @Override
    public Object visitIntType(JackParser.IntTypeContext ctx)
    {
        type = PrimitiveType.INT;
        return null;
    }

    @Override
    public Object visitCharType(JackParser.CharTypeContext ctx)
    {
        type = PrimitiveType.CHAR;
        return null;
    }

    @Override
    public Object visitBoolType(JackParser.BoolTypeContext ctx)
    {
        type = PrimitiveType.BOOLEAN;
        return null;
    }

    @Override
    public Object visitUserType(JackParser.UserTypeContext ctx)
    {
        String typeName = requireNonNull(ctx.ID().getText());
        type = new UserType(typeName);
        return null;
    }
}
