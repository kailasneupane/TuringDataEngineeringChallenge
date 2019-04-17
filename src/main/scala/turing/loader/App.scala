package turing.loader


import java.time.LocalTime

import com.google.gson.{Gson, GsonBuilder}
import org.apache.spark.{SparkConf, SparkContext}
import turing.lib.PyRepoInfo
import turing.loader.ProcessJob.pathProperty
import turing.utils.HdfsUtils

import scala.collection.mutable.ListBuffer

object App {

  println("Process execution started at " + LocalTime.now())

  val sparkConf = new SparkConf().setAppName("Practice").setMaster("local[*]")
    .set("spark.hadoop.validateOutputSpecs", "false") // to override output
    .set("spark.hadoop.mapreduce.input.fileinputformat.input.dir.recursive", "true")

  val sparkContext = new SparkContext(sparkConf)

  def main(args: Array[String]): Unit = {

    val gson = new GsonBuilder().setPrettyPrinting().create()
    val reposList = sparkContext.textFile(HdfsUtils.rootPath + "/" + pathProperty.getProperty("uberRepoRawPath") + "*").collect().toList
    val pyRepoInfoList: ListBuffer[PyRepoInfo] = ListBuffer()


    ProcessJob.uberRepoLoader()

    reposList.filter(x => x.startsWith("https://")).foreach(repoUrl => {


      /**
        * 1st. clone each repos
        */
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

      val pyRepoWholeInfo = ProcessJob.extractInfos(sparkContext, repoUrl)

      println()
      println(new Gson().toJson(pyRepoWholeInfo))
      pyRepoInfoList += pyRepoWholeInfo

    })

    sparkContext.stop()

    //println(gson.toJson(pyRepoInfoList.toArray))
    val outputStrJson: String = gson.toJson(pyRepoInfoList.toArray)
    val finalOutput = HdfsUtils.rootPath + "/" + pathProperty.getProperty("resultJsonFullPath")
    HdfsUtils.saveTextStrToHdfs(outputStrJson, finalOutput)

    println("\nProcess Completed!!!\n")

    println("Process execution completed at " + LocalTime.now())
  }

}
