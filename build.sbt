import sbt.Keys.libraryDependencies

name := "akka-gRPC-persistence-example"

version := "0.1"

scalaVersion := "2.12.10"

lazy val akkaVersion = "2.5.26"
lazy val scalatestVersion = "3.0.8"
lazy val leveldbVersion = "1.8"
lazy val betterFilesVersion = "3.8.0"

lazy val server = (project in file("server"))
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-persistence-typed" % akkaVersion,
      "org.fusesource.leveldbjni" % "leveldbjni-all" % leveldbVersion,
      "ch.qos.logback" % "logback-classic" % "1.2.3"
    ) ++ Seq(
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion,
      "org.scalatest" %% "scalatest" % scalatestVersion,
      "com.github.pathikrit" %% "better-files" % betterFilesVersion
    ).map(_ % "test")
  )


lazy val client = (project in file("client"))
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-persistence-typed" % akkaVersion,
      "org.fusesource.leveldbjni" % "leveldbjni-all" % leveldbVersion,
      "ch.qos.logback" % "logback-classic" % "1.2.3"
    ) ++ Seq(
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion,
      "org.scalatest" %% "scalatest" % scalatestVersion,
      "com.github.pathikrit" %% "better-files" % betterFilesVersion
    ).map(_ % "test")
  )
