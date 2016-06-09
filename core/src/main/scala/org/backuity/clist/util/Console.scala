package org.backuity.clist.util

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

    def clear(): Unit = {
      buffer.setLength(0)
    }

    def content: String = buffer.toString

    override def toString: String = {
      "Console.InMemory(bufferSize=" + buffer.length() + ")"
    }
  }
}