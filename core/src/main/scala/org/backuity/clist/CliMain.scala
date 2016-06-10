package org.backuity.clist

abstract class CliMain(name: String = null, description: String = "") extends Command(name, description) {

  final def main(args: Array[String]): Unit = {
    runWith(args)
  }

  /**
    * Convenience method for testing the CLI.
    * @return None if `args` are invalid or if the usage/help was requested
    */
  def runWith[T](args: Array[String]): Option[T] = {
    Cli.parse(args).withCommand(this) { _ => run }
  }

  def run[T]: T
}
