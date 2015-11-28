package TCP;

import java.util.Enumeration;
import java.util.Vector;

import SimpleBed.SimpleHost;
import Support.Coordinate;
import Support.NetworkEvent;
import Support.Simusys;

public class TCPBony implements Support.Entity {

    private SimpleHost parent;
    private short port;
    private Vector<NetworkEvent> events = new Vector<NetworkEvent>();
    private TCPBony peer = null;
    private boolean active;

    private static final int PACKET_SIZE = 1024; // in byte
    private boolean[] buffer = new boolean[W];
    private static final int ELEPHANT = 500; // in KByte
    private static final int TTL = 24;
    private static final int W = 128;
    private int LFS = -1; // Last Frame Send
    private int LAR = -1; // Last Ack Received
    private int LAS = -1; // Last Ack Send
    private int LASindex = LAS + 1;
    private int cwnd = 1;
    private int frac = 0;
    private int ssthresh = 64;
    private int repeated = 0;
    private boolean fastrecovery = false;
    private int highest = -1;
    private long lastTimeOut = -1;
    private double RTOi = 48;
    private double Ri = RTOi; // Smoothed RTTime
    private double Vi = RTOi;
    private long firstSend = 0;
    private long[] sentTime = new long[W];
    private int filesize = 0; // in KB;
    private boolean ok = true;
    public int delaystart = 0;
    //retransmission bytes
    private int re = 0, out = 0;
    private int lossw = 0, nloss = 0, sumw = 0;
    private boolean inloss = false;
    private boolean need_dest_Coor_check = false;
    private boolean finish_notified = false;
    private boolean do_not_reply = false;
    public TCPBony(SimpleHost parent, short port) {
        this.parent = parent;
        parent.addConnection(this);
        this.port = port;
        // EventManager.register(this);
    }

    public TCPBony(SimpleHost parent, short port, int filesize) {
        this(parent, port);
        this.filesize = filesize;
        this.ok = false;
    }
    public void set_Coor_check() {
        need_dest_Coor_check = true;
    }
    public SimpleHost getParent() {
        return parent;
    }
    public int flowid;
    public void setDoNotReply() {
        do_not_reply = true;
    }
    public void setPeer(TCPBony peer, boolean active) {
        this.peer = peer;
        this.active = active;
        if (active)
            this.flowid = Simusys.rand.nextInt(1024) + 1;//this.parent.getnewflowid();
        else
            this.flowid = peer.flowid;
        //System.out.println(parent.id+"send to"+peer.getParent().id + "with dest"+peer.getCoor());
    }
    public TCPBony getPeer() {
        return peer;
    }

    public short getPort() {
        return port;
    }

    public boolean finished() {
        if (ok)
            return false;
        else {
            if (LAR > filesize) {
                //Notify Finish
                if (Simusys.loadAwareIsOn()) {
                    parent.tryReleaseFlow(parent.id,peer.parent.id,flowid);
                    parent.tryReleaseFlow(peer.parent.id,parent.id,peer.flowid);
                }
                return true;
            }
            else return false;
        }
    }

    @Override
    public String getName() {
        return "Port " + port + "@" + parent.getName();
    }

    @Override
    public boolean performEvent(NetworkEvent event) {
        if (this.finished()) return false;
        if (event.getTarget() == this) {
            if (event.getTime() == Simusys.time() && event.getType() == NetworkEvent.TIMEOUT)
                timeout(event);
        } else
            System.err.println("Scheduler fail: Unmatched event " + event.getTarget().getName()
                               + " expected at " + event.getTime()
                               + ", at " + Simusys.time());

        return false;
    }

    private void retransmit(TCPMessage m) {
//		System.out.println(this.getName() + " Retransmit " + m.getSeq() + "to "+this.peer.getName());
        cancelTimeout((int) m.getSeq());

        NetworkEvent nev = new NetworkEvent(NetworkEvent.SEND, m, Simusys.time(), this);
        parent.addEvent(nev);
        addEvent(new NetworkEvent(NetworkEvent.TIMEOUT, m, (long) (Simusys.time() + RTOi), this));
        sentTime[(int) (m.getSeq() % W)] = Simusys.time();

        re++;
        out++;
    }

