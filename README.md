# Publet SBT Plugin

A simple plugin for [sbt](http://www.scala-sbt.org/) to start/stop a publet server from within sbt. This aims
to be of use when developing extensions for publet.

## Install

Add the plugin in `project/build.sbt` together with the publet dependency:

    resolvers += "eknet.org" at "https://eknet.org/maven2"

    addSbtPlugin("org.eknet.publet" % "publet-sbt-plugin" % "1.0.0-SNAPSHOT")

Please change the version as appropriate. In the `build.sbt` file, add the settings
to your project:

    seq(publetSettings: _*)

    libraryDependencies += "org.eknet.publet" %% "publet-app" % "1.0.0-SNAPSHOT" % "publet"


## Usage

To use the plugin, have a look at the provided keys. They're all prefixed with `publet-`.

The command `publet-start` starts a publet instance with the current project. It can be
viewed at <http://localhost:8088>. Similarly `publet-stop` stops the running publet instance. Both
can be executed at once using `publet-restart`.

When changing templates in the current project a `package` command should update the classpath
accordingly and a refresh in the browser shows the changes. The server does not need to be restarted.
