# 2주차. 변수·타입·연산자

**예상 시간:** 8시간 | **목표:** 자바의 정적 타입 체계를 이해하고, 형변환과 `String` 비교에서 생기는 함정을 피할 수 있다.

## 🖐 먼저 손으로 (읽기 전에 하세요)

> 이 주차의 핵심은 아래 단계를 **직접 타이핑하면서** 깨닫는 것입니다. 아래쪽 설명 문서는 먼저 읽지 않아도 됩니다. 막혔을 때 사전처럼 펼쳐 보세요.

### 1단계. 예측하고 → 치고 → 비교하기 (20분)

1. 아래 코드를 **실행하기 전에** 각 줄의 출력을 노트에 먼저 적습니다.
2. 코드를 한 글자씩 타이핑합니다 (복사·붙여넣기 금지). 오타가 나면 컴파일러가 알려 줍니다.
3. 실행해서 결과를 비교합니다. 예측이 틀린 줄에 ★를 치고, **왜 그렇게 나왔는지 한 문장**으로 적습니다. 이 ★가 오늘 배운 진짜 내용입니다.

```java
// File: Predict02.java
public class Predict02 {
    public static void main(String[] args) {
        System.out.println(10 / 4);
        System.out.println(10 / 4.0);
        System.out.println(-7 / 2);
        System.out.println(-7 % 3);
        System.out.println((int) 3.99);
        System.out.println((int) -3.99);
        System.out.println('a' + 1);
        System.out.println((char) ('a' + 1));
        System.out.println(0.1 + 0.2 == 0.3);
        String x = "hi", y = "hi", z = new String("hi");
        System.out.println((x == y) + " " + (x == z) + " " + x.equals(z));
        byte b = (byte) 200;
        System.out.println(b);
    }
}
```

<details>
<summary>실행 결과 (예측을 적은 뒤에 여세요)</summary>

```text
2
2.5
-3
-1
3
-3
98
b
false
true false true
-56
```

</details>

### 2단계. 일부러 망가뜨리기 (20분)

방금 친 코드(또는 아래 설명 문서의 예제)를 하나씩 **고장 내 보고**, 어떤 메시지가 나오는지 확인합니다. 먼저 어떻게 될지 예상한 뒤 실행하세요. 메시지를 외울 필요는 없습니다. 에러 메시지가 **어떤 모양이고 어디를 가리키는지** 눈에 익히는 것이 목표입니다.

| 이렇게 바꿔 보세요 | 어떻게 될까요? |
|---|---|
| `int x = 3.5;` | `incompatible types: possible lossy conversion from double to int` |
| `int y;` 만 선언하고 `System.out.println(y);` | `variable y might not have been initialized` |
| `long big = 3000000000;` (L 없이) | `integer number too large` — 왼쪽이 long 이어도 리터럴은 int 로 읽힌다 |
| `String s = 'hello';` | `unclosed character literal` — 작은따옴표는 문자 1개용 |
| `int a = 1; int a = 2;` | `variable a is already defined in method main(String[])` |
| `"abc" - 1` | `bad operand types for binary operator '-'` |
| `int i = Integer.MAX_VALUE + 1;` 출력 | 에러가 **나지 않는다**. `-2147483648` 이 조용히 출력된다. 이게 오버플로우의 무서움 |

### 3단계. 빈칸에서 만들기 — 자동 채점 (1~3시간)

`../../labs/week02/` 폴더의 `Mine.java`를 열어 `// 여기를 고치세요` 부분을 직접 구현합니다. 구현하고 채점기를 돌려 ✅를 하나씩 늘려 가세요.

```bash
cd labs
./check.sh 02
```

- 처음에는 거의 다 ❌입니다. 정상입니다. **❌ 메시지의 "기대 / 실제"를 읽고** 위에서부터 하나씩 고치세요.
- 컴파일 에러가 나면 첫 번째 에러부터 읽으세요.
- 15분 넘게 막혔을 때만 `../../labs/solutions/week02/`를 열어 보세요. 보고 나서는 **닫고 다시 직접 쳐서** 구현하세요.


---

## 1. 정적 타입

자바는 변수를 만들 때 **타입을 반드시 정하고**, 이후에 바꿀 수 없습니다.

```java
int age = 30;
age = "서른";   // 컴파일 에러: String을 int에 넣을 수 없음
```

