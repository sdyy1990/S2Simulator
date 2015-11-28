package TCP;

import Support.Coordinate;
import Support.PDU;

public class FlowMessage extends PDU {

    public FlowId fid;
    public long timestamp;
    Coordinate coor;
    public FlowMessage(TCPMessage sdu, long timestamp) {
        super(PDU.LBDAR, 25 + sdu.size, PDU.TCP, sdu);
        this.timestamp = timestamp;
    }

    @Override
    public int getdestid() {
        return sdu.getdestid();
    }

    @Override
    public int getflowid() {
        return sdu.getflowid();
    }

    @Override
    public int getsrcid() {
        return sdu.getsrcid();
    }

}
