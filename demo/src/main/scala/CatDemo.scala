import java.io.File

import org.backuity.clit._

object CatDemo {

  object Cat extends Command(description = "concatenate files and print on the standard output") {

    var files = args[Seq[File]](description = "files to concat")

    var showAll = opt[Boolean](abbrev = "A", description = "equivalent to -vET")
    var numberNonblank = opt[Boolean](abbrev = "b", description = "number nonempty output lines, overrides -n")
  }

  def main(args: Array[String]) {
    Cli.parse(args)
      .version("1.2")
      .withCommand(Cat) {
        case Cat =>
          println(Cat.files)
      }
  }
}
