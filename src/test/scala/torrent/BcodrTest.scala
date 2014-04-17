package torrent

import org.scalatest._
import org.scalatest.Assertions.assertResult
import torrent.Bcodr._

class BcodrTest extends FlatSpec {
    "bencode/bdecode" should "correctly encode/decode an Int" in {
        var encoded = "i123e"
        var decoded = 123
        assertResult(decoded)(bdecode(encoded))
        assertResult(encoded)(bencode(decoded))
        // negative number
        encoded = "i-45e"
        decoded = -45
        assertResult(decoded)(bdecode(encoded))
        assertResult(encoded)(bencode(decoded))
        // zero
        encoded = "i0e"
        decoded = 0
        assertResult(decoded)(bdecode(encoded))
        assertResult(encoded)(bencode(decoded))
        // exactly 10-digit number
    	encoded = "i1234567890e"
		decoded = 1234567890
        assertResult(decoded)(bdecode(encoded))
        assertResult(encoded)(bencode(decoded))
    }
    
    "bencode/bdecode" should "correctly encode/decode a String" in {
    	// multidigit word length
        var encoded = "17:the rain in spain"
        var decoded = "the rain in spain"
        assertResult(decoded)(bdecode(encoded))
        assertResult(encoded)(bencode(decoded))
        // single digit word length
        encoded = "3:foo"
        decoded = "foo"
        assertResult(decoded)(bdecode(encoded))
        assertResult(encoded)(bencode(decoded))
        encoded = "4:whoa"
        decoded = "whoa"
        assertResult(decoded)(bdecode(encoded))
        assertResult(encoded)(bencode(decoded))
        // exactly 10 digit string length
        encoded = "10:1234567890"
        decoded = "1234567890"
        assertResult(decoded)(bdecode(encoded))
        assertResult(encoded)(bencode(decoded))
    }
    
    "bencode/bdecode" should "correctly encode/decode a List" in {
        var encoded = "l3:fooe"
        var decoded:List[Any] = List("foo")
        assertResult(decoded)(bdecode(encoded))
        assertResult(encoded)(bencode(decoded))
        // polymorphic list
        encoded = "l3:fooi123ee"
        decoded = List("foo", 123)
        assertResult(decoded)(bdecode(encoded))
        assertResult(encoded)(bencode(decoded))
    }
    
    "bencode/bdecode" should "correctly encode/decode a dictionary" in {
        var encoded = "d3:fooi123ee"
        var decoded:Map[String,Any] = Map("foo" -> 123)
        assertResult(decoded)(bdecode(encoded))
        assertResult(encoded)(bencode(decoded))
        encoded = "d3:cow3:moo4:spam4:eggse"
        decoded = Map("cow" -> "moo", "spam" -> "eggs")
        assertResult(decoded)(bdecode(encoded))
        assertResult(encoded)(bencode(decoded))
        // polymorphic
        encoded = "d3:bar3:cow3:fooi123ee"
        decoded = Map("foo" -> 123, "bar" -> "cow")
        assertResult(decoded)(bdecode(encoded))
        assertResult(encoded)(bencode(decoded))
        // map containing list
        encoded = "d3:cow3:moo4:listl3:fooi123ee4:spam4:eggse"
        decoded = Map("cow" -> "moo", "list" -> List("foo", 123), "spam" -> "eggs")
        assertResult(decoded)(bdecode(encoded))
        assertResult(encoded)(bencode(decoded))
        // reorder same map and expect same ordered result
        decoded = Map("list" -> List("foo", 123), "spam" -> "eggs", "cow" -> "moo")
        assertResult(decoded)(bdecode(encoded))
        assertResult(encoded)(bencode(decoded))
        // another reordering example
        encoded = "d3:bari456e3:fooi123ee"
        decoded = Map("foo" -> 123, "bar" -> 456)
        assertResult(decoded)(bdecode(encoded))
        assertResult(encoded)(bencode(decoded))
    }
    
