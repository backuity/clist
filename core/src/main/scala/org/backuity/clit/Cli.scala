package org.backuity.clit

object Cli {

  def parse(args: Array[String])(implicit console: Console, exit: Exit): Parser = {
    new Parser().parse(args)
  }
}

