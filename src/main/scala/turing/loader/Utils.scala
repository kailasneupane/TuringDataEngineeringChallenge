package turing.loader


import java.io.File

import sys.process._

/**
  * Created by kneupane on 4/10/19.
  */
object Utils {

  private def retainPyFilesOnly(path: java.io.File): Unit = {
    if (path.isDirectory)
      path.listFiles.foreach(retainPyFilesOnly)
    if (path.exists && !path.getName.endsWith(".py"))
      path.delete()
  }


  def cloneRepoAndRetainPyFilesOnly(url: String, cloningPath: String = "output/repos/"): Unit = {
    val directory = new java.io.File(cloningPath)
    if (!directory.exists) {
      directory.mkdir
    }
    val repoName = url.substring(url.lastIndexOf("/"))
    s"git clone $url $cloningPath$repoName --branch master --single-branch" !

    retainPyFilesOnly(new File(cloningPath + repoName))
    println(s"git clone $repoName successful and only .py files retained.")
  }

}
