# 12주차. 제네릭과 Optional

**예상 시간:** 8시간 | **목표:** 컬렉션의 `<T>`가 무엇을 보장하는지 이해하고, 간단한 제네릭 클래스·메서드를 직접 만들며, `null` 대신 `Optional`을 쓸 줄 안다.

## 🖐 먼저 손으로 (읽기 전에 하세요)

> 이 주차의 핵심은 아래 단계를 **직접 타이핑하면서** 깨닫는 것입니다. 아래쪽 설명 문서는 먼저 읽지 않아도 됩니다. 막혔을 때 사전처럼 펼쳐 보세요.

### 1단계. 예측하고 → 치고 → 비교하기 (20분)

1. 아래 코드를 **실행하기 전에** 각 줄의 출력을 노트에 먼저 적습니다.
2. 코드를 한 글자씩 타이핑합니다 (복사·붙여넣기 금지). 오타가 나면 컴파일러가 알려 줍니다.
3. 실행해서 결과를 비교합니다. 예측이 틀린 줄에 ★를 치고, **왜 그렇게 나왔는지 한 문장**으로 적습니다. 이 ★가 오늘 배운 진짜 내용입니다.

```java
// File: Predict12.java
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Predict12 {
    static <T extends Comparable<T>> T max(T a, T b) {
        return a.compareTo(b) >= 0 ? a : b;
    }

    public static void main(String[] args) {
        System.out.println(max(3, 7));
        System.out.println(max("pear", "apple"));
        List<String> a = new ArrayList<>();
        List<Integer> b = new ArrayList<>();
        System.out.println(a.getClass() == b.getClass());
        Optional<String> o = Optional.of("x");
        System.out.println(o.map(String::toUpperCase).orElse("none"));
        System.out.println(Optional.empty().isPresent());
        System.out.println(Optional.ofNullable((String) null).map(String::length).orElse(-1));
        try {
            Optional.of(null);
        } catch (NullPointerException e) {
            System.out.println("NPE");
        }
        List<? extends Number> nums = List.of(1, 2.5);
        System.out.println(nums.get(0).doubleValue() + nums.get(1).doubleValue());
    }
}
```

<details>
<summary>실행 결과 (예측을 적은 뒤에 여세요)</summary>

```text
7
pear
true
X
false
-1
NPE
3.5
```

</details>

### 2단계. 일부러 망가뜨리기 (20분)

방금 친 코드(또는 아래 설명 문서의 예제)를 하나씩 **고장 내 보고**, 어떤 메시지가 나오는지 확인합니다. 먼저 어떻게 될지 예상한 뒤 실행하세요. 메시지를 외울 필요는 없습니다. 에러 메시지가 **어떤 모양이고 어디를 가리키는지** 눈에 익히는 것이 목표입니다.

| 이렇게 바꿔 보세요 | 어떻게 될까요? |
|---|---|
| `List<String> l = new ArrayList<>(); l.add(1);` | `incompatible types` / `no suitable method found for add(int)` — 실행하기 전에 막힌다 |
| `List<Object> lo = new ArrayList<String>();` | `incompatible types` — String 이 Object 의 자식이어도 `List<String>` 은 `List<Object>` 가 아니다 |
| `max(new Object(), new Object())` | `method max in class ... cannot be applied to given types` — `Object` 는 `Comparable` 이 아니라서 거절 (아래쪽 `reason:` 줄을 읽어 보세요) |
| `Optional.empty().get()` | `NoSuchElementException: No value present` |
| `List raw = new ArrayList(); raw.add(1);` 후 `javac` | `Note: ... uses unchecked or unsafe operations.` — raw 타입을 쓰지 말라는 경고 |
| 제네릭 클래스 안에서 `T item = new T();` | 컴파일 에러. 타입 소거 때문에 런타임에는 T 가 무엇인지 모른다 |

### 3단계. 빈칸에서 만들기 — 자동 채점 (1~3시간)

`../../labs/week12/` 폴더의 `MyStack`, `Cache`, `Util`를 열어 `// 여기를 고치세요` 부분을 직접 구현합니다. 구현하고 채점기를 돌려 ✅를 하나씩 늘려 가세요.

```bash
cd labs
./check.sh 12
```

- 처음에는 거의 다 ❌입니다. 정상입니다. **❌ 메시지의 "기대 / 실제"를 읽고** 위에서부터 하나씩 고치세요.
- 컴파일 에러가 나면 첫 번째 에러부터 읽으세요.
- 15분 넘게 막혔을 때만 `../../labs/solutions/week12/`를 열어 보세요. 보고 나서는 **닫고 다시 직접 쳐서** 구현하세요.


