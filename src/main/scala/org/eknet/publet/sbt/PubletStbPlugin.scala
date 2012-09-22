package org.eknet.publet.sbt

import sbt._
import org.eknet.publet.server._

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 22.09.12 20:03
 */
class PubletStbPlugin extends Plugin {

  val publetDir = TaskKey[File]("publet-dir", "The publet directory.")
  val publetStart = TaskKey[Unit]("publet-start", "Starts a publet instance.")
  val publetStop = TaskKey[Unit]("publet-stop", "Starts a publet instance.")

  lazy val allSettings = Seq(
    publetStart <<= (publetDir) map ((pdir: File) => {

      System.setProperty(ServerConfig.propertyWorkingDirectory, pdir.getAbsolutePath)
      Main.startup()

      ()
    })
  )
}
