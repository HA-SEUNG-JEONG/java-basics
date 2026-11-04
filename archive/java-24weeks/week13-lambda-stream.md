# 13주차. 람다와 Stream

**예상 시간:** 10시간 | **목표:** 반복문으로 짠 컬렉션 처리를 Stream으로 바꾸고 반대로도 바꿀 수 있다. **마일스톤 점검 주차**입니다.

## 🖐 먼저 손으로 (읽기 전에 하세요)

> 이 주차의 핵심은 아래 단계를 **직접 타이핑하면서** 깨닫는 것입니다. 아래쪽 설명 문서는 먼저 읽지 않아도 됩니다. 막혔을 때 사전처럼 펼쳐 보세요.

### 1단계. 예측하고 → 치고 → 비교하기 (20분)

1. 아래 코드를 **실행하기 전에** 각 줄의 출력을 노트에 먼저 적습니다.
2. 코드를 한 글자씩 타이핑합니다 (복사·붙여넣기 금지). 오타가 나면 컴파일러가 알려 줍니다.
3. 실행해서 결과를 비교합니다. 예측이 틀린 줄에 ★를 치고, **왜 그렇게 나왔는지 한 문장**으로 적습니다. 이 ★가 오늘 배운 진짜 내용입니다.

```java
// File: Predict13.java
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Predict13 {
    public static void main(String[] args) {
        List<Integer> nums = List.of(5, 3, 8, 1, 9, 2);
        System.out.println(nums.stream().filter(n -> n > 2).map(n -> n * 10).toList());
        System.out.println(nums.stream().sorted().limit(3).toList());
        System.out.println(nums.stream().mapToInt(n -> n).sum());
        System.out.println(nums.stream().reduce(1, (a, b) -> a * b));
        System.out.println(nums.stream().anyMatch(n -> n > 8) + " " + nums.stream().allMatch(n -> n > 1));
        System.out.println(Stream.of("bb", "a", "ccc", "dd").collect(Collectors.groupingBy(String::length)));
        System.out.println(IntStream.rangeClosed(1, 4).boxed().map(String::valueOf).collect(Collectors.joining("-")));
        Stream<Integer> s = nums.stream();
        s.count();
        try {
            s.count();
        } catch (IllegalStateException e) {
            System.out.println("reused");
        }
        System.out.println(nums.stream().skip(2).findFirst().get());
    }
}
```

<details>
<summary>실행 결과 (예측을 적은 뒤에 여세요)</summary>

```text
[50, 30, 80, 90]
[1, 2, 3]
28
2160
true false
{1=[a], 2=[bb, dd], 3=[ccc]}
1-2-3-4
reused
8
```

</details>

### 2단계. 일부러 망가뜨리기 (20분)

방금 친 코드(또는 아래 설명 문서의 예제)를 하나씩 **고장 내 보고**, 어떤 메시지가 나오는지 확인합니다. 먼저 어떻게 될지 예상한 뒤 실행하세요. 메시지를 외울 필요는 없습니다. 에러 메시지가 **어떤 모양이고 어디를 가리키는지** 눈에 익히는 것이 목표입니다.

| 이렇게 바꿔 보세요 | 어떻게 될까요? |
|---|---|
| `int base = 10; Function<Integer,Integer> f = x -> x + base; base = 20;` | `local variables referenced from a lambda expression must be final or effectively final` |
| Stream 에 최종 연산(`toList()` 등)을 빼고 `nums.stream().filter(n -> { System.out.println(n); return true; });` | 아무것도 출력되지 않는다. 중간 연산은 **게으르다** |
| `nums.stream().toList().add(1)` | `UnsupportedOperationException` — `toList()` 결과는 불변 |
| 같은 `Stream` 변수에 최종 연산 두 번 | `IllegalStateException: stream has already been operated upon or closed` |
| `Function<Integer,Integer> f = x -> { x + 1; };` | `not a statement` — 블록 람다는 `return` 이 필요 |
| `nums.stream().map(n -> n * 2).forEach(System.out::println)` 를 `nums.forEach` 와 비교 | 결과는 같지만 `map` 이 새 값을 만든다는 차이를 확인 |

### 3단계. 빈칸에서 만들기 — 자동 채점 (1~3시간)

`../../labs/week13/` 폴더의 `Mine.java` (반복문 대신 Stream 으로 풀어야 채점기를 통과합니다)를 열어 `// 여기를 고치세요` 부분을 직접 구현합니다. 구현하고 채점기를 돌려 ✅를 하나씩 늘려 가세요.

```bash
cd labs
./check.sh 13
```

- 처음에는 거의 다 ❌입니다. 정상입니다. **❌ 메시지의 "기대 / 실제"를 읽고** 위에서부터 하나씩 고치세요.
- 컴파일 에러가 나면 첫 번째 에러부터 읽으세요.
- 15분 넘게 막혔을 때만 `../../labs/solutions/week13/`를 열어 보세요. 보고 나서는 **닫고 다시 직접 쳐서** 구현하세요.


---

## 1. 람다식

**메서드가 하나뿐인 인터페이스(함수형 인터페이스)** 를 짧게 구현하는 문법입니다.

