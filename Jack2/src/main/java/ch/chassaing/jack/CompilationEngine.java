package ch.chassaing.jack;

import ch.chassaing.jack.parse.*;
import ch.chassaing.jack.token.Identifier;
import ch.chassaing.jack.token.Keyword;
import ch.chassaing.jack.token.Symbol;
import ch.chassaing.jack.token.Token;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static ch.chassaing.jack.Contracts.precondition;
import static ch.chassaing.jack.token.Keyword.*;
import static ch.chassaing.jack.token.Symbol.*;
import static java.util.Objects.requireNonNull;

public final class CompilationEngine
{
    private final Tokenizer tokenizer;

    private Token currentToken;
    private String className;
    private String subroutineName;
    private Keyword subroutineType;
    private Map<String, VarType> staticVars;
    private Map<String, VarType> fields;
    private Map<String, VarType> parameters;
    private Map<String, VarType> localVars;

    public CompilationEngine(@NotNull Tokenizer tokenizer)
    {
        this.tokenizer = tokenizer;
    }

    /**
     * Compile the next class in the token stream. Returns true if
     * there was a class to compile or false, if there are no more
     * classes to compile.<br/>
     * Since all code must be in classes, we consume the first token
     * and fail the parsing if it ain't a class.
     */
    public void compileClass()
    {
        precondition(CLASS.equals(currentToken));
        precondition(staticVars == null);
        precondition(fields == null);

        advance();
        className = consumeIdentifier();
        report("class %s%n", className);

        staticVars = new LinkedHashMap<>();
        fields = new LinkedHashMap<>();

        consumeSymbol(LEFT_BRACE);

        while (true) {

            advance();
            if (RIGHT_BRACE.equals(currentToken)) {
                break;
            } else if (STATIC.equals(currentToken)) {
                for (VarDec varDec : compileClassVarDec()) {
                    checkClassVarNotExists(varDec.name());
                    staticVars.put(varDec.name(), varDec.type());
                }
            } else if (FIELD.equals(currentToken)) {
                for (VarDec varDec : compileClassVarDec()) {
                    checkClassVarNotExists(varDec.name());
                    fields.put(varDec.name(), varDec.type());
                }
            } else {
                compileSubroutineDec();
            }
        }

        // class is now ended
        className = null;
        staticVars = null;
        fields = null;
    }

    private void checkClassVarNotExists(@NotNull String varName)
    {
        if (staticVars.containsKey(varName) || fields.containsKey(varName)) {
            throw reportError("Duplicate class variable declaration " + varName);
        }
    }

    @NotNull
    private Set<VarDec> compileClassVarDec()
    {
        precondition(STATIC.equals(currentToken) || FIELD.equals(currentToken));
        Token qualifier = currentToken;

        advance();
        VarType type = requireNonNull(determineVarType(false));

        Set<VarDec> classVariables = new LinkedHashSet<>();
        boolean hasVar = false;
        while (true) {
            advance();
            if (Symbol.SEMICOLON.equals(currentToken)) {
                if (!hasVar) throw reportError("Expecting variable identifier");
                break;
            } else if (Symbol.COMMA.equals(currentToken)) {
                if (!hasVar) throw reportError("Expecting variable identifier");
                hasVar = false;
            } else {
                String name = consumeIdentifier();
                classVariables.add(new VarDec(type, name));
                hasVar = true;
            }
        }

        report("class variables %s %s %s%n", qualifier, type, classVariables);

        return classVariables;
    }

    @Nullable
    private VarType determineVarType(boolean canBeVoid)
    {
        VarType varType = VarType.fromToken(currentToken);
        if (varType == null) {
            if (!canBeVoid || !VOID.equals(currentToken)) {
                throw reportError("Expecting primitive type or user type");
            }
        }
        return varType;
    }

    private void compileSubroutineDec()
    {
        precondition(subroutineType == null);
        precondition(CONSTRUCTOR.equals(currentToken) ||
                     FUNCTION.equals(currentToken) ||
                     METHOD.equals(currentToken));
        subroutineType = (Keyword) currentToken;

        advance();
        VarType returnType = determineVarType(true);

        advance();
        subroutineName = consumeIdentifier();
        report("Subroutine %s %s %s".formatted(subroutineType,
                                               returnType == null ? "void" : returnType.toString(),
                                               subroutineName));

        compileParameterList();
        compileSubroutineBody();

        parameters = null;
        localVars = null;
        subroutineName = null;
        subroutineType = null;
    }

    private void compileParameterList()
    {
        precondition(parameters == null);

        advance();
        consumeSymbol(LEFT_PAREN);

        parameters = new LinkedHashMap<>();
        boolean hasParameter = false;
        while (true) {
            advance();
            if (COMMA.equals(currentToken)) {
                if (!hasParameter) throw reportError("Unexpected comma");
                hasParameter = false;
            } else if (RIGHT_PAREN.equals(currentToken)) {
                if (!hasParameter) throw reportError("Unexpected comma");
                break;
            } else {
                VarType type = determineVarType(false);
                advance();
                String name = consumeIdentifier();
                if (parameters.containsKey(name)) throw reportError("Duplicate parameter " + name);
                report("parameter %s %s%n", type, name);
                parameters.put(name, type);
                hasParameter = true;
            }
        }
    }

