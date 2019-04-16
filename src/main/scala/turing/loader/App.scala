package turing.loader


import java.time.LocalTime

import com.google.gson.{Gson, GsonBuilder}
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import turing.lib.PyRepoInfo
import turing.utils.HdfsUtils

object App {

  val sparkConf = new SparkConf().setAppName("Practice").setMaster("local[*]")
  sparkConf.set("spark.hadoop.validateOutputSpecs", "false") // to override output
  sparkConf.set("spark.hadoop.mapreduce.input.fileinputformat.input.dir.recursive", "true")

  val sparkContext = new SparkContext(sparkConf)

  def main(args: Array[String]): Unit = {

    println("Process execution started at " + LocalTime.now())

    // clone each repo
    //https://raw.githubusercontent.com/monikturing/turing-data-challenge/master/url_list.csv
    ProcessJob.uberRepoLoader()

    /**
      * 1st. clone each repos
      */
    var repoUrl = "https://github.com/kevinburke/hamms"
    var urlSplit = repoUrl.split("/")
    var repoAuthor = urlSplit(urlSplit.length - 2)
    var repoName = urlSplit(urlSplit.length - 1)
    var pyStage0RepoPath = HdfsUtils.rootPath + "/" + ProcessJob.pathProperty.getProperty("pyStage0Path") + repoAuthor + "/" + repoName


    println("pyStage0RepoPath => " + pyStage0RepoPath)
    ProcessJob.cloneRepoAndRetainPyFilesOnly(repoUrl)


    /**
      * 1. Number of lines of code [this excludes comments, whitespaces, blank lines].
      *
      * 2. List of external libraries/packages used.
      *
      * 3. The Nesting factor for the repository: the Nesting factor is the average depth of a
      * nested for loop throughout the code. (entire repository)
      *
      * 4. Code duplication: What percentage of the code is duplicated per file.
      * If the same 4 consecutive lines of code (disregarding blank lines, comments,
      * etc. other non code items) appear in multiple places in a file, all the occurrences
      * except the first occurence are considered to be duplicates.
      *
      * 5. Average number of parameters per function definition in the repository.
      *
      * 6. Average Number of variables defined per line of code in the repository.
      */
    val pyRepoRdd: RDD[(String, String)] = sparkContext.wholeTextFiles(pyStage0RepoPath + "/*")
    pyRepoRdd.foreach(x => println(x._1))

    /**
      * 1. Number of lines of code [this excludes comments, whitespaces, blank lines].
      */
    val filteredPyLines: RDD[String] = sparkContext.textFile(pyStage0RepoPath + "/*").filter(x => !(x.trim.isEmpty || x.trim.startsWith("#")))
    val pyLinesCount = filteredPyLines.count()
    //println("No. of python lines in repo. = " + pyLinesCount)

    /**
      * 2. List of external libraries/packages used.
      * 4. Code duplication: What percentage of the code is duplicated per file.
      * If the same 4 consecutive lines of code (disregarding blank lines, comments,
      * etc. other non code items) appear in multiple places in a file, all the occurrences
      * 5. Average number of parameters per function definition in the repository.
      * 6. Average Number of variables defined per line of code in the repository.
      */
    val pyData = ProcessJob.listOutPyImportsVarsFuncsPerRepo(sparkContext, pyRepoRdd, repoName)
    var librariesList = pyData._1.getImportsArray
    var avgParamsPerFunctionDefinition: Double = 1.0 * pyData._1.getFunctionParamsCount / pyData._1.getFunctionsCount
    var avgVariables: Double = 1.0 * pyData._1.getVariableCount / pyLinesCount

    var pyRepoWholeInfo = new PyRepoInfo(
      repoUrl,
      pyLinesCount,
      librariesList,
      0.36,
      "%.6f".format(pyData._2).toDouble,
      "%.6f".format(avgParamsPerFunctionDefinition).toDouble,
      "%.6f".format(avgVariables).toDouble
    )

    println("\n\n")

    val gson = new GsonBuilder().setPrettyPrinting().create()
    println(gson.toJson(pyRepoWholeInfo))

    println("\n\nProcess Completed!!!\n")

    println("Process execution completed at " + LocalTime.now())
  }

}
