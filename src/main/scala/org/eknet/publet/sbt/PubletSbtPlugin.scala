package org.eknet.publet.sbt

import sbt._
import org.eknet.publet.server.ServerConfig

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 22.09.12 20:03
 */
object PubletSbtPlugin extends Plugin {

  val publetPort = SettingKey[Int]("publet-port", "The port for the publet server.")
  val publetDir = SettingKey[File]("publet-dir", "The publet directory.")
  val publetStart = TaskKey[Unit]("publet-start", "Starts a publet instance.")
  val publetStop = TaskKey[Unit]("publet-stop", "Starts a publet instance.")
  val publetRestart = TaskKey[Unit]("publet-restart", "Stops and starts the publet instance.")
  val publetClean = TaskKey[Unit]("publet-clean", "Removes the publet instance directory.")

  private val classDir = Keys.classDirectory in (Compile)

  val publetSettings = Seq(
    publetPort := 8088,
    publetDir <<= Keys.target(dir => {
      dir / "publetwork"
    }),
    publetClean <<= publetDir map (dir => {
      IO.delete(dir)
    }),
    publetStart <<= (publetDir, publetPort, classDir) map ((pdir: File, port: Int, cd: File) => {
      startPublet(port, pdir.getAbsolutePath, cd.toURI.toString)
    }),
    publetStop := {
      stopPublet()
    },
    publetRestart <<= (publetDir, publetPort, classDir) map ((pdir: File, port: Int, cd: File) => {
      stopPublet()
      startPublet(port, pdir.getAbsolutePath, cd.toURI.toString)
    })
  )

  private def startPublet(port: Int, workDir: String, projectClasspath: String) {
    System.setProperty(ServerConfig.propertyWorkingDirectory, workDir)
    ServerRunner.start(port, workDir, projectClasspath)
  }

  private def stopPublet() {
    ServerRunner.stop()
  }
}
