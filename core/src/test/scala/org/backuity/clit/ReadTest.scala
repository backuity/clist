package org.backuity.clit

import org.backuity.matchete.JunitMatchers
import org.junit.Test

class ReadTest extends JunitMatchers {

    @Test
    def javaEnum(): Unit = {
        implicitly[Read[Season]].reads("winter") must_== Season.WINTER
    }

    @Test
    def incorrectJavaEnum(): Unit = {
        implicitly[Read[Season]].reads("summr") must throwA[ReadException].`with`("expectations") {
            case ReadException(value,expected) =>
                expected must_== "one of autumn,spring,summer,winter"
                value must_== "summr"
        }
    }
}
