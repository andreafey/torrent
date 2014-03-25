package torrent

import org.scalatest._
import org.scalatest.Assertions.assertResult
import scala.io.Source

class MetafileTest extends FlatSpec {
    val testheader = "src/test/resources/nbr6header-only.torrent"
//    val testfile1 = "src/test/resources/Python programming nbr 6.torrent"
    val testfile2 = "src/test/resources/bestbasstest.torrent"
    val testfile3 = "src/test/resources/sky.torrent"
    
// d
    // 4:info
    //  d5:filesld6:lengthi291e4:pathl27:Distributed by Mininova.txtee
    //           d6:lengthi15628267e4:pathl15:poniishow11.mp3eee
    // 4:name98:best bass music EDM dubstep podcast on the Internet by Poniiboi facebook.comPONIImusic EDM dubstep
    // 12:piece lengthi1048576e6:pieces
// d8:announce36:http://tracker.mininova.org/announce7:comment41:Auto-generated torrent by Mininova.org CD13:creation datei1387881974e
//   4:info
//      d5:filesld6:lengthi291e4:pathl27:Distributed by Mininova.txtee
//                  d6:lengthi9886464e4:pathl26:SKYLAB VOL 2 Instrumentals26:adventures with razpro.mp3ee
//                  d6:lengthi12613568e4:pathl26:SKYLAB VOL 2 Instrumentals16:doodoo droid.mp3ee
//                  d6:lengthi10016768e4:pathl26:SKYLAB VOL 2 Instrumentals30:earth is where im going to.mp3ee
//                  d6:lengthi11835264e4:pathl26:SKYLAB VOL 2 Instrumentals12:going to.mp3ee
//                  d6:lengthi9805824e4:pathl26:SKYLAB VOL 2 Instrumentals10:heaven.mp3ee
//                  d6:lengthi10017024e4:pathl26:SKYLAB VOL 2 Instrumentals31:hundred thousand lightyears.mp3ee
//                  d6:lengthi10491264e4:pathl26:SKYLAB VOL 2 Instrumentals14:never know.mp3ee
//        .......
//                  d6:lengthi10395008e4:pathl41:SKYLINE LYRICS MIXTAPE (SKYLAB) BY RAZPRO15:STRAPPED IN.mp3eee
//     4:name93:SKYLAB VOL 2 [Instrumentals] 10 dope beats produced by RAZPRO MERRY CHRISTMAS  HAPPY NEW YEAR
//        12:piece lengthi1048576e
//     6:pieces3540
    "Metafile(file)" should "create an object with known values" in {
        val mf2 = new Metafile(testfile2);
        val files = mf2.files
        // file itself plus minnova distribution info file
        assertResult(2)(files.length)
        assertResult(291)(files(0).length)
        assertResult(Some("Distributed by Mininova.txt"))(files(0).path)
        assertResult(15628267)(files(1).length)
        assertResult(Some("poniishow11.mp3"))(files(1).path)
        val expectedName = "best bass music EDM dubstep podcast on the Internet by Poniiboi facebook.comPONIImusic EDM dubstep"
        assertResult(expectedName)(mf2.info("name"))
        assertResult(1048576)(mf2.pieceLength)
        
        val mf3 = new Metafile(testfile3)
        assertResult("http://tracker.mininova.org/announce")(mf3.announce)
        assertResult("SKYLAB VOL 2 [Instrumentals] 10 dope beats produced by RAZPRO MERRY CHRISTMAS  HAPPY NEW YEAR")(mf3.info("name"))
        assertResult(1048576)(mf3.pieceLength)
        val files3 = mf3.files
        assertResult(20)(files.length)
        assertResult("SKYLAB VOL 2 Instrumentals12:going to.mp3")(files3(4).path)
        assertResult("Auto-generated torrent by Mininova.org CD")(mf3.comment)
        assertResult(1048576)(mf3.pieceLength)
        assertResult(1387881974)(mf3.creationDate)
    }
    "bencode" should "correctly encode Metafile" in {
        val mf = new Metafile(testfile2)
        val encoded = Bcodr.bencode(mf.metamap)
        assertResult(mf.encoded)(encoded)
    }
    
}
