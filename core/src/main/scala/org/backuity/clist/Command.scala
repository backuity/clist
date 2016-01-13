package org.backuity.clist

import org.backuity.clist.Formatting.ClassUtil


/** @param name if not specified the lower-cased class name will be used */
abstract class Command(name: String = null, val description: String = "") extends ValidationUtils {

  /** @throws ParsingException if arguments or options cannot be parsed */
  def read(args: List[String]) = {

    var remainingArgs = args

    parseOptions()
    parseArguments()
    applyValidators()

    // ------------------------------------

    def popArg(): Option[String] = {
      remainingArgs match {
        case Nil => None
        case head :: tail =>
          remainingArgs = tail
          Some(head)
      }
    }

    def hasArg: Boolean = remainingArgs.nonEmpty

    def parseArguments(): Unit = {
      for (cmdArg <- arguments) {
        cmdArg match {
          case sCmdArg: SingleCliArgument[_] =>
            popArg() match {
              case None =>
                sCmdArg match {
                  case _: CliMandatoryArgument[_] =>
                    throw ParsingException("No argument provided for " + cmdArg.name)
                  case optArg: CliOptionalArgument[_] =>
                    setVar(cmdArg, optArg.default)
                }

              case Some(arg) =>
                readAndSetVar(sCmdArg, arg)
            }

          case mult: MultipleCliArgument[_] =>
            if (remainingArgs.isEmpty) {
              throw new ParsingException(s"Insufficient arguments for ${mult.name}")
            } else {
              try {
                val value = mult.reader.reads(remainingArgs)
                setVar(mult, value)
                remainingArgs = Nil
              } catch {
                case ReadException(value, expected) =>
                  throw new ParsingException(s"Incorrect parameter ${mult.name} '$value', expected $expected")
              }
            }
        }
      }

      if (hasArg) {
        throw new ParsingException("Too many arguments: " + remainingArgs.mkString(", "))
      }
    }

    def parseOptions(): Unit = {
      val argsToParse = remainingArgs.takeWhile(_.startsWith("-"))
      var processedOptions = Set.empty[CliOption[_]]

      def remainingOptions = options -- processedOptions

      for (arg <- argsToParse) {
        findOptionForArg(remainingOptions, arg) match {
          case None => throw ParsingException("No option found for " + arg)
          case Some((option, value)) =>
            popArg()
            processedOptions += option
            readAndSetVar(option, value)
        }
      }

      for (option <- remainingOptions) {
        setVar(option, option.default)
      }
    }
  }

  /**
    * @return the matching option along with its value
    */
  private def findOptionForArg(options: Set[CliOption[_]], arg: String): Option[(CliOption[_], String)] = {
    for (option <- options) {
      option.abbrev.foreach { abbrev =>
        if (arg == ("-" + abbrev)) {
          return Some(option, "")
        }
      }
      option.longName.foreach { longName =>
        if (arg == ("--" + longName)) {
          return Some(option, "")
        }
        val key = "--" + longName + "="
        if (arg.startsWith(key)) {
          return Some(option, arg.substring(key.length))
        }
      }
    }
    None
  }

  private[this] def readAndSetVar(arg: CliAttribute[_] with SingleArgAttribute[_], strValue: String): Unit = {
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

  private[this] val _name = if (name != null) name
  else {
    getClass.spinalCaseName
  }

  private[this] var _arguments: List[CliArgument[_]] = Nil

  /**
    * Add to the end of the argument list.
    */
  private[clist] def enqueueArgument(arg: CliArgument[_]): Unit = {
    _arguments :+= arg
  }

  private[this] var _options: Set[CliOption[_]] = Set.empty
  private[clist] def addOption(opt: CliOption[_]): Unit = {
    _options += opt
  }

  private[this] var _validators: Set[Unit => Unit] = Set.empty

  def validate(validator: => Unit): Unit = {
    _validators += { _ => validator }
  }

  def validators: Set[Unit => Unit] = _validators

  private def applyValidators(): Unit = {
    _validators.foreach(_.apply(()))
  }

  def parsingError(msg: String): Nothing = {
    throw new ParsingException(msg)
  }
}