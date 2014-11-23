package cli

trait Usage {
  def show(commands: Commands) : String
}

object Usage {

  object Default extends Usage {

    import org.backuity.ansi.AnsiFormatter.FormattedHelper

    override def show(commands: Commands): String = {

      val usage = new java.lang.StringBuilder()
      var indentLevel = 0
      var beginning = true

      def incIndent(): Unit = { indentLevel += 1 }
      def decIndent(): Unit = { indentLevel -= 1 }
      def indent( f : => Unit ): Unit = { incIndent(); f; decIndent() }

      def addLine(str: String = ""): Unit = { add(str + "\n") }
      def add(str: String): Unit = {
        val pad = if( beginning ) "\t" * indentLevel else ""
        usage.append(pad + str)
        if( str.endsWith("\n") ) {
          beginning = true
        } else {
          beginning = false
        }
      }

      def argLabel(arg: Argument[_]) : String = {
        (arg.abbrev match {
          case Some(abbrev) => "-" + abbrev + (if( arg.name.isDefined ) ", " else "")
          case None         => ""
        }) + (arg.name match {
          case Some(name) => "--" + name + (if( arg.tpe == classOf[Int] || arg.tpe == classOf[Long] ) {
            "=NUM"
          } else if( arg.tpe == classOf[String] ) {
            "=STRING"
          } else "")
          case None       => ""
        })
      }

      def argText(labelMaxSize: Int, arg: Argument[_]) : String = {
        val label = argLabel(arg)
        val description = arg.description.getOrElse("")
        val default = arg.default match {
          case Some(d) if d != false => ansi"%italic{(default: $d)}"
          case _ => ""
        }
        val padding = " " * (labelMaxSize - label.length)

        ansi"%yellow{$label}" + (if( description.isEmpty && default.isEmpty ) {
          ""
        } else {
          padding + " : " + description + (if( !description.isEmpty && !default.isEmpty ) " " else "") + default
        })
      }

      def addArguments(args: Set[Argument[_]]): Unit = {
        val labelMaxSize = args.map(argLabel).map(_.length).max

        for( arg <- args ) {
          addLine(argText(labelMaxSize, arg))
        }
      }

      addLine(ansi"%underline{Usage}")
      addLine()
      addLine(ansi" %bold{cli} %yellow{[options]} %bold{command} %yellow{[command options]}")
      addLine()
      addLine(ansi"%underline{Options:}")
      addLine()
      indent {
        addArguments(commands.arguments)
      }

      addLine()
      addLine(ansi"%underline{Commands:}")
      indent {
        for (command <- commands.commands) {
          addLine()
          add(ansi"%bold{${command.label}}")
          val commandSpecificArgs = command.arguments -- commands.arguments
          val description = if( command.description != "" ) " : " + command.description else ""
          if (commandSpecificArgs.isEmpty) {
            addLine(description)
          } else {
            addLine(" [command options]" + description)
            indent {
              addArguments(commandSpecificArgs)
            }
          }
        }
      }

      usage.toString
    }
  }
}