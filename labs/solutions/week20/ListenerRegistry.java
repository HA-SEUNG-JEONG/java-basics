import java.util.ArrayList;
import java.util.List;

public class ListenerRegistry {
    private final List<Runnable> listeners = new ArrayList<>();

    public void register(Runnable listener) {
        listeners.add(listener);
    }

    public void unregister(Runnable listener) {
        listeners.remove(listener);
    }

    public int size() {
        return listeners.size();
    }

    public void fireAll() {
        for (Runnable r : List.copyOf(listeners)) r.run();
    }
}
