package org.backuity.cli

class Parser {
  private var version : Option[String] = None
  private var help : Option[String] = Some("help")
  private var usage : Usage = Usage.Default
  private var args: List[String] = Nil

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

  def parse(args: Array[String]) : Parser = {
    this.args = args.toList
    this
  }

  // terminal methods, i.e that execute the parser

  def withCommands[T <: Command : Manifest](withCommands : T*) : T = {
    val commands = Commands(withCommands : _*)
    val (globalOptions,commandOptions) = args.splitAt(args.indexWhere( ! _.startsWith("-")))
    commandOptions match {
      case Nil => throw ParsingException("No command found")
      case cmdName :: params =>
        commands.findByName(cmdName) match {
          case None => throw ParsingException("Command " + cmdName + " does not exist")
          case Some(cmd) =>
            cmd.read(params ::: globalOptions)
            cmd.asInstanceOf[T]
        }
    }
  }

  def withStaticCommand(command: Command): Unit = {
    command.read(args)
  }

  def withCommand[C <: Command, R](command: C)(onSuccess: C => R): R = {
    command.read(args)
    onSuccess(command)
  }
}