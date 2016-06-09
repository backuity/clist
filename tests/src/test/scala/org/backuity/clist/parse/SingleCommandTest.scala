package org.backuity.clist.parse

import org.backuity.clist._
import org.junit.Test


class SingleCommandTest extends ClistTestBase {

  import SingleCommandTest._

  @Test
  def parse(): Unit = {
    Cli.parse(Array("stuff")).withCommand(new Run) { run =>
      run.target must_== "stuff"
    }
  }
}

object SingleCommandTest {
  class Run extends Command {
    var target = arg[String](name = "the-target")
  }
}