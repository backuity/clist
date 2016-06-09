package org.backuity.clist.util

import java.io.File
import java.util.Calendar

import org.backuity.clist.Season
import org.backuity.matchete.JunitMatchers
import org.junit.Test


class ReadTest extends JunitMatchers {

  @Test
  def javaEnum(): Unit = {
    implicitly[Read[Season]].reads("winter") must_== Season.WINTER
  }

  @Test
  def incorrectJavaEnum(): Unit = {
    implicitly[Read[Season]].reads("summr") must throwA[ReadException].suchAs {
      case ReadException(value, expected) =>
        expected must_== "one of autumn,spring,summer,winter"
        value must_== "summr"
    }
  }

  @Test
  def incorrectInt(): Unit = {
    implicitly[Read[Int]].reads("blabla") must throwA[ReadException].suchAs {
      case ReadException(value, expected) =>
        value must_== "blabla"
        expected must_== "an Int"
    }
  }

  @Test
  def incorrectLong(): Unit = {
    implicitly[Read[Long]].reads("blabla") must throwA[ReadException].suchAs {
      case ReadException(value, expected) =>
        value must_== "blabla"
        expected must_== "a Long"
    }
  }

  @Test
  def incorrectDouble(): Unit = {
    implicitly[Read[Double]].reads("blabla") must throwA[ReadException].suchAs {
      case ReadException(value, expected) =>
        value must_== "blabla"
        expected must_== "a Double"
    }
  }

  @Test
  def incorrectDate(): Unit = {
    implicitly[Read[Calendar]].reads("blabla") must throwA[ReadException].suchAs {
      case ReadException(value, expected) =>
        value must_== "blabla"
        expected must_== "a date formatted as yyyy-MM-dd"
    }
  }

  @Test
  def readTuple(): Unit = {
    implicitly[Read[(String,Int)]].reads("john=28") must_== ("john", 28)
  }

  @Test
  def readSequence(): Unit = {
    implicitly[Read[Seq[String]]].reads("this\tand    that") must_== Seq("this", "and", "that")
    implicitly[Read[Seq[Int]]].reads("1 2    3") must_== Seq(1, 2, 3)
  }

  @Test
  def readFile(): Unit = {
    implicitly[Read[File]].reads("/home/john/file") must_== new File("/home/john/file")
  }
}
