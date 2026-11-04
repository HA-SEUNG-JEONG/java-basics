# 8주차. 예외·패키지·Object 메서드

**예상 시간:** 10시간 | **목표:** 예외를 올바르게 던지고 처리하며, `equals`/`hashCode`가 왜 쌍으로 필요한지 설명할 수 있다.

## 🖐 먼저 손으로 (읽기 전에 하세요)

> 이 주차의 핵심은 아래 단계를 **직접 타이핑하면서** 깨닫는 것입니다. 아래쪽 설명 문서는 먼저 읽지 않아도 됩니다. 막혔을 때 사전처럼 펼쳐 보세요.

### 1단계. 예측하고 → 치고 → 비교하기 (20분)

1. 아래 코드를 **실행하기 전에** 각 줄의 출력을 노트에 먼저 적습니다.
2. 코드를 한 글자씩 타이핑합니다 (복사·붙여넣기 금지). 오타가 나면 컴파일러가 알려 줍니다.
3. 실행해서 결과를 비교합니다. 예측이 틀린 줄에 ★를 치고, **왜 그렇게 나왔는지 한 문장**으로 적습니다. 이 ★가 오늘 배운 진짜 내용입니다.

```java
// File: Predict08.java
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Predict08 {
    static int f() {
        try {
            return 1;
        } finally {
            System.out.println("finally");
        }
    }

    static int g() {
        int x = 1;
        try {
            return x;
        } finally {
            x = 2;
        }
    }

    static String h() {
        try {
            throw new IllegalStateException("boom");
        } catch (RuntimeException e) {
            return "caught " + e.getMessage();
        } finally {
            System.out.println("cleanup");
        }
    }

    public static void main(String[] args) {
        System.out.println(f());
        System.out.println(g());
        System.out.println(h());
        try {
            Object o = "s";
            Integer i = (Integer) o;
        } catch (ClassCastException | NullPointerException e) {
            System.out.println("multi");
        }
        try {
            System.out.println(10 / args.length);
        } catch (ArithmeticException e) {
            System.out.println(e.getMessage());
        }
        System.out.println(new HashSet<>(List.of("a")).equals(Set.of("a")));
    }
}
```

<details>
<summary>실행 결과 (예측을 적은 뒤에 여세요)</summary>

```text
finally
1
1
cleanup
caught boom
multi
/ by zero
true
```

</details>

### 2단계. 일부러 망가뜨리기 (20분)

방금 친 코드(또는 아래 설명 문서의 예제)를 하나씩 **고장 내 보고**, 어떤 메시지가 나오는지 확인합니다. 먼저 어떻게 될지 예상한 뒤 실행하세요. 메시지를 외울 필요는 없습니다. 에러 메시지가 **어떤 모양이고 어디를 가리키는지** 눈에 익히는 것이 목표입니다.

| 이렇게 바꿔 보세요 | 어떻게 될까요? |
|---|---|
| `throw new Exception("x");` 를 `throws` 선언 없이 메서드 안에서 | `unreported exception java.lang.Exception; must be caught or declared to be thrown` — checked 예외 |
| `throw new RuntimeException("x");` 로 바꾸기 | 컴파일이 된다. checked 와 unchecked 의 차이를 체감 |
| `catch (Exception e)` 를 먼저 쓰고 그 뒤에 `catch (IllegalArgumentException e)` | `exception IllegalArgumentException has already been caught` |
| `catch` 블록을 비워 두고 예외를 일으켜 보기 | 아무 일도 없는 것처럼 지나간다. 이 침묵이 나중에 원인 찾기를 지옥으로 만든다 |
| `equals` 만 재정의한 클래스를 `HashSet` 에 같은 내용으로 2번 넣기 | size 가 2 가 된다 (`hashCode` 미재정의) |
| `Integer.parseInt("12a")` | `NumberFormatException: For input string: "12a"` |

### 3단계. 빈칸에서 만들기 — 자동 채점 (1~3시간)

