package bigdata.streaming.spark

import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.{DStream, ReceiverInputDStream}
import org.apache.spark.streaming.{Seconds, StreamingContext}

object SparkStreamingWordCount {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
      .setMaster("local[2]")
      .setAppName("StreamingWordCount")
    val ssc: StreamingContext = new StreamingContext(conf, Seconds(5))

    //ssc.sparkContext.

    val lines: ReceiverInputDStream[String] = ssc.socketTextStream("localhost", 9999)
    val words: DStream[String] = lines.flatMap( _.split("\\s"))
    val pairs: DStream[(String, Int)] = words.map(word => (word, 1))
    val wordCounts: DStream[(String, Int)] = pairs.reduceByKey( _ + _ )
    wordCounts.print()

    ssc.start()
    ssc.awaitTermination()
    //ssc.awaitTerminationOrTimeout(20000)
  }
}
