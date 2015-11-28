package Support;
import Support.NetworkEvent;

public interface Entity {

    public String getName();

    public String getState();



    public boolean performEvent(NetworkEvent event);

    public boolean performEventsAt(long tick);

    public boolean performPendingEventsAt(long tick);

    public void addEvent(NetworkEvent e);

}