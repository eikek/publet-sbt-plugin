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

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 28.10.12 00:49
 */
class ServiceRunner(loader: ClassLoader, wd: String) {

  lazy val service = {
    val sconfig = loader.loadClass("org.eknet.publet.server.ServerConfig")
    val config = loader.loadClass("org.eknet.publet.server.DefaultConfig").newInstance().asInstanceOf[AnyRef]
    val setter = config.getClass.getMethod("setWorkingDirectory", classOf[String])
    setter.invoke(config, wd)
    val clazz = loader.loadClass("org.eknet.publet.app.PubletService")
    clazz.getConstructor(sconfig, classOf[Option[ClassLoader]]).newInstance(config, Some(loader)).asInstanceOf[AnyRef]
  }

  lazy val starter = service.getClass.getMethod("start")
  lazy val stopper = service.getClass.getMethod("stop")

  def start() {
    starter.invoke(service)
  }

  def restart() {
    stop()
    start()
  }

  def stop() {
    stopper.invoke(service)
  }
}
