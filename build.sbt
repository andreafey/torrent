name := "torrent"

scalaVersion := "2.10.2"

libraryDependencies ++= Seq(
    "io.spray" % "spray-can" % "1.3.1",
    "com.typesafe.akka" %% "akka-actor" % "2.3.0",
    "io.spray" % "spray-routing" % "1.3.1",
    "org.jvnet.mimepull" % "mimepull" % "1.7",
    "org.scalatest" % "scalatest_2.10" % "2.0" % "test",
    "com.typesafe" % "scalalogging-slf4j_2.10" % "1.1.0",
    "org.slf4j" % "slf4j-log4j12" % "1.7.6",
    "junit" % "junit" % "4.8.1" % "test"
)

resolvers += "spray repo" at "http://repo.spray.io"

//unmanagedClasspath in Runtime <<= (unmanagedClasspath in Runtime, baseDirectory) map { (cp, bd) => cp :+ Attributed.blank(bd / "src" / "main" / "resources") }

unmanagedSourceDirectories in Compile <++= baseDirectory { base =>
  Seq(
    base / "src/main/resources",
    base / "src/test/resources"
  )
}

