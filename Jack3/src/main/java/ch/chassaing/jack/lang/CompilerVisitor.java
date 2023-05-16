package ch.chassaing.jack.lang;

import ch.chassaing.jack.lang.subroutine.SubroutineScope;
import ch.chassaing.jack.lang.type.PrimitiveType;
import ch.chassaing.jack.lang.type.Type;
import ch.chassaing.jack.lang.type.UserType;
import ch.chassaing.jack.lang.var.VarScope;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * One per class
 */
public class CompilerVisitor
        extends JackBaseVisitor<Type> {
    private final VMWriter vmWriter;

    private ClassInfo classInfo;
    private SubroutineInfo subroutineInfo;

    private VarScope varScope; // the scope of the variable being declared

    public CompilerVisitor(VMWriter vmWriter) {this.vmWriter = vmWriter;}

    public ClassInfo getClassInfo() {

        return classInfo;
    }

    private void raise(@NotNull String message,
                       @NotNull ParserRuleContext ctx) {

        throw new IllegalArgumentException(message + " at " + ctx.getText());
    }

    @Override
    public Type visitClass(JackParser.ClassContext ctx) {

        classInfo = new ClassInfo(ctx.ID().getText());
        visitChildren(ctx);
        return new UserType(classInfo.getName());
    }

    @Override
    public Type visitStaticVarDec(JackParser.StaticVarDecContext ctx) {

        requireNonNull(classInfo);
        mustBeNull(varScope);
        varScope = VarScope.STATIC;
        Type type = visitVarDec(ctx.varDec());
        varScope = null;
        return type;
    }

    @Override
    public Type visitFieldVarDec(JackParser.FieldVarDecContext ctx) {

        requireNonNull(classInfo);
        mustBeNull(varScope);
        varScope = VarScope.FIELD;
        Type type = visitVarDec(ctx.varDec());
        varScope = null;
        return type;
    }

    @Override
    public Type visitSubroutineDec(JackParser.SubroutineDecContext ctx) {

        requireNonNull(classInfo);
        mustBeNull(subroutineInfo);

        SubroutineScope scope;
        if (ctx.FUNCTION() != null) scope = SubroutineScope.FUNCTION;
        else if (ctx.CONSTRUCTOR() != null) scope = SubroutineScope.CONSTRUCTOR;
        else scope = SubroutineScope.METHOD;

        Type returnType = null;
        if (ctx.type() != null) {
            returnType = requireNonNull(visitType(ctx.type()));
        }

        String name = ctx.ID().getText();
        subroutineInfo = new SubroutineInfo(classInfo, name, scope, returnType);
        classInfo.addSubroutine(subroutineInfo);
        if (scope == SubroutineScope.METHOD) {
            subroutineInfo.addParameter("this", new UserType(classInfo.getName()));
        }

        ctx.parameter().forEach(this::visitParameter);

        Type blockType = visitBlock(ctx.block());
        if (!Objects.equals(returnType, blockType)) {
            raise("Return type (%s) doesn't correspond to type returned in block (%s)"
                          .formatted(returnType, blockType),
                  ctx);
        }

        subroutineInfo = null;
        return returnType;
    }

    @Override
    public Type visitParameter(JackParser.ParameterContext ctx) {

        requireNonNull(subroutineInfo);
        Type type = visitType(ctx.type()); // determine the type

        String name = ctx.ID().getText();
        if (!subroutineInfo.addParameter(name, type)) {
            raise("Duplicate parameter " + name, ctx);
        }

        return type;
    }

    @Override
    public Type visitLocalVarDec(JackParser.LocalVarDecContext ctx) {

        mustBeNull(varScope);
        varScope = VarScope.LOCAL;
        Type type = visitVarDec(ctx.varDec());
        varScope = null;
        return type;
    }

    @Override
    public Type visitVarDec(JackParser.VarDecContext ctx) {

        requireNonNull(classInfo);
        requireNonNull(varScope);
        if (varScope == VarScope.LOCAL) {
            requireNonNull(subroutineInfo);
        }
        Type type = visitType(ctx.type());

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

        return type;
    }

    @Override
    public Type visitType(JackParser.TypeContext ctx) {

        if (ctx.INT() != null) {
            return PrimitiveType.INT;
        } else if (ctx.CHAR() != null) {
            return PrimitiveType.CHAR;
        } else if (ctx.BOOL() != null) {
            return PrimitiveType.BOOLEAN;
        } else {
            return new UserType(ctx.ID().getText());
        }
    }

    @Override
    public Type visitEquality(JackParser.EqualityContext ctx) {

        TerminalNode op = null;
        Type previousType = null;
        int childCount = ctx.getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (i % 2 == 1) {
                op = (TerminalNode) ctx.getChild(i);
            } else {
                Type type = visitComparison((JackParser.ComparisonContext) ctx.getChild(i));
                if (op != null) {
                    if (!Objects.equals(previousType, type)) {
                        raise("Types must match : %s; %s".formatted(previousType, type), ctx);
                    }
                    vmWriter.writeArithmetic(Command.EQ);
                    if (op.getSymbol().getType() == JackParser.UNEQUAL) {
                        vmWriter.writeArithmetic(Command.NOT);
                    }
                    op = null;
                }
                previousType = type;
            }
        }

        return childCount == 1 ? requireNonNull(previousType) : PrimitiveType.BOOLEAN;
    }

    @Override
    public Type visitComparison(JackParser.ComparisonContext ctx) {

        TerminalNode op = null;
        Type previousType = null;
        int childCount = ctx.getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (i % 2 == 1) {
                op = (TerminalNode) ctx.getChild(i);
            } else {
                Type type = visitTerm((JackParser.TermContext) ctx.getChild(i));
                if (op != null) {
                    if (!Objects.equals(previousType, type) ||
                        !(PrimitiveType.INT.equals(type) || PrimitiveType.CHAR.equals(type))) {
                        raise("Types must match and be either int or char: %s; %s"
                                      .formatted(previousType, type), ctx);
                    }
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
                previousType = type;
            }
        }
        return childCount == 1 ? previousType : PrimitiveType.BOOLEAN;
    }

    @Override
    public Type visitTerm(JackParser.TermContext ctx) {

        TerminalNode op = null;
        Type previousType = null;
        int childCount = ctx.getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (i % 2 == 1) {
                op = (TerminalNode) ctx.getChild(i);
            } else {
                Type type = visitFactor((JackParser.FactorContext) ctx.getChild(i));
                if (op != null) {
                    if (!Objects.equals(previousType, type) || !PrimitiveType.INT.equals(type)) {
                        raise("Types must match : %s; %s".formatted(previousType, type), ctx);
                    }
                    if (op.getSymbol().getType() == JackParser.MULT) {
                        vmWriter.writeCall("Math.multiply", 2);
                    } else if (op.getSymbol().getType() == JackParser.DIV) {
                        vmWriter.writeCall("Math.divide", 2);
                    } else { // must be OR
                        vmWriter.writeArithmetic(Command.OR);
                    }
                    op = null;
                }
                previousType = type;
            }
        }
        return previousType;
    }

    @Override
    public Type visitFactor(JackParser.FactorContext ctx) {

        TerminalNode op = null;
        Type previousType = null;
        int childCount = ctx.getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (i % 2 == 1) {
                op = (TerminalNode) ctx.getChild(i);
            } else {
                Type type = visitUnary((JackParser.UnaryContext) ctx.getChild(i));
                if (op != null) {
                    if (!Objects.equals(previousType, type) || !PrimitiveType.INT.equals(type)) {
                        raise("Types must match : %s; %s".formatted(previousType, type), ctx);
                    }
                    if (op.getSymbol().getType() == JackParser.PLUS) {
                        vmWriter.writeArithmetic(Command.ADD);
                    } else if (op.getSymbol().getType() == JackParser.MINUS) {
                        vmWriter.writeArithmetic(Command.SUB);
                    } else { // must be AND
                        vmWriter.writeArithmetic(Command.AND);
                    }
                    op = null;
                }
                previousType = type;
            }
        }
        return previousType;
    }

    @Override
    public Type visitUnary(JackParser.UnaryContext ctx) {

        visitChildren(ctx);
        return PrimitiveType.INT;
    }

    @Override
    public Type visitPrimary(JackParser.PrimaryContext ctx) {

        if (ctx.NUMBER() != null) {
            vmWriter.writePush(Segment.CONSTANT, Integer.parseInt(ctx.NUMBER().getText()));
            return PrimitiveType.INT;
        } else if (ctx.STRING() != null) {
            // TODO write code to create String object
            return new UserType("String");
        } else if (ctx.subroutineCall() != null) {
            return visitSubroutineCall(ctx.subroutineCall());
        } else if (ctx.TRUE() != null) {
            // TODO verify
            vmWriter.writePush(Segment.CONSTANT, 0);
            vmWriter.writeArithmetic(Command.NOT);
            return PrimitiveType.BOOLEAN;
        } else if (ctx.FALSE() != null) {
            // TODO verify
            vmWriter.writePush(Segment.CONSTANT, 0);
            return PrimitiveType.BOOLEAN;
        } else if (ctx.NULL() != null) {
            // TODO verify
            vmWriter.writePush(Segment.CONSTANT, 0);
            return PrimitiveType.BOOLEAN;
        } else if (ctx.THIS() == null) {
            // TODO verify
            vmWriter.writePush(Segment.POINTER, 0);
            return new UserType(classInfo.getName());
        } else {
            return visitExpression(ctx.expression());
        }
    }

    @Override
    public Type visitSubroutineCall(JackParser.SubroutineCallContext ctx) {

        return new UserType(ctx.ID().getText());
    }

    private static void mustBeNull(Object object) {

        if (object != null) {
            throw new IllegalArgumentException();
        }
    }
}
