package turing.lib

import org.apache.spark.SparkContext

import scala.collection.mutable.ListBuffer


class DuplicateFinder(sparkContext: SparkContext) extends Serializable {


  private var duplicate4LinesAccumulator = sparkContext.longAccumulator("Duplicate code count accumulator")

  def getConsecutive4LineDuplicateCount = duplicate4LinesAccumulator.value

  def duplicateAnalysisPerFile(fileStr: String): Unit = {
    //  println("duplicate lines check ma chiryo")
    val fileStrCRLF = fileStr.replaceAll("\r\n", "\n").replaceAll("\r", "\n")
    val lines = fileStrCRLF.split("\n").filter(x => !(x.trim.startsWith("#") || x.trim.isEmpty))
    var each4lines: ListBuffer[String] = ListBuffer()

    for (i <- 0 until (lines.length - 3)) {
      each4lines += (lines(i) + lines(i + 1) + lines(i + 2) + lines(i + 3))
    }

    // each4lines.groupBy(fourLines => fourLines).mapValues(_.length).foreach(println)
    var exclusiveDuplicateCount: Int = 0
    try {
      exclusiveDuplicateCount = each4lines.groupBy(fourLines => fourLines).mapValues(_.length).map(x => x._2).filter(x => x != 1).map(x => x - 1).reduce(_ + _)
    } catch {
      case e1: UnsupportedOperationException => ()
    }
    duplicate4LinesAccumulator.add(0l + exclusiveDuplicateCount)
  }
}
