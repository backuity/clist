package org.backuity.cli

class Parser(implicit console: Console, exit: Exit) {
  private var version : Option[String] = None
  private var versionCmd : Option[String] = None
  private var help : Option[String] = Some("help")
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
    this.help = None
    this
  }

  def noShowUsageOnError() : Parser = {
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
    this.help = Some(name)
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
   * @throws ParsingException if `noUsageOnError` is enabled
   */
  def withCommands[T <: Command : Manifest](withCommands : T*) : Option[T] = {
    val commands = Commands(withCommands : _*)

    @inline def fail(msg: String) = this.fail(msg, commands)

    args.indexWhere( ! _.startsWith("-")) match {
      case -1 => fail("No command found, expected one of " +
        commands.commands.map(_.label).toList.sorted.mkString(", "))

      case idx =>
        val (globalOptions, cmdName :: params) = args.splitAt(idx)
        if (help.contains(cmdName)) {
          console.println(usage.show(commands))
          None
        } else if (versionCmd.contains(cmdName)) {
          console.println(version.get)
          None
        } else {
          commands.findByName(cmdName) match {
            case None => fail(s"Unknown command '$cmdName'")
            case Some(cmd) =>
              cmd.read(params ::: globalOptions)
              Some(cmd.asInstanceOf[T])
          }
        }
    }
  }

  def withCommand[C <: Command,R](command: C)(f : C => R = { a : C => () }): R = {
    command.read(args)
    f(command)
  }

  private def fail(msg: String, commands: Commands): Nothing = {
    if( showUsageOnError ) {
      console.println(usage.show(commands))
    }
    if( exceptionOnError ) {
      throw ParsingException(msg)
    } else {
      exit.exit(customExitCode.getOrElse(defaultExitCode))
    }
  }
}