import sbt._
import Keys._

object BuildSettings {
  val buildSettings = Seq(
    organization := "org.backuity.cli",
    version := "0.1-SNAPSHOT",
    scalaVersion := "2.11.7",
    // Sonatype OSS deployment
    publishTo <<= version { (v: String) =>
      val nexus = "https://oss.sonatype.org/"
      if (v.trim.endsWith("SNAPSHOT"))
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases" at nexus + "service/local/staging/deploy/maven2")
    },
    publishMavenStyle := true,
    publishArtifact in Test := false,
    pomIncludeRepository := { _ => false},

    licenses := ("Apache2", new java.net.URL("http://www.apache.org/licenses/LICENSE-2.0.txt")) :: Nil
  )
}

object ClitBuild extends Build {

  import BuildSettings._

  lazy val root = Project("root",
    file("."),
    settings = buildSettings ++ Seq(publishArtifact := false)
  ) aggregate(macros, tests)

  lazy val macros = Project("macros",
    file("macros"),
    settings = buildSettings ++ Seq(
      name := "clit",
      libraryDependencies <+= scalaVersion("org.scala-lang" % "scala-reflect" % _),
      libraryDependencies ++= Seq(
          "org.backuity" %% "ansi-interpolator" % "1.1" % "provided",

          // Tests
          "org.backuity" %% "matchete" % "1.11" % "test",
          "com.novocode" % "junit-interface" % "0.11" % "test",
          "org.mockito" % "mockito-core" % "1.10.8" % "test")
    )
  )

  lazy val tests = Project("tests",
    file("tests"),
    settings = buildSettings ++ Seq(
      libraryDependencies ++= Seq(
        "org.backuity" %% "ansi-interpolator" % "1.1" % "provided",
        // Tests
        "org.backuity" %% "matchete" % "1.11" % "test",
        "com.novocode" % "junit-interface" % "0.11" % "test"),

      publishArtifact := false)
  ) dependsOn macros
}