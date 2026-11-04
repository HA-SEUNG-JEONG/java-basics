import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Cache<K, V> {
    private final Map<K, V> map = new HashMap<>();

    void put(K key, V value) {
        map.put(key, value);
    }

    Optional<V> get(K key) {
        return Optional.ofNullable(map.get(key));
    }

    int size() {
        return map.size();
    }
}
