
name := "cli-test"

organization := "org.backuity"

scalaVersion := "2.11.2"

scalacOptions ++= Seq("-deprecation", "-unchecked")

libraryDependencies ++= Seq(
  "commons-io"    % "commons-io" % "2.4",
  "com.github.scopt" %% "scopt" % "3.2.0",
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.2",
  //
  // Tests
  "org.backuity" %% "matchete" % "1.10" % "test",
  "com.novocode" % "junit-interface" % "0.11" % "test",
  "org.mockito" % "mockito-core" % "1.10.8" % "test"
)