package org.backuity.cli

import org.backuity.cli.Cli._
import org.backuity.matchete.JunitMatchers
import org.junit.Test

class CommandsTest extends JunitMatchers {

  import CommandsTest._

  @Test
  def commandsShouldGuessGlobalOptions(): Unit = {
    val cmds = Commands(Run, Show, Graph)
    cmds.arguments must contain(
      anArgument("opt1"),
      anArgument("opt2"),
      anArgument("season"))
  }

  def anArgument(name : String) = an[CliArgument[_]](s"argument named $name") {
    case arg : CliArgument[_] => arg.name must_== Some(name)
  }
}

object CommandsTest {

  trait GlobalOptions { this : Command =>
    var opt1 = arg[Boolean]()
    var opt2 = arg[String](default = "pouette")

    var season = arg[JavaSeason](default = JavaSeason.WINTER)
  }

  trait SomeCategoryOptions extends GlobalOptions { this : Command =>
    var optA = arg[Int](default = 1)
    var optB = arg[Boolean]()
  }

  object Run extends Command with SomeCategoryOptions {
    var runSpecific = arg[Long](default = 123L)
  }

  object Show extends Command(name = "cho",
    description = "show the shit!") with GlobalOptions {
  }

  object Graph extends Command with SomeCategoryOptions

  object Season extends Enumeration {
    type Season = Value
    val WINTER, SPRING, SUMMER, AUTUMN = Value
  }
}
