# 11주차. 컬렉션 프레임워크

**예상 시간:** 10시간 | **목표:** 상황에 맞는 컬렉션을 고르고, 정렬과 순회를 능숙하게 다룬다. 자바 실무에서 가장 많이 쓰는 주차입니다.

## 🖐 먼저 손으로 (읽기 전에 하세요)

> 이 주차의 핵심은 아래 단계를 **직접 타이핑하면서** 깨닫는 것입니다. 아래쪽 설명 문서는 먼저 읽지 않아도 됩니다. 막혔을 때 사전처럼 펼쳐 보세요.

### 1단계. 예측하고 → 치고 → 비교하기 (20분)

1. 아래 코드를 **실행하기 전에** 각 줄의 출력을 노트에 먼저 적습니다.
2. 코드를 한 글자씩 타이핑합니다 (복사·붙여넣기 금지). 오타가 나면 컴파일러가 알려 줍니다.
3. 실행해서 결과를 비교합니다. 예측이 틀린 줄에 ★를 치고, **왜 그렇게 나왔는지 한 문장**으로 적습니다. 이 ★가 오늘 배운 진짜 내용입니다.

```java
// File: Predict11.java
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class Predict11 {
    public static void main(String[] args) {
        List<Integer> list = new ArrayList<>(List.of(10, 20, 30, 40));
        list.remove(1);
        System.out.println(list);
        list.remove(Integer.valueOf(10));
        System.out.println(list);
        Set<String> set = new TreeSet<>(List.of("pear", "apple", "fig", "apple"));
        System.out.println(set);
        Map<String, Integer> m = new LinkedHashMap<>();
        m.put("b", 1);
        m.put("a", 2);
        m.put("b", 3);
        System.out.println(m);
        System.out.println(m.getOrDefault("z", -1));
        m.merge("a", 10, Integer::sum);
        System.out.println(m.get("a"));
        Deque<Integer> st = new ArrayDeque<>();
        st.push(1);
        st.push(2);
        st.push(3);
        System.out.println(st.pop() + " " + st.peek());
        try {
            List.of(1).add(2);
        } catch (UnsupportedOperationException e) {
            System.out.println("immutable");
        }
    }
}
```

<details>
<summary>실행 결과 (예측을 적은 뒤에 여세요)</summary>

```text
[10, 30, 40]
[30, 40]
[apple, fig, pear]
{b=3, a=2}
-1
12
3 2
immutable
```

</details>

### 2단계. 일부러 망가뜨리기 (20분)

방금 친 코드(또는 아래 설명 문서의 예제)를 하나씩 **고장 내 보고**, 어떤 메시지가 나오는지 확인합니다. 먼저 어떻게 될지 예상한 뒤 실행하세요. 메시지를 외울 필요는 없습니다. 에러 메시지가 **어떤 모양이고 어디를 가리키는지** 눈에 익히는 것이 목표입니다.

| 이렇게 바꿔 보세요 | 어떻게 될까요? |
|---|---|
| `List<int> nums = ...;` | `unexpected type; required: reference, found: int` — 컬렉션은 래퍼 타입(`Integer`)만 담는다 |
| `for (Integer n : list) { list.remove(n); }` | `ConcurrentModificationException` — 순회 중 수정 금지. `removeIf` 로 고치기 |
| `list.get(5)` (크기 3) | `IndexOutOfBoundsException: Index 5 out of bounds for length 3` |
| `Map<String,Integer> m = new HashMap<>(); int v = m.get("none");` | `NullPointerException` — `get` 이 null 을 돌려주고 그것을 int 로 언박싱하다가 터진다 |
| `Set.of(1, 1)` | `IllegalArgumentException: duplicate element: 1` |
| `equals`/`hashCode` 없는 클래스를 `HashSet` 에 같은 내용으로 두 번 넣기 | 중복이 제거되지 않는다 |

### 3단계. 빈칸에서 만들기 — 자동 채점 (1~3시간)

`../../labs/week11/` 폴더의 `Mine.java`를 열어 `// 여기를 고치세요` 부분을 직접 구현합니다. 구현하고 채점기를 돌려 ✅를 하나씩 늘려 가세요.

```bash
cd labs
./check.sh 11
```

- 처음에는 거의 다 ❌입니다. 정상입니다. **❌ 메시지의 "기대 / 실제"를 읽고** 위에서부터 하나씩 고치세요.
- 컴파일 에러가 나면 첫 번째 에러부터 읽으세요.
- 15분 넘게 막혔을 때만 `../../labs/solutions/week11/`를 열어 보세요. 보고 나서는 **닫고 다시 직접 쳐서** 구현하세요.


---

## 1. 큰 그림

```
Collection
├── List   순서 O, 중복 O      ArrayList(기본), LinkedList
├── Set    순서 X, 중복 X      HashSet(기본), LinkedHashSet(삽입 순서), TreeSet(정렬)
└── Queue  FIFO               ArrayDeque(기본), PriorityQueue(우선순위)

Map  키 -> 값, 키 중복 X       HashMap(기본), LinkedHashMap(삽입 순서), TreeMap(키 정렬)
```

