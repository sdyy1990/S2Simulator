package TCP;

import Support.Coordinate;

public class TCPCoorMessage extends TCPMessage {

    public Coordinate srcCoor,dstCoor;

    public TCPCoorMessage(int size, Coordinate src, Coordinate dest, short sport, short dport, int ttl,int srcc ,int dstc, int flowid) {
        super(size,sport,dport,ttl,srcc,dstc,flowid);
        this.srcCoor= src;
        this.dstCoor = dest;
    }

    public Coordinate getdestCoor() {
        return dstCoor;
    }
}
