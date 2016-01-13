package org.backuity.clist

import java.io.File

import org.backuity.matchete.JunitMatchers
import org.junit.Test

class CommandAttributesTest extends JunitMatchers {

  import CommandAttributesTest._

  @Test
  def multiArgs(): Unit = {
    Cat.arguments must_== List(MultipleCliArgument(
      tpe = classOf[List[File]],
      commandAttributeName = "files",
      name = "files",
      description = Some("some files"))(implicitly[ReadMultiple[List[File]]]))
  }

  @Test
  def argsOrderMustBeDeclarationOrder(): Unit = {
    MultiArgsAndSingleArg.arguments.map(_.name) must_== List("className", "otherArg", "classArgs")
  }

  @Test
  def intArg(): Unit = {
    new IntArg // used to throw an exception because `null.asInstanceOf[Int]` produces 0
  }
}

object CommandAttributesTest {
  class IntArg extends Command {
    var a = arg[Int]()
  }

  object Cat extends Command {
    var files = args[List[File]](description = "some files")
  }

  object MultiArgsAndSingleArg extends Command {

    var className = arg[String]()
    var otherArg = arg[String]()
    var classArgs = args[List[String]]()
  }
}