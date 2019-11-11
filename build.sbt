import sbt.Keys.libraryDependencies

name := "akka-gRPC-persistence-example"

lazy val akkaVersion = "2.6.0"
lazy val scalatestVersion = "3.0.8"
lazy val leveldbVersion = "1.8"
lazy val betterFilesVersion = "3.8.0"

lazy val commonSettings = Seq(
  version := "0.0.1",
  fork := true,
  scalaVersion := "2.12.10",
  test in assembly := {}
)

lazy val protobufApi = (project in file("protobuf-api"))
  .settings(
    assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)
  )

lazy val server = (project in file("server"))
  .enablePlugins(AkkaGrpcPlugin)
  .enablePlugins(JavaAgent) // ALPN agent
  .settings(
    PB.protoSources in Compile += (resourceDirectory in(protobufApi, Compile)).value,
    akkaGrpcGeneratedLanguages := Seq(AkkaGrpc.Scala),
    akkaGrpcGeneratedSources := Seq(AkkaGrpc.Server)
  )
  .settings(
    commonSettings,
    javaAgents += "org.mortbay.jetty.alpn" % "jetty-alpn-agent" % "2.0.9" % "runtime;test",
    mainClass in assembly := Some("example.server.Main"),
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-persistence-typed" % akkaVersion,
      "com.typesafe.akka" %% "akka-stream-typed" % akkaVersion,
      "com.typesafe.akka" %% "akka-discovery" % akkaVersion, // Forcing version imported from gRPC
      "org.fusesource.leveldbjni" % "leveldbjni-all" % leveldbVersion,
      "ch.qos.logback" % "logback-classic" % "1.2.3"
    ) ++ Seq(
      "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion,
      "org.scalatest" %% "scalatest" % scalatestVersion,
    ).map(_ % "test")
  )
  .dependsOn(protobufApi)

lazy val client = (project in file("client"))
  .enablePlugins(AkkaGrpcPlugin)
  .disablePlugins(sbtassembly.AssemblyPlugin)
  .settings(
    PB.protoSources in Compile += (resourceDirectory in(protobufApi, Compile)).value,
    akkaGrpcGeneratedSources := Seq(AkkaGrpc.Client)
  )
  .settings(
    commonSettings,
    mainClass in assembly := Some("example.Main"),
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % "1.2.3"
    )
  )
  .dependsOn(protobufApi)
