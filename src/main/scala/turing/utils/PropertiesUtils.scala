package turing.utils

import java.io.{IOException, InputStream}
import java.util.Properties

object PropertiesUtils {

  def loadProperties(resourcePath: String): Properties = {
    val prop = new Properties
    new PropertiesUtils().appendProperties(resourcePath, prop)
    prop
  }
}

class PropertiesUtils {
  private def appendProperties(fileName: String, prop: Properties): Unit = {
    val propStream = classOf[PropertiesUtils].getClassLoader.getResourceAsStream(fileName)
    try
      prop.load(propStream)
    catch {
      case e: IOException =>
        e.printStackTrace()
    }
  }
}
