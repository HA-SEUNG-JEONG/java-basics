import java.util.ArrayList;
import java.util.List;

/**
 * 20주차 실습 4. "등록만 하고 해제를 잊는" 메모리 누수의 전형.
 * register 는 이미 있습니다. 해제하는 unregister 를 만드세요. 같은 listener 를 두 번 unregister 해도 에러가 없어야 합니다.
 * (누수가 왜 생기는지는 문서의 설명을 보세요)
 */
public class ListenerRegistry {
    private final List<Runnable> listeners = new ArrayList<>();

    public void register(Runnable listener) {
        listeners.add(listener);
    }

    public void unregister(Runnable listener) {
        // 여기를 고치세요
        throw new UnsupportedOperationException("unregister 를 구현하세요");
    }

    public int size() {
        return listeners.size();
    }

    public void fireAll() {
        for (Runnable r : List.copyOf(listeners)) r.run();
    }
}