**선택 규칙:** 모르겠으면 `ArrayList`, `HashSet`, `HashMap`, `ArrayDeque`. 순서나 정렬이 필요할 때만 `Linked`/`Tree`를 고르세요.

변수 타입은 **인터페이스로** 선언합니다.

```java
List<String> names = new ArrayList<>();   // O
ArrayList<String> names = new ArrayList<>(); // 피하세요: 구현에 묶임
```

> **JS와 비교:** `ArrayList` ≈ `Array`, `HashMap` ≈ `Map`, `HashSet` ≈ `Set`. 차이: 자바 컬렉션은 **원소 타입을 `<String>`처럼 고정**하고, 기본형은 못 담고 래퍼(`Integer`)를 담습니다.

## 2. List

```java
// File: ListDemo.java
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListDemo {
    public static void main(String[] args) {
        List<String> list = new ArrayList<>();
        list.add("b");
        list.add("a");
        list.add("c");
        list.add(1, "x");                  // 인덱스에 삽입
        System.out.println(list);          // [b, x, a, c]
        System.out.println(list.get(2));   // a
        list.set(0, "z");
        list.remove("x");                  // 값으로 삭제
        list.remove(0);                    // 인덱스로 삭제 (위와 오버로딩이 다름에 주의)
        System.out.println(list + " size=" + list.size());   // [a, c] size=2
        System.out.println(list.contains("c"));               // true
        System.out.println(list.indexOf("c"));                // 1

        Collections.sort(list);
        Collections.reverse(list);
        System.out.println(list);

        // 불변 리스트
        List<Integer> fixed = List.of(1, 2, 3);
        // fixed.add(4);                    // UnsupportedOperationException

        // 순회 중 삭제 함정
        List<Integer> nums = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6));
        try {
            for (Integer n : nums) {
                if (n % 2 == 0) nums.remove(n);   // ConcurrentModificationException
            }
        } catch (java.util.ConcurrentModificationException e) {
            System.out.println("순회 중에 직접 삭제하면 안 된다");
        }
        nums.removeIf(n -> n % 2 == 0);           // 올바른 방법
        System.out.println(nums);                  // [1, 3, 5]
    }
}
```

`List<Integer>`에서 `remove(1)`은 **인덱스 1을 삭제**하고, 값 1을 지우려면 `remove(Integer.valueOf(1))`입니다.

## 3. Set

```java
// File: SetDemo.java
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

public class SetDemo {
    public static void main(String[] args) {
        Set<String> set = new HashSet<>();
        System.out.println(set.add("a"));    // true
        System.out.println(set.add("a"));    // false: 이미 있음
        set.add("b");
        System.out.println(set.size());      // 2

        Set<Integer> sorted = new TreeSet<>(Set.of(5, 1, 3));
        System.out.println(sorted);          // [1, 3, 5]

        Set<String> ordered = new LinkedHashSet<>();
        ordered.add("z"); ordered.add("a");
        System.out.println(ordered);         // [z, a] 삽입 순서 유지

        // 중복 제거 관용구
        java.util.List<Integer> dup = java.util.List.of(1, 2, 2, 3, 3, 3);
        System.out.println(new LinkedHashSet<>(dup));  // [1, 2, 3]

        // 집합 연산
        Set<Integer> a = new HashSet<>(Set.of(1, 2, 3));
        Set<Integer> b = Set.of(2, 3, 4);
        a.retainAll(b);                      // 교집합
        System.out.println(a);               // [2, 3]
    }
}
```

`HashSet`에 넣는 객체는 8주차의 `equals`/`hashCode`가 올바르게 구현되어 있어야 합니다.

## 4. Map

```java
// File: MapDemo.java
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class MapDemo {
    public static void main(String[] args) {
        Map<String, Integer> stock = new HashMap<>();
        stock.put("apple", 3);
        stock.put("banana", 5);
        stock.put("apple", 4);                         // 같은 키는 덮어쓴다
        System.out.println(stock.get("apple"));        // 4
        System.out.println(stock.get("grape"));        // null (없으면 null)
        System.out.println(stock.getOrDefault("grape", 0)); // 0
        System.out.println(stock.containsKey("banana"));    // true
        stock.remove("banana");

        stock.putIfAbsent("kiwi", 1);                  // 없을 때만 넣기
        stock.merge("kiwi", 5, Integer::sum);          // 있으면 합치고 없으면 넣기 -> 6
        stock.computeIfAbsent("pear", k -> 10);
        System.out.println(new TreeMap<>(stock));      // 키 정렬해서 출력: {apple=4, kiwi=6, pear=10}

        // 순회
        for (Map.Entry<String, Integer> e : new TreeMap<>(stock).entrySet()) {
            System.out.println(e.getKey() + " -> " + e.getValue());
        }
        stock.forEach((k, v) -> System.out.print(""));   // 람다 순회 (13주차)
    }
}
```

### 대표 패턴: 단어 빈도 세기

