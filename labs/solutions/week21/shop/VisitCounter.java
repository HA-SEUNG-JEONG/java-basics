package shop;

import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Component;

@Component
public class VisitCounter {
    private final AtomicInteger count = new AtomicInteger();

    public int increment() { return count.incrementAndGet(); }
    public int current() { return count.get(); }
    public void reset() { count.set(0); }
}
