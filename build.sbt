name := "progress-bar"

organization := "text-scan.com"

version := "0.0.1"

scalaVersion := "2.13.0"

val root = (project in file("."))
  .enablePlugins(PlayScala)

dependencyOverrides += "org.scala-lang" % "scala-compiler" % scalaVersion.value

libraryDependencies ++= Seq(
  "com.beachape" %% "enumeratum" % "1.5.13"
)

scalacOptions in ThisBuild ++= Seq(
  "-target:jvm-1.8",
  "-encoding", "UTF-8",
  "-deprecation", // warning and location for usages of deprecated APIs
  "-feature", // warning and location for usages of features that should be imported explicitly
  "-unchecked", // additional warnings where generated code depends on assumptions
  "-Xlint" // recommended additional warnings
)

mainClass in Compile := Some("Main")
