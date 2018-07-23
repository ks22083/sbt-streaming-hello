package scalaex

// tutorial http://allaboutscala.com/tutorials/chapter-2-learning-basics-scala-programming/learn-to-create-use-enumerations/
// criticism : https://underscore.io/blog/posts/2014/09/03/enumerations.html
object  Weekdays extends  Enumeration {
  type Weekdays = Value
  val Mon = Value(1, "понедельник")
  val Tue, Wed, Thu, Fri, Sat, Sun = Value


}

object ScalaEnum {

  /**
    * Generates the graph using the given dataset and markup.
    *
    * @param args When set to true, performs a sort of 'debug' trace, which takes a shitload of memory and doesnt write out the graph
    *             asdf asdfasdf alksdjhf alksdhf alksdhj
    *             skdfkajhsdf
    *
    *  @return
    */
  def main(args: Array[String]): Unit = {
    val day = Weekdays.Mon

    Weekdays.values.foreach(println)

    val whatIs:Weekdays.Value =  Weekdays(2)
    println(whatIs)

  }
}
