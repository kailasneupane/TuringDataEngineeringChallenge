package turing.loader


import java.time.LocalTime

import com.google.gson.GsonBuilder
import org.apache.hadoop.fs.Path
import org.apache.spark.{SparkConf, SparkContext}
import turing.loader.ProcessJob.pathProperty
import turing.utils.HdfsUtils


object App {

  sys.props.update("spark.ui.proxyBase", "")

  val startTime = System.nanoTime()
  println("Process execution started at " + LocalTime.now())

  val sparkConf = new SparkConf().setAppName("Practice").setMaster("local[*]")
    .set("spark.hadoop.validateOutputSpecs", "false") // to override output
    .set("spark.hadoop.mapreduce.input.fileinputformat.input.dir.recursive", "true")
    .set("spark.driver.allowMultipleContexts", "true")


  val sparkContext = new SparkContext(sparkConf)

  def main(args: Array[String]): Unit = {

    val uberRepoUrl = pathProperty.getProperty("uberRepoRawPath")
    val individualJsonPath = pathProperty.getProperty("individualJsonPath")
    val individualEmptyJsonPath = pathProperty.getProperty("individualEmptyJsonPath")
    val uberRepoHadoopPath = HdfsUtils.rootPath + "/" + uberRepoUrl + "*"

    println(s"Loading uber repo from $uberRepoUrl.")
    ProcessJob.uberRepoLoader()

    val counter = sparkContext.longAccumulator("Processed repo counter")
    sparkContext.textFile(uberRepoHadoopPath).filter(x => x.startsWith("https://")).foreach(repoUrl => {

      val urlSplit = repoUrl.split("/")
      val repoAuthor = urlSplit(urlSplit.length - 2)
      val repoName = urlSplit(urlSplit.length - 1)
      val storingPath = HdfsUtils.rootPath + "/" + individualJsonPath + repoAuthor + "_" + repoName + ".json"
      val storingPathOfEmptyRepo = HdfsUtils.rootPath + "/" + individualEmptyJsonPath + repoAuthor + "_" + repoName + ".json"

      counter.add(1)
      println(s"\nworking on $repoUrl.")
      println("Processing ID = " + counter.value)

      if (HdfsUtils.hdfs.exists(new Path(storingPath)) && !ProcessJob.isPyRepoJsonStrEmpty(sparkContext, storingPath)) { // todo check empty json
        println(s"Json data from $repoUrl is already generated and it is not Empty.\nIf you want to process again then delete $storingPath first.\n")
      } else {

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
        val jsonStr = new GsonBuilder().setPrettyPrinting().create().toJson(pyRepoWholeInfo)

        /**
          * Json generated from individual repo is saved in following path and later all json are
          * merged to one single json array.
          * HDFS Path => hdfsRootPath/stage1/individual_repos/repoAuthor_repoName.json
          */
        HdfsUtils.saveTextStrToHdfs(jsonStr, storingPath)

        /**
          * If a repo is empty or does not contain even a single .py file, it will be stored in following HDFS path
          * to debug later.
          * HDFS Path => hdfsRootPath/stage1/individual_repos_empty/repoAuthor_repoName.json
          *
          */
        if (ProcessJob.isPyRepoEmpty(pyRepoWholeInfo)) {
          HdfsUtils.saveTextStrToHdfs(jsonStr, storingPathOfEmptyRepo)
        }

        println("*****************************************")
        println(jsonStr)
        println("*****************************************")
      }
    })

    val finalJsonStr = sparkContext.wholeTextFiles(HdfsUtils.rootPath + "/" + individualJsonPath + "/*").map(u => u._2).collect().mkString("[\n", ",\n", "\n]")
    val finalJsonStrPath = HdfsUtils.rootPath + "/" + pathProperty.getProperty("resultJsonFilePath")
    println(s"Stopping Process and generating final results.json from $individualJsonPath.\n")
    sparkContext.stop()

    HdfsUtils.saveTextStrToHdfs(finalJsonStr, finalJsonStrPath)
    println("**********************************************************************************")
    println("results.json successfully created at: \n" + finalJsonStrPath)
    println("**********************************************************************************")
    println("\nProcess execution completed at " + LocalTime.now())
    val timeTakenInSecond = 1.0 * (System.nanoTime() - startTime) / 1000000000
    printf("Total time taken: %f seconds.\n", timeTakenInSecond)
  }

}
