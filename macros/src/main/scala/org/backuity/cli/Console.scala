package org.backuity.cli

trait Console {

  def print(message: Any): Unit

  def println(message: Any): Unit
}

object Console {
  implicit val out = new Console {
    def print(message: Any): Unit = {
      scala.Console.print(message)
    }

    def println(message: Any): Unit = {
      scala.Console.println(message)
    }
  }

  /** thread-safe */
  class InMemory extends Console {
    private val buffer = new StringBuffer

    def print(message: Any): Unit = {
      append(message.toString)
    }

    def println(message: Any): Unit = {
      append(message.toString + System.lineSeparator())
    }

    private def append(message: String): Unit = {
      buffer.append(message)
    }

    override def toString : String = {
      buffer.toString
    }
  }
}