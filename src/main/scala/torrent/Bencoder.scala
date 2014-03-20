package torrent

import annotation.tailrec
import collection.mutable.ListBuffer

object Bencoder {
    
    /**
     * Returns a List containing Strings, Ints, Lists or Maps[String,Any]
     * 3:abc => "abc" (where # is the length of the string)
     * i123e => 123
     * li123e3:abc => List(123, "abc)
     * di123e3:abc4:frooi45e => Map
     */
    def items(stream:Stream[Char]):List[Any] = items(stream, ListBuffer()).toList
    private def items(stream:Stream[Char], acc:ListBuffer[Any]):ListBuffer[Any] 
    		= stream match {
        case s if (s.isEmpty) => acc
        case ch #:: _ => {
            val ch:Char = stream.take(1).toList(0)
            ch match {
                // doing list or dict parsing
                case c if (c == 'e') => acc
	            case c if (c == 'i') => {
	                val num = stream.tail.takeWhile(p => 'e' != p).mkString("")
	                // pull the e off the stream
	                items(stream.splitAt(2 + num.length)._2, acc += Integer.parseInt(num))
	            }
	            case c if (c.isDigit) => {
	                val countStr = stream.takeWhile(p => ':' != p).mkString("")
	                val (_, tail) = stream.splitAt(countStr.length + 1)
	                val (str, tail2) = tail.splitAt(Integer.parseInt(countStr))
	                items(tail2, acc += str.mkString(""))
	            }
	            case c if (c == 'l') => {
	                // drop l and get a sublist ending at outer 'e'
	                val list = items(stream.tail, ListBuffer()).toList
	                val bytes = size(list)
	                items(stream.drop(bytes), acc += list)
	            }
	            case m if (m == 'd') => {
	                // drop d and get a sublist ending at outer 'e'
	                val list = items(stream.tail, ListBuffer()).toList
	                val map:Map[String,Any] = list.sliding(2, 2).collect{case List(a,b) => (a.toString,b)}.toMap
	                val bytes = size(map)
	                items(stream.drop(bytes), acc+= map)
	            }
          }
        }
    }
    
    // (str => 1 + floor(log_10(str.length)) + str.length) [charlen of digits (log + 1)]
    // (int => 3 + floor(log_10(int))) ['i', 'e', charlen of digits]
    // (list => 2 + sum(list.reduce(itemSize(_) + itemSize(_)))) ['l', 'e']
    def size(item:Any):Int = item match {
        case i:Int => {
            val neg = if (i < 0) 1 else 0
            2 + neg + Math.ceil(Math.log10(Math.abs(i))).toInt
        }
        case s:String => 1 + Math.ceil(Math.log10(s.length)).toInt + s.length // +1 ':'
        case l:List[Any] => l.foldLeft(2)((a,i) => a + size(i)) // +2 'l', 'e'
        case m:Map[String,Any] => m.foldLeft(2)((a, kv) => a + size(kv._1) + size(kv._2)) // +2 'd' 'e'
    }
//    @tailrec
//    def collectInt(stream:Stream[Char], acc:Int):Int = stream.head match {
//        case 'e' => acc
//        case num => collectInt(stream.tail, acc*10 + num.asDigit)
//    }
//    def atEnd(c:Char) = 'e' == c

}