package turing.loader

import java.io.File

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

import sys.process._

object App {

  val sparkConf = new SparkConf().setAppName("Practice").setMaster("local[*]")
  sparkConf.set("spark.hadoop.validateOutputSpecs", "false") // to override output
  val sparkContext = new SparkContext(sparkConf)

  def main(args: Array[String]): Unit = {

    // clone each repo
    //https://raw.githubusercontent.com/monikturing/turing-data-challenge/master/url_list.csv

    /**
      * 1st. clone each repos
      */
    var repoUrl = "https://github.com/kevinburke/hamms"
    var repoName = repoUrl.substring(repoUrl.lastIndexOf("/"))
    var cloningPath: String = "output/repos"
    var pyProjectPath = cloningPath + repoName + "/*"
    cloneRepo(repoUrl)


    /**
      * 1. Number of lines of code [this excludes comments, whitespaces, blank lines].
      * 2. List of external libraries/packages used.
      */
    var pyRdd: RDD[String] = sparkContext.textFile(pyProjectPath)
    pyRdd.filter(x => !x.trim.startsWith("#") || !x.trim.isEmpty)

    println("No. of python lines in repo. = " + pyRdd.count())

    /**
      * 1.
      */


    println("Process Completed!!!")
  }

  def cloneRepo(url: String, cloningPath: String = "output/repos/"): Unit = {

    val directory = new File(cloningPath)
    if (!directory.exists) {
      directory.mkdir
    }

    val repoName = url.substring(url.lastIndexOf("/"))

    println(repoName)

    s"git clone $url $cloningPath$repoName --branch master" !

    println("Repo \"" + repoName + "\" cloned.")

    Utils.retainPyFilesOnly(cloningPath + repoName)
  }

}
