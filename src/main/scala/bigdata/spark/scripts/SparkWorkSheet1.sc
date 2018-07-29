package bigdata.spark.scripts

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.log4j.{Level, Logger}

object SparkWorkSheet1 {
  // Set off the vebose log messages
  Logger.getLogger("org").setLevel(Level.OFF)
  Logger.getLogger("akka").setLevel(Level.OFF)

  val conf = new SparkConf()
    .setMaster("local[*]")
    .setAppName("SparkWorkSheet1")


  lazy val sc = new SparkContext(conf)

  val list = sc.parallelize(1 to 100)
  list.sum()

  // Your Spark codes are here
  // spark: sparkSession object (like sqlContext in Spark 1.x)
  // sc: sparkContext object (Spark 1.x compatible)

  sc.stop()
}