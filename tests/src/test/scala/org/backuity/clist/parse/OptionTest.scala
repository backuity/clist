package org.backuity.clist.parse

import org.backuity.clist._
import org.junit.Test
import java.lang.System.{lineSeparator => crlf}

class OptionTest extends ClistTestBase {

  import OptionTest._

  @Test
  def doNotReuseTheSameOptionMoreThanOnce(): Unit = {
    Cli.parse(Array("-o", "--opt1")).throwExceptionOnError().withCommand(new Run)() must throwA[ParsingException].withMessage(
      "No option found for --opt1")
  }

  @Test
  def incorrectCommandOption(): Unit = {
    Cli.parse(Array("--unknown-option", "target")).withCommand(new Run)() must exitWithCode(1)
    console.content must_== (Usage.Default.show("x",Commands(new Run)) + crlf +
      "No option found for --unknown-option" + crlf)
  }
}

object OptionTest {

  class Run extends Command {
    var opt1 = opt[Boolean](abbrev = "o")
  }
}
