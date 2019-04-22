package turing.utils

import java.io.File
import java.nio.file.Files

object LocalfsUtils {

  def getRecursiveListOfFiles(dir: File): Array[File] = {
    val these = dir.listFiles
    these ++ these.filter(x => {
      {
        if (Files.isSymbolicLink(x.toPath)) x.delete() //side effect :(
      }
      x.isDirectory
    }).flatMap(getRecursiveListOfFiles)
  }

  def checkPyFiles(repoPath: String): Unit = {
    getRecursiveListOfFiles(new File(repoPath)).reverse.foreach(path => {
      if (!path.getName.endsWith(".py"))
        path.delete()
      if (path.getName.startsWith("_") || path.getName.startsWith("."))
        path.renameTo(new File(path.getParent + "/i" + path.getName))
      if (!path.isDirectory) {
        path.renameTo(new File(path.getParent + "/" + path.getName.replaceAll(":", "%3A").replace("*", "star_")))
      }
      if (path.isDirectory) {
        val newPath = path.getPath.replaceAll(":", "%3A").replace("*", "star_")
        path.renameTo(new File(newPath))
      }
      if (path.isDirectory && path.getName.startsWith(".")) {
        path.renameTo(new File(path.getPath.replaceAll("/\\.", "/i")))
      }
    })
  }

  def retainPyFilesOnly(repoPath: String): Boolean = {
    checkPyFiles(repoPath)
    //second attempt to review renamed/moved folders/files
    checkPyFiles(repoPath)
    getRecursiveListOfFiles(new File(repoPath)).filter(path => path.getName.endsWith(".py")).size > 0
  }

}
