package org.backuity.clist

import java.io.File

import org.backuity.matchete.JunitMatchers
import org.junit.Test

class ReadMultipleTest extends JunitMatchers {

  @Test
  def readMultipleString(): Unit = {
    implicitly[ReadMultiple[List[String]]].reads(List("a", "b", "c")) must_==
      List("a", "b", "c")
  }

  @Test
  def readMultipleFiles(): Unit = {
    implicitly[ReadMultiple[Set[File]]].reads(List("a", "b", "c")) must_==
      Set(new File("a"), new File("b"), new File("c"))
  }

}
