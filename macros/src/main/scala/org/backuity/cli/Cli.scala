package org.backuity.cli

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

object Cli {

  /**
   * Define an attribute of a [[Command]] to be a command line argument.
   *
   * Ex: Given the `cat` command, `var files = arg[ List[String] ]()` would produce
   *     the following command : `cat <file>...`
   */
  // We'd really like to have default parameters here but we can't due
  // to https://issues.scala-lang.org/browse/SI-5920
  // The partial workaround is to use the apply method of the ArgumentBuilder
  // Once SI-5920 gets fixed we'll be able to make some of the runtime checks happen
  // at compile time.
  def arg[T] : CliArgument.Builder[T] = macro arg_impl[T]

  /**
   * Define an attribute of a [[Command]] to be a command line option.
   *
   * Ex: Given the `cat` command, `var verbose = opt[Boolean]()` would produce
   *     the following command : `cat [options]` with options containing `--verbose`
   */
  // same as arg[T] above
  def opt[T] : CliOption.Builder[T] = macro opt_impl[T]

  def arg_impl[T: c.WeakTypeTag](c: blackbox.Context) = {
    macro_impl(c, isOption = false)
  }

  def opt_impl[T: c.WeakTypeTag](c: blackbox.Context) = {
    macro_impl(c, isOption = true)
  }

  private def macro_impl[T: c.WeakTypeTag](c: blackbox.Context, isOption : Boolean) = {
    import c.universe._

    val term: TermSymbol = c.internal.enclosingOwner.asTerm
    // why isPublic returns false??
    // TODO make sure the var is public
    // println(term.name + " - " + term.isVar + " - " + term.isPrivate + " - " + term.isPrivateThis)
    if (!term.isVar) {
      c.abort(term.pos, "Command arguments can only be a public `var`.")
    }

    if( isOption ) {
      q"""new _root_.org.backuity.cli.CliOption.Builder(this, ${term.name.toString.trim})"""
    } else {
      q"""new _root_.org.backuity.cli.CliArgument.Builder(this, ${term.name.toString.trim})"""
    }
  }

  def parse(args: Array[String]) : Parser = {
    new Parser().parse(args)
  }
}