`../../labs/week08/` 폴더의 `InsufficientFundsException`, `Wallet`, `Parse`, `Product`, `Resources`를 열어 `// 여기를 고치세요` 부분을 직접 구현합니다. 구현하고 채점기를 돌려 ✅를 하나씩 늘려 가세요.

```bash
cd labs
./check.sh 08
```

- 처음에는 거의 다 ❌입니다. 정상입니다. **❌ 메시지의 "기대 / 실제"를 읽고** 위에서부터 하나씩 고치세요.
- 컴파일 에러가 나면 첫 번째 에러부터 읽으세요.
- 15분 넘게 막혔을 때만 `../../labs/solutions/week08/`를 열어 보세요. 보고 나서는 **닫고 다시 직접 쳐서** 구현하세요.


---

## 1. 예외란

프로그램 실행 중 정상 흐름을 깨는 사건입니다. 처리하지 않으면 프로그램이 스택 트레이스를 출력하고 종료합니다.

```
Exception in thread "main" java.lang.ArithmeticException: / by zero
    at Demo.main(Demo.java:4)
```

스택 트레이스는 **위에서 아래로 읽습니다.** 첫 줄이 예외 종류와 메시지, 그다음이 발생한 위치(가장 안쪽 호출)입니다.

> **JS와 비교:** JS의 `try/catch/finally`, `throw`와 거의 같습니다. 차이는 자바에는 **catch할 예외 타입을 지정**하고, 일부 예외는 **처리하지 않으면 컴파일이 안 된다**는 점입니다.

## 2. 예외의 종류

```
Throwable
├── Error                  (OutOfMemoryError 등: 잡지 않는다)
└── Exception
    ├── RuntimeException   -> unchecked: 컴파일러가 처리를 강제하지 않음
    │     (NullPointerException, IllegalArgumentException, IllegalStateException ...)
    └── 그 외 Exception     -> checked: 반드시 try/catch 하거나 throws로 선언
          (IOException, 직접 만든 비즈니스 예외 등)
```

| 구분 | 언제 쓰나 |
|------|-----------|
| unchecked (`RuntimeException` 계열) | 프로그래머 실수·잘못된 인자·잘못된 상태. **대부분 이쪽** |
| checked (`Exception` 계열) | 호출자가 복구할 수 있는 외부 요인 (파일 없음, 네트워크 실패) |

요즘 실무와 Spring은 대체로 unchecked를 선호합니다. checked 예외를 남발하면 모든 호출 경로에 `throws`가 번집니다.

## 3. try / catch / finally

```java
// File: ExceptionDemo.java
import java.util.List;

public class ExceptionDemo {
    static int parse(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            System.out.println("숫자가 아님: " + s);
            return -1;
        } finally {
            System.out.println("finally: 예외 여부와 상관없이 항상 실행");
        }
    }

    static void validate(int age) {
        if (age < 0) {
            throw new IllegalArgumentException("나이는 음수일 수 없습니다: " + age);
        }
    }

    public static void main(String[] args) {
        System.out.println(parse("42"));
        System.out.println(parse("abc"));

        try {
            validate(-1);
        } catch (IllegalArgumentException e) {
            System.out.println("잡음: " + e.getMessage());
        }

        // 여러 예외를 한 번에 (| 로 연결)
        try {
            Object o = "문자열";
            Integer i = (Integer) o;
        } catch (ClassCastException | NullPointerException e) {
            System.out.println("캐스팅/널 문제");
        }

        // List.of는 불변이라 수정하면 예외
        try {
            List.of(1, 2).add(3);
        } catch (UnsupportedOperationException e) {
            System.out.println("불변 리스트는 수정할 수 없다");
        }
    }
}
```

### 규칙

