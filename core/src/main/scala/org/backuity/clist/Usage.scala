package org.backuity.clist

trait Usage {
  def show(programName: String, commands: Commands): String
}

object Usage {

  object Default extends Usage {

    import org.backuity.ansi.AnsiFormatter.FormattedHelper

    private val indentString = "   "

    override def show(programName: String, commands: Commands): String = {

      val usage = new java.lang.StringBuilder()
      var indentLevel = 0
      var beginning = true

      @inline def incIndent(): Unit = { indentLevel += 1 }
      @inline def decIndent(): Unit = { indentLevel -= 1 }
      @inline def indent(f: => Unit): Unit = { incIndent(); f; decIndent() }

      @inline def addLine(str: String = ""): Unit = { add(str + "\n") }
      def add(str: String): Unit = {
        if (str.trim.isEmpty) {
          // do not pad for nothing
          usage.append(str)
        } else {
          val pad = if (beginning) indentString * indentLevel else ""
          val (trimmedStr, lineBreak) = if (str.endsWith("\n")) (str.substring(0, str.length - 1), "\n") else (str, "")
          val indentedStr = trimmedStr.replaceAll("\n", "\n" + (indentString * indentLevel))
          usage.append(pad + indentedStr + lineBreak)
        }
        if (str.endsWith("\n")) {
          beginning = true
        } else {
          beginning = false
        }
      }

      def addCommandSynopsis(command: Command,
                             commandSpecificOpts: Set[CliOption[_]],
                             optionLabel: String = "options"): Unit = {
        add(ansi"%bold{${command.label}}")
        val description = if (command.description != "") " : " + command.description else ""

        if (commandSpecificOpts.nonEmpty) {
          add(ansi" %yellow{[$optionLabel]}")
        }
        if (command.arguments.nonEmpty) {
          add(" " + command.arguments.map(showArg).mkString(" "))
        }
        addLine(description)
      }

      /** @param opts non empty */
      def addOptions(opts: Set[CliOption[_]]): Unit = {
        val labelMaxSize = opts.map(optLabel).map(_.length).max

        for (opt <- opts.toList.sortBy(_.name)) {
          addLine(optText(labelMaxSize, opt))
        }
      }

      addLine(ansi"%underline{Usage}")
      addLine()
      if (commands.size == 1) {
        val command = commands.commands.head
        add(" ")
        addCommandSynopsis(command, command.options)
        if (command.options.nonEmpty) {
          addLine()
          addLine(ansi"%underline{Options}")
          addLine()
          indent {
            addOptions(command.options)
          }
        }
      } else {
        // commands.size > 1
        addLine(ansi" %bold{$programName} %yellow{[options]} %bold{command} %yellow{[command options]}")
        if (commands.options.nonEmpty) {
          addLine()
          addLine(ansi"%underline{Options}")
          addLine()
          indent {
            addOptions(commands.options)
          }
        }
        addLine()
        addLine(ansi"%underline{Commands}")
        indent {
          for (command <- commands.commandsSortedByLabel) {
            addLine()
            val commandSpecificOpts = command.options -- commands.options
            addCommandSynopsis(command, commandSpecificOpts, optionLabel = "command options")

            // body
            // TODO args
            if (commandSpecificOpts.nonEmpty) {
              indent {
                addOptions(commandSpecificOpts)
              }
            }
          }
        }
      }
      usage.toString
    }

    private def optText(labelMaxSize: Int, arg: CliOption[_]): String = {
      val label = optLabel(arg)
      val description = arg.description.getOrElse("")
      val default = arg.default match {
        case Some(d) if d != false => ansi"%italic{(default: ${showDefault(d)})}"
        case _ => ""
      }
      val padding = " " * (labelMaxSize - label.length)
      val lineBreakPadding = " " * (labelMaxSize + 3 /* 3 = " : " */)
      val indentedDescription = description.replaceAll("\n", "\n" + lineBreakPadding)

      ansi"%yellow{$label}" + (if (description.isEmpty && default.isEmpty) {
        ""
      } else {
        padding + " : " + indentedDescription + (if (!description.isEmpty && !default.isEmpty) " " else "") + default
      })
    }

    private def optLabel(arg: CliOption[_]): String = {
      (arg.abbrev match {
        case Some(abbrev) => "-" + abbrev + (if (arg.longName.isDefined) ", " else "")
        case None => ""
      }) + (arg.longName match {
        case Some(name) => "--" + name + showValuesFor(arg.tpe)
        case None => ""
      })
    }

    private def showDefault(t: Any): String = {
      if (t.getClass.isEnum) {
        t.toString.toLowerCase
      } else {
        t.toString
      }
    }

    private def showValuesFor(clazz: Class[_]): String = {
      if (clazz == classOf[Int] || clazz == classOf[Long]) {
        "=NUM"
      } else if (clazz == classOf[String]) {
        "=STRING"
      } else if (clazz.isEnum) {
        "=" + clazz.getEnumConstants.map(_.toString.toLowerCase).mkString("|")
      } else ""
    }

    private def showArg(arg: CliArgument[_]): String = {
      val multipleSuffix = arg match {
        case _: SingleCliArgument[_] => ""
        case _: MultipleCliArgument[_] => " ..."
      }
      s"<${arg.name}>$multipleSuffix"
    }
  }
}