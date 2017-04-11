logLevel := Level.Warn

addSbtPlugin("org.scala-sbt" % "sbt-core-next" % "0.1.1")
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.1.1")
addSbtPlugin("io.spray" % "sbt-revolver" % "0.7.2")
addSbtPlugin("com.thesamet" % "sbt-protoc" % "0.99.1")
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.3")
addSbtPlugin("com.trueaccord.scalapb" % "sbt-scalapb" % "0.4.20")

// https://mvnrepository.com/artifact/com.trueaccord.scalapb/compilerplugin_2.10
libraryDependencies += "com.trueaccord.scalapb" % "compilerplugin_2.10" % "0.5.43"


resolvers += Resolver.url("bintray-sbt-plugins", url("https://dl.bintray.com/sbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns)