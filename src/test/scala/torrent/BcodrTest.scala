package torrent

import org.scalatest._
import org.scalatest.Assertions.assertResult
import scala.io.Source
import torrent.Bcodr._
import scala.collection.mutable.ListBuffer

class BcodrTest extends FlatSpec {
    "bdecode" should "create an Int if input begins with an i" in {
        assertResult(123)(bdecode("i123e".toStream).head)
        // negative numbers
        assertResult(-45)(bdecode("i-45e".toStream).head)
        // zero
        assertResult(0)(bdecode("i0e".toStream).head)
    }
    
    "bdecode" should "create a String if input begins with a number" in {
        assertResult("whoa")(bdecode("4:whoa".toStream).head)
        // multidigit wordlength
        assertResult("the rain in Spain")(bdecode("17:the rain in Spain".toStream).head)
    }
    
    "bdecode" should "return a List if input begins with an l" in {
        assertResult(List("whoa"))(bdecode("l4:whoae".toStream).head)
        // polymorphic list
        assertResult(List("whoa", 123))(bdecode("l4:whoai123ee".toStream).head)
        // nested lists, maps
    }
    
    "bdecode" should "return a Map if input begins with a d" in {
        val chars1 = "d3:fooi123ee"
        val map1 = Map("foo" -> 123)
        assertResult(map1)(bdecode(chars1.toStream).head)
        assertResult(chars1)(bencode(map1))
        assertResult(chars1.length)(size(map1))
        // multi-item
        val chars2 = "d3:cow3:moo4:spam4:eggse"
        val map2 = Map("cow" -> "moo", "spam" -> "eggs")
        assertResult(map2)(bdecode(chars2.toStream).head)
        assertResult(chars2)(bencode(map2))
        assertResult(chars2.length)(size(map2))
        // polymorphic
        val chars3 = "d3:bar3:cow3:fooi123ee"
        val map3 = Map("foo" -> 123, "bar" -> "cow")
        assertResult(map3)(bdecode(chars3.toStream).head)
        assertResult(chars3)(bencode(map3))
        assertResult(chars3.length)(size(map3))
        // nested dicts, lists
        val chars4 = "d4:listl4:whoai123ee3:mapd3:bar3:cow3:fooi123eee"
        val map4 = Map("map" -> Map("foo" -> 123, "bar" -> "cow"),
        			   "list" -> List("whoa", 123))
        assertResult(map4)(bdecode(chars4.toStream).head)
        assertResult(chars4)(bencode(map4))
        assertResult(chars4.length)(size(map4))
//                map = "d3:map:d3:fooi123e3:bar3:cowe4:list:l4:whoai123eee"
    }
    "bdecode" should "properly return a Map with deeper nestings" in {
        val expect:Map[String,Any] = Map("map" -> Map("foo" -> 123, "bar" -> "cow"),
                "list" -> List("whoa", 123, Map("anoo" -> 345, "r567" -> "etage", "six" -> Map("froom" -> "plumb", "flop" -> 3736))))
        val encodedAll = "d4:listl4:whoai123ed4:anooi345e4:r5675:etage3:sixd4:flopi3736e5:froom5:plumbeee3:mapd3:bar3:cow3:fooi123eee"
        val list:List[Any] = expect("list").asInstanceOf[List[Any]]
        val map:Map[String,Any] = list(2).asInstanceOf[Map[String,Any]]
        val encodedMap = "d4:anooi345e4:r5675:etage3:sixd4:flopi3736e5:froom5:plumbee"
        assertResult(encodedMap)(bencode(map))
        assertResult(map)(bdecode(encodedMap.toStream).head)
        // first verify your string is correct
        assertResult(encodedAll)(bencode(expect))
        assertResult(expect)(bdecode(encodedAll.toStream).head)
    }
    
    "size" should "return the length required to encode an Int" in {
        assertResult(5)(size(123)) // i123e
        assertResult(6)(size(-123)) // i-123e
    }
    
