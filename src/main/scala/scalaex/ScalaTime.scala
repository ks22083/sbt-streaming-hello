package exit

import java.text.SimpleDateFormat
import java.time.{LocalDate, LocalDateTime}
import java.time.format.DateTimeFormatter
import java.util.Calendar

import com.typesafe.scalalogging.Logger
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

object ScalaTime {

  val LOGGER = Logger[ScalaTime]
  /**
  Generates the graph using the given dataset and markup.
    NOTE: This should only be called when a cached version is unavailable
    param dataLocator Dataset location, other tags for writing trion
  @param args When set to true, performs a sort of 'debug' trace, which takes a shitload of memory and doesnt write out the graph

  @return
    */
  def main(args: Array[String]): Unit = {

    println(s"timestamp func: ${timestamp()}")

    val tstamp = DateTimeFormat
      .forPattern("HH:mm:ss.SSS")
      .print(DateTime.now())
    println(s"simple way: ${tstamp}")
    LOGGER.warn(s"simple way: ${tstamp}")

    val tstampj8 = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss.SSS"))
    println(s"Java8: $tstampj8")
//    var allSuitesPassed = true
    val lst = Seq(0, 0, 0, 1, 0)

    val lst2 = lst.takeWhile( _ == 0 )
    val lst3: Option[Int] = lst.find( _ != 0)

    import util.control.Breaks.{break, breakable}
    breakable {
      lst.foreach { e =>
        if ( e != 0 )
          break
      }
    }
    println(lst)
    println(lst2)
//    for (e<-lst
//      if allSuitesPassed
//      if (e != 0)
//        allSuitesPassed = false

  }

  val format = new /*java.text.*/SimpleDateFormat("HH:mm:ss.SSS")
  val calendar = /*java.util.*/Calendar.getInstance()

  def timestamp(): String = {
//    val format = new java.text.SimpleDateFormat("HH:mm:ss.SSS")
//    val now = java.util.Calendar.getInstance().getTime()
    format.format(calendar.getTime)
  }
}

class ScalaTime {}