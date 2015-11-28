package SimpleBed;

import java.util.Vector;

import MSG.Host;
import MSG.Link;
import MSG.Node;
import Support.EventManager;
import Support.NetworkEvent;
import Support.Simusys;

public abstract class SimpleSwitch extends Node {
    protected int port;
    public int id;
    protected Vector<Link> ports;
    protected Vector< Vector<NetworkEvent>> send_events;
    protected Vector< Vector<NetworkEvent>> recv_events;
    public Vector< Node > neighbours;
    public Vector<SimpleHost> hosts;
    protected int buffer_size;
    public SimpleSwitch(int port,int id) {

        this.port = port;
        this.ports = new Vector<Link>();
        this.neighbours = new Vector<Node>();
        this.hosts = new Vector<SimpleHost>();
        buffer_size = port*1000*Link.PSIZE;
        send_events = new Vector<Vector<NetworkEvent>> ();
        recv_events = new Vector<Vector<NetworkEvent>> ();
        for (int i = 0 ; i <=port; i++) {
            send_events.add(new Vector<NetworkEvent>());
            recv_events.add(new Vector<NetworkEvent>());
        }

        EventManager.register(this);
        this.id = id;
    }

    public void addNeighbour(Link l, Node neigh) {
        this.neighbours.add(neigh);
        this.ports.add(l);
    }
    public void addHost(Link l, SimpleHost h0) {
        //if (this.neighbours.size()>0) {
        //    System.err.println("err: add Neighbour before adding host");
        //}
        if (this.ports.size()>=this.port) {
            System.err.println("err: no avialiable ports");
        }
        this.hosts.add(h0);
        this.ports.add(l);
        h0.setRelatedSwitch(this);
        h0.setEdgeLink(l);
    }

    @Override
    public String getState() {
        return "["+buffer_size+"]";
    }

    @Override
    public boolean performEvent(NetworkEvent event) {

        if (event.getTarget()== this) {
            if (event.getType()==NetworkEvent.SEND)
                return send(event);
            else if (event.getTime()==Simusys.time() && event.getType() == NetworkEvent.RECEIVE) {
                event.getRelatedLink().increaseQueueSize(this, event.getPDU().size);
                return recv(event);
            }
        }
        else
            System.err.println("event"+event.hashCode()+"Scheduler fail: Unmatched event target=" + event.getTarget().getName() +" this="+getName() +"link = "+event.getRelatedLink().hashCode()+
                               " expected at " + event.getTime()
                               + ", at " + Simusys.time());

        return false;
    }

    boolean recv(NetworkEvent event) {
        event.setType(NetworkEvent.SEND);
        event.setTarget(this);

        Link link = getLink(event.getPDU());
        
        // System.out.println("swich"+this.id+" recvpkgto"+event.getPDU().getdestid()+" link"+link.getName());
        if (buffer_size >= event.getPDU().size) {
            this.getSendQueueByLink(link).add(event);
            event.setRelatedLink(link);
            buffer_size -= event.getPDU().size;
        }
        else
            ;//System.err.println(this.getName()+"has to drop packets due to full queue");

        return false;
    }

    boolean send(NetworkEvent event) {
        if (!event.getRelatedLink().hasMoreSpace(this, event.getPDU().size))
            return false;
        //event.setTarget(this);
        buffer_size += event.getPDU().size;
        event.getRelatedLink().transmit(event);
        return true;
    }



    @Override
    public boolean performEventsAt(long tick) {
        for (int i = 0; i < this.send_events.size(); i++) {
            Vector<NetworkEvent> sb = send_events.get(i);
            while (!sb.isEmpty()) {
                NetworkEvent re = sb.get(0);
                if (!this.performEvent(re))
                    break;
                sb.remove(0);
            }
        }

        return true;
    }

    @Override
    public boolean performPendingEventsAt(long tick) {
        Vector<Vector<NetworkEvent>> rb = new Vector<Vector<NetworkEvent>>();
        int [] a = new int[recv_events.size()];
        for (int i = 0; i < recv_events.size(); i++) {
            rb.add(recv_events.get(i));
            a[i] = i;
        }
        while (!rb.isEmpty()) {
            int i = Simusys.rand.nextInt(rb.size());
            if (rb.get(i).isEmpty()) {
                rb.remove(i);
                continue;
            }

            NetworkEvent se = rb.get(i).get(0);
            if (!this.performEvent(se)) {
                rb.get(i).remove(0);
                continue;
            }
        }

        return true;

    }

    @Override
    public void addEvent(NetworkEvent e) {
        //System.out.print("adding event time"+e.getTime()+" target=" + e.getTarget().getName() +" this="+getName()+  "  link="+e.getRelatedLink().toString());
        //System.out.println("  que="+getRecvQueueByLink(e.getRelatedLink()).hashCode() +"  linkhash="+e.getRelatedLink().hashCode() + "event"+e.hashCode());
        if (e.getType()==NetworkEvent.RECEIVE) {
            this.getRecvQueueByLink(e.getRelatedLink()).add(e);
        }

    }

    protected Vector<NetworkEvent> getRecvQueueByLink(Link relatedLink) {
        return recv_events.get(ports.indexOf(relatedLink));
    }

    protected Vector<NetworkEvent> getSendQueueByLink(Link link) {
        return send_events.get(ports.indexOf(link));
    }
}
