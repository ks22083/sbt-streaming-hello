package scalaex

// tutorial http://allaboutscala.com/tutorials/chapter-2-learning-basics-scala-programming/learn-to-create-use-enumerations/
// criticism : https://underscore.io/blog/posts/2014/09/03/enumerations.html
object  Weekdays extends  Enumeration {
  type Weekdays = Value
  val Mon = Value(1, "понедельник")
  val Tue, Wed, Thu, Fri, Sat, Sun = Value


}

object ScalaEnum {

  def main(args: Array[String]): Unit = {
    import scalaex.Weekdays._
    val day = Mon

    Weekdays.values.foreach(println)

    val whatIs:Weekdays.Value =  Weekdays(2)
    println(whatIs)

  }
}
