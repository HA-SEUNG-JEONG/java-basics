import java.util.List;
import java.util.stream.IntStream;

public class Check {
    public static void main(String[] args) {
        T.eq("parallelSum(100, 4) = 5050", 5050L, () -> Mine.parallelSum(100, 4));
        T.eq("parallelSum(1_000_000, 8) = 500000500000", 500_000_500_000L, () -> Mine.parallelSum(1_000_000, 8));
        T.eq("parallelSum(10, 3)  (나누어떨어지지 않는 경우) = 55", 55L, () -> Mine.parallelSum(10, 3));

        T.eq("safeCounter 를 5번 실행해도 항상 정확하다 (4 x 100000)", true, () -> {
            for (int i = 0; i < 5; i++) {
                if (Mine.safeCounter(4, 100_000) != 400_000) return false;
            }
            return true;
        });
        String src = T.source(args[0] + "/Mine.java");
        T.ok("synchronized / Atomic / Lock 중 하나를 썼는가",
                () -> src.contains("synchronized (") || src.contains("synchronized(") || src.contains("Atomic")
                        || src.contains("Lock") || src.contains("LongAdder"),
                "공유 변수에 대한 동기화 수단이 필요합니다");

        List<Integer> ids = IntStream.rangeClosed(1, 10).boxed().toList();
        List<String> expected = ids.stream().map(i -> "user-" + i).toList();
        long start = System.nanoTime();
        T.eq("fetchAll 결과와 순서", expected, () -> Mine.fetchAll(ids));
        long ms = (System.nanoTime() - start) / 1_000_000;
        T.ok("fetchAll 이 병렬로 실행되는가 (10개 x 200ms 를 1.2초 안에, 실제 " + ms + "ms)", () -> ms < 1200,
                "하나씩 순서대로 실행하면 약 2000ms 가 걸린다");
        T.done();
    }
}
