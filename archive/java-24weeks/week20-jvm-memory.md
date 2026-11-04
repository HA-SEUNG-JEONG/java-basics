# 20주차. JVM과 메모리

**예상 시간:** 8시간 | **목표:** 스택과 힙, 가비지 컬렉션(GC), 메모리 누수를 눈으로 보고 손으로 일으켜 본다. 서버는 몇 주씩 꺼지지 않고 돌기 때문에, 메모리를 이해하지 못하면 **어느 날 새벽에 죽는 코드**를 만들게 됩니다.

> 명령은 모두 `../../labs/backend/` 에서 실행합니다. 1~2단계의 단일 파일 실습은 아무 폴더에서나 `java 파일.java` 로 실행합니다.

## 🖐 먼저 손으로 (읽기 전에 하세요)

> 이 주차의 핵심은 아래 단계를 **직접 타이핑하면서** 깨닫는 것입니다. 아래쪽 설명 문서는 먼저 읽지 않아도 됩니다. 막혔을 때 사전처럼 펼쳐 보세요.

### 1단계. 예측하고 → 치고 → 비교하기 (30분)

1. 아래 코드를 **실행하기 전에** 6줄의 출력을 노트에 먼저 적습니다. (`Integer` 비교는 2주차·8주차의 `==` 와 `equals` 를 떠올리세요)
2. 한 글자씩 타이핑하고 **힙을 32MB 로 제한**해 실행합니다.

   ```bash
   java -Xmx32m Predict20.java
   ```

3. 틀린 줄에 ★를 치고 이유를 한 문장으로 적습니다.

```java
// File: Predict20.java
import java.util.ArrayList;
import java.util.List;

public class Predict20 {
    static int depth = 0;

    static void down() {
        depth++;
        down();
    }

    public static void main(String[] args) {
        int[] a = {1, 2, 3};
        int[] b = a;
        b[0] = 99;
        System.out.println(a[0]);

        Integer x = 127, y = 127, p = 128, q = 128;
        System.out.println((x == y) + " " + (p == q) + " " + p.equals(q));

        String s1 = "hi", s2 = "hi", s3 = new String("hi");
        System.out.println((s1 == s2) + " " + (s1 == s3) + " " + (s1 == s3.intern()));

        try {
            down();
        } catch (StackOverflowError e) {
            System.out.println("스택 터짐, 1000번 넘게 들어갔나: " + (depth > 1000));
        }

        System.out.println("최대 힙(MB): " + Runtime.getRuntime().maxMemory() / 1024 / 1024);
        List<byte[]> keep = new ArrayList<>();
        try {
            while (true) keep.add(new byte[1024 * 1024]);
        } catch (OutOfMemoryError e) {
            keep = null;
            System.out.println("OOM: " + e.getMessage());
        }
    }
}
```

<details>
<summary>실행 결과 (예측을 적은 뒤에 여세요)</summary>

```text
99
true false true
true false true
스택 터짐, 1000번 넘게 들어갔나: true
최대 힙(MB): 32
OOM: Java heap space
```

- `99`: 배열은 힙에 하나만 있고 `a`, `b` 는 같은 객체를 가리킵니다.
- `127` 과 `128`: `Integer` 는 -128~127 을 미리 만들어 재사용합니다. 그래서 `==` 가 127 에서만 `true` 입니다. 값 비교는 항상 `equals`.
- `s1 == s3` 가 `false`: `new String` 은 항상 새 객체입니다. `intern()` 은 풀(pool)에 있는 것을 돌려줍니다.
- 스택 깊이 숫자 자체는 기계와 옵션마다 다릅니다. 줄 4와 6의 결과만 보면 됩니다. 마지막 줄은 힙이 가득 차서 `OutOfMemoryError` 가 `Java heap space` 메시지와 함께 나온 것입니다.

</details>

### 2단계. 일부러 망가뜨리기 + JVM 들여다보기 (40분)

| 이렇게 바꿔 보세요 | 어떻게 될까요? |
|---|---|
| `java -Xmx32m` 를 `-Xmx256m` 으로 | 같은 `OOM` 이지만 오래 걸린다. 한계가 클 뿐 한계가 없는 것이 아니다 |
| 아래 `Depth.java` 를 `java Depth`, `java -Xss256k Depth`, `java -Xss4m Depth` 로 | 숫자가 달라진다. **스택 크기는 스레드마다 고정**이고 `-Xss` 로 바꾼다 (내 컴퓨터에서 약 46,000 / 1,500 / 98,000) |
| `down()` 에서 `depth++` 를 빼고 한 줄로 `down();` 만 | 스택은 똑같이 터진다. 깊이를 세는 것과 무관 |
| `-Xlog:gc` 를 붙여 `java -Xmx32m -Xlog:gc Predict20.java` | 줄마다 `Pause Young ... 14M->13M(32M)` 같은 GC 기록. 쓰레기가 거의 없으면 GC 를 해도 줄지 않는다 (`14M->13M`) |

