# 6주차. 상속과 다형성

**예상 시간:** 10시간 | **목표:** 상속·오버라이딩·추상 클래스를 사용하고, 다형성이 왜 유용한지 코드로 설명할 수 있다.

## 🖐 먼저 손으로 (읽기 전에 하세요)

> 이 주차의 핵심은 아래 단계를 **직접 타이핑하면서** 깨닫는 것입니다. 아래쪽 설명 문서는 먼저 읽지 않아도 됩니다. 막혔을 때 사전처럼 펼쳐 보세요.

### 1단계. 예측하고 → 치고 → 비교하기 (20분)

1. 아래 코드를 **실행하기 전에** 각 줄의 출력을 노트에 먼저 적습니다.
2. 코드를 한 글자씩 타이핑합니다 (복사·붙여넣기 금지). 오타가 나면 컴파일러가 알려 줍니다.
3. 실행해서 결과를 비교합니다. 예측이 틀린 줄에 ★를 치고, **왜 그렇게 나왔는지 한 문장**으로 적습니다. 이 ★가 오늘 배운 진짜 내용입니다.

```java
// File: Predict06.java
public class Predict06 {
    static class Animal {
        String name() { return "animal"; }
        String hello() { return "I am " + name(); }
        static String kind() { return "A"; }
    }

    static class Dog extends Animal {
        @Override String name() { return "dog"; }
        static String kind() { return "D"; }
    }

    public static void main(String[] args) {
        Animal a = new Dog();
        System.out.println(a.name());
        System.out.println(a.hello());
        System.out.println(a.kind());
        System.out.println(Dog.kind());
        System.out.println(a instanceof Dog);
        System.out.println(a.getClass().getSimpleName());
        Object o = a;
        System.out.println(o instanceof Animal);
        try {
            String s = (String) o;
        } catch (ClassCastException e) {
            System.out.println("CCE");
        }
    }
}
```

<details>
<summary>실행 결과 (예측을 적은 뒤에 여세요)</summary>

```text
dog
I am dog
A
D
true
Dog
true
CCE
```

</details>

### 2단계. 일부러 망가뜨리기 (20분)

방금 친 코드(또는 아래 설명 문서의 예제)를 하나씩 **고장 내 보고**, 어떤 메시지가 나오는지 확인합니다. 먼저 어떻게 될지 예상한 뒤 실행하세요. 메시지를 외울 필요는 없습니다. 에러 메시지가 **어떤 모양이고 어디를 가리키는지** 눈에 익히는 것이 목표입니다.

| 이렇게 바꿔 보세요 | 어떻게 될까요? |
|---|---|
| `@Override` 가 붙은 메서드 이름을 `nam()` 으로 오타 | `method does not override or implement a method from a supertype` — `@Override` 가 없었다면 조용히 새 메서드가 생겼을 것 |
| `new Shape()` (추상 클래스) | `Shape is abstract; cannot be instantiated` |
| `Circle` 에서 `area()` 구현을 지우기 | `Circle is not abstract and does not override abstract method area() in Shape` |
| 부모에 인자 있는 생성자만 있는데 자식 생성자에서 `super(...)` 를 지우기 | `constructor ... in class ... cannot be applied to given types` |
| `Dog d = new Animal();` | `incompatible types: Animal cannot be converted to Dog` |
| 자식의 `name()` 앞에 `private` 붙이기 | `attempting to assign weaker access privileges` — 접근 범위는 좁힐 수 없다 |

### 3단계. 빈칸에서 만들기 — 자동 채점 (1~3시간)

`../../labs/week06/` 폴더의 `Circle`, `Rect`, `Triangle`, `Shapes`, `Manager`, `Engineer`, `Payroll` (`Shape`, `Employee` 는 수정 금지)를 열어 `// 여기를 고치세요` 부분을 직접 구현합니다. 구현하고 채점기를 돌려 ✅를 하나씩 늘려 가세요.

```bash
cd labs
./check.sh 06
```

