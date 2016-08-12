package org.backuity.clist

import org.backuity.clist.Command.ParseContext
import org.backuity.clist.util.Read
import org.backuity.matchete.JunitMatchers
import org.junit.Test

class CommandTest extends JunitMatchers {

  @Test
  def parseContextValidateShouldOnlyRemoveOneArgument(): Unit = {
    val arg = CliOptionalArgument(classOf[String], "", "", None, "")(implicitly[Read[String]])

    new ParseContext(List(arg), Set.empty, List("arg")).validate(arg, "arg").remainingArgs must_== List()
    new ParseContext(List(arg), Set.empty, List("arg", "arg")).validate(arg, "arg").remainingArgs must_== List("arg")
    new ParseContext(List(arg), Set.empty, List("hey", "arg", "arg")).validate(arg, "arg").remainingArgs must_== List("hey", "arg")
    new ParseContext(List(arg), Set.empty, List("hey", "arg")).validate(arg, "arg").remainingArgs must_== List("hey")
    new ParseContext(List(arg), Set.empty, List("hey", "arg", "arg", "hoy")).validate(arg, "arg").remainingArgs must_== List("hey", "arg", "hoy")
  }
}
