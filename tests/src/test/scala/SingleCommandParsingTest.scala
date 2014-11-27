import org.backuity.cli._
import Cli.{arg,opt}
import org.backuity.matchete.JunitMatchers
import org.junit.Test


class SingleCommandParsingTest extends JunitMatchers {

  object Run extends Command {
    var target = arg[String](name = "the-target")
  }

  object RunOptional extends Command {
    var target = arg[Option[String]](required = false)
  }

  object RunWithOption extends Command {
    var target = arg[String]()
    var opt1 = opt[Boolean](name = "1")
    var opt2 = opt[String](default = "haha")
  }

  @Test
  def parseOptionalArgument(): Unit = {
    Cli.parse(Array()).withCommand(RunOptional) {
      RunOptional.target must_== None
    }
  }

  @Test
  def parse(): Unit = {
    Cli.parse(Array("stuff")).withCommand(Run) {
      Run.target must_== "stuff"
    }
  }

  @Test
  def parseOptions(): Unit = {
    Cli.parse(Array("sun","--1")).withCommand(RunWithOption) {}
    RunWithOption.target must_== "sun"
    RunWithOption.opt1 must_== true
    RunWithOption.opt2 must_== "haha"

    Cli.parse(Array("sea", "--opt2=another")).withCommand(RunWithOption) {}
    RunWithOption.target must_== "sea"
    RunWithOption.opt1 must_== false
    RunWithOption.opt2 must_== "another"
  }
}
