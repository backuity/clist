package org.backuity.clist

object Formatting {

  implicit class StringUtil(val str: String) extends AnyVal {
    /**
      * Turn "HelloWorld" into "hello-world"
      */
    def toSpinalCase: String = {
      str.replaceAll("([a-z])([A-Z])", "$1-$2").toLowerCase
    }
  }

  implicit class ClassUtil(val clazz: Class[_]) extends AnyVal {
    /**
      * @return a spinal-cased simple name
      */
    def spinalCaseName: String = {
      val className = clazz.getSimpleName
      val simpleName = if (className.endsWith("$")) {
        className.substring(0, className.length - 1)
      } else {
        className
      }
      simpleName.toSpinalCase
    }
  }
}
