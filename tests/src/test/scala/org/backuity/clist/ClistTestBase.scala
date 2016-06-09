package org.backuity.clist

import org.backuity.clist.util.Console
import org.backuity.matchete.JunitMatchers

trait ClistTestBase extends JunitMatchers with ExitMatchers {

  implicit val console = new Console.InMemory
}
