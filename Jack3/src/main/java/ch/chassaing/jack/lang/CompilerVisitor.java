package ch.chassaing.jack.lang;

import ch.chassaing.jack.lang.subroutine.SubroutineScope;
import ch.chassaing.jack.lang.type.*;
import ch.chassaing.jack.lang.var.VarScope;
import ch.chassaing.jack.lang.writer.VMWriter;
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

    public CompilerVisitor(VMWriter vmWriter) {this.vmWriter = vmWriter;}

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

        // set up this for methods and constructors
        if (scope == SubroutineScope.METHOD) {
            vmWriter.writePush(Segment.ARGUMENT, 0);
            vmWriter.writePop(Segment.POINTER, 0);
        } else if (scope == SubroutineScope.CONSTRUCTOR) {
            int numberOfFields = classInfo.numberOfFields();
            if (numberOfFields == 0) {
                raise("Constructor in class without fields", ctx);
            }
            vmWriter.writePush(Segment.CONSTANT, numberOfFields);
            vmWriter.writeCall("Memory.alloc", 1);
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
    public Object visitAssignVariable(JackParser.AssignVariableContext ctx)
    {

        VarInfo varInfo = visitVarUse(ctx.varUse());
        Type varType = varInfo.type();
        Type expressionType = (Type) visitExpression(ctx.expression());

        if (!varType.compatible(expressionType)) {
            raise("Expression type (%s) is not of the variable type (%s)"
                          .formatted(expressionType, varType), ctx);
        }
        vmWriter.writePop(varInfo.scope().segment, varInfo.order());
        return null;
    }

    @Override
    public Object visitAssignArray(JackParser.AssignArrayContext ctx)
    {
        /*
         * We first evaluate the right hand side of the assignment. This way
         * we don't need to store the right hand side in a temp as is
         * suggested in the book. This is possible, because we are using an AST.
         */
        VarInfo varInfo = visitVarUse(ctx.varUse());
        if (varInfo.type() != Array.INSTANCE) {
            raise("%s must be of type Array".formatted(varInfo), ctx);
        }

        // an Array is not (yet) typed, thus we don't care about the expression(1) type
        visitExpression(ctx.expression(1));

        vmWriter.writePush(varInfo.scope().segment, varInfo.order());
        Type idxType = (Type) visitExpression(ctx.expression(0));
        if (!PrimitiveType.INT.compatible(idxType)) {
            raise("expression must resolve to int type", ctx);
        }
        vmWriter.writeArithmetic(Command.ADD);

        // pop the address to THAT
        vmWriter.writePop(Segment.POINTER, 1);

        // now pop expression(1) to THAT 0
        vmWriter.writePop(Segment.THAT, 0);

        return null;
    }

    @Override
    public Type visitIfStatement(JackParser.IfStatementContext ctx)
    {

        String elseLabel = subroutineInfo.nextLabel();
        String afterLabel = subroutineInfo.nextLabel();
        Type expressionType = (Type) visitExpression(ctx.expression());
        if (PrimitiveType.BOOLEAN != expressionType) {
            raise("Expression in if must be boolean", ctx);
        }
        boolean hasElse = ctx.ifStatement() != null ||
                          ctx.block(1) != null;

        vmWriter.writeArithmetic(Command.NOT);
        vmWriter.writeIf(elseLabel);
        visitBlock(ctx.block(0));
        if (hasElse) {
            vmWriter.writeGoto(afterLabel);
        }
        vmWriter.writeLabel(elseLabel);

        if (hasElse) {

            if (ctx.ifStatement() != null) {
                visitIfStatement(ctx.ifStatement());
            } else if (ctx.block(1) != null) {
                visitBlock(ctx.block(1));
            }
            vmWriter.writeLabel(afterLabel);
        }

        return null;
    }

    @Override
    public Type visitWhileStatement(JackParser.WhileStatementContext ctx)
    {

        String whileLabel = subroutineInfo.nextLabel();
        String afterLabel = subroutineInfo.nextLabel();

        vmWriter.writeLabel(whileLabel);
        Type expressionType = (Type) visitExpression(ctx.expression());
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
            returnType = (Type) visitExpression(ctx.expression());
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
    public Type visitCombination(JackParser.CombinationContext ctx)
    {

        TerminalNode op = null;
        Type type = null, previousType = null;
        int childCount = ctx.getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (i % 2 == 1) {
                op = (TerminalNode) ctx.getChild(i);
                previousType = type;
            } else {
                if (op == null) {
                    type = visitEquality((JackParser.EqualityContext) ctx.getChild(i));
                    if (type == null) {
                        raise("type is null", ctx);
                    }
                } else {
                    String skipLabel = subroutineInfo.nextLabel("skipShortCircuit");
                    String endLabel = subroutineInfo.nextLabel("endShortCircuit");

                    if (op.getSymbol().getType() == JackParser.SHORT_AND) {
                        vmWriter.writeArithmetic(Command.NOT);
                    }

                    // skip to end if && false or || true
                    vmWriter.writeIf(skipLabel);
                    // evaluate second only if not skipped
                    type = visitEquality((JackParser.EqualityContext) ctx.getChild(i));
                    // only booleans allowed
                    if (!PrimitiveType.BOOLEAN.compatible(type) ||
                        !PrimitiveType.BOOLEAN.compatible(previousType)) {
                        raise("Short circuit can only be applied to booleans", ctx);
                    }
                    vmWriter.writeGoto(endLabel);
                    vmWriter.writeLabel(skipLabel);

                    // result of skipped is false for && and true for ||
                    vmWriter.writePush(Segment.CONSTANT, 0);
                    if (op.getSymbol().getType() == JackParser.SHORT_OR) {
                        vmWriter.writeArithmetic(Command.NOT);
                    }
                    vmWriter.writeLabel(endLabel);
                }
            }
        }
        return requireNonNull(type);
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
        Type type = null;
        Type previousType = null;
        int childCount = ctx.getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (i % 2 == 1) { // i odd
                op = (TerminalNode) ctx.getChild(i);
                previousType = type;
            } else { // i even
                type = visitFactor((JackParser.FactorContext) ctx.getChild(i));
                if (type == null) {
                    raise("A term element must have a type", ctx);
                }
                if (op != null) {
                    if (op.getSymbol().getType() == JackParser.OR) {
                        // types must not be UserType and should be compatible
                        if (type instanceof UserType || previousType instanceof UserType) {
                            raise("OR not permitted with user types", ctx);
                        } else if (!type.compatible(previousType)) {
                            raise("Types must be compatible for OR", ctx);
                        }
                    } else if (!PrimitiveType.INT.compatible(type) || !PrimitiveType.INT.compatible(previousType)) {
                        raise("Types must be compatible with int : %s; %s".formatted(previousType, type), ctx);
                    }
                    switch (op.getSymbol().getType()) {
                        case JackParser.MINUS -> vmWriter.writeArithmetic(Command.SUB);
                        case JackParser.PLUS -> vmWriter.writeArithmetic(Command.ADD);
                        case JackParser.OR -> vmWriter.writeArithmetic(Command.OR);
                        default -> raise("Unknown symbol " + op, ctx);
                    }
                }
                op = null;
            }
        }
        return requireNonNull(type);
    }

    @Override
    public Type visitFactor(JackParser.FactorContext ctx)
    {

        TerminalNode op = null;
        Type previousType = null;
        Type type = null;
        int childCount = ctx.getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (i % 2 == 1) {
                op = (TerminalNode) ctx.getChild(i);
                previousType = type;
            } else {
                type = visitUnary((JackParser.UnaryContext) ctx.getChild(i));
                if (type == null) {
                    raise("A factor element must have a type", ctx);
                }
                if (op != null) {
                    type = visitUnary((JackParser.UnaryContext) ctx.getChild(i));
                    if (type == null) {
                        raise("A factor element must have a type", ctx);
                    }
                    if (op.getSymbol().getType() == JackParser.AND) {
                        // types must not be UserType and should be compatible
                        if (type instanceof UserType || previousType instanceof UserType) {
                            raise("AND not permitted with user types", ctx);
                        } else if (!type.compatible(previousType)) {
                            raise("Types must be compatible for AND", ctx);
                        }
                    } else if (!PrimitiveType.INT.compatible(type) || !PrimitiveType.INT.compatible(previousType)) {
                        raise("Types must be compatible with int : %s; %s".formatted(previousType, type), ctx);
                    }
                    switch (op.getSymbol().getType()) {
                        case JackParser.DIV -> vmWriter.writeCall("Math.divide", 2);
                        case JackParser.MULT -> vmWriter.writeCall("Math.multiply", 2);
                        case JackParser.AND -> vmWriter.writeArithmetic(Command.AND);
                        default -> raise("Unknown symbol " + op, ctx);
                    }
                }
                op = null;
            }
        }
        return requireNonNull(type);
    }

    @Override
    public Type visitUnary(JackParser.UnaryContext ctx)
    {

        Type type = (Type) visitChildren(ctx);
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
                raise("Must be boolean", ctx);
            }
            vmWriter.writeArithmetic(Command.NOT);
        }
        return type;
    }

    @Override
    public Type visitPrimary(JackParser.PrimaryContext ctx)
    {

        if (ctx.expression() != null) {
            return (Type) visitExpression(ctx.expression());
        } else if (ctx.subroutineCall() != null) {
            return (Type) visitSubroutineCall(ctx.subroutineCall());
        } else if (ctx.arrayReferencing() != null) {
            return visitArrayReferencing(ctx.arrayReferencing());
        } else if (ctx.varUse() != null) {
            VarInfo varInfo = visitVarUse(ctx.varUse());
            vmWriter.writePush(varInfo.scope().segment, varInfo.order());
            return varInfo.type();
        } else if (ctx.NUMBER() != null) {
            vmWriter.writePush(Segment.CONSTANT, Integer.parseInt(ctx.NUMBER().getText()));
            return PrimitiveType.INT;
        } else if (ctx.STRING() != null) {
            String text = ctx.STRING().getText();
            // maxlength ist länge des texts
            vmWriter.writePush(Segment.CONSTANT, text.length());
            vmWriter.writeCall("String.new", 1); // neuer String auf dem Stack
            for (int i = 1; i < text.length() - 1; i++) {
                // THIS sollte auf dem Stack bleiben
                vmWriter.writePush(Segment.CONSTANT, text.charAt(i));
                vmWriter.writeCall("String.appendChar", 2); // this und char
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
            if (!subroutineInfo.scope().callFromLocal()) {
                raise("this can only be used in method", ctx);
            }
            vmWriter.writePush(Segment.POINTER, 0);
            return Type.of(classInfo.name());
        }

        raise("Unknown primary", ctx);
        return null;
    }

    @Override
    @NotNull
    public VarInfo visitVarUse(JackParser.VarUseContext ctx)
    {

        String varName = ctx.ID().getText();
        if (ctx.THIS() != null) {
            VarInfo result = classInfo.findVar(varName);
            if (result == null) {
                raise("Field %s not found in %s".formatted(varName, classInfo.name()), ctx);
            }
            if (result.scope() != VarScope.FIELD) {
                raise("Variable accessed with this. must be field", ctx);
            }
            return result;
        }

        return getVarInfo(ctx, varName);
    }

    @Override
    public Type visitArrayReferencing(JackParser.ArrayReferencingContext ctx)
    {

        String varName = ctx.ID().getText();
        VarInfo varInfo = getVarInfo(ctx, varName);
        if (varInfo.type() != Array.INSTANCE) {
            raise("%s must be of type Array".formatted(varInfo), ctx);
        }
        vmWriter.writePush(varInfo.scope().segment, varInfo.order());
        Type idxType = (Type) visitExpression(ctx.expression());
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
        return UnknownType.INSTANCE;
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

    @Override
    public Type visitCallLocal(JackParser.CallLocalContext ctx)
    {

        if (!subroutineInfo.scope().callFromLocal()) {
            raise("local calls can only be made from other methods", ctx);
        }
        String name = ctx.ID().getText();
        int nArgs = ctx.expressionList().expression().size();
        vmWriter.writePush(Segment.POINTER, 0); // THIS onto the stack
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
            if (!(var.type() instanceof UserType || var.type() instanceof Array)) {
                raise("Cannot call methods on primitive types", ctx);
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
            String callee = var.type() instanceof UserType userType ?
                            userType.name() : "Array";
            vmWriter.writeCall(callee + "." + fun, nArgs + 1);
        } else {
            // Muss Funktion oder Konstruktor sein
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
