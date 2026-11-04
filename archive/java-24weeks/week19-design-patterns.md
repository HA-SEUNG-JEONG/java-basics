# 19주차. 설계 원칙과 디자인 패턴

**예상 시간:** 9시간 | **목표:** "동작하는 코드"에서 "바꾸기 쉬운 코드"로 넘어간다. 의존성 주입(DI)을 손으로 해 보고, 전략·팩토리·옵저버 세 가지 패턴을 직접 구현한다. **다음 주 Spring의 IoC/DI를 이해하기 위한 준비 주차**입니다.

> 패턴을 외우는 주차가 아닙니다. 패턴 이름은 **이미 풀어 본 문제에 붙이는 이름표**입니다. 먼저 불편함을 느끼고, 그다음에 이름을 배웁니다.

## 🖐 먼저 손으로 (읽기 전에 하세요)

> 이 주차의 핵심은 아래 단계를 **직접 타이핑하면서** 깨닫는 것입니다. 아래쪽 설명 문서는 먼저 읽지 않아도 됩니다. 막혔을 때 사전처럼 펼쳐 보세요.

### 1단계. 예측하고 → 치고 → 비교하기 (20분)

1. 아래 코드를 **실행하기 전에** 출력 9줄을 노트에 먼저 적습니다. (`15505 / 10` 은 정수 나눗셈, 2주차)
2. 한 글자씩 타이핑합니다. 이 파일은 `main` 이 있는 클래스를 **맨 앞에** 둡니다. (`java Predict19.java` 는 첫 번째 클래스를 실행하기 때문)
3. 틀린 줄에 ★를 치고 이유를 한 문장으로 적습니다.

```java
// File: Predict19.java
import java.util.ArrayList;
import java.util.List;

public class Predict19 {
    public static void main(String[] args) {
        List<String> log = new ArrayList<>();
        Shop normal = new Shop(x -> 0, log);
        Shop vip = new Shop(x -> x / 10, log);
        Shop event = new Shop(x -> Math.min(x, 3000), log);
        System.out.println(normal.checkout(15505));
        System.out.println(vip.checkout(15505));
        System.out.println(event.checkout(2000));
        System.out.println(log);

        List<Runnable> listeners = new ArrayList<>();
        listeners.add(() -> System.out.println("A"));
        listeners.add(() -> { throw new IllegalStateException("B 실패"); });
        listeners.add(() -> System.out.println("C"));
        try {
            for (Runnable r : listeners) r.run();
        } catch (IllegalStateException e) {
            System.out.println("바깥에서 잡음: " + e.getMessage());
        }
        for (Runnable r : listeners) {
            try {
                r.run();
            } catch (IllegalStateException e) {
                System.out.println("하나씩 잡음: " + e.getMessage());
            }
        }
    }
}

interface Discount {
    int apply(int amount);
}

class Shop {
    private final Discount discount;
    private final List<String> log;

    Shop(Discount discount, List<String> log) {
        this.discount = discount;
        this.log = log;
    }

    int checkout(int amount) {
        int pay = amount - discount.apply(amount);
        log.add("pay " + pay);
        return pay;
    }
}
```

<details>
<summary>실행 결과 (예측을 적은 뒤에 여세요)</summary>

```text
15505
13955
0
[pay 15505, pay 13955, pay 0]
A
바깥에서 잡음: B 실패
A
하나씩 잡음: B 실패
C
```

`Shop` 은 할인 방식을 모릅니다. 인터페이스 `Discount` 만 알고, 무엇이 들어올지는 만드는 쪽이 정합니다. `Shop` 코드를 한 줄도 바꾸지 않고 세 가지 가게를 만들었습니다. 이것이 오늘의 전부입니다. 아래쪽은 구독자 한 명의 예외가 나머지를 막는 것을 보여 줍니다.

</details>

### 2단계. 일부러 망가뜨리기 (30분)

3단계 실습을 끝낸 뒤에 해 보세요. 통과한 `OrderService` 를 아래처럼 고쳐 보고, **어느 테스트가 어떤 이유로 실패하는지** 확인합니다.

| 이렇게 바꿔 보세요 | 어떻게 될까요? |
|---|---|
| `OrderService.placeOrder` 의 `policy.discount(amount)` 를 `new PercentDiscount(10).discount(amount)` 로 | `newPolicyNeedsNoChangeInOrderService`, `orderService_doesNotNameConcreteClasses` 2개 실패. 서비스가 구체 클래스에 묶였다 |
| `placeOrder` 에서 금액 검사(`amount <= 0`)를 저장 **뒤**로 옮기기 | 잘못된 주문이 저장소에 남는다. 테스트의 "부작용 없음" 검사가 잡아낸다 |
| `EventBus.publish` 에서 `try/catch` 제거 | 예외를 던진 구독자 **뒤의** 구독자가 호출되지 않는다 (1단계 출력의 `C`가 빠지는 것과 같은 원리) |
| `DiscountPolicies.forGrade` 의 `default` 를 `NormalDiscount` 반환으로 | 오타 등급(`"VIPP"`)이 조용히 "할인 없음"이 된다. 모르는 값은 **실패**시키는 편이 안전하다 |

