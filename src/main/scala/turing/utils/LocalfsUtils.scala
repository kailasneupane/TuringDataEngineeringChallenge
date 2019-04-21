package turing.utils

import java.io.File

import org.apache.commons.io.FileUtils

object LocalfsUtils {

  def getRecursiveListOfFiles(dir: File): Array[File] = {
    val these = dir.listFiles
    these ++ these.filter(_.isDirectory).flatMap(getRecursiveListOfFiles)
  }

  def retainPyFilesOnly(repoPath: String): Boolean = {
    getRecursiveListOfFiles(new File(repoPath)).reverse.foreach(path => {
      if (!path.getName.endsWith(".py"))
        path.delete()
      if (path.getName.startsWith("_"))
        path.renameTo(new File(path.getParent + "/i" + path.getName))
      if (!path.isDirectory && path.getName.contains(":")) {
        path.renameTo(new File(path.getParent + "/" + path.getName.replaceAll(":", "%3A")))
      }
      if (path.isDirectory && path.getPath.contains(":")) {
        val newPath = path.getPath.replaceAll(":", "%3A")
        path.renameTo(new File(newPath))
      }
      if (path.isDirectory && path.getName.startsWith(".")) {
        println(path.getParent + " will be renamed")
        path.renameTo(new File(path.getPath.replaceAll("/\\.", "/i")))
      }
    })
    getRecursiveListOfFiles(new File(repoPath)).filter(path => path.getName.endsWith(".py")).size > 0
  }

}
