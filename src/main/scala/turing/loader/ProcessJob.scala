package turing.loader

import java.io.File
import java.net.URL
import java.util.Properties

import com.google.gson.Gson
import org.antlr.v4.runtime.tree.ParseTreeWalker
import org.antlr.v4.runtime.{CharStreams, CommonTokenStream}
import org.apache.commons.io.FileUtils
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.eclipse.jgit.api.errors.TransportException
import org.eclipse.jgit.api.{CloneCommand, Git}
import parser.python3.{Python3Lexer, Python3Parser}
import turing.lib.{DuplicateFinder, PyCodeExplorer, PyRepoInfo}
import turing.utils.{HdfsUtils, LocalfsUtils, StringUtils}
import javautils.PropertiesUtils

import sys.process._

/**
  * Created by kneupane on 4/10/19.
  */
object ProcessJob {

  var pathProperty: Properties = PropertiesUtils.loadProperties("paths/in_out_paths.jobcfg")

  /**
    * This uberRepoLoader function is used to load the file:
    * https://raw.githubusercontent.com/monikturing/turing-data-challenge/master/url_list.csv
    * to HDFS.
    */
  def uberRepoLoader(): Unit = {
    val url = pathProperty.getProperty("uberRepoUrl")
    val fileName = url.substring(url.lastIndexOf("/") + 1)
    val localPath = pathProperty.getProperty("uberRepoLocalPath")
    val localPathFile = localPath + fileName
    val hadoopPath = HdfsUtils.rootPath + "/" + pathProperty.getProperty("uberRepoRawPath") + fileName

    if (!new File(localPathFile).exists()) {
      println(s"$fileName doesn't exist on $localPath. So, downloading from $url")
      new File(localPath).mkdirs()
      new URL(url) #> new File(localPathFile) !!

    }

    HdfsUtils.copyPyFilesFromLocalToHdfs(localPathFile, hadoopPath, false)
    println(s"uber repo loaded in hadoop path: $hadoopPath")
  }


  /**
    * The urls list fetched from uberRepoLoader method is then passed to this function one by one.
    * This function will then clones the repo, checks python files and load then to HDFS.
    *
    * @param url
    */
  def cloneRepoAndRetainPyFilesOnly(url: String): Unit = {
    val cloningPathLocal: String = pathProperty.getProperty("pyLocalPath")
    val urlSplit = url.split("/")
    val repoAuthor = urlSplit(urlSplit.length - 2)
    val repoName = urlSplit(urlSplit.length - 1)
    val srcPath = cloningPathLocal + repoAuthor + "/" + repoName
    val cloneDirectory = new java.io.File(srcPath)
    val destPath = HdfsUtils.rootPath + "/" + pathProperty.getProperty("pyStage0Path") + repoAuthor + "/" + repoName

    FileUtils.deleteDirectory(cloneDirectory)

    val cloneCommandMaster: CloneCommand = Git.cloneRepository.setURI(url).setDirectory(cloneDirectory).setBranch("master")
    val cloneCommandDefault: CloneCommand = Git.cloneRepository.setURI(url).setDirectory(cloneDirectory)
    try {
      println(s"loading files from $url")
      cloneCommandMaster.call().getRepository.close
      if (!LocalfsUtils.retainPyFilesOnly(srcPath)) {
        println(s"Loading $url with py files in it is unsuccesfull !!! Retrying with default branch.")
        cloneCommandDefault.call().getRepository.close
        if (!LocalfsUtils.retainPyFilesOnly(srcPath)) {
          println("Seems to be a repo without py files in it !!!")
          FileUtils.deleteDirectory(cloneDirectory)
          cloneDirectory.mkdirs()
          new File(srcPath + "/dummy.py").createNewFile()
        }
      }
    } catch {
      case e: TransportException => {
        println("Loading files unsuccesfull !!!\nSeems like repo doesn't exists or it is a private repo.")
        FileUtils.deleteDirectory(cloneDirectory)
        cloneDirectory.mkdirs()
        new File(srcPath + "/dummy.py").createNewFile()
      }
    }
    HdfsUtils.copyPyFilesFromLocalToHdfs(srcPath, destPath, false)
  }

