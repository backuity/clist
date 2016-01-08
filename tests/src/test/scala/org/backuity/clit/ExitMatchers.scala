package org.backuity.clit

import org.backuity.matchete.MatcherSupport

trait ExitMatchers extends MatcherSupport {

  implicit val exit = Exit.withException

  def exitWithCode(code: Int) = throwAn[ExitException].`with`("an exit code " + code) {
    case ExitException(c) => c must_== code
  }
}
