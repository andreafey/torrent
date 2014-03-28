package torrent

import annotation.tailrec
import collection.mutable.ListBuffer
import com.typesafe.scalalogging.slf4j.Logging

object Bcodr extends Logging {
    
    /**
     * Returns a List containing Strings, Ints, Lists or Maps[String,Any]
     * 3:abc => "abc" (where # is the length of the string)
     * i123e => 123
     * li123e3:abc => List(123, "abc)
     * di123e3:abc4:frooi45e => Map
     */
    def bdecode(stream:Stream[Char]):List[Any] = bdecode(stream, ListBuffer()).toList
    private def bdecode(stream:Stream[Char], acc:ListBuffer[Any]):ListBuffer[Any] 
    		= stream match {
        case s if (s.isEmpty) => {
            logger.trace("{bdecode} stream empty; returning out of recursion" + acc.mkString(","))
            acc
        }
        case ch #:: _ => {
            stream.drop(1) // ch
            ch match {
                // end of a list or dictionary 
                case e if (e == 'e') => {
                    logger.trace("{bdecode}('e') returning out of recursion" + acc.mkString(","))
                    acc
                }
                // beginning of an Int
	            case i if (i == 'i') => {
	                val numStr = stream.tail.takeWhile(p => 'e' != p).mkString
	                val (num, len) = (Integer.parseInt(numStr), numStr.length)
	                logger.trace("{bdecode}('i') parsing int -> numStr=" + numStr + ", num=" + num + ", len=" + len)
	                // pull the e off the stream
	                bdecode(stream.splitAt(2 + len)._2, acc += num)
	            }
	            // beginning of a String
	            case n if (n.isDigit) => {
	                val countStr = stream.takeWhile(p => ':' != p).mkString("")
	                val (_, tail) = stream.splitAt(countStr.length + 1)
	                val (str, tail2) = tail.splitAt(Integer.parseInt(countStr))
	                logger.trace("{bdecode}('n') parsing String -> countStr=" + countStr + "(:)str=" + str.mkString)
	                bdecode(tail2, acc += str.mkString)
	            }
	            // you are at the beginning of a list
	            case l if (l == 'l') => {
	                // drop l and get a sublist ending at outer 'e'
	                val list = bdecode(stream.tail, ListBuffer()).toList
	                val bytes = size(list)
	                logger.trace("{bdecode}('l') parsing list -> bytes=" + bytes + ", list=" + list.mkString(","))
	                bdecode(stream.drop(bytes), acc += list)
	            }
	            // you are at the beginning of a dictionary
	            case d if (d == 'd') => {
	                // drop d and get a sublist ending at outer 'e'
	                val list = bdecode(stream.tail, ListBuffer()).toList
	                val map:Map[String,Any] = list.sliding(2, 2).collect{case List(a,b) => (a.toString,b)}.toMap
	                val bytes = size(map)
	                logger.trace("{bdecode}('d') parsing dict -> bytes=" + bytes + ", map=" + map.mkString(","))
	                bdecode(stream.drop(bytes), acc += map)
	            }
	            // this happens when you are getting a list of keys and values while processing a map
	            case noop if (noop == ':') => bdecode(stream.tail, acc)
	            case wha => {
	            	logger.debug("{bdecode} unexpected result = " + wha + "; acc = " + acc.mkString(","))
	            	throw new IllegalArgumentException("unmatched char in bdecode")
	            }
          }
        }
    }
    
    /**
     * Determine the number of characters required to represent an item
     * 3:abc => "abc" (where # is the length of the string)
     * i123e => 123
     * li123e3:abc => List(123, "abc)
     * di123e3:abc4:frooi45e => Map
     */
     def size(item:Any):Int = item match {
        case i:Int => {
            val neg = if (i < 0) 1 else 0
            2 + neg + Math.ceil(Math.log10(Math.abs(i))).toInt
        }
        case s:String => 1 + Math.ceil(Math.log10(s.length + 1)).toInt + s.length // +1 ':', log(len + 1) b/c log(10) = 1
        case l:List[Any] => l.foldLeft(2)((a,i) => a + size(i)) // +2 'l', 'e'
        case m:Map[String,Any] => m.foldLeft(2)((a, kv) => a + size(kv._1) + size(kv._2)) // +2 'd' 'e'
    }
    /**
     * Encode an item to a String
     * Any<|BcodeAny where BcodeAny in Int, String, List[BcodeAny], Map[String,BcodeAny]
     * 3:abc => "abc" (where # is the length of the string)
     * i123e => 123
     * li123e3:abc => List(123, "abc)
     * di123e3:abc4:frooi45e => Map
     */
    // better to use default or make private scoped helper method?
    def bencode(item:Any, acc:String = ""):String = item match {
        case i:Int => acc + "i" + i.toString + "e"
        case s:String => acc + s.length + ":" + s
        case l:List[Any] => l.foldLeft(acc + "l")((a, i) => bencode(i, a)) + "e"
        case m:Map[String,Any] => {
            // keys must appear in sorted order
            val keys = m.keys.toList.sorted
            keys.foldLeft(acc + "d")((a, k) => bencode(k, a) + bencode(m(k))) + "e"
        }
        case _ => throw new IllegalArgumentException("item must be in (Int, String, List, Map[String,Any]")
    }

}