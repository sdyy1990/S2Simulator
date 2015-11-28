package TCP;

import Support.Coordinate;
import Support.PDU;
import Support.Simusys;

public class TCPMessage extends PDU {

    public static final int SYN = 1; // unused
    public static final int ACK = 2; // unused
    public static final int FIN = 3; // unused

    private short sport, dport;
    public int ttl;
    private long seq;
    private long ack;
    private int op = 0; // unused

    private long ts;
    private TCPBony tcpdst = null;
    private int srcid, dstid;
    public TCPMessage(int size, short sport, short dport, int ttl,int srcc ,int dstc, int flowid) {
        super(TCP, size, NIL, null);
        this.sport = sport;
        this.dport = dport;
        this.ttl = ttl;
        srcid = srcc;
        dstid = dstc;
        this.flowid = flowid;
    }
    public void setOp(int op) {
        this.op = op;
    }

    public int getOp() {
        return op;
    }

    public void setSeq(long seq) {
        this.seq = seq;
    }

    public void setAck(long ack) {
        this.ack = ack;
    }

    public long getAck() {
        return ack;
    }

    public long getSeq() {
        return seq;
    }

    public short getSport() {
        return sport;
    }

    public short getDport() {
        return dport;
    }

    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }

    public TCPBony getTcpdst() {
        return tcpdst;
    }

    public void setTcpdst(TCPBony tcpdst) {
        this.tcpdst = tcpdst;
    }
    @Override
    public int getdestid() {
        return dstid;
    }
    @Override
    public int getflowid() {
        return flowid;
    }
    @Override
    public int getsrcid() {
        return srcid;
    }
}
