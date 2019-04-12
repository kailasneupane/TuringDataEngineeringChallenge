package turing.loader


import java.util.Properties

import org.apache.commons.io.FileUtils
import turing.utils.{HdfsUtils, PropertiesUtils}

import sys.process._

/**
  * Created by kneupane on 4/10/19.
  */
object Utils {

  var pathProperty: Properties = PropertiesUtils.loadProperties("paths/in_out_paths.jobcfg")

  private def retainPyFilesOnly(path: java.io.File): Unit = {
    if (path.isDirectory)
      path.listFiles.foreach(retainPyFilesOnly)
    if (path.exists && !path.getName.endsWith(".py") || path.isHidden)
      path.delete()
  }

  def cloneRepoAndRetainPyFilesOnly(url: String): Unit = {
    val cloningPathLocal: String = pathProperty.getProperty("pyLocalPath")
    val repoName = url.substring(url.lastIndexOf("/") + 1)
    val cloneDirectory = new java.io.File(cloningPathLocal + "/" + repoName)

    FileUtils.deleteDirectory(cloneDirectory)

    s"git clone $url $cloningPathLocal$repoName --branch master --single-branch" !

    retainPyFilesOnly(new java.io.File(cloningPathLocal + "/" + repoName))
    println(s"git clone $repoName successful and only .py files retained.")

    val srcPath = cloningPathLocal + repoName
    val destPath = HdfsUtils.rootPath + "/" + pathProperty.getProperty("pyStage0Path") + "/" + repoName
    HdfsUtils.copyPyFilesFromLocalToHdfs(srcPath, destPath, true)

    println(s"Files copied from $srcPath to $destPath")
  }

}
