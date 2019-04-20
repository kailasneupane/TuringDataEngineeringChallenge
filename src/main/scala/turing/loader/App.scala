package turing.loader


import java.time.LocalTime

import com.google.gson.{Gson, GsonBuilder}
import org.apache.spark.{SparkConf, SparkContext}
import turing.lib.PyRepoInfo
import turing.loader.ProcessJob.pathProperty
import turing.utils.HdfsUtils

import scala.collection.mutable.ListBuffer

object App {

  val startTime = System.nanoTime()
  println("Process execution started at " + LocalTime.now())

  val sparkConf = new SparkConf().setAppName("Practice").setMaster("local[*]")
    .set("spark.hadoop.validateOutputSpecs", "false") // to override output
    .set("spark.hadoop.mapreduce.input.fileinputformat.input.dir.recursive", "true")

  val sparkContext = new SparkContext(sparkConf)

  def main(args: Array[String]): Unit = {

    val uberRepoUrl = pathProperty.getProperty("uberRepoRawPath")
    val uberRepoHadoopPath = HdfsUtils.rootPath + "/" + uberRepoUrl + "*"
    val gson = new GsonBuilder().setPrettyPrinting().create()
    val pyRepoInfoList: ListBuffer[PyRepoInfo] = ListBuffer()

    println(s"Loading uber repo from $uberRepoUrl.")
    ProcessJob.uberRepoLoader()

    sparkContext.textFile(uberRepoHadoopPath).collect().toList
      .filter(x => x.startsWith("https://")).foreach(repoUrl => {

      println("working on " + repoUrl)

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
      println(gson.toJson(pyRepoWholeInfo))
      println()

      /**
        * the repo's info is associated to case class is then added to listBuffer to hold the info.
        */
      pyRepoInfoList += pyRepoWholeInfo

    })

    sparkContext.stop()

    /**
      * the bufferList is then converted to a big json data
      */
    val outputStrJson: String = gson.toJson(pyRepoInfoList.toArray)

    val finalOutput = HdfsUtils.rootPath + "/" + pathProperty.getProperty("resultJsonFullPath")

    /**
      * The final json data is saved as textFile in hdfs path : stage1/repos_info/results.json
      */
    HdfsUtils.saveTextStrToHdfs(outputStrJson, finalOutput)

    println("Process execution completed at " + LocalTime.now())
    val timeTakenInSecond = 1.0 * (System.nanoTime() - startTime) / 1000000000
    printf("Total time taken: %.2f seconds.\n", timeTakenInSecond)
    println("\nPlease find results.json in hadoop path: \n" + finalOutput + "\n")

  }

}
