package org.backuity.cli

trait Usage {
  def show(commands: Commands) : String
}

object Usage {

  object Default extends Usage {

    import org.backuity.ansi.AnsiFormatter.FormattedHelper

    def showDefault(t : Any): String = {
      if( t.getClass.isEnum ) {
        t.toString.toLowerCase
      } else {
        t.toString
      }
    }

    def showValuesFor(clazz : Class[_]) : String = {
      if( clazz == classOf[Int] || clazz == classOf[Long] ) {
        "=NUM"
      } else if( clazz == classOf[String] ) {
        "=STRING"
      } else if( clazz.isEnum ) {
        "=" + clazz.getEnumConstants.map(_.toString.toLowerCase).mkString("|")
      } else ""
    }

    val indentString = "   "

    override def show(commands: Commands): String = {

      val usage = new java.lang.StringBuilder()
      var indentLevel = 0
      var beginning = true

      def incIndent(): Unit = { indentLevel += 1 }
      def decIndent(): Unit = { indentLevel -= 1 }
      def indent( f : => Unit ): Unit = { incIndent(); f; decIndent() }

      def addLine(str: String = ""): Unit = { add(str + "\n") }
      def add(str: String): Unit = {
        if( str.trim.isEmpty ) {
          // do not pad for nothing
          usage.append(str)
        } else {
          val pad = if (beginning) indentString * indentLevel else ""
          usage.append(pad + str)
        }
        if( str.endsWith("\n") ) {
          beginning = true
        } else {
          beginning = false
        }
      }

      def optLabel(arg: CliOption[_]) : String = {
        (arg.abbrev match {
          case Some(abbrev) => "-" + abbrev + (if( arg.longName.isDefined ) ", " else "")
          case None         => ""
        }) + (arg.longName match {
          case Some(name) => "--" + name + showValuesFor(arg.tpe)
          case None       => ""
        })
      }

      def optText(labelMaxSize: Int, arg: CliOption[_]) : String = {
        val label = optLabel(arg)
        val description = arg.description.getOrElse("")
        val default = arg.default match {
          case Some(d) if d != false => ansi"%italic{(default: ${showDefault(d)})}"
          case _ => ""
        }
        val padding = " " * (labelMaxSize - label.length)

        ansi"%yellow{$label}" + (if( description.isEmpty && default.isEmpty ) {
          ""
        } else {
          padding + " : " + description + (if( !description.isEmpty && !default.isEmpty ) " " else "") + default
        })
      }

      /** @param opts non empty */
      def addOptions(opts: Set[CliOption[_]]): Unit = {
        val labelMaxSize = opts.map(optLabel).map(_.length).max

        for( opt <- opts.toList.sortBy(_.name) ) {
          addLine(optText(labelMaxSize, opt))
        }
      }

      addLine(ansi"%underline{Usage}")
      addLine()
      addLine(ansi" %bold{cli} %yellow{[options]} %bold{command} %yellow{[command options]}")
      addLine()
      addLine(ansi"%underline{Options:}")
      addLine()
      indent {
        addOptions(commands.options)
      }

      addLine()
      addLine(ansi"%underline{Commands:}")
      indent {
        for (command <- commands.commandsSortedByLabel) {
          addLine()
          add(ansi"%bold{${command.label}}")
          val commandSpecificOpts = command.options -- commands.options
          val description = if( command.description != "" ) " : " + command.description else ""

          if( command.arguments.nonEmpty ) {
            add(" " + command.arguments.map(arg => s"<${arg.name}>").mkString(" "))
          }
          if( commandSpecificOpts.nonEmpty ) {
            add(" [command options]")
          }
          addLine(description)

          // body
          // TODO args
          if( commandSpecificOpts.nonEmpty ) {
            indent {
              addOptions(commandSpecificOpts)
            }
          }
        }
      }

      usage.toString
    }
  }
}