### 3단계. 빈칸에서 만들기 — 자동 채점 (3~5시간)

`../../labs/backend/week19/src/main/java/` 의 스텁을 채웁니다. 파일마다 맨 위 주석에 규칙이 적혀 있습니다.

| 파일 | 만드는 것 | 연습하는 원칙·패턴 |
|---|---|---|
| `PercentDiscount`, `FixedDiscount`, `NoDiscount` | 할인 정책 3종 | 전략(Strategy) |
| `DiscountPolicies.forGrade` | 등급 → 정책 | 팩토리(Factory) |
| `OrderService` | 정책·저장소·알림을 **생성자로 받아** 주문 처리 | 의존성 주입(DI), 단일 책임 |
| `EventBus<E>` | 구독·발행 | 옵저버(Observer) |

```bash
cd ../../labs/backend
./gradlew :week19:test
```

- 처음에는 12개 중 11개가 실패합니다. 구체 클래스 이름 검사(`orderService_doesNotNameConcreteClasses`) 하나만 빈 스텁에서도 통과하는데, 구현을 채우다가 `new PercentDiscount(...)` 를 쓰는 순간 실패로 바뀝니다.
- `OrderService.java` 안에 `PercentDiscount`, `InMemoryOrderRepository` 같은 **구체 클래스 이름이 나오면 실패**합니다. 테스트가 소스 파일의 글자를 검사합니다.
- 정답은 `../../labs/solutions/week19/` 에 있습니다. **15분 넘게 막혔을 때만** 열고, 본 뒤에는 덮고 **직접 다시 치세요.**

---

## 1. SOLID 중 지금 필요한 셋

| 원칙 | 한 줄 | 이번 주 실습에서 |
|---|---|---|
| **S**ingle Responsibility | 클래스는 바뀔 이유가 하나여야 한다 | 할인 계산·저장·알림이 `OrderService` 한 곳에 뒤섞이지 않게 쪼갠다 |
| **O**pen/Closed | 확장에는 열려 있고 수정에는 닫혀 있다 | 새 할인 정책을 추가해도 `OrderService` 는 그대로 |
| **D**ependency Inversion | 구체가 아니라 **인터페이스**에 의존한다 | `OrderService` 는 `DiscountPolicy` 인터페이스만 안다 |

나머지 L(리스코프 치환)·I(인터페이스 분리)는 6·7주차의 상속·인터페이스 설계에서 이미 만났습니다.

## 2. 의존성 주입 (DI) — 이번 주의 핵심

```java
// 나쁜 예: 안에서 new 로 직접 만든다 -> 바꾸려면 이 클래스를 수정해야 하고, 테스트에서 가짜로 바꿀 수 없다
class OrderService {
    private final DiscountPolicy policy = new PercentDiscount(10);
}

// 좋은 예: 밖에서 받는다 (생성자 주입)
class OrderService {
    private final DiscountPolicy policy;
    OrderService(DiscountPolicy policy) { this.policy = policy; }
}
```

**객체를 누가 만들어서 넣어 주는가**가 바뀝니다. 지금은 `main` 이나 테스트가 `new` 로 조립합니다. **21주차 Spring 에서는 이 "조립"을 Spring 컨테이너가 대신합니다.** 그 컨테이너를 IoC(제어의 역전) 컨테이너라고 부릅니다. 그때 "마법"처럼 보이지 않으려면 오늘 직접 조립해 본 경험이 필요합니다.

> **JS와 비교:** Node/Express 에서 `function createService(db, logger)` 처럼 의존성을 인자로 받는 코드, 또는 테스트에서 `jest.mock` 으로 갈아 끼우는 것이 같은 발상입니다.

테스트가 쉬워지는 것이 가장 큰 이득입니다. 주어진 테스트에는 이런 가짜 알림이 있습니다. 진짜 문자 서비스를 호출하지 않고 "무슨 메시지를 보냈는가"만 기록합니다.

```java
List<String> sent = new ArrayList<>();
Notifier fake = sent::add;                    // Notifier.send(String) 하나뿐인 인터페이스이므로 메서드 참조로 충분
```

## 3. 전략 (Strategy)

