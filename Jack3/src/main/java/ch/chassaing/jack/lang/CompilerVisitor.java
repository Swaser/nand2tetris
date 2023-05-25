package ch.chassaing.jack.lang;

import ch.chassaing.jack.lang.subroutine.SubroutineScope;
import ch.chassaing.jack.lang.type.PrimitiveType;
import ch.chassaing.jack.lang.type.Type;
import ch.chassaing.jack.lang.type.UnknownType;
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
        return new UserType(classInfo.name());
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
        if (!classInfo.addSubroutine(subroutineInfo)) {
            raise("Subroutine exists: " + name, ctx);
        }
        if (scope == SubroutineScope.METHOD) {
            subroutineInfo.addParameter("this", new UserType(classInfo.name()));
        }

        ctx.parameter().forEach(this::visitParameter);

        for (JackParser.LocalVarDecContext localVarCtx : ctx.localVarDec()) {
            visitLocalVarDec(localVarCtx);
        }

        Type blockType = null;
        for (JackParser.StatementContext statementContext : ctx.statement()) {
            blockType = visitStatement(statementContext);
        }

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
    public Type visitLetStatement(JackParser.LetStatementContext ctx) {

        VarInfo varInfo = getVarInfo(ctx, ctx.ID().getText());
        Type varType = varInfo.type();
        Type expressionType = visitExpression(ctx.expression());
        // expression is now on stack

        if (!varType.equals(expressionType)) {
            raise("Expression type (%s) is not of the variable type (%s)"
                          .formatted(expressionType, varType), ctx);
        }
        switch (varInfo.scope()) {
            case STATIC -> vmWriter.writePop(Segment.STATIC, varInfo.order());
            case FIELD -> vmWriter.writePop(Segment.THIS, varInfo.order());
            case PARAMETER -> vmWriter.writePop(Segment.ARGUMENT, varInfo.order());
            case LOCAL -> vmWriter.writePop(Segment.LOCAL, varInfo.order());
        }
        return varType;
    }

    @Override
    public Type visitIfStatement(JackParser.IfStatementContext ctx) {

        String elseLabel = subroutineInfo.nextLabel();
        String afterLabel = subroutineInfo.nextLabel();
        Type expressionType = visitExpression(ctx.expression());
        if (!expressionType.equals(PrimitiveType.BOOLEAN)) {
            raise("Expression in if must be boolean", ctx);
        }
        vmWriter.writeArithmetic(Command.NOT);
        vmWriter.writeIf(elseLabel);

        visitBlock(ctx.block(0));
        vmWriter.writeGoto(afterLabel);

        vmWriter.writeLabel(elseLabel);
        if (ctx.ifStatement() != null) {
            visitIfStatement(ctx.ifStatement());
        } else if (ctx.block(1) != null) {
            visitBlock(ctx.block(1));
        }

        vmWriter.writeLabel(afterLabel);
        return null;
    }

    @Override
    public Type visitWhileStatement(JackParser.WhileStatementContext ctx) {

        String whileLabel = subroutineInfo.nextLabel();
        String afterLabel = subroutineInfo.nextLabel();

        vmWriter.writeLabel(whileLabel);
        Type expressionType = visitExpression(ctx.expression());
        if (!expressionType.equals(PrimitiveType.BOOLEAN)) {
            raise("Expression in while must be boolean", ctx);
        }
        vmWriter.writeArithmetic(Command.NOT);
        vmWriter.writeIf(afterLabel);

        visitBlock(ctx.block());
        vmWriter.writeGoto(whileLabel);

        vmWriter.writeLabel(afterLabel);
        return null;
    }

    @Override
    public Type visitDoStatement(JackParser.DoStatementContext ctx) {

        visitSubroutineCall(ctx.subroutineCall());
        vmWriter.writePop(Segment.TEMP, 0);
        return null;
    }

    @Override
    public Type visitReturnStatement(JackParser.ReturnStatementContext ctx) {

        Type returnType = null;
        if (ctx.expression() != null) {
            returnType = visitExpression(ctx.expression());
        } else {
            vmWriter.writePush(Segment.CONSTANT, 0);
        }
        vmWriter.writeReturn();
        if (!Objects.equals(subroutineInfo.returnType(), returnType)) {
            raise("Return type (%s) must correspond to subroutine type (%s)"
                          .formatted(returnType, subroutineInfo.returnType()), ctx);
        }
        return returnType;
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

        if (ctx.expression() != null) {
            return visitExpression(ctx.expression());
        } else if (ctx.ID() != null) {
            VarInfo varInfo = getVarInfo(ctx.getParent(), ctx.ID().getText());
            switch (varInfo.scope()) {
                case STATIC -> vmWriter.writePush(Segment.STATIC, varInfo.order());
                case FIELD -> vmWriter.writePush(Segment.THIS, varInfo.order());
                case PARAMETER -> vmWriter.writePush(Segment.ARGUMENT, varInfo.order());
                case LOCAL -> vmWriter.writePush(Segment.LOCAL, varInfo.order());
            }
            return varInfo.type();
        } else if (ctx.NUMBER() != null) {
            vmWriter.writePush(Segment.CONSTANT, Integer.parseInt(ctx.NUMBER().getText()));
            return PrimitiveType.INT;
        } else if (ctx.STRING() != null) {
            vmWriter.writeCall("String.new", 0); // neuer String auf dem Stack
            String text = ctx.STRING().getText();
            for (int i = 1; i < text.length() - 1; i++) {
                // THIS sollte auf dem Stack bleiben
                vmWriter.writePush(Segment.CONSTANT, text.charAt(i));
                vmWriter.writeCall("String.append", 2); // this und char
            }
            return new UserType("String");
        } else if (ctx.TRUE() != null) {
            // TRUE ist -1
            vmWriter.writePush(Segment.CONSTANT, 0);
            vmWriter.writeArithmetic(Command.NOT);
            return PrimitiveType.BOOLEAN;
        } else if (ctx.FALSE() != null) {
            // FALSE ist 0
            vmWriter.writePush(Segment.CONSTANT, 0);
            return PrimitiveType.BOOLEAN;
        } else if (ctx.NULL() != null) {
            vmWriter.writePush(Segment.CONSTANT, 0);
            return UnknownType.INSTANCE;
        } else if (ctx.THIS() != null) {
            if (subroutineInfo.scope() != SubroutineScope.METHOD) {
                raise("this can only be used in method", ctx);
            }
            vmWriter.writePush(Segment.ARGUMENT, 0);
            return new UserType(classInfo.name());
        }

        raise("Unknown primary", ctx);
        return null;
    }

    @NotNull
    private VarInfo getVarInfo(ParserRuleContext ctx,
                               String varName) {

        VarInfo varInfo = subroutineInfo.findVar(varName);
        if (varInfo == null) {
            raise("Couldn't find variable " + varName, ctx);
        }
        return varInfo;
    }

    /* Muss Methode sein und kann nur von Methode aus aufgerufen werden */
    @Override
    public Type visitCallLocal(JackParser.CallLocalContext ctx) {

        if (subroutineInfo.scope() != SubroutineScope.METHOD) {
            raise("local calls can only be made from other methods", ctx);
        }
        String name = ctx.ID().getText();
        int nArgs = ctx.expressionList().expression().size();
        vmWriter.writePush(Segment.ARGUMENT,0); // Adresse des Objekts auf den Stack
        visitExpressionList(ctx.expressionList());
        vmWriter.writeCall(classInfo.name() + "." + name, nArgs + 1);
        return UnknownType.INSTANCE;
    }

    @Override
    public Type visitCallRemote(JackParser.CallRemoteContext ctx) {
        /*
         In Nand 2 Tetris haben wir keine Kenntnis über andere Klassen.
         Deshalb gilt folgende Konvention:
         - Wenn der Aufgerufene als Variable bekannt ist, dann ist es eine Methode
         - Sonst ist es eine Funktion oder ein Konstruktor
        */
        String other = ctx.ID(0).getText();
        String fun = ctx.ID(1).getText();
        int nArgs = ctx.expressionList().expression().size();
        VarInfo var = subroutineInfo.findVar(other);
        
        if (var != null) {
            // Muss eine Methode sein
            if (!(var.type() instanceof UserType)) {
                raise("Can only call methods on user types", ctx);
            }
            if (var.scope() == VarScope.FIELD) {
                if (subroutineInfo.scope() == SubroutineScope.FUNCTION) {
                    raise("Cannot use field %s in function %s"
                                  .formatted(var.name(), subroutineInfo.name()), ctx);
                }
            }
            // Objekt Adresse auf den Stack holen
            vmWriter.writePush(var.scope().segment, var.order());
            // (restliche) Argumente auf den Stack holen
            visitExpressionList(ctx.expressionList());
            vmWriter.writeCall( ((UserType) var.type()).name() + "." + fun, nArgs+1);
        } else {
            // Muss Funktion oder Konstruktor sein
            if (nArgs == 0) {
                // Platz schaffen für return Wert
                vmWriter.writePush(Segment.CONSTANT, 0);
                nArgs = 1;
            }
            visitExpressionList(ctx.expressionList());
            vmWriter.writeCall(other + "." + fun, nArgs);
        }
        return UnknownType.INSTANCE;
    }

    private static void mustBeNull(Object object) {

        if (object != null) {
            throw new IllegalArgumentException();
        }
    }
}
