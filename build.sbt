import Dependencies._

ThisBuild / scalaVersion := "2.11.2"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "np.com.kailasneupane"
ThisBuild / organizationName := "kailasneupane"

javacOptions ++= Seq("-source", "1.8", "-target", "1.8")


lazy val root = (project in file("."))
  .settings(
    name := "TuringDataEngineeringChallenge",
    assemblyJarName in assembly := "turingsPyGitAnalysis.jar",
    mainClass in assembly := Some("turing.loader.App"),
    libraryDependencies += scalaTest % Test
  )

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs@_*) => MergeStrategy.discard
  case x => MergeStrategy.first
}

libraryDependencies += "org.apache.spark" %% "spark-core" % "2.3.0"
libraryDependencies += "org.antlr" % "antlr4-runtime" % "4.7.2"
libraryDependencies += "org.eclipse.jgit" % "org.eclipse.jgit" % "5.3.0.201903130848-r"

dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-core" % "2.7.8"
dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-databind" % "2.7.8"
dependencyOverrides += "com.fasterxml.jackson.module" % "jackson-module-scala_2.11" % "2.7.8"

assemblyShadeRules in assembly := Seq(
  ShadeRule.rename("io.netty.**" -> "shadenetty.@1").inAll
)

mainClass in(Compile, run) := Some("turing.loader.App")


ThisBuild / developers := List(
  Developer(
    id = "1",
    name = "Kailash Neupane",
    email = "kailasneupane@gmail.com",
    url = url("https://kailasneupane.com.np")
  )
)
ThisBuild / pomIncludeRepository := { _ => false }
ThisBuild / publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}
ThisBuild / publishMavenStyle := true
