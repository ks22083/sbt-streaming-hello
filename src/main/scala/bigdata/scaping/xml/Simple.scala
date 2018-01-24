package bigdata.scaping.xml

import java.io.{ByteArrayInputStream, InputStreamReader}
import java.net.{ConnectException, SocketTimeoutException, UnknownHostException}

import scala.xml._
import scalaj.http.Http

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
                    pubDate: Option[String]
                   ) extends rssNode

  def processItemNode(n: Node): Record = {
    Record((n \\ "title").text,
      (n \\ "link").text,
      (n \\ "description").text,
      Some((n \\ "category").text),
      Some(n \\ "enclosure"),
      Some((n \\ "guid").text),
      Some((n \\ "pubDate").text)
    )
  }

  // todo handle XML escape characters like &amp; &quot; &apos; &lt; &gt;
  def main(args: Array[String]): Unit = {
//    val response = Http("https://rg.ru/xml/index.xml")
//    val response = Http("https://russian.rt.com/rss")
//    val response = Http("http://feeds.bbci.co.uk/news/world/rss.xml")
//  fontanka need conversion from windows-1251
    val response = Http("http://www.fontanka.ru/fontanka.rss")
//    val response = Http("http://fontanka.ru/fontanka.rss") // Page moved
//    val response = Http("http://localhost/fontanka.rss") // Connection refused
    .timeout(connTimeoutMs = 3000, readTimeoutMs = 10000)

    try {
      response.asBytes.code match {
        case 200 => println("Ok")
        case 301 =>
          // "http://fontanka.ru/fontanka.rss"
          println(s"Page moved: ${response.asString.body}")
          System.exit(1)
        case 404 => println(s"Page not found: ${response.url}"); System.exit(1)
        case n: Int => println(s"${response.asString}\nResponse code: $n"); System.exit(1)
      }
    } catch {
      case e: UnknownHostException => println(s"Host not found: ${e.getMessage}"); System.exit(1)
      case e: SocketTimeoutException => println(s"$e URL:${response.url}"); System.exit(1)
      case e: ConnectException => println(s"$e URL:${response.url}"); System.exit(1)
    }

    val declAttrs: Map[String,String] = response.asString.body
      .split("\\?")(1).split(" ").toList
      .filter(_ != "xml")
      .map((x) => {val kv=x.split("="); (kv(0).toLowerCase(),kv(1).replace("\"", "")) })
      .toMap
    // TODO there should be a way to get encoding from XML declaration
    val xml = XML
        .load(new InputStreamReader(
          new ByteArrayInputStream(response.asBytes.body), declAttrs getOrElse ("encoding", "UTF-8"))
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
