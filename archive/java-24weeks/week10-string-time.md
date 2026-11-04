# 10주차. 문자열·날짜·래퍼 클래스

**예상 시간:** 8시간 | **목표:** 자주 쓰는 표준 API를 문서에서 찾아 쓸 수 있고, 날짜·시간을 올바른 클래스로 다룬다.

## 🖐 먼저 손으로 (읽기 전에 하세요)

> 이 주차의 핵심은 아래 단계를 **직접 타이핑하면서** 깨닫는 것입니다. 아래쪽 설명 문서는 먼저 읽지 않아도 됩니다. 막혔을 때 사전처럼 펼쳐 보세요.

### 1단계. 예측하고 → 치고 → 비교하기 (20분)

1. 아래 코드를 **실행하기 전에** 각 줄의 출력을 노트에 먼저 적습니다.
2. 코드를 한 글자씩 타이핑합니다 (복사·붙여넣기 금지). 오타가 나면 컴파일러가 알려 줍니다.
3. 실행해서 결과를 비교합니다. 예측이 틀린 줄에 ★를 치고, **왜 그렇게 나왔는지 한 문장**으로 적습니다. 이 ★가 오늘 배운 진짜 내용입니다.

```java
// File: Predict10.java
import java.time.LocalDate;

public class Predict10 {
    public static void main(String[] args) {
        String s = "Java";
        s.concat("21");
        System.out.println(s);
        System.out.println(s.concat("21"));
        System.out.println("a,b,,c,,".split(",").length);
        System.out.println("  x ".strip().length());
        System.out.println("banana".indexOf("an") + " " + "banana".lastIndexOf("an"));
        System.out.println("b".compareTo("a") + " " + "apple".compareTo("apricot"));
        Integer p = 127, q = 127, r = 128, t = 128;
        System.out.println((p == q) + " " + (r == t) + " " + r.equals(t));
        System.out.println(Math.round(2.5) + " " + Math.round(-2.5) + " " + Math.round(2.4));
        System.out.println(LocalDate.of(2024, 1, 31).plusMonths(1));
        System.out.println(7 / 2 * 2.0);
    }
}
```

<details>
<summary>실행 결과 (예측을 적은 뒤에 여세요)</summary>

```text
Java
Java21
4
1
1 3
1 -2
true false true
3 -2 2
2024-02-29
6.0
```

</details>

### 2단계. 일부러 망가뜨리기 (20분)

방금 친 코드(또는 아래 설명 문서의 예제)를 하나씩 **고장 내 보고**, 어떤 메시지가 나오는지 확인합니다. 먼저 어떻게 될지 예상한 뒤 실행하세요. 메시지를 외울 필요는 없습니다. 에러 메시지가 **어떤 모양이고 어디를 가리키는지** 눈에 익히는 것이 목표입니다.

| 이렇게 바꿔 보세요 | 어떻게 될까요? |
|---|---|
| `"abc".substring(2, 10)` | `StringIndexOutOfBoundsException: begin 2, end 10, length 3` |
| `Integer.parseInt("12a")` | `NumberFormatException: For input string: "12a"` |
| `LocalDate.of(2026, 13, 1)` | `DateTimeException: Invalid value for MonthOfYear (valid values 1 - 12): 13` |
| `LocalDate.of(2026, 2, 30)` | `DateTimeException: Invalid date 'FEBRUARY 30'` |
| `Integer a = null; int b = a;` | `NullPointerException` — 언박싱에서 터진다 |
| `new BigDecimal(0.1)` 과 `new BigDecimal("0.1")` 를 각각 출력 | 앞쪽은 `0.1000000000000000055511151231257827...` 처럼 길게 나온다. **문자열로 만들어야 하는 이유** |

### 3단계. 빈칸에서 만들기 — 자동 채점 (1~3시간)

`../../labs/week10/` 폴더의 `Mine.java`를 열어 `// 여기를 고치세요` 부분을 직접 구현합니다. 구현하고 채점기를 돌려 ✅를 하나씩 늘려 가세요.

```bash
cd labs
./check.sh 10
```

- 처음에는 거의 다 ❌입니다. 정상입니다. **❌ 메시지의 "기대 / 실제"를 읽고** 위에서부터 하나씩 고치세요.
- 컴파일 에러가 나면 첫 번째 에러부터 읽으세요.
- 15분 넘게 막혔을 때만 `../../labs/solutions/week10/`를 열어 보세요. 보고 나서는 **닫고 다시 직접 쳐서** 구현하세요.


---

## 1. String 심화

`String`은 **불변**입니다. 수정처럼 보이는 모든 메서드는 새 객체를 돌려줍니다.

