package turing.loader

import java.io.{File, FileNotFoundException}
import java.net.URL
import java.util.Properties

import org.antlr.v4.runtime.tree.ParseTreeWalker
import org.antlr.v4.runtime.{CharStreams, CommonTokenStream}
import org.apache.commons.io.FileUtils
import org.apache.hadoop.fs.Path
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.eclipse.jgit.api.errors.TransportException
import org.eclipse.jgit.api.Git
import parser.python3.{Python3Lexer, Python3Parser}
import turing.lib.{DuplicateFinder, PyCodeExplorer, PyRepoInfo}
import turing.utils.{HdfsUtils, StringUtils}
import javautils.PropertiesUtils

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

    new File(localPath).mkdirs()
    new URL(url) #> new File(localPathFile) !!

    HdfsUtils.copyPyFilesFromLocalToHdfs(localPathFile, hadoopPath, false)
    println(s"uber repo loaded in $hadoopPath")
  }

  def retainPyFilesOnly(path: java.io.File): Unit = {
    if (path.isDirectory)
      path.listFiles.foreach(retainPyFilesOnly)
    if ((path.exists && !path.getName.endsWith(".py")) || (path.isHidden || path.getName.startsWith("."))) {
      path.delete()
    }

    //because hadoop assumes underscored files should be ignored
    if (!path.isDirectory && path.getName.startsWith("_") && path.getName.endsWith(".py")) {
      path.renameTo(new java.io.File(path.getParent + "/i" + path.getName))
    }
  }

  def cloneRepoAndRetainPyFilesOnly(url: String): Unit = {
    val cloningPathLocal: String = pathProperty.getProperty("pyLocalPath")
    val urlSplit = url.split("/")
    val repoAuthor = urlSplit(urlSplit.length - 2)
    val repoName = urlSplit(urlSplit.length - 1)
    val cloneDirectory = new java.io.File(cloningPathLocal + repoAuthor + "/" + repoName)
    val srcPath = cloningPathLocal + repoAuthor + "/" + repoName
    val destPath = HdfsUtils.rootPath + "/" + pathProperty.getProperty("pyStage0Path") + repoAuthor + "/" + repoName

    FileUtils.deleteDirectory(cloneDirectory)

    //s"git clone $url $cloningPathLocal$repoAuthor/$repoName --branch master --single-branch" !
    try {
      Git.cloneRepository().setURI(url)
        .setBranch("master")
        .setDirectory(cloneDirectory)
        .call()
    } catch {
      case e1: TransportException => {
        println(s"Repo $url not available !!!")
        println("TransportException ma chiryo. It means repo is not publicly available.")
        new File(s"$cloningPathLocal$repoAuthor/$repoName").mkdirs()
        new File(s"$cloningPathLocal$repoAuthor/$repoName/dummy.py").createNewFile()
        // HdfsUtils.hdfs.create(new Path(destPath + "/" + "dummy.py"), true)
      }
    }
    retainPyFilesOnly(new java.io.File(cloningPathLocal + repoAuthor + "/" + repoName))

    try {
      HdfsUtils.copyPyFilesFromLocalToHdfs(srcPath, destPath, false)
    } catch {
      case e1: FileNotFoundException => {
        println("Unable to clone from " + url)
        println("1st FileNotFound ma chiryo. Reason may be master branch is empty or not available. so cloning again with default branch")
        Git.cloneRepository().setURI(url)
          .setDirectory(cloneDirectory)
          .call()
        //  HdfsUtils.hdfs.create(new Path(destPath + "/" + "empty_file.py"), true)
        retainPyFilesOnly(new java.io.File(cloningPathLocal + repoAuthor + "/" + repoName))
        try {
          HdfsUtils.copyPyFilesFromLocalToHdfs(srcPath, destPath, false)
        } catch {
          case e1: FileNotFoundException => {
            println("2nd FileNotFound ma chiryo. Reason may be still default branch is empty.")
            HdfsUtils.hdfs.create(new Path(destPath + "/" + "dummy.py"), true)
          }
        }
      }
    }
    println(s"git clone $repoAuthor/$repoName successful and only .py files retained.")
    println(s"Files copied from $srcPath to $destPath")
  }

  def extractInfos(sparkContext: SparkContext, repoUrl: String): PyRepoInfo = {
    val urlSplit = repoUrl.split("/")
    val repoAuthor = urlSplit(urlSplit.length - 2)
    val repoName = urlSplit(urlSplit.length - 1)
    val pyStage0RepoPath = HdfsUtils.rootPath + "/" + ProcessJob.pathProperty.getProperty("pyStage0Path") + repoAuthor + "/" + repoName

    val filteredPyLines: RDD[String] = sparkContext.textFile(pyStage0RepoPath + "/*").filter(x => !(x.trim.isEmpty || x.trim.startsWith("#")))
    val pyLinesCount = filteredPyLines.count() //python lines per repo

    val pyCodeExplorer = new PyCodeExplorer(sparkContext)
    val duplicateFinder = new DuplicateFinder(sparkContext)

    val pyRepoRdd: RDD[(String, String)] = sparkContext.wholeTextFiles(pyStage0RepoPath + "/*")
    val filesCount = pyRepoRdd.count() //python fiiles count per repo

    pyRepoRdd.foreach(x => {
      //to get function, function params, imports, variables
      val lexer = new Python3Lexer(CharStreams.fromString(x._2))
      val parser = new Python3Parser(new CommonTokenStream(lexer))
      ParseTreeWalker.DEFAULT.walk(pyCodeExplorer, parser.file_input())

      //to get duplicate code count
      duplicateFinder.duplicateAnalysisPerFile(x._2)

    })

    println("For loop into parenthesis = " + pyCodeExplorer.getForLoopParenthesisStr)

    val nesting_factor = "%.6f".format(StringUtils.getNestingFactor(pyCodeExplorer.getForLoopParenthesisStr)).toDouble
    val code_duplication = "%.6f".format(1.0 * duplicateFinder.getConsecutive4LineDuplicateCount / filesCount).toDouble
    val average_parameters = "%.6f".format(1.0 * pyCodeExplorer.getFunctionParamsCount / pyCodeExplorer.getFunctionsCount).toDouble
    val average_variables = "%.6f".format(1.0 * pyCodeExplorer.getVariableCount / pyLinesCount).toDouble

    new PyRepoInfo(
      repoUrl,
      pyLinesCount,
      pyCodeExplorer.getImportsArray,
      if (nesting_factor.isNaN) 0.0 else nesting_factor,
      if (code_duplication.isNaN) 0.0 else code_duplication,
      if (average_parameters.isNaN) 0.0 else average_parameters,
      if (average_variables.isNaN) 0.0 else average_variables
    )
  }

}
