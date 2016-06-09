package org.backuity.clist.parse

import java.lang.System.{lineSeparator => crlf}

import org.backuity.ansi.AnsiFormatter.FormattedHelper
import org.backuity.clist.{Commands, _}
import org.junit.Test

class MultipleCommandTest extends ClistTestBase {

  import MultipleCommandTest._

  @Test
  def parseMultipleCommands(): Unit = {
    Cli.parse(Array("--1", "--season=summer", "run", "--run-specific=456", "-B", "stuff")).withCommands(Run, Show) must_== Some(Run)
    Run.opt1 must beTrue
    Run.opt2 must_== "haha"
    Run.target must_== "stuff"
    Run.runSpecific must_== 456
    Run.season must_== Season.SUMMER
    Run.optB must beTrue
  }

  @Test
  def noOptionCommand(): Unit = {
    Cli.parse(Array("cho")).withCommands(Run,Show,Dry) must_== Some(Show)
  }

  @Test
  def version(): Unit = {
    Cli.parse(Array("version")).version("1.2.3").withCommands(Run,Show,Dry) must_== None
    console.content must_== ("1.2.3" + crlf)
  }

  @Test
  def exitOnError(): Unit = {
    Cli.parse(Array("incorrect")).exitCode(12).withCommands(Run,Show,Dry) must exitWithCode(12)
  }

  @Test
  def showUsageOnError(): Unit = {
    Cli.parse(Array("incorrect"))
        .throwExceptionOnError()
        .withProgramName("x")
        .withCommands(Run,Show,Dry) must throwA[ParsingException]

    console.content must_== (Usage.Default.show("x",Commands(Run,Show,Dry)) + crlf)
  }

  @Test
  def overrideVersionCommand(): Unit = {
    Cli.parse(Array("--vers")).version("1.0.x", command = "--vers").withCommands(Run,Show,Dry) must_== None
    console.content must_== ("1.0.x" + crlf)
  }
}


object MultipleCommandTest {

  sealed trait GlobalOptions { this : Command =>
    var opt1 = opt[Boolean](name = "1",
      description = "This is a wonderful command")
    var opt2 = opt[String](
      description = "Man you should try this one",
      default = "haha")

    var season = opt[Season](default = Season.WINTER)
  }

  trait SomeCategoryOptions extends GlobalOptions { this : Command =>
    var optA = opt[Int](name = "A", default = 1)
    var optB = opt[Boolean](description = "some flag", abbrev = "B")
  }

  object Run extends Command with SomeCategoryOptions {
    var target = arg[String]()

    var runSpecific = opt[Long](default = 123L)
  }

  object Show extends Command(name = "cho",
    description = "show the shit!") with GlobalOptions {
  }

  object Dry extends Command with SomeCategoryOptions

}