1. **구체적인 예외부터** catch합니다. `catch (Exception e)`로 뭉뚱그려 잡지 마세요.
2. **catch 블록을 비워 두지 마세요.** 최소한 로그를 남기거나 다시 던집니다. 예외를 삼키면 원인을 찾을 수 없습니다.
3. `finally`는 정리 작업용입니다. 파일·연결 정리는 아래 try-with-resources가 더 안전합니다.

## 4. try-with-resources

`AutoCloseable`을 구현한 자원은 블록이 끝나면 **자동으로 `close()`** 됩니다.

```java
// File: ResourceDemo.java
class Conn implements AutoCloseable {
    private final String name;
    Conn(String name) {
        this.name = name;
        System.out.println(name + " 열림");
    }
    void query() { throw new IllegalStateException("쿼리 실패"); }
    @Override
    public void close() { System.out.println(name + " 닫힘"); }
}

public class ResourceDemo {
    public static void main(String[] args) {
        try (Conn a = new Conn("A"); Conn b = new Conn("B")) {
            b.query();
        } catch (IllegalStateException e) {
            System.out.println("처리: " + e.getMessage());
        }
        // 출력 순서: A 열림, B 열림, B 닫힘, A 닫힘, 처리: 쿼리 실패
    }
}
```

닫힘은 열린 순서의 **역순**입니다. 파일·DB 연결·스트림은 항상 이 구문으로 다루세요 (14주차).

## 5. 사용자 정의 예외

```java
// File: CustomExceptionDemo.java
class InsufficientBalanceException extends RuntimeException {
    private final long shortage;

    InsufficientBalanceException(long shortage) {
        super("잔액이 " + shortage + "원 부족합니다");
        this.shortage = shortage;
    }

    long getShortage() { return shortage; }
}

class Wallet {
    private long balance;
    Wallet(long balance) { this.balance = balance; }

    void pay(long amount) {
        if (amount > balance) {
            throw new InsufficientBalanceException(amount - balance);
        }
        balance -= amount;
    }
}

public class CustomExceptionDemo {
    public static void main(String[] args) {
        Wallet w = new Wallet(1_000);
        try {
            w.pay(1_500);
        } catch (InsufficientBalanceException e) {
            System.out.println(e.getMessage());          // 잔액이 500원 부족합니다
            System.out.println(e.getShortage());
        }
    }
}
```

예외를 감싸서 던질 때는 **원인(cause)을 반드시 넘기세요.**

```java
catch (SQLException e) {
    throw new RuntimeException("주문 저장 실패", e);   // e를 빼면 원래 원인이 사라진다
}
```

## 6. 패키지와 `import`

클래스를 폴더 구조처럼 묶는 이름 공간입니다. 규칙은 **패키지 이름 = 디렉터리 경로**입니다.

```
src/
└── com/example/shop/
    ├── Main.java            // package com.example.shop;
    └── model/
        └── Product.java     // package com.example.shop.model;
```

```java
package com.example.shop;

import com.example.shop.model.Product;
import java.util.List;           // java.lang 패키지(String, Math 등)만 import 없이 사용 가능

public class Main {
    public static void main(String[] args) {
        Product p = new Product("노트북");
    }
}
```

- 패키지 이름은 보통 도메인을 뒤집어 소문자로 씁니다 (`com.example.shop`).
- IDE는 import를 자동으로 추가·정리해 줍니다 (IntelliJ: `Cmd+Option+O`).
- 이 방식은 16주차 Gradle 프로젝트(`src/main/java`)에서 직접 사용합니다.

## 7. Object의 핵심 메서드: `equals`, `hashCode`, `toString`

모든 클래스는 `Object`를 상속합니다. 기본 `equals`는 `==`처럼 **같은 객체인지**만 확인합니다. 내용이 같으면 같은 값으로 취급하고 싶다면 오버라이딩해야 합니다.

