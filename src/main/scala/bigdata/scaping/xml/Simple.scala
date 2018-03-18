package bigdata.scaping.xml

import java.io.{ByteArrayInputStream, InputStreamReader}
import java.net.{ConnectException, MalformedURLException, SocketTimeoutException, UnknownHostException}
import java.util
import java.util.Locale

import edu.stanford.nlp.simple.Sentence
import org.joda.time.{DateTime, DateTimeComparator, Duration, Period}
import org.joda.time.format.{DateTimeFormat, DateTimeFormatterBuilder}

import scala.xml._
import scalaj.http.{Http, HttpConstants, HttpResponse}

object Simple {

  sealed abstract class rssNode ()

  case class ChannelInfo(title: String,
                         link: String,
                         description: String,
                         language: Option[String],
                         imageURL: Option[String],
                         pubDate: Option[String]
                        ) extends rssNode

  case class Record(title: String,
                    link: String,
                    description: String,
                    category: Option[Seq[String]],
                    enclosure: Option[NodeSeq],
                    guid: Option[String],
                    pubDate: Option[DateTime]
                   ) extends rssNode

  private val jodaParsers = Array(
    DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss Z").getParser,
    DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss ZZZ").getParser,
    DateTimeFormat.forPattern("dd MMM yyyy HH:mm:ss ZZZ").getParser,
    DateTimeFormat.forPattern("dd MMM yyyy HH:mm:ss Z").getParser
  )

  private val jodaFormatter = new DateTimeFormatterBuilder()
    .append(null, jodaParsers) // use parsers array
    .toFormatter.withLocale(Locale.ENGLISH).withOffsetParsed()

  def parsePubDate(dateStr: String): Option[DateTime] = {
    Some(jodaFormatter.parseDateTime(dateStr))
  }

  def processItemNode(n: Node): Record = {
    Record((n \\ "title").text.trim,
      (n \\ "link").text,
      (n \\ "description").text.trim,
      Some((n \\ "category").map(_.text)),
      Some(n \\ "enclosure"),
      Some((n \\ "guid").text),
      try {
        parsePubDate((n \\ "pubDate").text)
      } catch {
        case e: IllegalArgumentException => None
      }
    )
  }

  type SDocument = edu.stanford.nlp.simple.Document
  import scala.collection.JavaConversions._

  def stanfordNLP(text: String):Unit = {
    //    val doc = new SDocument("Lucy in the sky with diamonds. Hard day's night.")
    val doc = new SDocument(text)

    doc.sentences().foreach(x => println (s">>$x>>${x.sentiment()}\n"))
  }



