package org.backuity.clist
package parse

import java.lang.System.{lineSeparator => crlf}
import org.backuity.ansi.AnsiFormatter.FormattedHelper
import org.backuity.clist.util.ExitException
import org.junit.Test

class MultiCommandErrorTest extends ClistTestBase {

  import MultiCommandErrorTest._

  @Test
  def missingArgumentErrorMustDisplayContextualInformation(): Unit = {
    Cli.parse(Array("run")).noUsageOnError().withCommands(Run, Show) must throwAn[ExitException]
    console.content.trim must_== ansi"Failed to parse command %bold{run}: No argument provided for %bold{target}"
    console.clear()

    Cli.parse(Array("count", "blabla")).noUsageOnError().withCommands(Run, Count) must throwAn[ExitException]
    console.content.trim must_== ansi"Failed to parse command %bold{count}: Incorrect value for argument %bold{lines}, got 'blabla', expected an Int"
  }

  @Test
  def commandNotFound(): Unit = {
    Cli.parse(Array("unknown", "--verbose")).noUsageOnError().withCommands(Run,Show,Count) must throwAn[ExitException]
    console.content.trim must_== ansi"Unknown command 'unknown', expected one of %bold{cho}, %bold{count}, %bold{run}"
  }

  @Test
  def commandNotFound_SuggestSimilarCommands(): Unit = {
    Cli.parse(Array("cout", "--verbose")).noUsageOnError().withCommands(Run,Show,Count) must throwAn[ExitException]
    console.content.trim must_== ansi"Unknown command 'cout', did you mean %bold{count}?"
  }

  @Test
  def noCommandSpecified(): Unit = {
    Cli.parse(Array("--1")).throwExceptionOnError().withCommands(Run,Show,Count) must throwA[ParsingException].withMessage(
      ansi"No command found, expected one of %bold{cho}, %bold{count}, %bold{run}")
    Cli.parse(Array()).throwExceptionOnError().withCommands(Run,Show,Count) must throwA[ParsingException].withMessage(
      ansi"No command found, expected one of %bold{cho}, %bold{count}, %bold{run}")
  }

  @Test
  def incorrectArgument_Enum(): Unit = {
    Cli.parse(Array("--season=summr", "cho")).throwExceptionOnError().withCommands(Run,Show,Count) must throwA[ParsingException].withMessage(
      ansi"Failed to parse command %bold{cho}: Incorrect value for option %bold{season}, got 'summr', expected one of autumn,spring,summer,winter")
  }

  @Test
  def incorrectCommandOption(): Unit = {
    Cli.parse(Array("run", "--unknown-option", "target"))
      .withProgramName("x")
      .withCommands(Run,Show,Count) must exitWithCode(1)

    console.content must_== (Usage.Default.show("x",Commands(Run,Show,Count)) + crlf +
      ansi"Failed to parse command %bold{run}: No option found for --unknown-option" + crlf)
  }
}

object MultiCommandErrorTest {
  object Run extends Command(description = "run") {
    var target = arg[String]()
  }

  object Count extends Command(description = "one, two, three") {
    var lines = arg[Int]()
  }

  object Show extends Command(name = "cho", description = "show") {
    var season = opt[Season](default = Season.WINTER)
  }
}
