package turing.loader

import scala.reflect.io.File

/**
  * Created by kneupane on 4/10/19.
  */
object Utils {

  def retainPyFilesOnly(path: String): Unit = {
    for {
      files <- Option(new java.io.File(path).listFiles)
      file <- files if !file.getName.endsWith(".py")
    } file.delete()
  }

}
