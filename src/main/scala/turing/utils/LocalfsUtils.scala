package turing.utils

import java.io.File

import org.apache.commons.io.FileUtils

import sys.process._

object LocalfsUtils {


  def getRecursiveListOfFiles(dir: File): Array[File] = {
    val these = dir.listFiles
    these ++ these.filter(_.isDirectory).flatMap(getRecursiveListOfFiles)
  }

  def retainPyFilesOnly(repoPath: String): Boolean = {
    getRecursiveListOfFiles(new File(repoPath)).reverse.foreach(path => {
      println(path.getPath)
      if (!path.getName.endsWith(".py"))
        path.delete()
      if (path.getName.startsWith("_"))
        path.renameTo(new File(path.getParent + "/i" + path.getName))
    })
    getRecursiveListOfFiles(new File(repoPath)).filter(path => path.getName.endsWith(".py")).size > 0
  }

}
