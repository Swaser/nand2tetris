package ch.chassaing.jack;

import ch.chassaing.jack.parse.VarType;
import ch.chassaing.jack.token.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;

import static ch.chassaing.jack.token.KeywordType.*;

public final class CompilationEngine
{
    public static final EnumSet<KeywordType> KW_VARIABLE_QUALIFIERS = EnumSet.of(STATIC, FIELD);
    public static final EnumSet<KeywordType> KW_PRIMITIVE_TYPES = EnumSet.of(INT, CHAR, BOOLEAN);
    private final Tokenizer tokenizer;

    /**
     * Holds the name of the current class, or null if not currently in class
     */
    private String className;

    /**
     * Holds the name of the current subroutine, or null if not in subroutine
     */
    private String subroutineName;

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

        report("class " + className);

        while ((token = tokenizer.peek()) != null) {

            if (token instanceof Keyword keyword) {

                if (EnumSet.of(STATIC, FIELD).contains(keyword.type())) {
                    compileClassVarDec();
                    continue;
                }

                if (EnumSet.of(CONSTRUCTOR, FUNCTION, METHOD).contains(keyword.type())) {
                    compileSubroutineDec();
                    continue;
                }
            }

            if (Symbol.RIGHT_BRACE.equals(token)) {
                break;
            }

            throw reportError("Unexpected token", token);
        }

        if (!Symbol.RIGHT_BRACE.equals((token = tokenizer.advance()))) {
            throw reportError("Expected closing brace '}'", token);
        }

        className = null;
        return true;
    }

    private void compileSubroutineDec()
    {
        Token token;
        while (!Symbol.RIGHT_BRACE.equals((token = tokenizer.advance()))) {

        }

        subroutineName = null;
    }

    private void compileClassVarDec()
    {
        Token token = tokenizer.advance();
        if (!(token instanceof Keyword qualifier) ||
            !KW_VARIABLE_QUALIFIERS.contains(qualifier.type())) {
            throw reportError("ClassVarDec must start with keyword 'static' or 'field'", token);
        }

        token = tokenizer.advance();
        VarType varType = null;
        if (token instanceof Keyword typeKeyword) {
            varType = switch (typeKeyword.type()) {
                case INT -> new VarType.Primitive(VarType.PrimitiveType.INT);
                case CHAR -> new VarType.Primitive(VarType.PrimitiveType.CHAR);
                case BOOLEAN -> new VarType.Primitive(VarType.PrimitiveType.BOOLEAN);
                default -> null;
            };
        }
        else if (token instanceof Identifier typeIdentifier) {
            varType = new VarType.UserType(typeIdentifier.value());
        }

        if (varType == null) {
            throw reportError("Expect either primitive type or user type", token);
        }

        Set<String> variableNames = new LinkedHashSet<>();
        while (true) {
            token = tokenizer.advance();
            if (token instanceof Identifier varIdentifier) {
                if (variableNames.contains(varIdentifier.value())) {
                    throw reportError("Duplicate variable declaration", token);
                }
                variableNames.add(varIdentifier.value());
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
