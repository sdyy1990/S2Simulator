package Example;

import MSG.Link;
import SimpleBed.SimpleBed;
import SimpleBed.SimpleHost;
import Support.Simusys;
import TCP.TCPBony;

public class ExampleBed {

    public static  void main(String args[]) {

        int hosts = 2;
        int flows = 2;

        //Topology : H0 -------- S3 ---------- S4 ------- H1
        //                linkA      linkB         linkC
        SimpleHost H0 = new SimpleHost(0);
        SimpleHost H1 = new SimpleHost(3);
        //each switch has 2 ports.
        ExampleSwitch S3 = new ExampleSwitch(2,3); 
        ExampleSwitch S4 = new ExampleSwitch(2,4);
        
        
        TCPBony[] tcpclist = new TCPBony[flows];
        TCPBony[] tcpslist = new TCPBony[flows];
        for (int i = 0 ; i < flows; i++) {
        TCPBony tcpc = new TCPBony(H0,(short) 80,32768); //tcpc transmit 32786K data to tcps
        TCPBony tcps = new TCPBony(H1,(short) 321);

        tcpclist[i] = tcpc;
        tcpslist[i] = tcps;

        tcpc.setPeer(tcps, true); //set tcpc as Sender 
        tcps.setPeer(tcpc, false); //set tcps as receiver 

        }
        
        
        //Connect host to switches
        Link linkA = new Link(H0,S3);
        Link linkC = new Link(H1,S4);
        S3.addHost(linkA,H0);
        S4.addHost(linkC,H1);
        
        //Connect switches , 
        //NOTE: inter-switch links should be connected AFTER ALL HOSTS ARE CONECTED
        
        Link linkB = new Link(S3,S4);
        S3.addNeighbour(linkB,S4);
        S4.addNeighbour(linkB,S3);
        
        
        
        Simusys.reset();

        long end = 2000000;
        SimpleBed simplebed = new SimpleBed();
        simplebed.bedrun(end, tcpclist,hosts,false);
    }
}
