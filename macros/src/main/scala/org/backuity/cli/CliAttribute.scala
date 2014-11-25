package org.backuity.cli

sealed abstract class CliAttribute[T] {
  val tpe: Class[T]
  val name: String
  val description: Option[String]
  val default: Option[T]
  val reader: Read[T]
}

case class CliArgument[T](tpe: Class[T],
                          name: String,
                          description: Option[String],
                          required: Boolean,
                          default: Option[T])(val reader: Read[T]) extends CliAttribute[T] {
}

case class CliOption[T](tpe: Class[T],
                     longName: Option[String],
                     description: Option[String],
                     abbrev: Option[String],
                     default: Option[T])(val reader: Read[T]) extends CliAttribute[T] {

  val name = longName.getOrElse(abbrev.get)
}

object CliOption {
  // this class cannot be within Cli due to some macros restrictions
  class Builder[T : Read : Manifest](command: Command, varName: String) {

    /**
     * - unless it is a boolean, an optional argument must have a default value
     * - a boolean cannot have a default value (we want to avoid a boolean being true
     *   by default.. it would always be true)
     */
    def apply[U <: T](name: String = null,
                      description: String = null,
                      abbrev: String = null,
                      abbrevOnly: String = null,
                      default: T = null.asInstanceOf[T]) : T = {

      def fail(msg: String) {
        throw new IllegalArgumentException(s"Incorrect argument $varName: $msg")
      }

      // TODO check those at compile time (when SI-5920 gets fixed)

      if( abbrevOnly != null && abbrev != null ) {
        fail("cannot define both abbrev and abbrevOnly")
      }
      if( default == null && manifest[T].runtimeClass != classOf[Boolean] ) {
        fail("a non-boolean option must have a default value")
      }
      // TODO we really want to forbid setting the default value but we have no way to check that..
      //      ..it seems that `default: T = null.asInstanceOf[T]` does not get a null for primitive types
      //      but rather a default value.
      if( manifest[T].runtimeClass == classOf[Boolean] && default.asInstanceOf[Boolean]) {
        fail(s"a boolean argument cannot have a default value set to true")
      }

      command.addOption(CliOption(
        manifest[T].runtimeClass.asInstanceOf[Class[T]],
        Option(name).orElse(Some(varName.trim)),
        Option(description),
        Option(abbrevOnly).orElse(Option(abbrev)),
        Option(default))(implicitly[Read[T]]))

      default
    }
  }
}

object CliArgument {
  class Builder[T : Read : Manifest](command: Command, varName: String) {

    /**
     * - unless it is a boolean, an optional argument must have a default value
     * - a boolean cannot have a default value (we want to avoid a boolean being true
     *   by default.. it would always be true)
     */
    def apply[U <: T](name: String = null,
                      description: String = null,
                      required: Boolean = true,
                      default: T = null.asInstanceOf[T]) : T = {

      def fail(msg: String) {
        throw new IllegalArgumentException(s"Incorrect argument $varName: $msg")
      }

      // TODO check those at compile time (when SI-5920 gets fixed)

      if( default == null && !required
          && manifest[T].runtimeClass != classOf[Option[_]]
          && manifest[T].runtimeClass != classOf[Boolean] ) {
        fail("a non-boolean optional argument must have a default value")
      }
      // TODO we really want to forbid setting the default value but we have no way to check that..
      //      ..it seems that `default: T = null.asInstanceOf[T]` does not get a null for primitive types
      //      but rather a default value.
      if( manifest[T].runtimeClass == classOf[Boolean] && default.asInstanceOf[Boolean]) {
        fail(s"a boolean argument cannot have a default value set to true")
      }

      command.addArgument(CliArgument(
        manifest[T].runtimeClass.asInstanceOf[Class[T]],
        Option(name).getOrElse(varName.trim),
        Option(description),
        required,
        Option(default))(implicitly[Read[T]]))

      default
    }
  }
}