import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class Mine {
    static String slowFetch(int id) {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return "user-" + id;
    }

    static long parallelSum(long n, int parts) throws Exception {
        try (ExecutorService pool = Executors.newFixedThreadPool(parts)) {
            List<Future<Long>> futures = new ArrayList<>();
            long chunk = (n + parts - 1) / parts;
            for (int i = 0; i < parts; i++) {
                final long from = i * chunk + 1;
                final long to = Math.min(n, (i + 1) * chunk);
                futures.add(pool.submit(() -> {
                    long sum = 0;
                    for (long v = from; v <= to; v++) sum += v;
                    return sum;
                }));
            }
            long total = 0;
            for (Future<Long> f : futures) total += f.get();
            return total;
        }
    }

    static int safeCounter(int threads, int perThread) throws Exception {
        AtomicInteger counter = new AtomicInteger();
        List<Thread> list = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            Thread t = new Thread(() -> {
                for (int k = 0; k < perThread; k++) counter.incrementAndGet();
            });
            list.add(t);
            t.start();
        }
        for (Thread t : list) t.join();
        return counter.get();
    }

    static List<String> fetchAll(List<Integer> ids) throws Exception {
        try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {
            List<CompletableFuture<String>> futures = ids.stream()
                    .map(id -> CompletableFuture.supplyAsync(() -> slowFetch(id), pool))
                    .toList();
            return futures.stream().map(CompletableFuture::join).toList();
        }
    }
}
