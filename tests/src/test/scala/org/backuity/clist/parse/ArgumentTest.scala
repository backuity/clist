package org.backuity.clist.parse

import org.backuity.clist._
import org.junit.Test

class ArgumentTest extends ClistTestBase {

  import ArgumentTest._

  @Test
  def parseNamedArg(): Unit = {
    Cli.parse(Array("--the-target=stuff")).throwExceptionOnError().withCommand(new Run) { run =>
      run.target must_== "stuff"
    }
  }

  @Test
  def parseNamedArgAtTheEnd(): Unit = {
    Cli.parse(Array("--1", "--opt2=hehe", "--target=stuff")).throwExceptionOnError().withCommand(new RunWithOption) { run =>
      run.target must_== "stuff"
      run.opt1 must beTrue
      run.opt2 must_== "hehe"
    }
  }

  @Test
  def parseMultiArgAttributes(): Unit = {
    Cli.parse(Array("one", "two", "3")).withCommand(new MultiArgAttributes) { cmd =>
      cmd.argOne must_== "one"
      cmd.arg2 must_== "two"
      cmd.other must_== 3
    }
  }

  @Test
  def parseSameArgumentMultipleTimes(): Unit = {
    Cli.parse(Array("same", "same", "3")).withCommand(new MultiArgAttributes) { cmd =>
      cmd.argOne must_== "same"
      cmd.arg2 must_== "same"
      cmd.other must_== 3
    }
  }
}

object ArgumentTest {
  class Run extends Command {
    var target = arg[String](name = "the-target")
  }

  class RunWithOption extends Command {
    var target = arg[String]()
    var opt1 = opt[Boolean](name = "1")
    var opt2 = opt[String](default = "haha")
  }

  class MultiArgAttributes extends Command {
    var argOne = arg[String]()
    var arg2 = arg[String]()
    var other = arg[Int]()
  }
}