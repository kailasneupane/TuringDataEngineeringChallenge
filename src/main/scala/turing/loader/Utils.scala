package turing.loader


import java.net.URI

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.FileSystem
import org.apache.hadoop.fs.Path

import sys.process._

/**
  * Created by kneupane on 4/10/19.
  */
object Utils {

  val hadoopConf = new Configuration()
  val hdfs = FileSystem.get(new URI("hdfs://localhost:54310"), hadoopConf)

  private def retainPyFilesOnly(path: java.io.File): Unit = {
    if (path.isDirectory)
      path.listFiles.foreach(retainPyFilesOnly)
    if (path.exists && !path.getName.endsWith(".py"))
      path.delete()
  }

  def cloneRepoAndRetainPyFilesOnly(url: String, hadoopPathOfRetainedPy: String = "stage0/repos/"): Unit = {
    val cloningPathLocal: String = "/home/kneupane/work/projects/practice/turing_git_analysis/output/repos/"
    val directory = new java.io.File(cloningPathLocal)

    val repoName = url.substring(url.lastIndexOf("/"))


    if (!directory.exists) {
      directory.mkdir
    }

    s"git clone $url $cloningPathLocal$repoName --branch master --single-branch" !

    retainPyFilesOnly(new java.io.File(cloningPathLocal + repoName))
    println(s"git clone $repoName successful and only .py files retained in local.")


    val srcPath = new Path(cloningPathLocal + repoName)
    val destPath = new Path(hadoopPathOfRetainedPy + repoName)
    if (hdfs.exists(destPath)) {
      hdfs.delete(destPath, true)
    }
    hdfs.copyFromLocalFile(true, true, srcPath, destPath)

    println(s"Files copied from $srcPath to $destPath")
  }

}
