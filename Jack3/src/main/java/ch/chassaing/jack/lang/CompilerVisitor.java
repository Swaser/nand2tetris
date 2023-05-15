package ch.chassaing.jack.lang;

import ch.chassaing.jack.lang.subroutine.SubroutineScope;
import ch.chassaing.jack.lang.type.PrimitiveType;
import ch.chassaing.jack.lang.type.Type;
import ch.chassaing.jack.lang.type.UserType;
import ch.chassaing.jack.lang.var.VarScope;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.jetbrains.annotations.NotNull;

import static java.util.Objects.requireNonNull;

/**
 * One per class
 */
public class CompilerVisitor
        extends JackBaseVisitor<Object>
{
    private final VMWriter vmWriter;

    private ClassInfo classInfo;
    private SubroutineInfo subroutineInfo;

    private VarScope varScope; // the scope of the variable being declared
    private Type type;   // the type according to the type rule

    public CompilerVisitor(VMWriter vmWriter) {this.vmWriter = vmWriter;}

    public ClassInfo getClassInfo()
    {
        return classInfo;
    }

    private void raise(@NotNull String message, @NotNull ParserRuleContext ctx)
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
        requireNonNull(classInfo);
        mustBeNull(varScope);
        varScope = VarScope.STATIC;
        visitVarDec(ctx.varDec());
        varScope = null;
        return null;
    }

    @Override
    public Object visitFieldVarDec(JackParser.FieldVarDecContext ctx)
    {
        requireNonNull(classInfo);
        mustBeNull(varScope);
        varScope = VarScope.FIELD;
        visitVarDec(ctx.varDec());
        varScope = null;
        return null;
    }

    @Override
    public Object visitSubroutineDec(JackParser.SubroutineDecContext ctx)
    {
        requireNonNull(classInfo);
        mustBeNull(type);
        mustBeNull(subroutineInfo);

        SubroutineScope scope;
        if (ctx.FUNCTION() != null) scope = SubroutineScope.FUNCTION;
        else if (ctx.CONSTRUCTOR() != null) scope = SubroutineScope.CONSTRUCTOR;
        else scope = SubroutineScope.METHOD;

        Type returnType = null;
        if (ctx.type() != null) {
            visitType(ctx.type());
            returnType = requireNonNull(type);
            type = null;
        }

        String name = ctx.ID().getText();
        subroutineInfo = new SubroutineInfo(classInfo, name, scope, returnType);
        classInfo.addSubroutine(subroutineInfo);
        if (scope == SubroutineScope.METHOD) {
            subroutineInfo.addParameter("this", new UserType(classInfo.getName()));
        }

        ctx.parameter().forEach(this::visitParameter);

        visitBlock(ctx.block());

        subroutineInfo = null;
        return null;
    }

    @Override
    public Object visitParameter(JackParser.ParameterContext ctx)
    {
        requireNonNull(subroutineInfo);
        mustBeNull(type);
        visitType(ctx.type()); // determine the type
        requireNonNull(type);

        String name = ctx.ID().getText();
        if (!subroutineInfo.addParameter(name, type)) {
            raise("Duplicate parameter " + name, ctx);
        }

        type = null;
        return null;
    }

    @Override
    public Object visitLocalVarDec(JackParser.LocalVarDecContext ctx)
    {
        mustBeNull(varScope);
        varScope = VarScope.LOCAL;
        visitVarDec(ctx.varDec());
        varScope = null;
        return null;
    }

    @Override
    public Object visitVarDec(JackParser.VarDecContext ctx)
    {
        requireNonNull(classInfo);
        requireNonNull(varScope);
        if (varScope == VarScope.LOCAL) {
            requireNonNull(subroutineInfo);
        }
        mustBeNull(type);
        visitType(ctx.type());
        requireNonNull(type);

        for (TerminalNode node : ctx.ID()) {
            String name = node.getText();
            boolean success = switch (varScope) {
                case STATIC -> classInfo.addStaticVar(name, type);
                case FIELD -> classInfo.addFieldVar(name, type);
                case LOCAL -> subroutineInfo.addLocalVar(name, type);
                default -> throw new IllegalArgumentException();
            };
            if (!success) {
                raise("Duplicate variable declaration " + name, ctx);
            }
        }

        type = null;
        return null;
    }

    @Override
    public Object visitType(JackParser.TypeContext ctx)
    {
        if (ctx.INT() != null) {
            type = PrimitiveType.INT;
        } else if (ctx.CHAR() != null) {
            type = PrimitiveType.CHAR;
        } else if (ctx.BOOL() != null) {
            type = PrimitiveType.BOOLEAN;
        } else {
            type = new UserType(ctx.ID().getText());
        }
        return null;
    }

    @Override
    public Object visitEquality(JackParser.EqualityContext ctx)
    {
        TerminalNode op = null;
        int childCount = ctx.getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (i % 2 == 1) {
                op = (TerminalNode) ctx.getChild(i);
            } else {
                visitComparison((JackParser.ComparisonContext) ctx.getChild(i));
                if (op != null) {
                    vmWriter.writeArithmetic(Command.EQ);
                    if (op.getSymbol().getType() == JackParser.UNEQUAL) {
                        vmWriter.writeArithmetic(Command.NOT);
                    }
                }
            }
        }

        return null;
    }

    @Override
    public Object visitComparison(JackParser.ComparisonContext ctx)
    {
        TerminalNode op = null;
        int childCount = ctx.getChildCount();
        for (int i=0; i<childCount; i++) {
            if (i % 2 == 1) {
                op = (TerminalNode) ctx.getChild(i);
            } else {
                visitTerm((JackParser.TermContext) ctx.getChild(i));
                if (op != null) {
                    if (op.getSymbol().getType() == JackParser.LT) {
                        vmWriter.writeArithmetic(Command.LT);
                    } else if (op.getSymbol().getType() == JackParser.LE) {
                        vmWriter.writeArithmetic(Command.GT);
                        vmWriter.writeArithmetic(Command.NOT);
                    } else if (op.getSymbol().getType() == JackParser.GT) {
                        vmWriter.writeArithmetic(Command.GT);
                    } else { // must be "ge"
                        vmWriter.writeArithmetic(Command.LT);
                        vmWriter.writeArithmetic(Command.NOT);
                    }
                    op = null;
                }
            }
        }
        return null;
    }

    @Override
    public Object visitTerm(JackParser.TermContext ctx)
    {
        TerminalNode op = null;
        int childCount = ctx.getChildCount();
        for (int i=0; i<childCount; i++) {
            if (i %2 == 1) {
                op = (TerminalNode) ctx.getChild(i);
            } else {
                visitFactor((JackParser.FactorContext) ctx.getChild(i));
                if (op != null) {
                    if (op.getSymbol().getType() == JackParser.MULT) {
                        vmWriter.writeCall("Math.multiply", 2);
                    } else if (op.getSymbol().getType() == JackParser.DIV) {
                        vmWriter.writeCall("Math.divide", 2);
                    } else { // must be OR
                        vmWriter.writeArithmetic(Command.OR); // must be OR
                    }
                    op = null;
                }
            }
        }
        return null;
    }

    private static void mustBeNull(Object object)
    {
        if (object != null) {
            throw new IllegalArgumentException();
        }
    }
}
