def f( x: Int): Int = x * x
def s = "Hello"

val fff = new Function1[Int, Int] {
  def apply(v1: Int): Int =  f(v1)
}

val sss = new Function0[String] {
  def apply: String = s
}

fff(3)
sss
sss()
