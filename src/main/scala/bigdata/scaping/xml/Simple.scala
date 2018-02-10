package bigdata.scaping.xml

import java.io.{ByteArrayInputStream, InputStreamReader}
import java.net.{ConnectException, MalformedURLException, SocketTimeoutException, UnknownHostException}

import org.joda.time.DateTime
import org.joda.time.format.{DateTimeFormat, DateTimeFormatterBuilder}

import scala.xml._
import scalaj.http.{Http, HttpConstants, HttpResponse}

object Simple {

  sealed abstract class rssNode ()

  case class ChannelInfo(title: String, link: String,
                         description: String, language: String,
                         imageURL:String
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
    .toFormatter.withOffsetParsed()

  def parsePubDate(dateStr: String): Option[DateTime] = {
      Some(jodaFormatter.parseDateTime(dateStr))
  }

  def processItemNode(n: Node): Record = {
    Record((n \\ "title").text,
      (n \\ "link").text,
      (n \\ "description").text,
      Some((n \\ "category").text),
      Some(n \\ "enclosure"),
      Some((n \\ "guid").text),
      parsePubDate((n \\ "pubDate").text)
    )
  }

  // todo handle XML escape characters like &amp; &quot; &apos; &lt; &gt;
  def main(args: Array[String]): Unit = {
//    val request = Http("https://rg.ru/xml/index.xml")
//    val request = Http("https://russian.rt.com/rss")
//    val request = Http("http://feeds.bbci.co.uk/news/world/rss.xml")
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
    // TODO handle XML escape characters like &amp; &quot; &apos; &lt; &gt;

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
      (chanNode \ "image" \ "url" ).text
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

  }
}
