
object Main {


  trait Read[A] { self =>
    def arity: Int
    def tokensToRead: Int = if (arity == 0) 0 else 1
    def reads: String => A
    def map[B](f: A => B): Read[B] = new Read[B] {
      val arity = self.arity
      val reads = self.reads andThen f
    }
  }
  object Read {
    import java.util.{Locale, Calendar, GregorianCalendar}
    import java.text.SimpleDateFormat
    import java.io.File
    import java.net.URI
    def reads[A](f: String => A): Read[A] = new Read[A] {
      val arity = 1
      val reads = f
    }
    implicit val intRead: Read[Int] = reads { _.toInt }
    implicit val stringRead: Read[String] = reads { identity }
    implicit val doubleRead: Read[Double] = reads { _.toDouble }
    implicit val booleanRead: Read[Boolean] =
      reads { _.toLowerCase match {
        case "true" => true
        case "false" => false
        case "yes" => true
        case "no" => false
        case "1" => true
        case "0" => false
        case s =>
          throw new IllegalArgumentException("'" + s + "' is not a boolean.")
      }}
    implicit val longRead: Read[Long] = reads { _.toLong }
    implicit val bigIntRead: Read[BigInt] = reads { BigInt(_) }
    implicit val bigDecimalRead: Read[BigDecimal] = reads { BigDecimal(_) }
    implicit val yyyymmdddRead: Read[Calendar] = calendarRead("yyyy-MM-dd")
    def calendarRead(pattern: String): Read[Calendar] = calendarRead(pattern, Locale.getDefault)
    def calendarRead(pattern: String, locale: Locale): Read[Calendar] =
      reads { s =>
        val fmt = new SimpleDateFormat(pattern)
        val c = new GregorianCalendar
        c.setTime(fmt.parse(s))
        c
      }
    implicit val fileRead: Read[File] = reads { new File(_) }
    implicit val uriRead: Read[URI] = reads { new URI(_) }
    implicit def tupleRead[A1: Read, A2: Read]: Read[(A1, A2)] = new Read[(A1, A2)] {
      val arity = 2
      val reads = { (s: String) =>
        splitKeyValue(s) match {
          case (k, v) => implicitly[Read[A1]].reads(k) -> implicitly[Read[A2]].reads(v)
        }
      }
    }
    private def splitKeyValue(s: String): (String, String) =
      s.indexOf('=') match {
        case -1 => throw new IllegalArgumentException("Expected a key=value pair")
        case n: Int => (s.slice(0, n), s.slice(n + 1, s.length))
      }
    implicit val unitRead: Read[Unit] = new Read[Unit] {
      val arity = 0
      val reads = { (s: String) => () }
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

    trait Command {

      var arguments : Set[Argument[_]] = Set.empty

      def validate() : Unit = {}

      /** - at least one of name or abbrev must be non-empty
        * - an optional argument must have a default value 
        */
      def arg[T : Read](name : String = null,
                        description : String = null,
                        abbrev : String = null,
                        required : Boolean = false,
                        default : T = null) : T = {

        if( name.isEmpty && abbrev.isEmpty ) {
          throw new IllegalArgumentException("Invalid option, either name or abbrev must be defined")
        }
        if( default == null && !required ) {
          throw new IllegalArgumentException("An optional argument must have a default value")
        }

        val argument = Argument(
          implicitly[Read[T]],
          Option(name),
          Option(description),
          Option(abbrev),
          required,
          Option(default))

        arguments += argument
        default
      }
    }

    trait Usage {
      def show(commands: Commands) : String
    }

    object Usage {
      import AnsiFormatter._

      object Default extends Usage {
        override def show(commands: Commands): String = {
          val usage = new java.lang.StringBuilder()

          def addLine(str: String = "") {
            usage.append(str + "\n")
          }

          addLine(ansi"\Underline{Usage}\n")
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

      def version(version: String) : Parser = {
        this.version = Some(version)
        this
      }

      def noHelp() : Parser = {
        this.help = None
        this
      }

      def parse[T <: Command : Manifest](args: Array[String], withCommands : T*) : T = {
        val commands = Commands(withCommands : _*)

//        println(usage.show(commands))
        //println("Base type found : " + manifest[T])
        withCommands(0)
      }
    }

    def version(version: String) : Parser = {
      new Parser().version(version)
    }

    def noHelp() : Parser = {
      new Parser().noHelp()
    }

    def parse[T <: Command : Manifest](args: Array[String], withCommands : T*) : T = {
      new Parser().parse(args, withCommands : _*)
    }
  }

  // ----------------------------------------------------

  abstract class Command extends Cli.Command {
    var opt1 = arg[String](name = "opt1", default = "hello")
    var opt2 = arg[String](name = "opt2", default = "haha")
  }

  trait Options { this : Cli.Command =>
    var optA = arg[Int](name = "optA", default = 1)
    var optB = arg[Int](name = "optB", default = 123)
  }

  object Run extends Command with Options {
    var runSpecific = arg[Long](name = "runSpecific", default = 123L)
  }

  object Show extends Command {
  }

  def main(args: Array[String]) {

    Cli.version("1.2.3").parse(args, withCommands = Run, Show) match {
      case Run =>
        println(Run.runSpecific)
        println(Run.opt1)

      case Show => println("show")
    }
  }
}
