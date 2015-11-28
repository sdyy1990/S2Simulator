package MSG;

import java.util.HashSet;
import java.util.Vector;
import Support.NetworkEvent;
import Support.Simusys;

public class Link {
    public static final int PSIZE = (int) (1280 * 1);
    private Node down;
    private Node up;

    protected double bandwidth; // in Gbps
    protected int delay; // in ticks
    protected int upq, downq; // queue size, in byte
    private int MMM;
    public int eventcount ;
    public HashSet<Integer> flowsOnMe;
    public Link(Node down, Node up) {
        this.down = down;
        this.up = up;
        this.eventcount = 0 ;
        this.bandwidth = 1;
        this.delay = 1;
        this.upq = this.downq = this.MMM = (int) (PSIZE * 1); // 10 packets in queue
        flowsOnMe=new HashSet<Integer>();
    }

    public Node getUpNode() {
        return up;
    }

    public Node getDownNode() {
        return down;
    }

    public long getDelay() {
        return delay;
    }

    //public double getBandwidth() {
    //	return bandwidth;
    //}

    public String getName() {
        return null;
    }

    protected void reduceUpQueueSize(int size) {
        upq -= size;
        if(upq < 0)
            System.err.println(this.getName() + " Link Error: Up Queue Overflow!");
    }

    protected void increaseUpQueueSize(int size) {
        upq += size;
        if(upq > MMM) {
            System.err.println(this.getName() + " Link Error: Up Queue Underflow!" + upq);
            throw new IndexOutOfBoundsException();
            // System.exit(0);
        }
    }

    protected void reduceDownQueueSize(int size) {
        downq -= size;
        if(downq < 0)
            System.err.println(this.getName() + " Link Error: Down Queue Overflow!");
    }

    protected void increaseDownQueueSize(int size) {
        downq += size;
        if(downq > MMM)
            System.err.println(this.getName() + " Link Error: Down Queue Underflow!" + downq);
    }

    public boolean transmit(NetworkEvent event) {
        //System.out.println(event.getPDU().getflowid());
        flowsOnMe.add(Integer.valueOf(1024*(event.getPDU().getsrcid()*1024+event.getPDU().getdestid()) + event.getPDU().getflowid()));

        this.eventcount++;
        event.setType(NetworkEvent.RECEIVE);
        event.setRelatedLink(this);
        int size = event.getPDU().size;
        int del = size/PSIZE + 1;
        //event.getPDU().appendhistory(this.getName());
        event.setTime(Simusys.time() + delay*del);
        event.getPDU().trasmit();
        if (event.getTarget() == up) {
            event.setTarget(down);
            reduceDownQueueSize(event.getPDU().size);
        } else if (event.getTarget() == down) {
            event.setTarget(up);
            reduceUpQueueSize(event.getPDU().size);
        } else {
            System.out.println("Invalid addEvent: this link has no queue can carry this event");
            return false;
        }

        event.getTarget().addEvent(event);
        return true;
    }

    public boolean hasMoreSpace(Node src, int size) {
        if (src == down)
            return upq >= size;
        else if (src == up)
            return downq >= size;
        return false;
    }
    /*
    	public String getState() {
    		String res = "";

    		res += "down queue size: [";res += downq;res += "] up queue size: [";
    		res += upq;	res += "]";

    		return res;
    	}
    */
    public void increaseQueueSize(Node node, int size) {
        // System.out.println("increase qs");
        if (node == up) {
            increaseUpQueueSize(size);
        } else if (node == down) {
            increaseDownQueueSize(size);
        }
    }

    public void tryReleaseFlow(int id2, int id3,int flowid) {
        flowsOnMe.remove(Integer.valueOf(1024*(id2*1024+id3) +flowid));

    }

}
