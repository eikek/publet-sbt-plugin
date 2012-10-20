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

import org.eknet.publet.server._
import org.eknet.publet.app._
import scala.Some
import org.eclipse.jetty.webapp.{WebAppContext, WebAppClassLoader}
import tools.nsc.util.ScalaClassLoader.URLClassLoader
import java.net.URL
import org.eclipse.jetty.servlet.ServletContextHandler
import com.google.common.base.{CharMatcher, Splitter}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 23.09.12 14:16
 */
object ServerRunner {

  private var server: PubletServer = null

  /**
   * Creates and starts a publet server instance using the specified
   * port number and working directory.
   *
   * @param portNum
   * @param workingDir
   * @param projectClasspath Comma or semicolon separated path of filenames or URLs
   * pointing to directories or jar files. Directories should end
   * with '/'.
   */
  def start(portNum: Int, workingDir: String, projectClasspath: String) {
    if (server == null) {
      val config = new DefaultConfig {
        override def port = Some(portNum)
        override def workingDirectory = workingDir
      }
      server = new PubletServer(config, new CodeWebappConfigurer(None) {
        override protected def postProcessWebAppContext(webapp: WebAppContext) {
          val loader = new WebAppClassLoader(getClass.getClassLoader, webapp)
          loader.addClassPath(projectClasspath)
          webapp.setClassLoader(loader)
          webapp.setExtraClasspath(projectClasspath)
        }

        override protected def postProcessServletContext(sch: ServletContextHandler) {
          import collection.JavaConversions._
          val urls = Splitter.on(CharMatcher.anyOf(",;"))
            .omitEmptyStrings()
            .split(projectClasspath)
            .map(new URL(_))
            .toSeq

          val loader = new URLClassLoader(urls, this.getClass.getClassLoader)
          sch.setClassLoader(loader)
          sch.setInitParameter("custom-classpath", projectClasspath)
        }
      })
    }
    server.startInBackground()
  }

  def stop() {
    if (server != null) {
      server.stop()
      server = null
    }
  }
}
