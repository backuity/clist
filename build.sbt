lazy val commonSettings = Seq(
  organization := "org.backuity.clist",
  scalaVersion := "2.12.2",
  version := "3.3.0-SNAPSHOT",

  crossScalaVersions := Seq("2.11.11", "2.12.2"),
  scalacOptions ++= Seq("-deprecation", "-unchecked"),

  // use intransitive to avoid getting scala-reflect transitively
  ivyConfigurations += config("compileonly").intransitive.hide,

  unmanagedClasspath in Compile ++= update.value.select(configurationFilter("compileonly")),
  unmanagedClasspath in Test ++= update.value.select(configurationFilter("compileonly"))
)

lazy val releaseSettings = commonSettings ++ Seq(
  homepage := Some(url("https://github.com/backuity/clist")),
  licenses := Seq("Apache 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.html")),

  publishMavenStyle := true,

  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (version.value.trim.endsWith("SNAPSHOT"))
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  },

  // replace publish by publishSigned
  publish := PgpKeys.publishSigned.value,

  pomIncludeRepository := { _ => false },

  pomExtra :=
    <scm>
      <url>git@github.com:backuity/clist.git</url>
      <connection>scm:git:git@github.com:backuity/clist.git</connection>
    </scm>
      <developers>
        <developer>
          <id>backuitist</id>
          <name>Bruno Bieth</name>
          <url>https://github.com/backuitist</url>
        </developer>
      </developers>
)

lazy val localSettings = commonSettings ++ Seq(
  publish := {},
  publishLocal := {}
)

val ansi = "org.backuity" %% "ansi-interpolator" % "1.1.0" % "compileonly"
val matchete = Seq(
  "org.backuity" %% "matchete-junit" % "1.28.0" % "test",
  "com.novocode" % "junit-interface" % "0.11" % "test")
val mockito = "org.mockito" % "mockito-core" % "1.10.8" % "test"

lazy val root = project.in(file(".")).
  settings(releaseSettings : _*).
  settings(
    publishArtifact := false).
  aggregate(
    core,
    macros,
    tests,
    demo)

lazy val core = project.in(file("core")).
  settings(releaseSettings: _*).
  settings(
    name := "clist-core",
    libraryDependencies ++= matchete :+ ansi :+ mockito)

lazy val macros = project.in(file("macros")).
  settings(releaseSettings: _*).
  settings(
    name := "clist-macros",
    libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value).
  dependsOn(core)

lazy val tests = project.in(file("tests")).
  settings(localSettings: _*).
  settings(
    libraryDependencies ++= matchete :+ ansi).
  dependsOn(
    core,
    macros) // % "compileonly") does not work!

lazy val demo = project.in(file("demo")).
  settings(localSettings: _*).
  dependsOn(
    core,
    macros) // % "compileonly") does not work!