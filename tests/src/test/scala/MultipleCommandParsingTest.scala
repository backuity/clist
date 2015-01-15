import java.lang.System.{lineSeparator => crlf}

import org.backuity.cli.Cli._
import org.backuity.cli._
import org.backuity.matchete.JunitMatchers
import org.junit.Test

class MultipleCommandParsingTest extends JunitMatchers with ExitMatchers {

  import MultipleCommandParsingTest._

  implicit val console = new Console.InMemory

  @Test
  def parseMultipleCommands(): Unit = {
    Cli.parse(Array("--1", "--season=summer", "run","stuff", "--runSpecific=456", "-B")).withCommands(Run, Show) must_== Some(Run)
    Run.opt1 must beTrue
    Run.opt2 must_== "haha"
    Run.target must_== "stuff"
    Run.runSpecific must_== 456
    Run.season must_== Season.SUMMER
    Run.optB must beTrue
  }

  @Test
  def noCommandSpecified(): Unit = {
    Cli.parse(Array("--1")).throwExceptionOnError().withCommands(Run,Show,Dry) must throwA[ParsingException].withMessage(
      "No command found, expected one of cho, dry, run")
    Cli.parse(Array()).throwExceptionOnError().withCommands(Run,Show,Dry) must throwA[ParsingException].withMessage(
      "No command found, expected one of cho, dry, run")
  }

  @Test
  def incorrectArgument_Enum(): Unit = {
    Cli.parse(Array("--season=summr", "cho")).throwExceptionOnError().withCommands(Run,Show,Dry) must throwA[ParsingException].withMessage(
      "Incorrect parameter season 'summr', expected one of autumn,spring,summer,winter")
  }

  @Test
  def noOptionCommand(): Unit = {
    Cli.parse(Array("cho")).withCommands(Run,Show,Dry) must_== Some(Show)
  }

  @Test
  def wrongCommandShouldPrintErrorAndExit(): Unit = {
    Cli.parse(Array("baaad")).noUsageOnError().exitCode(123).withCommands(Run,Show,Dry) must exitWithCode(123)
    console.content must_== ("Unknown command 'baaad'" + crlf)
  }

  @Test
  def wrongCommandShouldThrowAParsingException(): Unit = {
    Cli.parse(Array("baaad")).throwExceptionOnError().withCommands(Run,Show,Dry) must throwA[ParsingException].withMessage(
      "Unknown command 'baaad'")
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
    Cli.parse(Array("incorrect")).throwExceptionOnError().withCommands(Run,Show,Dry) must throwA[ParsingException]
    console.content must_== (Usage.Default.show(Commands(Run,Show,Dry)) + crlf)
  }

  @Test
  def overrideVersionCommand(): Unit = {
    Cli.parse(Array("--vers")).version("1.0.x", command = "--vers").withCommands(Run,Show,Dry) must_== None
    console.content must_== ("1.0.x" + crlf)
  }

  @Test
  def incorrectCommandOption(): Unit = {
    Cli.parse(Array("run", "target", "--unknown-option")).withCommands(Run,Show,Dry) must exitWithCode(1)
    console.content must_== (Usage.Default.show(Commands(Run,Show,Dry)) + crlf +
      "No option found for --unknown-option" + crlf)
  }
}


object MultipleCommandParsingTest {

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
