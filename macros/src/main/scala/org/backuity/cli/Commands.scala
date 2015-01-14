package org.backuity.cli

/**
 * @param commands a non-empty set of commands
 */
case class Commands private( options : Set[CliOption[_]],
                     commands : Set[Command]) {
  def size = commands.size

  val commandsSortedByLabel = commands.toList.sortBy(_.label)

  def findByName(name: String) : Option[Command] = {
    commands.find( _.label == name)
  }
}

object Commands {

  /** Build a Commands by infering its global arguments
    * @param commands must not be empty
    */
  def apply[T <: Command : Manifest](commands : T*) : Commands = {
    if( commands.isEmpty ) throw new IllegalArgumentException("Cannot constructor an empty Commands")
    val commonArgs = commands.map(_.options).reduce(_.intersect(_))
    new Commands(commonArgs, commands.toSet)
  }
}