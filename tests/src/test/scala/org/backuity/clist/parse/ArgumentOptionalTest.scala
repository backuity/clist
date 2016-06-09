package org.backuity.clist.parse

import org.backuity.clist._
import org.junit.Test

class ArgumentOptionalTest extends ClistTestBase {

  import ArgumentOptionalTest._

  @Test
  def parseOptionalArgument(): Unit = {
    Cli.parse(Array()).withCommand(new RunOptional) { run =>
      run.target must_== None
    }
  }

  @Test
  def multipleOptionalArgument(): Unit = {
    Cli.parse(Array()).withCommand(new MultipleOptional) { run =>
      run.arg1 must_== None
      run.arg2 must_== None
    }

    Cli.parse(Array("ha")).withCommand(new MultipleOptional) { run =>
      run.arg1 must_== Some("ha")
      run.arg2 must_== None
    }

    Cli.parse(Array("ha", "he")).withCommand(new MultipleOptional) { run =>
      run.arg1 must_== Some("ha")
      run.arg2 must_== Some("he")
    }
  }
}

object ArgumentOptionalTest {
  class RunOptional extends Command {
    var target = arg[Option[String]](required = false)
  }

  class MultipleOptional extends Command {
    var arg1 = arg[Option[String]](required = false)
    var arg2 = arg[Option[String]](required = false)
  }
}