    "size" should "return the length required to encode a String" in {
        assertResult(5)(size("foo")) // 3:foo
        assertResult(20)(size("the rain in spain")) // 17:the rain in spain
    }
    
    "size" should "return the length required to encode a list" in {
        assertResult("l3:fooe".length)(size(List("foo"))) // l3:fooe
        assertResult("l3:fooi123ee".length)(size(List("foo", 123))) // l3:fooi123ee
    }
    
    "size" should "return the length required to encode a dictionary" in {
        assertResult("d3:fooi123ee".length)(size(Map("foo" -> 123)))
        assertResult("d3:cow3:moo4:spam4:eggse".length)(size(Map("cow" -> "moo", "spam" -> "eggs")))
        assertResult("d3:fooi123e3:bari456ee".length)(size(Map("foo" -> 123, "bar" -> 456)))
    }
    
    
    "size" should "return the length required to encode a 10-digit Int" in {
        assertResult(12)(size(1234567890)) // i1234567890e
    }
    
    
    "size" should "return the length required to encode a 10-length String" in {
        assertResult(13)(size("1234567890")) // 10:1234567890
    }
    
    "size" should "correctly encode nested dictionaries" in {
        val file1 = Map("length" -> 291, "path" -> List("Distributed by Mininova.txt"))
        val enc1 = "d6:lengthi291e4:pathl27:Distributed by Mininova.txtee"
        assertResult(enc1.size)(size(file1))
        assertResult(file1)(bdecode(enc1.toStream).head)
        assertResult(enc1)(bencode(file1))
        val file2 = Map("length" -> 12613568, "path" -> List("SKYLAB VOL 2 Instrumentals, doodoo droid.mp3"))
        val enc2 = "d6:lengthi12613568e4:pathl44:SKYLAB VOL 2 Instrumentals, doodoo droid.mp3ee"
        assertResult(enc2.size)(size(file2))
        assertResult(file2)(bdecode(enc2.toStream).head)
        assertResult(enc2)(bencode(file2))
        
        val file3 = Map("length" -> 9805824, "path" -> List("SKYLAB VOL 2 Instrumentals", "heaven.mp3"))
        val enc3 = "d6:lengthi9805824e4:pathl26:SKYLAB VOL 2 Instrumentals10:heaven.mp3ee"
        assertResult(enc3.size)(size(file3))
        assertResult(file3)(bdecode(enc3.toStream).head)
        assertResult(enc3)(bencode(file3))
        
        val file4 = Map("length" -> 10249088, "path" -> List("SKYLINE LYRICS MIXTAPE (SKYLAB) BY RAZPRO","I KNOW.mp3"))
        val enc4 = "d6:lengthi10249088e4:pathl41:SKYLINE LYRICS MIXTAPE (SKYLAB) BY RAZPRO10:I KNOW.mp3ee"
        assertResult(enc4.size)(size(file4))
        assertResult(file4)(bdecode(enc4.toStream).head)
        assertResult(enc4)(bencode(file4))
        
        val f = "ld6:lengthi291e4:pathl27:Distributed by Mininova.txteed6:lengthi9886464e4:pathl26:SKYLAB VOL 2 Instrumentals26:adventures with razpro.mp3eed6:lengthi12613568e4:pathl26:SKYLAB VOL 2 Instrumentals16:doodoo droid.mp3eed6:lengthi10016768e4:pathl26:SKYLAB VOL 2 Instrumentals30:earth is where im going to.mp3ee"
        val allenc = List(f, 
               "d6:lengthi11835264e4:pathl26:SKYLAB VOL 2 Instrumentals12:going to.mp3ee",
//               "d6:lengthi9805824e4:pathl26:SKYLAB VOL 2 Instrumentals10:heaven.mp3ee",
               "d6:lengthi10017024e4:pathl26:SKYLAB VOL 2 Instrumentals31:hundred thousand lightyears.mp3ee",
               "d6:lengthi10491264e4:pathl26:SKYLAB VOL 2 Instrumentals14:never know.mp3ee",
               "d6:lengthi9807744e4:pathl26:SKYLAB VOL 2 Instrumentals15:rocket fuel.mp3ee",
               "d6:lengthi12483264e4:pathl26:SKYLAB VOL 2 Instrumentals19:skyline venture.mp3ee",
               "d6:lengthi11477888e4:pathl26:SKYLAB VOL 2 Instrumentals11:warpage.mp3ee",
               "d6:lengthi7366464e4:pathl41:SKYLINE LYRICS MIXTAPE (SKYLAB) BY RAZPRO36:COMPUTER SYNTHS, FUCK YOU PAY ME.mp3ee",
               "d6:lengthi4457664e4:pathl41:SKYLINE LYRICS MIXTAPE (SKYLAB) BY RAZPRO20:FAVORITE FEELING.mp3ee",
               "d6:lengthi6884288e4:pathl41:SKYLINE LYRICS MIXTAPE (SKYLAB) BY RAZPRO13:FROM HERE.mp3ee",
//               "d6:lengthi10249088e4:pathl41:SKYLINE LYRICS MIXTAPE (SKYLAB) BY RAZPRO10:I KNOW.mp3ee",
               "d6:lengthi5506688e4:pathl41:SKYLINE LYRICS MIXTAPE (SKYLAB) BY RAZPRO13:MOVING ON.mp3ee",
               "d6:lengthi13381568e4:pathl41:SKYLINE LYRICS MIXTAPE (SKYLAB) BY RAZPRO18:REALEST FORMAT.mp3ee",
               "d6:lengthi8679744e4:pathl41:SKYLINE LYRICS MIXTAPE (SKYLAB) BY RAZPRO11:SKYLINE.mp3ee",
               "d6:lengthi10224128e4:pathl41:SKYLINE LYRICS MIXTAPE (SKYLAB) BY RAZPRO14:SPACE GIRL.mp3ee",
               "d6:lengthi10395008e4:pathl41:SKYLINE LYRICS MIXTAPE (SKYLAB) BY RAZPRO15:STRAPPED IN.mp3eee").mkString
            //4:name93:SKYLAB VOL 2 [Instrumentals] 10 dope beats produced by RAZPRO MERRY CHRISTMAS  HAPPY NEW YEAR"
        val alldec = List(Map("length" -> 291, "path" -> List("Distributed by Mininova.txt")), 
                Map("length" -> 9886464, "path" -> List("SKYLAB VOL 2 Instrumentals", "adventures with razpro.mp3")), 
                Map("length" -> 12613568, "path" -> List("SKYLAB VOL 2 Instrumentals", "doodoo droid.mp3")), 
                Map("length" -> 10016768, "path" -> List("SKYLAB VOL 2 Instrumentals", "earth is where im going to.mp3")), 
                Map("length" -> 11835264, "path" -> List("SKYLAB VOL 2 Instrumentals", "going to.mp3")), 
//                Map("length" -> 9805824, "path" -> List("SKYLAB VOL 2 Instrumentals", "heaven.mp3")), 
                Map("length" -> 10017024, "path" -> List("SKYLAB VOL 2 Instrumentals","hundred thousand lightyears.mp3")), 
                Map("length" -> 10491264, "path" -> List("SKYLAB VOL 2 Instrumentals","never know.mp3")), 
                Map("length" -> 9807744, "path" -> List("SKYLAB VOL 2 Instrumentals","rocket fuel.mp3")), 
                Map("length" -> 12483264, "path" -> List("SKYLAB VOL 2 Instrumentals","skyline venture.mp3")), 
                Map("length" -> 11477888, "path" -> List("SKYLAB VOL 2 Instrumentals","warpage.mp3")), 
                Map("length" -> 7366464, "path" -> List("SKYLINE LYRICS MIXTAPE (SKYLAB) BY RAZPRO","COMPUTER SYNTHS, FUCK YOU PAY ME.mp3")), 
                Map("length" -> 4457664, "path" -> List("SKYLINE LYRICS MIXTAPE (SKYLAB) BY RAZPRO","FAVORITE FEELING.mp3")), 
                Map("length" -> 6884288, "path" -> List("SKYLINE LYRICS MIXTAPE (SKYLAB) BY RAZPRO","FROM HERE.mp3")), 
//                Map("length" -> 10249088, "path" -> List("SKYLINE LYRICS MIXTAPE (SKYLAB) BY RAZPRO","I KNOW.mp3")), 
                Map("length" -> 5506688, "path" -> List("SKYLINE LYRICS MIXTAPE (SKYLAB) BY RAZPRO","MOVING ON.mp3")), 
                Map("length" -> 13381568, "path" -> List("SKYLINE LYRICS MIXTAPE (SKYLAB) BY RAZPRO","REALEST FORMAT.mp3")), 
                Map("length" -> 8679744, "path" -> List("SKYLINE LYRICS MIXTAPE (SKYLAB) BY RAZPRO","SKYLINE.mp3")), 
                Map("length" -> 10224128, "path" -> List("SKYLINE LYRICS MIXTAPE (SKYLAB) BY RAZPRO","SPACE GIRL.mp3")), 
                Map("length" -> 10395008, "path" -> List("SKYLINE LYRICS MIXTAPE (SKYLAB) BY RAZPRO","STRAPPED IN.mp3")))
        assertResult(alldec)(bdecode(allenc.toStream).head)
        assertResult(allenc.size)(size(alldec))
        assertResult(allenc)(bencode(alldec))
//          Expected List(Map(length -> 291, path -> List(Distributed by Mininova.txt)), Map(length -> 9886464, path -> List(SKYLAB VOL 2 Instrumentals, adventures with razpro.mp3)), Map(length -> 12613568, path -> List(SKYLAB VOL 2 Instrumentals, doodoo droid.mp3)), Map(length -> 10016768, path -> List(SKYLAB VOL 2 Instrumentals, earth is where im going to.mp3)), Map(length -> 11835264, path -> List(SKYLAB VOL 2 Instrumentals, going to.mp3)), Map(length -> 10017024, path -> List(SKYLAB VOL 2 Instrumentals, hundred thousand lightyears.mp3)), Map(length -> 10491264, path -> List(SKYLAB VOL 2 Instrumentals, never know.mp3)), Map(length -> 9807744, path -> List(SKYLAB VOL 2 Instrumentals, rocket fuel.mp3)), Map(length -> 12483264, path -> List(SKYLAB VOL 2 Instrumentals, skyline venture.mp3)), Map(length -> 11477888, path -> List(SKYLAB VOL 2 Instrumentals, warpage.mp3)), Map(length -> 7366464, path -> List(SKYLINE LYRICS MIXTAPE (SKYLAB) BY RAZPRO, COMPUTER SYNTHS, FUCK YOU PAY ME.mp3)), Map(length -> 4457664, path -> List(SKYLINE LYRICS MIXTAPE (SKYLAB) BY RAZPRO, FAVORITE FEELING.mp3)), Map(length -> 6884288, path -> List(SKYLINE LYRICS MIXTAPE (SKYLAB) BY RAZPRO, FROM HERE.mp3)), Map(length -> 13381568, path -> List(SKYLINE LYRICS MIXTAPE (SKYLAB) BY RAZPRO, REALEST FORMAT.mp3)), Map(length -> 8679744, path -> List(SKYLINE LYRICS MIXTAPE (SKYLAB) BY RAZPRO, SKYLINE.mp3)), Map(length -> 10224128, path -> List(SKYLINE LYRICS MIXTAPE (SKYLAB) BY RAZPRO, SPACE GIRL.mp3)), Map(length -> 10395008, path -> List(SKYLINE LYRICS MIXTAPE (SKYLAB) BY RAZPRO, STRAPPED IN.mp3)))
//           but got List(Map(length -> 291, path -> List(Distributed by Mininova.txt)), Map(length -> 9886464, path -> List(SKYLAB VOL 2 Instrumentals, adventures with razpro.mp3)), Map(length -> 12613568, path -> List(SKYLAB VOL 2 Instrumentals, doodoo droid.mp3)), Map(length -> 10016768, path -> List(SKYLAB VOL 2 Instrumentals, earth is where im going to.mp3)), Map(length -> 11835264, path -> List(SKYLAB VOL 2 Instrumentals, going to.mp3)), Map(length -> 10017024, path -> List(SKYLAB VOL 2 Instrumentals, hundred thousand lightyears.mp3)), Map(length -> 10491264, path -> List(SKYLAB VOL 2 Instrumentals, never know.mp3)), Map(length -> 9807744, path -> List(SKYLAB VOL 2 Instrumentals, rocket fuel.mp3)), Map(length -> 12483264, path -> List(SKYLAB VOL 2 Instrumentals, skyline venture.mp3)), Map(length -> 11477888, path -> List(SKYLAB VOL 2 Instrumentals, warpage.mp3)), Map(length -> 7366464, path -> List(SKYLINE LYRICS MIXTAPE (SKYLAB) BY RAZPRO, COMPUTER SYNTHS, FUCK YOU PAY ME.mp3)), Map(length -> 4457664, path -> List(SKYLINE LYRICS MIXTAPE (SKYLAB) BY RAZPRO, FAVORITE FEELING.mp3)), Map(length -> 6884288, path -> List(SKYLINE LYRICS MIXTAPE (SKYLAB) BY RAZPRO, FROM HERE.mp3)), Map(length -> 5506688, path -> List(SKYLINE LYRICS MIXTAPE (SKYLAB) BY RAZPRO, MOVING ON.mp3)), Map(length -> 13381568, path -> List(SKYLINE LYRICS MIXTAPE (SKYLAB) BY RAZPRO, REALEST FORMAT.mp3)), Map(length -> 8679744, path -> List(SKYLINE LYRICS MIXTAPE (SKYLAB) BY RAZPRO, SKYLINE.mp3)), Map(length -> 10224128, path -> List(SKYLINE LYRICS MIXTAPE (SKYLAB) BY RAZPRO, SPACE GIRL.mp3)), Map(length -> 10395008, path -> List(SKYLINE LYRICS MIXTAPE (SKYLAB) BY RAZPRO, STRAPPED IN.mp3)))
//        // TODO why is this not a list?
////        val map = Map("length" -> 4457664, 
////                      "path" -> List("SKYLINE LYRICS MIXTAPE (SKYLAB) BY RAZPRO, FAVORITE FEELING.mp3")
////                      ) -> Map("length" -> 6884288, "path" -> List("SKYLINE LYRICS MIXTAPE (SKYLAB) BY RAZPRO, FROM HERE.mp3")), Map("length" -> 11477888, "path" -> List("SKYLAB VOL 2 Instrumentals, warpage.mp3")) -> Map("length" -> 7366464, "path" -> List("SKYLINE LYRICS MIXTAPE (SKYLAB) BY RAZPRO, COMPUTER SYNTHS, FUCK YOU PAY ME.mp3")), "files" -> List(Map("length" -> 291, "path" -> List("Distributed by Mininova.txt")), Map("length" -> 9886464, "path" -> List("SKYLAB VOL 2 Instrumentals, adventures with razpro.mp3")), Map("length" -> 12613568, "path" -> List("SKYLAB VOL 2 Instrumentals, doodoo droid.mp3")), Map("length" -> 10016768, "path" -> List("SKYLAB VOL 2 Instrumentals, earth is where im going to.mp3")), Map("length" -> 11835264, "path" -> List("SKYLAB VOL 2 Instrumentals, going to.mp3")), Map("length" -> 9805824, "path" -> List("SKYLAB VOL 2 Instrumentals, heaven.mp3"))), Map("length" -> 9807744, "path" -> List("SKYLAB VOL 2 Instrumentals, rocket fuel.mp3")) -> Map("length" -> 12483264, "path" -> List("SKYLAB VOL 2 Instrumentals, skyline venture.mp3")), Map("length" -> 10017024, "path" -> List("SKYLAB VOL 2 Instrumentals, hundred thousand lightyears.mp3")) -> Map("length" -> 10491264, "path" -> List("SKYLAB VOL 2 Instrumentals, never know.mp3")))
////        bytes=1176, map=Map(length -> 4457664, path -> List(SKYLINE LYRICS MIXTAPE (SKYLAB) BY RAZPRO, FAVORITE FEELING.mp3)) -> Map(length -> 6884288, path -> List(SKYLINE LYRICS MIXTAPE (SKYLAB) BY RAZPRO, FROM HERE.mp3)),Map(length -> 11477888, path -> List(SKYLAB VOL 2 Instrumentals, warpage.mp3)) -> Map(length -> 7366464, path -> List(SKYLINE LYRICS MIXTAPE (SKYLAB) BY RAZPRO, COMPUTER SYNTHS, FUCK YOU PAY ME.mp3)),files -> List(Map(length -> 291, path -> List(Distributed by Mininova.txt)), Map(length -> 9886464, path -> List(SKYLAB VOL 2 Instrumentals, adventures with razpro.mp3)), Map(length -> 12613568, path -> List(SKYLAB VOL 2 Instrumentals, doodoo droid.mp3)), Map(length -> 10016768, path -> List(SKYLAB VOL 2 Instrumentals, earth is where im going to.mp3)), Map(length -> 11835264, path -> List(SKYLAB VOL 2 Instrumentals, going to.mp3)), Map(length -> 9805824, path -> List(SKYLAB VOL 2 Instrumentals, heaven.mp3))),Map(length -> 9807744, path -> List(SKYLAB VOL 2 Instrumentals, rocket fuel.mp3)) -> Map(length -> 12483264, path -> List(SKYLAB VOL 2 Instrumentals, skyline venture.mp3)),Map(length -> 10017024, path -> List(SKYLAB VOL 2 Instrumentals, hundred thousand lightyears.mp3)) -> Map(length -> 10491264, path -> List(SKYLAB VOL 2 Instrumentals, never know.mp3))
    }
    
