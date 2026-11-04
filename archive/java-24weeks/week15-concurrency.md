# 15주차. 동시성 기초

**예상 시간:** 8시간 | **목표:** 스레드가 왜 위험한지 재현해 보고, `ExecutorService`·`CompletableFuture`로 안전하게 병렬 작업을 처리하는 기본기를 익힌다. 깊이 파는 주차가 아니라 **개념 지도를 그리는 주차**입니다.

## 🖐 먼저 손으로 (읽기 전에 하세요)

> 이 주차의 핵심은 아래 단계를 **직접 타이핑하면서** 깨닫는 것입니다. 아래쪽 설명 문서는 먼저 읽지 않아도 됩니다. 막혔을 때 사전처럼 펼쳐 보세요.

### 1단계. 예측하고 → 치고 → 비교하기 (20분)

1. 아래 코드를 **실행하기 전에** 각 줄의 출력을 노트에 먼저 적습니다.
2. 코드를 한 글자씩 타이핑합니다 (복사·붙여넣기 금지). 오타가 나면 컴파일러가 알려 줍니다.
3. 실행해서 결과를 비교합니다. 예측이 틀린 줄에 ★를 치고, **왜 그렇게 나왔는지 한 문장**으로 적습니다. 이 ★가 오늘 배운 진짜 내용입니다.

```java
// File: Predict15.java
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class Predict15 {
    public static void main(String[] args) throws Exception {
        Thread t = new Thread(() -> System.out.println("in " + Thread.currentThread().getName()), "worker");
        t.run();
        t.start();
        t.join();
        System.out.println("main " + Thread.currentThread().getName());
        ExecutorService pool = Executors.newFixedThreadPool(2);
        Future<Integer> f = pool.submit(() -> 6 * 7);
        System.out.println(f.get());
        Future<?> bad = pool.submit((Runnable) () -> { throw new IllegalStateException("x"); });
        try {
            bad.get();
        } catch (ExecutionException e) {
            System.out.println(e.getCause().getMessage());
        }
        pool.shutdown();
        System.out.println(CompletableFuture.supplyAsync(() -> 20).thenApply(v -> v + 1).join());
        System.out.println(pool.awaitTermination(1, TimeUnit.SECONDS));
    }
}
```

<details>
<summary>실행 결과 (예측을 적은 뒤에 여세요)</summary>

```text
in main
in worker
main main
42
x
21
true
```

</details>

### 2단계. 일부러 망가뜨리기 (20분)

방금 친 코드(또는 아래 설명 문서의 예제)를 하나씩 **고장 내 보고**, 어떤 메시지가 나오는지 확인합니다. 먼저 어떻게 될지 예상한 뒤 실행하세요. 메시지를 외울 필요는 없습니다. 에러 메시지가 **어떤 모양이고 어디를 가리키는지** 눈에 익히는 것이 목표입니다.

| 이렇게 바꿔 보세요 | 어떻게 될까요? |
|---|---|
| `t.start()` 를 `t.run()` 으로 (또는 반대로) | `run()` 은 현재(main) 스레드에서 실행된다. 스레드 이름이 증거 |
| `t.join()` 줄 삭제 | `main main` 이 `in worker` 보다 먼저 나올 수 있다. 실행 순서는 보장되지 않는다 |
| 공유 카운터에서 `synchronized` / `AtomicInteger` 를 빼기 (4스레드 x 10만 번) | 결과가 40만보다 작다. 실행할 때마다 값이 달라지는지 5번 실행해 보세요 |
| `ExecutorService` 를 만들고 `shutdown()` 호출 없이 `main` 끝내기 | 프로그램이 **종료되지 않고 멈춰 있다** (Ctrl+C 로 종료). 풀의 스레드가 계속 살아 있기 때문 |
| `future.get()` 대신 작업 안에서 예외를 던지고 `get` 을 호출하지 않기 | 예외가 **어디에도 출력되지 않는다**. 조용히 사라지는 예외 |
| `Thread.sleep(1000)` 을 `try/catch` 없이 | `unreported exception java.lang.InterruptedException` — checked 예외 |