---

## 1. 제네릭이 필요한 이유

제네릭이 없던 시절에는 아무 객체나 담는 컬렉션을 썼습니다.

```java
List raw = new ArrayList();      // 타입 지정 없음 (raw type): 쓰지 마세요
raw.add("문자열");
raw.add(123);
String s = (String) raw.get(1);  // 컴파일은 되지만 실행하면 ClassCastException
```

제네릭은 **타입 오류를 컴파일 시점으로 당겨 옵니다.**

```java
List<String> list = new ArrayList<>();
list.add(123);                   // 컴파일 에러: 실행하기 전에 알 수 있다
String s = list.get(0);          // 캐스팅이 필요 없다
```

> **JS/TS와 비교:** TypeScript의 `Array<string>`과 비슷합니다. 다만 자바 제네릭은 컴파일 후 타입 정보가 **지워집니다**(타입 소거). 그래서 `new T()`나 `T[]` 생성이 안 되고, 런타임에 `List<String>`과 `List<Integer>`를 구분할 수 없습니다.

## 2. 제네릭 클래스

```java
// File: GenericClassDemo.java
class Box<T> {
    private T value;

    Box(T value) { this.value = value; }

    T get() { return value; }
    void set(T value) { this.value = value; }
}

// 타입 매개변수 둘
record Pair<A, B>(A first, B second) {
    Pair<B, A> swap() { return new Pair<>(second, first); }
}

public class GenericClassDemo {
    public static void main(String[] args) {
        Box<String> s = new Box<>("hello");   // 다이아몬드 <>: 우변 타입 생략
        String v = s.get();
        System.out.println(v);

        Box<Integer> n = new Box<>(10);
        // n.set("문자열");                    // 컴파일 에러

        Pair<String, Integer> p = new Pair<>("나이", 30);
        System.out.println(p.swap());          // Pair[first=30, second=나이]
    }
}
```

타입 매개변수 이름 관례: `T`(Type), `E`(Element), `K`(Key), `V`(Value), `R`(Return).

## 3. 제네릭 메서드와 제한

```java
// File: GenericMethodDemo.java
import java.util.List;

public class GenericMethodDemo {
    // <T> 가 메서드 앞에 온다
    static <T> T firstOrNull(List<T> list) {
        return list.isEmpty() ? null : list.get(0);
    }

    // 타입 제한: T는 Comparable<T>를 구현한 타입이어야 한다
    static <T extends Comparable<T>> T max(List<T> list) {
        T best = list.get(0);
        for (T item : list) {
            if (item.compareTo(best) > 0) best = item;
        }
        return best;
    }

    public static void main(String[] args) {
        System.out.println(firstOrNull(List.of("a", "b")));       // a
        System.out.println(max(List.of(3, 9, 4)));                // 9
        System.out.println(max(List.of("apple", "pear", "fig"))); // pear
        // max(List.of(new Object()));                            // 컴파일 에러: Comparable이 아님
    }
}
```

## 4. 와일드카드 `?`

`List<Integer>`는 `List<Number>`의 하위 타입이 **아닙니다.** (`Integer`가 `Number`의 자식이어도 제네릭 타입은 그렇지 않습니다.) 이를 해결하는 것이 와일드카드입니다.

```java
// File: WildcardDemo.java
import java.util.ArrayList;
import java.util.List;

public class WildcardDemo {
    // ? extends Number: Number 또는 그 자식들의 리스트를 "읽기만" 한다 (Producer)
    static double sum(List<? extends Number> nums) {
        double total = 0;
        for (Number n : nums) total += n.doubleValue();
        return total;
    }

    // ? super Integer: Integer 또는 그 부모들의 리스트에 "쓰기만" 한다 (Consumer)
    static void fill(List<? super Integer> out) {
        for (int i = 1; i <= 3; i++) out.add(i);
    }

    public static void main(String[] args) {
        System.out.println(sum(List.of(1, 2, 3)));         // 6.0
        System.out.println(sum(List.of(1.5, 2.5)));        // 4.0

        List<Number> numbers = new ArrayList<>();
        fill(numbers);
        System.out.println(numbers);                        // [1, 2, 3]
    }
}
```

기억법 **PECS**: Producer는 `extends`, Consumer는 `super`. 처음엔 깊이 외우지 말고, 라이브러리 시그니처(`Collections.sort`, `Stream.map` 등)에서 마주칠 때 읽을 수 있으면 충분합니다.