    "bencode" should "return an encoded representaton of an Int" in {
        assertResult("i123e")(bencode(123))
        assertResult("i0e")(bencode(0))
        assertResult("i-45e")(bencode(-45))
    }
    
    "bencode" should "return an encoded representation of a String" in {
        assertResult("17:the rain in spain")(bencode("the rain in spain"))
        assertResult("3:foo")(bencode("foo"))
    }
    
    "bencode" should "return an encoded representation of a List" in {
        assertResult("l3:fooe")(bencode(List("foo"))) // l3:fooe
        assertResult("l3:fooi123ee")(bencode(List("foo", 123))) // l3:fooi123ee
    }
    
    "bencode" should "return an encoded representation of a dictionary" in {
        assertResult("d3:fooi123ee")(bencode(Map("foo" -> 123)))
        assertResult("d3:cow3:moo4:spam4:eggse")(bencode(Map("cow" -> "moo", "spam" -> "eggs")))
    }
    
    "bencode" should "sort dictionary keys" in {
    	assertResult("d3:bari456e3:fooi123ee")(bencode(Map("foo" -> 123, "bar" -> 456)))
    }
    "bencode" should "return an encoded representation of a map containing a list" in {
        assertResult("d3:cow3:moo4:listl3:fooi123ee4:spam4:eggse")(bencode(Map("cow" -> "moo", "list" -> List("foo", 123), "spam" -> "eggs")))
        assertResult("d3:cow3:moo4:listl3:fooi123eee")(bencode(Map("cow" -> "moo", "list" -> List("foo", 123))))
        val enc1 = "d6:lengthi291e4:pathl27:Distributed by Mininova.txtee"
        assertResult(enc1)(bencode(Map("length" -> 291, "path" -> List("Distributed by Mininova.txt"))))
//        val file1 = Map("length" -> 291, "path" -> List("Distributed by Mininova.txt"))
    }
    
}