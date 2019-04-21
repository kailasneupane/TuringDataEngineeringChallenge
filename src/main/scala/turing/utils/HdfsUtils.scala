package turing.utils

import java.net.URI

import sys.process._
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}

object HdfsUtils {

  val hadoopConf = new Configuration()
  //val uriFromLocal = Process("/usr/local/hadoop/bin/hdfs getconf -confKey fs.defaultFS").!!.trim
  val uri = hadoopConf.get("fs.defaultFS")
  val hdfs = FileSystem.get(new URI(uri), hadoopConf)

  def rootPath = hdfs.getHomeDirectory.toString

  def copyPyFilesFromLocalToHdfs(src: String, dest: String, delSrc: Boolean = false): Unit = {
    val srcPath = new Path(src)
    val destPath = new Path(dest)
    if (hdfs.exists(destPath)) {
      hdfs.delete(destPath, true)
    }
    hdfs.copyFromLocalFile(delSrc, true, srcPath, destPath)
  }

  def saveTextStrToHdfs(content: String, filePath: String): Unit = {
    val outputFile = hdfs.create(new Path(filePath))
    outputFile.write(content.getBytes("UTF-8"))
    outputFile.close()
  }

}
