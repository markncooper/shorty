import sbt.Keys.scalaVersion

name := """shorty"""
organization := "com.brigade"

version := System.getProperty("BUILD_VERSION", "1.0-SNAPSHOT")

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"

publishMavenStyle := true

crossPaths := false

resolvers := ("Sonatype Nexus Repository Manager" at "http://nexus.brigade.zone:8081/nexus/content/groups/public/") +: resolvers.value

libraryDependencies += "org.json4s" %% "json4s-native" % "3.5.0"
libraryDependencies += "org.scalaj" %% "scalaj-http" % "2.3.0"
libraryDependencies += "org.scala-lang" % "scala-library" % "2.11.8"
libraryDependencies += "org.scala-lang" % "scala-compiler" % "2.11.8"

libraryDependencies += "com.adrianhurt" %% "play-bootstrap" % "1.1-P25-B3"

mainClass in assembly := Some("play.core.server.ProdServerStart")

fullClasspath in assembly += Attributed.blank(PlayKeys.playPackageAssets.value)

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case r if r.startsWith("reference.conf") => MergeStrategy.concat
  case "application.conf" => MergeStrategy.concat
  case x => MergeStrategy.first
}

test in assembly := {}

artifact in (Compile, assembly) := {
  val art = (artifact in (Compile, assembly)).value
  art.copy(`classifier` = Some("assembly"))
}

addArtifact(artifact in (Compile, assembly), assembly)

//
// Publish fat runnable jar to Nexus. Started via:
//    sbt clean assembly publish
//
credentials += Credentials("Sonatype Nexus Repository Manager", "nexus.brigade.zone", "jenkins", "oIy8BYbDVCAvS3kJ7xHERmBmCBN4dGiAgFNeLeUGReunW")
publishTo := Some("Brigade Nexus Repo" at "http://nexus.brigade.zone/nexus/content/repositories/releases/")

// Uncomment to test local deploys
//publishTo := Some(Resolver.file("file",  new File( "/Users/mcooper/.m2/test" )) )
