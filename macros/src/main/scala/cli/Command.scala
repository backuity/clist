package cli

/** @param name if not specified the lower-cased class name will be used */
abstract class Command(name: String = null, val description: String = "") {

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