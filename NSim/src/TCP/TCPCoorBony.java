package TCP;

import MSG.Host;
import SimpleBed.SimpleHost;


public class TCPCoorBony extends TCPBony {

	public TCPCoorBony(Host here, short s) {
		super(here,s);
	}
	public TCPCoorBony(Host here, short s, int flowsizeKB) {
		super(here,s,flowsizeKB);
	}
	public TCPMessage newMessage(int i, short port2, short port3, int ttl2,
			SimpleHost h1,SimpleHost h2, int flowid2) {
		return new TCPCoorMessage(i,((Host) h1).getCoor(),((Host) h2).getCoor(),port2,port3,ttl2,h1.id,h2.id,flowid2);
	}
}
