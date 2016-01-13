# Command Line Interface Scala Toolkit [![Build Status](https://travis-ci.org/backuity/clist.png?branch=master)](https://travis-ci.org/backuity/clist)

The Backuity CLIST is a scala-only (2.11+) library for quickly building beautiful type-safe modular and reusable mutable CLIs.

- [You said beautiful](#you-said-beautiful)
- [Why mutable?](#why-mutable)
- [Let's start - Single Command CLI](#lets-start---single-command-cli)
- [Argument vs Arguments vs Options](#argument-vs-arguments-vs-options)
- [Parsing](#parsing)
- [Exit code vs Exception](#exit-code-vs-exception)
- [Version, Help and Usage](#version-help-and-usage)
- [Multiple Commands](#multiple-commands)
  - [Composition: grouping Options into Traits](#composition-grouping-options-into-traits)
  - [Adding behavior to Commands](#adding-behavior-to-commands)

## You said beautiful?

An image is worth a thousand words, here is a taste of what you'd get for free:

![usage-demo](usage-demo.png)

## Why mutable?

We think that CLIs do not require an immutable approach. Immutability often comes at the expense of simplicity.
If you are looking for an immutable CLI library you should take a look at projects like https://github.com/scopt/scopt.

## Let's start - Single Command CLI

First let's configure our SBT build
```scala
libraryDependencies ++= Seq(
   "org.backuity.clist" %% "clist-core"   % "2.0.0",
   "org.backuity.clist" %% "clist-macros" % "2.0.0" % "provided")
```

Then define a command:
```scala
  import org.backuity.clist._

  class Cat extends Command(description = "concatenate files and print on the standard output") {

    // `opt`, `arg` and `args` are scala macros that will extract the name of the member
    // to use it as the option/arguments name.
    // Here for instance the member `showAll` will be turned into the option `--show-all`
    var showAll        = opt[Boolean](abbrev = "A", description = "equivalent to -vET")

    // an abbreviated form can be added, so that this option can be triggered both by `--number-nonblank` or `-b`
    var numberNonblank = opt[Boolean](abbrev = "b", description = "number nonempty output lines, overrides -n")

    var files          = args[Seq[File]](description = "files to concat")
  }
```

And use it to parse `args`:
```scala
  def main(args: Array[String]) {
    Cli.parse(args).withCommand(new Cat) { case cat =>
          // the new Cat instance will be mutated to receive the command-line arguments
          println(cat.files)
      }
  }
```

## Argument vs Arguments vs Options

A `Command` can have 3 kinds of attributes:
  - `opt`: an option is always optional and must start with a dash `-`.
           It can have an abbreviated form. Declaration order does not matter.
  - `arg`: an arg might be optional. Argument declaration order matters.
  - `args`: the equivalent of a var-args. At most one must be specified and it must be declared last.


## Parsing

The parsing is done through the `Read` and `ReadMultiple` type-classes. User-specific instances can be provided by simply
adding them to the implicit scope.
`Read` (used by `opt` and `arg`) parses a String into a type `T`,
whereas `ReadMultiple` (used by `args`) takes a __list__ of string to produce a type `U`.

Note that on the command there is a distinction between
```
cat file1 file2 "file with space"
```
and
```
cat file1 file2 file with space
```

## Exit code vs Exception

By default, upon failure, the `Cli` will exit with code 1. This behavior can be customized:
  - `Cli.parse(args).throwExceptionOnError()` : throws an exception instead of exiting
  - `Cli.parse(args).exitCode(12)` : exits with a specific code

## Version, Help and Usage

You can provide a version number for your program through `version("1.0.0")`. This will add a `version` option,
whose name can be customized with `version("1.0.0", "custom-name")`.

By default a help command is added, which displays the command usage. This can be removed with `noHelp()`.
The usage is printed for each parsing error but this can be disabled with `noUsageOnError()`.

Finally the usage can be customized through `withUsage(newCustomUsage)`.

## Multiple Commands

To build a multi-command CLI simply provide the parser with more than one command:
```scala
object Run extends Command
object Show extends Command

val res = Cli.parse(args).withCommands(Run, Show)
// res will be an Option[Command]
```

It makes sense now to define a name for our program:
```scala
Cli.parse(args).withProgramName("my-cli").withCommands(Run, Show)
```

### Composition: grouping Options into Traits

It is entirely possible (and encouraged) to factorize options into traits and compose Commands with them:
```scala
trait Common {
   var verbose = opt[Boolean](abbrev = "v")
}

object Run extends Command with Common
object Show extends Command with Common

val res = Cli.parse(args).withCommands(Run, Show)
// res is also now infered as an `Option[Common]`
```

You can also seal your command hierarchy to allow exhaustive pattern matching checks:
```scala
sealed trait Common { // same as above
}

Cli.parse(args).withCommands(Run, Show) match {
   case Some(Run)  =>
   case Some(Show) =>
   case None =>
}
```

### Adding behavior to Commands

Depending on your taste, you might want to define the behavior of your commands within them:
```scala
sealed trait Common {
   var verbose = opt[Boolean](abbrev = "v")
   def run(): Unit
}

object Run extends Command with Common {
   def run(): Unit = {
      println("Running...")
   }
}

object Show extends Command with Common {
   def run(): Unit = {
      println("Showing...")
   }
}

Cli.parse(args).withCommands(Run, Show).foreach(_.run())
```
