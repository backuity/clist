package org.backuity.clist

import org.backuity.clist.util.{Formatting, Read, ReadMultiple}
import Formatting._

sealed abstract class CliAttribute[T] {
  val tpe: Class[T]
  val name: String

  /**
    * Name of the command attribute that will receive the value.
    */
  val commandAttributeName: String

  val description: Option[String]

  /** Whether this attribute is of type `Boolean` */
  def isBoolean = tpe == classOf[Boolean]
}

trait SingleArgAttribute[T] { this: CliAttribute[T] =>
  val reader: Read[T]
}

/**
  * An argument is a value directly passed to a command. It may be optional.
  */
abstract sealed class CliArgument[T] extends CliAttribute[T]

sealed trait SingleCliArgument[T] extends CliArgument[T] with SingleArgAttribute[T]

case class CliOptionalArgument[T](tpe: Class[T],
                                  commandAttributeName: String,
                                  name: String,
                                  description: Option[String],
                                  default: T)
                                 (val reader: Read[T]) extends SingleCliArgument[T] {
}

case class CliMandatoryArgument[T](tpe: Class[T],
                                   commandAttributeName: String,
                                   name: String,
                                   description: Option[String])
                                  (val reader: Read[T]) extends SingleCliArgument[T] {
}

case class MultipleCliArgument[T](tpe: Class[T],
                                  commandAttributeName: String,
                                  name: String,
                                  description: Option[String])
                                 (val reader: ReadMultiple[T]) extends CliArgument[T] {
}


/**
  * As opposed to an argument an option is always optional.
  */
case class CliOption[T](tpe: Class[T],
                        commandAttributeName: String,
                        longName: Option[String],
                        description: Option[String],
                        abbrev: Option[String],
                        useEnv: Option[Boolean],
                        default: T)(val reader: Read[T]) extends CliAttribute[T] with SingleArgAttribute[T] {

  val name = longName.getOrElse(abbrev.get)
}

object CliAttribute {

  class Builder[T: Read : Manifest](varName: String) {

    val clazz: Class[_] = manifest[T].runtimeClass

    def fail(msg: String) {
      throw new IllegalArgumentException(s"Incorrect argument '$varName': $msg")
    }

    def validateDefault(default: T): T = {

      // TODO check those at compile time (when SI-5920 gets fixed)

      if (default == null
        && clazz != classOf[Option[_]]
        && clazz != classOf[Boolean]) {

        fail("an optional argument that has neither type Option nor Boolean must have a default value")
      }

      (if (default != null) {
        default
      } else if (clazz == classOf[Option[_]]) {
        None
      } else if (clazz == classOf[Boolean]) {
        false
      }).asInstanceOf[T]
    }
  }


}

object CliOption {
  // this class cannot be within Cli due to some macros restrictions
  class Builder[T: Read : Manifest](command: Command, varName: String) extends CliAttribute.Builder[T](varName) {

    val spinalCasedVarName = varName.toSpinalCase

    /**
      * - unless it is a boolean, an optional argument must have a default value
      * - a boolean cannot have a default value (we want to avoid a boolean being true
      *   by default.. it would always be true)
      *
      * @param abbrevOnly when set, disable the long form (`--name`)
      */
    def apply[U <: T](name: String = null,
                      description: String = null,
                      abbrev: String = null,
                      abbrevOnly: String = null,
                      useEnv: Boolean = false,
                      default: T = null.asInstanceOf[T]): T = {

      // TODO check those at compile time (when SI-5920 gets fixed)

      if (clazz != classOf[Boolean] && (abbrev != null || abbrevOnly != null)) {
        fail("only boolean options can have an abbreviation")
      }
      if (abbrevOnly != null && abbrev != null) {
        fail("cannot define both abbrev and abbrevOnly")
      }

      val nonNullDefault = validateDefault(default)
      val longName = if (abbrevOnly != null) {
        None
      } else {
        Option(name).orElse(Some(spinalCasedVarName.trim))
      }

      command.addOption(CliOption(
        tpe         = manifest[T].runtimeClass.asInstanceOf[Class[T]],
        commandAttributeName = varName,
        longName    = longName,
        description = Option(description),
        abbrev      = Option(abbrevOnly).orElse(Option(abbrev)),
        useEnv      = Some(useEnv),
        default     = nonNullDefault)(implicitly[Read[T]]))

      default
    }
  }
}

object CliArgument {
  class Builder[T: Read : Manifest](command: Command, varName: String) extends CliAttribute.Builder[T](varName) {

    private val spinalCasedVarName = varName.toSpinalCase

    /**
      * - unless it is a boolean, an optional argument must have a default value
      * - a boolean cannot have a default value (we want to avoid a boolean being true
      * by default.. it would always be true)
      */
    def apply[U <: T](name: String = null,
                      description: String = null,
                      required: Boolean = true,
                      default: T = null.asInstanceOf[T]): T = {

      // TODO check those at compile time (when SI-5920 gets fixed)

      if (required) {

        if (default != null && classOf[AnyVal].isAssignableFrom(clazz)) {
          fail("cannot specify a default value for a required argument")
        }

        command.enqueueArgument(CliMandatoryArgument(
          tpe                  = manifest[T].runtimeClass.asInstanceOf[Class[T]],
          commandAttributeName = varName,
          name                 = Option(name).getOrElse(spinalCasedVarName.trim),
          description          = Option(description))(implicitly[Read[T]]))
      } else {

        val nonNullDefault = validateDefault(default)

        command.enqueueArgument(CliOptionalArgument(
          tpe                  = manifest[T].runtimeClass.asInstanceOf[Class[T]],
          commandAttributeName = varName,
          name                 = Option(name).getOrElse(spinalCasedVarName.trim),
          description          = Option(description),
          default              = nonNullDefault)(implicitly[Read[T]]))
      }

      default
    }
  }
}

object MultipleCliArgument {
  class Builder[T: ReadMultiple : Manifest](command: Command, varName: String) {

    /**
      * - unless it is a boolean, an optional argument must have a default value
      * - a boolean cannot have a default value (we want to avoid a boolean being true
      * by default.. it would always be true)
      */
    def apply[U <: T](name: String = null,
                      description: String = null): T = {

      command.enqueueArgument(MultipleCliArgument(
        tpe                  = manifest[T].runtimeClass.asInstanceOf[Class[T]],
        commandAttributeName = varName,
        name                 = Option(name).getOrElse(varName.toSpinalCase.trim),
        description          = Option(description))(
        implicitly[ReadMultiple[T]]))

      null.asInstanceOf[T]
    }
  }
}
