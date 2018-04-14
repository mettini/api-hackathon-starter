name := """api-hackathon-starter"""

version := "1.0.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.5"

libraryDependencies += jdbc
libraryDependencies += ehcache
libraryDependencies += evolutions

libraryDependencies += "com.h2database" % "h2" % "1.4.192"
libraryDependencies += "mysql" % "mysql-connector-java" % "5.1.41"
libraryDependencies += "com.typesafe.play" %% "anorm" % "2.5.3"
libraryDependencies += "com.github.nscala-time" %% "nscala-time" % "2.18.0"
libraryDependencies += "org.mindrot" % "jbcrypt" % "0.4"
libraryDependencies += "com.sendgrid" % "sendgrid-java" % "4.1.2"
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.0" % Test
libraryDependencies += "org.scalamock" %% "scalamock-scalatest-support" % "3.6.0" % "test"
libraryDependencies += "com.softwaremill.macwire" %% "macros" % "2.3.0" % "provided"

val akkaVersion = "2.5.11"
dependencyOverrides += "com.typesafe.akka" %% "akka-actor" % akkaVersion
dependencyOverrides += "com.typesafe.akka" %% "akka-stream" % akkaVersion
dependencyOverrides += "com.google.guava" % "guava" % "23.0"
dependencyOverrides += "rg.webjars" % "webjars-locator-core" % "0.33"
dependencyOverrides += "org.codehaus.plexus" % "plexus-utils" % "3.0.17"
dependencyOverrides += "org.slf4j" % "slf4j-api" % "1.7.25"
