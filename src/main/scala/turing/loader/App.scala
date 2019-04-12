package turing.loader

import java.io.InputStream
import java.net.{HttpURLConnection, URL}

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import turing.utils.HdfsUtils

object App {

  val sparkConf = new SparkConf().setAppName("Practice").setMaster("local[*]")
  sparkConf.set("spark.hadoop.validateOutputSpecs", "false") // to override output
  sparkConf.set("spark.hadoop.mapreduce.input.fileinputformat.input.dir.recursive", "true")

  val sparkContext = new SparkContext(sparkConf)

  def main(args: Array[String]): Unit = {


    // clone each repo
    //https://raw.githubusercontent.com/monikturing/turing-data-challenge/master/url_list.csv
    Utils.uberRepoLoader()

    /**
      * 1st. clone each repos
      */
    var repoUrl = "https://github.com/kevinburke/hamms"
    var repoName = repoUrl.substring(repoUrl.lastIndexOf("/") + 1)
    var pyStage0Path = HdfsUtils.rootPath + "/" + Utils.pathProperty.getProperty("pyStage0Path") + repoName


    println(pyStage0Path)
    Utils.cloneRepoAndRetainPyFilesOnly(repoUrl)


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
    val pyRdd: RDD[String] = sparkContext.textFile(pyStage0Path + "/*").filter(x => !x.trim.startsWith("#") || !x.trim.isEmpty)


    /**
      * 1. Number of lines of code [this excludes comments, whitespaces, blank lines].
      */
    var pyLines = pyRdd.count()
    println("No. of python lines in repo. = " + pyLines)

    /**
      * 2. List of external libraries/packages used.
      */


    println("\n\nProcess Completed!!!")
  }

}