### 3단계. 빈칸에서 만들기 — 자동 채점 (1~3시간)

`../../labs/week15/` 폴더의 `Mine.java` (`slowFetch` 는 수정 금지)를 열어 `// 여기를 고치세요` 부분을 직접 구현합니다. 구현하고 채점기를 돌려 ✅를 하나씩 늘려 가세요.

```bash
cd labs
./check.sh 15
```

- 처음에는 거의 다 ❌입니다. 정상입니다. **❌ 메시지의 "기대 / 실제"를 읽고** 위에서부터 하나씩 고치세요.
- 컴파일 에러가 나면 첫 번째 에러부터 읽으세요.
- 15분 넘게 막혔을 때만 `../../labs/solutions/week15/`를 열어 보세요. 보고 나서는 **닫고 다시 직접 쳐서** 구현하세요.


---

## 1. 스레드란

한 프로그램 안에서 **동시에 실행되는 흐름**입니다. 웹 서버는 요청마다 스레드를 사용하고, 파일 다운로드를 하는 동안 화면이 멈추지 않는 것도 스레드 덕분입니다.

> **JS와 비교:** JS는 기본적으로 **싱글 스레드 + 이벤트 루프**라서 데이터 경쟁이 거의 없습니다. 자바는 **진짜 멀티 스레드**이고, 여러 스레드가 같은 객체를 동시에 건드릴 수 있어서 동기화 문제를 직접 신경 써야 합니다. 이 차이가 이번 주의 핵심입니다.

## 2. 스레드 만들기

```java
// File: ThreadBasics.java
public class ThreadBasics {
    public static void main(String[] args) throws InterruptedException {
        Runnable task = () -> {
            String name = Thread.currentThread().getName();
            System.out.println(name + " 시작");
            try {
                Thread.sleep(200);                 // 0.2초 대기
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // 인터럽트 상태 복원
                return;
            }
            System.out.println(name + " 종료");
        };

        Thread t1 = new Thread(task, "worker-1");
        Thread t2 = new Thread(task, "worker-2");
        t1.start();                  // start()여야 새 스레드. run()을 직접 부르면 그냥 메서드 호출
        t2.start();
        t1.join();                   // 끝날 때까지 기다리기
        t2.join();
        System.out.println("모두 종료");
    }
}
```

출력 순서는 실행할 때마다 달라질 수 있습니다. **동시성 프로그램에서 순서에 의존하면 안 됩니다.**

## 3. 경쟁 상태(Race Condition) 재현

`count++`는 읽기 → 더하기 → 쓰기, 세 단계입니다. 두 스레드가 끼어들면 업데이트가 사라집니다.

```java
// File: RaceDemo.java
import java.util.concurrent.atomic.AtomicInteger;

public class RaceDemo {
    static int unsafe = 0;
    static int guarded = 0;
    static final Object LOCK = new Object();
    static final AtomicInteger atomic = new AtomicInteger();

    public static void main(String[] args) throws InterruptedException {
        Runnable work = () -> {
            for (int i = 0; i < 100_000; i++) {
                unsafe++;                            // 안전하지 않음
                synchronized (LOCK) { guarded++; }   // 한 번에 한 스레드만 들어갈 수 있다
                atomic.incrementAndGet();            // 락 없이 원자적으로 증가
            }
        };

        Thread a = new Thread(work);
        Thread b = new Thread(work);
        a.start(); b.start();
        a.join(); b.join();

        System.out.println("unsafe  = " + unsafe + "   (기대: 200000, 보통 더 작다)");
        System.out.println("guarded = " + guarded);   // 200000
        System.out.println("atomic  = " + atomic);    // 200000
    }
}
```

