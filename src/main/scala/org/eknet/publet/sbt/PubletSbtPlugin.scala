/*
 * Copyright 2012 Eike Kettner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

  def createClasspath(pf: File, cd: File, classpath: Keys.Classpath) = {
    val excludes = Set("scala-library.jar", "scala-compiler.jar", pf.getName)
    val filtered = classpath.filterNot(p => excludes.contains(p.data.asFile.getName))
    if (filtered.isEmpty) cd.toURI.toString
      else cd.toURI.toString+";"+ filtered.map(_.data.toURI.toString).mkString(";") 
  }

  val publetSettings = Seq(
    publetPort := 8088,
    publetDir <<= Keys.target(dir => {
      dir / "publetwork"
    }),
    publetClean <<= publetDir map (dir => {
      IO.delete(dir)
    }),
    publetStart <<= (publetDir, publetPort, classDir, Keys.fullClasspath in Runtime, Keys.`package` in Compile) map ((pdir, port, cd, classpath, pf) => {
      val cp = createClasspath(pf, cd, classpath)
      startPublet(port, pdir.getAbsolutePath, cp)
    }),
    publetStop := {
      stopPublet()
    },
    publetRestart <<= (publetDir, publetPort, classDir, Keys.fullClasspath in Runtime, Keys.`package` in Compile) map ((pdir, port, cd, classpath, pf) => {
      stopPublet()
      val cp = createClasspath(pf, cd, classpath)
      startPublet(port, pdir.getAbsolutePath, cp)
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
