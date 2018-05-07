import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import scala.util.parsing.json.JSON
import org.scalatest.FunSuite

class ScalaTests extends  FunSuite {

  test("fasterxml should parse JSON") {

    val data = """
    {"some": "json data"}
"""

    val mapper = new ObjectMapper

    mapper.registerModule(DefaultScalaModule)
    val v: Map[String, String] = mapper.readValue(data, classOf[Map[String, String]])
    val z: Map[String, String] = JSON.parseFull(data).get.asInstanceOf[Map[String,String]]
    assert(v === z)
  }

}
