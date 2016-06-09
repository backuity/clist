package org.backuity.clist.parse

import org.backuity.clist._
import org.junit.Test

class OptionAbbreviatedTest extends ClistTestBase {
  import OptionAbbreviatedTest._

  @Test
  def parseAbbrev(): Unit = {
    Cli.parse(Array()).withCommand(new RunWithAbbrev) { run =>
      run.opt1 must beFalse
      run.a must beFalse
    }

    Cli.parse(Array("-o")).withCommand(new RunWithAbbrev) { run =>
      run.opt1 must beTrue
    }

    Cli.parse(Array("-a")).withCommand(new RunWithAbbrev) { run =>
      run.a must beTrue
    }
  }

  @Test
  def abbrevOnlyShouldFailForLongSyntax(): Unit = {
    Cli.parse(Array("--a")).throwExceptionOnError().withCommand(new RunWithAbbrev)() must throwA[ParsingException].withMessage(
      "No option found for --a")
  }
}

object OptionAbbreviatedTest {
  // TODO test abbrev not allowed for non boolean types

  class RunWithAbbrev extends Command {
    var opt1 = opt[Boolean](abbrev = "o")
    var a = opt[Boolean](abbrevOnly = "a")
  }
}
