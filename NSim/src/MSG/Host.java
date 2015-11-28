package MSG;

import java.util.Vector;


import SimpleBed.SimpleHost;
import Support.Coordinate;
import Support.EventManager;
import Support.NetworkEvent;
import Support.PDU;
import Support.PathLengthCounter;
import Support.Simusys;
import TCP.FlowId;
import TCP.FlowMessage;
import TCP.TCPBony;
import TCP.TCPMessage;

public class Host extends SimpleHost {

    private Coordinate coor;
    public Host(Coordinate d,int id) {
    	super(id);
        coor = d;
        
    }

    public String getName() {
        return "MSH"+id;
    }

    private PathLengthCounter pathLengthCounter = null;

    

    @Override
    protected boolean receive(NetworkEvent event) {
        PDU pdu = event.getPDU();

        if (pdu.type == PDU.LBDAR) {
            pdu = pdu.sdu;
        }

        if (pdu.type == PDU.TCP) {

            TCPMessage m = (TCPMessage) pdu;
            if (pathLengthCounter!=null) {
                pathLengthCounter.set(pdu.getsrcid(), this.id, pdu.getlinkcount());
            }
            //if (m.getsrcid() == this.id) return true;
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
    @Override
    public String getState() {
        return "unimplemented yet";
    }
    public Coordinate getCoor() {
        return coor;
    }
    public void setCoordinate(Coordinate c) {
        this.coor = c;
    }
    public Switch getRelatedSwitch() {
        return (Switch) relatedSwitch;
    }


}