"여러 알고리즘 중 하나를 골라 끼운다." 인터페이스 하나(`DiscountPolicy`)와 구현 여러 개(`PercentDiscount` …)입니다. 구현이 한 줄이면 **람다**로 충분합니다. (`DiscountPolicy` 는 메서드가 하나뿐인 `@FunctionalInterface`)

```java
DiscountPolicy half = amount -> amount / 2;
```

## 4. 팩토리 (Factory)

"어떤 구현을 만들지 고르는 `if`/`switch` 를 한 곳에만 둔다." 등급이 늘어나도 고칠 곳은 `DiscountPolicies.forGrade` 하나입니다. `switch` 표현식(7주차)에 `default -> throw` 를 두면 모르는 값이 조용히 지나가지 않습니다.

## 5. 옵저버 (Observer)

"무슨 일이 생겼다"를 알리는 쪽(`publish`)과 듣는 쪽(`subscribe`)을 분리합니다. 알리는 쪽은 누가 듣는지 모릅니다. 구독자는 `Consumer<E>` 로 받으면 람다를 그대로 넘길 수 있습니다. **구독자 하나의 실패가 다른 구독자를 막지 않게** 하나씩 `try/catch` 로 감싸는 것이 실무에서 흔한 요구입니다. (1단계 출력의 `하나씩 잡음`)

> 24주차 Spring 에서는 이 이름이 `ApplicationEventPublisher`/`@EventListener` 로 다시 나옵니다. 지금 만든 `EventBus` 가 그 원리입니다.

## 6. 과설계 경계

패턴은 **중복과 변경이 실제로 아플 때** 꺼내는 도구입니다. 구현이 하나뿐인 인터페이스, 쓰지 않을 확장 지점은 오히려 비용입니다. 3단계에서도 `NoDiscount` 클래스 대신 람다 `amount -> 0` 으로 충분하다고 느껴지면 그 감각이 맞습니다. (테스트는 `NoDiscount` 클래스를 요구하니 만들되, 생각은 해 보세요.)

## 연습문제

1. `Notifier` 구현으로 `ConsoleNotifier`(표준 출력)와 `FakeNotifier`(리스트에 기록)를 만들고, 실제 `main` 에서는 앞의 것으로, 테스트에서는 뒤의 것으로 `OrderService` 를 조립해 보세요.
2. `EventBus<Order>` 를 만들어 `OrderService` 가 주문을 저장한 뒤 `publish(order)` 하게 하고, "알림 전송"과 "통계 집계"를 구독자 두 개로 분리해 보세요. `OrderService` 가 `Notifier` 를 몰라도 되는 구조로 바꿀 수 있나요?
3. `DiscountPolicy` 두 개를 합치는 `andThen(DiscountPolicy)` 기본 메서드(`default`)를 인터페이스에 추가해 보세요. (예: 등급 할인 후 쿠폰 할인)
4. 9주차 도서 대출 시스템의 `Library.lend` 에 "대출 한도 정책"을 전략으로 뽑아내 보세요. (일반 회원 3권, 프리미엄 10권)

## 퀴즈

1. 의존성 주입에서 "주입"이란 정확히 무엇을 누가 하는 일인가?
2. `OrderService` 가 `new PercentDiscount(10)` 을 직접 만들면 어떤 문제가 생기나?
3. 전략과 팩토리의 역할 차이는?
4. 옵저버에서 구독자 하나가 예외를 던졌을 때 나머지를 계속 호출하려면?

<details>
<summary>정답</summary>

1. 객체가 쓸 의존 객체를 스스로 만들지 않고, 바깥(조립하는 코드 또는 프레임워크)이 만들어 생성자 등으로 넣어 주는 것
2. 정책을 바꾸려면 서비스 코드를 수정해야 하고, 테스트에서 가짜 정책으로 갈아 끼울 수 없다 (서비스가 구체 클래스에 묶인다)
3. 전략은 "무슨 방식으로 계산할지"를 표현하는 객체, 팩토리는 "그중 어느 것을 만들지" 고르는 곳
4. 구독자를 하나씩 `try/catch` 로 감싸 예외를 잡고 다음 구독자로 넘어간다

</details>

## 체크리스트

- [ ] `new` 로 직접 만들 때와 생성자로 받을 때의 차이를 테스트 작성 관점에서 설명할 수 있다
- [ ] 구체 클래스를 건드리지 않고 새 할인 정책을 추가했다 (O 의 의미)
- [ ] 12개 테스트를 모두 통과했다
- [ ] "내가 만든 `OrderService` 조립을 Spring 이 대신해 준다"는 문장을 내 말로 설명할 수 있다
