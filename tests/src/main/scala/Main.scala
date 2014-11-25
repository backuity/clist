import cli.{Command,Commands,Cli,ArgumentBuilder,Usage}

object Main {

  import Cli.arg

  trait GlobalOptions { this : Command =>
    var opt1 = arg[Boolean](name = "1",
                           description = "This is a wonderful command")
    var opt2 = arg[String](abbrev = "2",
                           description = "Man you should try this one",
                           default = "haha")

    var season = arg[Season](default = Season.WINTER)
  }
  
  trait SomeCategoryOptions extends GlobalOptions { this : Command =>
    var optA = arg[Int](name = "A", default = 1)
    var optB = arg[Boolean](description = "some flag")
  }

  object Run extends Command with SomeCategoryOptions {
    var runSpecific = arg[Long](default = 123L)
  }

  object Show extends Command(name = "cho",
                              description = "show the shit!") with GlobalOptions {
  }

  object Test extends Command with SomeCategoryOptions

  def main(args: Array[String]) {

    println(Usage.Default.show(Commands(Run,Show,Test)))
    
    Cli.parse(args).version("1.2").withCommand(Run) {
      println("Parsed with run : optB=" + Run.optB)
    }

    Cli.parse(args).version("1.2.3").withCommands(Run, Show) match {
      case Run =>
        println("specific : " + Run.runSpecific)
        println("opt1 : " + Run.opt2)

      case Show => println("show")
    }
  }
}
