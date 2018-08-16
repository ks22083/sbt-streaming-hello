import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext, SparkEnv}

val conf = new SparkConf()
conf.setMaster("local[*]")
conf.setAppName("Simple Application")

val sc = SparkContext.getOrCreate(conf)

def showRddPartitions[T](rdd: RDD[T]): Unit ={
  println("Content of [rdd] per parition:")
  rdd.glom.collect()
    .map(x => x.toList.mkString(","))
    .foreach(x => println(s"    Partition: $x"))
}

//See http://apachesparkbook.blogspot.com/2015/12/combiner-in-pair-rdds-combinebykey.html

val inputrdd = sc.parallelize(Seq(
  ("maths", 50), ("maths", 60),
  ("english", 65),
  ("physics", 66), ("physics", 61), ("physics", 87), ("english", 70)),
  2)

inputrdd.aggregateByKey((0,0))(
  seqOp =  (acc, value) => (acc._1 + value, acc._2 + 1),
  combOp = (acc1, acc2) => (acc1._1 + acc2._1, acc2._1 + acc2._2)
)
//  .mapValues(i => i._1/i._2)
  .take(3)

inputrdd.getNumPartitions
showRddPartitions(inputrdd)

val reduced = inputrdd.combineByKey(
  (mark) => {
    println(s"Create combiner -> ${mark}")
    (mark, 1)
  },
  (acc: (Int, Int), v) => {
    println(s"""Merge value : (${acc._1} + ${v}, ${acc._2} + 1)""")
    (acc._1 + v, acc._2 + 1)
  },
  (acc1: (Int, Int), acc2: (Int, Int)) => {
    println(s"""Merge Combiner : (${acc1._1} + ${acc2._1}, ${acc1._2} + ${acc2._2})""")
    (acc1._1 + acc2._1, acc1._2 + acc2._2)
  }
)
showRddPartitions(reduced)
reduced.getNumPartitions

//val logFile = "/opt/local/spark-2.2.1-bin-hadoop2.7/README.md"
//val logData = sc.textFile(logFile).cache()
//val numAs = logData.filter(line => line.contains("a")).count()
//val numBs = logData.filter(line => line.contains("b")).count()
//
//println(s"Lines with a: $numAs, Lines with b: $numBs")
//
//val rdd = sc.parallelize(List("Apples", "bananas", "APPLES", "pears", "Bananas"))
//rdd.toDebugString
//val rdd2 = rdd.map(_.toLowerCase).filter(_.startsWith("a"))
//println("total partitions: " + rdd.getNumPartitions)  // how many? why?
//
//rdd2.dependencies // this is the source/root RDD
//rdd2.toDebugString

SparkEnv.get.closureSerializer

//
//val pagecounts = sc.textFile("~/Wikipediafiles/pagecounts-20160801-000000")
//pagecounts.take(10).foreach(println)
//println(s"Lines in pagecount: ${pagecounts.count}")
//
//pagecounts.filter(_.split(" ")(0) == "en").take(10).foreach(println)
//pagecounts.filter(_.split(" ")(0) == "en").count()

//sc.stop()
