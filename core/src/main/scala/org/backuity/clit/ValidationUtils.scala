package org.backuity.clit

trait ValidationUtils {

  /** @return true if none of b are true */
  def none(b: Boolean*): Boolean = {
    b.forall(b => !b)
  }

  /** @return true if one and only one b is true */
  def onlyOne(b: Boolean*): Boolean = {
    b.count(b => b) == 1
  }

  /** @return true if more than one b is true */
  def moreThanOne(b: Boolean*): Boolean = {
    b.count(b => b) > 1
  }
}
