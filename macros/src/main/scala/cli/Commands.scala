package cli

case class Commands( arguments : Set[Argument[_]],
                     commands : Set[Command])

object Commands {
  /** Build a Commands by infering its global arguments */
  def apply[T <: Command : Manifest](commands : T*) : Commands = {
    if( commands.size < 2 ) sys.error("A Commands must have at least 2 commands")

    val commonArgs = commands.map(_.arguments).reduce( _.intersect(_) )
    new Commands(commonArgs, commands.toSet)
  }
}