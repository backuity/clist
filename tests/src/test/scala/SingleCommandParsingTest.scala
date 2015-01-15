import org.backuity.cli._
import Cli.{arg,opt}
import org.backuity.matchete.JunitMatchers
import org.junit.Test

import java.lang.System.{lineSeparator => crlf}


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
  def parseCommandMustSetDefaults(): Unit = {
    Cli.parse(Array("sun", "--1")).withCommand(new RunWithOption) { run =>
      run.target must_== "sun"
      run.opt1 must_== true
      run.opt2 must_== "haha"
    }

    Cli.parse(Array("sea", "--opt2=another")).withCommand(new RunWithOption) { run =>
      run.target must_== "sea"
      run.opt1 must_== false
      run.opt2 must_== "another"
    }

    Cli.parse(Array("sea", "--opt2=toto", "--1")).withCommand(new RunWithOption) { run =>
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
    Cli.parse(Array("sun", "--1")).withCommand(StaticRun)()
    StaticRun.target must_== "sun"
    StaticRun.opt1 must_== true
    StaticRun.opt2 must_== "haha"

    Cli.parse(Array("sea", "--opt2=another")).withCommand(StaticRun)()
    StaticRun.target must_== "sea"
    StaticRun.opt1 must_== false
    StaticRun.opt2 must_== "another"

    Cli.parse(Array("sea", "--opt2=toto", "--1")).withCommand(StaticRun)()
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
    Cli.parse(Array("target", "--unknown-option")).withCommand(new Run)() must exitWithCode(1)
    console.content must_== (Usage.Default.show(Commands(new Run)) + crlf +
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

  object StaticRun extends RunWithOption
}