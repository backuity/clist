package org.backuity.clist.parse

import org.backuity.clist._
import org.junit.Test

class ArgumentsTest extends ClistTestBase {
  import ArgumentsTest._

  @Test
  def parseMultiArgs(): Unit = {
    Cli.parse(Array("-a", "arg1", "arg2")).withCommand(new MultiArgs) { cmd =>
      cmd.names must_== Seq("arg1", "arg2")
      cmd.a must beTrue
    }
  }

  @Test
  def failMissingMultipleArgs(): Unit = {
    Cli.parse(Array()).throwExceptionOnError().withCommand(new MultiArgs)() must throwA[ParsingException].withMessage(
      "Insufficient arguments for names")
  }
}

object ArgumentsTest {

  class MultiArgs extends Command {
    var names = args[Seq[String]]()
    var a = opt[Boolean](abbrev = "a")
  }
}