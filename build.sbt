import sbt.Keys.libraryDependencies
import NativePackagerHelper._

name := "akka-gRPC-persistence-example"

lazy val akkaVersion = "2.6.4"
lazy val scalatestVersion = "3.1.1"
lazy val leveldbVersion = "1.8"
lazy val betterFilesVersion = "3.8.0"
lazy val logbackVersion = "1.2.3"
lazy val typeSafeConfig = "1.4.0"

lazy val commonSettings = Seq(
  version := "0.0.2",
  fork := true,
  scalaVersion := "2.13.1"
)

lazy val root = Project("root", file("."))
  .aggregate(protobufApi, server, client)

lazy val protobufApi = (project in file("protobuf-api"))
  .settings(
    commonSettings
  )

lazy val server = (project in file("server"))
  .enablePlugins(AkkaGrpcPlugin, JavaAgent, JavaAppPackaging)
  .settings(
    PB.protoSources in Compile += (resourceDirectory in(protobufApi, Compile)).value,
    akkaGrpcGeneratedLanguages := Seq(AkkaGrpc.Scala),
    akkaGrpcGeneratedSources := Seq(AkkaGrpc.Server)
  )
  .settings(
    commonSettings,
    javaAgents += "org.mortbay.jetty.alpn" % "jetty-alpn-agent" % "2.0.9" % "runtime;test",
    mainClass in (Compile, packageBin) := Some(
      "example.server.Main"
    ),
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-persistence-typed" % akkaVersion,
      "com.typesafe.akka" %% "akka-stream-typed" % akkaVersion,
      "com.typesafe.akka" %% "akka-discovery" % akkaVersion, // Forcing version imported from gRPC
      "com.typesafe" % "config" % typeSafeConfig,
      "org.fusesource.leveldbjni" % "leveldbjni-all" % leveldbVersion,
      "ch.qos.logback" % "logback-classic" % "1.2.3"
    ) ++ Seq(
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion,
      "org.scalatest" %% "scalatest" % scalatestVersion,
    ).map(_ % "test")
  )
  .dependsOn(protobufApi)

lazy val client = (project in file("client"))
  .enablePlugins(AkkaGrpcPlugin, JavaAppPackaging)
  .settings(
    PB.protoSources in Compile += (resourceDirectory in(protobufApi, Compile)).value,
    akkaGrpcGeneratedSources := Seq(AkkaGrpc.Client)
  )
  .settings(
    commonSettings,
    mainClass in (Compile, packageBin) := Some(
      "example.client.Main"
    ),
    libraryDependencies ++= Seq(
      "com.typesafe" % "config" % typeSafeConfig,
      "ch.qos.logback" % "logback-classic" % logbackVersion
    )
  )
  .dependsOn(protobufApi)
