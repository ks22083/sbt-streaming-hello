package bigdata.scaping.xml

import scala.xml.{Node, NodeSeq, XML}
import scalaj.http.Http

object Simple {

  case class Record(title: String,
                    link: String,
                    description: String,
                    category: String,
                    enclosure: Option[NodeSeq],
                    guid: String,
                    pubDate: String
                   )

  // todo handle XML escape characters like &amp; &quot; &apos; &lt; &gt;
  def processItemNode(n: Node): Record = {
    Record((n \\ "title").text,
      (n \\ "link").text,
      (n \\ "description").text,
      (n \\ "category").text,
      Some(n \\ "enclosure"),
      (n \\ "guid").text,
      (n \\ "pubDate").text
    )
  }

  def main(args: Array[String]): Unit = {
    val response = Http("https://rg.ru/xml/index.xml")
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
      .foreach(x => println(x.pubDate, x.category, x.title, x.enclosure.get))
  }
}
