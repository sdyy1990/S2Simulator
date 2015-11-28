package Jellyfish;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import MSG.Host;
import MSG.Link;
import MSG.Switch;
import Support.Coordinate;
import Support.PDU;
import TCP.TCPMessage;

public class Jellyfish_Switch extends Switch {

    private int routeTableLength = 0;
    private Vector< Vector<Vector<Link>  > > routetable;
    private Vector< Vector<Vector<Integer>  > > routeIDS;
    private int nxtAviliableLink = 0;
    private Host[] globalhosts;
    private Map<String,Link> LinkMaps = new HashMap<String,Link> ();
    private Map<Integer,Link> LinktokenMaps = new HashMap<Integer,Link> ();
    public Jellyfish_Switch(int port, int id, int RL, Host[] hosts) {
        super(null,port,id,0,false);
        LinktokenMaps.clear();
        this.routeTableLength =  RL;
        routetable = new Vector< Vector<Vector<Link> > > ();
        routeIDS = new Vector< Vector<Vector<Integer>  > > ();
        for (int i = 0 ; i < RL+1; i++) {
            routetable.add(new Vector<Vector<Link>  >());
            routeIDS.add(new Vector<Vector<Integer>  >());
            for (int j = 0 ; j < RL+1; j++) {
                routetable.get(i).add(null);
                routeIDS.get(i).add(null);
            }
        }
        this.globalhosts = hosts;
    }
    public Link getLink(PDU pdu) {
        int dstid = pdu.getdestid();

        Jellyfish_Switch jDest = (Jellyfish_Switch) globalhosts[dstid].getRelatedSwitch();
        Jellyfish_Switch jSource = (Jellyfish_Switch) globalhosts[pdu.getsrcid()].getRelatedSwitch();
        //System.out.println(this.getName()+"getting link src"+jSource.getName()+" dst"+jDest.getName());
        if (jDest == this) {
            int id = hosts.indexOf(globalhosts[dstid]);
            return ports.get(id);
        }
        if (pdu.pathtoken>=0) {
            return getLink(pdu.pathtoken);
        }
        Link ans =  getLink(jSource,jDest, pdu);
        //System.out.println("res = "+ans.toString());
        return ans;
    }
    private Link getLink(int pathtoken) {
        Link who = LinktokenMaps.get(Integer.valueOf(pathtoken));
        if (who == null) {

            Iterator iter = LinktokenMaps.keySet().iterator();
            while (iter.hasNext()) {
                System.out.println(iter.next().toString());
            }
            System.out.println("who");

        }
        return who;
    }
    public Link getLink(Jellyfish_Switch src, Jellyfish_Switch dest, PDU pdu) {
        int kk = routetable.get(src.id).get(dest.id).size();

        String str = src.id +"+"+dest.id+" "+pdu.getflowid();
        if (!LinkMaps.containsKey(str)) {
            LinkMaps.put(str, routetable.get(src.id).get(dest.id).get(Math.abs(str.hashCode())%kk));//);
        }
        int pathtoken  = routeIDS.get(src.id).get(dest.id).get(Math.abs(str.hashCode())%kk).intValue() ;
        pdu.setpathtoken(pathtoken);
        return LinkMaps.get(str);
        //int as = str.hashCode(); if (as<0) as = -as;
        //System.out.println(as);
        //return routetable.get(src.id).get(dest.id).get(as % kk);

    }
    public void setSrcDestWithLink(Jellyfish_Switch src, Jellyfish_Switch h, Link l,int id) {
        if (routetable.get(src.id).get(h.id) == null ) {
            routetable.get(src.id).set(h.id, new Vector<Link>());
            routeIDS.get(src.id).set(h.id,new Vector<Integer>());
        }
        routetable.get(src.id).get(h.id).add(l);
        routeIDS.get(src.id).get(h.id).add(Integer.valueOf(id));
        LinktokenMaps.put(Integer.valueOf(id), l);
    }
    public void setDestWithNode(Jellyfish_Switch src, Jellyfish_Switch dest,
                                Jellyfish_Switch there,int id) {
        for (Link l: ports) {
            if (l.getUpNode().equals(there)) {
                setSrcDestWithLink(src,dest,l,id);
                return;
            }
            if (l.getDownNode().equals(there)) {
                setSrcDestWithLink(src,dest,l,id);
                return;
            }
        }
        System.out.println("ERROR setting dest with node");
    }


}
