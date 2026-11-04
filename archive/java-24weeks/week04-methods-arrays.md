# 4주차. 메서드와 배열

**예상 시간:** 8시간 | **목표:** 코드를 메서드로 나누고, 배열을 다룰 수 있다. 4주차 끝에는 **마일스톤 점검**이 있습니다.

## 🖐 먼저 손으로 (읽기 전에 하세요)

> 이 주차의 핵심은 아래 단계를 **직접 타이핑하면서** 깨닫는 것입니다. 아래쪽 설명 문서는 먼저 읽지 않아도 됩니다. 막혔을 때 사전처럼 펼쳐 보세요.

### 1단계. 예측하고 → 치고 → 비교하기 (20분)

1. 아래 코드를 **실행하기 전에** 각 줄의 출력을 노트에 먼저 적습니다.
2. 코드를 한 글자씩 타이핑합니다 (복사·붙여넣기 금지). 오타가 나면 컴파일러가 알려 줍니다.
3. 실행해서 결과를 비교합니다. 예측이 틀린 줄에 ★를 치고, **왜 그렇게 나왔는지 한 문장**으로 적습니다. 이 ★가 오늘 배운 진짜 내용입니다.

```java
// File: Predict04.java
import java.util.Arrays;

public class Predict04 {
    static void a(int x) { x = 99; }
    static void b(int[] arr) { arr[0] = 99; }
    static void c(int[] arr) { arr = new int[]{7, 7}; }
    static int f(int n) { return n <= 1 ? 1 : n * f(n - 1); }

    public static void main(String[] args) {
        int x = 1;
        a(x);
        System.out.println(x);
        int[] p = {1, 2};
        b(p);
        System.out.println(Arrays.toString(p));
        c(p);
        System.out.println(Arrays.toString(p));
        int[] q = p;
        q[1] = 5;
        System.out.println(p[1]);
        int[] r = p.clone();
        r[1] = 0;
        System.out.println(p[1]);
        System.out.println(Arrays.toString(new String[2]));
        System.out.println(f(5));
        int[] e = {1, 2, 3};
        System.out.println((e == e.clone()) + " " + Arrays.equals(e, e.clone()));
    }
}
```

<details>
<summary>실행 결과 (예측을 적은 뒤에 여세요)</summary>

```text
1
[99, 2]
[99, 2]
5
5
[null, null]
120
false true
```

</details>

### 2단계. 일부러 망가뜨리기 (20분)

방금 친 코드(또는 아래 설명 문서의 예제)를 하나씩 **고장 내 보고**, 어떤 메시지가 나오는지 확인합니다. 먼저 어떻게 될지 예상한 뒤 실행하세요. 메시지를 외울 필요는 없습니다. 에러 메시지가 **어떤 모양이고 어디를 가리키는지** 눈에 익히는 것이 목표입니다.

| 이렇게 바꿔 보세요 | 어떻게 될까요? |
|---|---|
| `int[] a = new int[3]; a[3] = 1;` | `ArrayIndexOutOfBoundsException: Index 3 out of bounds for length 3` — 몇 번째 줄인지 스택 트레이스를 읽어 보세요 |
| `a.length()` (배열에 괄호 붙이기) | `cannot find symbol` — 배열의 길이는 필드라서 괄호가 없다. 반대로 `"abc".length` 도 에러 |
| `int` 반환 메서드에서 `return` 줄 지우기 | `missing return statement` |
| `add(int a, int b)` 를 `add(1)` 로 호출 | `method add in class ... cannot be applied to given types` — 필요한 것/받은 것을 비교해 보여 준다 |
| `System.out.println(p);` (배열을 그냥 출력) | `[I@6d06d69c` 같은 주소 표현. `Arrays.toString` 이 필요한 이유 |
| `f` 의 종료 조건 `n <= 1` 을 지우고 `f(5)` | `StackOverflowError` — 재귀의 종료 조건이 왜 필요한지 몸으로 확인 |

### 3단계. 빈칸에서 만들기 — 자동 채점 (1~3시간)

`../../labs/week04/` 폴더의 `Mine.java`를 열어 `// 여기를 고치세요` 부분을 직접 구현합니다. 구현하고 채점기를 돌려 ✅를 하나씩 늘려 가세요.

