package torrent

import org.scalatest._
import org.scalatest.Assertions._
import scala.io.Source
import torrent.Bencoder._
import scala.collection.mutable.ListBuffer

class BencoderTest extends FlatSpec {
//    "collectInt" should "parse ints off the stream until reaching an e" in {
//        val chars:Stream[Char] = Stream('1', '2', '3', 'e')
//        val item:Int = collectInt(chars, 0)
//        assertResult(123)(item)
//    }
//    
    "items" should "return an Int if input begins with an i" in {
        val chars:Stream[Char] = "i123e".toStream
        val item = items(chars).head
        assertResult(123)(item)
    }
    
    "items" should "return a String if input begins with a number" in {
        val chars:Stream[Char] = "4:whoa".toStream
        val item = items(chars).head
        assertResult("whoa")(item)
    }
    
    "items" should "return a List if input begins with an l" in {
        val chars:Stream[Char] = "l4:whoae".toStream
        assertResult(List("whoa"))(items(chars).head)
    }
    
    "items" should "return a Map if input begins with a d" in {
        val chars1 = "d3:fooi123ee".toStream
        assertResult(Map("foo" -> 123))(items(chars1).head)
        val chars2 = "d3:cow3:moo4:spam4:eggse".toStream
        assertResult(Map("cow" -> "moo", "spam" -> "eggs"))(items(chars2).head)
        val chars3 = "d3:fooi123e3:bari456ee".toStream
        assertResult(Map("foo" -> 123, "bar" -> 456))(items(chars3).head)
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
    
}