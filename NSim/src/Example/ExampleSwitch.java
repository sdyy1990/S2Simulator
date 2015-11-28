package Example;

import MSG.Link;
import SimpleBed.SimpleSwitch;
import Support.PDU;

public class ExampleSwitch extends SimpleSwitch {

	public ExampleSwitch(int port, int id) {
		super(port, id);
	}

	@Override
	public String getName() {
		return null;
	}
	//Example Switch: have 1 neighbor switch, 1 neighbor host
	@Override
	protected Link getLink(PDU pdu) {
		int dst = pdu.getdestid();
		if (dst == hosts.get(0).id)
			return ports.get(0);
		else 
			return ports.get(1);
	}

}
