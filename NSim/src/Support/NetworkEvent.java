package Support;				// protocol support package

import MSG.Link;

public class NetworkEvent {

    /** Protocol event protocol -> node */
    public final static int SEND = 0;

    /** Protocol event link -> node */
    public final static int RECEIVE = 1; // when in switch, need an origin

    /** Protocol event node -> link */
    // public final static int TRANSMIT = 2;

    /** Protocol event medium forward in hub/switch/router */
    // public final static int FORWARD = 3;

    /** Protocol event timeout and retransmission in TCP client */
    public final static int TIMEOUT = 4;

    public final static int REFLECT = 5;

    /** Protocol event protocol data unit */
    private PDU pdu = null;

    /** Protocol event type */
    private int eventType;

    /** Protocol event target */
    private Entity target;

    /** Protocol event origin */
    private Link related = null;

    /** protocol event time */
    private long time;

    /**
    Constructor for a protocol event.

    @param type		event type
    @param pdu		protocol data unit
     */
    public NetworkEvent(int type, PDU pdu, long time, Entity target) {
        eventType = type;
        this.pdu = pdu;
        this.time = time;
        this.target = target;
    }

    /**
    Get the PDU attribute of a protocol event

    @return   The pDU value
     */
    public PDU getPDU() {
        return(pdu);
    }

    /**
    Set the PDU attribute of a protocol event

    @return
     */
    public void setPDU(PDU pdu) {
        this.pdu = pdu;
    }

    /**
    Get the target attribute of a protocol event.

    @return		target
     */
    public Entity getTarget() {
        return(target);
    }

    /**
    Set the target attribute of a protocol event.

    @return		target
     */
    public void setTarget(Entity target) {
        this.target = target;
    }

    /**
    Get the origin attribute of a protocol event.

    @return		origin
     */
    public Link getRelatedLink() {
        return related;
    }

    /**
    Set the target attribute of a protocol event.

    @return		target
     */
    public void setRelatedLink(Link related) {
        this.related = related;
    }


    /**
    Get the type attribute of a protocol event.

    @return   The type value
     */
    public int getType() {
        return(eventType);
    }

    /**
    Set the type attribute of a protocol event.

    @return   The type value
     */
    public void setType(int eventType) {
        this.eventType = eventType;
    }

    /**
    Get the time attribute of a protocol event.

    @return   The time value
     */
    public long getTime() {
        return time;
    }

    /**
    Convert to a string representation of a protocol event.

    @return   A string representation of the object.
     */
    public String toString() {
        return("Event <Type " + eventType + " Target " + target.getName() + " Time " + this.time + ">");
    }

    public void setTime(long time) {
        this.time = time;
    }

}

