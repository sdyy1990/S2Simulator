package Jellyfish;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Vector;

import MSG.Host;
import MSG.Link;
import MSG.Switch;
import Support.Coor1D;

public class Jellyfish_Topo {

    private int nodecount;
    public Jellyfish_Switch[] nodes;
    public Host[] hosts;
    public int CoorType;
    public int dimentions;
    public Vector<Link> links;
    public Jellyfish_Topo (int port ,int host, String Topofilename, String hopFilename) {
        try {
            System.out.println(Topofilename);
            File file = new File(Topofilename);
            if (!file.exists()) {
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                URL url = classLoader.getResource(Topofilename);
                file = new File(url.getPath());
            }

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
                    this.nodes = new Jellyfish_Switch[this.nodecount];
                    st = new int[nodecount*port];
                    ed = new int[nodecount*port];
                    for (int i = 0 ; i < nodecount; i++) {
                        this.nodes[i] = new Jellyfish_Switch(port,i,nodecount,hosts);
                    }
                }
                if (arrl[0].equals("L")) {
                    this.dimentions = Integer.parseInt(arrl[1]);
                }
                if (arrl[0].equals("E")) {
                    st[edgecnt] = Integer.parseInt(arrl[1])-1;
                    ed[edgecnt] = Integer.parseInt(arrl[2])-1;
                    edgecnt++;
                }
                if (arrl[0].equals("h") || arrl[0].equals("H")) {
                    int id = Integer.parseInt(arrl[1])-1;
                    int homeswitch = Integer.parseInt(arrl[2])-1;
                    if (id >= host) continue;
                    this.hosts[id] = new Host(null,id);
                    this.hosts[id].setRelatedSwitch(this.nodes[homeswitch]);
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
            System.out.println(hopFilename);

            File file2 = new File(hopFilename);
            if (!file2.exists()) {
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                URL url = classLoader.getResource(Topofilename);
                file2 = new File(url.getPath());
            }
            BufferedReader bf2 = new BufferedReader(new FileReader(file2));
            while (true) {
                String k = bf2.readLine();
                if (k==null) break;
                String[] arrl = k.split(" ");
                //System.out.println(k);
                //System.out.println(arrl);
                if (arrl.length<=2) break;
                int x, y, z,w,kwww;
                x= Integer.parseInt(arrl[0]) - 1;
                y= Integer.parseInt(arrl[1]) - 1;
                z= Integer.parseInt(arrl[2]) - 1;
                w= Integer.parseInt(arrl[3]) - 1;
                kwww = Integer.parseInt(arrl[4]);
                this.nodes[x].setDestWithNode(this.nodes[y],this.nodes[z],this.nodes[w],kwww);
            }


        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
