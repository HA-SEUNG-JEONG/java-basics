/**
 * 20주차 실습 1. 가장 오래 쓰이지 않은 항목부터 버리는 크기 제한 캐시.
 *  - 항목 수가 capacity 를 넘으면 "가장 오래 전에 사용된" 항목 하나를 지운다.
 *  - put 과 get 모두 "사용" 으로 친다.
 *  - 힌트: java.util.LinkedHashMap(초기용량, 0.75f, true) 의 accessOrder 와 removeEldestEntry
 *    (상속해도 되고, 내부에 필드로 들고 있어도 됩니다)
 */
public class LruCache<K, V> {
    public LruCache(int capacity) {
        // 여기를 고치세요
    }

    public V get(K key) {
        // 여기를 고치세요
        throw new UnsupportedOperationException("get 을 구현하세요");
    }

    public void put(K key, V value) {
        // 여기를 고치세요
        throw new UnsupportedOperationException("put 을 구현하세요");
    }

    public boolean containsKey(K key) {
        // 여기를 고치세요 (containsKey 가 "사용" 으로 치지는 않아도 됩니다)
        throw new UnsupportedOperationException("containsKey 를 구현하세요");
    }

    public int size() {
        // 여기를 고치세요
        throw new UnsupportedOperationException("size 를 구현하세요");
    }
}
