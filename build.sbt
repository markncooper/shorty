import sbt.Keys.scalaVersion

name := """shorty"""
organization := "com.brigade"
rpmVendor := "brigade"
enablePlugins(RpmPlugin)

version := System.getProperty("BUILD_VERSION", "1.0-SNAPSHOT")

lazy val root = (project in file(".")).enablePlugins(PlayScala, RpmPlugin)

maintainer in Linux := "Mark Cooper <markncooper@gmail.com>"

packageSummary in Linux := "Shorty - a very simple url shortener"
packageDescription := "Shorty - a very simple url shortener. Implemented as a Play service."
rpmRelease := "1"
rpmVendor := "brigade.com"
rpmUrl := Some("http://github.com/markncooper/shorty")
rpmLicense := Some("Apache v2")

scalaVersion := "2.11.8"

publishMavenStyle := true

crossPaths := false

resolvers := ("Sonatype Nexus Repository Manager" at "http://nexus.brigade.zone:8081/nexus/content/groups/public/") +: resolvers.value

libraryDependencies += "org.json4s" %% "json4s-native" % "3.5.0"
libraryDependencies += "org.scalaj" %% "scalaj-http" % "2.3.0"
libraryDependencies += "org.scala-lang" % "scala-library" % "2.11.8"
libraryDependencies += "org.scala-lang" % "scala-compiler" % "2.11.8"

libraryDependencies += "com.mixpanel" % "mixpanel-java" % "1.4.4"

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

