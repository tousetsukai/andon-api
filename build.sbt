name := "andon-api"

organization := "com.satsukita-andon"

version := "0.0.0"

scalaVersion := "2.11.7"

scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Xlint",
  "-Xfatal-warnings",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-unused",
  "-Ywarn-unused-import",
  "-Ywarn-value-discard"
)

libraryDependencies ++= {
  val akkaHttp = "1.0"
  val json4s = "3.2.11"
  val scalikejdbc = "2.2.7"
  Seq(
    "com.typesafe.akka" %% "akka-stream-experimental" % akkaHttp,
    "com.typesafe.akka" %% "akka-http-core-experimental" % akkaHttp,
    "com.typesafe.akka" %% "akka-http-experimental" % akkaHttp,
    "org.json4s" %% "json4s-jackson" % json4s,
    "org.json4s" %% "json4s-ext" % json4s,
    "org.scalikejdbc" %% "scalikejdbc" % scalikejdbc,
    "org.scalikejdbc" %% "scalikejdbc-config" % scalikejdbc,
    "org.scalikejdbc" %% "scalikejdbc-test" % scalikejdbc % "test",
    "org.scalatest" %% "scalatest" % "2.2.4" % "test",
    "com.h2database" % "h2" % "1.4.187",
    "ch.qos.logback" % "logback-classic" % "1.1.3",
    "com.github.nscala-time" %% "nscala-time" % "2.0.0",
    "org.eclipse.jgit" % "org.eclipse.jgit" % "4.0.1.201506240215-r",
    "com.pauldijou" %% "jwt-json4s-jackson" % "0.4.1"
  )
}

// db migration using flyway

seq(flywaySettings:  _*)

import com.typesafe.config.ConfigFactory

val conf = ConfigFactory.parseFile(new File("src/main/resources/application.conf"))

flywayUrl := conf.getString("db.default.url")

flywayUser := conf.getString("db.default.user")
