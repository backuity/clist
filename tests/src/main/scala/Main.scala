import cli.Cli.{Commands, Usage, Command}
import cli._

object Main {

  import Cli.arg

  trait GlobalOptions { this : Command =>
    var opt1 = arg[Boolean](name = "1",
                           description = "This is a wonderful command",
                           default = false)
    var opt2 = arg[String](name = "opt2",
                           abbrev = "2",
                           description = "Man you should try this one",
                           default = "haha")
  }
  
  trait SomeCategoryOptions extends GlobalOptions { this : Command =>
    var optA = arg[Int](name = "A", default = 1)
    var optB = arg[Boolean](description = "some flag",
                            default = true)
  }

  object Run extends Command with SomeCategoryOptions {
    var runSpecific = arg[Long](default = 123L)
  }

  object Show extends Command(name = "cho",
                              description = "show the shit!") with GlobalOptions {
  }

  def main(args: Array[String]) {

    println(Usage.Default.show(Commands(Run,Show)))
    
    Cli.parse(args).version("1.2").withCommand(Run) {
      println("Parsed with run : optB=" + Run.optB)
    }

    Cli.parse(args).version("1.2.3").withCommands(Run, Show) match {
      case Run =>
        println("specific : " + Run.runSpecific)
        println("opt1 : " + Run.opt1)

      case Show => println("show")
    }
  }
}
