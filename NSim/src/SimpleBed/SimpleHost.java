package SimpleBed;

import java.util.Vector;

import MSG.Link;
import MSG.Node;
import Support.EventManager;
import Support.NetworkEvent;
import Support.PDU;
import Support.Simusys;
import TCP.FlowId;
import TCP.FlowMessage;
import TCP.TCPBony;
import TCP.TCPMessage;

public class SimpleHost extends Node {

    protected int flowcount = 0;
    public int id;
    protected Link edge;
    protected SimpleSwitch relatedSwitch;

    protected Vector<TCPBony> connections = new Vector<TCPBony>();
    protected Vector<Vector<NetworkEvent>> send_buffer = new Vector<Vector<NetworkEvent>>();
    protected Vector<NetworkEvent> received_buffer = new Vector<NetworkEvent>();

    protected int buffer_sz = 1000 * Link.PSIZE;
    public SimpleHost(int id){

        EventManager.register(this);
        this.id = id;
        this.flowcount = 0;
    }

	public void setEdgeLink(Link edge) {
		this.edge = edge;
		
	}
    public boolean performPendingEventsAt(long tick) {
        while (!received_buffer.isEmpty()) {
            // System.out.println(this.getName() + " receive At" + tick);
            NetworkEvent re = received_buffer.get(0);
            if (re.getType() != NetworkEvent.RECEIVE) {
                System.err.println("NON-RECEIVE event in rb!");
                System.exit(0);
            }

            if (!this.performEvent(re))
                break;
            received_buffer.remove(0);
        }

        return true;
    }

	public void addConnection(TCPBony tcp) {
		connections.add(tcp);
		send_buffer.add(new Vector<NetworkEvent>());
		if (connections.size() != send_buffer.size()) 
			System.err.println("Endhost create TCP connection fail!");
	}

    public boolean performEventsAt(long tick) {

        for (int i = 0; i < connections.size(); i++) {
            connections.get(i).performPendingEventsAt(tick);
        }

        Vector<Vector<NetworkEvent>> sb_copy = new Vector<Vector<NetworkEvent>>();
        for (int i = 0; i < send_buffer.size(); i++)
            sb_copy.add(send_buffer.get(i));
        while (!sb_copy.isEmpty()) {
            int i = Simusys.rand.nextInt(sb_copy.size());
            if (sb_copy.get(i).isEmpty()) {
                sb_copy.remove(i);
                continue;
            }

            NetworkEvent se = sb_copy.get(i).get(0);
            if (!this.performEvent(se))
                break;
            sb_copy.get(i).remove(0);
        }

        return true;
    }


    public void addEvent(NetworkEvent e) {

        if (e.getType() == NetworkEvent.SEND && buffer_sz > e.getPDU().size) {
            TCPBony kid = (TCPBony) e.getTarget();

            if (kid.isElephant() && kid.isActive()) {
                TCPMessage m = (TCPMessage) e.getPDU();
                FlowMessage fm = new FlowMessage(m, Simusys.time());
                fm.fid = new FlowId(m.getsrcid(), m.getdestid(), m.getSport(), m.getDport());
                // System.out.println(this.getName() + " elephant " + fm.fid);
                e.setPDU(fm);
            }
            //	System.out.println(this.getName() + " addEve "+e.getPDU().getsrcid()+"->"+e.getPDU().getdestid());
            if (this.id != e.getPDU().getsrcid()) {
                System.out.println("somthing wrong");
            }
            int i = connections.indexOf(kid);
            e.setTarget(this);
            send_buffer.get(i).add(e);
            buffer_sz -= e.getPDU().size;
        } else if (e.getType() == NetworkEvent.RECEIVE) {
            received_buffer.add(e);
        } else {
            // System.out.println(this.getName() + " Drop packet due to buffer overflow: " + buffer_sz +
            //	" from " + ((TCPMessage) e.getPDU()).getSport() +
            //	" Seq: " + ((TCPMessage) e.getPDU()).getSeq());
        }
    }

    public boolean performEvent(NetworkEvent event) {
        if (event.getTarget() == this) {
            if (event.getType() == NetworkEvent.SEND)
                return send(event);
            else if (event.getTime() == Simusys.time() && event.getType() == NetworkEvent.RECEIVE) {
                event.getRelatedLink().increaseQueueSize(this, event.getPDU().size);
                return receive(event);
            } else
                return false;
        } else
            System.err.println("Scheduler fail: Unmatched event " + event.getTarget().getName()
                               + " expected at " + event.getTime()
                               + ", at " + Simusys.time());
        return false;
    }

    protected boolean receive(NetworkEvent event) {
        PDU pdu = event.getPDU();

        if (pdu.type == PDU.LBDAR) {
            pdu = pdu.sdu;
        }

        if (pdu.type == PDU.TCP) {

            TCPMessage m = (TCPMessage) pdu;
            if (m.getdestid() != this.id) {
                System.err.println(pdu.routehistory);
                System.err.println("Routing Error: A non relevent packet routed "+m.getdestid()+"to host!"+id);

                System.exit(0);
            }

            TCPBony tcp = m.getTcpdst();
            tcp.receive(m);
            return true;
        }

        return true;
    }

    protected boolean send(NetworkEvent event) {
        if (!edge.hasMoreSpace(this, event.getPDU().size)) {
            // link busy: stay in buffer
            return false;
        }

        // System.out.println(this.getName() + "send");
        event.setTarget(this);
        buffer_sz += event.getPDU().size;
        edge.transmit(event);
        return true;
    }

    protected Link getLink(PDU pdu) {
        return edge;
    }

    public int getnewflowid() {
        flowcount ++;
        return flowcount;
    }

    public void setRelatedSwitch(SimpleSwitch simpleSwitch) {
        this.relatedSwitch = simpleSwitch;
    }
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String getState() {
		// TODO Auto-generated method stub
		return null;
	}

    public void tryReleaseFlow(int id2, int id3, int flowid) {
        Simusys.tryReleaseFlow(id2, id3,flowid);
    }
}
