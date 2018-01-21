import org.scalatest.FunSuite

class SimpleTest extends FunSuite {

  test("should pass") {
    print("test passed")
    assert(condition = true)
  }

  ignore("Should be ignored") {
    fail()
  }

}
