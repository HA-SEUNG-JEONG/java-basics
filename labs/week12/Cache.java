import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

// 12주차 실습 2: 키-값 캐시. get 은 null 대신 Optional 을 돌려준다.
public class Cache<K, V> {
    private final Map<K, V> map = new HashMap<>();

    void put(K key, V value) {
        // 여기를 고치세요
    }

    Optional<V> get(K key) {
        return null; // 여기를 고치세요
    }

    int size() {
        return -1; // 여기를 고치세요
    }
}
