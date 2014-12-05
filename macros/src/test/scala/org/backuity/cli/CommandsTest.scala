package org.backuity.cli

import org.backuity.cli.Cli._
import org.backuity.matchete.JunitMatchers
import org.junit.Test

class CommandsTest extends JunitMatchers {

  import CommandsTest._

  @Test
  def commandsShouldGuessGlobalOptions(): Unit = {
    val cmds = Commands(Run, Show, Graph)
    cmds.options must contain(
      anOption("opt1"),
      anOption("opt2"),
      anOption("season"))
  }

  def anOption(name : String) = an[CliOption[_]](s"argument named $name") {
    case opt : CliOption[_] => opt.name must_== name
  }
}

object CommandsTest {

  trait GlobalOptions { this : Command =>
    var opt1 = opt[Boolean]()
    var opt2 = opt[String](default = "pouette")

    var season = opt[Season](default = Season.WINTER)
  }

  trait SomeCategoryOptions extends GlobalOptions { this : Command =>
    var optA = opt[Int](default = 1)
    var optB = opt[Boolean]()
  }

  object Run extends Command with SomeCategoryOptions {
    var runSpecific = opt[Long](default = 123L)
  }
  object Show extends Command with GlobalOptions
  object Graph extends Command with SomeCategoryOptions
}
