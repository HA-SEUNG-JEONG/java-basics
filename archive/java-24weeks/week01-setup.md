# 1주차. 개발 환경과 첫 프로그램

**예상 시간:** 8시간 | **목표:** JDK를 설치하고, 자바 프로그램이 "작성 → 컴파일 → 실행"되는 흐름을 설명할 수 있다.

## 🖐 먼저 손으로 (읽기 전에 하세요)

> 이 주차의 핵심은 아래 단계를 **직접 타이핑하면서** 깨닫는 것입니다. 아래쪽 설명 문서는 먼저 읽지 않아도 됩니다. 막혔을 때 사전처럼 펼쳐 보세요.

### 1단계. 예측하고 → 치고 → 비교하기 (20분)

1. 아래 코드를 **실행하기 전에** 각 줄의 출력을 노트에 먼저 적습니다.
2. 코드를 한 글자씩 타이핑합니다 (복사·붙여넣기 금지). 오타가 나면 컴파일러가 알려 줍니다.
3. 실행해서 결과를 비교합니다. 예측이 틀린 줄에 ★를 치고, **왜 그렇게 나왔는지 한 문장**으로 적습니다. 이 ★가 오늘 배운 진짜 내용입니다.

```java
// File: Predict01.java
public class Predict01 {
    public static void main(String[] args) {
        System.out.print("A");
        System.out.print("B");
        System.out.println("C");
        System.out.println("D\"E\"");
        System.out.printf("%5d|%-5d|%05d%n", 42, 42, 42);
        System.out.println(1 + 2 + "3" + 4 + 5);
    }
}
```

<details>
<summary>실행 결과 (예측을 적은 뒤에 여세요)</summary>

```text
ABC
D"E"
   42|42   |00042
3345
```

</details>

### 2단계. 일부러 망가뜨리기 (20분)

방금 친 코드(또는 아래 설명 문서의 예제)를 하나씩 **고장 내 보고**, 어떤 메시지가 나오는지 확인합니다. 먼저 어떻게 될지 예상한 뒤 실행하세요. 메시지를 외울 필요는 없습니다. 에러 메시지가 **어떤 모양이고 어디를 가리키는지** 눈에 익히는 것이 목표입니다.

| 이렇게 바꿔 보세요 | 어떻게 될까요? |
|---|---|
| `println` 의 `p` 를 대문자 `P` 로 바꾸기 | `cannot find symbol` — 어떤 심볼을 못 찾는지 에러가 가리킨다 |
| `public static void main` 에서 `static` 지우고 `java Predict01.java` | `error: 'main' method is not declared 'public static'` — 컴파일은 되지만 JVM 이 시작점으로 인정하지 않는다 |
| 파일 이름을 `Predict.java` 로 바꾸고 `javac` 해 보기 | `class Predict01 is public, should be declared in a file named Predict01.java` |
| `javac Predict01.java` 후 `.class` 파일을 지우고 `java Predict01` | `Could not find or load main class` — 컴파일 결과물이 없어서 실행할 수 없다 |
| `1 + 2 + "3"` 에서 `"3"` 의 따옴표를 지우기 | 숫자 6 이 나온다. 따옴표 하나가 `+` 의 의미를 바꾼다 |

### 3단계. 고장난 프로그램 고치기 (30분)

`../../labs/week01/` 에 일부러 망가뜨린 프로그램 3개가 있습니다. 각각 `Hello, Java!` 가 출력되도록 고치세요.

```bash
cd ../../labs/week01
java Broken1.java
java Broken2.java
java Broken3.java
```

- 에러가 **한꺼번에 나오지 않는다**는 것을 관찰하세요. 첫 에러를 고치면 새 에러가 나타나기도 합니다 (컴파일러가 단계별로 검사하기 때문입니다).
- `Broken3` 은 컴파일은 되는데 실행이 안 됩니다. 컴파일 에러와 실행 에러가 어떻게 다른지 비교해 보세요.
- 막히면 `../../labs/solutions/week01/` 을 열어 보세요. 열기 전에 15분은 고민하세요.

---

## 1. JDK, JRE, JVM

| 용어 | 의미 |
|------|------|
| **JVM** (Java Virtual Machine) | 바이트코드를 실행하는 가상 머신. 운영체제마다 JVM이 있어서 같은 바이트코드가 어디서나 돈다 |
| **JRE** | JVM + 표준 라이브러리. 실행만 할 때 필요 |
| **JDK** | JRE + 컴파일러(`javac`) 등 개발 도구. **우리가 설치할 것** |

실행 흐름은 이렇습니다.

```
Hello.java  --javac-->  Hello.class (바이트코드)  --java(JVM)-->  실행
   소스 코드             컴파일 결과물                    기계어로 번역하며 실행
```

> **JS와 비교:** JavaScript는 소스를 그대로 엔진이 실행합니다. 자바는 먼저 컴파일하므로 **타입 오류·오타 같은 실수를 실행 전에** 잡아 줍니다. TypeScript의 `tsc`와 비슷하지만, 자바는 컴파일이 필수입니다.

## 2. 설치와 확인

