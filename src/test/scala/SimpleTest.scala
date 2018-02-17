import java.util.Locale

import bigdata.scaping.xml.Simple
import org.scalatest.{BeforeAndAfter, FunSuite}

// See http://doc.scalatest.org/3.0.1/#org.scalatest.FunSuite
class SimpleTest extends FunSuite with BeforeAndAfter {

  before {

  }

  after {

  }

  test("should pass") {
    println("Test Passed")
    assert(1 === 1)
  }

  test("should parse all dates") {
    val testSet = Seq(
      "10 Feb 2018 16:37:28 GMT",
      "10 Feb 2018 16:24:27 +0300",
      "Sat, 10 Feb 2018 16:24:27 +0300",
      "Sat, 10 Feb 2018 16:18:04 GMT"
    )

    try {
      testSet.foreach(Simple.parsePubDate)
    } catch {
      case e: Exception =>
        e.printStackTrace()
        fail()
    }
  }

  test( "shouldn't parse this date" ) {
    val testDate = "foo bar"

    try {
      Simple.parsePubDate(testDate)
      fail("Exception was expected")
    } catch {
      case e: Exception => succeed //pass
    }
  }

  test("should throw exception") {
    val testDate = "foo bar"
    intercept[IllegalArgumentException] { Simple.parsePubDate(testDate) }
  }

  test("should parse item correctly") {
    val fullItem = <item xmlns:atom="http://www.w3.org/2005/Atom">
      <guid>https://lenta.ru/news/2018/02/15/vetropark/</guid>
      <title>«Росатом» и Республика Карелия построят ветропарк на берегу Белого моря</title>
      <link>https://lenta.ru/news/2018/02/15/vetropark/</link>
      <description>
        В рамках Российского инвестиционного форума в Сочи, глава Республики Карелии Артур Парфенчиков и генеральный директор АО «НоваВинд» (дивизиона Госкорпорации «Росатом» отвечающего за программы в новой энергетике) Александр Корчагин, обсудили перспективы проекта строительства ветропарка на берегу Белого моря.
      </description>
      <pubDate>Thu, 15 Feb 2018 17:42:14 +0300</pubDate>
      <enclosure type="image/jpeg" length="141868" url="https://icdn.lenta.ru/images/2018/02/15/17/20180215172354775/pic_23daa977d78b3e17da331ca1ddcdc7d2.jpg"/>
      <category>Экономика</category>
    </item>

    val result = Simple.processItemNode(fullItem)

    assert( result.title === "«Росатом» и Республика Карелия построят ветропарк на берегу Белого моря")
    assert( result.link === "https://lenta.ru/news/2018/02/15/vetropark/")
    assert( result.category.get === "Экономика")
//    assert( result.description.replaceAll("""^\s+(?m)""", "").replaceAll("""(?m)\s+$""", "") ===
    assert( result.description ===
      "В рамках Российского инвестиционного форума в Сочи, глава Республики Карелии Артур Парфенчиков и генеральный директор АО «НоваВинд» (дивизиона Госкорпорации «Росатом» отвечающего за программы в новой энергетике) Александр Корчагин, обсудили перспективы проекта строительства ветропарка на берегу Белого моря.")
    assert( result.pubDate.get.toString("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH) ===
      "Thu, 15 Feb 2018 17:42:14 +0300")
  }

  test("Should be implemented") (pending)

  ignore("Should be ignored") {
    fail()
  }

}
