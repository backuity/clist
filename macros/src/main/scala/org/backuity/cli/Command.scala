package org.backuity.cli

import sun.security.pkcs.ParsingException

/** @param name if not specified the lower-cased class name will be used */
abstract class Command(name: String = null, val description: String = "") {
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
              case _ : CliMandatoryArgument =>
                throw ParsingException("No argument provided for " + cmdArg.name)
              case optArg : CliOptionalArgument =>
                setVar(cmdArg, optArg.default)
            }

          case Some(arg) =>
            readAndSetVar(cmdArg, arg)
        }
      }
    }

    def parseOptions(): Unit = {
      var processedOptions = Set.empty[CliOption[_]]

      for( arg <- remainingArgs ) {
        findOptionForArg(arg) match {
          case None => throw ParsingException("No option found for " + arg)
          case Some((option,value)) =>
            processedOptions += option
            setVar(option, value)
        }
      }
    }
  }

  private def findOptionForArg(arg: String) : Option[(CliOption[_], String)] = {
    for( option <- options ) {
      option.abbrev match {
        case Some(abbrev) => if( arg == ("-" + abbrev)) {
          
        }
      }
    }
  }

  private[this] def readAndSetVar(arg: CliAttribute[_], strValue: String): Unit = {
    val value = arg.reader.reads(strValue)
    setVar(arg, value)
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
    val className = getClass.getSimpleName.toLowerCase
    if( className.endsWith("$") ) {
      className.substring(0, className.length - 1)
    } else {
      className
    }
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