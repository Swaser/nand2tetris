package ch.chassaing.jack.token;

public sealed interface Token
        permits Identifier, IntegerConstant, Keyword, StringConstant, Symbol
{
}
