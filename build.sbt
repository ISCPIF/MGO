organization := "fr.iscpif"

name := "mgo"

scalaVersion := "2.11.1"

crossScalaVersions := Seq("2.10.4", "2.11.1")

resolvers ++= Seq(
  "Scala Tools Snapshots" at "http://scala-tools.org/repo-snapshots/"
)

libraryDependencies += "com.github.scala-incubator.io" %% "scala-io-core" % "0.4.3"

libraryDependencies += "org.apache.commons" % "commons-math3" % "3.2"

libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.0.6"

resolvers ++= Seq(
  "Sonatype Releases" at "http://oss.sonatype.org/content/repositories/releases"
)

libraryDependencies ++= Seq(
  "org.scalacheck" %% "scalacheck" % "1.11.3" % "test"
)

publishMavenStyle := true

publishArtifact in Test := false

//publishTo <<= version { (v: String) =>
//  val maven = "http://maven.iscpif.fr/"
//  if (v.trim.endsWith("SNAPSHOT")) Some("snapshots" at maven + "snapshots") 
//  else Some("releases"  at maven + "releases")
//}

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

pomIncludeRepository := { _ => false }

licenses := Seq("Affero GPLv3" -> url("http://www.gnu.org/licenses/"))

homepage := Some(url("https://github.com/romainreuillon/mgo"))

 scmInfo := Some(ScmInfo(url("https://github.com/romainreuillon/mgo.git"), "scm:git:git@github.com:romainreuillon/mgo.git"))

pomExtra := (
  <developers>
    <developer>
      <id>romainreuillon</id>
      <name>Romain Reuillon</name>
    </developer>
    <developer>
      <id>guillaumecherel</id>
      <name>Guillaume Cherel</name>
    </developer>
  </developers>
)


credentials += Credentials(Path.userHome / ".sbt" / "iscpif.credentials")

scalariformSettings

releaseSettings
