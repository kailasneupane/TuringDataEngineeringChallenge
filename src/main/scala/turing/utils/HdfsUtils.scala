package turing.utils

import java.net.URI

import sys.process._
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}

object HdfsUtils {

  val hadoopConf = new Configuration()
  val uri = Process("/usr/local/hadoop/bin/hdfs getconf -confKey fs.defaultFS").!!.trim
  val hdfs = FileSystem.get(new URI(uri), hadoopConf)

  def main(args: Array[String]): Unit = {
    println(hdfs.getHomeDirectory)
    var property = PropertiesUtils.loadProperties("paths/in_out_paths.jobcfg")
    println(property.getProperty("pyLocalPath"))
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


}
