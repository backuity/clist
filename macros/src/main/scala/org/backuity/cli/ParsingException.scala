package org.backuity.cli

case class ParsingException(msg: String) extends RuntimeException(msg) {
}
