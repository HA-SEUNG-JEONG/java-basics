# 5주차. 클래스와 객체

**예상 시간:** 10시간 | **목표:** 데이터와 동작을 하나의 클래스로 묶고, 캡슐화·`static`·참조의 개념을 설명할 수 있다.

## 🖐 먼저 손으로 (읽기 전에 하세요)

> 이 주차의 핵심은 아래 단계를 **직접 타이핑하면서** 깨닫는 것입니다. 아래쪽 설명 문서는 먼저 읽지 않아도 됩니다. 막혔을 때 사전처럼 펼쳐 보세요.

### 1단계. 예측하고 → 치고 → 비교하기 (20분)

1. 아래 코드를 **실행하기 전에** 각 줄의 출력을 노트에 먼저 적습니다.
2. 코드를 한 글자씩 타이핑합니다 (복사·붙여넣기 금지). 오타가 나면 컴파일러가 알려 줍니다.
3. 실행해서 결과를 비교합니다. 예측이 틀린 줄에 ★를 치고, **왜 그렇게 나왔는지 한 문장**으로 적습니다. 이 ★가 오늘 배운 진짜 내용입니다.

```java
// File: Predict05.java
public class Predict05 {
    static class Box {
        int v;
        static int created = 0;

        Box(int v) {
            this.v = v;
            created++;
        }
    }

    static void bump(Box b) { b.v++; }
    static void replace(Box b) { b = new Box(100); }

    public static void main(String[] args) {
        Box a = new Box(1);
        Box b = a;
        b.v = 10;
        System.out.println(a.v);
        bump(a);
        System.out.println(a.v);
        replace(a);
        System.out.println(a.v);
        System.out.println(a == b);
        System.out.println(a == new Box(11));
        System.out.println(Box.created);
        Box n = null;
        try {
            System.out.println(n.v);
        } catch (NullPointerException e) {
            System.out.println("NPE");
        }
    }
}
```

<details>
<summary>실행 결과 (예측을 적은 뒤에 여세요)</summary>

```text
10
11
11
true
false
3
NPE
```

</details>

### 2단계. 일부러 망가뜨리기 (20분)

방금 친 코드(또는 아래 설명 문서의 예제)를 하나씩 **고장 내 보고**, 어떤 메시지가 나오는지 확인합니다. 먼저 어떻게 될지 예상한 뒤 실행하세요. 메시지를 외울 필요는 없습니다. 에러 메시지가 **어떤 모양이고 어디를 가리키는지** 눈에 익히는 것이 목표입니다.

| 이렇게 바꿔 보세요 | 어떻게 될까요? |
|---|---|
| 다른 클래스에서 `account.balance = 1000;` (필드가 private) | `balance has private access in Account` |
| 생성자의 `this.owner = owner;` 를 `owner = owner;` 로 | 컴파일 에러가 **없다**. 필드는 영원히 `null` — 가장 흔한 입문자 버그. 왜 그럴까요? |
| `main` 에서 인스턴스 메서드를 객체 없이 호출 | `non-static method ... cannot be referenced from a static context` |
| `final` 필드에 두 번 대입 | `cannot assign a value to final variable` |
| 생성자를 `Account(String owner)` 하나만 두고 `new Account()` | `constructor Account in class Account cannot be applied to given types` |
| `Account acc = null; acc.deposit(1);` | `NullPointerException` — 5주차 이후 가장 많이 만날 예외 |

### 3단계. 빈칸에서 만들기 — 자동 채점 (1~3시간)

`../../labs/week05/` 폴더의 `Account.java`, `IntStack.java`를 열어 `// 여기를 고치세요` 부분을 직접 구현합니다. 구현하고 채점기를 돌려 ✅를 하나씩 늘려 가세요.

```bash
cd labs
./check.sh 05
```

- 처음에는 거의 다 ❌입니다. 정상입니다. **❌ 메시지의 "기대 / 실제"를 읽고** 위에서부터 하나씩 고치세요.
- 컴파일 에러가 나면 첫 번째 에러부터 읽으세요.
- 15분 넘게 막혔을 때만 `../../labs/solutions/week05/`를 열어 보세요. 보고 나서는 **닫고 다시 직접 쳐서** 구현하세요.


---

## 1. 왜 클래스인가

