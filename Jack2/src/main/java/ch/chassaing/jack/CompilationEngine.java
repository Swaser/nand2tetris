package ch.chassaing.jack;

import ch.chassaing.jack.parse.*;
import ch.chassaing.jack.token.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static ch.chassaing.jack.token.KeywordType.*;
import static java.util.stream.Collectors.joining;

public final class CompilationEngine
{
    public static final EnumSet<KeywordType> KW_VARIABLE_QUALIFIERS = EnumSet.of(STATIC, FIELD);
    public static final EnumSet<KeywordType> KW_PRIMITIVE_TYPES = EnumSet.of(INT, CHAR, BOOLEAN);
    private final Tokenizer tokenizer;

    private String className;
    private String subroutineName;
    private Map<String, ClassVarDec> classVars;
    private Map<String, VarDec> parameters;
    private Map<String, VarDec> localVars;

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

        classVars = new LinkedHashMap<>();
        report("class " + className);

        while ((token = tokenizer.peek()) != null) {

            if (token instanceof Keyword keyword) {

                if (EnumSet.of(STATIC, FIELD).contains(keyword.type())) {
                    Set<ClassVarDec> newClassVariables = compileClassVarDec();
                    for (ClassVarDec classVar : newClassVariables) {
                        if (classVars.containsKey(classVar.name())) {
                            throw reportError("Duplicate class variable declaration " + classVar, null);
                        }
                        classVars.put(classVar.name(), classVar);
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
        classVars = null;
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

        String varNames = classVariables
                .stream()
                .map(it -> it.varDec().name()).collect(joining(", "));

        report("ClassVarDec %s %s %s".formatted(qualifier, varType, varNames));

        return classVariables;
    }

    private void compileSubroutineDec()
    {
        Token token = tokenizer.advance();
        SubroutineType subroutineType = null;
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
            parameters.put(varDec.name(), varDec);
        }

        // todo compile body

        if (returnType == null) {
            // TODO push 0 onto stack
        }

        parameters = null;
        localVars = null;
        subroutineName = null;
    }

    @NotNull
    private Set<VarDec> compileParameterList()
    {
        Token token = tokenizer.advance();
        if (!Symbol.LEFT_PAREN.equals(token)) {
            throw reportError("Expecting '(' after subroutine name", token);
        }
        Set<VarDec> varDecs = new LinkedHashSet<>();
        while (true) {
            token = tokenizer.advance();
            if (Symbol.COMMA.equals(token)) {
                if (varDecs.isEmpty()) {
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
            if (varDecs.contains(varDec)) {
                throw reportError("Duplicate parameter declaration " + varDec, token);
            }
            varDecs.add(varDec);
        }

        System.out.printf("ParameterList %s%n", varDecs);
        return varDecs;
    }

    private void report(@NotNull String msg)
    {
        System.out.println(msg);
    }

    private RuntimeException reportError(@NotNull String message,
                                         @Nullable Token token)
    {
        return new IllegalArgumentException("%d - %s: %s".formatted(tokenizer.lineNumber(), message, token));
    }
}