```java
// File: LambdaDemo.java
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class LambdaDemo {
    public static void main(String[] args) {
        // 익명 클래스 (예전 방식)
        Comparator<String> old = new Comparator<String>() {
            @Override
            public int compare(String a, String b) { return a.length() - b.length(); }
        };

        // 람다 (같은 의미)
        Comparator<String> byLen = (a, b) -> a.length() - b.length();

        List<String> words = new ArrayList<>(List.of("kiwi", "fig", "banana"));
        words.sort(byLen);
        System.out.println(words);                     // [fig, kiwi, banana]

        // 표준 함수형 인터페이스 4종
        Function<String, Integer> len = s -> s.length();        // T -> R
        Predicate<String> isLong = s -> s.length() > 4;         // T -> boolean
        Consumer<String> printer = s -> System.out.println(s);  // T -> void
        Supplier<String> hello = () -> "hello";                 // () -> T
        BiFunction<Integer, Integer, Integer> add = (a, b) -> a + b;

        System.out.println(len.apply("자바"));                    // 2
        System.out.println(isLong.test("banana"));               // true
        printer.accept("출력");
        System.out.println(hello.get());
        System.out.println(add.apply(2, 3));

        // 메서드 참조: 람다가 메서드 하나를 그대로 호출할 때의 축약형
        Function<String, Integer> len2 = String::length;         // s -> s.length()
        Consumer<String> printer2 = System.out::println;         // s -> System.out.println(s)
        words.forEach(printer2);

        // 람다가 쓰는 지역 변수는 사실상 final이어야 한다
        int base = 10;
        Function<Integer, Integer> plus = x -> x + base;
        // base = 20;                                           // 컴파일 에러: 람다 캡처 후 변경 불가
        System.out.println(plus.apply(1));                       // 11
    }
}
```

> **JS와 비교:** 화살표 함수 `(a, b) => a + b`와 문법이 거의 같습니다(`->`). 차이는 람다가 반드시 **함수형 인터페이스 타입**으로만 쓰인다는 점입니다 (JS처럼 독립적인 함수 값이 아님).

## 2. Stream이란

컬렉션 데이터를 **선언적으로 처리하는 파이프라인**입니다.

```
데이터 소스 -> 중간 연산(filter, map, sorted ...) -> 최종 연산(collect, count, forEach ...)
```

- 중간 연산은 **게으르게** 실행됩니다. 최종 연산이 호출될 때 비로소 처리됩니다.
- Stream은 **원본 컬렉션을 수정하지 않습니다.** 한 번 쓰면 재사용할 수 없습니다.

```java
// File: StreamBasics.java
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class StreamBasics {
    public static void main(String[] args) {
        List<Integer> nums = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        // 반복문 버전: 짝수만 제곱해서 모으기
        // List<Integer> result = new ArrayList<>();
        // for (int n : nums) if (n % 2 == 0) result.add(n * n);

        List<Integer> result = nums.stream()
                .filter(n -> n % 2 == 0)       // 조건에 맞는 것만
                .map(n -> n * n)               // 변환
                .toList();                     // 불변 리스트로 수집 (Java 16+)
        System.out.println(result);            // [4, 16, 36, 64, 100]

        System.out.println(nums.stream().mapToInt(Integer::intValue).sum());   // 55
        System.out.println(nums.stream().mapToInt(n -> n).average().orElse(0)); // 5.5
        System.out.println(nums.stream().anyMatch(n -> n > 9));                // true
        System.out.println(nums.stream().count());                             // 10
        System.out.println(nums.stream().reduce(0, Integer::sum));             // 55

        System.out.println(IntStream.rangeClosed(1, 5).boxed().toList());       // [1, 2, 3, 4, 5]
        System.out.println(
                List.of("a", "b", "c").stream().collect(Collectors.joining("-"))); // a-b-c
    }
}
```

## 3. 자주 쓰는 연산 정리

| 분류 | 연산 | 의미 |
|------|------|------|
| 중간 | `filter` | 조건으로 거르기 |
| 중간 | `map` | 요소를 다른 값으로 변환 |
| 중간 | `flatMap` | 중첩된 스트림 펼치기 |
| 중간 | `sorted` | 정렬 (`Comparator` 가능) |
| 중간 | `distinct` / `limit` / `skip` | 중복 제거 / 개수 제한 / 건너뛰기 |
| 최종 | `toList` / `collect` | 결과 모으기 |
| 최종 | `count` / `sum` / `min` / `max` | 집계 |
| 최종 | `anyMatch` / `allMatch` / `noneMatch` | 조건 검사 |
| 최종 | `findFirst` | 첫 요소 (`Optional`) |
| 최종 | `forEach` | 각 요소에 작업 |

기본형 전용 스트림 `IntStream`, `LongStream`, `DoubleStream`은 박싱 비용 없이 `sum()`, `average()` 등을 제공합니다 (`mapToInt`로 변환).

## 4. 실전 예제: 주문 데이터 분석

