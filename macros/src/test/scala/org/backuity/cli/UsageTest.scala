package org.backuity.cli

import org.backuity.cli.Cli._
import org.backuity.matchete.JunitMatchers
import org.junit.Test

class UsageTest extends JunitMatchers {

  import UsageTest._
  import org.backuity.ansi.AnsiFormatter.FormattedHelper

  @Test
  def fullUsage(): Unit = {
      Usage.Default.show("program-name", Commands(Run, Show, Dry)) must_==
          ansi"""%underline{Usage}
            |
            | %bold{program-name} %yellow{[options]} %bold{command} %yellow{[command options]}
            |
            |%underline{Options:}
            |
            |   %yellow{--1}                                  : This is a wonderful command
            |   %yellow{--opt2=STRING}                        : Man you should try this one
            |                                          it really rocks
            |   %yellow{--season=winter|spring|summer|autumn}
            |
            |%underline{Commands:}
            |
            |   %bold{cho} : show the shit!
            |
            |   %bold{dry} %yellow{[command options]}
            |      %yellow{--A=NUM}
            |      %yellow{--opt-b} : some flag
            |
            |   %bold{run} <target> %yellow{[command options]} : run run baby run
            |      %yellow{--A=NUM}
            |      %yellow{--opt-b}            : some flag
            |      %yellow{--run-specific=NUM}
            |""".stripMargin
  }

  @Test
  def singleCommandUsage(): Unit = {
    Usage.Default.show("program-name", Commands(Run)) must_==
      ansi"""%underline{Usage}
            |
            | %bold{run} <target> %yellow{[options]} : run run baby run
            |
            |%underline{Options:}
            |
            |   %yellow{--1}                                  : This is a wonderful command
            |   %yellow{--A=NUM}
            |   %yellow{--opt-b}                              : some flag
            |   %yellow{--opt2=STRING}                        : Man you should try this one
            |                                          it really rocks
            |   %yellow{--run-specific=NUM}
            |   %yellow{--season=winter|spring|summer|autumn}
            |""".stripMargin
  }

  @Test
  def noOptionCmdUsage(): Unit = {
    Usage.Default.show("program-name", Commands(NoOptionCmd)) must_==
      ansi"""%underline{Usage}
            |
            | %bold{no-option-cmd}
            |""".stripMargin
  }

}

object UsageTest {
    trait GlobalOptions { this : Command =>
        var opt1 = opt[Boolean](name = "1",
            description = "This is a wonderful command")
        var opt2 = opt[String](description = "Man you should try this one\n"+
                                             "it really rocks",
            default = "haha")

        var season = opt[Season](default = Season.WINTER)
    }

    trait SomeCategoryOptions extends GlobalOptions { this : Command =>
        var optA = opt[Int](name = "A", default = 1)
        var optB = opt[Boolean](description = "some flag")
    }

    object Run extends Command(description = "run run baby run") with SomeCategoryOptions {
        var target = arg[String]()

        var runSpecific = opt[Long](default = 123L)
    }

    object Show extends Command(name = "cho",
        description = "show the shit!") with GlobalOptions {
    }

    object Dry extends Command with SomeCategoryOptions

    object NoOptionCmd extends Command
}