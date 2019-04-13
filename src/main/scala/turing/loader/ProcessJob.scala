package turing.loader


import java.io.File
import java.net.URL
import java.util.Properties
import java.util.concurrent.atomic.LongAccumulator

import org.antlr.v4.runtime.tree.ParseTreeWalker
import org.antlr.v4.runtime.{CharStreams, CommonTokenStream}
import org.apache.commons.io.FileUtils
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.util.LongAccumulator
import parser.python3.{Python3BaseListener, Python3Lexer, Python3Parser}
import turing.utils.{HdfsUtils, PropertiesUtils}

import sys.process._

/**
  * Created by kneupane on 4/10/19.
  */
object ProcessJob {

  var pathProperty: Properties = PropertiesUtils.loadProperties("paths/in_out_paths.jobcfg")

  def uberRepoLoader(): Unit = {
    val url = pathProperty.getProperty("uberRepoUrl")
    val fileName = url.substring(url.lastIndexOf("/") + 1)
    val localPath = pathProperty.getProperty("uberRepoLocalPath")
    val localPathFile = localPath + fileName
    val hadoopPath = HdfsUtils.rootPath + "/" + pathProperty.getProperty("uberRepoRawPath") + fileName

    println("local path " + localPath)
    println("fileName " + fileName)
    println(s"Loading uber repo from $url")

    Process(s"mkdir -p $localPath").!
    new URL(url) #> new File(localPathFile) !!

    HdfsUtils.copyPyFilesFromLocalToHdfs(localPathFile, hadoopPath, false)
    println(s"uber repo loaded in $hadoopPath")
  }

  private def retainPyFilesOnly(path: java.io.File): Unit = {
    if (path.isDirectory)
      path.listFiles.foreach(retainPyFilesOnly)
    if (path.exists && !path.getName.endsWith(".py") || path.isHidden)
      path.delete()
  }

  def cloneRepoAndRetainPyFilesOnly(url: String): Unit = {
    val cloningPathLocal: String = pathProperty.getProperty("pyLocalPath")
    val repoName = url.substring(url.lastIndexOf("/") + 1)
    val cloneDirectory = new java.io.File(cloningPathLocal + "/" + repoName)

    FileUtils.deleteDirectory(cloneDirectory)

    s"git clone $url $cloningPathLocal$repoName --branch master --single-branch" !

    retainPyFilesOnly(new java.io.File(cloningPathLocal + "/" + repoName))
    println(s"git clone $repoName successful and only .py files retained.")

    val srcPath = cloningPathLocal + repoName
    val destPath = HdfsUtils.rootPath + "/" + pathProperty.getProperty("pyStage0Path") + "/" + repoName
    HdfsUtils.copyPyFilesFromLocalToHdfs(srcPath, destPath, false)

    println(s"Files copied from $srcPath to $destPath")
  }

  def listOutPyImportsVarsFuncsPerRepo(pyRepoRdd: RDD[(String, String)]): Unit = {
    println("list garnema chiryo :D")

    // var imports: Set[String] = Set()
    // var functionsCounter: Long = 0l
    // var functionParamCounter: Long = 0l

    var variableCounter = 0l

    pyRepoRdd.foreach(x => {
      var lexer = new Python3Lexer(CharStreams.fromString(x._2))
      var parser = new Python3Parser(new CommonTokenStream(lexer))



      ParseTreeWalker.DEFAULT.walk(new Python3BaseListener() {

        override def enterExpr_stmt(ctx: Python3Parser.Expr_stmtContext): Unit = {
          var variable = ctx.testlist_star_expr().get(0).getText()
          if (!variable.trim.startsWith("print")) {
            //increase counter
          }
        }
      }, parser.file_input())
    })
  }

}
