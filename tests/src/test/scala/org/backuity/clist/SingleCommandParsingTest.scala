package org.backuity.clist

import java.lang.System.{lineSeparator => crlf}

import org.backuity.matchete.JunitMatchers
import org.junit.Test


class SingleCommandParsingTest extends JunitMatchers with ExitMatchers {

  import SingleCommandParsingTest._

  implicit val console = new Console.InMemory

  @Test
  def parseOptionalArgument(): Unit = {
    Cli.parse(Array()).withCommand(new RunOptional) { run =>
      run.target must_== None
    }
  }

  @Test
  def parse(): Unit = {
    Cli.parse(Array("stuff")).withCommand(new Run) { run =>
      run.target must_== "stuff"
    }
  }

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
  def parseMultiArgs(): Unit = {
    Cli.parse(Array("-a", "arg1", "arg2")).withCommand(new MultiArgs) { cmd =>
      cmd.names must_== Seq("arg1", "arg2")
      cmd.a must beTrue
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
  def failMissingMultipleArgs(): Unit = {
    Cli.parse(Array()).throwExceptionOnError().withCommand(new MultiArgs)() must throwA[ParsingException].withMessage(
      "Insufficient arguments for names")
  }

  @Test
  def parseCommandMustSetDefaults(): Unit = {
    Cli.parse(Array("--1", "sun")).withCommand(new RunWithOption) { run =>
      run.target must_== "sun"
      run.opt1 must_== true
      run.opt2 must_== "haha"
    }

    Cli.parse(Array("--opt2=another", "sea")).withCommand(new RunWithOption) { run =>
      run.target must_== "sea"
      run.opt1 must_== false
      run.opt2 must_== "another"
    }

    Cli.parse(Array("--opt2=toto", "--1", "sea")).withCommand(new RunWithOption) { run =>
      run.target must_== "sea"
      run.opt1 must_== true
      run.opt2 must_== "toto"
    }

    Cli.parse(Array("nice")).withCommand(new RunWithOption) { run =>
      run.target must_== "nice"
      run.opt1 must_== false
      run.opt2 must_== "haha"
    }
  }

  @Test
  def parseStaticCommandMustResetDefaults(): Unit = {
    object StaticRun extends RunWithOption

    Cli.parse(Array("--1", "sun")).withCommand(StaticRun)()
    StaticRun.target must_== "sun"
    StaticRun.opt1 must_== true
    StaticRun.opt2 must_== "haha"

    Cli.parse(Array("--opt2=another", "sea")).withCommand(StaticRun)()
    StaticRun.target must_== "sea"
    StaticRun.opt1 must_== false
    StaticRun.opt2 must_== "another"

    Cli.parse(Array("--opt2=toto", "--1", "sea")).withCommand(StaticRun)()
    StaticRun.target must_== "sea"
    StaticRun.opt1 must_== true
    StaticRun.opt2 must_== "toto"

    Cli.parse(Array("nice")).withCommand(StaticRun)()
    StaticRun.target must_== "nice"
    StaticRun.opt1 must_== false
    StaticRun.opt2 must_== "haha"
  }

  @Test
  def parseAbbrev(): Unit = {
    Cli.parse(Array()).withCommand(new RunWithAbbrev) { run =>
      run.opt1 must beFalse
      run.a must beFalse
    }

    Cli.parse(Array("-o")).withCommand(new RunWithAbbrev) { run =>
      run.opt1 must beTrue
    }

    Cli.parse(Array("-a")).withCommand(new RunWithAbbrev) { run =>
      run.a must beTrue
    }
  }

  @Test
  def abbrevOnlyShouldFailForLongSyntax(): Unit = {
    Cli.parse(Array("--a")).throwExceptionOnError().withCommand(new RunWithAbbrev)() must throwA[ParsingException].withMessage(
      "No option found for --a")
  }

  @Test
  def doNotReuseTheSameOptionMoreThanOnce(): Unit = {
    Cli.parse(Array("-o", "--opt1")).throwExceptionOnError().withCommand(new RunWithAbbrev)() must throwA[ParsingException].withMessage(
      "No option found for --opt1")
  }

  @Test
  def incorrectCommandOption(): Unit = {
    Cli.parse(Array("--unknown-option", "target")).withCommand(new Run)() must exitWithCode(1)
    console.content must_== (Usage.Default.show("x",Commands(new Run)) + crlf +
      "No option found for --unknown-option" + crlf)
  }
}

object SingleCommandParsingTest {
  class Run extends Command {
    var target = arg[String](name = "the-target")
  }

  class RunOptional extends Command {
    var target = arg[Option[String]](required = false)
  }

  class RunWithOption extends Command {
    var target = arg[String]()
    var opt1 = opt[Boolean](name = "1")
    var opt2 = opt[String](default = "haha")
  }

  // TODO test abbrev not allowed for non boolean types

  class RunWithAbbrev extends Command {
    var opt1 = opt[Boolean](abbrev = "o")
    var a = opt[Boolean](abbrevOnly = "a")
  }

  class MultiArgs extends Command {
    var names = args[Seq[String]]()
    var a = opt[Boolean](abbrev = "a")
  }

  class MultiArgAttributes extends Command {
    var argOne = arg[String]()
    var arg2 = arg[String]()
    var other = arg[Int]()
  }
}