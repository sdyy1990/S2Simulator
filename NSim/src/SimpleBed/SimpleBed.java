package SimpleBed;

import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.Vector;

import Support.Entity;
import Support.EventManager;
import Support.Simusys;
import TCP.TCPBony;

public class SimpleBed {
    public void bedrun(long end, TCPBony[] tcpc,int hosts,boolean loadaware) {

        Simusys.setLoadAware(loadaware);
        int flows = tcpc.length;

        for (int i = 0; i < flows; i++)
            tcpc[i].delaystart = Simusys.rand.nextInt(1000);
        for (int i = 0; i < flows; i++)
            tcpc[i].send();

        double lastamt = 0;
        DecimalFormat df5  = new DecimalFormat("#0.00000");
        DecimalFormat df3  = new DecimalFormat("#0.000");
        //for (int i = 0; i < flows; i++)
        //System.out.print(tcpc[i].getParent().id + "->" + tcpc[i].getPeer().getParent().id+",\t");
        System.out.println();
        double[] prevamt = new double [flows];
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
            if (Simusys.time() % (Simusys.ticePerSecond() / 1000) == 0) {
                double rate = 0, amount = 0;
                int finishcnt = 0;

                for (int i = 0; i < flows; i++) {
                    rate += tcpc[i].getRate();
                    amount += tcpc[i].getAmount();
                    if (tcpc[i].finished()) finishcnt++;
                    if (rate <0) rate = 0;
                    if (amount <0) amount  =0;
                }
                System.out.print(df3.format(Simusys.time()*(1.0/Simusys.ticePerSecond()))
                                 +",\t"+finishcnt
                                 +",\t"+df5.format((amount-lastamt)/1000/hosts)+",.,");
                if (false)
                    for (int i = 0 ; i < flows; i++) {
                        if (tcpc[i].finished()) {
                            System.out.print(",f");
                            continue;
                        }
                        double top = tcpc[i].getAmount() - prevamt[i];
                        if (top<0) top = 0;
                        System.out.print(","+df5.format(top/1000));
                        prevamt[i] = tcpc[i].getAmount();
                    }
                lastamt=amount;

                System.out.println();

                /*
                System.out.println(df5.format(rate) + "," + df5.format(Simusys.time()*(1.0/Simusys.ticePerSecond()))+
                		"," + df5.format(amount) +","+df5.format(rate/flows*flowperLink) +
                		","+finishcnt+","+
                		(amount-lastamt)/1000/hosts);

                */
                if (finishcnt==flows) break;
            }

            Simusys.iterate();
        }

        System.out.println(Simusys.time()*(1.0/Simusys.ticePerSecond()));


    }
}
