package MSG;
import java.io.File;
import java.net.URL;

import SimpleBed.ShuffleBed;
import Support.Simusys;

public class MSGShuffleBed {
    public static  void main(String args[]) {
        File file = null;

        if (args.length <5) {
            System.out.println("Jellyfish Simulator");
            System.out.println("arg0 : port number ");
            System.out.println("arg1 : host number ");
            System.out.println("arg2 : Topo File");
            System.out.println("arg3 : flow per link");
            System.out.println("arg4 : flow size in KB");
            System.out.println("arg5 : loadaware >0 is true");
        }

        file = new File("//home//yy//Shared//TopoFiles//"+args[2]);
        if (!file.exists()) {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            URL url = classLoader.getResource(args[2]);
            file = new File(url.getPath());
        }
        boolean flex = true;
        flex = true;
        MSG_Topology topo = new MSG_Topology(Integer.parseInt(args[0]), Integer.parseInt(args[1]),file,flex,2);
        Host[] hosts = topo.hosts;
        ShuffleBed shufflebed = new ShuffleBed(hosts,Integer.parseInt(args[4]),Integer.parseInt(args[3]));
        Simusys.setLink(topo.links);
        shufflebed.run(Integer.parseInt(args[5])>0);
    }
}
