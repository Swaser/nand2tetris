package ch.chassaing.jack.calc;

import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;

public class Calculator
    extends CalcBaseListener
{
    private final Map<String, Integer> varMap = new HashMap<>();
    private final Stack<Integer> stack = new Stack<>();

    @Override
    public void enterNum(CalcParser.NumContext ctx)
    {
        stack.push(Integer.valueOf(ctx.INT().getText()));
    }

    @Override
    public void enterVar(CalcParser.VarContext ctx)
    {
        Integer val = Objects.requireNonNullElse(varMap.get(ctx.ID().getText()),0);
        stack.push(val);
    }

    @Override
    public void exitMulDiv(CalcParser.MulDivContext ctx)
    {
        int r = stack.pop();
        int l = stack.pop();
        if (ctx.op.getType() == CalcParser.MUL) {
            stack.push(l * r);
        } else {
            stack.push(l / r);
        }
    }

    @Override
    public void exitAddSub(CalcParser.AddSubContext ctx)
    {
        int r = stack.pop();
        int l = stack.pop();
        if (ctx.op.getType() == CalcParser.ADD) {
            stack.push(l + r);
        } else {
            stack.push(l - r);
        }
    }

    @Override
    public void exitPrint(CalcParser.PrintContext ctx)
    {
        System.out.println(stack.pop());
    }

    @Override
    public void exitAssign(CalcParser.AssignContext ctx)
    {
        String var = ctx.ID().getText();
        int value = stack.pop();
        varMap.put(var, value);
        System.out.printf("%s = %d%n", var, value);
    }
}