> **JS와 비교:** JS의 `let x = 1; x = "a";`는 가능하지만 자바에서는 불가능합니다. TypeScript의 타입 선언과 비슷하지만, 자바는 **실행 중에도 타입이 강제**됩니다 (타입 정보가 지워지지 않음).

## 2. 기본형(primitive) 8가지

| 타입 | 크기 | 용도 | 예 |
|------|------|------|----|
| `byte` | 1바이트 | 이진 데이터 | `byte b = 10;` |
| `short` | 2바이트 | 거의 안 씀 | |
| `int` | 4바이트 | **기본 정수** | `int n = 100;` |
| `long` | 8바이트 | 큰 정수 (`L` 접미사) | `long l = 3_000_000_000L;` |
| `float` | 4바이트 | 거의 안 씀 (`f` 접미사) | |
| `double` | 8바이트 | **기본 실수** | `double d = 3.14;` |
| `char` | 2바이트 | 문자 하나 (작은따옴표) | `char c = 'A';` |
| `boolean` | | `true` / `false` | |

처음엔 `int`, `long`, `double`, `boolean`, `char` 다섯 개만 외우면 충분합니다.

**참조형**은 기본형이 아닌 모든 타입입니다. `String`, 배열, 직접 만든 클래스 등이 해당합니다. 값이 아니라 **객체를 가리키는 주소**를 담습니다 (5주차에서 자세히).

## 3. 종합 예제

```java
// File: Types.java
public class Types {
    public static void main(String[] args) {
        int age = 30;
        long big = 3_000_000_000L;      // 숫자 구분용 _ 사용 가능
        double pi = 3.14159;
        boolean ok = true;
        char c = 'A';
        String s = "자바";
        var inferred = 10;              // 타입 추론: int. 지역 변수에서만 사용 가능
        System.out.println(age + " " + big + " " + pi + " " + ok + " " + c + " " + s + " " + inferred);

        // 정수 나눗셈은 소수점을 버린다
        System.out.println(7 / 2);      // 3
        System.out.println(7 / 2.0);    // 3.5
        System.out.println(7 % 2);      // 1 (나머지)

        // 오버플로우: 예외 없이 조용히 틀린 값이 된다
        int max = Integer.MAX_VALUE;
        System.out.println(max + 1);    // -2147483648

        // 형변환
        double d = 9.99;
        int truncated = (int) d;        // 명시적 형변환(캐스팅): 9, 반올림이 아니라 버림
        long widened = age;             // 작은 타입 -> 큰 타입은 자동
        System.out.println(truncated + " " + widened);

        // 부동소수점 오차
        System.out.println(0.1 + 0.2);  // 0.30000000000000004

        // String 비교
        String a = new String("hi");
        String b = new String("hi");
        System.out.println(a == b);         // false: 같은 객체인가?
        System.out.println(a.equals(b));    // true:  같은 내용인가?
    }
}
```

### 꼭 기억할 함정 4가지

1. **정수끼리 나누면 정수**입니다. 평균을 구할 때 `sum / count`가 둘 다 `int`면 소수점이 사라집니다. 한쪽을 `double`로 바꾸세요 (`sum / (double) count`).
2. **오버플로우**는 에러가 나지 않습니다. 큰 수를 다룰 땐 `long`을 쓰세요.
3. **`double`로 돈 계산을 하지 마세요.** 0.1 + 0.2 문제가 생깁니다. 금액은 정수(원 단위)나 `BigDecimal`을 씁니다.
4. **`String`은 `==`가 아니라 `.equals()`로 비교**합니다. `==`는 "같은 객체인가"를 묻습니다.

## 4. 연산자

```java
// File: Operators.java
public class Operators {
    public static void main(String[] args) {
        int x = 10;
        x += 5;           // x = x + 5
        x++;              // 16
        System.out.println(x);

        int a = 5;
        int b = a++;      // b는 5, a는 6 (후위: 쓰고 나서 증가)
        int c = ++a;      // a는 7, c는 7 (전위: 증가하고 나서 씀)
        System.out.println(a + " " + b + " " + c);

        boolean r = (x > 10) && (x < 20) || false;   // &&, ||, ! 논리 연산
        System.out.println(r);

        int age = 20;
        String type = age >= 19 ? "성인" : "미성년";   // 삼항 연산자
        System.out.println(type);

        // 비교 연산자는 === 가 없다. 기본형은 == 하나뿐
        System.out.println(1 == 1);
    }
}
```

