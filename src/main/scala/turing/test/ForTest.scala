package turing.test

import java.io.{File, IOException}
import java.nio.charset.Charset
import java.nio.file.Files

/**
  * Created by kneupane on 4/15/19.
  */
object ForTest {

  val fileName = "/home/kneupane/work/projects/practice/turing_git_analysis/src/main/scala/turing/test/simple.py"

  @throws[IOException]
  private def readFile(file: File, encoding: Charset) = {
    val encoded = Files.readAllBytes(file.toPath)
    new String(encoded, encoding)
  }


  def main(args: Array[String]): Unit = {
    val source: String = readFile(new File(fileName), Charset.forName("UTF-8"))
      .replaceAll("\r\n", "\n").replaceAll("\r", "\n")

    var fors: Array[String] = source.split("\n").filter(x => x.contains("for"))
      .map(x => (x.split("for ")(0) + "for").replace("    "," "))

   // var gap = fors()

    // countAlgo(fors)
    fors.foreach(println)

  }


  def countAlgo(strLine: Array[String]): Int = {


    0
  }

}
