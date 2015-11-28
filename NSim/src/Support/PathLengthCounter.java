package Support;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import MSG.Host;

public class PathLengthCounter {
    private Map<String,Vector<Integer> > mp;
    private Host[] hosts;
    public PathLengthCounter(Host[] hostsss) {
        mp = new HashMap<String,Vector<Integer> >();
        hosts = hostsss;
    }

    public void set(Host H1, Host H2, int x) {
        String key = H1.getName() + "."+H2.getName();
        if (!mp.containsKey(key))
            mp.put(key, new Vector<Integer>());
        mp.get(key).add(Integer.valueOf(x));
    }
    public Vector<Integer> get(Host H1, Host H2) {
        String key = H1.getName() + "."+H2.getName();
        return mp.get(key);
    }

    public void set(int getsrcid, int id, int getlinkcount) {
        set(hosts[getsrcid],hosts[id],getlinkcount);
    }


}
