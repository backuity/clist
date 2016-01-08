package org.backuity.clit

import org.backuity.ansi.AnsiFormatter.FormattedHelper
import org.backuity.clit.Formatting.ClassUtil

class Parser(implicit console: Console, exit: Exit) {
  private var customProgramName: Option[String] = None
  private var version: Option[String] = None
  private var versionCmd: Option[String] = None
  private var helpCmd: Option[String] = Some("help")
  private var usage: Usage = Usage.Default
  private var showUsageOnError: Boolean = true
  private var exceptionOnError: Boolean = false
  private val defaultExitCode: Int = 1
  private var customExitCode: Option[Int] = None
  private var args: List[String] = Nil

  /**
    * Specify a version for this program. It adds a version command and prints it
    * in the usage message.
    */
  def version(version: String, command: String = "version"): Parser = {
    this.version = Some(version)
    this.versionCmd = Some(command)
    this
  }

  /** Disable the help command. */
  def noHelp(): Parser = {
    this.helpCmd = None
    this
  }

  /**
    * Do not print a usage on parsing failure.
    * By default a usage is printed.
    */
  def noUsageOnError(): Parser = {
    this.showUsageOnError = false
    this
  }

  /** Throws a `ParsingException` if the parser cannot parse the arguments.
    * By default the parser prints the error message and exits the VM
    * (through `System.exit`).
    */
  def throwExceptionOnError(): Parser = {
    this.exceptionOnError = true
    checkExceptionOnErrorAndExitCode()
    this
  }

  /**
    * Define which code is used to exit the program if the parser is unabled to parse the arguments.
    * Default exit-code is 1.
    *
    * @note This option cannot be used together with `noUsageOnError`.
    */
  // TODO use a macro to enforce the exclusion with noUsageOnError?
  def exitCode(code: Int): Parser = {
    this.customExitCode = Some(code)
    checkExceptionOnErrorAndExitCode()
    this
  }

  private def checkExceptionOnErrorAndExitCode(): Unit = {
    if (exceptionOnError && customExitCode.isDefined) {
      sys.error("Cannot use both exit-code and throws-exception-on-error.")
    }
  }

  def withProgramName(name: String): Parser = {
    this.customProgramName = Some(name)
    this
  }

  /**
    * Customize the name of the help command, by default 'help'.
    */
  def withHelpCommand(name: String): Parser = {
    this.helpCmd = Some(name)
    this
  }

  /**
    * Customize the usage message.
    */
  def withUsage(usage: Usage): Parser = {
    this.usage = usage
    this
  }

  def parse(args: Array[String]): Parser = {
    this.args = args.toList
    this
  }

  // terminal methods, i.e that execute the parser

  /**
    * @return None if version or help is executed
    * @throws ParsingException if `noUsageOnError` is enabled
    */
  def withCommands[T <: Command : Manifest](withCommands: T*): Option[T] = {
    val commands = Commands(withCommands: _*)

    if (parseVersion() || parseHelp(commands)) {
      None
    } else {
      withParsingException(commands) {
        args.indexWhere(!_.startsWith("-")) match {
          case -1 => throw ParsingException("No command found, expected one of " +
            commands.commands.map(_.label).toList.sorted.map(name => ansi"%bold{$name}").mkString(", "))

          case idx =>
            val (globalOptions, cmdName :: params) = args.splitAt(idx)
            commands.findByName(cmdName) match {
              case None => throw ParsingException(s"Unknown command '$cmdName'")
              case Some(cmd) =>
                cmd.read(params ::: globalOptions)
                Some(cmd.asInstanceOf[T])
            }
        }
      }
    }
  }

  def withCommand[C <: Command : Manifest, R](command: C)(f: C => R = { a: C => () }): Option[R] = {
    val commands = Commands(command)
    if (parseVersion() || parseHelp(commands)) {
      None
    } else {
      withParsingException(commands) {
        command.read(args)
        Some(f(command))
      }
    }
  }

  private def programName: String = {
    customProgramName.getOrElse(guessProgramName)
  }

  private def guessProgramName: String = {
    val stack = Thread.currentThread.getStackTrace
    val main = stack.last
    main.getClass.spinalCaseName
  }

  private def parseVersion(): Boolean = {
    withFirstArg { case arg if versionCmd == Some(arg) =>
      console.println(version.get)
    }
  }

  private def parseHelp(commands: Commands): Boolean = {
    withFirstArg { case arg if helpCmd == Some(arg) =>
      console.println(usage.show(programName, commands))
    }
  }

  private def withFirstArg(pf: PartialFunction[String, Unit]): Boolean = {
    args.headOption match {
      case Some(arg) if pf.isDefinedAt(arg) => pf(arg); true
      case _ => false
    }
  }

  private def withParsingException[T](commands: Commands)(f: => T): T = {
    try {
      f
    } catch {
      case e: ParsingException => fail(Right(e), commands)
    }
  }

  private def fail(error: Either[String, ParsingException], commands: Commands): Nothing = {
    if (showUsageOnError) {
      console.println(usage.show(programName, commands))
    }
    if (exceptionOnError) {
      error match {
        case Left(msg) => throw ParsingException(msg)
        case Right(ex) => throw ex
      }
    } else {
      console.println(error.fold(identity, _.msg))
      exit.exit(customExitCode.getOrElse(defaultExitCode))
    }
  }
}