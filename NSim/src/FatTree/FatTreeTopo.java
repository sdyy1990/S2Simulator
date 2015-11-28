package FatTree;

import java.util.Vector;

import MSG.Host;
import MSG.Link;
import SimpleBed.SimpleHost;
public class FatTreeTopo {
	public int k;
    public FatTreeSwitch[] nodes;
    public SimpleHost[] hosts;
    public int hostcount;
    public int switchcount;

    public Vector<Link> links;
    public FatTreeTopo(int k) {
    	hostcount = k*k*k/4;
    	switchcount= k*k*5/4;
    	nodes = new FatTreeSwitch[switchcount];
    	hosts = new SimpleHost[hostcount];
        links = new Vector<Link>();

    	for (int i = 0 ; i < switchcount; i++)
    		nodes[i] = new FatTreeSwitch(k,i);
    	for (int i = 0 ; i < hostcount; i++)
    		hosts[i] = new SimpleHost(i);
    	for (int i = k*k; i<k*k*5/4; i++)
    		for (int j = 0 ; j < k; j++)
    			addlink(i,k*k/2+j*(k/2)+i%(k/2));
    	for (int slot = 0; slot<k; slot++)
    		for (int i = 0 ; i < k/2; i++)
    			for (int j = 0 ; j < k/2; j++)
    				addlink(k*k/2+slot*(k/2)+i,slot*(k/2)+j);
    	for (int slot = 0 ; slot<k; slot++)
    		for (int i = 0; i < k/2; i++)
    			for (int j = 0 ; j < k/2; j++)
    				addhostlink(slot*(k/2)+i,(slot*(k/2)+i)*(k/2)+j);
    	
    }
	private void addhostlink(int i, int j) {
		Link l = new Link(nodes[i],hosts[j]);
        nodes[i].addHost(l,hosts[j]);
        links.add(l);
	}
	private void addlink(int i, int j) {
		// TODO Auto-generated method stub
		Link l = new Link(nodes[i],nodes[j]);
        nodes[i].addNeighbour(l, nodes[j]);
        nodes[j].addNeighbour(l, nodes[i]);
        links.add(l);
        
	}
}
