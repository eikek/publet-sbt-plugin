import sbt._
import Keys._

object SbtPubletBuild extends Build {
  lazy val module = Project("sbt-publet", file(".")) settings(buildProperties: _*)

  def buildProperties: Seq[Project.Setting[_]] = Defaults.defaultSettings ++ Seq(
    sbtPlugin := true,
    organization := "org.eknet.publet",
    name := "sbt-publet",
    version := "1.0.0-SNAPSHOT",
    publishMavenStyle := true,
    publishArtifact in Test := false,
    pomIncludeRepository := (_ => false),
    pomExtra := extraPom,
    scalacOptions ++= Seq("-deprecation", "-unchecked"),
    libraryDependencies ++= Seq(
      "org.eknet.publet" %% "publet-server" % "1.0.0-SNAPSHOT" % "optional"
    )
  )

  def extraPom = (
    <url>https://eknet.org/projects/publet/</url>
    <licenses>
      <license>
        <name>Apache 2</name>
        <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
      </license>
    </licenses>
  )
}