## 5. 문자열 기초와 text block

```java
// File: Strings1.java
public class Strings1 {
    public static void main(String[] args) {
        String name = "Haseung";
        System.out.println(name.length());          // 7
        System.out.println(name.toUpperCase());     // HASEUNG
        System.out.println(name.charAt(0));         // H
        System.out.println(name.substring(0, 3));   // Has (끝 인덱스 미포함)
        System.out.println("a,b,c".split(",").length); // 3
        System.out.println(name.contains("seu"));   // true

        // 문자열은 불변(immutable): 메서드는 새 문자열을 돌려준다
        name.toUpperCase();
        System.out.println(name);                   // 여전히 Haseung

        // 여러 줄 문자열 (Java 15+)
        String json = """
                {
                  "name": "Haseung",
                  "age": 30
                }
                """;
        System.out.print(json);

        // 서식 조합
        System.out.println("나이: %d, 이름: %s".formatted(30, name));
    }
}
```

> **JS와 비교:** 템플릿 리터럴 `${name}`은 자바에 없습니다. `+` 연결, `String.format`, `.formatted()`를 씁니다.

## 6. 입력받기 (`Scanner`)

```java
import java.util.Scanner;

public class Echo {   // 파일명 Echo.java
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("이름? ");
        String name = sc.nextLine();
        System.out.print("나이? ");
        int age = sc.nextInt();
        System.out.println(name + "님은 " + (age + 1) + "살이 됩니다.");
    }
}
```

> `nextInt()` 뒤에 `nextLine()`을 쓰면 남은 줄바꿈 문자를 먼저 읽어서 빈 문자열이 나옵니다. 숫자를 읽은 뒤 문자열을 읽어야 한다면 `nextLine()`을 한 번 더 호출해 버리세요.

## 연습문제

1. 섭씨 온도(`double`)를 화씨로 변환해 출력하세요. 공식: `F = C × 9 / 5 + 32`. 정수 나눗셈 함정을 피했는지 확인하세요 (`C = 36.5`).
2. 총 초(`int`, 예: 3725)를 `1시간 2분 5초` 형태로 출력하세요. (`/`와 `%` 사용)
3. 세 과목 점수(`int`)의 평균을 소수 둘째 자리까지 출력하세요 (`%.2f`).
4. `int` 변수에 `Integer.MAX_VALUE`를 넣고 1을 더한 결과를 확인한 뒤, `long`을 써서 올바른 결과가 나오게 고치세요.
5. 사용자에게 이름과 태어난 해를 입력받아 `"홍길동님은 2026년 기준 33살입니다"`를 출력하세요 (`Scanner`).
6. (도전) 문자열 `"Java is fun"`에서 모음(a, e, i, o, u) 개수를 세세요. 반복문은 3주차에 배우니, 오늘은 `replace`와 `length`의 조합으로 풀어 보세요. (힌트: 모음을 지운 문자열의 길이와 원래 길이의 차이)

## 퀴즈

1. `7 / 2`의 결과와 타입은?
2. `int x = 3.5;`가 컴파일 에러인 이유는?
3. `String s1 = new String("a"); String s2 = new String("a");`에서 `s1 == s2`의 값은?
4. `var`는 어디서 쓸 수 있나?

<details>
<summary>정답</summary>

1. `3`, `int` (정수 나눗셈)
2. `double`을 `int`에 넣으면 정보 손실이 있어서 명시적 캐스팅이 필요 (`(int) 3.5`)
3. `false` (서로 다른 객체). 내용 비교는 `equals`
4. 지역 변수 선언에서만 (필드, 메서드 매개변수·반환 타입 불가). 초기값이 있어야 타입을 추론할 수 있음

</details>

## 체크리스트

- [ ] 기본형 5종(`int`, `long`, `double`, `boolean`, `char`)의 용도를 설명할 수 있다
- [ ] 정수 나눗셈과 오버플로우 함정을 직접 재현해 봤다
- [ ] `==`와 `.equals()`의 차이를 설명할 수 있다
- [ ] `Scanner`로 입력을 받는 프로그램을 작성했다
