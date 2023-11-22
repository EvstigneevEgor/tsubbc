ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.12"

// Core with minimal dependencies, enough to spawn your first bot.
libraryDependencies += "com.bot4s" %% "telegram-core" % "5.7.0"

// Extra goodies: Webhooks, support for games, bindings for actors.
libraryDependencies += "com.bot4s" %% "telegram-akka" % "5.7.0"
val sttpVersion = "3.9.0"

libraryDependencies += "com.github.pureconfig" %% "pureconfig" % "0.17.1"

libraryDependencies ++= Seq(
  "com.softwaremill.sttp.client3" %% "core" % sttpVersion,
  "com.softwaremill.sttp.client3" %% "circe" % sttpVersion,
  "com.softwaremill.sttp.client3" %% "okhttp-backend" % sttpVersion
)

libraryDependencies ++= {
  Seq(
    "com.typesafe.slick"              %% "slick"                          % "3.3.2",
    "com.typesafe.slick"              %% "slick-hikaricp"                 % "3.3.2",
    "org.slf4j"                       % "slf4j-api"                       % "1.7.5",
    "ch.qos.logback"                  % "logback-classic"                 % "1.0.9",
    "org.xerial"                      % "sqlite-jdbc"                     % "3.34.0",
    "org.scalatest"                   % "scalatest_2.11"                  % "2.2.1"               % "test"
  )
}



lazy val root = (project in file("."))
  .settings(
    name := "testbot"
  )