  // todo handle XML escape characters like &amp; &quot; &apos; &lt; &gt;
  def main(args: Array[String]): Unit = {

    stanfordNLP("This is a test")
//    val request = Http("https://sdelanounas.ru/index/rss")
//    val request = Http("https://rg.ru/xml/index.xml")
//    val request = Http("https://lenta.ru/rss/news")
//    val request = Http("https://russian.rt.com/rss") // url/category/type
//    val request = Http("https://www.rt.com/rss/") // url/category/type
//    val request = Http("https://www.bfm.ru/news.rss")
//    val request = Http("http://feeds.bbci.co.uk/news/world/rss.xml") // type[news]/location[world-latin-america]
//    val request = Http("http://rss.cnn.com/rss/edition.rss")
//    val request = Http("https://www.theguardian.com/world/rss")
//    val request = Http("https://www.theguardian.com/world/russia/rss")
    val request = Http("http://rss.nytimes.com/services/xml/rss/nyt/World.xml")
//    val request = Http("http://rss.nytimes.com/services/xml/rss/nyt/HomePage.xml")
//    val request = Http("http://feeds.washingtonpost.com/rss/world") // No pubDate
//  fontanka need conversion from windows-1251
//    val request = Http("http://www.fontanka.ru/fontanka.rss")
//    val request = Http("http://fontanka.ru/fontanka.rss") // Page moved
//    val request = Http("http://localhost/fontanka.rss") // Connection refused
//    val request = Http("http://apache.spark.org/") // Service unavailable
    .timeout(connTimeoutMs = 3000, readTimeoutMs = 10000)

    var evalRequest = None: Option[HttpResponse[Array[Byte]]]

    try {
      evalRequest = Some(request.asBytes)
      evalRequest.get.code match {
        case 200 => //println("Ok")
        case 301 => println(s"Page Moved: ${evalRequest.get.toString}")
          // Better to handle forwarding

          println(s"${request.url}->${evalRequest.get.header("Location").get}")
          System.exit(1)
        case 404 => println(s"Page Not Found: ${request.url}")
          System.exit(1)
        case 503 => println(s"Service Unavailable: ${request.url}")
          // Better to retry with some strategy
          System.exit(1)
        case n: Int => println(s"${evalRequest.get.toString}\nResponse code: $n")
          System.exit(1)
      }
    } catch {
      case e: UnknownHostException => println(s"Host not found: ${e.getMessage}"); System.exit(1)
      case e: SocketTimeoutException => println(s"$e URL:${request.url}"); System.exit(1)
      case e: ConnectException => println(s"$e URL:${request.url}"); System.exit(1)
      case e: MalformedURLException => println(s"$e URL:${request.url}"); System.exit(1)
    }

    val response = evalRequest.getOrElse(throw new RuntimeException())
    println(response.headers)
    /* All this code has written because in case
     * WHEN http server didn't return charset for the xml content-type
     *   AND incoming xml declaration has encoding = "some-encoding"
     * In this case we use xml declaration encoding
     * In case XML declaration doesn't have encoding attribute
     *    we use HTTP response charset if set or ISO-8859-1 as default
     */
    val respCharset: String = response.headers.get("content-type").flatMap(_.headOption).flatMap(ct => {
      HttpConstants.CharsetRegex.findFirstMatchIn(ct).map(_.group(1))
    }).getOrElse("ISO-8859-1")
    val declAttrs: Map[String,String] = new String(response.body, respCharset)
      .split("\\?")(1).split(" ").toList
      .filter(_ != "xml")
      .map((x) => {val kv=x.split("="); (kv(0).toLowerCase(),kv(1).replace("\"", "")) })
      .toMap
/*
    println(response.contentType.getOrElse("None"))
    println(response.headers)
    println(s"HTTP charset:$respCharset\nXML encoding:${declAttrs.get("encoding")}")
*/

    // TODO there should be a way to get encoding from XML declaration

    val xml = XML
        .load(new InputStreamReader(
          new ByteArrayInputStream(response.body), declAttrs getOrElse ("encoding", respCharset/*"UTF-8"*/))
        )
    // TODO transform url with formatter
    // TODO handle XML escape characters like &amp; &quot; &apos; &lt; &gt; &laquo; &nbsp;

    val formatter = new PrettyPrinter(240,4)

    val chanNode: NodeSeq = xml \\ "rss" \ "channel"
    chanNode.head.child
      .filter(x => x.label != "item" && !x.isAtom)
      .foreach(x => println(formatter.format(x)))

    val chanInfo  = ChannelInfo(
      (chanNode \ "title").text,
      (chanNode \ "link").text,
      (chanNode \ "description").text,
      Some((chanNode \ "language").text),
      Some((chanNode \ "image" \ "url" ).text),
      Some((chanNode \ "pubDate" ).text)
    )

    println(chanInfo+"\n")

    val itemNodes = xml \\ "item"
    itemNodes.take(1).foreach(x => println(formatter.format(x)))

    itemNodes
      .take(1)
      .map(processItemNode)
      .foreach(x => println(x.pubDate.get, x.category.get, x.title, x.enclosure.get))

    println(itemNodes
      .take(1)
      .map(processItemNode).head)

    println(s"title:${chanInfo.title}\ndescr:${chanInfo.description}\npDate:${chanInfo.pubDate.getOrElse("?")}")

    val recs = itemNodes.map(processItemNode)
//    recs.foreach(x => println(s"${x.category.get}, ${x.link}"))
    //http://allaboutscala.com/tutorials/chapter-8-beginner-tutorial-using-scala-collection-functions/scala-reduce-example/
    println(recs.map(x => x.category.get).groupBy(x=>x).mapValues(_.size))
    val rssSize = itemNodes.size
//    println(itemNodes.size)

    val categoryMap = recs.flatMap(x => x.category.get).foldLeft(Map.empty[String, Int]){
      (count, word) => count + (word -> (count.getOrElse(word, 0) + 1))
    }

    println(categoryMap.toList.sortBy(-_._2))


    // Sort incoming records by pubDate
    val c = DateTimeComparator.getInstance()
    val sortedRecs = recs
      .sortWith((a, b) => (a.pubDate.isDefined && b.pubDate.isDefined
        && c.compare(a.pubDate.get, b.pubDate.get) > 0) )

    sortedRecs
      .foreach(x =>
        println(s"""${x.pubDate.getOrElse("?")}, ${x.title}\n${" "*10}${x.description}\n${x.category.get}""".stripMargin))

    //Top categories
    val catMap = sortedRecs.flatMap(x => x.category.get).foldLeft(Map.empty[String, Int]){
      (count, word) => count + (word -> (count.getOrElse(word, 0) + 1))
    }.toList

    println(catMap.sortBy(-_._2))

    // Rate of news items
    println("\n Channel stats")
    val minMax = dateMinMax(recs)
//    val period = new Period(minMax._1, minMax._2)
    val duration = new Duration(minMax._1, minMax._2).getStandardMinutes
    println( s"""$rssSize events in ${duration}min. (${rssSize.toFloat/duration} ev/min or ${rssSize.toFloat*60/duration} ev/hour)
                |earliest=${minMax._1} latest=${minMax._2}""".stripMargin)

    // List of periods between items
    val durationList = sortedRecs.map(x => x.pubDate.get)
      .foldLeft((List.empty[Duration], new DateTime))
      {(acc, tStamp) => (acc._1 :+ new Duration(tStamp, acc._2), tStamp)}._1

    println(durationList.map(x=>x.getStandardMinutes))

    // Sentiment analysis
    val titles = sortedRecs
      .take(1)
      .map(x => new SDocument(x.description))

    titles.flatMap(_.sentences()).foreach( x=> println(x.sentiment()))

    case class SentAn (title: String, decs: String, titleSA: String, descSA: String)
    val analyzeThis = (x: Record) => SentAn(x.title, x.description,
      new SDocument(x.title).sentences().map(_.sentiment()).toString(),
      new SDocument(x.description).sentences().map(_.sentiment()).toString())
    sortedRecs
//      .take(1)
      .foreach(x => println(analyzeThis(x)))
//      .foreach((x: rssNode) => println(s"${x.title} - ${new SDocument(x.title).sentences().map(_.sentiment())}"))


//        println(s"${x.title}->${new SDocument(x.title).sentences().foreach( x => println(x.sentiment()))}"))
//
  }

  def fileWordCount(filename: String): Map[String, Int] = {
    scala.io.Source.fromFile(filename)
      .getLines
      .flatMap(_.split("\\W+"))
      .foldLeft(Map.empty[String, Int]){
        (count, word) => count + (word -> (count.getOrElse(word, 0) + 1))
      }
  }

  def dateMinMax(recs: Seq[Record]): (DateTime, DateTime) = {
    val comparator = DateTimeComparator.getInstance()
    //TODO handle empty list
    recs.foldLeft(recs.head.pubDate.get, recs.head.pubDate.get)
    { case ((min:DateTime, max: DateTime), e: Record) =>
      (if (comparator.compare(min, e.pubDate.getOrElse(max)) < 0) min else e.pubDate.get,
       if (comparator.compare(max, e.pubDate.getOrElse(min)) >= 0) max else e.pubDate.get)
    }
  }

}