```java
// File: WordCount.java
import java.util.Map;
import java.util.TreeMap;

public class WordCount {
    public static void main(String[] args) {
        String text = "to be or not to be that is the question to";
        Map<String, Integer> count = new TreeMap<>();
        for (String w : text.split(" ")) {
            count.merge(w, 1, Integer::sum);           // 없으면 1, 있으면 +1
        }
        System.out.println(count);   // {be=2, is=1, not=1, or=1, question=1, that=1, the=1, to=3}
    }
}
```

`HashMap`의 순회 순서는 **보장되지 않습니다.** 결과를 출력하거나 테스트할 때 순서가 필요하면 `LinkedHashMap`/`TreeMap`을 쓰세요.

## 5. Queue / Deque

```java
// File: QueueDemo.java
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.PriorityQueue;
import java.util.Queue;

public class QueueDemo {
    public static void main(String[] args) {
        Queue<String> q = new ArrayDeque<>();      // 큐: 먼저 넣은 것이 먼저 나온다
        q.offer("a"); q.offer("b");
        System.out.println(q.poll());              // a
        System.out.println(q.peek());              // b

        Deque<Integer> stack = new ArrayDeque<>(); // 스택: 나중에 넣은 것이 먼저 나온다
        stack.push(1); stack.push(2);
        System.out.println(stack.pop());           // 2

        PriorityQueue<Integer> pq = new PriorityQueue<>();  // 작은 값이 먼저
        pq.add(5); pq.add(1); pq.add(3);
        System.out.println(pq.poll());             // 1
    }
}
```

`Stack` 클래스는 낡은 설계입니다. 스택은 `ArrayDeque`를 쓰세요.

## 6. 정렬: `Comparable`과 `Comparator`

```java
// File: SortDemo.java
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

record Person(String name, int age) {}

public class SortDemo {
    public static void main(String[] args) {
        List<Person> people = new ArrayList<>(List.of(
                new Person("민지", 25),
                new Person("하승", 30),
                new Person("철수", 25)));

        people.sort(Comparator.comparing(Person::age));                 // 나이 오름차순
        System.out.println(people);

        people.sort(Comparator.comparing(Person::age).reversed()        // 나이 내림차순
                .thenComparing(Person::name));                          // 같으면 이름순
        System.out.println(people);

        // 기본 정렬 순서를 클래스가 가지려면 Comparable<T> 구현 (예: String, Integer)
    }
}
```

## 7. 컬렉션 선택 가이드

| 하고 싶은 일 | 선택 |
|--------------|------|
| 순서 있는 목록, 인덱스 접근 | `ArrayList` |
| 중복 제거, 포함 여부 빠르게 확인 | `HashSet` |
| 키로 값 찾기 | `HashMap` |
| 입력 순서 유지 + 키 조회 | `LinkedHashMap` |
| 항상 정렬된 상태 | `TreeSet`, `TreeMap` |
| 선입선출 대기열 / 스택 | `ArrayDeque` |
| 읽기 전용 상수 컬렉션 | `List.of`, `Set.of`, `Map.of` |

## 연습문제

1. 정수 리스트에서 **중복을 제거**하고 오름차순으로 정렬해 출력하세요.
2. 문자열 리스트를 **길이순, 같으면 사전순**으로 정렬하세요 (`Comparator`).
3. 학생 이름과 점수를 `Map<String, Integer>`로 관리하고, **최고점 학생**과 **평균**을 구하세요.
4. 괄호 문자열(`"({[]})"`)이 올바른지 **스택**(`ArrayDeque`)으로 검사하세요.
5. 두 리스트의 **교집합, 합집합, 차집합**을 `Set`으로 구하세요.
6. 9주차 도서 대출 프로젝트를 열어 `Map`을 사용하는 부분을 다시 읽고, `HashMap`이 `TreeMap`/`LinkedHashMap`으로 바뀌면 출력이 어떻게 달라지는지 확인하세요.
7. (도전) 텍스트 파일 대신 문자열 한 덩어리에서 **가장 많이 나온 단어 상위 3개**를 구하세요. (`Map` → `List<Map.Entry>` 정렬)

## 퀴즈

1. `List<Integer> l = ...; l.remove(1)`은 무엇을 삭제하나?
2. `HashMap`의 순회 순서는 보장되나?
3. `Set`에 같은 내용의 객체를 넣었는데 중복 제거가 안 된다. 가장 먼저 의심할 것은?
4. 순회 중 요소를 안전하게 삭제하는 방법은?

<details>
<summary>정답</summary>

1. 인덱스 1의 요소 (값 1이 아니다)
2. 보장되지 않는다
3. `equals`/`hashCode` 재정의 누락 (또는 둘 중 하나만 구현)
4. `Iterator.remove()` 또는 `removeIf` (가장 간단)

</details>

## 체크리스트

- [ ] 상황별로 컬렉션을 고르는 이유를 설명할 수 있다
- [ ] `Map.merge`/`getOrDefault`로 단어 빈도 코드를 작성했다
- [ ] `Comparator.comparing().thenComparing()`을 썼다
- [ ] 순회 중 삭제 예외를 재현하고 고쳤다