```java
// File: StringDemo.java
import java.util.Arrays;

public class StringDemo {
    public static void main(String[] args) {
        String s = "  Hello, Java World  ";

        System.out.println(s.strip());                       // 앞뒤 공백 제거 (trim 대신 strip 권장)
        System.out.println(s.isBlank());                     // false (공백만 있으면 true)
        System.out.println("".isEmpty());                    // true
        System.out.println(s.strip().replace("Java", "자바"));
        System.out.println(s.indexOf("Java"));               // 9 (없으면 -1)
        System.out.println(s.strip().startsWith("Hello"));   // true
        System.out.println("a-b-c".split("-").length);       // 3
        System.out.println(Arrays.toString("1,2,,3".split(","))); // [1, 2, , 3]
        System.out.println(String.join("/", "a", "b", "c")); // a/b/c
        System.out.println("ab".repeat(3));                  // ababab
        System.out.println("Java".equalsIgnoreCase("JAVA")); // true
        System.out.println("apple".compareTo("banana") < 0); // true: 사전순 비교
        System.out.println(String.valueOf(123) + 1);         // "1231"

        // 문자 단위 순회
        int vowels = 0;
        for (char c : "education".toCharArray()) {
            if ("aeiou".indexOf(c) >= 0) vowels++;
        }
        System.out.println("모음: " + vowels);                // 5

        // 서식
        System.out.println(String.format("%05d | %-6s| %8.2f", 42, "ab", 3.14159));
    }
}
```

## 2. StringBuilder

반복문에서 `+`로 문자열을 계속 이어붙이면 **그때마다 새 객체**를 만듭니다. 많이 이어붙일 땐 `StringBuilder`를 씁니다.

```java
// File: BuilderDemo.java
public class BuilderDemo {
    public static void main(String[] args) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            sb.append(i).append(',');
        }
        sb.setLength(sb.length() - 1);          // 마지막 쉼표 제거
        System.out.println(sb);                 // 1,2,3,4,5
        System.out.println(sb.reverse());       // 5,4,3,2,1 (원본이 바뀐다)

        // 성능 차이 체감 (결과는 환경마다 다름)
        long t1 = System.nanoTime();
        String s = "";
        for (int i = 0; i < 20_000; i++) s += "x";
        long t2 = System.nanoTime();
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < 20_000; i++) b.append("x");
        long t3 = System.nanoTime();
        System.out.println("+ 연결: " + (t2 - t1) / 1_000_000 + "ms, StringBuilder: " + (t3 - t2) / 1_000_000 + "ms");
    }
}
```

단순한 한두 번의 연결은 `+`가 읽기 좋습니다. **반복문 안에서의 누적**만 `StringBuilder`를 쓰세요.

## 3. 래퍼 클래스와 오토박싱

기본형마다 대응되는 참조형이 있습니다: `int`↔`Integer`, `double`↔`Double`, `boolean`↔`Boolean`, `long`↔`Long`, `char`↔`Character`. 컬렉션(11주차)은 기본형을 담지 못하고 래퍼 타입을 담기 때문에 필요합니다.

```java
// File: WrapperDemo.java
public class WrapperDemo {
    public static void main(String[] args) {
        Integer a = 10;          // 오토박싱: int -> Integer
        int b = a;               // 언박싱: Integer -> int

        System.out.println(Integer.parseInt("123") + 1);       // 124
        System.out.println(Double.parseDouble("3.5"));
        System.out.println(Integer.toString(255, 2));          // 11111111
        System.out.println(Integer.MAX_VALUE + " " + Integer.MIN_VALUE);
        System.out.println(Integer.compare(3, 5));             // -1
        System.out.println(Character.isDigit('7'));            // true

        // 함정 1: 래퍼끼리 == 비교
        Integer x = 127, y = 127;
        Integer p = 1000, q = 1000;
        System.out.println(x == y);       // true  (-128~127은 캐시된 같은 객체)
        System.out.println(p == q);       // false (다른 객체)
        System.out.println(p.equals(q));  // true  -> 래퍼는 항상 equals

        // 함정 2: null 언박싱
        Integer none = null;
        try {
            int n = none;                 // NullPointerException
        } catch (NullPointerException e) {
            System.out.println("null 언박싱 NPE");
        }
    }
}
```

## 4. Math

```java
// File: MathDemo.java
public class MathDemo {
    public static void main(String[] args) {
        System.out.println(Math.max(3, 7));           // 7
        System.out.println(Math.abs(-5));             // 5
        System.out.println(Math.pow(2, 10));          // 1024.0 (double)
        System.out.println(Math.sqrt(16));            // 4.0
        System.out.println(Math.round(2.5));          // 3
        System.out.println(Math.round(-2.5));         // -2 (주의: 0 방향이 아니라 +무한대 방향 반올림)
        System.out.println(Math.floor(-1.1));         // -2.0
        System.out.println(Math.ceil(1.1));           // 2.0
        System.out.println(Math.floorMod(-7, 3));     // 2 (-7 % 3 은 -1)

        try {
            Math.addExact(Integer.MAX_VALUE, 1);      // 오버플로우를 예외로 알려 준다
        } catch (ArithmeticException e) {
            System.out.println("오버플로우 감지");
        }
    }
}
```

돈 계산에는 `double` 대신 `BigDecimal`을 씁니다 (`new BigDecimal("0.1").add(new BigDecimal("0.2"))` → `0.3`). **문자열로 생성**하는 것이 핵심입니다.

## 5. 날짜와 시간 (`java.time`)

