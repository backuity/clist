import org.backuity.cli.Cli._
import org.backuity.cli._
import org.backuity.matchete.JunitMatchers
import org.junit.Test

class MultipleCommandParsingTest extends JunitMatchers {

  import MultipleCommandParsingTest._

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
    Cli.parse(Array("--1")).withCommands(Run,Show,Dry) must throwA[ParsingException].withMessage(
      "No command found, expected one of cho, dry, run")
    Cli.parse(Array()).withCommands(Run,Show,Dry) must throwA[ParsingException].withMessage(
      "No command found, expected one of cho, dry, run")
  }

  @Test
  def incorrectArgument_Enum(): Unit = {
    Cli.parse(Array("--season=summr", "cho")).withCommands(Run,Show,Dry) must throwA[ParsingException].withMessage(
      "Incorrect parameter season 'summr', expected one of autumn,spring,summer,winter")
  }

  @Test
  def noOptionCommand(): Unit = {
    Cli.parse(Array("cho")).withCommands(Run,Show,Dry) must_== Some(Show)
  }

  @Test
  def wrongCommand(): Unit = {
    Cli.parse(Array("baaad")).withCommands(Run,Show,Dry) must throwA[ParsingException].withMessage(
      "Unknown command 'baaad'")
  }

  @Test
  def version(): Unit = {
    implicit val console = new Console.InMemory
    Cli.parse(Array("version")).version("1.2.3").withCommands(Run,Show,Dry) must_== None
    console.toString must_== ("1.2.3" + System.lineSeparator())
  }

  @Test
  def exitOnError(): Unit = {
    implicit val exit = Exit.withException
    Cli.parse(Array("incorrect")).exitCode(12).withCommands(Run,Show,Dry) must throwAn[ExitException].`with`("error code 12") {
      case ExitException(12) =>
    }
  }

  @Test
  def showUsageOnError(): Unit = {
    implicit val exit = Exit.withException
    implicit val console = new Console.InMemory
    Cli.parse(Array("incorrect")).throwExceptionOnError().withCommands(Run,Show,Dry) must throwA[ParsingException]
    console.toString must_== (Usage.Default.show(Commands(Run,Show,Dry)) + System.lineSeparator())
  }

  @Test
  def overrideVersionCommand(): Unit = {
    implicit val console = new Console.InMemory
    Cli.parse(Array("--vers")).version("1.0.x", command = "--vers").withCommands(Run,Show,Dry) must_== None
    console.toString must_== ("1.0.x" + System.lineSeparator())
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
