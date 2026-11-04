# 7주차. 인터페이스·enum·record

**예상 시간:** 10시간 | **목표:** 구현이 아니라 **계약(인터페이스)** 에 의존하는 설계를 익히고, enum·record·sealed로 데이터를 간결하게 모델링한다.

## 🖐 먼저 손으로 (읽기 전에 하세요)

> 이 주차의 핵심은 아래 단계를 **직접 타이핑하면서** 깨닫는 것입니다. 아래쪽 설명 문서는 먼저 읽지 않아도 됩니다. 막혔을 때 사전처럼 펼쳐 보세요.

### 1단계. 예측하고 → 치고 → 비교하기 (20분)

1. 아래 코드를 **실행하기 전에** 각 줄의 출력을 노트에 먼저 적습니다.
2. 코드를 한 글자씩 타이핑합니다 (복사·붙여넣기 금지). 오타가 나면 컴파일러가 알려 줍니다.
3. 실행해서 결과를 비교합니다. 예측이 틀린 줄에 ★를 치고, **왜 그렇게 나왔는지 한 문장**으로 적습니다. 이 ★가 오늘 배운 진짜 내용입니다.

```java
// File: Predict07.java
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class Predict07 {
    enum Level { LOW, MID, HIGH }

    record P(int x, int y) {}

    interface Greeter {
        String name();
        default String greet() { return "hi " + name(); }
    }

    public static void main(String[] args) {
        System.out.println(Level.valueOf("MID").ordinal());
        System.out.println(Level.HIGH.compareTo(Level.LOW));
        System.out.println(Arrays.toString(Level.values()));
        P p = new P(1, 2);
        System.out.println(p);
        System.out.println(p.equals(new P(1, 2)));
        System.out.println(p == new P(1, 2));
        System.out.println(new HashSet<>(List.of(p, new P(1, 2))).size());
        Greeter g = () -> "kim";
        System.out.println(g.greet());
    }
}
```

<details>
<summary>실행 결과 (예측을 적은 뒤에 여세요)</summary>

```text
1
2
[LOW, MID, HIGH]
P[x=1, y=2]
true
false
1
hi kim
```

</details>

### 2단계. 일부러 망가뜨리기 (20분)

방금 친 코드(또는 아래 설명 문서의 예제)를 하나씩 **고장 내 보고**, 어떤 메시지가 나오는지 확인합니다. 먼저 어떻게 될지 예상한 뒤 실행하세요. 메시지를 외울 필요는 없습니다. 에러 메시지가 **어떤 모양이고 어디를 가리키는지** 눈에 익히는 것이 목표입니다.

| 이렇게 바꿔 보세요 | 어떻게 될까요? |
|---|---|
| 인터페이스를 구현하는 메서드 앞의 `public` 지우기 | `attempting to assign weaker access privileges; was public` |
| `switch (level)` 표현식에서 `case HIGH` 한 줄 지우기 (default 없이) | `the switch expression does not cover all possible input values` |
| `sealed interface Shape permits Circle, Square` 인데 `record Tri() implements Shape` 추가 | `class is not allowed to extend sealed class` — 허용 목록 밖은 막힌다 |
| record 에서 `p.getX()` 호출 | `cannot find symbol` — record 의 접근자는 `x()` (get 접두사 없음) |
| `Level.valueOf("NONE")` | `IllegalArgumentException: No enum constant ...` |
| `record P(int x, int y)` 안에서 `x = 5;` 로 필드 변경 시도 | `cannot assign a value to final variable x` — record 는 불변이다 |

### 3단계. 빈칸에서 만들기 — 자동 채점 (1~3시간)

`../../labs/week07/` 폴더의 `PercentDiscount`, `FixedDiscount`, `Cart`, `Day`, `Money`, `Eval`를 열어 `// 여기를 고치세요` 부분을 직접 구현합니다. 구현하고 채점기를 돌려 ✅를 하나씩 늘려 가세요.

```bash
cd labs
./check.sh 07
```

- 처음에는 거의 다 ❌입니다. 정상입니다. **❌ 메시지의 "기대 / 실제"를 읽고** 위에서부터 하나씩 고치세요.
- 컴파일 에러가 나면 첫 번째 에러부터 읽으세요.
- 15분 넘게 막혔을 때만 `../../labs/solutions/week07/`를 열어 보세요. 보고 나서는 **닫고 다시 직접 쳐서** 구현하세요.


