package turing.loader


import java.io.File
import java.net.URL
import java.util.Properties

import org.apache.commons.io.FileUtils
import turing.utils.{HdfsUtils, PropertiesUtils}

import sys.process._

/**
  * Created by kneupane on 4/10/19.
  */
object Utils {

  var pathProperty: Properties = PropertiesUtils.loadProperties("paths/in_out_paths.jobcfg")

  def uberRepoLoader(): Unit = {
    val url = pathProperty.getProperty("uberRepoUrl")
    val fileName = url.substring(url.lastIndexOf("/") + 1)
    val localPath = pathProperty.getProperty("uberRepoLocalPath")
    val localPathFile = localPath + fileName
    val hadoopPath = HdfsUtils.rootPath + "/" + pathProperty.getProperty("uberRepoRawPath") + fileName

    println("local path " + localPath)
    println("fileName " + fileName)
    println(s"Loading uber repo from $url")

    Process(s"mkdir -p $localPath").!
    new URL(url) #> new File(localPathFile) !!

    HdfsUtils.copyPyFilesFromLocalToHdfs(localPathFile, hadoopPath, false)
    println(s"uber repo loaded in $hadoopPath")
  }

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
    HdfsUtils.copyPyFilesFromLocalToHdfs(srcPath, destPath, false)

    println(s"Files copied from $srcPath to $destPath")
  }

}
