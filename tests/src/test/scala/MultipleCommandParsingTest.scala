import org.backuity.cli.Cli._
import org.backuity.cli.{Cli, Command}
import org.backuity.matchete.JunitMatchers
import org.junit.Test

class MultipleCommandParsingTest extends JunitMatchers {

  import MultipleCommandParsingTest._

  @Test
  def parseMultipleCommands(): Unit = {
    Cli.parse(Array("--1", "--season=summer", "run","stuff", "--runSpecific=456", "-B")).withCommands(Run, Show) must_== Run
    Run.opt1 must beTrue
    Run.opt2 must_== "haha"
    Run.target must_== "stuff"
    Run.runSpecific must_== 456
    Run.season must_== Season.SUMMER
    Run.optB must beTrue
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

  object Test extends Command with SomeCategoryOptions

}