```java
// File: EqualsDemo.java
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

class Member {
    private final String email;
    private final String name;

    Member(String email, String name) {
        this.email = email;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Member m)) return false;
        return Objects.equals(email, m.email) && Objects.equals(name, m.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, name);
    }

    @Override
    public String toString() {
        return "Member[" + email + ", " + name + "]";
    }
}

class BadMember {
    final String email;
    BadMember(String email) { this.email = email; }

    @Override
    public boolean equals(Object o) {          // equals만 재정의하고 hashCode는 방치
        return o instanceof BadMember b && email.equals(b.email);
    }
}

public class EqualsDemo {
    public static void main(String[] args) {
        Member a = new Member("a@x.com", "하승");
        Member b = new Member("a@x.com", "하승");
        System.out.println(a == b);          // false
        System.out.println(a.equals(b));     // true

        Set<Member> set = new HashSet<>();
        set.add(a);
        set.add(b);
        System.out.println(set.size());      // 1: 중복으로 인식

        Set<BadMember> bad = new HashSet<>();
        bad.add(new BadMember("a@x.com"));
        bad.add(new BadMember("a@x.com"));
        System.out.println(bad.size());      // 보통 2: hashCode가 달라서 중복을 못 잡는다
    }
}
```

**규칙: `equals`를 재정의하면 `hashCode`도 반드시 함께 재정의합니다.** 같은 객체는 같은 해시 코드를 가져야 `HashSet`/`HashMap`이 제대로 동작합니다. 7주차의 `record`는 이를 자동으로 해 주므로, 단순 값 객체라면 `record`를 쓰는 것이 가장 쉽습니다.

## 연습문제

1. 사용자가 입력한 문자열을 `int`로 변환하되, 숫자가 아니면 "다시 입력하세요"를 출력하고 올바른 값이 들어올 때까지 반복하는 프로그램을 작성하세요.
2. `Account.withdraw`를 `boolean` 반환 대신 `InsufficientBalanceException`을 던지도록 바꾸세요. 호출하는 쪽의 코드가 어떻게 달라지는지 비교해 보세요.
3. `AutoCloseable`을 구현한 `Timer` 클래스를 만들어 try-with-resources 블록의 실행 시간을 출력하게 하세요 (`System.nanoTime()`).
4. 아래 코드에서 `finally`와 `return`이 섞일 때 어떤 값이 출력되는지 **실행 전에 예측**한 뒤 확인하세요.
   ```java
   static int f() {
       try { return 1; } finally { System.out.println("finally"); }
   }
   ```
5. `Product(id, name)` 클래스를 만들고 `equals`/`hashCode`를 직접 구현한 뒤, 같은 내용을 `record`로 바꿔 두 결과가 같은지 `HashSet`으로 확인하세요.
6. 패키지를 나눠서 `com.example.library.Main`과 `com.example.library.model.Book`을 만들고, 터미널에서 `javac -d out $(find . -name "*.java")` 후 `java -cp out com.example.library.Main`으로 실행해 보세요.

## 퀴즈

1. checked 예외와 unchecked 예외의 차이는?
2. try-with-resources에서 자원은 어떤 순서로 닫히나?
3. `equals`만 재정의하면 어떤 문제가 생기나?
4. 예외를 감싸서 다시 던질 때 주의할 점은?

<details>
<summary>정답</summary>

1. checked는 컴파일러가 `try/catch`나 `throws`를 강제하고, unchecked(`RuntimeException` 계열)는 강제하지 않는다
2. 열린 순서의 역순
3. `HashSet`/`HashMap`에서 같은 값으로 취급되지 않는다 (해시 코드가 달라서)
4. 원인 예외(cause)를 생성자에 넘겨야 원래 스택 트레이스가 보존된다

</details>

## 체크리스트

- [ ] 스택 트레이스를 읽고 에러가 발생한 줄을 찾을 수 있다
- [ ] 사용자 정의 예외를 만들고 던졌다
- [ ] try-with-resources의 닫힘 순서를 확인했다
- [ ] `equals`/`hashCode`를 쌍으로 재정의했다
- [ ] 패키지를 나눈 프로젝트를 컴파일·실행했다
