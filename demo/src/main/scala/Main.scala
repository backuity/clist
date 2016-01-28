import org.backuity.clist._

object Main {

  // this lets you be exhaustive when matching on the command return
  sealed trait MyCommand extends Command

  trait GlobalOptions { this: Command =>
    var opt1 = opt[Boolean](name = "1",
                           description = "This is a wonderful command")
    var opt2 = opt[String](description = "Man you should try this one",
                           default = "haha")

    var season = opt[Season](default = Season.WINTER)
  }
  
  trait SomeCategoryOptions extends GlobalOptions { this: Command =>
    var optA = opt[Int](name = "A", default = 1)
    var optB = opt[Boolean](description = "some flag")
  }

  object Run extends Command(description = "run run baby run") with SomeCategoryOptions with MyCommand {
    var target = arg[String]()

    var runSpecific = opt[Long](default = 123L)
  }

  object Show extends Command(name = "cho",
                              description = "show the shit!") with GlobalOptions with MyCommand {
  }

  object Test extends Command with SomeCategoryOptions with MyCommand

  def main(args: Array[String]) {

    Cli.parse(args).version("1.2.3").withCommands(Run, Show, Test) match {
      case Some(Run) =>
        println("Executed Run command:")
        println("\t- target : " + Run.target)
        println("\t- specific : " + Run.runSpecific)
        println("\t- opt1 : " + Run.opt2)

      case Some(Show) =>
        println("show")
        println("\t - season : " + Show.season)

      case Some(Test) =>
        println("test")

      case None =>
        println("nothing done")
    }
  }
}
