name := "progress-bar"

organization := "lilinio.com"

version := "0.0.1"

scalaVersion := "2.13.0"

val root = (project in file("."))
  .enablePlugins(PlayScala)

dependencyOverrides += "org.scala-lang" % "scala-compiler" % scalaVersion.value

libraryDependencies ++= Seq(
  "com.beachape" %% "enumeratum" % "1.5.13",
  "com.h2database" % "h2" % "1.4.192",
  "com.typesafe.slick" %% "slick" % "3.3.2"
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
