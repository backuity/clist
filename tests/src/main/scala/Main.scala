import cli.Cli.{Commands, Usage, Command}
import cli._

object Main {

  import Cli.arg

  trait GlobalOptions { this : Command =>
    var opt1 = arg[String](name = "1", default = "helloX")
    var opt2 = arg[String](name = "2", default = "haha")
  }
  
  trait SomeCategoryOptions extends GlobalOptions { this : Command =>
    private var optA = arg[Int](name = "A", default = 1)
    var optB = arg[Int](name = "B", default = 123)
  }

  object Run extends Command with SomeCategoryOptions {
    var runSpecific = arg[Long](default = 123L)
  }

  object Show extends Command(name = "cho") {
  }

  def main(args: Array[String]) {

    println(Run.arguments.mkString("\n\t"))

    println("Run.name = " + Run.label)
    println("Show.name = " + Show.label)

    println("Usage : " + Usage.Default.show(Commands(Run,Show)))
    
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
