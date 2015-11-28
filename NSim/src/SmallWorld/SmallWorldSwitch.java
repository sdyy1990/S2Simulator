package SmallWorld;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import MSG.Host;
import MSG.Link;
import MSG.Node;
import MSG.Switch;
import Support.Coordinate;
import Support.PDU;
import Support.Simusys;
import Support.SmallWorldCoor;
import TCP.TCPCoorMessage;
public class SmallWorldSwitch extends Switch {

    private Link hostlink;
    private Map<Integer,Link> hostlinkmap = new HashMap<Integer,Link>();
    public SmallWorldSwitch(Coordinate coor, int port, int id, int routing_hop,
                            boolean flex_space) {
        super(coor, port, id, routing_hop, flex_space);
        System.out.println("Do not use this");
    }
    public SmallWorldSwitch(Coordinate coor,int port, int id,int hostcnt,int stid) {
        super(coor,port, id,0,false );
        for (int i = 0 ; i < hostcnt; i++) {
            Host host = new Host(coor,stid+i);
            hostlink = new Link(this, host);
            host.setEdgeLink(hostlink);
            host.setRelatedSwitch(this);
            this.addHost(hostlink, host);
            hostlinkmap.put(Integer.valueOf(i+stid),hostlink);
        }
        for (Integer s: hostlinkmap.keySet()) {

            System.out.print(s.intValue()+" ");
        }
        System.out.println("-->"+this.id);
        //System.out.println(id+" "+coor.toString());
    }

    @Override
    public Link getLink(PDU pdu) {
        Integer srcDest = Integer.valueOf(pdu.getsrcid()*1024+pdu.getdestid());

        int subid;
        if (!flowSrcDestLst.containsKey(srcDest)) {
            flowSrcDestLst.put(srcDest, new Vector<Integer>());
            flowSrcDestLst.get(srcDest).add(Integer.valueOf(pdu.getflowid()));
        }
        else if (flowSrcDestLst.get(srcDest).indexOf(Integer.valueOf(pdu.getflowid()))<0)
            flowSrcDestLst.get(srcDest).add(Integer.valueOf(pdu.getflowid()));
        subid = flowSrcDestLst.get(srcDest).indexOf(Integer.valueOf(pdu.getflowid()));

        Coordinate coor2 = ((TCPCoorMessage) pdu).getdestCoor();
        double dist = this.coor.dist_to_switch(coor2);
        double dist0 = dist;
        int who = -1;
        int whoid = 0;
        for (int i = 0 ; i < this.neighbours.size(); i++) {
            SmallWorldSwitch neigh = (SmallWorldSwitch) neighbours.get(i);
            double d = coor2.dist_to_switch(((Host)neighbours.get(i)).getCoor());
            if (d<dist) {
                who = i;
                dist= d;
            }
        }
        if (who < 0)  return gethostlink(pdu);
        Vector<Link> hops = new Vector<Link>();

        for (int i = 0 ; i < this.neighbours.size(); i++) {
            SmallWorldSwitch neigh = (SmallWorldSwitch) neighbours.get(i);
            double d = coor2.dist_to_switch(((Host)neighbours.get(i)).getCoor());

            if (d < dist + 0.1) {
                hops.add(ports.get(i+hosts.size()));
                if (i==d)
                    whoid = hops.size()-1;
            }

        }
        if (Simusys.loadAwareIsOn()&&pdu.first_hop) {
            pdu.first_hop = false;
            sortLinksByLoad(hops);
            return hops.get(subid % hops.size());
        } else
            return hops.get(Math.abs( (pdu.getflowid()+this.getName()).hashCode()) % hops.size());

    }
    /*


    for (int i = 0 ; i < this.neighbours.size(); i++) {
    SmallWorldSwitch neigh = (SmallWorldSwitch) neighbours.get(i);

    double d = coor2.dist_to_switch(neighbours.get(i).getCoor());
     	if (d<dist) { who = i; dist= d;}
     	if (d<dist0)
     	for (int j = 0 ; j < neigh.neighbours.size(); j++){
     		double d1 = coor2.dist_to_switch(neigh.neighbours.get(j).getCoor());
         	if (d1<dist) { who = i; dist= d1;}
     	}

    }
    if (who < 0)  return hostlink;
    Vector<Link> hops = new Vector<Link>();
    for (int i = 0 ; i < this.neighbours.size(); i++) {
    SmallWorldSwitch neigh = (SmallWorldSwitch) neighbours.get(i);

    double d = coor2.dist_to_switch(neighbours.get(i).getCoor());
    if (d<dist0)
     	for (int j = 0 ; j < neigh.neighbours.size(); j++){
     		double d1 = coor2.dist_to_switch(neigh.neighbours.get(j).getCoor());
         	if (d1<dist+0.1) { hops.add(ports.get(i+hosts.size())); break; }
     	}

    if (d < dist + 0.1) {
    	hops.add(ports.get(i+hosts.size()));
    }

    }
    return hops.get(Math.abs( (pdu.getflowid()+this.getName()).hashCode()) % hops.size());
    */

    private Link gethostlink(PDU pdu) {
        Link ans =  hostlinkmap.get(Integer.valueOf(pdu.getdestid()));
        if (ans == null) {
            for (Integer s: hostlinkmap.keySet()) {
                System.out.print(s.intValue()+" ");
            }
            System.out.println("-->"+this.id);
            System.out.println(pdu.getdestid()+".."+((TCPCoorMessage) pdu).getdestCoor());
            System.out.println(this.getCoor());

            Coordinate coor2 = ((TCPCoorMessage) pdu).getdestCoor();
            for (int i = 0 ; i < this.neighbours.size(); i++) {
                SmallWorldSwitch neigh = (SmallWorldSwitch) neighbours.get(i);
                double d = coor2.dist_to_switch(((Host)neighbours.get(i)).getCoor());
                System.out.println(neigh.getCoor()+"withdist"+d+" ");
            }
        }
        //    System.out.println( this.coor.dist_to_switch(coor2));

        return ans;
    }
}