지금까지는 변수와 메서드를 따로 다뤘습니다. 은행 계좌를 표현하려면 `String[] owners`, `int[] balances`처럼 배열을 나란히 관리해야 하고, 실수하기 쉽습니다. **클래스는 관련된 데이터(필드)와 동작(메서드)을 한 덩어리로** 묶습니다.

> **JS와 비교:** JS의 `class`와 개념은 같습니다. 다만 자바는 필드를 **반드시 타입과 함께 선언**하고, 접근 제한자(`private` 등)가 언어 차원에서 강제됩니다. (JS의 `#private`과 비슷한 역할)

## 2. 첫 클래스

```java
// File: AccountDemo.java
class Account {
    // 필드: 객체가 가진 데이터. private으로 숨긴다
    private final String owner;
    private long balance;

    // 생성자: 객체를 만들 때 호출. 클래스 이름과 같고 반환 타입이 없다
    Account(String owner, long initial) {
        this.owner = owner;          // this: 지금 만들어지는(호출된) 객체 자신
        this.balance = initial;
    }

    // 메서드: 객체가 할 수 있는 동작
    void deposit(long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("입금액은 0보다 커야 합니다");
        }
        balance += amount;
    }

    boolean withdraw(long amount) {
        if (amount <= 0 || amount > balance) {
            return false;
        }
        balance -= amount;
        return true;
    }

    // getter: 읽기 전용 접근
    long getBalance() { return balance; }
    String getOwner() { return owner; }

    @Override
    public String toString() {
        return owner + "의 잔액: " + balance + "원";
    }
}

public class AccountDemo {
    public static void main(String[] args) {
        Account acc = new Account("하승", 10_000);   // 객체(인스턴스) 생성
        acc.deposit(5_000);
        System.out.println(acc.withdraw(20_000));   // false: 잔액 부족
        System.out.println(acc.withdraw(3_000));    // true
        System.out.println(acc);                    // toString() 자동 호출

        // acc.balance = 1_000_000;   // 컴파일 에러: private 필드에 직접 접근 불가
    }
}
```

용어를 정리하면 다음과 같습니다.

| 용어 | 의미 |
|------|------|
| 클래스 | 객체를 만드는 설계도 |
| 객체 / 인스턴스 | `new`로 만든 실제 대상 |
| 필드 | 객체의 데이터 (멤버 변수) |
| 메서드 | 객체의 동작 |
| 생성자 | 객체를 초기화하는 특수 메서드 |

## 3. 캡슐화 (Encapsulation)

**필드는 `private`, 접근은 메서드로.** 이유는 "잘못된 상태를 만들 수 없게" 하려는 것입니다. `balance`가 `public`이면 누구나 음수로 만들 수 있지만, `deposit`/`withdraw`를 통해서만 바꾸게 하면 규칙을 한 곳에서 지킬 수 있습니다.

| 접근 제한자 | 같은 클래스 | 같은 패키지 | 자식 클래스 | 어디서나 |
|-------------|:---:|:---:|:---:|:---:|
| `private` | O | | | |
| (없음, package-private) | O | O | | |
| `protected` | O | O | O | |
| `public` | O | O | O | O |

기본 원칙: **가능한 한 좁게** 열어 둡니다. 필드는 `private`, 외부에 필요한 메서드만 `public`.

## 4. 생성자 오버로딩과 `this(...)`

```java
// File: ConstructorDemo.java
class Point {
    final int x;
    final int y;

    Point() {
        this(0, 0);            // 다른 생성자 호출 (반드시 첫 줄)
    }

    Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // 불변 객체: 상태를 바꾸지 않고 새 객체를 돌려준다
    Point move(int dx, int dy) {
        return new Point(x + dx, y + dy);
    }

    @Override
    public String toString() { return "(" + x + ", " + y + ")"; }
}

public class ConstructorDemo {
    public static void main(String[] args) {
        Point origin = new Point();
        Point p = origin.move(3, 4);
        System.out.println(origin + " -> " + p);   // (0, 0) -> (3, 4)
    }
}
```

생성자를 하나도 만들지 않으면 인자 없는 **기본 생성자**가 자동으로 생깁니다. 하나라도 직접 만들면 자동 생성은 사라집니다.

`final` 필드는 한 번만 대입할 수 있어서, **불변 객체**를 만들 때 유용합니다. 불변은 버그가 적은 코드의 가장 강력한 습관입니다.