```java
// File: Depth.java
public class Depth {
    static int depth = 0;
    static void down() { depth++; down(); }
    public static void main(String[] a) {
        try { down(); } catch (StackOverflowError e) { System.out.println(depth); }
    }
}
```

**실행 중인 JVM 들여다보기.** 아래 `Sleepy.java` 를 만들어 한 터미널에서 `java -Xmx64m Sleepy.java` 로 실행해 두고(8초 뒤 종료), 다른 터미널에서:

```java
// File: Sleepy.java
import java.util.ArrayList;
import java.util.List;

public class Sleepy {
    public static void main(String[] a) throws Exception {
        List<byte[]> keep = new ArrayList<>();
        for (int i = 0; i < 20; i++) keep.add(new byte[1024 * 1024]);
        Thread.sleep(8000);
        System.out.println(keep.size());
    }
}
```

```bash
jps -l                      # 실행 중인 자바 프로세스 목록. Sleepy 를 찾아 pid 확인
jcmd <pid> GC.heap_info     # 힙 사용량 (garbage-first heap total 65536K, used ...K)
jcmd <pid> VM.flags         # -XX:MaxHeapSize=67108864 (= 64MB). 우리가 준 -Xmx64m 이다
```

IDE 없이도 JVM 안을 볼 수 있다는 감각이 목표입니다. 숫자의 정확한 값은 외우지 않습니다.

### 3단계. 빈칸에서 만들기 — 자동 채점 (2~4시간)

`../../labs/backend/week20/src/main/java/` 의 네 파일을 채웁니다.

| 파일 | 만드는 것 | 관련 개념 |
|---|---|---|
| `LruCache` | 크기 제한 캐시. 가장 오래 안 쓴 항목부터 버림 | 힙은 유한하다. 캐시는 **반드시 상한**이 있어야 한다 |
| `Recursion.sumTo`, `fib` | 반복문 버전 | 스택은 유한하다 |
| `Point` | `equals`/`hashCode` 직접 작성 | 같은 값을 계속 쌓는 `HashSet` 의 낭비 |
| `ListenerRegistry.unregister` | 등록 해제 | 해제를 잊는 누수 |

```bash
cd ../../labs/backend
./gradlew :week20:test
```

- 이 주차의 테스트 JVM 은 **힙을 64MB 로 제한**해 두었습니다 (`week20/build.gradle.kts`). 20KB 짜리를 5만 개(약 1GB) 넣어도 `capacity` 만큼만 남는 `LruCache` 여야 통과합니다.
- **용량을 무시한 캐시(예: 그냥 `HashMap`)로 구현하면** `OutOfMemoryError` 로 테스트 JVM 이 죽고, Gradle 이 `Could not complete execution for Gradle Test Executor` 라는 낯선 메시지를 냅니다. OOM 이라는 말이 없어도 힙을 의심하세요. 이 메시지를 한 번 본 사람은 서버에서도 알아봅니다.
- 처음에는 10개 중 9개가 실패합니다. 정답은 `../../labs/solutions/week20/` 에 있습니다. **15분 넘게 막혔을 때만** 열고, 본 뒤에는 덮고 **직접 다시 치세요.**

---

## 1. 메모리 구역 지도

| 구역 | 들어 있는 것 | 크기 옵션 | 넘치면 |
|---|---|---|---|
| **스택**(스레드마다 하나) | 메서드 호출 기록, 지역 변수, 참조 값 | `-Xss` | `StackOverflowError` |
| **힙**(JVM 전체 공유) | `new` 로 만든 객체와 배열 | `-Xms`(시작) `-Xmx`(최대) | `OutOfMemoryError: Java heap space` |
| 메타스페이스 | 클래스 정보 | `-XX:MaxMetaspaceSize` | `OutOfMemoryError: Metaspace` (드묾) |

`int[] b = a;` 는 스택의 변수 `b` 에 **같은 힙 객체의 주소**를 복사합니다. 객체가 복사되는 게 아닙니다. (1단계 첫 줄 `99`)

> **JS와 비교:** JS 엔진(V8)에도 힙과 GC 가 있지만 개발자가 크기를 정하는 일은 드뭅니다. 자바 서버는 `-Xmx` 를 직접 정해서 띄우는 일이 흔합니다. 컨테이너(도커)에서 메모리 한계와 `-Xmx` 가 어긋나면 프로세스가 통째로 죽습니다.

## 2. 가비지 컬렉션 (GC)

