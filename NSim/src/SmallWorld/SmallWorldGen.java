package SmallWorld;

import MSG.Host;
import MSG.Link;

public class SmallWorldGen {
    public static  void main(String args[]) {

        if (args.length <5) {
            System.out.println("Smallworld Simulator");
            System.out.println("arg0 : dim ");
            System.out.println("arg1 : width ");
            System.out.println("arg2 : height ,can be 0");
            System.out.println("arg3 : depth, can be 0");
            System.out.println("arg4 : tot port number / switch");
            System.out.println("arg5 : tot hosts");
        }


        int Dim = Integer.parseInt(args[0]);
        if (Dim<=3) {
            int width=0,height=1,depth=1;
            width = Integer.parseInt(args[1]);
            if (Dim>=2)   height = Integer.parseInt(args[2]);
            if (Dim==3)   depth = Integer.parseInt(args[3]);

            SmallWorldTopo topo = new SmallWorldTopo(Dim,width, height, depth,Integer.parseInt(args[4]),Integer.parseInt(args[5]));
            int hosts = topo.hosts.length;
            System.out.println("N "+hosts);
            System.out.println("D "+Dim);
            for (Link s:topo.links) {
                String s1 = s.getUpNode().getName();
                String s2 = s.getDownNode().getName();
                if (!(s1.startsWith("S")&&s2.startsWith("S")))continue;
                System.out.println("E "+(Integer.parseInt(s1.substring(s1.indexOf("S")+1))+1)+" "+
                                   (Integer.parseInt(s2.substring(s2.indexOf("S")+1))+1)+" "+1);
            }

            for (SmallWorldSwitch s: topo.nodes) {
                System.out.println("C "+s.id+" "+s.coor.toString());
            }
            for (Host h : topo.hosts) {
                System.out.println("h "+(h.id+1)+" "+(h.getRelatedSwitch().id+1));
            }

        }
    }
}
