package turing.test

import java.io.{File, IOException}
import java.nio.charset.Charset
import java.nio.file.Files

import org.antlr.v4.runtime.tree.ParseTreeWalker
import org.antlr.v4.runtime.{CharStreams, CommonTokenStream}
import parser.python3.{Python3BaseListener, Python3Lexer, Python3Parser}

import scala.collection.mutable.ListBuffer

/**
  * Created by kneupane on 4/18/19.
  */
object TestPySc {

  @throws[IOException]
  private def readFile(file: File, encoding: Charset) = {
    val encoded = Files.readAllBytes(file.toPath)
    new String(encoded, encoding)
  }

  val fileName = "/home/kneupane/work/projects/practice/turing_git_analysis/src/main/scala/turing/test/simple.py"
  var parens = ""

  var parenthesisAccumulator: ListBuffer[String] = ListBuffer()
  var nestCount = 0
  var LoopsCount = 0

  def main(args: Array[String]): Unit = {
    val source = readFile(new File(fileName), Charset.forName("UTF-8"))

    val lexer = new Python3Lexer(CharStreams.fromString(source))
    val parser = new Python3Parser(new CommonTokenStream(lexer))


    ParseTreeWalker.DEFAULT.walk(new Python3BaseListener() {
      override def enterCompound_stmt(ctx: Python3Parser.Compound_stmtContext): Unit = {
        try {
          val startLine = ctx.for_stmt.getStart.getLine
          //val startIndex = ctx.for_stmt.getStart.getCharPositionInLine
          //val endLine = ctx.for_stmt.getStop.getLine
          parenthesisAccumulator += "("
        } catch {
          case e1: NullPointerException => ()
        }
      }

      override def exitCompound_stmt(ctx: Python3Parser.Compound_stmtContext): Unit = {
        try {
          val endLine = ctx.for_stmt.getStop.getLine
          parenthesisAccumulator += ")"
        } catch {
          case e1: NullPointerException => ()
        }
      }
    }, parser.file_input)

    println("loop to parenthesis = " + parenthesisAccumulator.mkString(""))
    println("loop depth = " + TestPy.maxDepth(parenthesisAccumulator.mkString("")))
    println("loop index  = " + getCommaSeparatedParenthesis(parenthesisAccumulator.mkString("")))
    println("loop index  = " + nestingFactor(getCommaSeparatedParenthesis(parenthesisAccumulator.mkString(""))))

  }

  def getCommaSeparatedParenthesis(parStr: String): String = {
    var parStrTemp = parStr
    var finalStr = ""
    var firstParenthesisLastIndex = TestPy.lastParenthesis(parStr, 0) + 1
    finalStr += parStrTemp.substring(0, firstParenthesisLastIndex)
    parStrTemp = parStrTemp.substring(firstParenthesisLastIndex)
    // println("final str = " + finalStr)
    // println("next chunk = " + parStrTemp)
    while (parStrTemp.length > 0) {
      firstParenthesisLastIndex = TestPy.lastParenthesis(parStrTemp, 0) + 1
      finalStr += "," + parStrTemp.substring(0, firstParenthesisLastIndex)
      parStrTemp = parStrTemp.substring(firstParenthesisLastIndex)
    }
    finalStr
  }

  def nestingFactor(str: String): Double = {
    var nestValue = 0
    var chunks: Array[String] = str.split(",")
    for (i <- 0 until chunks.length) {
      nestValue += TestPy.maxDepth(chunks(i))
    }
    1.0 * nestValue / chunks.length
  }

}
