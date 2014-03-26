name := "torrent"

scalaVersion := "2.10.3"

libraryDependencies ++= Seq(
    "org.scalatest" % "scalatest_2.10" % "2.0" % "test",
    "io.spray" % "spray-can" % "1.3.1"
)

resolvers += "spray repo" at "http://repo.spray.io"

unmanagedClasspath in Runtime <<= (unmanagedClasspath in Runtime, baseDirectory) map { (cp, bd) => cp :+ Attributed.blank(bd / "src" / "main" / "resources") }

