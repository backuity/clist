package cli

case class Argument[T](tpe: Class[_],
                       name: Option[String],
                       description: Option[String],
                       abbrev: Option[String],
                       required: Boolean,
                       default: Option[T])(val reader: Read[T]) {
}