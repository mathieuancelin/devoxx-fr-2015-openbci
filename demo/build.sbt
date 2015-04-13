name := """openbci"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  ws,
  "commons-io" % "commons-io" % "2.4",
  "org.reactivestreams" % "reactive-streams-spi" % "0.3",
  "org.reactivestreams" % "reactive-streams-tck" % "0.4.0",
  "com.typesafe.akka" %% "akka-stream-experimental" % "0.11"
)
