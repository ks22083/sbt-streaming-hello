package bigdata.scaping.xml

import scala.xml.{Node, NodeSeq, XML}
import scalaj.http.Http

object Simple {

  case class Record(title: String,
                    link: String,
                    description: String,
                    category: Option[String],
                    enclosure: Option[NodeSeq],
                    guid: Option[String],
                    pubDate: Option[String]
                   )

  // todo handle XML escape characters like &amp; &quot; &apos; &lt; &gt;
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

  def main(args: Array[String]): Unit = {
//    val response = Http("https://rg.ru/xml/index.xml")
//    val response = Http("https://russian.rt.com/rss")
    val response = Http("http://feeds.bbci.co.uk/news/world/rss.xml")
    .timeout(connTimeoutMs = 3000, readTimeoutMs = 10000)
    .asString

    val xml = XML.loadString(response.body)
    //println(response)

    case class ChannelInfo(title: String, link: String,
                           description: String, language: String,
                           imageURL:String
                          )

    val chanNode: NodeSeq = xml \\ "rss" \ "channel"
    chanNode.head.child
      .filter(x => x.label != "item" && !x.isAtom)
      .foreach(println)

    val chanInfo  = ChannelInfo(
      (chanNode \ "title").text,
      (chanNode \ "link").text,
      (chanNode \ "description").text,
      (chanNode \ "language").text,
      (chanNode \ "image" \ "url" ).text
    )

    println(chanInfo+"\n")

    val itemNodes = xml \\ "item"
    itemNodes.take(1).foreach(println)

    itemNodes
      .take(1)
      .map(processItemNode)
      .foreach(x => println(x.pubDate.get, x.category.get, x.title, x.enclosure.get))

    println(itemNodes
      .take(1)
      .map(processItemNode).head)

  }
}
