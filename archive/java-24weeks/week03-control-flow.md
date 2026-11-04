# 3주차. 제어문

**예상 시간:** 8시간 | **목표:** 조건문과 반복문으로 간단한 알고리즘 문제를 풀 수 있다.

## 🖐 먼저 손으로 (읽기 전에 하세요)

> 이 주차의 핵심은 아래 단계를 **직접 타이핑하면서** 깨닫는 것입니다. 아래쪽 설명 문서는 먼저 읽지 않아도 됩니다. 막혔을 때 사전처럼 펼쳐 보세요.

### 1단계. 예측하고 → 치고 → 비교하기 (20분)

1. 아래 코드를 **실행하기 전에** 각 줄의 출력을 노트에 먼저 적습니다.
2. 코드를 한 글자씩 타이핑합니다 (복사·붙여넣기 금지). 오타가 나면 컴파일러가 알려 줍니다.
3. 실행해서 결과를 비교합니다. 예측이 틀린 줄에 ★를 치고, **왜 그렇게 나왔는지 한 문장**으로 적습니다. 이 ★가 오늘 배운 진짜 내용입니다.

```java
// File: Predict03.java
public class Predict03 {
    public static void main(String[] args) {
        int sum = 0;
        for (int i = 0; i < 5; i++) {
            if (i == 2) continue;
            if (i == 4) break;
            sum += i;
        }
        System.out.println(sum);

        int n = 3;
        while (n-- > 0) System.out.print(n + " ");
        System.out.println();

        int k = 10;
        do { k += 5; } while (k < 10);
        System.out.println(k);

        int a = 5;
        String r = switch (a % 3) {
            case 0 -> "zero";
            case 1 -> "one";
            default -> "many";
        };
        System.out.println(r);

        for (int i = 0, j = 10; i < j; i += 3, j -= 3) System.out.print(i + ":" + j + " ");
        System.out.println();
    }
}
```

<details>
<summary>실행 결과 (예측을 적은 뒤에 여세요)</summary>

```text
4
2 1 0
15
many
0:10 3:7
```

</details>

### 2단계. 일부러 망가뜨리기 (20분)

방금 친 코드(또는 아래 설명 문서의 예제)를 하나씩 **고장 내 보고**, 어떤 메시지가 나오는지 확인합니다. 먼저 어떻게 될지 예상한 뒤 실행하세요. 메시지를 외울 필요는 없습니다. 에러 메시지가 **어떤 모양이고 어디를 가리키는지** 눈에 익히는 것이 목표입니다.

| 이렇게 바꿔 보세요 | 어떻게 될까요? |
|---|---|
| `if (x = 5)` | `incompatible types: int cannot be converted to boolean` — JS 와 달리 오타가 컴파일 단계에서 걸린다 |
| `if (1) { ... }` | 같은 에러. 자바의 조건은 반드시 boolean |
| `switch` 표현식에서 `default` 줄 삭제 | `the switch expression does not cover all possible input values` |
| `for (int i = 0; i < 3; i++);` (끝에 세미콜론) 바로 다음 줄에 `System.out.println("hi");` | `hi` 가 3번이 아니라 **1번**만 출력된다. 세미콜론이 빈 반복문 본문이 된다 |
| `for` 문 바깥에서 `System.out.println(i);` | `cannot find symbol` — 변수는 선언된 블록 `{}` 안에서만 산다 |
| `while (true) { }` 실행 후 터미널에서 `Ctrl+C` | 무한 루프. 멈추는 방법을 아는 것도 실력이다 |

### 3단계. 빈칸에서 만들기 — 자동 채점 (1~3시간)

`../../labs/week03/` 폴더의 `Mine.java`를 열어 `// 여기를 고치세요` 부분을 직접 구현합니다. 구현하고 채점기를 돌려 ✅를 하나씩 늘려 가세요.

```bash
cd labs
./check.sh 03
```

- 처음에는 거의 다 ❌입니다. 정상입니다. **❌ 메시지의 "기대 / 실제"를 읽고** 위에서부터 하나씩 고치세요.
- 컴파일 에러가 나면 첫 번째 에러부터 읽으세요.
- 15분 넘게 막혔을 때만 `../../labs/solutions/week03/`를 열어 보세요. 보고 나서는 **닫고 다시 직접 쳐서** 구현하세요.


---

## 1. `if` / `else`

```java
// File: IfDemo.java
public class IfDemo {
    public static void main(String[] args) {
        int score = 85;
        String grade;
        if (score >= 90) {
            grade = "A";
        } else if (score >= 80) {
            grade = "B";
        } else {
            grade = "C";
        }
        System.out.println(grade);   // B
    }
}
```

- 조건은 반드시 `boolean`입니다. JS처럼 `if (1)`, `if ("")`, `if (null)`은 **컴파일 에러**입니다.
- 한 줄짜리 본문도 **중괄호를 항상 쓰는 습관**을 들이세요. 나중에 줄을 추가할 때 버그가 생기는 걸 막아 줍니다.

## 2. `switch` (표현식)

Java 14+의 화살표 `switch`를 쓰세요. `break`를 빼먹어서 생기던 fall-through 버그가 없습니다.

```java
// File: SwitchDemo.java
public class SwitchDemo {
    public static void main(String[] args) {
        String day = "TUE";

        // 값을 돌려주는 switch 표현식
        int order = switch (day) {
            case "MON" -> 1;
            case "TUE", "WED" -> 2;      // 여러 값을 한 번에
            default -> 0;
        };
        System.out.println(order);       // 2

        // 여러 문장이 필요하면 블록 + yield
        String msg = switch (order) {
            case 1 -> "월요일";
            case 2 -> {
                String s = "화/수";
                yield s + "요일";
            }
            default -> "기타";
        };
        System.out.println(msg);
    }
}
```

