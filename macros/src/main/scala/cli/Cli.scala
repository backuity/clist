package cli

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

object Cli {

  // We'd really like to have default parameters here but we can't due
  // to https://issues.scala-lang.org/browse/SI-5920
  // The partial workaround is to use the apply method of the ArgumentBuilder
  // Once SI-5920 gets fixed we'll be able to make some of the runtime checks happen
  // at compile time.
  def arg[T] : ArgumentBuilder[T] = macro arg_impl[T]

  def arg_impl[T: c.WeakTypeTag](c: blackbox.Context) = {
    import c.universe._

    val term: TermSymbol = c.internal.enclosingOwner.asTerm
    // why isPublic returns false??
    // TODO make sure the var is public
    // println(term.name + " - " + term.isVar + " - " + term.isPrivate + " - " + term.isPrivateThis)
    if( ! term.isVar ) {
      c.abort(term.pos, "Command arguments can only be a public `var`.")
    }

    q"""new ArgumentBuilder(this, ${term.name.toString.trim})"""
  }

  def parse(args: Array[String]) : Parser = {
    new Parser().parse(args)
  }
}

// this class cannot be within Cli due to some macros restrictions
class ArgumentBuilder[T : Read : Manifest](command: Command, varName: String) {

  /**
   * - unless it is a boolean, an optional argument must have a default value
   * - a boolean cannot have a default value (we want to avoid a boolean being true
   *   by default.. it would always be true)
   */
  def apply[U <: T](name: String = null,
            description: String = null,
            abbrev: String = null,
            abbrevOnly: String = null,
            required: Boolean = false,
            default: T = null.asInstanceOf[T]) : T = {

    def fail(msg: String) {
      throw new IllegalArgumentException(s"Incorrect argument $varName: $msg")
    }

    // TODO check those that at compile time (when SI-5920 gets fixed)

    if( abbrevOnly != null && abbrev != null ) {
      fail("cannot define both abbrev and abbrevOnly")
    }
    if( default == null && !required && manifest[T].runtimeClass != classOf[Boolean] ) {
      fail("a non-boolean optional argument must have a default value")
    }
    // TODO we really want to forbid setting the default value but we have no way to check that..
    //      ..it seems that `default: T = null.asInstanceOf[T]` does not get a null for primitive types
    //      but rather a default value.
    if( manifest[T].runtimeClass == classOf[Boolean] && default.asInstanceOf[Boolean]) {
      fail(s"a boolean argument cannot have a default value set to true")
    }

    command.addArgument(Argument(
      manifest[T].runtimeClass,
      if( abbrevOnly != null ) None else Option(name).orElse(Some(varName.trim)),
      Option(description),
      Option(abbrevOnly).orElse(Option(abbrev)),
      required,
      Option(default))(implicitly[Read[T]]))

    default
  }
}