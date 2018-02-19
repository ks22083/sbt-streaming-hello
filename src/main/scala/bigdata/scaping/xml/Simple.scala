package bigdata.scaping.xml

import java.io.{ByteArrayInputStream, InputStreamReader}
import java.net.{ConnectException, MalformedURLException, SocketTimeoutException, UnknownHostException}
import java.util.Locale

import org.joda.time.{DateTime, DateTimeComparator, Duration, Period}
import org.joda.time.format.{DateTimeFormat, DateTimeFormatterBuilder}

import scala.xml._
import scalaj.http.{Http, HttpConstants, HttpResponse}

object Simple {

  sealed abstract class rssNode ()

  case class ChannelInfo(title: String, link: String,
                         description: String, language: String,
                         imageURL: String,
                         pubDate: Option[String]
                        ) extends rssNode

  case class Record(title: String,
                    link: String,
                    description: String,
                    category: Option[String],
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
      Some((n \\ "category").text),
      Some(n \\ "enclosure"),
      Some((n \\ "guid").text),
      try {parsePubDate((n \\ "pubDate").text)} catch { case e: IllegalArgumentException => None}
    )
  }

  // todo handle XML escape characters like &amp; &quot; &apos; &lt; &gt;
  def main(args: Array[String]): Unit = {
//    val request = Http("https://sdelanounas.ru/index/rss")
//    val request = Http("https://rg.ru/xml/index.xml")
//    val request = Http("https://lenta.ru/rss/news")
//    val request = Http("https://russian.rt.com/rss") // url/category/type
//    val request = Http("https://www.rt.com/rss/") // url/category/type
//    val request = Http("http://feeds.bbci.co.uk/news/world/rss.xml") // type[news]/location[world-latin-america]
//    val request = Http("http://rss.cnn.com/rss/edition.rss")
//  fontanka need conversion from windows-1251
    val request = Http("http://www.fontanka.ru/fontanka.rss")
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
    // TODO handle XML escape characters like &amp; &quot; &apos; &lt; &gt; _&laquo;_ &nbsp;

    val formatter = new PrettyPrinter(240,4)

    val chanNode: NodeSeq = xml \\ "rss" \ "channel"
    chanNode.head.child
      .filter(x => x.label != "item" && !x.isAtom)
      .foreach(x => println(formatter.format(x)))

    val chanInfo  = ChannelInfo(
      (chanNode \ "title").text,
      (chanNode \ "link").text,
      (chanNode \ "description").text,
      (chanNode \ "language").text,
      (chanNode \ "image" \ "url" ).text,
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
    recs.foreach(x => println(s"${x.category.get}, ${x.link}"))
    //http://allaboutscala.com/tutorials/chapter-8-beginner-tutorial-using-scala-collection-functions/scala-reduce-example/
    println(recs.map(x => x.category.get).groupBy(x=>x).mapValues(_.size))
    val rssSize = itemNodes.size
//    println(itemNodes.size)

    val categoryMap = recs.map(x => x.category.get).foldLeft(Map.empty[String, Int]){
      (count: Map[String, Int], word: String) => count + (word -> (count.getOrElse(word, 0) + 1))
    }

    println(categoryMap.toList.sortBy(-_._2))

    val minMax = dateMinMax(recs)
    val period = new Period(minMax._1, minMax._2)
    val duration = new Duration(minMax._1, minMax._2).getStandardMinutes
    println( s"""$rssSize events in ${duration}min. (${rssSize.toFloat/duration} ev/min)
                 |earliest=${minMax._1} latest=${minMax._2}""".stripMargin)

    val c = DateTimeComparator.getInstance()
    val sortedRecs = recs
      .sortWith((a, b) => (a.pubDate.isDefined && b.pubDate.isDefined
        && c.compare(a.pubDate.get, b.pubDate.get) > 0) )

    sortedRecs.foreach(x => println(s"${x.pubDate.getOrElse("?")}, ${x.title}\n${" "*10}${x.description}"))

    val durationList = sortedRecs.map(x => x.pubDate.get)
      .foldLeft((List.empty[Duration], new DateTime))
      {(acc, tStamp) => (acc._1 :+ new Duration(tStamp, acc._2), tStamp)}

    println(durationList._1.map(x=>x.getStandardMinutes))
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
