import org.backuity.clit._

object PuppetModuleInstaller {

  trait Common { this : Command =>
    var verbose = opt[Boolean](abbrev = "v")
    var dontRecurse = opt[Boolean](description = "Do not fetch the module transitively")
  }

  trait FetchModes { this : Command =>
    var latest = opt[Boolean](description = "Fetch the highest possible minor version of each module or\n" +
                                            "use latest if no version information is available.")
    var latestForce = opt[Boolean](description = "Fetch the highest possible version of each module (disregard major information).")
    var latestHead = opt[Boolean](description = "Fetch the highest possible minor version of each module or\n" +
                                                "use HEAD if no version information is available.")
    var head = opt[Boolean](description = "Use HEAD version for all modules.")

    validate {
      if( moreThanOne(latest, latestForce, latestHead, head) ) {
        parsingError("Only one of latest, latest-force, latest-head, head can be used.")
      }
    }
  }

  object Graph extends Command(description = "Shows the graph of modules - do not install them") with Common with FetchModes {
  }

  object Run extends Command(description = "Run the installer") with Common with FetchModes {
    var check = opt[Boolean](description = "Only check that the module versions are consistent, that is,\n" +
                                           "we cannot find 2 incompatible version (different major) of the same module")
  }

  object Show extends Command(description = "Shows the current modules. Dirty modules are shown in yellow.") with Common {
  }

  // ----------------------------------------------

  def main(args: Array[String]) {

    Cli.parse(args)
       .withProgramName("puppet-module-installer")
       .version("1.1.0")
       .withCommands(Graph,Run,Show) match {

      case Some(Graph) => println("Graphing with mode: " + Graph.dontRecurse)
      case _ => println("other")
    }
  }
}