1. **JDK 21** 설치: [Adoptium Temurin](https://adoptium.net/) 또는 Amazon Corretto 21. macOS는 `brew install --cask temurin@21`도 됩니다.
2. 터미널에서 확인합니다.

   ```bash
   java -version
   javac -version
   ```

   둘 다 `21`로 시작하는 버전이 나와야 합니다. `javac`만 안 나오면 JRE만 설치된 것입니다.
3. IDE 설치: IntelliJ IDEA Community. 설치 후 프로젝트 SDK를 JDK 21로 지정합니다.

## 3. 첫 프로그램

```java
// File: Hello.java
public class Hello {
    public static void main(String[] args) {
        System.out.println("Hello, Java!");
    }
}
```

실행 방법은 두 가지입니다.

```bash
# 방법 1: 소스를 바로 실행 (학습용, JDK 11 이상)
java Hello.java

# 방법 2: 컴파일 후 실행 (실제 동작 방식)
javac Hello.java      # Hello.class 생성
java Hello            # .class 확장자 없이 클래스 이름만
```

### 한 줄씩 뜯어보기

| 코드 | 의미 |
|------|------|
| `public class Hello` | `Hello`라는 클래스 정의. **public 클래스의 이름은 파일명과 같아야 합니다** |
| `public static void main(String[] args)` | 프로그램의 시작점. 이 모양이 정확히 맞아야 JVM이 찾습니다 |
| `String[] args` | 터미널에서 넘긴 인자들 |
| `System.out.println(...)` | 표준 출력에 한 줄 출력 |
| `;` | **모든 문장의 끝에 필수** (JS처럼 생략 불가) |

## 4. 출력과 인자

```java
// File: Greeting.java
public class Greeting {
    public static void main(String[] args) {
        String name = args.length > 0 ? args[0] : "world";

        System.out.println("안녕, " + name + "!");           // 줄바꿈 포함
        System.out.print("줄바꿈 ");                         // 줄바꿈 없음
        System.out.print("없이 출력\n");
        System.out.printf("%s님, 올해는 %d년입니다.%n", name, 2026); // 서식 출력
    }
}
```

```bash
java Greeting.java 하승
```

자주 쓰는 서식 지정자: `%s` 문자열, `%d` 정수, `%f` 실수(`%.2f`는 소수 둘째 자리), `%n` 줄바꿈.

## 5. 주석

```java
// 한 줄 주석
/* 여러 줄
   주석 */
/** 문서화 주석(Javadoc). 클래스·메서드 위에 씁니다 */
```

## 6. 컴파일 에러 읽기 연습

일부러 틀려 보세요.

```java
// File: Broken.java
public class Broken {
    public static void main(String[] args) {
        System.out.println("세미콜론 빠뜨리기")
    }
}
```

```
Broken.java:3: error: ';' expected
```

파일명, 줄 번호, 원인이 한 줄에 나옵니다. 앞으로 에러가 나면 **첫 번째 에러만** 고치고 다시 컴파일하세요. 뒤의 에러는 첫 에러 때문에 연쇄로 생기는 경우가 많습니다.

흔한 실수 3가지는 다음과 같습니다.

- 대소문자 구분: `system.out.println`은 에러 (`System`이어야 함)
- 따옴표: 문자열은 **큰따옴표** `"..."`, 작은따옴표 `'a'`는 문자 하나
- 파일명과 public 클래스명 불일치

## 연습문제

1. `Hello.java`를 수정해 본인 이름과 오늘 날짜를 두 줄로 출력하세요.
2. `printf`로 `이름: 하승 / 나이: 30 / 키: 175.5cm` 형태로 출력하세요 (키는 `%.1f` 사용).
3. 인자를 두 개 받아 `"<인자1>와 <인자2>"`를 출력하는 `Pair.java`를 작성하세요. 인자가 모자랄 때는 `"인자 2개가 필요합니다"`를 출력해야 합니다. (힌트: `args.length`)
4. `javac`로 컴파일한 뒤 생성된 `.class` 파일을 확인하세요. 그리고 소스 파일을 지운 상태에서 `java 클래스명`이 실행되는지 확인해 보세요. 이 실험으로 "컴파일 결과물만 있으면 실행된다"는 것을 확인할 수 있습니다.
5. (도전) IntelliJ에서 같은 프로그램을 만들고 `main` 옆의 초록 ▶ 버튼으로 실행해 보세요. 터미널 실행과 무엇이 같고 무엇이 다른가요?

## 퀴즈

1. JDK, JRE, JVM의 포함 관계는?
2. `javac`가 만드는 파일의 확장자는?
3. `public class Foo`는 어떤 이름의 파일에 있어야 하나?
4. `System.out.println`과 `System.out.print`의 차이는?

<details>
<summary>정답</summary>

1. JDK ⊃ JRE ⊃ JVM
2. `.class` (바이트코드)
3. `Foo.java`
4. `println`은 출력 후 줄바꿈, `print`는 줄바꿈 없음

</details>

## 체크리스트

- [ ] `java -version`, `javac -version`이 모두 21로 나온다
- [ ] 터미널에서 `java Hello.java`를 실행했다
- [ ] `javac`로 컴파일하고 `.class`를 직접 실행했다
- [ ] 컴파일 에러 메시지에서 줄 번호를 찾아 고쳤다