---

## 1. 인터페이스

인터페이스는 "이 메서드들을 반드시 제공한다"는 **약속**입니다. 구현 방법은 알려 주지 않습니다.

```java
// File: InterfaceDemo.java
interface Notifier {
    void send(String to, String message);

    // default 메서드: 구현을 제공할 수 있다 (Java 8+)
    default void sendAll(java.util.List<String> targets, String message) {
        for (String t : targets) {
            send(t, message);
        }
    }
}

class EmailNotifier implements Notifier {
    @Override
    public void send(String to, String message) {      // 인터페이스 메서드는 public
        System.out.println("[이메일] " + to + " <- " + message);
    }
}

class SmsNotifier implements Notifier {
    @Override
    public void send(String to, String message) {
        System.out.println("[SMS] " + to + " <- " + message);
    }
}

class OrderService {
    private final Notifier notifier;       // 구체 클래스가 아니라 인터페이스에 의존

    OrderService(Notifier notifier) {
        this.notifier = notifier;
    }

    void complete(String customer) {
        notifier.send(customer, "주문이 완료되었습니다");
    }
}

public class InterfaceDemo {
    public static void main(String[] args) {
        new OrderService(new EmailNotifier()).complete("a@x.com");
        new OrderService(new SmsNotifier()).complete("010-1234-5678");

        Notifier n = new EmailNotifier();
        n.sendAll(java.util.List.of("b@x.com", "c@x.com"), "공지");
    }
}
```

`OrderService`는 `EmailNotifier`가 있는지도 모릅니다. 알림 방식을 바꿔도 `OrderService`는 수정하지 않습니다. 6주차 연습문제 5번의 답이 바로 이것입니다. 또한 Spring의 DI(의존성 주입)가 이 구조 위에서 동작합니다.

### 추상 클래스와의 선택 기준

| | 인터페이스 | 추상 클래스 |
|--|-----------|-------------|
| 다중 구현/상속 | 여러 개 `implements` 가능 | `extends` 하나만 |
| 상태(필드) | 상수만 | 인스턴스 필드 가능 |
| 용도 | 능력·역할의 계약 | 공통 상태·로직을 공유하는 "같은 종류" |

기본값은 **인터페이스**를 먼저 고려하세요.

## 2. enum

정해진 값의 집합을 타입으로 만듭니다. 문자열 상수(`"ADMIN"`)를 쓰다가 오타를 내는 일이 사라집니다.

```java
// File: EnumDemo.java
enum Level {
    LOW(1), MEDIUM(5), HIGH(10);

    private final int weight;

    Level(int weight) { this.weight = weight; }

    int weight() { return weight; }
}

public class EnumDemo {
    public static void main(String[] args) {
        Level lv = Level.HIGH;
        System.out.println(lv);                     // HIGH
        System.out.println(lv.weight());            // 10
        System.out.println(Level.valueOf("LOW"));   // 문자열 -> enum (없으면 예외)
        System.out.println(lv.ordinal());           // 2 (선언 순서)

        for (Level l : Level.values()) {
            System.out.println(l + " " + l.weight());
        }

        String text = switch (lv) {                 // enum switch는 모든 값을 다루면 default 불필요
            case LOW -> "낮음";
            case MEDIUM -> "보통";
            case HIGH -> "높음";
        };
        System.out.println(text);
    }
}
```

## 3. record

값만 담는 불변 객체를 한 줄로 만듭니다. 생성자, getter, `equals`, `hashCode`, `toString`이 자동 생성됩니다.

```java
// File: RecordDemo.java
record Point(int x, int y) {
    // 컴팩트 생성자: 유효성 검사
    Point {
        if (x < 0 || y < 0) {
            throw new IllegalArgumentException("좌표는 0 이상이어야 합니다");
        }
    }

    // 메서드도 추가할 수 있다
    double distanceTo(Point other) {
        int dx = x - other.x;
        int dy = y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
}

public class RecordDemo {
    public static void main(String[] args) {
        Point a = new Point(0, 0);
        Point b = new Point(3, 4);
        System.out.println(b);                         // Point[x=3, y=4]
        System.out.println(b.x());                     // getter는 x() (get 접두사 없음)
        System.out.println(a.distanceTo(b));           // 5.0
        System.out.println(b.equals(new Point(3, 4))); // true: 내용 비교
    }
}
```

