package SimpleBed;

import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.Vector;

import MSG.Host;
import Support.Entity;
import Support.EventManager;
import Support.Simusys;
import TCP.TCPCoorBony;

public class ShuffleBed {
    private Vector<Host> hosts;
    private Vector<int[]> permu;
    int H;
    int subflow;
    int nowflowid[];
    TCPCoorBony[][] nowtcpc;
    int flowsizeKB;
    boolean[] allfinished;
    public ShuffleBed(Host h[],int _flowsizeKB,int _subflowcnt) {
        H = h.length;
        subflow= _subflowcnt;
        hosts = new Vector<Host>();
        for (int i = 0 ; i < h.length; i++)
            hosts.add(h[i]);
        permu = new Vector<int[]> ();
        for (int i = 0 ; i < h.length; i++)
            permu.add(permuWithoutMe(i));
        nowflowid = new int[H];
        flowsizeKB = _flowsizeKB;
        nowtcpc = new TCPCoorBony[H][subflow];
        allfinished = new boolean[H];
    }
    private boolean startNxtFlowAt(int i) {
        nowflowid[i]+=1;
        if (nowflowid[i]>=H-1)
            return false;
        Host here = hosts.get(i);
        Host remote = hosts.get(permu.get(i)[nowflowid[i]]);
        for (int k = 0 ; k < subflow; k++) {
            nowtcpc[i][k] = new TCPCoorBony(here,(short) (i*H*subflow+nowflowid[i]*subflow+k),flowsizeKB);
            TCPCoorBony RmTCP = new TCPCoorBony(remote,(short) (i*H+nowflowid[i]+k));

            nowtcpc[i][k].setPeer(RmTCP, true);
            RmTCP.setPeer(nowtcpc[i][k], false);
            nowtcpc[i][k].delaystart = Simusys.rand.nextInt(1000);
            nowtcpc[i][k].send();
        }
        //System.out.println("Flow at "+i+" started::"+nowflowid[i]);

        return true;
    }
    private int[] permuWithoutMe(int me) {
        int [] permu = new int[H-1];
        int loc = -1;
        for (int i = 1 ; i < H; i++) {
            {   loc++;
                permu[loc] = (me+i)%H;
            }
        }
        for (int i = 1 ; i < H-1; i++) {
            int rand = Simusys.rand.nextInt(i+1);
            int t = permu[rand];
            permu[rand]=permu[i];
            permu[i] = t;
        }
        return permu;
    }
    public void run(boolean loadaware) {


        Simusys.reset();
        Simusys.setLoadAware(loadaware);
        for (int i = 0 ; i < H; i++) {
            nowflowid[i]=-1;
            startNxtFlowAt(i);
            allfinished[i] = false;
        }




        double lastamt = 0;
        DecimalFormat df5  = new DecimalFormat("#0.00000");
        DecimalFormat df3  = new DecimalFormat("#0.000");

        double[] prevamt = new double [H];
        int finished = 0;
        while (true) {
            Vector<Entity> entities = EventManager.getEntities();

            for (Enumeration<Entity> enums = entities.elements(); enums.hasMoreElements(); ) {
                Entity e = enums.nextElement();
                e.performPendingEventsAt(Simusys.time());
            }

            for (Enumeration<Entity> enums = entities.elements(); enums.hasMoreElements(); ) {
                Entity e = enums.nextElement();
                e.performEventsAt(Simusys.time());
            }
            for (int i = 0 ; i < H; i++)
                if (finished(nowtcpc[i]) && (allfinished[i]==false)) {
                    if (allfinished[i]) continue;
                    prevamt[i] = 0;
                    if (!startNxtFlowAt(i)) {
                        finished++;
                        //System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!! completed"+i+allfinished[i]);
                        allfinished[i] = true;
                        //	System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!! completed"+i+allfinished[i]);
                    }
                }
            if (finished == H) {
                System.out.println(Simusys.time()*(1.0/Simusys.ticePerSecond()));
                return ;
            }

            if (Simusys.time() % (Simusys.ticePerSecond() / 10) == 0) {

                double rate = 0, amount = 0;
                int finishcnt = 0;
                for (int i = 0; i < H; i++) {
                    amount += getAmount(nowtcpc[i]);
                    if (rate <0) rate = 0;
                    if (amount <0) amount  =0;
                }

                System.out.print(df3.format(Simusys.time()*(1.0/Simusys.ticePerSecond()))

                                 +",\t"+finishcnt
                                 +",\t"+df5.format((amount-lastamt)/1000/H)+",.,");
                for (int i = 0 ; i < H; i++) {
                    if (finished(nowtcpc[i])) {
                        System.out.print(",f");
                        continue;
                    }
                    double top = getAmount(nowtcpc[i]) - prevamt[i];
                    if (top<0) top = 0;
                    System.out.print(","+df5.format(top/1000));
                    prevamt[i] = getAmount(nowtcpc[i]);
                }
                lastamt=amount;
                System.out.println();
            }

            Simusys.iterate();
        }


    }
    private boolean finished(TCPCoorBony[] tcpBonies) {
        for (TCPCoorBony s : tcpBonies)
            if (!s.finished()) return false;
        return true;
    }
    private double getAmount(TCPCoorBony[] tcpBonies) {
        double ans = 0.0;
        for (TCPCoorBony s : tcpBonies) {
            ans += s.getAmount();
        }
        return 0;
    }
}

