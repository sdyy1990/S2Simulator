package Jellyfish;

import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.Vector;

import MSG.MSG_Topology;
import SimpleBed.SimpleBed;
import Support.Entity;
import Support.EventManager;
import Support.Simusys;
import TCP.TCPBony;

public class JellyfishBed {

    public static  void main(String args[]) {
        if (args.length <5) {
            System.out.println("Jellyfish Simulator");
            System.out.println("arg0 : port number ");
            System.out.println("arg1 : host number ");
            System.out.println("arg2 : Topo File");
            System.out.println("arg3 : Next hop file");
            System.out.println("arg4 : flow per link");
            System.out.println("arg5 : flow size in KB");
            System.out.println("arg6 : end time in e-5 s");
        }
        Jellyfish_Topo topo = new Jellyfish_Topo(Integer.parseInt(args[0]),Integer.parseInt(args[1]),"//home//yy//Shared//TopoFiles//"+args[2],"//home//yy//Shared//TopoFiles//"+args[3]);

        int hosts = topo.hosts.length;
        int flowperLink = Integer.parseInt(args[4]);
        int flows = (int) (topo.hosts.length) * flowperLink;

        TCPBony[] tcpc = new TCPBony[flows];
        TCPBony[] tcps = new TCPBony[flows];
        int permu[] = Simusys.getpermu(hosts,hosts);
        //flowperLink  = 1;
        // permu[0] = 33; permu[1]=59;
        for (int i = 0 ; i < flows; i++) {
            tcpc[i] = new TCPBony(topo.hosts[permu[i % hosts]],(short)(i+1),Integer.parseInt(args[5]));
            tcps[i] = new TCPBony(topo.hosts[permu[(i+1) % hosts]],(short) (i+1));
        }


        for (int i = 0 ; i < flows; i++) {
            tcpc[i].setPeer(tcps[i], true);
            tcps[i].setPeer(tcpc[i], false);
        }
        
       

        Simusys.reset();

        long end = Integer.parseInt(args[6]);
        SimpleBed simplebed = new SimpleBed();
        simplebed.bedrun(end, tcpc,hosts,false);


    }
}
