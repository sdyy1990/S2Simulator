package SmallWorld;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import Jellyfish.Jellyfish_Switch;
import MSG.Host;
import MSG.Link;
import MSG.Node;
import MSG.Switch;
import Support.Simusys;
import Support.SmallWorldCoor;

public class SmallWorldTopo {

    private int nodecount;
    public SmallWorldSwitch[] nodes;
    public Host[] hosts;
    public int CoorType;
    public int dimentions;
    public Vector<Link> links;

    public SmallWorldTopo(int dim, int width, int height, int depth,int port,int hostnumber) {
        double nearlimit = 0.0;
        if (dim<=2) depth =1;
        if (dim<=1) height = 1;
        nodecount = width * height * depth;
        nodes = new SmallWorldSwitch[nodecount];
        links = new Vector<Link>();

        int [] hostperswitch = new int[nodecount];
        for (int i = 0 ; i < hostnumber; i++)
            hostperswitch[i%nodecount]+=1;
        java.util.Random rand = new java.util.Random();
        for (int i = 0 ; i < hostnumber *3; i++) {
            int a = i%nodecount;
            int b = rand.nextInt(nodecount);
            int t = hostperswitch[a];
            hostperswitch[a] = hostperswitch[b];
            hostperswitch[b] = t;
        }
        int sts = 0;
        if (dim==3) {

            for (int x = 0 ; x< width; x ++)
                for (int  y = 0 ; y < height; y ++)
                    for (int z = 0 ; z< depth ; z ++) {
                        SmallWorldCoor hereC = new  SmallWorldCoor(x, y, z, width, height, depth);
                        int id = x *  (height * depth) + y * (depth) + z;
                        nodes[id] = new SmallWorldSwitch(hereC,port, id, hostperswitch[id],sts);
                        sts+=hostperswitch[id];
                    }

            nearlimit = 4.1;

        }
        if (dim==2) {
            for (int x = 0 ; x< width; x ++)
                for (int  y = 0 ; y < height; y ++) {
                    SmallWorldCoor hereC = new  SmallWorldCoor(x, y, width, height);
                    int id = x *  (height ) + y ;
                    nodes[id] = new SmallWorldSwitch(hereC,port, id, hostperswitch[id],sts);
                    sts+=hostperswitch[id];
                }
            nearlimit = 1.1;
        }
        if (dim == 1 ) {
            for (int x = 0 ; x< width; x ++) {
                SmallWorldCoor hereC = new  SmallWorldCoor(x, width);
                int id = x ;
                nodes[id] = new SmallWorldSwitch(hereC,port, id, hostperswitch[id],sts);
                sts+=hostperswitch[id];
            }
            nearlimit = 1.1;
        }
        hosts = new Host[hostnumber];
        int ii = 0;
        for (int i = 0 ; i < nodecount; i++)
            for (int j = 0 ; j < nodes[i].hosts.size(); j++)
                hosts[ii++] = (Host)nodes[i].hosts.get(j);
        for (int i = 0 ; i < nodecount; i++)
            for (int j = i+1; j < nodecount; j++)
                if (nodes[i].getCoor().dist_to_switch(nodes[j].getCoor()) < nearlimit) {
                    Link l = new Link(nodes[i],nodes[j]);
                    nodes[i].addNeighbour(l, nodes[j]);
                    nodes[j].addNeighbour(l, nodes[i]);
                    links.add(l);
                    //	System.out.println(i+" "+j);
                }

        while (true) {
            Vector<SmallWorldSwitch> unfull = new Vector<SmallWorldSwitch> ();
            for (int i = 0 ; i < nodecount; i++)
                if (nodes[i].neighbours.size()+nodes[i].hosts.size()<port)
                    unfull.add(nodes[i]);
            if (unfull.size()<2) break;
            Collections.shuffle(unfull);
            for (int i = 0 ; i < unfull.size()-1; i+=2) {
                Link l = new Link(unfull.get(i),unfull.get(i+1));
                unfull.get(i+1).addNeighbour(l, unfull.get(i));
                unfull.get(i).addNeighbour(l, unfull.get(i+1));
                links.add(l);
            }
        }
        /*
        int [] permu = Simusys.getpermu(nodecount, nodecount);
        for (int i = 0 ; i < permu.length-1; i+=2){
        	//System.out.println(permu[i]+" "+ permu[i+1]);
        	Link l = new Link(nodes[permu[i]],nodes[permu[i+1]]);
        	nodes[permu[i]].addNeighbour(l, nodes[permu[i+1]]);
        	nodes[permu[i+1]].addNeighbour(l, nodes[permu[i]]);
        	//System.out.println(i+" "+j);
        }*/
        for (SmallWorldSwitch node:nodes) {
            System.out.println(node.neighbours.size()+" " +node.getCoor());
        }

    }

}