```bash
cd labs
./check.sh 04
```

- 처음에는 거의 다 ❌입니다. 정상입니다. **❌ 메시지의 "기대 / 실제"를 읽고** 위에서부터 하나씩 고치세요.
- 컴파일 에러가 나면 첫 번째 에러부터 읽으세요.
- 15분 넘게 막혔을 때만 `../../labs/solutions/week04/`를 열어 보세요. 보고 나서는 **닫고 다시 직접 쳐서** 구현하세요.


---

## 1. 메서드

```java
// File: Methods.java
public class Methods {
    // 반환 타입 / 이름 / 매개변수
    static int add(int a, int b) {
        return a + b;
    }

    // 반환값이 없으면 void
    static void greet(String name) {
        System.out.println("안녕, " + name);
    }

    // 오버로딩: 이름은 같고 매개변수가 다른 메서드
    static int max(int a, int b) { return a > b ? a : b; }
    static double max(double a, double b) { return a > b ? a : b; }

    // 가변 인자 (내부적으로는 배열)
    static int sum(int... nums) {
        int total = 0;
        for (int n : nums) total += n;
        return total;
    }

    // 재귀
    static int factorial(int n) {
        if (n <= 1) return 1;
        return n * factorial(n - 1);
    }

    public static void main(String[] args) {
        System.out.println(add(2, 3));          // 5
        greet("자바");
        System.out.println(max(3, 7));          // 7   (int 버전)
        System.out.println(max(2.5, 1.5));      // 2.5 (double 버전)
        System.out.println(sum());              // 0
        System.out.println(sum(1, 2, 3, 4));    // 10
        System.out.println(factorial(5));       // 120
    }
}
```

- 지금은 `static`을 "객체 없이 부를 수 있는 메서드"로만 이해하세요. 5주차에 제대로 다룹니다.
- 자바에는 **함수만 단독으로 존재하지 않고** 항상 클래스 안에 있습니다.

### 값에 의한 전달 (pass by value)

자바는 **항상 값을 복사해서 전달**합니다. 기본형이면 값이 복사되고, 참조형이면 **주소가 복사**됩니다.

```java
// File: PassByValue.java
import java.util.Arrays;

public class PassByValue {
    static void changeInt(int x) {
        x = 100;                // 복사본만 바뀐다
    }

    static void changeArray(int[] arr) {
        arr[0] = 100;           // 같은 배열을 가리키므로 원본이 바뀐다
    }

    static void reassign(int[] arr) {
        arr = new int[]{9, 9};  // 복사된 주소를 다른 배열로 바꿨을 뿐, 원본은 그대로
    }

    public static void main(String[] args) {
        int n = 1;
        changeInt(n);
        System.out.println(n);                          // 1

        int[] a = {1, 2, 3};
        changeArray(a);
        System.out.println(Arrays.toString(a));         // [100, 2, 3]

        reassign(a);
        System.out.println(Arrays.toString(a));         // [100, 2, 3]
    }
}
```

> **JS와 비교:** JS의 객체 전달과 똑같은 동작입니다. 기본형은 복사, 객체는 참조(주소) 복사.

## 2. 배열

```java
// File: Arrays1.java
import java.util.Arrays;

public class Arrays1 {
    public static void main(String[] args) {
        int[] scores = new int[3];            // 크기 3, 기본값 0으로 채워짐
        scores[0] = 90;
        scores[1] = 80;
        System.out.println(scores.length);    // 3 (length는 필드, 괄호 없음)

        int[] primes = {2, 3, 5, 7};          // 선언과 동시에 초기화
        String[] names = new String[2];       // 참조형 배열의 기본값은 null
        System.out.println(names[0]);         // null

        // 배열 출력은 Arrays.toString
        System.out.println(primes);                    // [I@... (주소)
        System.out.println(Arrays.toString(primes));   // [2, 3, 5, 7]

        // 정렬, 복사, 비교
        int[] unsorted = {5, 2, 9, 1};
        Arrays.sort(unsorted);
        System.out.println(Arrays.toString(unsorted));          // [1, 2, 5, 9]
        int[] copy = Arrays.copyOf(unsorted, 6);                // 길이 6으로 복사, 남는 칸은 0
        System.out.println(Arrays.toString(copy));
        System.out.println(Arrays.equals(unsorted, new int[]{1, 2, 5, 9})); // true
        System.out.println(Arrays.binarySearch(unsorted, 5));   // 2 (정렬된 배열에서만)

        // 2차원 배열
        int[][] grid = new int[3][4];
        grid[1][2] = 7;
        for (int[] row : grid) {
            System.out.println(Arrays.toString(row));
        }

        // 범위를 벗어나면 런타임 예외
        try {
            System.out.println(primes[4]);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("에러: " + e.getMessage());   // Index 4 out of bounds for length 4
        }
    }
}
```

