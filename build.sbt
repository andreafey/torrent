name := "torrent"

scalaVersion := "2.10.3"

libraryDependencies += "org.scalatest" % "scalatest_2.10" % "2.0" % "test"

libraryDependencies += "org.scala-lang" % "scala-swing" % "2.10.2"

libraryDependencies += "org.scalaj" %% "scalaj-http" % "0.3.14"

unmanagedClasspath in Runtime <<= (unmanagedClasspath in Runtime, baseDirectory) map { (cp, bd) => cp :+ Attributed.blank(bd / "src" / "main" / "resources") }

