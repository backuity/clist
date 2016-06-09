package org.backuity.clist.parse

import org.backuity.clist._
import org.junit.Test

class DefaultsTest extends ClistTestBase {
  import DefaultsTest._

  @Test
  def trueOptionByDefault(): Unit = {
    Cli.parse(Array()).withCommand(new TrueOptionByDefault) { cmd =>
      cmd.opt1 must beTrue
    }

    Cli.parse(Array("--opt1")).withCommand(new TrueOptionByDefault) { cmd =>
      cmd.opt1 must beFalse
    }
  }

  @Test
  def nonBooleanOptionMustHaveADefault(): Unit = {
    // this will ultimately be a compilation error
    Cli.parse(Array("--missing=ha")).withCommand(new MissingDefaultOption)() must throwAn[IllegalArgumentException].withMessage(
      "Incorrect argument 'missing': an optional argument that has neither type Option nor Boolean must have a default value")
  }

  @Test
  def nonBooleanOptionalArgumentMustHaveADefault(): Unit = {
    // this will ultimately be a compilation error
    Cli.parse(Array("ha")).withCommand(new MissingDefaultOptionalArgument)() must throwAn[IllegalArgumentException].withMessage(
      "Incorrect argument 'missing': an optional argument that has neither type Option nor Boolean must have a default value")
  }

  @Test
  def parseCommandMustSetDefaults(): Unit = {
    Cli.parse(Array("--1", "sun")).withCommand(new RunWithOption) { run =>
      run.target must_== "sun"
      run.opt1 must_== true
      run.opt2 must_== "haha"
    }

    Cli.parse(Array("--opt2=another", "sea")).withCommand(new RunWithOption) { run =>
      run.target must_== "sea"
      run.opt1 must_== false
      run.opt2 must_== "another"
    }

    Cli.parse(Array("--opt2=toto", "--1", "sea")).withCommand(new RunWithOption) { run =>
      run.target must_== "sea"
      run.opt1 must_== true
      run.opt2 must_== "toto"
    }

    Cli.parse(Array("nice")).withCommand(new RunWithOption) { run =>
      run.target must_== "nice"
      run.opt1 must_== false
      run.opt2 must_== "haha"
    }
  }

  @Test
  def parseStaticCommandMustResetDefaults(): Unit = {
    object StaticRun extends RunWithOption

    Cli.parse(Array("--1", "sun")).withCommand(StaticRun)()
    StaticRun.target must_== "sun"
    StaticRun.opt1 must_== true
    StaticRun.opt2 must_== "haha"

    Cli.parse(Array("--opt2=another", "sea")).withCommand(StaticRun)()
    StaticRun.target must_== "sea"
    StaticRun.opt1 must_== false
    StaticRun.opt2 must_== "another"

    Cli.parse(Array("--opt2=toto", "--1", "sea")).withCommand(StaticRun)()
    StaticRun.target must_== "sea"
    StaticRun.opt1 must_== true
    StaticRun.opt2 must_== "toto"

    Cli.parse(Array("nice")).withCommand(StaticRun)()
    StaticRun.target must_== "nice"
    StaticRun.opt1 must_== false
    StaticRun.opt2 must_== "haha"
  }
}

object DefaultsTest {

  class RunWithOption extends Command {
    var target = arg[String]()
    var opt1 = opt[Boolean](name = "1")
    var opt2 = opt[String](default = "haha")
  }

  class TrueOptionByDefault extends Command {
    var opt1 = opt[Boolean](default = true)
  }

  class MissingDefaultOption extends Command {
    var missing = opt[String]()
  }

  class MissingDefaultOptionalArgument extends Command {
    var missing = arg[String](required = false)
  }
}