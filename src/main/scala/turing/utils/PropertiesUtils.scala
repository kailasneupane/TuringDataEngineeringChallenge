package turing.utils

import java.io.{IOException}
import java.util.Properties

object PropertiesUtils {

  def loadProperties(resourcePath: String): Properties = {
    val prop = new Properties
    new PropertiesUtilsCls().appendProperties(resourcePath, prop)
    prop
  }
}

class PropertiesUtilsCls {
  def appendProperties(fileName: String, prop: Properties): Unit = {
    val propStream = classOf[PropertiesUtilsCls].getClassLoader.getResourceAsStream(fileName)
    try
      prop.load(propStream)
    catch {
      case e: IOException =>
        e.printStackTrace()
    }
  }
}
