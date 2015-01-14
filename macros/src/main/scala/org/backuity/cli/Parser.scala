package org.backuity.cli

class Parser(implicit console: Console, exit: Exit) {
  private var version : Option[String] = None
  private var versionCmd : Option[String] = None
  private var helpCmd : Option[String] = Some("help")
  private var usage : Usage = Usage.Default
  private var showUsageOnError : Boolean = true
  private var exceptionOnError : Boolean = false
  private val defaultExitCode : Int = 1
  private var customExitCode : Option[Int] = None
  private var args: List[String] = Nil

  def version(version: String, command: String = "version") : Parser = {
    this.version = Some(version)
    this.versionCmd = Some(command)
    this
  }

  /** Disable the help command. */
  def noHelp() : Parser = {
    this.helpCmd = None
    this
  }

  def noUsageOnError() : Parser = {
    this.showUsageOnError = false
    this
  }

  /** Throws a `ParsingException` if the parser cannot parse the arguments. */
  def throwExceptionOnError() : Parser = {
    this.exceptionOnError = true
    checkExceptionOnErrorAndExitCode()
    this
  }

  /**
   * Define which code is used to exit the program if the parser is unabled to parse the arguments.
   * This option cannot be used together with `noUsageOnError`.
   */
  // TODO use a macro to enforce the exclusion with noUsageOnError?
  def exitCode(code: Int) : Parser = {
    this.customExitCode = Some(code)
    checkExceptionOnErrorAndExitCode()
    this
  }

  private def checkExceptionOnErrorAndExitCode(): Unit = {
    if( exceptionOnError && customExitCode.isDefined ) {
      sys.error("Cannot use both exit-code and throws-exception-on-error.")
    }
  }

  def withHelpCommand(name: String) : Parser = {
    this.helpCmd = Some(name)
    this
  }

  def withUsage(usage: Usage) : Parser = {
    this.usage = usage
    this
  }

  def parse(args: Array[String]) : Parser = {
    this.args = args.toList
    this
  }

  // terminal methods, i.e that execute the parser

  /**
   * @return None if version or help is executed
   * @throws ParsingException if `noUsageOnError` is enabled
   */
  def withCommands[T <: Command : Manifest](withCommands : T*) : Option[T] = {
    val commands = Commands(withCommands : _*)

    @inline def fail(msg: String) = this.fail(msg, commands)

    if( parseVersion() || parseHelp(commands) ) {
      None
    } else {
      args.indexWhere(!_.startsWith("-")) match {
        case -1 => fail("No command found, expected one of " +
          commands.commands.map(_.label).toList.sorted.mkString(", "))

        case idx =>
          val (globalOptions, cmdName :: params) = args.splitAt(idx)
          commands.findByName(cmdName) match {
            case None => fail(s"Unknown command '$cmdName'")
            case Some(cmd) =>
              cmd.read(params ::: globalOptions)
              Some(cmd.asInstanceOf[T])
          }
      }
    }
  }

  def withCommand[C <: Command : Manifest, R](command: C)(f : C => R = { a : C => () }): Option[R] = {
    if( parseVersion() || parseHelp(Commands(command)) ) {
      None
    } else {
      command.read(args)
      Some(f(command))
    }
  }

  private def parseVersion() : Boolean = {
    withFirstArg { case arg if versionCmd == Some(arg) =>
      console.println(version.get)
    }
  }

  private def parseHelp(commands: Commands) : Boolean = {
    withFirstArg { case arg if helpCmd == Some(arg) =>
      console.println(usage.show(commands))
    }
  }

  private def withFirstArg(pf : PartialFunction[String,Unit]) : Boolean = {
    args.headOption match {
      case Some(arg) if pf.isDefinedAt(arg) => pf(arg); true
      case _ => false
    }
  }

  private def fail(msg: String, commands: Commands): Nothing = {
    if( showUsageOnError ) {
      console.println(usage.show(commands))
    }
    if( exceptionOnError ) {
      throw ParsingException(msg)
    } else {
      console.println(msg)
      exit.exit(customExitCode.getOrElse(defaultExitCode))
    }
  }
}