    private boolean timeout(NetworkEvent event) {
        // System.out.println(this.getName() + " timeout at " + ((TCPMessage) event.getPDU()).getSeq());
        // ((TCPMessage) event.getPDU()).setSeq(LAR);
        retransmit((TCPMessage) event.getPDU());
        if (Simusys.time() == lastTimeOut)
            return false;

        lastTimeOut = Simusys.time();

        if (cwnd > 1) {
            ssthresh = cwnd / 2;
            cwnd = ssthresh;
            frac = 0;
        } else
            ssthresh = cwnd = 1;

        return true;
    }

    public boolean receive(TCPMessage received) {
        // System.out.println(this.getName() + "recv");
        if (do_not_reply) {
            //System.out.print(received.getinfo());
        }
        if (active) {
            int ack = (int) received.getAck();
            if (!fastrecovery && ack > LAR) {
                double R = 0;
                if (LAR + 1 == 0) {
                    Ri = Simusys.time() - firstSend;
                } else {
                    R = Simusys.time() - getSentTime(ack);
                    // R = (R <= 4.0) ? (R + 4) : R;
                    // System.out.println("R=" + Ri);
                    Ri = 0.825 * Ri + 0.125 * R;
                }

                int offset = ack - LAR;
                LAR = ack;
                repeated = 0;
                if (cwnd <= ssthresh)
                    cwnd = cwnd + 1;
                else {
                    frac += offset;
                    while (frac >= cwnd) {
                        frac -= cwnd;
                        cwnd++;
                    }
                }

                Vi = (long) (0.75 * Vi + 0.25 * Math.abs(Ri - R));
                RTOi = Ri * 16;
            } else if (!fastrecovery && ack == LAR) {
                repeated++;
                if (repeated >= 3) {
                    repeated = 0;
                    congestion();
                }
            } else if (fastrecovery && ack >= LAR) {
                if (ack >= highest) {
                    fastrecovery = false;
                    cwnd = ssthresh;
                    // ssthresh = 16;
                } else if (ack > LAR) {
                    fastRetransmit(ack);
                    cwnd = cwnd - (ack - LAR) + 1;
                }
                LAR = ack;
            }
            cancelTimeout();
            // performEvent(new NetworkEvent(NetworkEvent.SEND, null, Simusys.time(), this));
            send();
        } else {
            if (received.getDport() != port) {
                System.err.println("Err routed TCP message recved");
                return false;
            }
            /*if (need_dest_Coor_check && !received.dstCoor.equals(parent.getCoor())) {
                System.err.println("Err routed TCP message recved");
                return false;
            }*/
            int seq = (int) received.getSeq();
            int offset = seq - (LAS + 1);

            if (seq > LAS + 1 && !inloss) {
                lossw = offset + 1;
                inloss = true;
                nloss++;
                sumw += lossw;
            } else if (seq == LAS + 1 && inloss) {
                inloss = true;
            }

            if (offset >= W)
                return false; // out of buffer, drop the packet

            if (offset >= 0)
                buffer[(LASindex + offset) % W] = true;
            if (seq == LAS + 1) {
                int i = LASindex;
                while (buffer[i]) {
                    LAS++;
                    buffer[i] = false;
                    i = (i + 1) % W;
                }
                LASindex = i;
            }

            TCPMessage m = newMessage(28,  port, peer.port, TTL,parent,peer.parent,flowid);
            m.setAck(LAS);
            m.setTcpdst(peer);
            NetworkEvent nev = new NetworkEvent(NetworkEvent.SEND, m, Simusys.time(), this);
            parent.addEvent(nev);
        }

        return true;
    }
    
    public TCPMessage newMessage(int i, short port2, short port3, int ttl2,
			SimpleHost h1,SimpleHost h2, int flowid2) {
    	return new TCPMessage(i,port2,port3,ttl2,h1.id,h2.id,flowid2);
	}

	private double getSentTime(int ack) {
        // System.out.println("senttime=" + sentTime[ack % W]);

        return sentTime[ack % W];
    }

    private void fastRetransmit(int ack) {
        TCPMessage m =newMessage(PACKET_SIZE, port, peer.port, TTL,parent,peer.parent,flowid);
        m.setSeq(ack + 1);
        m.setTcpdst(peer);
        retransmit(m);
    }

