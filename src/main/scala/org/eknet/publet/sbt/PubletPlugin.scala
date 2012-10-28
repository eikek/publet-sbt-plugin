package org.eknet.publet.sbt

import sbt._

object PubletPlugin extends Plugin {

  object Keys {

    val port = SettingKey[Int]("port", "The port for the publet server.")
    val workdir = SettingKey[File]("workdir", "The publet directory.")
    val start = TaskKey[Unit]("start", "Starts a publet instance.")
    val stop = TaskKey[Unit]("stop", "Stops a publet instance.")
    val restart = TaskKey[Unit]("restart", "Stops and starts the publet instance.")
    val cleanWorkdir = TaskKey[Unit]("clean-workdir", "Removes the publet instance directory.")

  }
  val container = new PubletContainer("publet")

  def publetSettings = container.settings
}