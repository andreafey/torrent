package torrent

import collection.mutable.ListBuffer

object Bcodr {
    /**
     * Returns a List containing Strings, Ints, Lists or Maps[String,Any]
     * Any<|BcodeAny where BcodeAny in Int, String, List[BcodeAny], Map[String,BcodeAny]
     * 3:abc => "abc" (where # is the length of the string)
     * i123e => 123
     * li123e3:abc => List(123, "abc)
     * di123e3:abc4:frooi45e => Map
     */
    def bdecode(stream:Stream[Char]):Any = bdecode(stream, ListBuffer()).toList(0)
    def bdecode(string:String):Any = bdecode(string.toStream, ListBuffer()).toList(0)
    private def bdecode(stream:Stream[Char], acc:ListBuffer[Any]):ListBuffer[Any] = stream match {
        case s if (s.isEmpty) => acc
        case ch #:: chs => ch match {
            // beginning of a String
            case n if (n.isDigit) => {
                val strlen = stream.takeWhile(p => ':' != p).mkString
        		// length + 1 because next char is ':'
                val (str, next) = stream.drop(strlen.length + 1).splitAt(strlen.toInt)
                bdecode(next, acc += str.mkString)
            }
            // end of a list or dictionary 
            case 'e' => acc
            // beginning of an Int
            case 'i' => {
                val num = chs.takeWhile(p => 'e' != p).mkString
                // len + 1 pulls the e off the end
                bdecode(chs.splitAt(num.length + 1)._2, acc += num.toInt)
            }
            // beginning of a list
            case 'l' => {
                // drop l and get a sublist ending at outer 'e'
                val list = bdecode(chs, ListBuffer()).toList
                bdecode(stream.drop(size(list)), acc += list)
            }
            // beginning of a dictionary
            case 'd' => {
                // drop d and get a sublist ending at outer 'e'
                val list = bdecode(chs, ListBuffer()).toList
                // convert each consecutive pair in the list to a key -> value pair
                val map:Map[String,Any] = list.sliding(2, 2).collect{case List(a,b) => (a.toString,b)}.toMap
                val bytes = size(map)
                bdecode(stream.drop(bytes), acc += map)
            }
            case _ => throw new IllegalArgumentException("unmatched char in bdecode")
        }
    }
    /**
     * Determines the number of characters required to represent an item
     * Any<|BcodeAny where BcodeAny in Int, String, List[BcodeAny], Map[String,BcodeAny]
     * 3:abc => "abc" (where # is the length of the string)
     * i123e => 123
     * li123e3:abc => List(123, "abc)
     * di123e3:abc4:frooi45e => Map
     */
    private def size(item:Any):Int = item match {
        case i:Int => {
            // if negative, need +1 for the '-'
            val neg = if (i < 0) 1 else 0 
            2 + neg + Math.ceil(Math.log10(Math.abs(i))).toInt
        }
        // +1 is for ':', log(len + 1) b/c log(10) = 1
        case s:String => 1 + Math.ceil(Math.log10(s.length + 1)).toInt + s.length 
        case l:List[Any] => l.foldLeft(2)((a,i) => a + size(i)) // +2 'l', 'e'
        case m:Map[_,_] => m.foldLeft(2)((a, kv) => a + size(kv._1) + size(kv._2)) // +2 'd' 'e'
    }
    /**
     * Encodes an item to a String
     * Any<|BcodeAny where BcodeAny in Int, String, List[BcodeAny], Map[String,BcodeAny]
     * 3:abc => "abc" (where # is the length of the string)
     * i123e => 123
     * li123e3:abc => List(123, "abc)
     * di123e3:abc4:frooi45e => Map
     */
    def bencode(item:Any):String = bencode(item, "")
    private def bencode(item:Any, acc:String):String = item match {
        case i:Int => acc + "i" + i.toString + "e"
        case s:String => acc + s.length + ":" + s
        case l:List[Any] => l.foldLeft(acc + "l")((a, i) => bencode(i, a)) + "e"
        case m:Map[String @unchecked,_] => {
            // keys must appear in sorted order
            val keys:List[String] = m.keys.toList.sorted
            keys.foldLeft(acc + "d")((a, k) => bencode(k, a) + bencode(m(k))) + "e"
        }
        case _ => throw new IllegalArgumentException("item must be in (Int, String, List, Map[String,_]")
    }

}
