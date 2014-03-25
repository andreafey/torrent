package torrent

import Bcodr.bencode
import java.io.FileInputStream
import scala.io.Source

class Metafile(val metamap:Map[String, Any], val encoded:String) {
    def this(metamap:Map[String,Any]) = this(metamap, bencode(metamap))
//	def this(encoded:String) = {
//	    // cast
//	    this(Bcodr.bdecode(encoded.toStream)(0).asInstanceOf[(Map[String,Any])], encoded)
//	}
    def this(file:String) = {
        this(Bcodr.bdecode(Source.fromFile(file).map(_.toChar).toStream)(0) match {
	        case m:Map[String,Any] => m
	        case _ => throw new IllegalArgumentException("invalid encoding")
	    })
    }
	
	def encode = bencode(metamap)
	/* REQUIRED */
//	info: a dictionary that describes the file(s) of the torrent.
	def info = metamap("info") match {
		case i: Map[String, Any] => i
	    case _ => throw new ClassCastException
	}
	// announce URL of the tracker
	// TODO this is supposedly required but not found in test files
	val announce = metamap.getOrElse("announce", None)
	val multifile = info.contains("files")
	
	// TODO is this really space separated?
	// number of bytes in each piece; often 256, 512, or 1024 (not recommended for small torrents)
	val pieceLength:Int = info("piece length") match {
	    case i:Int => i
	    case _ => throw new ClassCastException
	}
	// pieces is SHA1 not urlencoded
	// pieces is the concatenated 20 Byte SHA1 hashes of each piece
	// TODO this does not seem to be what I expected - looks like binary data
	val pieces:String = info("pieces") match {
	    case s:String => s
	    case _ => throw new ClassCastException
	}
    
    /* FILES */
	/**
	 * returns List of file metadata which contains exactly one entry if this is
	 * a single-file torrent and more if it is multifile
	 */
    def files:List[FileSpec] = if (multifile) {
        info("files") match {
            case l:List[Map[String,Any]] =>
                l map (m => new FileSpec(length(m), name, path(m), md5lookup(m)))
            case _ => throw new ClassCastException
        }
    } else {
        List(new FileSpec(length(metamap), None, name, md5lookup(metamap)))
    }
	
	/* OPTIONAL */
	/* not required to be in info dictionary */
	val pryvate:Boolean = if (info.contains("private")) {
	    info("private") match {
	        case 1 => true
	        case 0 => false
	        case _ => throw new IllegalStateException
	    }
	} else false

//  this is an extention to the official specification,
//  offering backwards-compatibility..
	val announceList = metamap.getOrElse("announce-list", None) match {
	    case None => None
	    case l:List[List[String]] => Some(l)
        case _ => throw new ClassCastException
	}
	
//  the creation time of the torrent, in standard UNIX epoch 
//  format (integer, seconds since 1-Jan-1970 00:00:00 UTC)
	val creationDate = metamap.getOrElse("creation-date", None) match {
	    case None => None
	    case l:Int => Some(l)
        case _ => throw new ClassCastException
	}
//  free-form textual comments of the author (string)
    val comment = metamap.getOrElse("comment", None) match {
	    case None => None
	    case l:String => Some(l)
        case _ => throw new ClassCastException
    }
//  name and version of the program used to create the .torrent (string)
    val createdBy = metamap.getOrElse("createdBy", None)

//  the string encoding format used to generate the pieces part
//  of the info dictionary in the .torrent metafile (string)
    val encoding = metamap.getOrElse("encoding", None)
	
	    
	/* PRIVATE FUNCTIONS */
	// length of a file in bytes (single file only)
    private def length(map:Map[String,Any]):Int = map("length") match {
        case len:Int => len
        case _ => throw new ClassCastException
    }
    // not used by BitTorrent, but included for compatibility
    private def md5lookup(map:Map[String, Any]):Option[String] = 
        map.getOrElse("md5sum", None) match {
        case None => None
        case md5:String => Some(md5)
        case _ => throw new ClassCastException
    }
    // recommended filename if single-file; recommended directory name if multifile
    private def name:Option[String] = info.getOrElse("name", None) match {
        case None => None
    	case n:String => Some(n)
    	case _ => throw new ClassCastException
    }
    // path to file relative to main dir
    private def path(map:Map[String,Any]):Option[String] = 
        map.getOrElse("path", None) match {
        case None => None
        // TODO replace with path separator
        case l:List[String] => Some(l.reduceLeft(_ + "/" + _))
        case _ => throw new ClassCastException
    }
    
    class FileSpec(val length:Int, val dir:Option[String], val path:Option[String] = None, val md5sum:Option[String] = None) {
        override def toString = {
            val map = Map("dir" -> dir, "path" -> path, "md5sum" -> md5sum)
            "[FileSpec] len=" + length + (map.filter {case (k,v) => v.isDefined}).mkString
        }
    }

}
