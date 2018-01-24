import org.scalatest.{BeforeAndAfter, FunSuite}

// See http://doc.scalatest.org/3.0.1/#org.scalatest.FunSuite
class SimpleTest extends FunSuite with BeforeAndAfter {

  before {

  }

  after {

  }

  test("should pass") {
    println("Test Passed")
    assert(1 == 1)
  }

  test("Should be implemented") (pending)

  ignore("Should be ignored") {
    fail()
  }

}
