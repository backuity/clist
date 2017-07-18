package org.backuity.clist

import scala.language.implicitConversions

/**
  * Bring more info about this project into Usage.show()
  *
  * Further info could be added here. e.g. Reference, CopyRight, etc.
  */
private final case class ProgramInfo(name: String,
                                     shortDescription: Option[String] = None,
                                     fullDescription: Option[String] = None,
                                     author: Set[String] = Set.empty,
                                     license: Option[String] = None,
                                     custom: Map[String, (String, String)] = Map.empty)

private object ProgramInfo {
  implicit def byName(name: String): ProgramInfo = ProgramInfo(name)
}