- 처음에는 거의 다 ❌입니다. 정상입니다. **❌ 메시지의 "기대 / 실제"를 읽고** 위에서부터 하나씩 고치세요.
- 컴파일 에러가 나면 첫 번째 에러부터 읽으세요.
- 15분 넘게 막혔을 때만 `../../labs/solutions/week06/`를 열어 보세요. 보고 나서는 **닫고 다시 직접 쳐서** 구현하세요.


---

## 1. 상속

```java
// File: InheritanceDemo.java
class Animal {
    protected final String name;

    Animal(String name) {
        this.name = name;
    }

    String sound() {
        return "...";
    }

    void introduce() {
        System.out.println(name + "가 말한다: " + sound());
    }
}

class Dog extends Animal {
    Dog(String name) {
        super(name);               // 부모 생성자 호출. 자식 생성자의 첫 줄
    }

    @Override                      // 부모 메서드를 재정의. 오타를 컴파일러가 잡아 준다
    String sound() {
        return "멍멍";
    }

    void fetch() {
        System.out.println(name + "가 공을 물어온다");
    }
}

class Cat extends Animal {
    Cat(String name) { super(name); }

    @Override
    String sound() { return "야옹"; }
}

public class InheritanceDemo {
    public static void main(String[] args) {
        Dog d = new Dog("바둑이");
        d.introduce();             // 바둑이가 말한다: 멍멍
        d.fetch();
    }
}
```

- 자바는 **단일 상속**만 지원합니다. `extends` 뒤에 클래스는 하나뿐입니다.
- 모든 클래스는 암묵적으로 `Object`를 상속합니다 (`toString`, `equals`, `hashCode` 등이 여기서 옵니다).
- `@Override`는 **항상 붙이세요.** 시그니처가 다르면 컴파일 에러가 나서 오버로딩과 혼동하는 실수를 막아 줍니다.

> **JS와 비교:** JS의 `extends`와 `super`는 거의 같습니다. 차이는 자바에서는 타입 호환성이 컴파일 시점에 검사된다는 점입니다.

## 2. 다형성 (Polymorphism)

**부모 타입 변수로 자식 객체를 다룰 수 있고, 실제 호출되는 메서드는 객체의 실제 타입이 정합니다.**

```java
// File: PolyDemo.java
abstract class Shape {
    abstract double area();            // 구현 없는 추상 메서드: 자식이 반드시 구현

    void print() {                     // 일반 메서드도 가질 수 있다
        System.out.printf("%s 넓이: %.2f%n", getClass().getSimpleName(), area());
    }
}

class Circle extends Shape {
    private final double r;
    Circle(double r) { this.r = r; }
    @Override double area() { return Math.PI * r * r; }
}

class Rect extends Shape {
    private final double w, h;
    Rect(double w, double h) { this.w = w; this.h = h; }
    @Override double area() { return w * h; }
}

public class PolyDemo {
    public static void main(String[] args) {
        // new Shape();               // 컴파일 에러: 추상 클래스는 인스턴스화 불가
        Shape[] shapes = { new Circle(1), new Rect(2, 3), new Circle(0.5) };

        double total = 0;
        for (Shape s : shapes) {       // 도형 종류를 몰라도 area()를 호출할 수 있다
            s.print();
            total += s.area();
        }
        System.out.printf("합계: %.2f%n", total);
    }
}
```

다형성의 이득은 **새 도형(예: `Triangle`)을 추가해도 `for` 루프는 한 글자도 안 바꿔도 된다**는 점입니다. 여기서 `if (s instanceof Circle) ... else if (s instanceof Rect) ...` 같은 분기를 쓰고 있다면, 설계를 다시 보세요.

## 3. 형변환과 `instanceof`