## 5. Optional — null 대신 "값이 없을 수 있음"을 타입으로

`null`은 자바 버그의 가장 큰 원인입니다. `Optional<T>`는 "값이 있을 수도, 없을 수도 있다"는 사실을 **반환 타입에 드러냅니다.**

```java
// File: OptionalDemo.java
import java.util.List;
import java.util.Optional;

record User(String name, String email) {}

public class OptionalDemo {
    static final List<User> USERS = List.of(
            new User("하승", "h@x.com"),
            new User("민지", null));

    static Optional<User> findByName(String name) {
        return USERS.stream().filter(u -> u.name().equals(name)).findFirst();
    }

    public static void main(String[] args) {
        Optional<User> found = findByName("하승");

        System.out.println(found.isPresent());                     // true
        System.out.println(found.map(User::email).orElse("없음"));  // h@x.com

        // 값이 없는 경우
        Optional<User> none = findByName("철수");
        System.out.println(none.map(User::name).orElse("없는 사용자")); // 없는 사용자
        System.out.println(none.isEmpty());                         // true

        // null일 수 있는 값을 감싸기: ofNullable
        String email = findByName("민지").map(User::email).orElse("이메일 없음");
        System.out.println(email);                                  // map 결과가 null이면 empty가 된다

        // 없으면 예외로
        try {
            findByName("철수").orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }

        // 있을 때만 실행
        findByName("하승").ifPresent(u -> System.out.println("찾음: " + u.name()));
    }
}
```

### Optional 사용 규칙

1. **메서드의 반환 타입**에 쓰세요: "결과가 없을 수 있다"를 호출자에게 알립니다.
2. 필드, 매개변수, 컬렉션 요소로 쓰지 마세요. 오히려 코드가 복잡해집니다.
3. `opt.get()`을 `isPresent()` 확인 없이 바로 호출하지 마세요. `orElse`, `orElseThrow`, `map`을 쓰세요.
4. 컬렉션이 비어 있는 경우는 `null`/`Optional`이 아니라 **빈 리스트**를 돌려주세요.
5. `Optional.of(null)`은 NPE입니다. null일 수도 있으면 `Optional.ofNullable`.

## 연습문제

1. `Stack<T>` 제네릭 클래스를 직접 만드세요 (`ArrayList<T>` 내부 사용). `push`, `pop`, `peek`, `isEmpty`.
2. `static <T> void swap(T[] arr, int i, int j)` 제네릭 메서드를 작성하고 `String[]`, `Integer[]`에 각각 써 보세요.
3. `static <T extends Comparable<T>> List<T> sortedCopy(List<T> list)`를 만드세요. 원본은 수정하지 않습니다.
4. `Cache<K, V>` 클래스를 만드세요. `put`, `Optional<V> get(K key)`, `size()`를 가집니다.
5. 11주차의 단어 빈도 코드에서, 단어가 없을 때 `Optional.empty()`를 돌려주는 `Optional<Integer> countOf(String word)`를 만드세요.
6. 컴파일 에러를 읽는 연습: 아래 코드가 왜 에러인지 설명하세요.
   ```java
   List<Integer> ints = List.of(1, 2);
   List<Number> nums = ints;
   ```

## 퀴즈

1. `List<Object> l = new ArrayList<String>();`이 컴파일 에러인 이유는?
2. `<T extends Comparable<T>>`는 무엇을 보장하나?
3. 타입 소거(type erasure)란?
4. 다음 중 `Optional` 사용 위치로 적절한 것은? (a) 메서드 반환 타입 (b) 엔티티 필드 (c) 메서드 매개변수

<details>
<summary>정답</summary>

1. 제네릭은 불공변(invariant)이다. `ArrayList<String>`은 `List<Object>`가 아니다
2. `T`가 서로 비교 가능(`compareTo`)한 타입임을 보장해서 정렬·최댓값에 쓸 수 있다
3. 컴파일 후 제네릭 타입 정보가 지워져, 런타임에는 `List`만 남는 것
4. (a)

</details>

## 체크리스트

- [ ] 제네릭 클래스와 제네릭 메서드를 각각 한 개 이상 만들었다
- [ ] `? extends`와 `? super`를 코드에서 읽을 수 있다
- [ ] `Optional.get()` 대신 `map`/`orElse`/`orElseThrow`를 썼다
- [ ] `null`을 반환하던 코드를 `Optional`로 바꿔 봤다
