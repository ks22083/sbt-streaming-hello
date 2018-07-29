name := "sbt-streaming-hello"

version := "0.1"

scalaVersion := "2.11.12"

mainClass in (Compile, run) := Some("bigdata.streaming.spark.SparkStreamingWordCount")
mainClass in (Compile, packageBin) := Some("bigdata.streaming.spark.SparkStreamingWordCount")

val sparkVersion = "2.2.0"

libraryDependencies ++= Seq(
  "com.typesafe.scala-logging" %% "scala-logging" % "3.6.0",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.6.5",
  "org.scalaj" %% "scalaj-http" % "2.3.0",
  "joda-time" % "joda-time" % "2.9.9",
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-streaming" % sparkVersion,
  "edu.stanford.nlp" % "stanford-corenlp" % "3.9.1",
  "edu.stanford.nlp" % "stanford-corenlp" % "3.9.1" classifier "models",
  "edu.stanford.nlp" % "stanford-corenlp" % "3.9.1" % "test" classifier "models",
  "org.scalatest" %% "scalatest" % "3.0.4" % "test"
)

initialCommands in console := """
  import org.apache.spark.{SparkConf, SparkContext}

  val conf = new SparkConf()
  conf.setMaster("local[*]")
  conf.setAppName("Simple Console Application")

  val sc = SparkContext.getOrCreate(conf)
  print("sparkVersion" + org.apache.spark.SPARK_VERSION + "\n")
  org.apache.spark.SparkEnv.get.conf.toDebugString

"""

// For Spqrk SQL use
//  import org.apache.spark.SparkSession
//
//  val spark = SparkSession.builder()
//    .master("local")
//    .appName("spark-shell")
//    .getOrCreate()
//  import spark.implicits._

cleanupCommands in console := "sc.stop()"