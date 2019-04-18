package turing.test;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import parser.python3.Python3BaseListener;
import parser.python3.Python3Lexer;
import parser.python3.Python3Parser;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;

public class TestPy {

    private static String readFile(File file, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(file.toPath());
        return new String(encoded, encoding);
    }

    static String fileName = "/home/kneupane/work/projects/practice/turing_git_analysis/src/main/scala/turing/test/simple.py";


    static String parens = "";

    public static void main(String[] args) throws IOException {
        //   System.out.println("ded de ded ed");
        String source = readFile(new File(fileName), Charset.forName("UTF-8"));

        Python3Lexer lexer = new Python3Lexer(CharStreams.fromString(source));
        Python3Parser parser = new Python3Parser(new CommonTokenStream(lexer));

        Stack<String> forStack = new Stack<>();
        ArrayList<String> list = new ArrayList<>();

        ParseTreeWalker.DEFAULT.walk(new Python3BaseListener() {

            /**
             * get function name from python file
             * get function parameter from python file () filter out ()
             * @param ctx
             */
            @Override
            public void enterFuncdef(Python3Parser.FuncdefContext ctx) {
                // System.out.printf("Function Name = %s\n", ctx.NAME().getText());
                // System.out.printf("Function Parameter = %s\n", ctx.parameters().getText());
            }

            /**
             * variable names list
             * need to remove print from variable
             * @param ctx
             */
            @Override
            public void enterExpr_stmt(Python3Parser.Expr_stmtContext ctx) {
                String variable = ctx.testlist_star_expr().get(0).getText();
                //System.out.println("variable: " + ctx.testlist_star_expr().get(0).getText());
                if (!variable.endsWith(")")) {
                }
                // System.out.println(variable);
            }

            //todo from here (imports)


            @Override
            public void enterImport_stmt(Python3Parser.Import_stmtContext ctx) {
                try {
                    //   System.out.println("import = " + ctx.import_name().dotted_as_names().dotted_as_name().get(0).dotted_name().NAME().get(0).getText());
                    //  // System.out.println("import = " + ctx.import_from().dotted_name().getText()); //flask aayo
                    //  // System.out.println("import from = " + ctx.import_from().getText());
                } catch (NullPointerException e) {

                }
            }

            /**
             * from flask import request => flask
             * @param ctx
             */
            @Override
            public void enterImport_from(Python3Parser.Import_fromContext ctx) {
                //   System.out.println("import from = "+ ctx.dotted_name().getText());
            }


            //for loop1
            @Override
            public void enterCompound_stmt(Python3Parser.Compound_stmtContext ctx) {
                try {
                    //   System.out.println("start line = " + ctx.for_stmt().getStart().getLine());
                    //  System.out.println("start index = " + ctx.for_stmt().getStart().getCharPositionInLine());

                    //  System.out.println("end line = " + ctx.for_stmt().getStop().getLine());

                    int startLine = ctx.for_stmt().getStart().getLine();
                    int startIndex = ctx.for_stmt().getStart().getCharPositionInLine();
                    int endLine = ctx.for_stmt().getStop().getLine();

                    //System.out.printf("(startLine, endLine, startIndex) = (%d, %d, %d)\n", startLine, endLine, startIndex);
                    System.out.println(/* ctx.getStart().getText() + */"(");
                    System.out.println(endLine + ")");
                    //System.out.println(ctx.getStop().getText());
                    //   System.out.printf("(endLine = %d)\n", endLine);

                    list.add("(");
                    list.add(endLine + ")");
                } catch (Exception e) {

                }
            }

        }, parser.file_input());

        System.out.println("loop counter = " + maxDepth("(1(34)(5(78)8)8)(1115)"));
    }

    static int maxDepth(String S) {
        int current_max = 0; // current count
        int max = 0; // overall maximum count
        int n = S.length();

        // Traverse the input string
        for (int i = 0; i < n; i++) {
            if (S.charAt(i) == '(') {
                current_max++;

                // update max if required
                if (current_max > max) {
                    max = current_max;
                }
            } else if (S.charAt(i) == ')') {
                if (current_max > 0) {
                    current_max--;
                } else {
                    return -1;
                }
            }
        }

        // finally check for unbalanced string
        if (current_max != 0) {
            return -1;
        }

        return max;
    }

    static int lastParenthesis(String expression, int index) {
        int i;

        // If index given is invalid and is
        // not an opening bracket.
        if (expression.charAt(index) != '(') {
            System.out.print(expression + ", "
                    + index + ": -1\n");
            return index;
        }

        // Stack to store opening brackets.
        Stack<Integer> st = new Stack<>();

        // Traverse through string starting from
        // given index.
        for (i = index; i < expression.length(); i++) {

            // If current character is an
            // opening bracket push it in stack.
            if (expression.charAt(i) == '(') {
                st.push((int) expression.charAt(i));
            } // If current character is a closing
            // bracket, pop from stack. If stack
            // is empty, then this closing
            // bracket is required bracket.
            else if (expression.charAt(i) == ')') {
                st.pop();
                if (st.empty()) {
                    System.out.print(expression + ", "
                            + index + ": " + i + "\n");
                    return i;
                }
            }
        }

        // If no matching closing bracket
        // is found.
        System.out.print(expression + ", "
                + index + ": -1\n");

        return -1;

    }
}