`unsafe`는 실행할 때마다 값이 달라질 수 있습니다. 이런 버그는 **재현이 어렵고 운영에서만 나타나기 때문에** 가장 무서운 종류입니다.

### 해결 도구 정리

| 도구 | 용도 |
|------|------|
| `synchronized` | 간단한 상호 배제 |
| `AtomicInteger` 등 | 카운터·플래그 하나를 락 없이 안전하게 |
| `ConcurrentHashMap` | 여러 스레드가 쓰는 맵 (`HashMap`은 안전하지 않음) |
| **불변 객체** | 아무도 바꾸지 못하면 동기화가 필요 없다. 가장 좋은 해결책 |

7주차의 `record`와 5주차의 `final` 필드가 동시성에서 큰 힘을 발휘합니다.

## 4. ExecutorService — 스레드 풀

스레드를 직접 만들지 말고 **작업을 제출**하세요. 풀이 스레드의 생성·재사용을 관리합니다.

```java
// File: ExecutorDemo.java
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ExecutorDemo {
    static int slowSquare(int n) throws InterruptedException {
        Thread.sleep(300);
        return n * n;
    }

    public static void main(String[] args) throws Exception {
        try (ExecutorService pool = Executors.newFixedThreadPool(4)) {    // Java 19+: close() 지원
            List<Future<Integer>> futures = new ArrayList<>();
            for (int i = 1; i <= 8; i++) {
                final int n = i;
                Callable<Integer> job = () -> slowSquare(n);              // 값을 돌려주는 작업
                futures.add(pool.submit(job));
            }

            int sum = 0;
            for (Future<Integer> f : futures) {
                try {
                    sum += f.get();                                       // 결과가 나올 때까지 대기
                } catch (ExecutionException e) {
                    System.out.println("작업 실패: " + e.getCause());
                }
            }
            System.out.println("합계: " + sum);                            // 1+4+9+...+64 = 204
        }                                                                 // 블록이 끝나면 작업 완료를 기다리고 종료
    }
}
```

8개 작업(각 0.3초)을 스레드 4개로 처리하므로 약 0.6초에 끝납니다. 순차 실행이면 2.4초입니다.

## 5. CompletableFuture — 비동기 조합

JS의 `Promise`와 가장 비슷한 도구입니다.

```java
// File: CompletableDemo.java
import java.util.concurrent.CompletableFuture;

public class CompletableDemo {
    static String fetchUser(int id) {
        sleep(300);
        return "user-" + id;
    }

    static int fetchScore(String user) {
        sleep(300);
        return user.length() * 10;
    }

    static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    public static void main(String[] args) {
        // JS: fetchUser().then(fetchScore).then(...)
        CompletableFuture<String> result = CompletableFuture
                .supplyAsync(() -> fetchUser(1))                  // 다른 스레드에서 실행
                .thenApply(CompletableDemo::fetchScore)           // 결과를 받아 변환
                .thenApply(score -> "점수: " + score)
                .exceptionally(ex -> "실패: " + ex.getMessage()); // catch와 같다

        System.out.println(result.join());                         // 점수: 60

        // 두 작업을 동시에 실행하고 합치기
        CompletableFuture<String> a = CompletableFuture.supplyAsync(() -> fetchUser(1));
        CompletableFuture<String> b = CompletableFuture.supplyAsync(() -> fetchUser(2));
        System.out.println(a.thenCombine(b, (x, y) -> x + " & " + y).join());
    }
}
```

> **JS와 비교:** `await`처럼 현재 스레드를 비동기로 재개하는 문법은 자바에 없습니다. 위처럼 체이닝하거나, 아래 가상 스레드로 **동기 방식 코드를 그대로 쓰는** 접근이 자바의 선택입니다.

## 6. 가상 스레드 (Java 21)

일반 스레드는 운영체제 스레드와 1:1이라 수천 개를 만들면 무겁습니다. **가상 스레드**는 JVM이 관리하는 가벼운 스레드라서 수만 개도 문제없고, 블로킹 I/O 코드를 그대로 써도 효율적입니다.

