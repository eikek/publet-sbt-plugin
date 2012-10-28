name := "plugin-test-simple"

seq(publetSettings: _*)

libraryDependencies += "org.eknet.publet" %% "publet-app" % "1.0.0-SNAPSHOT" % "publet"

libraryDependencies += "com.google.guava" % "guava" % "13.0.1" % "provided"
