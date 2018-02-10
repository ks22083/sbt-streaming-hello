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
    val testStr = "foo bar"

    try {
      Simple.parsePubDate(testStr)
      fail("Exception was expected")
    } catch {
      case e: Exception => assert (1 === 1) //pass
    }
  }

  test("Should be implemented") (pending)

  ignore("Should be ignored") {
    fail()
  }

}
