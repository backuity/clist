package org.backuity.clist.util

trait Read[A] { self =>
  def reads: String => A
  def map[B](f: A => B): Read[B] = new Read[B] {
    val reads = self.reads andThen f
  }
}

case class ReadException(value: String, expected: String) extends RuntimeException

/**
 * Note that we do not support scala Enumeration as this would require having a runtime dependency
 * on the reflection API. On the other hand Java enums are supported.
 */
object Read {
  import java.io.File
  import java.net.URI
  import java.text.SimpleDateFormat
  import java.util.{Calendar, GregorianCalendar, Locale}
  def reads[A](f: String => A): Read[A] = new Read[A] {
    val reads = f
  }
  implicit val intRead: Read[Int] = reads { _.toInt }
  implicit val stringRead: Read[String] = reads { identity }
  implicit val doubleRead: Read[Double] = reads { _.toDouble }
  implicit val booleanRead: Read[Boolean] =
    reads { _.toLowerCase match {
      case "true" | "yes" | "1" => true
      case "false" | "no" | "0"      => false
      case s =>
        throw new IllegalArgumentException("'" + s + "' is not a boolean.")
    }}
  implicit val longRead: Read[Long] = reads { _.toLong }
  implicit val bigIntRead: Read[BigInt] = reads { BigInt(_) }
  implicit val bigDecimalRead: Read[BigDecimal] = reads { BigDecimal(_) }

  implicit def optionRead[T : Manifest : Read] : Read[Option[T]] = reads { s =>
    Some(implicitly[Read[T]].reads(s))
  }

  implicit def javaEnumRead[T <: Enum[T] : Manifest]: Read[T] = reads { s =>
    val values = manifest[T].runtimeClass.getEnumConstants.map(_.toString)
    if( !values.contains(s.toUpperCase)) {
      throw ReadException(value = s, expected = "one of " + values.sorted.mkString(",").toLowerCase)
    } else {
      Enum.valueOf(manifest[T].runtimeClass.asInstanceOf[Class[T]], s.toUpperCase)
    }
  }

  implicit def seqRead[T](implicit readT: Read[T]): Read[Seq[T]] = reads { str =>
    str.split("\\p{Blank}+").map { readT.reads }
  }

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
    val reads = { (s: String) => () }
  }
}


