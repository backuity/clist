package org.backuity.cli

trait Exit {
  def exit(code: Int) : Nothing
}

case class ExitException(code:Int) extends RuntimeException

object Exit {
  implicit val system = new Exit {
    def exit(code: Int): Nothing = {
      sys.exit(code)
    }
  }

  /** throws an `ExitException` when exiting */
  val withException = new Exit {
    def exit(code: Int): Nothing = {
      throw ExitException(code)
    }
  }
}