```java
// File: VirtualThreadDemo.java
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

public class VirtualThreadDemo {
    public static void main(String[] args) {
        long start = System.nanoTime();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            IntStream.range(0, 2_000).forEach(i ->
                    executor.submit(() -> {
                        Thread.sleep(Duration.ofSeconds(1));     // 2000개가 동시에 1초씩 대기
                        return i;
                    }));
        }   // 모든 작업이 끝날 때까지 대기

        System.out.println("경과: " + (System.nanoTime() - start) / 1_000_000 + "ms");  // 약 1초 남짓
    }
}
```

플랫폼 스레드 풀 4개로 같은 작업을 하면 500초가 걸립니다. 입출력 대기가 많은 서버(Spring Boot 3.2+에서 `spring.threads.virtual.enabled=true`)에서 특히 유용합니다. CPU를 오래 쓰는 계산에는 이점이 없습니다.

## 7. 정리: 무엇을 선택할까

| 상황 | 선택 |
|------|------|
| 공유 데이터를 아예 만들지 않는다 | 불변 객체 (가장 좋음) |
| 카운터 하나 | `AtomicInteger` |
| 여러 줄을 한 번에 보호 | `synchronized` |
| 병렬 작업 여러 개 | `ExecutorService` |
| 비동기 결과를 이어 붙이기 | `CompletableFuture` |
| I/O 대기가 많은 서버 코드 | 가상 스레드 |

## 연습문제

1. `RaceDemo`의 반복 횟수와 스레드 수를 바꿔 `unsafe` 값이 어떻게 달라지는지 5번 이상 실행해 보세요.
2. `ExecutorService`로 1~100,000의 합을 4구간으로 나눠 병렬로 계산하고 합치세요. 단일 스레드와 결과가 같은지 검증합니다.
3. `ConcurrentHashMap`과 `HashMap`을 각각 여러 스레드에서 `merge`로 단어 빈도를 세는 데 써 보세요. 결과 차이를 관찰하세요.
4. `CompletableFuture`로 "사용자 조회 → 주문 목록 조회 → 합계 계산"을 이어 붙이고, 중간에 예외가 나면 기본값을 돌려주게 하세요.
5. 가상 스레드 예제를 `Executors.newFixedThreadPool(50)`으로 바꿔 소요 시간을 비교하세요.
6. (생각해 보기) 9주차 도서 대출 시스템에서 두 사용자가 동시에 같은 책을 대출하면 어떤 일이 벌어질까요? 어느 메서드를 어떻게 보호해야 할까요?

## 퀴즈

1. `thread.run()`과 `thread.start()`의 차이는?
2. 경쟁 상태란?
3. `synchronized`와 `AtomicInteger`의 선택 기준은?
4. 가상 스레드가 효과적인 작업과 그렇지 않은 작업은?

<details>
<summary>정답</summary>

1. `run()`은 현재 스레드에서 일반 메서드처럼 실행되고, `start()`는 새 스레드를 만들어 `run()`을 실행한다
2. 여러 스레드가 공유 데이터에 동시에 접근해 실행 순서에 따라 결과가 달라지는 문제
3. 변수 하나의 단순한 증가·교체는 `Atomic*`, 여러 상태를 함께 바꾸는 구간은 `synchronized`
4. 블로킹 I/O(네트워크, DB, 파일) 대기가 많은 작업에 효과적. CPU 집약 계산은 코어 수 이상으로 빨라지지 않는다

</details>

## 체크리스트

- [ ] 경쟁 상태를 직접 재현했다
- [ ] `ExecutorService`로 작업 여러 개를 제출하고 결과를 모았다
- [ ] `CompletableFuture`를 JS `Promise`와 대응시켜 설명할 수 있다
- [ ] "공유 상태를 줄이는 것이 최선"이라는 원칙을 설명할 수 있다
