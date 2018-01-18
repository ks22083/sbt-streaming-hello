package bigdata.streaming.spark

import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.{DStream, ReceiverInputDStream}
import org.apache.spark.streaming.{Seconds, StreamingContext}

object SparkStreamingWordCount {

  def createStreamingContext(checkpointDir: String): StreamingContext = {
    val conf = new SparkConf()
      .setAppName("StreamingWordCount")
      .setMaster("local[2]")
      .set("spark.streaming.backpressure.enabled", "true")
      .set("spark.streaming.stopGracefullyOnShutdown", "true")

    val ssc: StreamingContext = new StreamingContext(conf, Seconds(5))

    ssc.checkpoint(checkpointDir)
    ssc
  }

  def main(args: Array[String]): Unit = {

    val checkpointDir = "./checkpoint"

    val streamingContext =
      StreamingContext.getOrCreate(checkpointDir, () => createStreamingContext(checkpointDir) )

    val lines: ReceiverInputDStream[String] = streamingContext.socketTextStream("localhost", 9999)
    val words: DStream[String] = lines.flatMap( _.split("\\s"))
    val pairs: DStream[(String, Int)] = words.map(word => (word, 1))
    val wordCounts: DStream[(String, Int)] = pairs.reduceByKey( _ + _ )
    wordCounts.print()

    streamingContext.start()
    streamingContext.awaitTermination()
    //ssc.awaitTerminationOrTimeout(20000)
  }
}
