package turing.test;

import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import parser.python3.Python3Parser;

import java.io.File;
import java.io.IOException;

public class AstPrinter {
    public void print(RuleContext ctx) {
        explore(ctx, 0);
    }

    private void explore(RuleContext ctx, int indentation) {
        String ruleName = Python3Parser.ruleNames[ctx.getRuleIndex()];
        for (int i = 0; i < indentation; i++) {
            System.out.print("  ");
        }
        System.out.println(ruleName + " === " + ctx.getText());
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree element = ctx.getChild(i);
            if (element instanceof RuleContext) {
                explore((RuleContext) element, indentation + 1);
            }
        }
    }

    public static void main(String[] args) throws IOException {
        ParserFacade parserFacade = new ParserFacade();
        AstPrinter astPrinter = new AstPrinter();
        astPrinter.print(parserFacade.parse(new File("/home/kneupane/work/projects/practice/turing_git_analysis/src/main/scala/turing/test/simple.py")));


    }

}