    private void compileSubroutineBody()
    {
        precondition(localVars == null);

        consumeSymbol(LEFT_BRACE);

        localVars = new LinkedHashMap<>();
        while (true) {
            advance();
            if (VAR.equals(currentToken)) {
                compileVarDec();
            } else if (RIGHT_BRACE.equals(currentToken)) {
                break;
            } else {
                compileStatement();
            }
        }

        localVars = null;
    }

    private void compileVarDec()
    {
        precondition(localVars != null);
        precondition(VAR.equals(currentToken));

        advance();
        VarType varType = determineVarType(false);

        boolean varFound = false;
        while (true) {
            advance();
            if (SEMICOLON.equals(currentToken)) {
                if (!varFound) throw reportError("Expecting variable identifier before semicolon");
                break;
            } else if (COMMA.equals(currentToken)) {
                if (!varFound) throw reportError("Expecting variable identifier before comma");
                varFound = false;
            } else if (currentToken instanceof Identifier identifier) {
                String name = identifier.value();
                if (localVars.containsKey(name)) {
                    throw reportError("Duplicate local variable " + name);
                }
                report("local variable %s %s%n", varType, name);
                localVars.put(name, varType);
                varFound = true;
            } else {
                throw reportError("Unexpected token");
            }
        }
    }

    private void compileStatement()
    {
        precondition(currentToken instanceof Keyword ||
                     SEMICOLON.equals(currentToken));

        // empty statement
        if (SEMICOLON.equals(currentToken)) {
            return;
        }

        switch ((Keyword) currentToken) {

            case LET -> compileLet();
            case DO -> compileDo();
            case IF -> compileIf();
            case WHILE -> compileWhile();
            case RETURN -> compileReturn();
            default -> throw reportError("Unexpected keyword");
        }
    }

    private void compileLet()
    {
        precondition(Keyword.LET.equals(currentToken));

        advance();
        if (!(currentToken instanceof Identifier varIdentifier)) {
            throw reportError("Expecting variable name");
        }
        String name = varIdentifier.value();

        // handle the [expression] array index
        boolean hasArrayIndex = LEFT_BRACKET.equals(tokenizer.peek());
        if (hasArrayIndex) {
            consumeSymbol(Symbol.LEFT_BRACKET);
            VarType idxType = compileExpression();
            if (!PrimitiveType.INT.equals(idxType)) {
                throw reportError("index of array must be integer type");
            }
            consumeSymbol(Symbol.RIGHT_BRACKET);
        }

        consumeSymbol(Symbol.EQUAL);
        compileExpression();
        consumeSymbol(Symbol.SEMICOLON);

        // TODO now assign to variable
    }

    private void compileIf()
    {
        precondition(IF.equals(currentToken));

        consumeSymbol(Symbol.LEFT_PAREN);
        VarType conditionType = compileExpression();
        if (!PrimitiveType.BOOLEAN.equals(conditionType)) {
            throw reportError("If condition must be of type boolean");
        }
        consumeSymbol(Symbol.RIGHT_PAREN);

        // TODO jump to after if part if not true
        if (LEFT_BRACE.equals(tokenizer.peek())) compileBlock();
        else compileStatement();
        // TODO jump label
        if (ELSE.equals(tokenizer.peek())) {
            advance(); // consume 'else'
            if (LEFT_BRACE.equals(tokenizer.peek())) compileBlock();
            else compileStatement();
        }
    }

    private void compileWhile()
    {
        precondition(WHILE.equals(currentToken));

        consumeSymbol(LEFT_PAREN);
        VarType conditionType = compileExpression();
        if (!PrimitiveType.BOOLEAN.equals(conditionType)) {
            throw reportError("Condition must be boolean expression");
        }
        consumeSymbol(RIGHT_PAREN);
        if (LEFT_BRACE.equals(tokenizer.peek())) {
            compileBlock();
        } else {
            compileStatement();
        }
    }

    private void compileDo()
    {
        precondition(Keyword.DO.equals(currentToken));

    }

    private void compileReturn()
    {
        precondition(Keyword.RETURN.equals(currentToken));
    }

    private void compileBlock()
    {
        consumeSymbol(LEFT_BRACE);
        while (true) {
            advance();
            if (Symbol.RIGHT_BRACE.equals(advance())) {
                break;
            }
            compileStatement();
        }
    }

    @NotNull
    private VarType compileExpression()
    {
        return null;
    }

    @NotNull
    private String consumeIdentifier()
    {
        if (!(currentToken instanceof Identifier classIdentifier)) {
            throw reportError("Expecting identifier token");
        }
        return classIdentifier.value();
    }

    private void consumeSymbol(@NotNull Symbol symbol)
    {
        if (!symbol.equals(advance())) {
            throw reportError("Expecting " + symbol.repr);
        }
    }

    private Token advance()
    {
        currentToken = tokenizer.advance();
        return currentToken;
    }

    private void report(@NotNull String message, Object... params)
    {
        System.out.printf(message, params);
    }

    private RuntimeException reportError(@NotNull String message)
    {
        return new IllegalArgumentException(
                "%d - %s: %s".formatted(tokenizer.lineNumber(), message, currentToken));
    }
}
