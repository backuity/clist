package org.backuity.clist

import org.backuity.ansi.AnsiFormatter.FormattedHelper

/**
  * @param commands a non-empty set of commands
  */
case class Commands private(options: Set[CliOption[_]],
                            commands: Set[Command]) {
  def size: Int = commands.size

  def labels: Set[String] = commands.map(_.label)

  val commandsSortedByLabel = commands.toList.sortBy(_.label)

  def findByName(name: String): Option[Command] = {
    commands.find(_.label == name)
  }

  def ansiList: String = {
    labels.toList.sorted.map(name => ansi"%bold{$name}").mkString(", ")
  }
}

object Commands {

  /** Build a Commands by infering its global arguments
    * @param commands must not be empty
    */
  def apply[T <: Command : Manifest](commands: T*): Commands = {
    if (commands.isEmpty) throw new IllegalArgumentException("Commands must have at least one command")
    val commonArgs = commands.map(_.options).reduce(_.intersect(_))
    new Commands(commonArgs, commands.toSet)
  }
}