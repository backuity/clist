package org.backuity.clit

import org.backuity.matchete.JunitMatchers
import org.junit.Test

class ValidationUtilsTest extends JunitMatchers with ValidationUtils {

  @Test
  def testMoreThanOne(): Unit = {
    moreThanOne(true, false, true, false) must beTrue
    moreThanOne(true, true) must beTrue

    moreThanOne() must beFalse
    moreThanOne(true) must beFalse
    moreThanOne(false) must beFalse
    moreThanOne(false, false) must beFalse
    moreThanOne(true, false, false, false) must beFalse
  }

  @Test
  def testOnlyOne(): Unit = {
    onlyOne(true) must beTrue
    onlyOne(true, false, false) must beTrue
    onlyOne(false, false, true) must beTrue

    onlyOne() must beFalse
    onlyOne(false) must beFalse
    onlyOne(true, true) must beFalse
    onlyOne(true, true, false) must beFalse
  }

}
