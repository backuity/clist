package cli

import cli.Cli.{Argument, Command}
import scala.language.experimental.macros
import scala.reflect.macros.blackbox

class ArgumentBuilder[T : Read](command: Command, varName: String) {

  /**
   * - an optional argument must have a default value
   */
  def apply(name: String = null,
            description: String = null,
            abbrev: String = null,
            required: Boolean = false,
            default: T = null.asInstanceOf[T]) : T = {

    println("Configuring command " + command + " - varName = " + varName + " - name = " + name)

    if( default == null && !required ) {
      throw new IllegalArgumentException("An optional argument must have a default value")
    }

    command.addArgument(Argument(
        implicitly[Read[T]],
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
      println("Root class = " + manifest[T])

      null
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

  def arg[T] : ArgumentBuilder[T] = macro arg_impl[T]

  /** @param name if not specified the lower-cased class name will be used */
  abstract class Command(name: String = null) {

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
      import AnsiFormatter._

      override def show(commands: Commands): String = {

        val usage = new java.lang.StringBuilder()

        def addLine(str: String = "") {
          usage.append(str + "\n")
        }

        addLine(ansi"Hey \Underline{Usage}")
        addLine()
        addLine(ansi" cli [options] \yellow{command} \bold{[command parameter(s)]} [command options]")
        addLine()
        addLine(ansi"\Underline{Options:}")
        addLine()

        //          _jCommander.parameters.findAll { !it.parameter.hidden() }.sort { it.names }.each { globalParam ->
        //            LOG.info(
        //              String.format(" @|yellow %-${_jCommander.parameters.collect { computeOptionSyntax(it) }*.size().max()}s|@ : %s %s",
        //                computeOptionSyntax(globalParam),
        //                globalParam.description,
        //                computeDefaultValue(globalParam) ? "(Default: ${computeDefaultValue(globalParam)})" : ""))
        //          }
        //          println("")
        //          println("@|underline Commands:|@")
        //          _jCommander.commands.each { command, commandDescription ->
        //            LOG.info("")
        //            List<String> commandHelp = new ArrayList<>()
        //            commandHelp << _scriptName
        //            commandHelp << "[options]"
        //            commandHelp << "@|yellow ${command}|@"
        //            if (commandDescription.mainParameter) {
        //              commandHelp << "@|bold ${commandDescription.mainParameter?.description}|@"
        //            }
        //            if (_jCommander.commands[command].parameters.findAll { !it.parameter.hidden() }.size() > 0) {
        //              commandHelp << "[${command} options]"
        //            }
        //            LOG.info(" ${commandHelp.join(" ")}")
        //            commandDescription.parameters.findAll { !it.parameter.hidden() }.sort { it.names }.each { commandParam ->
        //              LOG.info(
        //                String.format(
        //                  " @|yellow %-${commandDescription.parameters.findAll { !it.parameter.hidden() }.collect { computeOptionSyntax(it) }*.trim()*.size().max()}s|@ : %s %s",
        //                  computeOptionSyntax(commandParam),
        //                  commandParam.description,
        //                  computeDefaultValue(commandParam) ? "(Default: ${computeDefaultValue(commandParam)})" : ""))
        //            }
        //        }

        usage.toString
      }
    }

//        private String computeOptionSyntax(ParameterDescription parameterDescription) {
//          parameterDescription.parameter.names().collect { paramName ->
//            if (parameterDescription.parameterized.type == Boolean) {
//              "${paramName}"
//            } else if (parameterDescription.parameterized.type == URL) {
//              "${paramName}=URL"
//            } else if (parameterDescription.parameterized.type.isEnum()) {
//              "${paramName}=[${parameterDescription.parameterized.type.enumConstants.collect { it.toString().toLowerCase() }.join("|")}]"
//            } else {
//              "${paramName}=value"
//            }
//          }.join(", ")
//        }
//        private String computeDefaultValue(ParameterDescription parameterDescription) {
//          if (parameterDescription.parameterized.type == Boolean && !parameterDescription.default) {
//            "false"
//          } else {
//            parameterDescription.default ? parameterDescription.default?.toString()?.toLowerCase() : ""
//          }
//        }
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
      //        println(usage.show(commands))
      //println("Base type found : " + manifest[T])
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
