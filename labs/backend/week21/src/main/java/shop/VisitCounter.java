package shop;

import java.util.concurrent.atomic.AtomicInteger;

/** 방문 횟수 보관소. Spring 이 "하나만" 만들어 모두에게 나눠 주게 하세요. (싱글톤 빈) */
public class VisitCounter {
    private final AtomicInteger count = new AtomicInteger();

    public int increment() { return count.incrementAndGet(); }
    public int current() { return count.get(); }
    public void reset() { count.set(0); }
}