    private void cancelTimeout(int seq) {
        Vector<NetworkEvent> es = new Vector<NetworkEvent>();
        for (Enumeration<NetworkEvent> enums = events.elements(); enums.hasMoreElements(); ) {
            NetworkEvent ne = enums.nextElement();
            if (ne.getType() == NetworkEvent.TIMEOUT && ((TCPMessage) ne.getPDU()).getSeq() == seq) {
                // EventManager.unregister(ne);
                es.add(ne);
            }
        }

        events.removeAll(es);
    }

    private void cancelTimeout() {
        Vector<NetworkEvent> es = new Vector<NetworkEvent>();
        for (Enumeration<NetworkEvent> enums = events.elements(); enums.hasMoreElements(); ) {
            NetworkEvent ne = enums.nextElement();
            if (ne.getType() == NetworkEvent.TIMEOUT && ((TCPMessage) ne.getPDU()).getSeq() <= LAR) {
                // EventManager.unregister(ne);
                es.add(ne);
            }
        }

        events.removeAll(es);
    }

    private void congestion() {
        if (fastrecovery)
            return;

        // System.out.println(this.getName() + " enter congestion at LAR=" + LAR + ", LFS=" + LFS);
        fastrecovery = true;
        fastRetransmit(LAR);

        if (cwnd > 1)
            cwnd = ssthresh = cwnd / 2;
        else
            cwnd = ssthresh = 1;
        cwnd += 3;
        frac = 0;

        highest = LFS;
    }

    public boolean send() {
        if (active) {
            finish_notified = false;
            // System.out.println(this.getName() + "send");

            if (LFS == -1)
                firstSend = Simusys.time();

            while (LFS - LAR < cwnd && LFS - LAR <= W && (ok || LAR <= filesize)) {

                TCPMessage m =newMessage(PACKET_SIZE, port, peer.port, TTL, parent, peer.parent,flowid);
                m.setSeq(LFS + 1);
                m.setTcpdst(peer);
                NetworkEvent nev = new NetworkEvent(NetworkEvent.SEND, m, Simusys.time()+delaystart, this);
                parent.addEvent(nev);
                addEvent(new NetworkEvent(NetworkEvent.TIMEOUT, m, (long) (Simusys.time() + RTOi+delaystart), this));
                sentTime[(int) (m.getSeq() % W)] = Simusys.time();
                LFS++;
                out++;
                delaystart = 0;
            }

            return true;
        } else
            return false;
    }

    @Override
    public boolean performPendingEventsAt(long tick) {
        for (Enumeration<NetworkEvent> enums = events.elements(); enums.hasMoreElements(); ) {
            NetworkEvent ne = enums.nextElement();
            if (ne.getTime() == tick) {
                this.performEvent(ne);
            }
        }

        return true;
    }

    @Override
    public void addEvent(NetworkEvent e) {
        events.add(e);
    }


    public boolean isElephant() {
        return LFS >= ELEPHANT;
    }

    @Override
    public String getState() {
        String res = "";

        if (active) {
            res += "active tcp, LFS=" + LFS;
            res += ", LAR=" + LAR;
            res += ", cwnd=" + cwnd;
            res += ", frac=" + frac;
            res += ", ssthresh=" + ssthresh;
            res += ", repeatd=" + repeated;
            res += ", RTOi=" + RTOi;
            if (fastrecovery)
                res += ", in fast recovery, highest=" + highest;
            double rate = (double) LAR * PACKET_SIZE * Simusys.ticePerSecond() * 8 / Simusys.time();
            res += ", rate = " + rate;
        } else {
            res += "inactive tcp, LAS=" + LAS;
            res += ", LASindex=" + LASindex;
        }

        return res;
    }

    public double getRate() {
        return (double) LAR * PACKET_SIZE * Simusys.ticePerSecond() * 8*1.0e-6 / Simusys.time();
    }

    public double getRate(long ref) {
        return (double) LAR * PACKET_SIZE * Simusys.ticePerSecond() * 8 *1.0e-6/ (Simusys.time() - ref);
    }

    public double getAmount() {
        return (double) LAR * PACKET_SIZE;
    }

    public double getOIRatio() {
        return (double) re / out;
    }

    public double getOOWindow() {
        if (nloss == 0)
            return 0;
        return (double) sumw / nloss;
    }

    @Override
    public boolean performEventsAt(long tick) {
        return false;
    }

    public boolean isActive() {
        return active;
    }

}