    "bencode/bdecode" should "correctly encode/decode nested dictionaries" in {
        // nested dicts, lists
        var encoded = "d4:listl4:whoai123ee3:mapd3:bar3:cow3:fooi123eee"
        var decoded:Map[String,Any] = Map("map" -> Map("foo" -> 123, "bar" -> "cow"),
        			   "list" -> List("whoa", 123))
        assertResult(decoded)(bdecode(encoded))
        assertResult(encoded)(bencode(decoded))
        // deeper nestings
        encoded = "d4:listl4:whoai123ed4:anooi345e4:r5675:etage3:sixd4:flopi3736e5:froom5:plumbeee3:mapd3:bar3:cow3:fooi123eee"
        decoded = Map("map" -> Map("foo" -> 123, "bar" -> "cow"),
                "list" -> List("whoa", 123, Map("anoo" -> 345, "r567" -> "etage", "six" -> Map("froom" -> "plumb", "flop" -> 3736))))
        assertResult(decoded)(bdecode(encoded))
        assertResult(encoded)(bencode(decoded))
        // check map items
        val list:List[Any] = decoded("list").asInstanceOf[List[Any]]
        val map:Map[String,Any] = list(2).asInstanceOf[Map[String,Any]]
        val encodedMap = "d4:anooi345e4:r5675:etage3:sixd4:flopi3736e5:froom5:plumbee"
        assertResult(encodedMap)(bencode(map))
        assertResult(map)(bdecode(encodedMap))
        
        encoded = "d6:lengthi291e4:pathl27:Distributed by Mininova.txtee"
        decoded = Map("length" -> 291, "path" -> List("Distributed by Mininova.txt"))
        assertResult(decoded)(bdecode(encoded))
        assertResult(encoded)(bencode(decoded))

        decoded = Map("length" -> 12613568, "path" -> List("SKYLAB VOL 2 Instrumentals, doodoo droid.mp3"))
        encoded = "d6:lengthi12613568e4:pathl44:SKYLAB VOL 2 Instrumentals, doodoo droid.mp3ee"
        assertResult(decoded)(bdecode(encoded))
        assertResult(encoded)(bencode(decoded))
        
        decoded = Map("length" -> 9805824, "path" -> List("SKYLAB VOL 2 Instrumentals", "heaven.mp3"))
        encoded = "d6:lengthi9805824e4:pathl26:SKYLAB VOL 2 Instrumentals10:heaven.mp3ee"
        assertResult(decoded)(bdecode(encoded))
        assertResult(encoded)(bencode(decoded))
        
        decoded = Map("length" -> 10249088, "path" -> List("SKYLINE LYRICS MIXTAPE (SKYLAB) BY RAZPRO","I KNOW.mp3"))
        encoded = "d6:lengthi10249088e4:pathl41:SKYLINE LYRICS MIXTAPE (SKYLAB) BY RAZPRO10:I KNOW.mp3ee"
        assertResult(decoded)(bdecode(encoded))
        assertResult(encoded)(bencode(decoded))
        
        val f = "ld6:lengthi291e4:pathl27:Distributed by Mininova.txteed6:lengthi9886464e4:pathl26:SKYLAB VOL 2 Instrumentals26:adventures with razpro.mp3eed6:lengthi12613568e4:pathl26:SKYLAB VOL 2 Instrumentals16:doodoo droid.mp3eed6:lengthi10016768e4:pathl26:SKYLAB VOL 2 Instrumentals30:earth is where im going to.mp3ee"
        val allenc = List(f, 
               "d6:lengthi11835264e4:pathl26:SKYLAB VOL 2 Instrumentals12:going to.mp3ee",
               "d6:lengthi9805824e4:pathl26:SKYLAB VOL 2 Instrumentals10:heaven.mp3ee",
               "d6:lengthi10017024e4:pathl26:SKYLAB VOL 2 Instrumentals31:hundred thousand lightyears.mp3ee",
               "d6:lengthi10491264e4:pathl26:SKYLAB VOL 2 Instrumentals14:never know.mp3ee",
               "d6:lengthi9807744e4:pathl26:SKYLAB VOL 2 Instrumentals15:rocket fuel.mp3ee",
               "d6:lengthi12483264e4:pathl26:SKYLAB VOL 2 Instrumentals19:skyline venture.mp3ee",
               "d6:lengthi11477888e4:pathl26:SKYLAB VOL 2 Instrumentals11:warpage.mp3ee",
               "d6:lengthi7366464e4:pathl41:SKYLINE LYRICS MIXTAPE (SKYLAB) BY RAZPRO36:COMPUTER SYNTHS, FUCK YOU PAY ME.mp3ee",
               "d6:lengthi4457664e4:pathl41:SKYLINE LYRICS MIXTAPE (SKYLAB) BY RAZPRO20:FAVORITE FEELING.mp3ee",
               "d6:lengthi6884288e4:pathl41:SKYLINE LYRICS MIXTAPE (SKYLAB) BY RAZPRO13:FROM HERE.mp3ee",
               "d6:lengthi10249088e4:pathl41:SKYLINE LYRICS MIXTAPE (SKYLAB) BY RAZPRO10:I KNOW.mp3ee",
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
                Map("length" -> 9805824, "path" -> List("SKYLAB VOL 2 Instrumentals", "heaven.mp3")), 
                Map("length" -> 10017024, "path" -> List("SKYLAB VOL 2 Instrumentals","hundred thousand lightyears.mp3")), 
                Map("length" -> 10491264, "path" -> List("SKYLAB VOL 2 Instrumentals","never know.mp3")), 
                Map("length" -> 9807744, "path" -> List("SKYLAB VOL 2 Instrumentals","rocket fuel.mp3")), 
                Map("length" -> 12483264, "path" -> List("SKYLAB VOL 2 Instrumentals","skyline venture.mp3")), 
                Map("length" -> 11477888, "path" -> List("SKYLAB VOL 2 Instrumentals","warpage.mp3")), 
                Map("length" -> 7366464, "path" -> List("SKYLINE LYRICS MIXTAPE (SKYLAB) BY RAZPRO","COMPUTER SYNTHS, FUCK YOU PAY ME.mp3")), 
                Map("length" -> 4457664, "path" -> List("SKYLINE LYRICS MIXTAPE (SKYLAB) BY RAZPRO","FAVORITE FEELING.mp3")), 
                Map("length" -> 6884288, "path" -> List("SKYLINE LYRICS MIXTAPE (SKYLAB) BY RAZPRO","FROM HERE.mp3")), 
                Map("length" -> 10249088, "path" -> List("SKYLINE LYRICS MIXTAPE (SKYLAB) BY RAZPRO","I KNOW.mp3")), 
                Map("length" -> 5506688, "path" -> List("SKYLINE LYRICS MIXTAPE (SKYLAB) BY RAZPRO","MOVING ON.mp3")), 
                Map("length" -> 13381568, "path" -> List("SKYLINE LYRICS MIXTAPE (SKYLAB) BY RAZPRO","REALEST FORMAT.mp3")), 
                Map("length" -> 8679744, "path" -> List("SKYLINE LYRICS MIXTAPE (SKYLAB) BY RAZPRO","SKYLINE.mp3")), 
                Map("length" -> 10224128, "path" -> List("SKYLINE LYRICS MIXTAPE (SKYLAB) BY RAZPRO","SPACE GIRL.mp3")), 
                Map("length" -> 10395008, "path" -> List("SKYLINE LYRICS MIXTAPE (SKYLAB) BY RAZPRO","STRAPPED IN.mp3")))
        assertResult(alldec)(bdecode(allenc))
        assertResult(allenc)(bencode(alldec))
    }
    
}