package turing.lib

import org.apache.spark.SparkContext
import parser.python3.{Python3BaseListener, Python3Parser}

class PyCodeExplorer(sparkContext: SparkContext, repoName: String) extends Python3BaseListener with Serializable {

  private var variableAccumulator = sparkContext.longAccumulator("variables accumulator")
  private var functionAccumulator = sparkContext.longAccumulator("functions accumulator")
  private var functionParamsAccumulator = sparkContext.longAccumulator("functions parameters accumulator")
  private var importsAccumulator = new SetAccumulator[String]()
  sparkContext.register(importsAccumulator, "imports accumulator")

  def getVariableCount = variableAccumulator.value

  def getImportsArray = importsAccumulator.value.toArray

  def getFunctionsCount = functionAccumulator.value

  def getFunctionParamsCount = functionParamsAccumulator.value

  //variable counter
  override def enterExpr_stmt(ctx: Python3Parser.Expr_stmtContext): Unit = {
    var variable = ctx.testlist_star_expr().get(0).getText()
    if (!variable.trim.endsWith(")") || !variable.trim.startsWith("print") || !variable.trim.startsWith("\"")) {
      println("ledol var = " + variable)
      variableAccumulator.add(1)
    }
  }

  //import counter1
  override def enterImport_stmt(ctx: Python3Parser.Import_stmtContext): Unit = {
    try {
      var importName = ctx.import_name().dotted_as_names().dotted_as_name().get(0).dotted_name().NAME().get(0).getText()
      if (!importName.equals(repoName))
        importsAccumulator.add(importName)
    } catch {
      case e: NullPointerException => ()
    }
  }

  //import counter2
  override def enterImport_from(ctx: Python3Parser.Import_fromContext): Unit = {
    var importName = ctx.dotted_name().getText()
    if (!importName.equals(repoName))
      importsAccumulator.add(importName)
  }

  //functions counter
  override def enterFuncdef(ctx: Python3Parser.FuncdefContext): Unit = {
    var functionName = ctx.NAME().getText()
    var functionParamName = ctx.parameters().getText()
    if (!functionParamName.equals("()")) {
      var params = functionParamName.split(",")
      functionParamsAccumulator.add(params.length)
    }
    functionAccumulator.add(1)
  }
}