DTO(데이터 전달 객체)나 값 객체는 `record`로 만드는 것이 기본입니다. 5주차의 `Point` 클래스 10줄이 `record` 한 줄과 같습니다.

## 4. sealed 인터페이스 + 패턴 매칭 switch (Java 21)

"허용된 구현체만 존재한다"고 선언하면, `switch`가 **빠진 경우를 컴파일러가 알려 줍니다.**

```java
// File: SealedDemo.java
sealed interface Shape permits Circle, Square {}
record Circle(double r) implements Shape {}
record Square(double side) implements Shape {}

public class SealedDemo {
    static double area(Shape s) {
        return switch (s) {                                  // default 없이도 완전성 검사
            case Circle c -> Math.PI * c.r() * c.r();
            case Square q -> q.side() * q.side();
        };
    }

    public static void main(String[] args) {
        System.out.printf("%.2f%n", area(new Circle(1)));    // 3.14
        System.out.printf("%.2f%n", area(new Square(2)));    // 4.00
    }
}
```

`Triangle`을 `permits`에 추가하면 `area()`의 `switch`에서 컴파일 에러가 납니다. 이것이 "수정 누락을 컴파일러가 잡아 주는" 안전장치입니다. 6주차의 열린 다형성(자식 클래스를 자유롭게 추가)과 달리, 종류가 **닫혀 있을 때** 적합합니다.

## 5. 정리: 무엇을 언제 쓸까

| 상황 | 선택 |
|------|------|
| 여러 구현을 갈아 끼울 수 있는 역할 | `interface` |
| 정해진 값 몇 개 | `enum` |
| 데이터를 담기만 하는 불변 객체 | `record` |
| 종류가 닫혀 있고 모두 처리를 강제하고 싶다 | `sealed` + `switch` |

## 연습문제

1. `Discount` 인터페이스(`int apply(int price)`)와 `PercentDiscount`, `FixedDiscount` 구현체를 만들고, 장바구니가 `Discount`에만 의존하도록 하세요.
2. 요일을 나타내는 `enum Day`를 만들고, 주말 여부를 알려 주는 `isWeekend()`를 추가하세요.
3. `record Money(long amount, String currency)`를 만들고 `add(Money)`를 구현하세요. 통화가 다르면 예외를 던집니다.
4. `sealed interface Expr`에 `Num(int)`, `Add(Expr, Expr)`, `Mul(Expr, Expr)`를 만들고, 재귀적으로 값을 계산하는 `eval(Expr)`을 `switch` 패턴 매칭으로 작성하세요. `Add(Num(1), Mul(Num(2), Num(3)))`의 결과는 7입니다.
5. `Comparable<Student>`를 구현해 점수순으로 정렬되게 하고, `Arrays.sort`로 확인하세요. (인터페이스가 실제 표준 라이브러리에서 쓰이는 예입니다)

## 퀴즈

1. 인터페이스가 추상 클래스보다 선호되는 이유 하나는?
2. `record`가 자동으로 만들어 주는 것 네 가지는?
3. `sealed` + `switch`의 장점은?
4. `enum`의 `valueOf("없는값")`은?

<details>
<summary>정답</summary>

1. 여러 개를 구현할 수 있고, 구현과 분리된 의존성을 만들기 쉬워서
2. 생성자, getter(접근자), `equals`/`hashCode`, `toString` (필드는 `final`)
3. 허용된 모든 경우를 처리했는지 컴파일러가 검사해, 새 종류 추가 시 누락을 잡아 준다
4. `IllegalArgumentException`

</details>

## 체크리스트

- [ ] 인터페이스에 의존하는 클래스를 직접 설계했다
- [ ] `enum`에 필드와 메서드를 넣어 봤다
- [ ] `record`로 값 객체를 만들었다
- [ ] 패턴 매칭 `switch`로 재귀 계산기를 작성했다
