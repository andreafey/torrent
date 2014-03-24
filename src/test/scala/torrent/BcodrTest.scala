package torrent

import org.scalatest._
import org.scalatest.Assertions.assertResult
import scala.io.Source
import torrent.Bcodr._
import scala.collection.mutable.ListBuffer

class BcodrTest extends FlatSpec {
    "items" should "create an Int if input begins with an i" in {
        assertResult(123)(bdecode("i123e".toStream).head)
        // negative numbers
        assertResult(-45)(bdecode("i-45e".toStream).head)
        // zero
        assertResult(0)(bdecode("i0e".toStream).head)
    }
    
    "items" should "create a String if input begins with a number" in {
        assertResult("whoa")(bdecode("4:whoa".toStream).head)
        // multidigit wordlength
        assertResult("the rain in Spain")(bdecode("17:the rain in Spain".toStream).head)
    }
    
    "items" should "return a List if input begins with an l" in {
        assertResult(List("whoa"))(bdecode("l4:whoae".toStream).head)
        // polymorphic list
        assertResult(List("whoa", 123))(bdecode("l4:whoai123ee".toStream).head)
        // nested lists, maps
    }
    
    "items" should "return a Map if input begins with a d" in {
        val chars1 = "d3:fooi123ee".toStream
        assertResult(Map("foo" -> 123))(bdecode(chars1).head)
        // multi-item
        val chars2 = "d3:cow3:moo4:spam4:eggse".toStream
        assertResult(Map("cow" -> "moo", "spam" -> "eggs"))(bdecode(chars2).head)
        // polymorphic
        val chars3 = "d3:fooi123e3:bar3:cowe".toStream
        assertResult(Map("foo" -> 123, "bar" -> "cow"))(bdecode(chars3).head)
        // nested dicts, lists
        val chars4 = "d3:map:d3:fooi123e3:bar3:cowe4:list:l4:whoai123eee".toStream
        assertResult(Map("map" -> Map("foo" -> 123, "bar" -> "cow"),
                "list" -> List("whoa", 123)))(bdecode(chars4).head)
//                map = "d3:map:d3:fooi123e3:bar3:cowe4:list:l4:whoai123eee"
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
        assertResult("d3:fooi123e3:bari456ee")(bencode(Map("foo" -> 123, "bar" -> 456)))
        
    }
    
}