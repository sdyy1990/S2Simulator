package FatTree;

import java.io.File;
import java.net.URL;

import MSG.MSG_Topology;
import SimpleBed.SimpleBed;
import Support.Simusys;
import TCP.TCPBony;
import TCP.TCPCoorBony;

public class FatTreeBed {
    public static  void main(String args[]) {
        File file = null;

        if (args.length <3) {
            System.out.println("Jellyfish Simulator");
            System.out.println("arg0 : port number ");
            System.out.println("arg1 : flow per link");
            System.out.println("arg2 : flow size in KB");
            System.out.println("arg3 : end time in e-5 s");
        }
        FatTreeTopo topo = new FatTreeTopo(Integer.parseInt(args[0]));
        int hosts = topo.hosts.length;
        int flowperLink = Integer.parseInt(args[1]);
        int subflowsize = Integer.parseInt(args[2]);
        int flows = (int) (topo.hosts.length) * flowperLink;
        TCPBony[] tcpc = new TCPBony[flows];
        TCPBony[] tcps = new TCPBony[flows];

        int permu[] = Simusys.getpermu(hosts,hosts);
        for (int i = 0 ; i < flows; i++) {
            //tcpc[i] = new TCPBony(topo.hosts[i%hosts],(short)(i+1),10000);
            //tcps[i] = new TCPBony(topo.hosts[i%hosts],(short) (i+1));

            tcpc[i] = new TCPBony(topo.hosts[permu[i % (hosts)]],(short)(i+1),subflowsize);
            tcps[i] = new TCPBony(topo.hosts[permu[(i+1) % (hosts)]],(short) (i+1));
        }

        for (int i = 0 ; i < flows; i++) {
            tcpc[i].setPeer(tcps[i], true);
            tcps[i].setPeer(tcpc[i], false);
            tcps[i].set_Coor_check();
            tcpc[i].set_Coor_check();
        }
        Simusys.setLink(topo.links);
        Simusys.reset();
        long end = Integer.parseInt(args[3]);

        SimpleBed simplebed = new SimpleBed();
        simplebed.bedrun(end, tcpc,hosts,true);
    }
}
