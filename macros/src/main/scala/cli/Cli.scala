package cli

import cli.Cli.{Argument, Command}
import scala.language.experimental.macros
import scala.reflect.macros.blackbox

class ArgumentBuilder[T : Read : Manifest](command: Command, varName: String) {

  /**
   * - an optional argument must have a default value
   */
  def apply(name: String = null,
            description: String = null,
            abbrev: String = null,
            required: Boolean = false,
            default: T = null.asInstanceOf[T]) : T = {

    if( default == null && !required ) {
      throw new IllegalArgumentException("An optional argument must have a default value")
    }

    command.addArgument(Argument(
        implicitly[Read[T]],
        manifest[T].runtimeClass,
        Option(name).orElse(Some(varName.trim)),
        Option(description),
        Option(abbrev),
        required,
        Option(default)))

    default
  }
}

object Cli {

  case class Argument[T](reader: Read[T],
                          tpe: Class[_],
                          name: Option[String],
                          description: Option[String],
                          abbrev: Option[String],
                          required: Boolean,
                          default: Option[T]) {
  }

  case class Commands( arguments : Set[Argument[_]],
                       commands : Set[Command])

  object Commands {
    def apply[T <: Command : Manifest](commands : T*) : Commands = {
      if( commands.size < 2 ) sys.error("A Commands must have at least 2 commands")

      val commonArgs = commands.map(_.arguments).reduce( _.intersect(_) )
      new Commands(commonArgs, commands.toSet)
    }
  }

  def arg_impl[T: c.WeakTypeTag](c: blackbox.Context) : c.Expr[ArgumentBuilder[T]] = {
    import c.universe._

    val term: TermSymbol = c.internal.enclosingOwner.asTerm
    // why isPublic returns false??
    // TODO make sure the var is public
    // println(term.name + " - " + term.isVar + " - " + term.isPrivate + " - " + term.isPrivateThis)
    if( ! term.isVar ) {
      c.abort(term.pos, "Command arguments can only be a public `var`.")
    }

    c.Expr[ArgumentBuilder[T]](q"""new ArgumentBuilder(this, ${term.name.toString})""")
  }

  // We'd really like to have default parameters here but we can't due
  // to https://issues.scala-lang.org/browse/SI-5920
  // The workaround is to use the apply method of the ArgumentBuilder
  def arg[T] : ArgumentBuilder[T] = macro arg_impl[T]

  /** @param name if not specified the lower-cased class name will be used */
  abstract class Command(name: String = null, val description: String = "") {

    val _name = if( name != null ) name else {
      val className = getClass.getSimpleName.toLowerCase
      if( className.endsWith("$") ) {
        className.substring(0, className.length - 1)
      } else {
        className
      }
    }

    def label = _name

    var arguments : Set[Argument[_]] = Set.empty
    def addArgument(arg: Argument[_]): Unit = {
      arguments += arg
    }

    def validate() : Unit = {}
  }

  trait Usage {
    def show(commands: Commands) : String
  }

  object Usage {

    object Default extends Usage {

      import org.backuity.ansi.AnsiFormatter.FormattedHelper

      override def show(commands: Commands): String = {

        val usage = new java.lang.StringBuilder()
        var indentLevel = 0
        var beginning = true

        def incIndent(): Unit = {
          indentLevel += 1
        }
        def decIndent(): Unit = {
          indentLevel -= 1
        }
        def indent( f : => Unit ): Unit = {
          incIndent()
          f
          decIndent()
        }

        def addLine(str: String = "") {
          add(str + "\n")
        }
        def add(str: String): Unit = {
          val pad = if( beginning ) "\t" * indentLevel else ""
          usage.append(pad + str)
          if( str.endsWith("\n") ) {
            beginning = true
          } else {
            beginning = false
          }
        }

        def argLabel(arg: Argument[_]) : String = {
          (arg.abbrev match {
            case Some(abbrev) => "-" + abbrev + (if( arg.name.isDefined ) ", " else "")
            case None         => ""
          }) + (arg.name match {
            case Some(name) => "--" + name + (if( arg.tpe == classOf[Int] || arg.tpe == classOf[Long] ) {
              "=NUM"
            } else if( arg.tpe == classOf[String] ) {
              "=STRING"
            } else "")
            case None       => ""
          })
        }

        def argText(labelMaxSize: Int, arg: Argument[_]) : String = {
          val label = argLabel(arg)
          val description = arg.description.getOrElse("")
          val default = arg.default match {
            case Some(d) => ansi"%italic{(default: $d)}"
            case None => ""
          }
          val padding = " " * (labelMaxSize - label.length)

          ansi"%yellow{$label}" + (if( description.isEmpty && default.isEmpty ) {
            ""
          } else {
            padding + " : " + description + (if( !description.isEmpty && !default.isEmpty ) " " else "") + default
          })
        }

        def addArguments(args: Set[Argument[_]]): Unit = {
          val labelMaxSize = args.map(argLabel).map(_.length).max

          for( arg <- args ) {
            addLine(argText(labelMaxSize, arg))
          }
        }

        addLine(ansi"%underline{Usage}")
        addLine()
        addLine(ansi" %bold{cli} %yellow{[options]} %bold{command} %yellow{[command options]}")
        addLine()
        addLine(ansi"%underline{Options:}")
        addLine()
        indent {
          addArguments(commands.arguments)
        }

        addLine()
        addLine(ansi"%underline{Commands:}")
        indent {
          for (command <- commands.commands) {
            addLine()
            add(ansi"%bold{${command.label}}")
            val commandSpecificArgs = command.arguments -- commands.arguments
            val description = if( command.description != "" ) " : " + command.description else ""
            if (commandSpecificArgs.isEmpty) {
              addLine(description)
            } else {
              addLine(" [command options]" + description)
              indent {
                addArguments(commandSpecificArgs)
              }
            }
          }
        }

        usage.toString
      }
    }
  }

  class Parser {
    var version : Option[String] = None
    var help : Option[String] = Some("help")
    var usage : Usage = Usage.Default
    var args: Array[String] = null

    def version(version: String) : Parser = {
      this.version = Some(version)
      this
    }

    def noHelp() : Parser = {
      this.help = None
      this
    }

    def withUsage(usage: Usage) : Parser = {
      this.usage = usage
      this
    }

    def withCommands[T <: Command : Manifest](withCommands : T*) : T = {
      val commands = Commands(withCommands : _*)

      // TODO
      withCommands(0)
    }

    def withCommand[T](command: Command)(onSuccess: => T): T = {
      // TODO
      onSuccess
    }

    def parse(args: Array[String]) : Parser = {
      this.args = args
      this
    }
  }

  def parse(args: Array[String]) : Parser = {
    new Parser().parse(args)
  }
}
