package MSG;

import Support.Entity;
import Support.NetworkEvent;
import Support.PDU;

public abstract class Node implements Entity {

    abstract protected Link getLink(PDU pdu);

	public void addEvent(NetworkEvent e) {
	}
}