`java.util.Date`, `Calendar`는 쓰지 마세요. Java 8+의 `java.time`이 표준입니다. 모든 객체가 **불변**입니다.

| 클래스 | 의미 | 예 |
|--------|------|----|
| `LocalDate` | 날짜 | 2026-10-20 |
| `LocalTime` | 시간 | 14:30:00 |
| `LocalDateTime` | 날짜+시간 (시간대 없음) | 2026-10-20T14:30 |
| `ZonedDateTime` | 날짜+시간+시간대 | 2026-10-20T14:30+09:00[Asia/Seoul] |
| `Instant` | UTC 기준 한 시점 (타임스탬프) | |
| `Duration` / `Period` | 시간 간격 / 날짜 간격 | |

```java
// File: TimeDemo.java
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class TimeDemo {
    public static void main(String[] args) {
        LocalDate d = LocalDate.of(2026, 10, 20);
        System.out.println(d.plusDays(30));                       // 2026-11-19
        System.out.println(d.getDayOfWeek());                     // TUESDAY
        System.out.println(d.withDayOfMonth(1));                  // 2026-10-01
        System.out.println(d.isLeapYear());                       // false
        System.out.println(d.getDayOfWeek() == DayOfWeek.SATURDAY);

        LocalDate birth = LocalDate.of(1996, 3, 15);
        Period age = Period.between(birth, d);
        System.out.println(age.getYears() + "살 " + age.getMonths() + "개월");
        System.out.println(ChronoUnit.DAYS.between(birth, d) + "일째");

        LocalDateTime dt = LocalDateTime.of(2026, 10, 20, 14, 30);
        System.out.println(dt.plusHours(10));                     // 2026-10-21T00:30

        Duration dur = Duration.between(dt, dt.plusMinutes(135));
        System.out.println(dur.toHours() + "시간 " + dur.toMinutesPart() + "분");

        // 포맷과 파싱
        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH:mm");
        System.out.println(dt.format(f));                         // 2026년 10월 20일 14:30
        System.out.println(LocalDate.parse("2026-12-25").getMonth()); // DECEMBER
        System.out.println(LocalDate.parse("2026/12/25", DateTimeFormatter.ofPattern("yyyy/MM/dd")));

        // 시간대
        ZonedDateTime seoul = ZonedDateTime.of(dt, ZoneId.of("Asia/Seoul"));
        System.out.println(seoul.withZoneSameInstant(ZoneId.of("America/New_York")).toLocalTime());
    }
}
```

> **JS와 비교:** JS의 `Date`는 가변이고 월이 0부터 시작하는 등 함정이 많습니다. `java.time`은 월이 1부터이고 불변이며, 날짜/시간/시간대가 타입으로 분리되어 있어 훨씬 안전합니다.

규칙: DB·API에 저장하거나 서버끼리 주고받을 때는 `Instant`(또는 UTC), 사용자에게 보여 줄 때만 시간대를 적용합니다.

## 연습문제

1. 문자열이 **회문**(앞뒤로 읽어도 같음, 예: `level`)인지 판별하는 메서드를 `StringBuilder.reverse()`와 `charAt` 두 가지 방식으로 작성하세요.
2. 영어 문장에서 **단어 개수**와 가장 긴 단어를 찾으세요 (`split("\\s+")`).
3. 이메일 문자열 `"user@example.com"`에서 아이디(`user`)와 도메인(`example.com`)을 분리하세요. `@`가 없으면 예외를 던지세요.
4. 오늘부터 크리스마스까지 **남은 일수**를 계산하세요.
5. 생년월일을 입력받아 **만 나이**와 **다음 생일까지 남은 일수**를 출력하세요.
6. 9주차의 도서 대출 프로젝트에 **반납 기한**(대출일 + 14일)을 추가하고, 연체 일수를 계산하세요.
7. `0.1 + 0.2`와 `new BigDecimal("0.1").add(new BigDecimal("0.2"))`의 결과를 비교하세요.

## 퀴즈

1. `String s = "a"; s.toUpperCase();` 실행 후 `s`의 값은?
2. 반복문 안에서 문자열을 이어붙일 때 권장되는 클래스는?
3. `Integer a = 1000, b = 1000; a == b`의 결과는? 올바른 비교는?
4. `LocalDate`와 `Instant`의 차이는?

<details>
<summary>정답</summary>

1. `"a"` (불변이라 원본은 그대로. 결과를 변수에 받아야 한다)
2. `StringBuilder`
3. `false` (캐시 범위를 벗어난 서로 다른 객체). `a.equals(b)` 또는 `a.intValue() == b.intValue()`
4. `LocalDate`는 시간대 없는 달력상 날짜, `Instant`는 UTC 기준의 정확한 한 시점

</details>

## 체크리스트

- [ ] 자바 API 문서에서 필요한 메서드를 직접 찾아 썼다
- [ ] `String` 불변성과 `StringBuilder`의 용도를 설명할 수 있다
- [ ] `Integer` 비교 함정을 재현했다
- [ ] `LocalDate`로 날짜 계산 문제를 해결했다
