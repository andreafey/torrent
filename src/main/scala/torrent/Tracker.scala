package torrent

import scalaj.http.Http._

class Tracker {
    
    // GET request
    def respond(req:Request) = {
//        Http.Response //
        
    }
//The tracker responds with "text/plain" document consisting of a bencoded dictionary with the following keys:
//failure reason: If present, then no other keys may be present. The value is a human-readable error message as to why the request failed (string).
//warning message: (new, optional) Similar to failure reason, but the response still gets processed normally. The warning message is shown just like an error.
//interval: Interval in seconds that the client should wait between sending regular requests to the tracker
//min interval: (optional) Minimum announce interval. If present clients must not reannounce more frequently than this.
//tracker id: A string that the client should send back on its next announcements. If absent and a previous announce sent a tracker id, do not discard the old value; keep using it.
//complete: number of peers with the entire file, i.e. seeders (integer)
//incomplete: number of non-seeder peers, aka "leechers" (integer)
//peers: (dictionary model) The value is a list of dictionaries, each with the following keys:
//peer id: peer's self-selected ID, as described above for the tracker request (string)
//ip: peer's IP address either IPv6 (hexed) or IPv4 (dotted quad) or DNS name (string)
//port: peer's port number (integer)
//peers: (binary model) Instead of using the dictionary model described above, the peers value may be a string consisting of multiples of 6 bytes. First 4 bytes are the IP address and last 2 bytes are the port number. All in network (big endian) notation.
}