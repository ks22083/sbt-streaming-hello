import org.apache.spark.{SparkConf, SparkContext}

val conf = new SparkConf()
conf.setMaster("local[*]")
conf.setAppName("Simple Application")

val sc = new SparkContext(conf)

val logFile = "/opt/spark-2.2.1-bin-hadoop2.7/README.md"
//val logFile = "/opt/local/spark-2.2.1-bin-hadoop2.7/README.md"
val logData = sc.textFile(logFile).cache()
val numAs = logData.filter(line => line.contains("a")).count()
val numBs = logData.filter(line => line.contains("b")).count()

println(s"Lines with a: $numAs, Lines with b: $numBs")

val pagecounts = sc.textFile("/home/esi/ttt/pagecounts-20160801-000000")
pagecounts.take(10).foreach(println)
println(s"Lines in pagecount: ${pagecounts.count}")

pagecounts.filter(_.split(" ")(0) == "en").take(10).foreach(println)
pagecounts.filter(_.split(" ")(0) == "en").count()


//sc.stop()