**배열의 한계:** 크기를 만든 뒤 바꿀 수 없습니다. 크기가 변하는 데이터는 11주차의 `ArrayList`를 씁니다.

## 3. 메서드 + 배열 조합 예제

```java
// File: Stats.java
public class Stats {
    static int max(int[] a) {
        int m = a[0];
        for (int v : a) if (v > m) m = v;
        return m;
    }

    static double average(int[] a) {
        int sum = 0;
        for (int v : a) sum += v;
        return (double) sum / a.length;
    }

    static int[] reverse(int[] a) {
        int[] r = new int[a.length];
        for (int i = 0; i < a.length; i++) {
            r[a.length - 1 - i] = a[i];
        }
        return r;
    }

    public static void main(String[] args) {
        int[] scores = {80, 95, 70, 100};
        System.out.println("최대: " + max(scores));
        System.out.printf("평균: %.2f%n", average(scores));
        System.out.println(java.util.Arrays.toString(reverse(scores)));
    }
}
```

설계 규칙: **메서드 하나는 한 가지 일만** 합니다. 입력 받기, 계산하기, 출력하기를 한 메서드에 섞지 마세요.

## 연습문제

1. `boolean isPrime(int n)` 메서드를 만들고, 이를 이용해 1~100의 소수를 출력하세요.
2. `int[] a`가 오름차순으로 정렬되어 있는지 검사하는 `isSorted(int[])`를 작성하세요.
3. 배열에서 **두 번째로 큰 값**을 찾는 메서드를 만드세요 (정렬하지 않고 한 번의 순회로).
4. 배열에서 특정 값의 **모든 위치(인덱스)**를 `int[]`로 돌려주는 `indexesOf(int[] a, int target)`를 작성하세요. (힌트: 개수를 먼저 센 뒤 배열을 만든다)
5. 피보나치 수열의 n번째 값을 **재귀**와 **반복문** 두 가지로 구현하고, n = 40일 때 재귀가 얼마나 느린지 체감해 보세요.
6. 로또 번호 생성기: 1~45 중 중복 없는 6개를 뽑아 오름차순으로 출력하세요. (`Random`, `Arrays.sort`)
7. 3×3 행렬 두 개를 더하는 메서드를 작성하세요 (2차원 배열).

## 마일스톤 점검 (4주 끝)

다음 중 하나를 **도움 없이** 50줄 안팎으로 완성하세요.

- **숫자 야구:** 컴퓨터가 서로 다른 3자리 숫자를 정하고, 사용자가 맞힐 때까지 `n 스트라이크 m 볼`을 알려 줍니다.
- **성적 관리:** 학생 5명의 3과목 점수를 2차원 배열에 담고, 학생별 평균과 과목별 최고점을 출력합니다. 메서드로 나눠서 작성합니다.

## 퀴즈

1. `int[] a = new int[5];`에서 `a[2]`의 값은?
2. 자바는 call by reference인가?
3. 오버로딩이 성립하려면 무엇이 달라야 하나? 반환 타입만 다르면 되나?
4. `String[] s = new String[3];`의 요소 기본값은?

<details>
<summary>정답</summary>

1. `0`
2. 아니다. 항상 call by value다. 참조형은 "주소 값"이 복사된다.
3. 매개변수의 타입·개수·순서. 반환 타입만 다른 것은 오버로딩이 아니고 컴파일 에러
4. `null`

</details>

## 체크리스트

- [ ] 메서드 작성 시 매개변수·반환 타입을 스스로 설계할 수 있다
- [ ] pass-by-value 예제의 출력 결과를 실행 전에 예측했다
- [ ] 배열 인덱스 범위 예외를 재현하고 읽었다
- [ ] 마일스톤 과제를 완성했다
