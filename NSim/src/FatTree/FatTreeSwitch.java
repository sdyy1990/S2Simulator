package FatTree;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import MSG.Link;
import SimpleBed.SimpleSwitch;
import Support.PDU;

public class FatTreeSwitch extends SimpleSwitch {
	private int k;
	public FatTreeSwitch(int port, int id) {
		super(port, id);
		k = port;
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getName() {
		return ("s"+id);
	}
    private Map<Integer,Link> routehistory = new HashMap<Integer,Link>();

    private Random rand = new Random();

	@Override
	protected Link getLink(PDU pdu) {
		Integer dprtid = Integer.valueOf(pdu.getflowid()*100000+pdu.getdestid());
		if (!routehistory.containsKey(dprtid)) {
			int dstslot = pdu.getdestid()/(k*k/4);
			if (id >= k*k) {
				//core
				Link ans = this.ports.get(dstslot);
				routehistory.put(dprtid, ans);
				return ans;
			}
			if (id >= k*k/2) {
				int myslot = (id/(k/2)) % k;
				Link ans;
				if (myslot == dstslot) {
					ans = this.ports.get(k/2 + (pdu.getdestid()/(k/2)%(k/2)));
				}
				else 
					ans = this.ports.get(rand.nextInt(k/2));
				routehistory.put(dprtid, ans);
				return ans;
			}
			Link ans;
			if (id == pdu.getdestid()/(k/2)) {
				ans = this.ports.get(k/2+pdu.getdestid()%(k/2));
			}
			else ans = this.ports.get(rand.nextInt(k/2));
			routehistory.put(dprtid, ans);
			return ans;
		
		}
		return routehistory.get(dprtid);
		
	}

}
