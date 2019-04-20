addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.7")
updateOptions := updateOptions.value.withCachedResolution(true)
addSbtPlugin("io.get-coursier" % "sbt-coursier" % "1.0.1")
