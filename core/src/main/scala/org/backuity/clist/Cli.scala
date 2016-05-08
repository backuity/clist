package org.backuity.clist

import org.backuity.clist.util.{Console, Exit}

object Cli {

  def parse(args: Array[String])(implicit console: Console, exit: Exit): Parser = {
    new Parser(args.toList)
  }
}

