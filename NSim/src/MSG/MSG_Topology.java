package MSG;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Vector;

import Support.Coor1D;
import Support.Coordinate;
import Support.Simusys;

public class MSG_Topology {
    private int nodecount;
    //private int hostcount;
    public Switch[] nodes;
    public Host[] hosts;
    //public Link[][] switchlinks;
    public int CoorType;
    public int dimentions;
    public Vector<Link> links;
    public MSG_Topology (int port ,int host, File file,boolean flex_space, int route_hop) {
        try {

            links = new Vector<Link>();
            BufferedReader bf = new BufferedReader(new FileReader(file));
            String line =null;
            //this.hostcount = host;
            this.hosts = new Host[host];
            //this.switchlinks = new Link[host][];
            int st[] = null,ed[] = null;
            int edgecnt = 0;
            while ((line =  bf.readLine())!=null) {
                //	System.out.println(line);
                String[] arrl = line.split(" ");
                if (arrl[0].equals("N")) {
                    this.nodecount = Integer.parseInt(arrl[1]);
                    this.nodes = new Switch[this.nodecount];
                    st = new int[nodecount*port];
                    ed = new int[nodecount*port];
                }
                if (arrl[0].equals("D")) {
                    int coord = Integer.parseInt(arrl[1]);
                    this.CoorType = coord;
                }
                if (arrl[0].equals("L")) {
                    this.dimentions = Integer.parseInt(arrl[1]);
                }
                if (arrl[0].equals("E")) {
                    st[edgecnt] = Integer.parseInt(arrl[1])-1;
                    ed[edgecnt] = Integer.parseInt(arrl[2])-1;
                    edgecnt++;
                }
                if (arrl[0].equals("C")) {
                    int id = Integer.parseInt(arrl[1]);
                    int[] coor = new int[this.dimentions];
                    for (int i = 0 ; i < this.dimentions; i++)
                        coor[i] = Integer.parseInt(arrl[2+i]);
                    this.nodes[id] = new Switch(new Support.Coor1D(coor),port,id,route_hop,flex_space);
                }
                if (arrl[0].equals("H")) {
                    int id = Integer.parseInt(arrl[1])-1;
                    int homeswitch = Integer.parseInt(arrl[2])-1;
                    double hcoor = Double.parseDouble(arrl[3]);
                    this.hosts[id] = new Host(new Coor1D((Coor1D) this.nodes[homeswitch].coor,hcoor),id);
                    Link l = new Link(this.hosts[id],this.nodes[homeswitch]);

                    //	System.out.println(id+" "+homeswitch);
                    this.nodes[homeswitch].addHost(l, this.hosts[id]);
                    this.hosts[id].setEdgeLink(l);
                }

            }
            for (int i = 0 ; i < edgecnt; i++) {
                Link l = new Link(this.nodes[st[i]],this.nodes[ed[i]]);
                this.nodes[st[i]].addNeighbour(l, this.nodes[ed[i]]);
                this.nodes[ed[i]].addNeighbour(l, this.nodes[st[i]]);
                links.add(l);
            }

            //assign switch nodes
            //assign hosts
            //assign switch-switch links
            //assign host - switch -links

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    public void printLinks_old() {
        double [] pardists = new double [links.size()];
        int [] id = new int [links.size()];
        for (int i = 0 ; i< links.size(); i++) {
            pardists[i] = poscor(((Host)links.get(i).getUpNode()).getCoor() ,((Host)links.get(i).getDownNode()).getCoor());
            id[i] = i;
        }
        for(int i = 0 ; i < links.size(); i++)
            for (int j = i+1; j< links.size(); j++)
                if (pardists[id[i]] > pardists[id[j]]) {
                    int tti = id[i];
                    id[i] = id[j];
                    id[j]=tti;
                }

        for (int i = 0; i < links.size(); i++) {
            Link l = links.get(id[i]);
            System.out.println(i+","+l.eventcount+","+pardists[id[i]]);

        }
    }
    public void printLinks_old2() {
        for (int i = 0; i < links.size(); i++) {
            Link l = links.get(i);
            Coor1D x1 = (Coor1D) ((Host)links.get(i).getUpNode()).getCoor();
            Coor1D x2 = (Coor1D) ((Host)links.get(i).getDownNode()).getCoor();

            double dd[] = new double [x1.switchcoor.length];
            System.out.print(i+","+l.eventcount+",");
            for (int j = 0 ; j < x1.switchcoor.length; j++) {
                dd[j] = (Coor1D.circulardist(x1.switchcoor[j] , x2.switchcoor[j]));
                System.out.print(dd[j]+",");
            }
            System.out.println();



        }
    }
    public void printLinks() {
        for (Switch s : nodes) {
            int nc = s.getNeighbourCount();
            Vector<Node> vn = s.getNeighbour();
            double mm = 100.0;
            for (int dim = 0 ; dim < this.dimentions; dim++) {
                double[] cl = new double[nc];
                for (int i = 0 ; i < nc; i++)
                    cl[i] =((Coor1D) ((Host)vn.get(i)).getCoor()).switchcoor[dim];
                double nowmin = maxDistBetween(cl);
                if (nowmin < mm) mm = nowmin;
            }


        }
    }
    private double maxDistBetween(double [] cl) {
        java.util.Arrays.sort(cl);
        double ans = Coor1D.circulardist(cl[0],cl[this.dimentions-1]);
        for (int i = 0 ; i < this.dimentions - 1; i++) {
            double t = Coor1D.circulardist(cl[i], cl[i+1]);
            if (t>ans) ans = t;
        }
        return ans;

    }
    private double poscor(Coordinate coor, Coordinate coor2) {
        Coor1D s1 = (Coor1D) coor;
        Coor1D s2 = (Coor1D) coor2;
        double dd[] = new double [s1.switchcoor.length];
        double ans = 0;
        for (int i = 0 ; i < s1.switchcoor.length; i++)
            dd[i] = (Coor1D.circulardist(s1.switchcoor[i] , s2.switchcoor[i]));
        return sum(dd);
        //return ans;
    }
    private double var(double [] a) {
        double ss1 = 0;
        double ss2 = 0;
        int n = a.length;
        for (int i = 0 ; i < n ; i++) {
            ss1 += a[i]*a[i];
            ss2 += a[i];
        }
        return ss1 - (ss2)*(ss2)/n;


    }

    private double sum(double [] a) {
        double ss1 = 0;
        double ss2 = 0;
        int n = a.length;
        for (int i = 0 ; i < n ; i++) {
            ss2 += a[i];
        }
        return ss2;


    }

}
