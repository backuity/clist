
import scala.util.parsing.combinator.RegexParsers

/**
 * Copied from https://github.com/drdozer/ansi_color
 */
object AnsiFormatter {

  implicit class FormattedHelper(val sc: StringContext) extends AnyVal {
    private def parseAndFormat(fmt: Formatter)(args: Seq[Any]): String = {
      val ipl = sc.standardInterpolator(identity, args)
      val p = FLParser.parseAll(FLParser.content, ipl)
      val sb = new StringBuilder
      fmt.format(p.get, Codes(), sb)
      sb.toString()
    }

    def ansi(args: Any*): String = parseAndFormat(AnsiFormatter)(args)

    def html(args: Any*): String = parseAndFormat(HtmlFormatter)(args)
  }

  trait Formatter {
    def format(ce: CommandExp, c: Codes, sb: StringBuilder): Unit

    def format(se: StringExp, c: Codes, sb: StringBuilder): Unit = sb append se.content

    def format(ce: CompoundExp, c: Codes, sb: StringBuilder): Unit = for(ch <- ce.children) format(ch, c, sb)

    def format(fle: FLExp, c: Codes, sb: StringBuilder): Unit = {
      fle match {
        case se: StringExp => format(se, c, sb)
        case ce: CommandExp => format(ce, c, sb)
        case ce: CompoundExp => format(ce, c, sb)
      }
    }
  }

  object FLParser extends RegexParsers {
    override def skipWhitespace = false

    val BACKSLASH: Parser[String] = """\"""
    val L_BRACKET: Parser[String] = """{"""
    val R_BRACKET: Parser[String] = """}"""

    lazy val escapedBS: Parser[StringExp] = BACKSLASH ~ BACKSLASH ^^
      { case a ~ b => StringExp(a + b) }

    lazy val raw_rB: Parser[StringExp] = R_BRACKET ^^ (StringExp apply _)

    lazy val word: Parser[String] = """\w+"""r

    lazy val command: Parser[CommandExp] = BACKSLASH ~ word ~ L_BRACKET ~ content ~ R_BRACKET ^^
      { case _ ~ w ~ _ ~ c ~ _ => CommandExp(w, c) }

    lazy val raw_text: Parser[String] = """[^\\}]+"""r

    lazy val text: Parser[StringExp] = raw_text ^^ (StringExp apply _)

    lazy val content: Parser[FLExp] = (command | escapedBS | text).* ^^ (CompoundExp apply _)
  }

  sealed trait FLExp

  case class StringExp(content: String) extends FLExp
  case class CommandExp(command: String, content: FLExp) extends FLExp
  case class CompoundExp(children: Seq[FLExp]) extends FLExp

  object AnsiFormatter extends Formatter {
    private val cCodes = Map(
      "black" -> 0,
      "red" -> 1,
      "green" -> 2,
      "yellow" -> 3,
      "blue" -> 4,
      "magenta" -> 5,
      "cyan" -> 6,
      "white" -> 7)

    override def format(ce: CommandExp, c: Codes, sb: StringBuilder) = ce.command match {
      case "bold" =>
        sb append "\u001b[1m"
        format(ce.content, c.copy(bold = true), sb)
        sb append "\u001b[22m"
      case "italic" =>
        sb append "\u001b[3m"
        format(ce.content, c.copy(italic = true), sb)
        sb append "\u001b[23m"
      case "Underline" =>
        sb append "\u001b[4m"
        format(ce.content, c.copy(underline = true), sb)
        sb append "\u001b[24m"
      case "blink" =>
        sb append "\u001b[5m"
        format(ce.content, c.copy(blink = true), sb)
        sb append "\u001b[25m"
      case "reverse" =>
        sb append "\u001b[7m"
        format(ce.content, c.copy(blink = true), sb)
        sb append "\u001b[27m"
      case color =>
        val cCode = cCodes(color)
        sb append s"\u001b[3${cCode}m"
        format(ce.content, c.copy(color = cCode), sb)
        sb append s"\u001b[3${c.color}m"
    }
  }

  object HtmlFormatter extends Formatter {
    override def format(ce: CommandExp, c: Codes, sb: StringBuilder) = ce.command match {
      case "bold" =>
        sb append "<b>"
        format(ce.content, c.copy(bold = true), sb)
        sb append "</b>"
      case "italic" =>
        sb append "<i>"
        format(ce.content, c.copy(bold = true), sb)
        sb append "</i>"
      case "Underline" =>
        sb append "<u>"
        format(ce.content, c.copy(bold = true), sb)
        sb append "</u>"
      case "blink" =>
        sb append "<blink>"
        format(ce.content, c.copy(bold = true), sb)
        sb append "</blink>"
      case "reverse" => // noop in html
        format(ce.content, c.copy(bold = true), sb)
      case color =>
        sb append s"<span style='color:$color'>"
        format(ce.content, c.copy(bold = true), sb)
        sb append "</span>"
    }
  }

  case class Codes(bold: Boolean = false,
                   italic: Boolean = false,
                   underline: Boolean = false,
                   blink: Boolean = false,
                   color: Integer = 9)
}
