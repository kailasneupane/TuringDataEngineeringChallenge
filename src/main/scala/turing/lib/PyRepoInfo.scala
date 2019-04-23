package turing.lib

import com.google.gson.{Gson, GsonBuilder}

/**
  * example:
  *
  * 'repository_url': 'https://github.com/tensorflow/tensorflow',
  * 'number of lines': 59234,
  * 'libraries': ['tensorflow','numpy',..],
  * 'nesting factor': 1.457845,
  * 'code duplication': 23.78955,
  * 'average parameters': 3.456367,
  * 'average variables': 0.03674
  */
case class PyRepoInfo(
                       repository_url: String,
                       number_of_lines: Long,
                       libraries: Array[String],
                       nesting_factor: Double,
                       code_duplication: Double,
                       average_parameters: Double,
                       average_variables: Double
                     )


object PyRepoInfoJsonParser {

  var pyRepoInfo = PyRepoInfo("lold", 12, Array("Apple", "ball"), 3.43, 0.45, 33.67, 120.45)


  def main(args: Array[String]): Unit = {
    var str = new GsonBuilder().setPrettyPrinting().create().toJson(pyRepoInfo)
    println(str)

    var s: PyRepoInfo = (new Gson().fromJson(str, classOf[PyRepoInfo]))
    println("*************")
    s.libraries.foreach(println)
  }
}