name := "sbt-streaming-hello"

version := "0.1"

scalaVersion := "2.11.12"

mainClass in (Compile, run) := Some("bigdata.streaming.spark.SparkStreamingWordCount")
mainClass in (Compile, packageBin) := Some("bigdata.streaming.spark.SparkStreamingWordCount")

val sparkVersion = "2.2.0"

libraryDependencies ++= Seq(
  "org.scalaj" %% "scalaj-http" % "2.3.0",
  "joda-time" % "joda-time" % "2.9.9",
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-streaming" % sparkVersion,
  "edu.stanford.nlp" % "stanford-corenlp" % "3.9.1",
  "edu.stanford.nlp" % "stanford-corenlp" % "3.9.1" classifier "models",
  "edu.stanford.nlp" % "stanford-corenlp" % "3.9.1" % "test" classifier "models",
  "org.scalatest" %% "scalatest" % "3.0.4" % "test"
)