```java
// File: CastDemo.java
class Animal {}
class Dog extends Animal {
    void fetch() { System.out.println("물어온다"); }
}
class Cat extends Animal {}

public class CastDemo {
    public static void main(String[] args) {
        Animal a = new Dog();          // 업캐스팅: 자동

        // a.fetch();                  // 컴파일 에러: Animal 타입에는 fetch()가 없다

        if (a instanceof Dog d) {      // 패턴 매칭 (Java 16+): 검사와 캐스팅을 한 번에
            d.fetch();
        }

        Animal c = new Cat();
        try {
            Dog wrong = (Dog) c;       // 잘못된 다운캐스팅은 런타임 예외
        } catch (ClassCastException e) {
            System.out.println("ClassCastException");
        }
    }
}
```

## 4. 상속 vs 합성

상속은 "A는 B이다(is-a)"일 때만 씁니다. "A는 B를 가지고 있다(has-a)"는 **필드로 가지는 합성**이 맞습니다.

```java
class Engine { void start() { System.out.println("부릉"); } }

class Car {
    private final Engine engine = new Engine();   // Car has-a Engine
    void start() { engine.start(); }
}
```

자식 클래스는 부모의 변경에 강하게 묶입니다. 코드를 재사용하려는 목적만으로 상속을 쓰지 마세요. "**상속보다 합성**"은 자바 커뮤니티의 오래된 원칙입니다.

`final class Foo`는 상속을 막고, `final` 메서드는 오버라이딩을 막습니다.

## 5. 자주 하는 실수

| 실수 | 결과 |
|------|------|
| 자식 생성자에서 `super(...)`를 안 부름 | 부모에 기본 생성자가 없으면 컴파일 에러 |
| 오버라이딩 시 접근 제한자를 좁힘 | 컴파일 에러 (`public` → `private` 불가) |
| 필드를 오버라이딩한다고 착각 | 필드는 오버라이딩되지 않고 가려질 뿐 |
| `static` 메서드를 오버라이딩한다고 착각 | 가려질 뿐 다형성이 적용되지 않음 |

## 연습문제

1. `Employee`(이름, 기본급)를 부모로, `Manager`(보너스 추가)와 `Engineer`(야근수당 추가)를 자식으로 만들고 `monthlyPay()`를 오버라이딩하세요. `Employee[]` 배열로 전체 급여 합계를 구하세요.
2. `Shape`에 `Triangle`을 추가하되, **`PolyDemo.main`은 수정하지 않고** 배열 내용만 바꿔 실행하세요.
3. 추상 클래스 `Vehicle`(속도, `move()` 추상 메서드)과 `Bike`, `Car`를 구현하세요.
4. `Animal`에 `instanceof`를 쓰지 않고 `describe()`를 오버라이딩해서 각 동물의 정보를 출력하게 하세요. 그 뒤 `instanceof` 분기 버전과 비교해 어느 쪽이 확장에 유리한지 설명하세요.
5. `Car`와 `Engine` 예제에서 `Engine`을 `ElectricEngine`으로 바꾸고 싶을 때 `Car`를 수정해야 하는 범위를 줄일 방법을 생각해 보세요. (다음 주 인터페이스에서 답을 확인합니다)

## 퀴즈

1. 추상 클래스는 `new`로 객체를 만들 수 있나?
2. `Animal a = new Dog(); a.sound();`는 누구의 `sound()`가 실행되나?
3. 자바에서 다중 상속이 불가능한 이유 중 하나는? (대안은?)
4. `super(...)` 호출이 반드시 와야 하는 위치는?

<details>
<summary>정답</summary>

1. 불가능. 자식 클래스를 통해서만 만든다
2. `Dog`의 `sound()`. 호출되는 메서드는 변수 타입이 아니라 실제 객체의 타입이 결정한다
3. 같은 이름의 메서드를 여러 부모에서 물려받을 때 어느 것을 쓸지 모호해지는 문제. 대안은 인터페이스(다음 주)
4. 자식 생성자의 첫 줄

</details>

## 체크리스트

- [ ] `@Override`를 습관적으로 쓴다
- [ ] 다형성이 `if-else` 분기를 대체하는 예를 만들어 봤다
- [ ] 상속과 합성을 구분해서 선택할 수 있다
- [ ] `instanceof` 패턴 매칭을 사용해 봤다