> 예전 방식(`case X:` + `break;`)은 오래된 코드에서 자주 보입니다. 읽을 수만 있으면 됩니다.

## 3. 반복문

```java
// File: LoopDemo.java
public class LoopDemo {
    public static void main(String[] args) {
        // for: 횟수를 알 때
        for (int i = 1; i <= 3; i++) {
            System.out.println("i = " + i);
        }

        // 향상된 for: 배열/컬렉션 순회
        int[] nums = {3, 1, 4, 1, 5};
        int sum = 0;
        for (int n : nums) {
            sum += n;
        }
        System.out.println("합: " + sum);

        // while: 조건이 참인 동안
        int count = 0;
        while (count < 3) {
            count++;
        }
        System.out.println("count = " + count);

        // do-while: 최소 한 번은 실행
        int k = 10;
        do {
            k++;
        } while (k < 5);
        System.out.println("k = " + k);   // 11

        // break / continue
        for (int i = 1; i <= 10; i++) {
            if (i % 2 == 0) continue;      // 짝수는 건너뛰기
            if (i > 7) break;              // 7 초과면 반복 종료
            System.out.print(i + " ");     // 1 3 5 7
        }
        System.out.println();

        // 중첩 반복문과 라벨
        outer:
        for (int i = 2; i <= 9; i++) {
            for (int j = 1; j <= 9; j++) {
                if (i * j > 20) break outer;   // 바깥 반복문까지 한 번에 종료
                System.out.print(i * j + "\t");
            }
            System.out.println();
        }
        System.out.println();
    }
}
```

## 4. 대표 알고리즘 패턴

```java
// File: Patterns.java
public class Patterns {
    public static void main(String[] args) {
        // FizzBuzz
        for (int i = 1; i <= 15; i++) {
            if (i % 15 == 0) System.out.println("FizzBuzz");
            else if (i % 3 == 0) System.out.println("Fizz");
            else if (i % 5 == 0) System.out.println("Buzz");
            else System.out.println(i);
        }

        // 최댓값 찾기
        int[] data = {7, 2, 9, 4};
        int max = data[0];
        for (int v : data) {
            if (v > max) max = v;
        }
        System.out.println("max = " + max);

        // 소수 판별
        int target = 29;
        boolean prime = target > 1;
        for (int d = 2; d * d <= target; d++) {
            if (target % d == 0) {
                prime = false;
                break;
            }
        }
        System.out.println(target + " 소수? " + prime);

        // 별 삼각형
        for (int row = 1; row <= 4; row++) {
            System.out.println("*".repeat(row));
        }
    }
}
```

## 5. 난수와 간단한 게임

```java
// File: Dice.java
import java.util.Random;

public class Dice {
    public static void main(String[] args) {
        Random rnd = new Random();
        int a = rnd.nextInt(6) + 1;   // 1~6
        int b = rnd.nextInt(6) + 1;
        System.out.println(a + " + " + b + " = " + (a + b));
    }
}
```

## 연습문제

1. 1부터 100까지 짝수의 합을 `for`로 구하세요. 같은 문제를 `while`로도 풀어 보세요.
2. 구구단 2~9단을 `2 x 1 = 2` 형태로 출력하세요.
3. 정수 하나를 입력받아 **윤년 여부**를 출력하세요. (4로 나누어떨어지고 100으로 나누어떨어지지 않거나, 400으로 나누어떨어지면 윤년)
4. `1~100` 난수를 하나 정해 두고, 사용자가 맞힐 때까지 입력을 받으며 `UP`/`DOWN`을 알려 주는 **숫자 맞히기 게임**을 만드세요. 시도 횟수도 출력합니다.
5. 입력받은 정수의 **각 자리 숫자의 합**을 구하세요 (예: 1234 → 10). 힌트: `% 10`과 `/ 10`
6. 별로 **피라미드**를 출력하세요 (높이 5). 공백과 `*`의 개수 규칙을 찾아야 합니다.
7. (도전) 1~1000 사이의 모든 **소수**를 출력하고 개수를 세세요.

## 퀴즈

1. `if (x = 5)`는 왜 자바에서 컴파일 에러인가?
2. `switch` 표현식에서 값을 돌려주려면 블록 안에서 어떤 키워드를 쓰나?
3. `continue`와 `break`의 차이는?
4. `for (int n : arr)`는 어떤 경우에 쓰나? 단점은?

<details>
<summary>정답</summary>

1. `x = 5`는 대입식이고 결과가 `int`라서 `boolean` 조건이 아님. (JS와 달리 오타 방지에 도움이 됨)
2. `yield`
3. `continue`는 현재 반복만 건너뛰고 다음으로, `break`는 반복문 자체를 종료
4. 배열/컬렉션의 모든 요소를 순서대로 읽을 때. 인덱스를 알 수 없고, 값을 바꾸더라도 원본 배열의 요소는 바뀌지 않는다 (기본형일 때)

</details>

## 체크리스트

- [ ] `switch` 표현식(화살표 + `yield`)을 쓸 수 있다
- [ ] `for`, `while`, `do-while`, 향상된 `for`를 각각 쓸 수 있다
- [ ] 숫자 맞히기 게임을 직접 완성했다
- [ ] FizzBuzz와 소수 판별을 외우지 않고 다시 작성할 수 있다
