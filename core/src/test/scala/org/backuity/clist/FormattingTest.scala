package org.backuity.clist

import org.backuity.clist.Formatting.ClassUtil
import org.backuity.matchete.JunitMatchers
import org.junit.Test

class FormattingTest extends JunitMatchers {

    @Test
    def classNameToCommandLine() : Unit = {
        classOf[FormattingTest.Simple].spinalCaseName must_== "simple"
        classOf[FormattingTest.RunAndMakeCafe].spinalCaseName must_== "run-and-make-cafe"
    }
}

object FormattingTest {
    class Simple

    class RunAndMakeCafe
}
