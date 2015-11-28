package Support;

import java.util.Random;
import java.util.Vector;

public class EventManager {
    private static Vector<Entity> entities = new Vector<Entity>();

    public static void register(Entity entity) {
        entities.add(entity);
    }

    public static void unregister(Entity entity) {
        entities.remove(entity);
    }

    public static Vector<Entity> getEntities() {
        shuffle(entities);
        return entities;
    }

    public static <T> void shuffle(Vector<T> evector) {
        Random random = new Random(Simusys.time());

        shuffle(evector, random);
    }

    public static <T> void shuffle(Vector<T> evector, Random random) {
        int size = evector.size();
        while (size > 1) {
            int i = Math.abs(random.nextInt() % size);
            int j = size - 1;
            if (i != j) {
                T nei = evector.get(i);
                T nej = evector.get(j);
                evector.set(i, nej);
                evector.set(j, nei);
            }
            size--;
        }
    }


    public static void reset() {
        entities.clear();
    }
}
