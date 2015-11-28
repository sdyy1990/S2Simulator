package SmallWorld;

import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.Vector;

import Jellyfish.JellyfishBed;
import Jellyfish.Jellyfish_Topo;
import SimpleBed.SimpleBed;
import Support.Entity;
import Support.EventManager;
import Support.Simusys;
import TCP.TCPBony;

public class SmallworldBed {

    public static  void main(String args[]) {

        if (args.length <5) {
            System.out.println("Smallworld Simulator");
            System.out.println("arg0 : dim ");
            System.out.println("arg1 : width ");
            System.out.println("arg2 : height ,can be 0");
            System.out.println("arg3 : depth, can be 0");
            System.out.println("arg4 : flow per link");
            System.out.println("arg5 : flow size in KB");
            System.out.println("arg6 : end time in e-5 s");
            System.out.println("arg7 : tot port number / switch");
            System.out.println("arg8 : tot hosts");
            System.out.println("arg9 : loadAware > 0");

        }

        int dim = Integer.parseInt(args[0]);
        int width = Integer.parseInt(args[1]);
        int height = Integer.parseInt(args[2]);
        int depth = Integer.parseInt(args[3]);
        SmallWorldTopo topo = new SmallWorldTopo(dim,width, height, depth,Integer.parseInt(args[7]),Integer.parseInt(args[8]));
        //SmallWorldTopo topo = new SmallWorldTopo(1,128,10,0);
        int hosts = topo.hosts.length;
        int flowperhost = Integer.parseInt(args[4]);
        int flows = (int) (topo.hosts.length) * flowperhost;
        TCPBony[] tcpc = new TCPBony[flows];
        TCPBony[] tcps = new TCPBony[flows];
        int permu[] = Simusys.getpermu(hosts,hosts);
        for (int i = 0 ; i < flows; i++) {
            tcpc[i] = new TCPBony(topo.hosts[permu[i % hosts]],(short)(i+1),Integer.parseInt(args[5]));
            tcps[i] = new TCPBony(topo.hosts[permu[(i+1) % hosts]],(short) (i+1));
        }

        for (int i = 0 ; i < flows; i++) {
            tcpc[i].setPeer(tcps[i], true);
            tcps[i].setPeer(tcpc[i], false);
        }

        Simusys.reset();
        Simusys.setLink(topo.links);
        long end = Integer.parseInt(args[6]);
        for (int i = 0; i < flows; i++)
            tcpc[i].send();



        SimpleBed simplebed = new SimpleBed();
        simplebed.bedrun(end, tcpc,hosts,Integer.parseInt(args[9])>0);



    }
}
