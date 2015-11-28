package TCP;

public class FlowId {
    public long src, dest;
    public int sport, dport;

    public FlowId(long src, long dest, int sport, int dport) {
        this.src = src;
        this.dest = dest;
        this.sport = sport;
        this.dport = dport;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;

        if (o instanceof FlowId) {
            FlowId fid = (FlowId) o;
            return src == fid.src && dest == fid.dest
                   && sport == fid.sport && dport == fid.dport;
        }

        return false;
    }

    @Override
    public int hashCode() {
        Long x = src + dest + sport + dport;
        return x.hashCode();
    }

    @Override
    public String toString() {
        return sport + "@" + src + " -> " + dport + "@" + dest;
    }

}