```java
// File: StreamOrders.java
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

record Order(String customer, String category, int price) {}

public class StreamOrders {
    public static void main(String[] args) {
        List<Order> orders = List.of(
                new Order("하승", "도서", 15_000),
                new Order("민지", "전자", 80_000),
                new Order("하승", "전자", 120_000),
                new Order("철수", "도서", 22_000),
                new Order("민지", "도서", 9_000));

        // 1) 5만원 이상 주문의 고객 이름 (중복 제거, 정렬)
        System.out.println(orders.stream()
                .filter(o -> o.price() >= 50_000)
                .map(Order::customer)
                .distinct()
                .sorted()
                .toList());                                       // [민지, 하승]

        // 2) 카테고리별 매출 합계
        Map<String, Integer> byCategory = orders.stream()
                .collect(Collectors.groupingBy(Order::category,
                        Collectors.summingInt(Order::price)));
        System.out.println(byCategory.get("도서"));                // 46000
        System.out.println(byCategory.get("전자"));                // 200000

        // 3) 고객별 주문 건수
        Map<String, Long> countByCustomer = orders.stream()
                .collect(Collectors.groupingBy(Order::customer, Collectors.counting()));
        System.out.println(countByCustomer.get("하승"));            // 2

        // 4) 가장 비싼 주문
        orders.stream()
                .max(Comparator.comparingInt(Order::price))
                .ifPresent(o -> System.out.println(o.customer() + " " + o.price()));

        // 5) 조건으로 둘로 나누기
        Map<Boolean, List<Order>> split = orders.stream()
                .collect(Collectors.partitioningBy(o -> o.price() >= 20_000));
        System.out.println(split.get(true).size() + " / " + split.get(false).size()); // 3 / 2
    }
}
```

## 5. Stream을 쓰지 말아야 할 때

- 단순히 요소 하나를 꺼내는 일 → 일반 `for`/`get`이 더 읽기 쉽습니다.
- 파이프라인이 4~5단계를 넘고 람다 안이 길어질 때 → 메서드로 빼거나 반복문을 쓰세요.
- 순회 중 **외부 변수를 수정**하는 것은 피하세요 (`forEach` 안에서 합계 변수 수정 등). `sum`, `reduce`, `collect`를 쓰세요.
- 디버깅이 필요하면 `peek(System.out::println)`을 임시로 끼우세요.

Stream은 가독성 도구입니다. 성능 때문에 쓰는 것이 아닙니다.

## 연습문제

1. 정수 리스트에서 **3의 배수의 합**을 Stream으로 구하세요.
2. 문자열 리스트에서 **길이가 3 이상인 단어를 대문자로 바꿔** 사전순으로 정렬한 리스트를 만드세요.
3. 11주차의 `WordCount`를 Stream으로 바꿔 보세요. (`Arrays.stream(text.split(" ")).collect(groupingBy(w -> w, counting()))`)
4. `Order` 예제에서 **고객별 총 결제액**을 구하고, 가장 많이 쓴 고객을 찾으세요.
5. `Map<String, Integer>`(과목 → 점수)에서 **평균 이상인 과목명**만 뽑으세요.
6. 반복문으로 짠 코드를 Stream으로, Stream으로 짠 코드를 반복문으로 바꿔 보는 연습을 각각 3개씩 하세요.
7. 직접 `Function<Integer, Integer>`를 `andThen`으로 이어 붙인 `(x → x*2) → (x → x+1)`의 결과를 확인하세요.

## 마일스톤 점검 (13주)

다음을 설명 없이 할 수 있어야 합니다.

- [ ] `for` + `if` + 결과 리스트에 `add`하는 코드를 `filter`/`map`/`toList`로 바꾼다
- [ ] 같은 코드를 다시 반복문으로 바꾼다
- [ ] `groupingBy`로 그룹 집계를 한다
- [ ] 람다에서 쓸 수 있는 지역 변수의 제약을 설명한다
- [ ] 9주차 프로젝트의 목록 처리 일부를 Stream으로 리팩토링한다

## 퀴즈

1. 함수형 인터페이스란?
2. 중간 연산과 최종 연산의 차이는? 최종 연산이 없으면 어떻게 되나?
3. `list.stream().map(String::length)`에서 `String::length`는 어떤 람다와 같은가?
4. 한 번 소비한 Stream을 다시 쓰면?

<details>
<summary>정답</summary>

1. 추상 메서드가 하나뿐인 인터페이스 (`Function`, `Predicate`, `Runnable`, `Comparator` 등)
2. 중간 연산은 Stream을 돌려주고 게으르게 실행되며, 최종 연산이 호출돼야 실제 처리가 일어난다. 최종 연산이 없으면 아무 일도 일어나지 않는다
3. `s -> s.length()`
4. `IllegalStateException` ("stream has already been operated upon or closed")

</details>

## 체크리스트

- [ ] 표준 함수형 인터페이스 4종(`Function`, `Predicate`, `Consumer`, `Supplier`)을 구분한다
- [ ] 메서드 참조 문법을 읽을 수 있다
- [ ] `groupingBy`/`counting`/`summingInt`를 썼다
- [ ] Stream을 쓰지 않는 편이 나은 경우를 안다
