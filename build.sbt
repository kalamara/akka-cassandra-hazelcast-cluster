
enablePlugins(JavaAppPackaging)

name := """akka-cassandra-hazelcast"""

version := "1.0"

scalaVersion := "2.11.8"

scalacOptions := Seq("-unchecked", "-feature", "-deprecation", "-encoding", "utf8")

autoCompilerPlugins := true

libraryDependencies ++= {
  val akkaVersion = "2.4.11"
  val scalaTestVersion = "2.2.6"
  val hazelcastVersion = "3.7"
  Seq(
    "com.typesafe" % "config" % "1.3.0",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.4.0",
    "com.typesafe.akka" %% "akka-http-core" % akkaVersion,
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-core" % akkaVersion,
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-cluster" % akkaVersion,
    "com.typesafe.akka" %% "akka-cluster-metrics" % akkaVersion,
    "com.typesafe.akka" %% "akka-remote" % akkaVersion,
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-experimental" % akkaVersion,
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
    /*"com.google.inject" % "guice" % "4.1.0",
    "com.google.code.findbugs" % "jsr305" % "3.0.1",*/
    "com.datastax.cassandra" % "cassandra-driver-core" % "3.0.2",
    "com.datastax.cassandra" % "cassandra-driver-mapping" % "3.0.2",
    "com.datastax.cassandra" % "cassandra-driver-extras" % "3.0.2",
    "com.trueaccord.scalapb" %% "scalapb-runtime" % "0.5.42" % "protobuf",
    "com.trueaccord.scalapb" %% "scalapb-json4s" % "0.1.2",
    "com.github.scopt" %% "scopt" % "3.5.0",
    "com.hazelcast" % "hazelcast" % hazelcastVersion,
    "com.hazelcast" % "hazelcast-client" % hazelcastVersion,
    "com.hazelcast" %% "hazelcast-scala" % "3.7.0" withSources(),
    compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
    "com.outworkers" %% "phantom-connectors" % "2.0.11",
    "com.outworkers" %% "phantom-dsl" % "2.0.11",
    "org.scalaz" %% "scalaz-core" % "7.2.8",
    "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
    "ch.qos.logback" % "logback-classic" % "1.1.3"
  )
}

assemblyMergeStrategy in assembly := {
  case "META-INF/io.netty.versions.properties" => MergeStrategy.first
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

//fork in run := true
Revolver.settings

PB.targets in Compile := Seq(
  scalapb.gen() -> (sourceManaged in Compile).value
)