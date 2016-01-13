package org.backuity.clist

object Cli {

  def parse(args: Array[String])(implicit console: Console, exit: Exit): Parser = {
    new Parser(args.toList)
  }
}

