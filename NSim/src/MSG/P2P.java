package MSG;

import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.Vector;

import Support.Entity;
import Support.EventManager;
import Support.Simusys;
import TCP.TCPBony;

public class P2P {
    public static double NIC = 814.28;

    public static  void main(String args[]) {
        Host h1 = new Host(null,0);
        Host h2 = new Host(null,1);
        Link l = new Link(h1,h2);
        h1.setEdgeLink(l);
        h2.setEdgeLink(l);
        TCPBony tcpc = new TCPBony(h1,(short) 3);
        TCPBony tcps = new TCPBony(h2,(short) 3);

        tcps.setPeer(tcpc, false);
        tcpc.setPeer(tcps, true);
        Simusys.reset();

        tcpc.send();
        long end = 1 * 300 * Simusys.ticePerSecond();

        while (Simusys.time() <= end) {
            Vector<Entity> entities = EventManager.getEntities();

            for (Enumeration<Entity> enums = entities.elements(); enums.hasMoreElements(); ) {
                Entity e = enums.nextElement();
                e.performPendingEventsAt(Simusys.time());
            }

            for (Enumeration<Entity> enums = entities.elements(); enums.hasMoreElements(); ) {
                Entity e = enums.nextElement();
                e.performEventsAt(Simusys.time());
            }

            if (Simusys.time() % (Simusys.ticePerSecond()) == 0) {
                double rate = 0, amount = 0;
                DecimalFormat df5  = new DecimalFormat("##.00000");
                rate += tcpc.getRate();
                amount += tcpc.getAmount();
                // System.out.println(rate);
                System.out.println(df5.format(rate) + "\t" + Simusys.time());
                // System.out.println();
            }

            Simusys.iterate();
        }
    }

}
