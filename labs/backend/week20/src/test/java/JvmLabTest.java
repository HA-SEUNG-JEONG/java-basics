import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class JvmLabTest {
    // ---- LruCache ----
    @Test
    void lru_evictsLeastRecentlyUsed() {
        var cache = new LruCache<String, Integer>(2);
        cache.put("a", 1);
        cache.put("b", 2);
        cache.get("a");          // a 를 사용했으니 b 가 가장 오래된 항목
        cache.put("c", 3);
        assertTrue(cache.containsKey("a"));
        assertFalse(cache.containsKey("b"), "가장 오래 쓰이지 않은 b 가 사라져야 합니다");
        assertTrue(cache.containsKey("c"));
        assertEquals(2, cache.size());
    }

    @Test
    void lru_getMissingReturnsNull() {
        assertNull(new LruCache<String, Integer>(3).get("없음"));
    }

    @Test
    void lru_putSameKeyReplacesValueWithoutGrowing() {
        var cache = new LruCache<String, Integer>(2);
        cache.put("a", 1);
        cache.put("a", 10);
        assertEquals(10, cache.get("a"));
        assertEquals(1, cache.size());
    }

    @Test
    void lru_staysSmallUnderHeapLimit() {
        // 이 테스트의 JVM 힙은 64MB 입니다. 20KB 짜리를 5만 개(약 1GB) 넣어도 capacity 만큼만 남아야 통과합니다.
        // capacity 를 무시하면 OutOfMemoryError 로 테스트 JVM 이 죽고, Gradle 은
        // "Could not complete execution for Gradle Test Executor" 라는 낯선 메시지를 냅니다. 그게 메모리 부족입니다.
        var cache = new LruCache<Integer, byte[]>(10);
        for (int i = 0; i < 50_000; i++) cache.put(i, new byte[20_000]);
        assertEquals(10, cache.size());
        assertTrue(cache.containsKey(49_999));
        assertFalse(cache.containsKey(0));
    }

    // ---- Recursion ----
    @Test
    void recursive_versionBlowsTheStack() {
        assertEquals(55, Recursion.sumToRecursive(10));
        assertThrows(StackOverflowError.class, () -> Recursion.sumToRecursive(10_000_000));
    }

    @Test
    void sumTo_loopSurvivesBigN() {
        assertEquals(0, Recursion.sumTo(0));
        assertEquals(55, Recursion.sumTo(10));
        assertEquals(50_000_005_000_000L, Recursion.sumTo(10_000_000));
    }

    @Test
    void fib_iterative() {
        assertEquals(0, Recursion.fib(0));
        assertEquals(1, Recursion.fib(1));
        assertEquals(55, Recursion.fib(10));
        assertEquals(2_880_067_194_370_816_120L, Recursion.fib(90));
    }

    // ---- Point ----
    @Test
    void point_equalsAndHashCode() {
        assertEquals(new Point(1, 2), new Point(1, 2));
        assertNotEquals(new Point(1, 2), new Point(2, 1));
        assertNotEquals(new Point(1, 2), null);
        assertNotEquals(new Point(1, 2), "문자열");
        assertEquals(new Point(3, 4).hashCode(), new Point(3, 4).hashCode(), "equals 가 같으면 hashCode 도 같아야 합니다");
    }

    @Test
    void point_setDoesNotGrowForSamePoint() {
        Set<Point> set = new HashSet<>();
        for (int i = 0; i < 100_000; i++) set.add(new Point(1, 2));
        assertEquals(1, set.size(), "같은 점을 10만 번 넣었는데 " + set.size() + "개가 쌓였습니다");
        var map = new HashMap<Point, String>();
        map.put(new Point(5, 5), "다섯");
        assertEquals("다섯", map.get(new Point(5, 5)));
    }

    // ---- ListenerRegistry ----
    @Test
    void registry_unregisterShrinksAndIsIdempotent() {
        var reg = new ListenerRegistry();
        AtomicInteger calls = new AtomicInteger();
        Runnable a = calls::incrementAndGet;
        Runnable b = calls::incrementAndGet;
        reg.register(a);
        reg.register(b);
        reg.unregister(a);
        reg.unregister(a);
        assertEquals(1, reg.size());
        reg.fireAll();
        assertEquals(1, calls.get());
    }
}