## 5. `static` — 인스턴스가 아닌 클래스의 것

```java
// File: StaticDemo.java
class Counter {
    private static int total = 0;      // 클래스 전체가 공유하는 필드
    private final int id;

    Counter() {
        total++;
        this.id = total;
    }

    int getId() { return id; }
    static int getTotal() { return total; }   // 객체 없이 호출 가능
}

public class StaticDemo {
    public static void main(String[] args) {
        new Counter();
        new Counter();
        Counter c = new Counter();
        System.out.println(c.getId());           // 3
        System.out.println(Counter.getTotal());  // 3  (클래스 이름으로 호출)
    }
}
```

- `static` 메서드 안에서는 `this`와 인스턴스 필드를 쓸 수 없습니다. 그래서 `main`에서 객체를 만들지 않고 인스턴스 메서드를 바로 호출하면 에러가 납니다.
- `Math.max()`, `Integer.parseInt()`처럼 **상태가 필요 없는 유틸리티**는 `static`이 적합합니다. 상태가 있으면 인스턴스로 만드세요.
- `static`을 남용하면 사실상 전역 변수라서 테스트와 유지보수가 어려워집니다.

## 6. 참조와 `null`

객체 변수는 **객체 자체가 아니라 객체를 가리키는 주소**를 담습니다.

```java
// File: ReferenceDemo.java
class Box {
    int value;
    Box(int value) { this.value = value; }
}

public class ReferenceDemo {
    public static void main(String[] args) {
        Box a = new Box(1);
        Box b = a;             // 주소 복사: 같은 객체를 가리킨다
        b.value = 99;
        System.out.println(a.value);    // 99

        Box c = new Box(99);
        System.out.println(a == c);     // false (다른 객체)

        Box nothing = null;             // 아무것도 가리키지 않음
        try {
            System.out.println(nothing.value);
        } catch (NullPointerException e) {
            System.out.println("NPE: null인 변수에 접근했다");
        }
    }
}
```

`NullPointerException`은 자바에서 가장 흔한 런타임 예외입니다. 12주차의 `Optional`이 이를 줄이는 도구입니다.

## 연습문제

1. `Student` 클래스(이름, 점수 3개)를 만들고 `average()` 메서드를 추가하세요. 점수는 0~100만 허용하고, 아니면 `IllegalArgumentException`을 던지세요.
2. `Rectangle`(가로, 세로)에 `area()`, `perimeter()`, `isSquare()`를 추가하세요. 값은 `final`로 두세요.
3. 위 `Account`에 **계좌번호를 자동 부여**하는 `static` 카운터를 추가하세요.
4. `Account`에 `transfer(Account to, long amount)`를 추가하세요. 출금이 실패하면 입금도 하지 않아야 합니다.
5. `Stack` 클래스를 직접 만드세요. `int[]` 내부 배열로 `push`, `pop`, `peek`, `isEmpty`를 구현합니다. 가득 찼을 때 `push`와 비었을 때 `pop`은 예외를 던집니다.
6. `ReferenceDemo`를 수정해 `b = new Box(5)`로 바꾼 뒤 `a.value`가 어떻게 되는지 예측하고 확인하세요.

## 퀴즈

1. 필드를 `private`으로 두는 가장 큰 이유는?
2. 생성자를 직접 하나 만들면 기본 생성자는 어떻게 되나?
3. `static` 메서드에서 `this`를 쓸 수 없는 이유는?
4. `Box a = new Box(1); Box b = a;`에서 `b.value = 9`를 하면 `a.value`는?

<details>
<summary>정답</summary>

1. 객체의 상태를 잘못 바꾸는 것을 막고, 규칙을 메서드 안에서 한 곳에서 지키기 위해
2. 자동 생성되지 않는다. 필요하면 직접 만들어야 한다
3. `static` 메서드는 특정 객체와 무관하게 호출되므로 "자기 자신"이 없다
4. `9` (같은 객체를 가리킴)

</details>

## 체크리스트

- [ ] 필드 `private`, 메서드 `public` 설계를 직접 해 봤다
- [ ] 생성자에서 유효성 검사를 했다
- [ ] `static` 필드와 인스턴스 필드의 차이를 설명할 수 있다
- [ ] `NullPointerException`이 왜 나는지 설명할 수 있다
