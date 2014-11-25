import org.backuity.cli._
import Cli.{arg,opt}
import org.backuity.matchete.JunitMatchers
import org.junit.Test


class SingleCommandParsingTest extends JunitMatchers {

  object Run extends Command {
    var opt1 = opt[Boolean](name = "1")
    var opt2 = opt[String](default = "haha")
  }

  @Test
  def parseSingleCommand(): Unit = {
    Cli.parse(Array("--1")).withCommand(Run) {
      Run.opt1 must_== true
      Run.opt2 must_== "haha"
    }

    Cli.parse(Array("--opt2=another")).withCommand(Run) {
      Run.opt1 must_== false
      Run.opt2 must_== "another"
    }
  }
}
