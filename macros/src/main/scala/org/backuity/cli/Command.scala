package org.backuity.cli

import org.backuity.cli.Formatting.ClassUtil


/** @param name if not specified the lower-cased class name will be used */
abstract class Command(name: String = null, val description: String = "") {

  /** @throws ParsingException if arguments or options cannot be parsed */
  def read(args: List[String]) = {

    var remainingArgs = args

    parseArguments()
    parseOptions()

    // ------------------------------------

    def popArg(): Option[String] = {
      remainingArgs match {
        case Nil => None
        case head :: tail =>
          remainingArgs = tail
          Some(head)
      }
    }

    def hasArg : Boolean = remainingArgs.nonEmpty

    def parseArguments(): Unit =  {
      for( cmdArg <- arguments ) {
        popArg() match {
          case None =>
            cmdArg match {
              case _ : CliMandatoryArgument[_] =>
                throw ParsingException("No argument provided for " + cmdArg.name)
              case optArg : CliOptionalArgument[_] =>
                setVar(cmdArg, optArg.default)
            }

          case Some(arg) =>
            readAndSetVar(cmdArg, arg)
        }
      }
    }

    def parseOptions(): Unit = {
      var processedOptions = Set.empty[CliOption[_]]

      def remainingOptions = options -- processedOptions

      for( arg <- remainingArgs ) {
        findOptionForArg(remainingOptions, arg) match {
          case None => throw ParsingException("No option found for " + arg)
          case Some((option,value)) =>
            processedOptions += option
            readAndSetVar(option, value)
        }
      }

      for( option <- remainingOptions ) {
        setVar(option, option.default)
      }
    }
  }

  /**
   * @return the matching option along with its value
   */
  private def findOptionForArg(options : Set[CliOption[_]], arg: String) : Option[(CliOption[_], String)] = {
    for( option <- options ) {
      option.abbrev.map { abbrev =>
        if( arg == ("-" + abbrev) ) {
          return Some(option, "")
        }
      }
      option.longName.map { longName =>
        if( arg == ("--" + longName) ) {
          return Some(option, "")
        }
        val key = "--" + longName + "="
        if( arg.startsWith(key) ) {
          return Some(option, arg.substring(key.length))
        }
      }
    }
    None
  }

  private[this] def readAndSetVar(arg: CliAttribute[_], strValue: String): Unit = {
    try {
      val value = arg.reader.reads(strValue)
      setVar(arg, value)
    } catch {
      case ReadException(value, expected) =>
        throw new ParsingException(s"Incorrect parameter ${arg.name} '$value', expected $expected")
    }
  }

  private[this] def setVar(arg: CliAttribute[_], value: Any): Unit = {
    setVar(arg.commandAttributeName, arg.tpe, value)
  }

  private[this] def setVar(name: String, tpe: Class[_], value: Any): Unit = {
    getClass.getMethod(name + "_$eq", tpe).invoke(this, value.asInstanceOf[Object])
  }

  def label = _name
  def arguments = _arguments
  def options = _options

  private[this] val _name = if( name != null ) name else {
    getClass.spinalCaseName
  }

  private[this] var _arguments : Set[CliArgument[_]] = Set.empty
  private[cli] def addArgument(arg: CliArgument[_]): Unit = {
    _arguments += arg
  }

  private[this] var _options : Set[CliOption[_]] = Set.empty
  private[cli] def addOption(opt: CliOption[_]): Unit = {
    _options += opt
  }

  def validate() : Unit = {}
}