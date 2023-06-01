package ch.chassaing.jack.lang;

import ch.chassaing.jack.lang.subroutine.SubroutineScope;
import ch.chassaing.jack.lang.type.*;
import ch.chassaing.jack.lang.var.VarScope;
import ch.chassaing.jack.lang.writer.VMWriter;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * One per class
 */
public class CompilerVisitor
        extends JackBaseVisitor<Type>
{
    private final VMWriter vmWriter;

    private ClassInfo classInfo;
    private SubroutineInfo subroutineInfo;

    private VarScope varScope; // the scope of the variable being declared

    public CompilerVisitor(VMWriter vmWriter) {this.vmWriter = vmWriter;}

    public ClassInfo getClassInfo()
    {

        return classInfo;
    }

    private void raise(@NotNull String message,
                       @NotNull ParserRuleContext ctx)
    {

        throw new IllegalArgumentException(message + " at " + ctx.getText());
    }

    private void warn(@NotNull String message,
                       @NotNull ParserRuleContext ctx)
    {

        System.out.println(message + " at " + ctx.getText());
    }

    @Override
    public Type visitClass(JackParser.ClassContext ctx)
    {
        classInfo = new ClassInfo(ctx.ID().getText());
        visitChildren(ctx);
        return Type.of(classInfo.name());
    }

    @Override
    public Type visitStaticVarDec(JackParser.StaticVarDecContext ctx)
    {
        requireNonNull(classInfo);
        mustBeNull(varScope);
        varScope = VarScope.STATIC;
        Type type = visitVarDec(ctx.varDec());
        varScope = null;
        return type;
    }

    @Override
    public Type visitFieldVarDec(JackParser.FieldVarDecContext ctx)
    {
        requireNonNull(classInfo);
        mustBeNull(varScope);
        varScope = VarScope.FIELD;
        Type type = visitVarDec(ctx.varDec());
        varScope = null;
        return type;
    }

    @Override
    public Type visitSubroutineDec(JackParser.SubroutineDecContext ctx)
    {
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
            subroutineInfo.addParameter("this", Type.of(classInfo.name()));
        }

        ctx.parameter().forEach(this::visitParameter);

        for (JackParser.LocalVarDecContext localVarCtx : ctx.localVarDec()) {
            visitLocalVarDec(localVarCtx);
        }

        vmWriter.writeFunction(subroutineInfo.fullName(),
                               subroutineInfo.numberOfLocalVars());

        // set up this
        if (scope == SubroutineScope.METHOD) {
            vmWriter.writePush(Segment.ARGUMENT, 0);
            vmWriter.writePop(Segment.POINTER, 0);
        } else if (scope == SubroutineScope.CONSTRUCTOR) {
            int numberOfFields = classInfo.numberOfFields();
            if (numberOfFields == 0) {
                raise("Constructor in class without fields", ctx);
            }
            vmWriter.writePush(Segment.CONSTANT, numberOfFields);
            vmWriter.writeCall("Memory.alloc", numberOfFields);
            vmWriter.writePop(Segment.POINTER, 0);
        }

        for (JackParser.StatementContext statementContext : ctx.statement()) {
            visitStatement(statementContext);
        }

        subroutineInfo = null;
        return returnType;
    }

    @Override
    public Type visitParameter(JackParser.ParameterContext ctx)
    {
        requireNonNull(subroutineInfo);
        Type type = visitType(ctx.type()); // determine the type

        String name = ctx.ID().getText();
        if (!subroutineInfo.addParameter(name, type)) {
            raise("Duplicate parameter " + name, ctx);
        }

        return type;
    }

    @Override
    public Type visitLocalVarDec(JackParser.LocalVarDecContext ctx)
    {
        mustBeNull(varScope);
        varScope = VarScope.LOCAL;
        Type type = visitVarDec(ctx.varDec());
        varScope = null;
        return type;
    }

    @Override
    public Type visitVarDec(JackParser.VarDecContext ctx)
    {
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
    public Type visitType(JackParser.TypeContext ctx)
    {
        if (ctx.INT() != null) {
            return PrimitiveType.INT;
        } else if (ctx.CHAR() != null) {
            return PrimitiveType.CHAR;
        } else if (ctx.BOOL() != null) {
            return PrimitiveType.BOOLEAN;
        } else {
            return Type.of(ctx.ID().getText());
        }
    }

    @Override
    public Type visitAssignVariable(JackParser.AssignVariableContext ctx)
    {
        VarInfo varInfo = getVarInfo(ctx, ctx.ID().getText());
        @NotNull Type varType = varInfo.type();
        Type expressionType = visitExpression(ctx.expression());

        if (!varType.compatible(expressionType)) {
            raise("Expression type (%s) is not of the variable type (%s)"
                          .formatted(expressionType, varType), ctx);
        }
        vmWriter.writePop(varInfo.scope().segment, varInfo.order());
        return null;
    }

    @Override
    public Type visitAssignArray(JackParser.AssignArrayContext ctx) {

        String varName = ctx.ID().getText();
        VarInfo varInfo = getVarInfo(ctx, varName);
        if (varInfo.type() != Array.INSTANCE) {
            raise("%s must be of type Array".formatted(varInfo), ctx);
        }
        vmWriter.writePush(varInfo.scope().segment, varInfo.order());
        Type idxType = visitExpression(ctx.expression(0));
        if (!PrimitiveType.INT.compatible(idxType)) {
            raise("expression must resolve to int type", ctx);
        }
        // expression is now on stack
        // calculate address of array element
        vmWriter.writeArithmetic(Command.ADD);
        // address is now on stack

        Type expressionType = visitExpression(ctx.expression(1));
        if (!PrimitiveType.INT.compatible(expressionType)) {
            raise("Expression type (%s) is not compatible with Array"
                          .formatted(expressionType), ctx);
        }

        // here a swap the two top stack elements would be convenient

        // now save expression in temp 0
        vmWriter.writePop(Segment.TEMP, 0);
        // address to that
        vmWriter.writePop(Segment.POINTER, 1);

        vmWriter.writePush(Segment.TEMP, 0);
        vmWriter.writePop(Segment.THAT, 0);
        return null;
    }

    @Override
    public Type visitIfStatement(JackParser.IfStatementContext ctx)
    {
        String elseLabel = subroutineInfo.nextLabel();
        String afterLabel = subroutineInfo.nextLabel();
        Type expressionType = visitExpression(ctx.expression());
        if (PrimitiveType.BOOLEAN != expressionType) {
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
    public Type visitWhileStatement(JackParser.WhileStatementContext ctx)
    {
        String whileLabel = subroutineInfo.nextLabel();
        String afterLabel = subroutineInfo.nextLabel();

        vmWriter.writeLabel(whileLabel);
        Type expressionType = visitExpression(ctx.expression());
        if (PrimitiveType.BOOLEAN != expressionType) {
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
    public Type visitDoStatement(JackParser.DoStatementContext ctx)
    {
        visitSubroutineCall(ctx.subroutineCall());
        vmWriter.writePop(Segment.TEMP, 0);
        return null;
    }

    @Override
    public Type visitReturnStatement(JackParser.ReturnStatementContext ctx)
    {
        Type returnType = null;
        if (ctx.expression() != null) {
            returnType = visitExpression(ctx.expression());
        } else {
            vmWriter.writePush(Segment.CONSTANT, 0);
        }
        vmWriter.writeReturn();
        if (subroutineInfo.returnType() == null &&
            returnType != null) {
            raise("Return type (%s) doesn't match void return type of subroutine"
                          .formatted(returnType), ctx);
        }
        if (subroutineInfo.returnType() != null &&
            !subroutineInfo.returnType().compatible(returnType)) {
            raise("Return type (%s) must match subroutine type (%s)"
                          .formatted(returnType, subroutineInfo.returnType()), ctx);
        }
        return returnType;
    }

    @Override
    public Type visitEquality(JackParser.EqualityContext ctx)
    {
        TerminalNode op = null;
        Type previousType = null;
        int childCount = ctx.getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (i % 2 == 1) {
                op = (TerminalNode) ctx.getChild(i);
            } else {
                Type type = visitComparison((JackParser.ComparisonContext) ctx.getChild(i));
                if (type == null) {
                    raise("An equality element must have a type", ctx);
                }
                if (op != null) {
                    if (!type.compatible(previousType)) {
                        warn("Possible incompatible types : %s; %s".formatted(previousType, type), ctx);
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

        requireNonNull(previousType);
        return childCount == 1 ? previousType : PrimitiveType.BOOLEAN;
    }

    @Override
    public Type visitComparison(JackParser.ComparisonContext ctx)
    {
        TerminalNode op = null;
        Type previousType = null;
        int childCount = ctx.getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (i % 2 == 1) {
                op = (TerminalNode) ctx.getChild(i);
            } else {
                Type type = visitTerm((JackParser.TermContext) ctx.getChild(i));
                if (type == null) {
                    raise("A comparison element must have a type", ctx);
                }
                if (op != null) {
                    if (!PrimitiveType.INT.compatible(type) ||
                        !PrimitiveType.INT.compatible(previousType)) {
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
        requireNonNull(previousType);
        return childCount == 1 ? previousType : PrimitiveType.BOOLEAN;
    }

    @Override
    public Type visitTerm(JackParser.TermContext ctx)
    {
        TerminalNode op = null;
        Type previousType = null;
        int childCount = ctx.getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (i % 2 == 1) {
                op = (TerminalNode) ctx.getChild(i);
            } else {
                Type type = visitFactor((JackParser.FactorContext) ctx.getChild(i));
                if (type == null) {
                    raise("A term element must have a type", ctx);
                }
                if (op != null) {
                    if (!PrimitiveType.INT.compatible(type) ||
                        !PrimitiveType.INT.compatible(previousType)) {
                        raise("Types must be compatible with int : %s; %s".formatted(previousType, type), ctx);
                    }
                    switch (op.getSymbol().getType()) {
                        case JackParser.MINUS -> vmWriter.writeArithmetic(Command.SUB);
                        case JackParser.PLUS -> vmWriter.writeArithmetic(Command.ADD);
                        case JackParser.OR -> vmWriter.writeArithmetic(Command.OR);
                        default -> raise("Unknown symbol " + op, ctx);
                    }
                    op = null;
                }
                previousType = type;
            }
        }
        requireNonNull(previousType);
        return previousType;
    }

    @Override
    public Type visitFactor(JackParser.FactorContext ctx)
    {
        TerminalNode op = null;
        Type previousType = null;
        int childCount = ctx.getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (i % 2 == 1) {
                op = (TerminalNode) ctx.getChild(i);
            } else {
                Type type = visitUnary((JackParser.UnaryContext) ctx.getChild(i));
                if (type == null) {
                    raise("A factor element must have a type", ctx);
                }
                if (op != null) {
                    if (!PrimitiveType.INT.compatible(type) ||
                        !PrimitiveType.INT.compatible(previousType)) {
                        raise("Types must be compatible with int : %s; %s".formatted(previousType, type), ctx);
                    }
                    switch (op.getSymbol().getType()) {
                        case JackParser.DIV -> vmWriter.writeCall("Math.divide", 2);
                        case JackParser.MULT -> vmWriter.writeCall("Math.multipy", 2);
                        case JackParser.AND -> vmWriter.writeArithmetic(Command.AND);
                        default -> raise("Unknown symbol " + op, ctx);
                    }
                    op = null;
                }
                previousType = type;
            }
        }
        return requireNonNull(previousType);
    }

    @Override
    public Type visitUnary(JackParser.UnaryContext ctx)
    {
        Type type = visitChildren(ctx);
        if (type == null) {
            raise("An unary element must have a type", ctx);
        }
        if (ctx.MINUS() != null) {
            if (!PrimitiveType.INT.compatible(type)) {
                raise("Can only negate ints", ctx);
            }
            vmWriter.writeArithmetic(Command.NEG);
        } else if (ctx.NOT() != null) {
            if (!PrimitiveType.BOOLEAN.compatible(type)) {
                vmWriter.writeArithmetic(Command.NOT);
            }
        }
        return type;
    }

    @Override
    public Type visitPrimary(JackParser.PrimaryContext ctx)
    {
        if (ctx.expression() != null) {
            return visitExpression(ctx.expression());
        } else if (ctx.subroutineCall() != null) {
            return visitSubroutineCall(ctx.subroutineCall());
        } else if (ctx.arrayReferencing() != null) {
            return visitArrayReferencing(ctx.arrayReferencing());
        } else if (ctx.ID() != null) {
            VarInfo varInfo = getVarInfo(ctx, ctx.ID().getText());
            vmWriter.writePush(varInfo.scope().segment, varInfo.order());
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
            return Type.of("String");
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
            // NULL ist 0
            vmWriter.writePush(Segment.CONSTANT, 0);
            return UnknownType.INSTANCE;
        } else if (ctx.THIS() != null) {
            if (subroutineInfo.scope() != SubroutineScope.METHOD) {
                raise("this can only be used in method", ctx);
            }
            vmWriter.writePush(Segment.ARGUMENT, 0);
            return Type.of(classInfo.name());
        }

        raise("Unknown primary", ctx);
        return null;
    }

    @Override
    public Type visitArrayReferencing(JackParser.ArrayReferencingContext ctx) {

        String varName = ctx.ID().getText();
        VarInfo varInfo = getVarInfo(ctx, varName);
        if (varInfo.type() != Array.INSTANCE) {
            raise("%s must be of type Array".formatted(varInfo), ctx);
        }
        vmWriter.writePush(varInfo.scope().segment, varInfo.order());
        Type idxType = visitExpression(ctx.expression());
        if (!PrimitiveType.INT.compatible(idxType)) {
            raise("expression must resolve to int type", ctx);
        }
        // expression is now on stack
        // calculate address of array element
        vmWriter.writeArithmetic(Command.ADD);
        // setup that with address
        vmWriter.writePop(Segment.POINTER, 1);
        // put content of THAT 0
        vmWriter.writePush(Segment.THAT, 0);
        return PrimitiveType.INT;
    }

    @NotNull
    private VarInfo getVarInfo(ParserRuleContext ctx,
                               String varName)
    {
        VarInfo varInfo = subroutineInfo.findVar(varName);
        if (varInfo == null) {
            raise("Couldn't find variable " + varName, ctx);
        }
        return varInfo;
    }

    /* Muss Methode sein und kann nur von Methode aus aufgerufen werden */
    @Override
    public Type visitCallLocal(JackParser.CallLocalContext ctx)
    {
        if (subroutineInfo.scope() != SubroutineScope.METHOD) {
            raise("local calls can only be made from other methods", ctx);
        }
        String name = ctx.ID().getText();
        int nArgs = ctx.expressionList().expression().size();
        vmWriter.writePush(Segment.ARGUMENT, 0); // Adresse des Objekts auf den Stack
        visitExpressionList(ctx.expressionList());
        vmWriter.writeCall(classInfo.name() + "." + name, nArgs + 1);
        return UnknownType.INSTANCE;
    }

    @Override
    public Type visitCallRemote(JackParser.CallRemoteContext ctx)
    {
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
            vmWriter.writeCall(((UserType) var.type()).name() + "." + fun, nArgs + 1);
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

    private static void mustBeNull(Object object)
    {
        if (object != null) {
            throw new IllegalArgumentException();
        }
    }
}
