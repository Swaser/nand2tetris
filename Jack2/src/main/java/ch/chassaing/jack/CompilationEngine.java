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
import static ch.chassaing.jack.token.KeywordType.*;

public final class CompilationEngine
{
    private final Tokenizer tokenizer;

    private String className;
    private String subroutineName;
    private SubroutineType subroutineType;
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
    public boolean compileClass()
    {
        Token token = tokenizer.advance();
        if (token == null) {
            // no more classes
            return false;
        }

        if (!Keyword.CLAZZ.equals(token)) {
            throw reportError("Expected token 'class'", token);
        }


        token = tokenizer.advance();
        if (!(token instanceof Identifier classIdentifier)) {
            throw reportError("Expected token 'identifier'", token);
        }

        className = classIdentifier.value();
        token = tokenizer.advance();
        if (!Symbol.LEFT_BRACE.equals(token)) {
            throw reportError("Expected opening brace '{'", token);
        }

        staticVars = new LinkedHashMap<>();
        fields = new LinkedHashMap<>();
        report("class " + className);

        while ((token = tokenizer.peek()) != null) {

            if (token instanceof Keyword keyword) {

                if (EnumSet.of(STATIC, FIELD).contains(keyword.type())) {
                    Set<ClassVarDec> newClassVariables = compileClassVarDec();
                    for (ClassVarDec classVar : newClassVariables) {
                        if (staticVars.containsKey(classVar.name()) ||
                            fields.containsKey(classVar.name())) {

                            throw reportError("Duplicate class variable declaration " + classVar, null);
                        }
                        if (classVar.qualifier() == Qualifier.STATIC) {
                            staticVars.put(classVar.name(), classVar.varDec().type());
                        } else {
                            fields.put(classVar.name(), classVar.varDec().type());
                        }
                    }

                    continue;
                }

                if (EnumSet.of(CONSTRUCTOR, FUNCTION, METHOD).contains(keyword.type())) {
                    compileSubroutineDec();
                    continue;
                }
            }

            token = tokenizer.advance(); // take next
            if (Symbol.RIGHT_BRACE.equals(token)) {
                break;
            }

            throw reportError("Unexpected token", token);
        }

        // if everything went right, then token contains a right brace
        if (!Symbol.RIGHT_BRACE.equals(token)) {
            throw reportError("Expected closing brace '}'", token);
        }

        className = null;
        staticVars = null;
        fields = null;
        return true;
    }

    @NotNull
    private Set<ClassVarDec> compileClassVarDec()
    {
        Token token = tokenizer.advance();
        Qualifier qualifier;
        if (Keyword.STATIC.equals(token)) {
            qualifier = Qualifier.STATIC;
        } else if (Keyword.FIELD.equals(token)) {
            qualifier = Qualifier.FIELD;
        } else {
            throw reportError("ClassVarDec must start with keyword 'static' or 'field'", token);
        }

        token = tokenizer.advance();
        VarType varType = VarType.fromToken(token);
        if (varType == null) {
            throw reportError("Expecting primitive type or user type", token);
        }

        Set<ClassVarDec> classVariables = new LinkedHashSet<>();
        while (true) {
            token = tokenizer.advance();
            if (token instanceof Identifier varIdentifier) {
                ClassVarDec classVarDec =
                        new ClassVarDec(qualifier, new VarDec(varType, varIdentifier.value()));
                if (classVariables.contains(classVarDec)) {
                    throw reportError("Duplicate variable declaration: " + classVarDec, token);
                }
                classVariables.add(classVarDec);
                continue;
            }

            if (Symbol.COMMA.equals(token)) {
                continue;
            }

            if (Symbol.SEMICOLON.equals(token)) {
                break;
            }

            throw reportError("Unexpected token", token);
        }

        report("ClassVarDec %s %s %s%n", qualifier, varType, classVariables);

        return classVariables;
    }

    private void compileSubroutineDec()
    {
        precondition(subroutineType == null);
        Token token = tokenizer.advance();
        if (Keyword.CONSTRUCTOR.equals(token)) {
            subroutineType = SubroutineType.CONSTRUCTOR;
        } else if (Keyword.FUNCTION.equals(token)) {
            subroutineType = SubroutineType.FUNCTION;
        } else if (Keyword.METHOD.equals(token)) {
            subroutineType = SubroutineType.METHOD;
        }
        if (subroutineType == null) {
            throw reportError("Expected 'constructor', or 'field', or 'method'", token);
        }

        token = tokenizer.advance();
        VarType returnType = VarType.fromToken(token);
        // When no type found, it must be void instead
        if (returnType == null && !Keyword.VOID.equals(token)) {
            throw reportError("Expecting subroutine return type", token);
        }

        token = tokenizer.advance();
        if (!(token instanceof Identifier nameIdentifier)) {
            throw reportError("Expecting subroutine name", token);
        }
        subroutineName = nameIdentifier.value();
        report("Subroutine %s %s %s".formatted(subroutineType,
                                               returnType == null ? "void" : returnType.toString(),
                                               subroutineName));

        parameters = new LinkedHashMap<>();
        for (VarDec varDec : compileParameterList()) {
            parameters.put(varDec.name(), varDec.type());
        }

        compileSubroutineBody();

        parameters = null;
        localVars = null;
        subroutineName = null;
        subroutineType = null;
    }

