package org.backuity.cli

/** @param name if not specified the lower-cased class name will be used */
abstract class Command(name: String = null, val description: String = "") {

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