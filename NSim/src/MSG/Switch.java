package MSG;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import SimpleBed.SimpleSwitch;
import Support.Coordinate;
import Support.EventManager;
import Support.NetworkEvent;
import Support.PDU;
import Support.Simusys;
import TCP.TCPCoorMessage;
public class Switch extends SimpleSwitch {

    public Coordinate coor;
    private int routing_neighbour_hop;
    private boolean flex_space_routing;
    protected Map< Integer, Vector<Integer> > flowSrcDestLst;
    private Map< Integer, Link > routeHistory;

    public Switch( Coordinate coor, int port , int id , int routing_hop, boolean flex_space) {
        super(port,id);
        routeHistory = new HashMap<Integer,Link> ();
        flowSrcDestLst = new HashMap< Integer, Vector<Integer> >();
        this.coor = coor;
        this.routing_neighbour_hop = routing_hop;
        this.flex_space_routing = flex_space;
    }



    protected double calculation_dist(Coordinate a, Coordinate b, PDU pdu) {
        if (this.flex_space_routing)
            return a.dist_to_switch(b);
        else
            return a.dist_to_switch_fixdim(b,pdu.getflowid());
    }
    protected Link getLink(PDU pdu) {
    	Integer myid = Integer.valueOf(pdu.getsrcid()*1024+pdu.getdestid()*1048576+pdu.getflowid());
    	if (routeHistory.containsKey(myid)) {
    		return routeHistory.get(myid);
    	}
    	else {
    		Link link = getLinkHere(pdu);
            routeHistory.put(myid, link);
            return link;
    	}
		
    }
    private Link getLinkHere(PDU pdu){
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
        if (this.routing_neighbour_hop ==0) return null;
        double dist = calculation_dist(this.coor,coor2,pdu);
        int who = -1;
        for (int i = 0 ; i < this.neighbours.size(); i++) {
            double d = coor2.dist_to_switch(((Switch)neighbours.get(i)).getCoor());
            if (d<dist) {
                who = i;
                dist= d;
            }
        }
        if (who < 0)  return getHostLink(coor2);
        if (pdu.first_hop) {
            if (pdu.first_hop)
                pdu.first_hop = false;
            else pdu.second_hop = false;

            //if (dist < 1e-8) return ports.get(who+hosts.size()); //1hop neigh;
            Vector<Link> possible_2hop = new Vector<Link>();

            possible_2hop.add(ports.get(who+hosts.size()));

            for (int i = 0 ; i < this.neighbours.size(); i++) {
                Switch neigh = (Switch) this.neighbours.get(i);
                //if (dist >  coor2.dist_to_switch(neigh.getCoor()))
                for (Node nn0: neigh.neighbours) if (nn0!=this) {
                	Switch nn = (Switch) nn0;
                        double d = coor2.dist_to_switch(nn.getCoor());
                        if (d<dist) {
                            if (possible_2hop.indexOf(ports.get(i+hosts.size()))<0)
                                possible_2hop.add(ports.get(i+hosts.size()));
                            break;
                        }
                    }
            }
            if (possible_2hop.size()>0) {
                if (Simusys.loadAwareIsOn()) {
                    sortLinksByLoad(possible_2hop);
                    return possible_2hop.get(subid % possible_2hop.size());
                } else
                    return possible_2hop.get(randomIdWithUpperBound(pdu.getflowid(), possible_2hop.size()) );
            }
        }
        who = -1;
        dist = 1e10;
        return getLink_OCT30(pdu);
    }
    int randomIdWithUpperBound(int seed, int up) {
        return seed % up;
    }
    protected void sortLinksByLoad(Vector<Link> VL) {
        for (int i = 0 ; i < VL.size(); i++)
            for (int j = i+1 ; j < VL.size(); j++)
                if (VL.get(i).flowsOnMe.size() > VL.get(j).flowsOnMe.size())
                    Collections.swap(VL,i,j);

    }
    protected Link getLink_Nov1(PDU pdu) {
        Coordinate coor2 = ((TCPCoorMessage) pdu).getdestCoor();
        if (this.routing_neighbour_hop ==0) return null;
        double dist = this.coor.dist_to_switch(coor2);
        double dist0 = dist;
        Vector<Link> candidate = new Vector<Link>();
        int [] spaces = new int [100];
        if (dist < 1e-9) return getHostLink(coor2);
        for (int i = 0 ; i < this.neighbours.size(); i++) {
            double d = coor2.dist_to_switch(((Switch)neighbours.get(i)).getCoor());
            if (d< 1e-9)  //candidate.add(ports.get(hosts.size()+i));
                return ports.get(hosts.size()+i);
        }
        if (pdu.first_hop) {
            pdu.first_hop= false;
            for (int i = 0 ; i < this.neighbours.size(); i++) {
                Switch neigh = (Switch) this.neighbours.get(i);
                //if (dist0 > calculation_dist(coor2,neigh.getCoor(),pdu))
                for (Node nn: neigh.neighbours) if (nn!=this) {
                        double d = coor2.dist_to_switch(((Switch)nn).getCoor());
                        if (d<1e-9) //candidate.add(ports.get(hosts.size()+i));
                            return ports.get(hosts.size()+i);
                    }
            }
            for (int space = 0 ; space < coor2.getdim(); space ++) {
                double mm = coor2.dist_to_switch_fixdim(this.coor, space);
                double mm0 = mm;
                int who = -1;
                for (int i = 0 ; i < this.neighbours.size(); i++) {
                    Switch neigh = (Switch) this.neighbours.get(i);
                    //		if (mm0 > coor2.dist_to_switch_fixdim(neigh.getCoor(),space))
                    for (Node nn: neigh.neighbours) if (nn!=this) {
                            double d = coor2.dist_to_switch_fixdim(((Switch)nn).getCoor(),space);
                            if (d<mm) {
                                mm = d;
                                who = i;
                            }
                        }
                }
                if (who<0) continue;
                if (candidate.indexOf(ports.get(hosts.size()+who))<0) {
                    candidate.add(ports.get(hosts.size()+who));
                    spaces[candidate.indexOf(ports.get(hosts.size()+who))] = space;
                }

            }
            int tod = Simusys.rand.nextInt(candidate.size());
            pdu.routingSpace = spaces[tod];
            return candidate.get(tod);
        }
        if (candidate.size() >0) return candidate.get(0);
        if (pdu.routingSpace < 0) System.out.println("Errrrrr");

        double dd = coor.dist_to_switch_fixdim(coor2,pdu.routingSpace);
        double dd0 = dd;
        int who = -1;
        for (int i = 0 ; i < this.neighbours.size(); i++) {
            double d = coor2.dist_to_switch_fixdim(((Switch) neighbours.get(i)).getCoor(),pdu.routingSpace);
            if (d<dd) {
                who = i;
                dist= d;
            }
        }
        for (int i = 0 ; i < this.neighbours.size(); i++) {
            Switch neigh = (Switch) this.neighbours.get(i);
            // 	if (dd0 > calculation_dist(coor2,neigh.getCoor(),pdu))
            for (Node nn: neigh.neighbours) if (nn!=this) {
                    double d = coor2.dist_to_switch_fixdim(((Switch)nn).getCoor(),pdu.routingSpace);
                    if (d<dd) {
                        who = i;
                        dd= d;
                    }
                }
        }


        if (who >=0) return ports.get(hosts.size()+who);
        return getHostLink(coor2);

    }
    protected Link getLink_OCT30(PDU pdu) {

        Coordinate coor2 = ((TCPCoorMessage) pdu).getdestCoor();
        if (this.routing_neighbour_hop ==0) return null;
        double dist = calculation_dist(this.coor,coor2,pdu);
        double dist0 = dist;
        int who = -1;
        for (int i = 0 ; i < this.neighbours.size(); i++) {
            double d = calculation_dist(coor2,((Switch)neighbours.get(i)).getCoor(),pdu);
            if (d<dist) {
                who = i;
                dist= d;
            }
        }
        if (this.routing_neighbour_hop ==1) {
            if (who >= 0) return ports.get(who+this.hosts.size());
        } else {
            for (int i = 0 ; i < this.neighbours.size(); i++) {
                Switch neigh = (Switch) this.neighbours.get(i);
                if (dist0 > calculation_dist(coor2,neigh.getCoor(),pdu))
                    for (Node nn: neigh.neighbours) if (nn!=this) {
                            double d = calculation_dist(coor2,((Switch)nn).getCoor(),pdu);
                            if (d<dist) {
                                who = i;
                                dist= d;
                            }
                        }
            }

        }
        if (dist < 1e-8) dist=dist0/2;
        if (who >=0) {
            /*
            Vector<Link> possible = new Vector<Link>();
            for (int i = 0 ; i < this.neighbours.size(); i++) {
             	double d = calculation_dist(coor2,neighbours.get(i).getCoor(),pdu);
               	if (d<dist*1.00005 && d <dist0) possible.add(ports.get(hosts.size()+i));
            }
            if (this.routing_neighbour_hop ==2) {
                for (int i = 0 ; i < this.neighbours.size();i++) {
            		Switch neigh = (Switch) this.neighbours.get(i);
            		//if (dist0 > calculation_dist(coor2,neigh.getCoor(),pdu))
            		for (Node nn: neigh.neighbours) if (nn!=this){
            			double d = calculation_dist(coor2,nn.getCoor(),pdu);
            	       	if (d<dist*1.0000005 && d < dist0) {possible.add(ports.get(hosts.size()+i)); break;}
            		}
            	}


            }*/
            return ports.get(hosts.size()+who);
            //return possible.get(Simusys.rand.nextInt(possible.size()));

        }
        //if (who >= 0) return ports.get(who+this.hosts.size());
        //route to this switch.
        return getHostLink(coor2);

    }

    private Link getHostLink(Coordinate coor2) {
        int who = -1;
        double dist = Coordinate.max_coordinate;
        for (int i = 0; i< this.hosts.size(); i++) {
            double d = coor2.dist_to_Host(((Host)this.hosts.get(i)).getCoor());
            if (d<dist) {
                who = i;
                dist = d;
            }
        }
        return ports.get(who);
    }


    public String getName() {
        return "S"+id;
    }

    public Coordinate getCoor() {
        return coor;
    }
    public void setCoordinate(Coordinate c) {
        this.coor = c;
    }
    public int getNeighbourCount() {
        return neighbours.size();
    }
    public Vector<Node> getNeighbour() {
        return neighbours;
    }
}