    @NotNull
    private Set<VarDec> compileParameterList()
    {
        Token token = tokenizer.advance();
        if (!Symbol.LEFT_PAREN.equals(token)) {
            throw reportError("Expecting '(' after subroutine name", token);
        }
        Set<VarDec> vars = new LinkedHashSet<>();
        while (true) {
            token = tokenizer.advance();
            if (Symbol.COMMA.equals(token)) {
                if (vars.isEmpty()) {
                    throw reportError("Unexpected comma", token);
                }
                continue;
            }
            if (Symbol.RIGHT_PAREN.equals(token)) {
                break;
            }
            VarType type = VarType.fromToken(token);
            if (type == null) {
                throw reportError("Expecting parameter type", token);
            }
            token = tokenizer.advance();
            if (!(token instanceof Identifier identifier)) {
                throw reportError("Expecting parameter name", token);
            }
            VarDec varDec = new VarDec(type, identifier.value());
            if (vars.contains(varDec)) {
                throw reportError("Duplicate parameter declaration " + varDec, token);
            }
            vars.add(varDec);
        }

        System.out.printf("ParameterList %s%n", vars);
        return vars;
    }

    private void compileSubroutineBody()
    {
        precondition(localVars == null);

        Token token = tokenizer.advance();
        if (!Symbol.LEFT_BRACE.equals(token)) {
            throw reportError("Expecting opening brace", token);
        }

        localVars = new LinkedHashMap<>();
        while (true) {
            token = tokenizer.advance();
            if (Symbol.RIGHT_BRACE.equals(token)) {
                break;
            }

            if (Keyword.VAR.equals(token)) {
                for (VarDec varDec : compileVarDec(token)) {
                    if (localVars.containsKey(varDec.name())) {
                        throw reportError("Duplicate local variable " + varDec, null);
                    }
                    localVars.put(varDec.name(), varDec.type());

                }
                continue;
            }

            compileStatement(token);
        }

        localVars = null;
    }

    @NotNull
    private Set<VarDec> compileVarDec(Token token)
    {
        precondition(Keyword.VAR.equals(token));

        token = tokenizer.advance();
        VarType varType = VarType.fromToken(token);
        if (varType == null) {
            throw reportError("Expecting variable type", token);
        }

        Set<VarDec> vars = new LinkedHashSet<>();
        while (true) {
            token = tokenizer.advance();
            if (Symbol.SEMICOLON.equals(token)) {
                if (vars.isEmpty()) {
                    throw reportError("Expecting at least one variable declaration", token);
                }
                break;
            }

            if (Symbol.COMMA.equals(token)) {
                if (vars.isEmpty()) {
                    throw reportError("Expecting at least one variable declaration", token);
                }
                continue;
            }

            if (!(token instanceof Identifier identifier)) {
                throw reportError("Expecting variable name", token);
            }

            VarDec varDec = new VarDec(varType, identifier.value());
            if (vars.contains(varDec)) {
                throw reportError("Double local variable " + varDec, null);
            }
            vars.add(varDec);
        }

        report("VarDec %s %s%n", varType, vars);

        return vars;
    }

    private void compileStatement(Token token)
    {

        if (Keyword.LET.equals(token)) {
            compileLet(token);
        } else if (Keyword.IF.equals(token)) {
            compileIf(token);
        } else if (Keyword.WHILE.equals(token)) {
            compileWhile(token);
        } else if (Keyword.DO.equals(token)) {
            compileDo(token);
        } else if (Keyword.RETURN.equals(token)) {
            compileReturn(token);
        } else {
            throw reportError("Unexpected token", token);
        }
    }

    private void compileLet(Token token)
    {
        precondition(Keyword.LET.equals(token));
        token = tokenizer.advance();
        if (!(token instanceof Identifier varIdentifier)) {
            throw reportError("Expecting variable name", token);
        }
        String varName = varIdentifier.value();
        token = tokenizer.advance();
        boolean hasArrayIndex = false;
        if (Symbol.LEFT_BRACKET.equals(token)) {
            hasArrayIndex = true;
            // [expression]
            compileExpression();
            token = tokenizer.advance();
            if (!Symbol.RIGHT_BRACKET.equals(token)) {
                throw reportError("Expecting ']'", token);
            }
        }
        if (!Symbol.EQUAL.equals(token)) {
            throw reportError("Expecting '='", token);
        }
        compileExpression();

        token = tokenizer.advance();
        if (!Symbol.SEMICOLON.equals(token)) {
            throw reportError("Expecting ';'", token);
        }

        // TODO now assign to variable
    }

    private void compileIf(Token token)
    {
        precondition(Keyword.IF.equals(token));
    }

    private void compileWhile(Token token)
    {
        precondition(Keyword.WHILE.equals(token));
    }

    private void compileDo(Token token)
    {
        precondition(Keyword.DO.equals(token));
    }

    private void compileReturn(Token token)
    {
        precondition(Keyword.RETURN.equals(token));
    }

    private void compileExpression()
    {

    }

    private void report(@NotNull String message, Object... params)
    {
        System.out.printf(message, params);
    }

    private RuntimeException reportError(@NotNull String message,
                                         @Nullable Token token)
    {
        return new IllegalArgumentException("%d - %s: %s".formatted(tokenizer.lineNumber(), message, token));
    }
}
