package org.eknet.publet.sbt

import org.eknet.publet.server._
import org.eknet.publet.app._
import scala.Some
import org.eclipse.jetty.webapp.{WebAppContext, WebAppClassLoader}
import tools.nsc.util.ScalaClassLoader.URLClassLoader
import java.net.URL
import org.eclipse.jetty.servlet.ServletContextHandler

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 23.09.12 14:16
 */
object ServerRunner {

  private var server: PubletServer = null

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
          val loader = new URLClassLoader(Seq(new URL(projectClasspath)), getClass.getClassLoader)
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
