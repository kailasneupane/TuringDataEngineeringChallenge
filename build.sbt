import Dependencies._

ThisBuild / scalaVersion     := "2.11.2"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"


javacOptions ++= Seq("-source", "1.8", "-target", "1.8")


lazy val root = (project in file("."))
  .settings(
    name := "turing_git_analysis",
    libraryDependencies += scalaTest % Test
  )

val sparkVersion = "2.3.0"

libraryDependencies ++= Seq(

  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-streaming" % sparkVersion,
  "org.apache.spark" %% "spark-sql" % sparkVersion,
  "org.apache.spark" %% "spark-hive" % sparkVersion,
  "org.apache.spark" %% "spark-repl" % sparkVersion,
  "org.apache.spark" %% "spark-streaming-twitter" % "1.6.3",
  "org.twitter4j" % "twitter4j-stream" % "4.0.6"
  //"org.apache.spark" % "spark-core_2.11_logging" % "1.5.2" from s"${baseDirectory}/src/resources/jars/spark-core_2.11-1.5.2.logging.jar"
)



mainClass in (Compile, run) := Some("turing.loader.App")

// Uncomment the following for publishing to Sonatype.
// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for more detail.

// ThisBuild / description := "Some descripiton about your project."
// ThisBuild / licenses    := List("Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt"))
// ThisBuild / homepage    := Some(url("https://github.com/example/project"))
// ThisBuild / scmInfo := Some(
//   ScmInfo(
//     url("https://github.com/your-account/your-project"),
//     "scm:git@github.com:your-account/your-project.git"
//   )
// )
// ThisBuild / developers := List(
//   Developer(
//     id    = "Your identifier",
//     name  = "Your Name",
//     email = "your@email",
//     url   = url("http://your.url")
//   )
// )
// ThisBuild / pomIncludeRepository := { _ => false }
// ThisBuild / publishTo := {
//   val nexus = "https://oss.sonatype.org/"
//   if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
//   else Some("releases" at nexus + "service/local/staging/deploy/maven2")
// }
// ThisBuild / publishMavenStyle := true
