lazy val commonSettings = Seq(
  organization := "org.backuity.clit",
  scalaVersion := "2.11.6",

  scalacOptions ++= Seq("-deprecation", "-unchecked"),

  // use intransitive to avoid getting scala-reflect transitively
  ivyConfigurations += config("compileonly").intransitive.hide,

  unmanagedClasspath in Compile ++= update.value.select(configurationFilter("compileonly")),
  unmanagedClasspath in Test ++= update.value.select(configurationFilter("compileonly"))
)

lazy val releaseSettings = commonSettings ++ Seq(
  homepage := Some(url("https://github.com/backuity/clit")),
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
      <url>git@github.com:backuity/clit.git</url>
      <connection>scm:git:git@github.com:backuity/clit.git</connection>
    </scm>
      <developers>
        <developer>
          <id>backuitist</id>
          <name>Bruno Bieth</name>
          <url>https://github.com/backuitist</url>
        </developer>
      </developers>
)


val ansi = "org.backuity" %% "ansi-interpolator" % "1.1" % "compileonly"
val matchete = Seq(
  "org.backuity" %% "matchete-junit" % "1.26" % "test",
  "com.novocode" % "junit-interface" % "0.11" % "test")
val mockito = "org.mockito" % "mockito-core" % "1.10.8" % "test"

lazy val root = project.in(file(".")).
  settings(releaseSettings : _*).
  settings(
    publishArtifact := false).
  aggregate(
    macros,
    tests,
    demo)

lazy val core = project.in(file("core")).
  settings(releaseSettings: _*).
  settings(
    name := "clit-core",
    libraryDependencies ++= matchete :+ ansi :+ mockito)

lazy val macros = project.in(file("macros")).
  settings(releaseSettings: _*).
  settings(
    name := "clit-macros",
    libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value).
  dependsOn(core)

lazy val tests = project.in(file("tests")).
  settings(commonSettings: _*).
  settings(
    libraryDependencies ++= matchete :+ ansi,
    publishArtifact := false).
  dependsOn(
    core,
    macros) // % "compileonly") does not work!

lazy val demo = project.in(file("demo")).
  settings(commonSettings: _*).
  settings(
    publishArtifact := false).
  dependsOn(
    core,
    macros) // % "compileonly") does not work!