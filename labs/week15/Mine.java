import java.util.List;

// 15주차 실습. 채점:  cd labs && ./check.sh 15
public class Mine {
    /** 이 메서드는 수정하지 마세요. 느린 조회를 흉내 낸다 (200ms) */
    static String slowFetch(int id) {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return "user-" + id;
    }

    /** 1..n 의 합을 parts 개 구간으로 나눠 ExecutorService 로 병렬 계산해 합친다 */
    static long parallelSum(long n, int parts) throws Exception {
        return -1; // 여기를 고치세요
    }

    /**
     * 스레드 threads 개가 각각 공유 카운터를 perThread 번씩 1 증가시킨 뒤의 최종 값을 돌려준다.
     * 정확히 threads * perThread 가 나와야 한다. (synchronized 또는 AtomicInteger)
     * 힌트: 일부러 안전하지 않게 먼저 짜서 값이 틀리게 나오는 것을 눈으로 본 다음 고치세요.
     */
    static int safeCounter(int threads, int perThread) throws Exception {
        return -1; // 여기를 고치세요
    }

    /**
     * ids 각각에 slowFetch 를 호출하되 모두 동시에 실행한다 (CompletableFuture 또는 가상 스레드).
     * 결과 순서는 ids 순서와 같아야 한다. 10개를 순서대로 하나씩 하면 2초, 병렬이면 0.2~0.5초.
     */
    static List<String> fetchAll(List<Integer> ids) throws Exception {
        return List.of(); // 여기를 고치세요
    }
}
