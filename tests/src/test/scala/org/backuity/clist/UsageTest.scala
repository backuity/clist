package org.backuity.clist

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
            |%underline{Options}
            |
            |   %yellow{--1}                                  : This is a wonderful command
            |   %yellow{--opt2=STRING}                        : Man you should try this one
            |                                          it really rocks
            |   %yellow{--season=winter|spring|summer|autumn}
            |
            |%underline{Commands}
            |
            |   %bold{cho} : show the shit!
            |
            |   %bold{dry} %yellow{[command options]}
            |      %yellow{--A=NUM}
            |      %yellow{--opt-b} : some flag
            |
            |   %bold{run} %yellow{[command options]} <target> [<opt-arg>] : run run baby run
            |      %yellow{--A=NUM}
            |      %yellow{--opt-b}            : some flag
            |      %yellow{--run-specific=NUM}
            |      <target> : the target to run
            |      <opt-arg> : an optional argument
            |""".stripMargin
  }

  @Test
  def singleCommandUsage(): Unit = {
    Usage.Default.show("program-name", Commands(Run)) must_==
      ansi"""%underline{Usage}
            |
            | %bold{run} %yellow{[options]} <target> [<opt-arg>] : run run baby run
            |
            |%underline{Options}
            |
            |   %yellow{--1}                                  : This is a wonderful command
            |   %yellow{--A=NUM}
            |   %yellow{--opt-b}                              : some flag
            |   %yellow{--opt2=STRING}                        : Man you should try this one
            |                                          it really rocks
            |   %yellow{--run-specific=NUM}
            |   %yellow{--season=winter|spring|summer|autumn}
            |
            |%underline{Arguments}
            |
            |   <target> : the target to run
            |   <opt-arg> : an optional argument
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

  @Test
  def doubleUsage(): Unit = {
    Usage.Default.show("stuff", Commands(DoubleCmd)) must_==
      ansi"""%underline{Usage}
            |
            | %bold{double-cmd} %yellow{[options]}
            |
            |%underline{Options}
            |
            |   %yellow{--dbl=NUM}
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
        var target = arg[String](description = "the target to run")
        var optArg = arg[Long](default = 42L, required = false, description = "an optional argument")

        var runSpecific = opt[Long](default = 123L)
    }

    object Show extends Command(name = "cho",
        description = "show the shit!") with GlobalOptions {
    }

    object Dry extends Command with SomeCategoryOptions

    object NoOptionCmd extends Command

    object DoubleCmd extends Command {
      var dbl = opt[Double]()
    }
}
