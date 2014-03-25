package torrent

import scala.util.Random
import java.lang.management.ManagementFactory

object ClientLauncher {

    def main(args: Array[String]): Unit = {}
//    "-AF" + {4 digits version (major/minor)} + "-" + {5 digit PID} + {7 random char}
    def peer = {
        val versionL = "0.0.1".split("\\.")
        String.format("-AF%s0%s0-%5s0%s",
                versionL(0), versionL(1), 
                sortaPID,
                Random.alphanumeric.take(7).mkString).toUpperCase
    }
    /**
     * returns a 5-character PID
     */
    def sortaPID:String = {
        val foo = ManagementFactory.getRuntimeMXBean().getName()
        val substr = foo.split("@")(0)
        if (substr.length < 5) String.format("%5s", substr).replaceAll(" ", "0")
        else substr.toList.take(5).mkString
    }
    
}