import java.io.File
import java.nio.file.Paths

import org.backuity.clist._

object ProgramInfoDemoTest {
  sealed trait CommonOpt {
    self: Command =>
    var debug = opt[Boolean](description = "debug mode")
    var timeout = opt[Long](default = 500, description = "max network timeout in millisecond")
    var port = opt[Option[Int]](description = "specify listening port for contact")
    var printCode = opt[Boolean](description = "print connection address as hex string.")

    var address = arg[Option[String]](required = false,
      description = "address or connection code, e.g. 127.0.0.1:8088")
  }

  private class Push extends Command(
    description = "publish a file and wait for pulling from client")
    with CommonOpt {
    var file = arg[File](required = true, description = "file to send")
  }

  private class Pull extends Command(
    description = "pull a published file from push node")
    with CommonOpt {
    var destDir =
      arg[File](required = false, default = Paths.get(".").toFile,
        description = "dest dir to save pulled file")
  }

  def main(args: Array[String]): Unit = {
    Cli.parse(args)
      .version("test-version")
      .withProgramName("eft")
      .withDescription("a file transfer tool.")
      .withCommands(new Push, new Pull)
      .foreach {printCmd}
  }

  private def printCmd(cmd: Command): Unit = {
    println(s"Push cmd executed with options:\n${cmd.options}\n args:\n${cmd.arguments}")
  }
}
