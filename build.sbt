name := "sbt-streaming-hello"

version := "0.1"

scalaVersion := "2.11.11"

mainClass in (Compile, run) := Some("bigdata.streaming.spark.SparkStreamingWordCount")

val sparkVersion = "2.2.0"

libraryDependencies ++= Seq(
  "org.scalaj" %% "scalaj-http" % "2.3.0",
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-streaming" % sparkVersion,
  "org.scalatest" % "scalatest" % "3.0.4" % "test"
)