package org.eknet.publet.sbt

import org.eknet.publet.server._
import org.eclipse.jetty.server.Server
import scala.Some
import org.eclipse.jetty.server.handler.{ContextHandler, ContextHandlerCollection}
import scala.Some

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 23.09.12 14:16
 */
object ServerRunner {

  private var server: PubletServer = null

  def start(portNum: Int, workingDir: String) {
    if (server == null) {
      val config = new DefaultConfig {
        override def port = Some(portNum)
        override def workingDirectory = workingDir
      }
      server = new PubletServer(config, new WebAppConfigurer {
        def configure(server: Server, config: ServerConfig) {
          CodeWebappConfigurer.configure(server, config)
          server.getHandler match {
            case x:ContextHandlerCollection => {
              for (handler <- x.getChildHandlers) {
                handler match {
                  case ch:ContextHandler => ch.setClassLoader(getClass.getClassLoader)
                  case _ =>
                }
              }
            }
            case _ =>
          }
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
