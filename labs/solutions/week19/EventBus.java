import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class EventBus<E> {
    private final List<Consumer<E>> listeners = new ArrayList<>();

    public void subscribe(Consumer<E> listener) {
        listeners.add(listener);
    }

    public void publish(E event) {
        for (Consumer<E> l : listeners) {
            try {
                l.accept(event);
            } catch (RuntimeException e) {
                // 한 구독자의 실패가 다른 구독자를 막지 않는다
            }
        }
    }
}
