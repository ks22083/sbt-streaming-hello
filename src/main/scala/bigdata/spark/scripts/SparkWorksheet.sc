import org.apache.spark.{SparkConf, SparkContext, SparkEnv}

val conf = new SparkConf()
conf.setMaster("local[*]")
conf.setAppName("Simple Application")

val sc = SparkContext.getOrCreate(conf)

val logFile = "/opt/local/spark-2.2.1-bin-hadoop2.7/README.md"
val logData = sc.textFile(logFile).cache()
val numAs = logData.filter(line => line.contains("a")).count()
val numBs = logData.filter(line => line.contains("b")).count()

println(s"Lines with a: $numAs, Lines with b: $numBs")

val rdd = sc.parallelize(List("Apples", "bananas", "APPLES", "pears", "Bananas"))
rdd.toDebugString
val rdd2 = rdd.map(_.toLowerCase).filter(_.startsWith("a"))
println("total partitions: " + rdd.getNumPartitions)  // how many? why?

rdd2.dependencies // this is the source/root RDD
rdd2.toDebugString

SparkEnv.get.closureSerializer

//
//val pagecounts = sc.textFile("~/Wikipediafiles/pagecounts-20160801-000000")
//pagecounts.take(10).foreach(println)
//println(s"Lines in pagecount: ${pagecounts.count}")
//
//pagecounts.filter(_.split(" ")(0) == "en").take(10).foreach(println)
//pagecounts.filter(_.split(" ")(0) == "en").count()

//sc.stop()
