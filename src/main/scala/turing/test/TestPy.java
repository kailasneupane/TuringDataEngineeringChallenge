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

public class TestPy {

    private static String readFile(File file, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(file.toPath());
        return new String(encoded, encoding);
    }

    static String fileName = "/home/serrorist/IdeaProjects/turing_git_analysis/src/main/scala/turing/test/simple.py";

    public static void main(String[] args) throws IOException {
        System.out.println("ded de ded ed");
        String source = readFile(new File(fileName), Charset.forName("UTF-8"));

        Python3Lexer lexer = new Python3Lexer(CharStreams.fromString(source));
        Python3Parser parser = new Python3Parser(new CommonTokenStream(lexer));


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
                System.out.println("variable: " + ctx.testlist_star_expr().get(0).getText());
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

            /**
             * for statement
             * @param ctx
             */
            @Override
            public void enterFor_stmt(Python3Parser.For_stmtContext ctx) {
                // System.out.println(ctx.getStop().getStopIndex());
            }

            @Override
            public void enterComp_iter(Python3Parser.Comp_iterContext ctx) {
               // System.out.println("ii = " + ctx.comp_for().ASYNC().getText());
            }
        }, parser.file_input());

    }
}
