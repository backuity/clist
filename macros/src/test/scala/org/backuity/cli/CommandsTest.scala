package org.backuity.cli

import org.backuity.cli.Cli._
import org.backuity.matchete.{Matcher, JunitMatchers}
import org.junit.Test

class CommandsTest extends JunitMatchers {

  import CommandsTest._

  @Test
  def commandsShouldGuessGlobalOptions(): Unit = {
    val cmds = Commands(Run, Show, Graph)
    cmds.options must contain(
      option("opt1"),
      option("opt2"),
      option("season"))
  }

  @Test
  def singleCommandCommands(): Unit = {
    Commands(Run).options must contain(
      option("run-specific"),
      option("opt1"),
      option("opt2"),
      option("season"))
  }

  @Test
  def emptyCommand(): Unit = {
    Commands() must throwAn[IllegalArgumentException].withMessage(
      "Commands must have at least one command")
  }

  def option(name: String): Matcher[CliOption[_]] = partialFunctionMatcher(s"option $name") {
    case opt => opt.name must_== name
  }
}

object CommandsTest {

  trait GlobalOptions { this: Command =>
    var opt1 = opt[Boolean]()
    var opt2 = opt[String](default = "pouette")

    var season = opt[Season](default = Season.WINTER)
  }

  trait SomeCategoryOptions extends GlobalOptions {
    this: Command =>
    var optA = opt[Int](default = 1)
    var optB = opt[Boolean]()
  }

  object Run extends Command with SomeCategoryOptions {
    var runSpecific = opt[Long](default = 123L)
  }
  object Show extends Command with GlobalOptions
  object Graph extends Command with SomeCategoryOptions
}
