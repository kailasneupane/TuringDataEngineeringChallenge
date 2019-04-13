package turing.utils

import java.net.URI

import sys.process._
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, LocatedFileStatus, Path, RemoteIterator}
import turing.loader.ProcessJob

object HdfsUtils {

  val hadoopConf = new Configuration()
  val uri = Process("/usr/local/hadoop/bin/hdfs getconf -confKey fs.defaultFS").!!.trim
  val hdfs = FileSystem.get(new URI(uri), hadoopConf)

  def main(args: Array[String]): Unit = {
    println(hdfs.getHomeDirectory)
    iterateEachRepoFiles("hamms")
  }

  def rootPath = hdfs.getHomeDirectory.toString

  def copyPyFilesFromLocalToHdfs(src: String, dest: String, delSrc: Boolean = false): Unit = {
    val srcPath = new Path(src)
    val destPath = new Path(dest)
    if (hdfs.exists(destPath)) {
      hdfs.delete(destPath, true)
    }
    hdfs.copyFromLocalFile(delSrc, true, srcPath, destPath)
  }

  def iterateEachRepoFiles(repoName: String): RemoteIterator[LocatedFileStatus] = {
    val fullPath = rootPath + "/" + ProcessJob.pathProperty.getProperty("pyStage0Path") + repoName
    var eachPyPath: RemoteIterator[LocatedFileStatus] = hdfs.listFiles(new Path(fullPath), true)
    //var listBuffer:ListBuffer[String] = ListBuffer()
    // while (eachPyPath.hasNext){
    //  listBuffer.append(eachPyPath.next().getPath.toString)
    //}
    //listBuffer.foreach(println)
    eachPyPath
  }


}
