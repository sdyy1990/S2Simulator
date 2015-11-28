package SmallWorld;

import MSG.Host;
import SimpleBed.ShuffleBed;
import Support.Simusys;

public class SmallWorldBedShuffle {


    public static  void main(String args[]) {
        SmallWorldTopo topo = new SmallWorldTopo(2,5,5,1,7,25);
        Host[] hosts = topo.hosts;
        Simusys.setLink(topo.links);
        ShuffleBed shufflebed = new ShuffleBed(hosts,Integer.parseInt(args[0]),Integer.parseInt(args[1]));
        shufflebed.run(true);

    }
}
