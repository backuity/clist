import java.io.File

import org.backuity.clist._

object CatDemo extends CliMain(
  name = "cat",
  description = "concatenate files and print on the standard output") {

  var showAll        = opt[Boolean](abbrev = "A", description = "equivalent to -vET")
  var numberNonblank = opt[Boolean](abbrev = "b", description = "number nonempty output lines, overrides -n")
  var maxLines       = opt[Int](default = 123)
  var files          = args[Seq[File]](description = "files to concat")

  def run: Unit = {
    println("files    = " + files)
    println("showAll  = " + showAll)
    println("maxLines = " + maxLines)
  }
}
