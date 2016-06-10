package org.backuity.clist

abstract class CliMain(name: String = null, description: String = "") extends Command(name, description) {

  final def main(args: Array[String]): Unit = {
    Cli.parse(args).withCommand(this) { _ => run }
  }

  def run: Unit
}
