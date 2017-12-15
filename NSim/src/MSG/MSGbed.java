package MSG;

import java.io.File;
import java.net.URL;
import SimpleBed.SimpleBed;
import Support.Simusys;
import TCP.TCPBony;
import TCP.TCPCoorBony;

public class MSGbed {
    public static  void main(String args[]) {
        File file = null;

        if (args.length <5) {
            System.out.println("SpaceShuffle Simulator");
            System.out.println("arg0 : port number ");
            System.out.println("arg1 : host number ");
            System.out.println("arg2 : Topo File");
            System.out.println("arg3 : flow per link");
            System.out.println("arg4 : flow size in KB");
            System.out.println("arg5 : end time in e-5 s");
            System.out.println("arg6 : loadAware is on");
        }

        file = new File(args[2]);
        if (!file.exists()) {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            URL url = classLoader.getResource(args[2]);
            file = new File(url.getPath());
        }
        boolean flex = true;
//		if (args.length>4)
//			flex = args[4].contains("F") || args[4].contains("f");
        flex = true;

        MSG_Topology topo = new MSG_Topology(Integer.parseInt(args[0]), Integer.parseInt(args[1]),file,flex,2);
        int hosts = topo.hosts.length;
        int flowperLink = Integer.parseInt(args[3]);
        int subflowsize = Integer.parseInt(args[4]);
        int flows = (int) (topo.hosts.length) * flowperLink;
        TCPBony[] tcpc = new TCPCoorBony[flows];
        TCPBony[] tcps = new TCPCoorBony[flows];

        int permu[] = Simusys.getpermu(hosts,hosts);
        for (int i = 0 ; i < flows; i++) {
            //tcpc[i] = new TCPBony(topo.hosts[i%hosts],(short)(i+1),10000);
            //tcps[i] = new TCPBony(topo.hosts[i%hosts],(short) (i+1));

            tcpc[i] = new TCPCoorBony(topo.hosts[permu[i % (hosts)]],(short)(i+1),subflowsize);
            tcps[i] = new TCPCoorBony(topo.hosts[permu[(i+1) % (hosts)]],(short) (i+1));
        }

        for (int i = 0 ; i < flows; i++) {
            tcpc[i].setPeer(tcps[i], true);
            tcps[i].setPeer(tcpc[i], false);
            tcps[i].set_Coor_check();
            tcpc[i].set_Coor_check();
        }
        Simusys.setLink(topo.links);
        Simusys.reset();
        long end = Integer.parseInt(args[5]);

        SimpleBed simplebed = new SimpleBed();
        simplebed.bedrun(end, tcpc,hosts,Integer.parseInt(args[6])>0);


    }
}
