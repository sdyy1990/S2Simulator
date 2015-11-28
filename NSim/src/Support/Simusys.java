package Support;

import java.util.Random;
import java.util.Vector;

import MSG.Link;

public class Simusys {
    private static long tick = 0; // in 0.01 ms
    private static long overflow = 0;
    public static void iterate() {
        if (tick == Long.MAX_VALUE)
            overflow++;
        tick++;
    }
    public static long time() {
        return tick;
    }

    public static long overflow() {
        return overflow;
    }

    public static void reset() {
        tick = 0;
        overflow = 0;
    }

    public static long ticePerSecond() {
        return 100000;
    }
    public static  Random rand = new Random();

    public static int[] getpermu(int hosts, int flows) {
        int maxx = (hosts<flows)?flows:hosts;
        int [] pp = new int[maxx];
        int [] ans = new int[flows+1];
        for (int i = 0 ; i < maxx; i++) pp[i] = i%hosts;
        for (int i = 1; i<hosts; i++) {
            int j = rand.nextInt(i+1);
            if (j!=i) {
                int t = pp[i];
                pp[i] = pp[j];
                pp[j] = t;
            }
        }
        for (int i = 0 ; i < flows; i++) ans [i] = pp[i];
        ans[flows] = ans[0];
        return ans;
    }
    public static boolean loadAwareIsOn() {
        return _loadAware;
    }
    private static boolean _loadAware = true;
    public static void setLoadAware(boolean b) {
        _loadAware = b;
    }
    public static Vector<Link> a;
    public static void tryReleaseFlow(int id2, int id3, int flowid) {
        for(Link w:a) {
            w.tryReleaseFlow(id2, id3, flowid);
        }

    }
    public static void setLink(Vector<Link> links) {
        a = links;
    }


}
