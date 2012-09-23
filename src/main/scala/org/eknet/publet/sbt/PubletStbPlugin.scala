package org.eknet.publet.sbt

import sbt._
import org.eknet.publet.server._

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 22.09.12 20:03
 */
object PubletStbPlugin extends Plugin {

  val publetPort = SettingKey[Int]("publet-port", "The port for the publet server.")
  val publetDir = SettingKey[File]("publet-dir", "The publet directory.")
  val publetStart = TaskKey[Unit]("publet-start", "Starts a publet instance.")
  val publetStop = TaskKey[Unit]("publet-stop", "Starts a publet instance.")
  val publetRestart = TaskKey[Unit]("publet-restart", "Stops and starts the publet instance.")

  val publetSettings = Seq(
    publetPort := 8088,
    publetDir := IO.createTemporaryDirectory,
    publetStart <<= (publetDir, publetPort) map ((pdir: File, port: Int) => {
      startPublet(port, pdir.getAbsolutePath)
    }),
    publetStop := {
      stopPublet()
    },
    publetRestart <<= (publetDir, publetPort) map ((pdir: File, port: Int) => {
      stopPublet()
      startPublet(port, pdir.getAbsolutePath)
    })
  )

  private def startPublet(port: Int, workDir: String) {
    System.setProperty(ServerConfig.propertyWorkingDirectory, workDir)
    ServerRunner.start(port, workDir)
  }

  private def stopPublet() {
    ServerRunner.stop()
  }
}