  /**
    * The major tasks from 1 to 6 mentioned on challenge is performed here.
    *
    * @param sparkContext
    * @param repoUrl
    * @return
    */
  def extractInfos(sparkContext: SparkContext, repoUrl: String): PyRepoInfo = {
    val urlSplit = repoUrl.split("/")
    val repoAuthor = urlSplit(urlSplit.length - 2)
    val repoName = urlSplit(urlSplit.length - 1)
    val pyStage0RepoPath = HdfsUtils.rootPath + "/" + ProcessJob.pathProperty.getProperty("pyStage0Path") + repoAuthor + "/" + repoName


    /**
      * Task 1 is performed here.
      */
    val filteredPyLines: RDD[String] = sparkContext.textFile(pyStage0RepoPath + "/*").filter(x => !(x.trim.isEmpty || x.trim.startsWith("#")))
    val pyLinesCount = filteredPyLines.count() //python lines per repo

    val pyCodeExplorer = new PyCodeExplorer(sparkContext)
    val duplicateFinder = new DuplicateFinder(sparkContext)

    val pyRepoRdd: RDD[(String, String)] = sparkContext.wholeTextFiles(pyStage0RepoPath + "/*")
    val filesCount = pyRepoRdd.count() //python files count per repo


    /**
      * 2nd, 3rd, 4th, 5th and 6th tasks are performed here.
      * For 2nd, 3rd, 5th and 6rh tasks,
      * the python.g4 grammar provided by antlr is used to generate parser and tasks
      * to find functions, parameters, variables, loops start and end are performed here.
      */
    pyRepoRdd.foreach(x => {
      val lexer = new Python3Lexer(CharStreams.fromString(x._2))
      val parser = new Python3Parser(new CommonTokenStream(lexer))
      ParseTreeWalker.DEFAULT.walk(pyCodeExplorer, parser.file_input())

      /**
        * 4th task is performed here.
        * Here, the consecutive four lines are taken as a chunk and recursively compared with each other
        */
      duplicateFinder.duplicateAnalysisPerFile(x._2)

    })

    val nesting_factor = "%.6f".format(StringUtils.getNestingFactor(pyCodeExplorer.getForLoopParenthesisStr)).toDouble
    val code_duplication = "%.6f".format(1.0 * duplicateFinder.getConsecutive4LineDuplicateCount / filesCount).toDouble
    val average_parameters = "%.6f".format(1.0 * pyCodeExplorer.getFunctionParamsCount / pyCodeExplorer.getFunctionsCount).toDouble
    val average_variables = "%.6f".format(1.0 * pyCodeExplorer.getVariableCount / pyLinesCount).toDouble


    /**
      * The final outcome of a single repo is then passed to case class to generate a json data.
      */
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

  def parsePyRepoInfoJsonStr(pyRepoInfoJsonStr: String): PyRepoInfo = {
    (new Gson().fromJson(pyRepoInfoJsonStr, classOf[PyRepoInfo]))
  }

  def isPyRepoJsonStrEmpty(sparkContext: SparkContext, hdfsPath: String): Boolean = {
    val jsonStr: String = sparkContext.wholeTextFiles(hdfsPath).map(x => x._2).first()
    isPyRepoEmpty(parsePyRepoInfoJsonStr(jsonStr))
  }

  def isPyRepoEmpty(pyRepoInfo: PyRepoInfo): Boolean = {
    (
      pyRepoInfo.number_of_lines == 0
        && pyRepoInfo.nesting_factor == 0.0
        && pyRepoInfo.libraries.isEmpty
        && pyRepoInfo.code_duplication == 0.0
        && pyRepoInfo.average_parameters == 0.0
        && pyRepoInfo.average_variables == 0.0
      )
  }

}