- GC 는 **어디서도 참조되지 않는 객체**의 메모리를 거둡니다. 참조가 하나라도 남아 있으면 거두지 않습니다.
- 대부분의 객체는 금방 버려집니다. 그래서 JVM 은 힙을 새 객체 영역(Young)과 오래 산 객체 영역으로 나눠 새 영역을 자주, 가볍게 청소합니다. (`Pause Young` 로그)
- GC 가 도는 동안 짧은 멈춤이 생길 수 있습니다. 대부분의 서비스에서는 문제 삼지 않아도 되고, 지연에 민감할 때만 GC 종류와 옵션을 조정합니다.
- `System.gc()` 는 "해 주세요"라는 요청일 뿐 보장이 아닙니다. 코드에서 직접 부르지 않습니다.

## 3. 메모리 누수 (자바에도 있다)

GC 가 있어도 누수는 생깁니다. **"쓰지 않는데 아직 참조되는 객체"** 가 쌓이는 것입니다.

| 흔한 원인 | 이번 주 실습 |
|---|---|
| 상한 없는 `static` 컬렉션·캐시 | `LruCache` |
| 등록만 하고 해제하지 않는 리스너·콜백 (등록 목록이 리스너를 계속 붙잡는다) | `ListenerRegistry.unregister` |
| `equals`/`hashCode` 가 없어 같은 값이 키로 계속 쌓임 | `Point` |
| 닫지 않은 연결·스트림 | 14주차의 `try-with-resources` |

`equals` 만 구현하고 `hashCode` 를 빼먹으면 해시 기반 컬렉션(`HashSet`, `HashMap`)에서 같은 값을 못 찾습니다. 8주차에서 배운 규칙 "equals 를 재정의하면 hashCode 도" 의 실제 피해가 이것입니다.

## 4. 재귀와 스택

재귀 호출 한 번이 스택 한 칸을 씁니다. 깊이가 수만을 넘으면 `StackOverflowError`. 반복문으로 바꾸면 스택은 한 칸만 씁니다. 자바는 꼬리 재귀 최적화를 하지 않으므로, **깊이가 입력에 비례하는 재귀는 반복문으로** 바꾸는 습관을 들입니다. (4주차 재귀)

## 연습문제

1. `Sleepy` 의 `20` 을 `80` 으로 바꿔 `-Xmx64m` 로 실행하면 어떤 일이 벌어지는지, 에러 메시지 전체를 읽어 보세요.
2. 1단계 코드에서 `-Xlog:gc` 의 출력 중 `Pause Young` 줄을 세 개 골라, 앞뒤 숫자(`14M->13M(32M)`)가 각각 무엇인지 문서 없이 추측해 적어 보세요. (정답: 수거 전 → 수거 후 (전체 힙))
3. `Point` 에서 `hashCode` 만 지우고 `HashSet` 에 같은 좌표를 두 번 넣어 크기를 출력해 보세요. (대부분 2 가 나옵니다. 왜 "항상" 이 아니라 "대부분"일까요?)
4. `LruCache` 를 `LinkedHashMap` 없이 `HashMap` + `ArrayDeque` 로 직접 구현해 보고, `get` 에서 순서를 갱신해야 하는 이유를 설명해 보세요.

## 퀴즈

1. 스택과 힙 중 `new int[1000]` 의 내용은 어디에 저장되고, 변수 `arr` 는 어디에 있나?
2. `StackOverflowError` 와 `OutOfMemoryError` 의 원인은 각각?
3. 자바에 GC 가 있는데도 메모리 누수가 생기는 이유는?
4. `Integer` 두 개를 `==` 로 비교하면 안 되는 이유는?

<details>
<summary>정답</summary>

1. 배열 내용은 힙, 변수 `arr`(참조)는 스택(지역 변수일 때)
2. 스택 오버플로: 호출이 너무 깊음(무한 재귀 등). OOM: 힙에 살아 있는 객체가 `-Xmx` 를 넘음
3. GC 는 "참조되지 않는 것"만 치운다. 쓰지 않지만 참조가 남아 있는 객체는 치우지 않는다
4. -128~127 은 캐시된 같은 객체라 우연히 `true`, 그 밖에는 서로 다른 객체라 `false`. 값 비교는 `equals`

</details>

## 체크리스트

- [ ] 스택과 힙에 무엇이 들어가는지 그림으로 그릴 수 있다
- [ ] `StackOverflowError` 와 `OutOfMemoryError` 를 직접 일으켜 메시지를 봤다
- [ ] `jps` 와 `jcmd` 로 실행 중인 JVM 의 힙 정보를 봤다
- [ ] "상한 없는 컬렉션"이 왜 위험한지 설명할 수 있다
- [ ] 10개 테스트를 모두 통과했다
