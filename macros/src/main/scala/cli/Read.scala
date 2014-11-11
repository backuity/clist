package cli

trait Read[A] { self =>
  def arity: Int
  def tokensToRead: Int = if (arity == 0) 0 else 1
  def reads: String => A
  def map[B](f: A => B): Read[B] = new Read[B] {
    val arity = self.arity
    val reads = self.reads andThen f
  }
}
object Read {
  import java.io.File
  import java.net.URI
  import java.text.SimpleDateFormat
  import java.util.{Calendar, GregorianCalendar, Locale}
  def reads[A](f: String => A): Read[A] = new Read[A] {
    val arity = 1
    val reads = f
  }
  implicit val intRead: Read[Int] = reads { _.toInt }
  implicit val stringRead: Read[String] = reads { identity }
  implicit val doubleRead: Read[Double] = reads { _.toDouble }
  implicit val booleanRead: Read[Boolean] =
    reads { _.toLowerCase match {
      case "true" => true
      case "false" => false
      case "yes" => true
      case "no" => false
      case "1" => true
      case "0" => false
      case s =>
        throw new IllegalArgumentException("'" + s + "' is not a boolean.")
    }}
  implicit val longRead: Read[Long] = reads { _.toLong }
  implicit val bigIntRead: Read[BigInt] = reads { BigInt(_) }
  implicit val bigDecimalRead: Read[BigDecimal] = reads { BigDecimal(_) }
  implicit val yyyymmdddRead: Read[Calendar] = calendarRead("yyyy-MM-dd")
  def calendarRead(pattern: String): Read[Calendar] = calendarRead(pattern, Locale.getDefault)
  def calendarRead(pattern: String, locale: Locale): Read[Calendar] =
    reads { s =>
      val fmt = new SimpleDateFormat(pattern)
      val c = new GregorianCalendar
      c.setTime(fmt.parse(s))
      c
    }
  implicit val fileRead: Read[File] = reads { new File(_) }
  implicit val uriRead: Read[URI] = reads { new URI(_) }
  implicit def tupleRead[A1: Read, A2: Read]: Read[(A1, A2)] = new Read[(A1, A2)] {
    val arity = 2
    val reads = { (s: String) =>
      splitKeyValue(s) match {
        case (k, v) => implicitly[Read[A1]].reads(k) -> implicitly[Read[A2]].reads(v)
      }
    }
  }
  private def splitKeyValue(s: String): (String, String) =
    s.indexOf('=') match {
      case -1 => throw new IllegalArgumentException("Expected a key=value pair")
      case n: Int => (s.slice(0, n), s.slice(n + 1, s.length))
    }
  implicit val unitRead: Read[Unit] = new Read[Unit] {
    val arity = 0
    val reads = { (s: String) => () }
  }
}


