# 16주차. Gradle·JUnit·최종 프로젝트

**예상 시간:** 12시간 이상 (여유 코스는 2~3주) | **목표:** 빌드 도구와 테스트를 갖춘 프로젝트를 만들고, 지금까지 배운 것을 모아 가계부 CLI를 완성한다. **기초 과정(1~16주)의 마지막 마일스톤**입니다. 이어서 17주차부터 백엔드 준비(Phase 5)가 시작됩니다.

## 🖐 먼저 손으로 (읽기 전에)

> 이 주차는 설명을 읽기보다 **직접 프로젝트를 만들고 테스트를 실패시켜 보는** 주차입니다.

### 1단계. 프로젝트 만들고 빌드 돌려 보기 (30분)

1. 빈 폴더 `ledger` 를 만들고 `gradle init --type java-application --dsl kotlin --test-framework junit-jupiter --java-version 21 --package com.example.ledger` 를 실행합니다. 질문에는 기본값으로 답하세요. (`--package` 를 빼면 패키지가 `org.example` 이 되어 아래 문서의 경로와 달라집니다.)
2. 생성된 파일 중 `app/build.gradle.kts`, `app/src/main/java/.../App.java`, `app/src/test/java/.../AppTest.java` 를 열어 **한 줄씩 소리 내어 읽고** 모르는 줄에 밑줄을 칩니다.
3. `./gradlew test` → `./gradlew run` → `./gradlew build` 를 차례로 실행하고 `app/build/` 폴더에 무엇이 생기는지 봅니다.

### 2단계. 빨강 → 초록 → 빨강 (1시간)

아래 순서를 **반드시 이 순서대로** 손으로 해 보세요.

1. 아래 설명 문서의 `LedgerTest` 를 먼저 타이핑합니다. 아직 `Ledger` 가 없으니 **컴파일 에러** (빨강1).
2. `Ledger` 를 최소한으로 만들어 컴파일만 되게 합니다 (`balance()` 는 `return 0;`). 테스트가 **실패** 합니다 (빨강2). 실패 메시지의 `expected` / `but was` 를 읽으세요.
3. `balance()` 를 구현해 **초록**을 만듭니다.
4. 이제 일부러 `balance()` 의 `-` 를 `+` 로 바꿔 보세요. 어느 테스트가 어떻게 실패하나요? 이 순간이 테스트의 가치입니다.
5. 원복한 뒤, 테스트에서 기대값 하나를 일부러 틀리게 바꿔 `FAILED` 출력을 눈에 익히세요.

### 3단계. 의존성 하나 추가해 보기 (30분)

`app/build.gradle.kts` 의 `dependencies` 안에 `implementation("org.apache.commons:commons-lang3:3.17.0")` 를 추가하고 `App.java` 의 `main` 에서 `org.apache.commons.lang3.StringUtils.leftPad("7", 3, '0')` 를 호출해 `007` 을 출력해 보세요. 의존성 한 줄이 어떻게 코드에서 쓸 수 있게 되는지 확인하는 연습입니다. (버전은 Maven Central 에서 최신으로 바꿔도 됩니다.)

### 4단계. 최종 프로젝트

아래 문서의 요구사항 표와 단계별 일정대로 가계부 CLI 를 만듭니다. **한 단계가 끝날 때마다 `./gradlew test` 가 초록**이어야 다음 단계로 넘어갑니다.

---


## 1. 왜 빌드 도구인가

지금까지는 파일 하나를 `java Foo.java`로 실행했습니다. 실제 프로젝트에는 수십~수백 개의 파일, 외부 라이브러리(Jackson, JUnit …), 테스트, 패키징이 있습니다. 빌드 도구가 이를 자동화합니다.

| 도구 | 비고 |
|------|------|
| **Gradle** | 현재 표준. Spring Boot 기본. 설정이 코드 (`build.gradle.kts`) |
| Maven | 오래된 표준. XML 설정(`pom.xml`). 기존 프로젝트에서 많이 만남 |

> **JS와 비교:** `npm` + `package.json`이 하는 일(의존성 설치, 스크립트 실행)과 `webpack`/`vite`가 하는 빌드를 Gradle이 함께 맡는다고 보면 됩니다. `build.gradle.kts` ≈ `package.json`.

## 2. Gradle 프로젝트 만들기

Gradle을 설치합니다 (macOS: `brew install gradle`). 이후 빈 폴더에서 다음을 실행합니다.

```bash
gradle init --type java-application --dsl kotlin --test-framework junit-jupiter --java-version 21 --package com.example.ledger
```

질문에는 기본값으로 답해도 됩니다. 생성되는 구조는 아래와 같고, 이 구조는 **고정된 관례**입니다.

```
ledger/
├── settings.gradle.kts
├── gradle/libs.versions.toml   // 라이브러리 버전을 모아 두는 파일 (version catalog)
├── gradlew, gradlew.bat        // Gradle Wrapper: 팀 모두가 같은 버전의 Gradle을 쓰게 한다
└── app/
    ├── build.gradle.kts        // 빌드 설정 (이 문서에서 말하는 build.gradle.kts 는 이 파일)
    └── src/
        ├── main/java/com/example/ledger/   // 앱 코드 (패키지 = 폴더)
        └── test/java/com/example/ledger/   // 테스트 코드 (main과 같은 패키지 구조)
```

자주 쓰는 명령은 다음과 같습니다. 이후로는 `gradle` 대신 `./gradlew`를 쓰세요.

```bash
./gradlew build        # 컴파일 + 테스트 + 패키징
./gradlew test         # 테스트만
./gradlew run          # 앱 실행 (application 플러그인)
./gradlew clean        # build 폴더 삭제
```

`app/build.gradle.kts`에서 알아야 할 부분입니다. 생성된 파일은 `libs.junit.jupiter` 처럼 `gradle/libs.versions.toml` 을 가리키는 줄과 예제용 `guava` 의존성을 포함하고 있습니다. 아래 코드는 같은 뜻을 좌표로 직접 적은 단순화한 형태이므로, 생성된 파일을 우선하세요.

```kotlin
plugins {
    application
}

repositories {
    mavenCentral()                  // 의존성을 내려받을 저장소
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    // implementation("com.fasterxml.jackson.core:jackson-databind:2.18.2")   // 필요할 때 추가
}

java {
    toolchain { languageVersion = JavaLanguageVersion.of(21) }
}

application {
    mainClass = "com.example.ledger.Main"    // init 직후에는 "com.example.ledger.App". 4단계에서 Main 을 만들 때 바꾼다
}

tasks.test {
    useJUnitPlatform()
}
```

의존성 한 줄을 추가하면 Gradle이 자동으로 내려받습니다. 의존성의 좌표는 `그룹:이름:버전`입니다. 라이브러리는 [Maven Central](https://central.sonatype.com/)에서 검색합니다.

## 3. JUnit 5로 테스트하기

테스트는 "내가 짠 코드가 맞다"를 **코드로 증명**합니다. 기능을 고칠 때 다른 기능이 망가졌는지 바로 알 수 있습니다.

예: 가계부의 핵심 로직 (`app/src/main/java/com/example/ledger/Ledger.java`)

```java
package com.example.ledger;

import java.util.ArrayList;
import java.util.List;

public class Ledger {
    private final List<Entry> entries = new ArrayList<>();

    public void add(Entry entry) {
        entries.add(entry);
    }

    public long balance() {
        long total = 0;
        for (Entry e : entries) {
            total += e.type() == Type.INCOME ? e.amount() : -e.amount();
        }
        return total;
    }

    public List<Entry> entries() {
        return List.copyOf(entries);
    }

    public enum Type { INCOME, EXPENSE }

    public record Entry(String date, Type type, String category, long amount) {
        public Entry {
            if (amount <= 0) {
                throw new IllegalArgumentException("금액은 0보다 커야 합니다: " + amount);
            }
        }
    }
}
```

테스트 (`app/src/test/java/com/example/ledger/LedgerTest.java`)

```java
package com.example.ledger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.ledger.Ledger.Entry;
import com.example.ledger.Ledger.Type;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LedgerTest {
    private Ledger ledger;

    @BeforeEach                       // 각 테스트 직전에 실행: 테스트끼리 영향을 주지 않게
    void setUp() {
        ledger = new Ledger();
    }

    @Test
    @DisplayName("수입에서 지출을 뺀 값이 잔액이다")
    void balanceIsIncomeMinusExpense() {
        ledger.add(new Entry("2026-10-01", Type.INCOME, "월급", 3_000_000));
        ledger.add(new Entry("2026-10-02", Type.EXPENSE, "식비", 12_000));

        assertEquals(2_988_000, ledger.balance());
    }

    @Test
    void emptyLedgerHasZeroBalance() {
        assertEquals(0, ledger.balance());
        assertTrue(ledger.entries().isEmpty());
    }

    @Test
    void rejectsNonPositiveAmount() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new Entry("2026-10-01", Type.EXPENSE, "식비", 0));
        assertTrue(e.getMessage().contains("0보다"));
    }
}
```

### 좋은 테스트의 규칙

1. **하나의 테스트는 하나의 사실만** 검증합니다 (이름이 곧 명세).
2. **Arrange → Act → Assert** 순서로 읽히게 씁니다 (준비 → 실행 → 검증).
3. 테스트끼리 **순서나 상태에 의존하지 않습니다.**
4. 정상 경로뿐 아니라 **경계값과 예외 경로**를 꼭 넣습니다 (빈 목록, 0, 음수, null).
5. 테스트가 실패하는 것을 한 번은 직접 확인합니다 (기대값을 일부러 틀리게). 항상 통과하는 테스트는 아무것도 증명하지 못합니다.

자주 쓰는 단언: `assertEquals(기대, 실제)`, `assertTrue`, `assertFalse`, `assertThrows`, `assertNotNull`, `assertAll`.

## 4. 최종 프로젝트: 가계부 CLI

### 요구사항

| 기능 | 설명 |
|------|------|
| 내역 추가 | 날짜, 수입/지출, 카테고리, 금액 |
| 내역 조회 | 전체, 월별(`yyyy-MM`), 카테고리별 |
| 통계 | 잔액, 월별 수입/지출 합계, 카테고리별 지출 합계와 비중(%) |
| 삭제 | 번호로 삭제 |
| 저장 | 종료 후 다시 실행해도 데이터가 유지된다 (CSV 또는 JSON 파일) |
| 입력 검증 | 잘못된 날짜·금액이면 메시지를 출력하고 프로그램은 계속 동작한다 |

### 설계 지침 (지금까지 배운 것을 모두 사용)

| 주차 | 활용 |
|------|------|
| 5~7 | `Entry`는 `record`, 종류는 `enum`, 저장소는 `interface Store`로 추상화 (파일/메모리 구현 교체 가능) |
| 8 | 사용자 정의 예외 (`InvalidCommandException` 등) |
| 10 | `LocalDate`, `YearMonth`로 월 계산 |
| 11 | `List`, `Map<String, Long>`으로 집계 |
| 13 | `Stream`과 `Collectors.groupingBy`로 통계 |
| 14 | 파일 저장·로드 |
| 16 | Gradle 프로젝트, JUnit 테스트 |

패키지 구조 예시:

```
com.example.ledger
├── Main.java                 // 입력 루프, 출력 포맷
├── Ledger.java               // 도메인 규칙 (잔액 계산, 필터)
├── Stats.java                // 통계 계산 (Stream)
├── Store.java                // interface: save/load
├── FileStore.java            // 파일 구현
└── (예외 클래스들)
```

규칙은 `Ledger`/`Stats`에, 입출력은 `Main`에 둡니다. **`Ledger`와 `Stats`는 콘솔을 전혀 몰라야** 테스트하기 쉽습니다.

### 단계별 진행 (권장 일정)

| 단계 | 할 일 | 목표 시간 |
|------|-------|-----------|
| 1 | `gradle init`, `Entry`/`Ledger` 작성 + 테스트 3개 | 2시간 |
| 2 | `Stats` 구현 + 테스트 (월별, 카테고리별) | 3시간 |
| 3 | `Store` 인터페이스 + `FileStore` + 저장/로드 테스트 (임시 폴더 사용: `@TempDir`) | 3시간 |
| 4 | `Main` 콘솔 루프, 입력 검증, 오류 메시지 | 3시간 |
| 5 | 리팩토링, README에 사용법 정리, 테스트 커버 확인 | 2시간 |

테스트 예시 `@TempDir`:

```java
@Test
void savesAndLoads(@TempDir java.nio.file.Path dir) throws Exception {
    var store = new FileStore(dir.resolve("ledger.csv"));
    store.save(List.of(new Entry("2026-10-01", Type.INCOME, "월급", 1_000)));
    assertEquals(1, store.load().size());
}
```

### 마일스톤 통과 기준 (16주)

- [ ] `./gradlew test`가 통과한다 (테스트 10개 이상)
- [ ] `./gradlew run`으로 앱을 실행해 모든 기능을 쓸 수 있다
- [ ] 프로그램을 종료한 뒤 다시 실행해도 데이터가 유지된다
- [ ] 잘못된 입력에도 프로그램이 죽지 않는다
- [ ] `Ledger`/`Stats` 코드에 `System.out`이나 `Scanner`가 없다
- [ ] 클래스마다 책임을 한 문장으로 설명할 수 있다

## 5. 16주 이후

### 이어서 배울 순서

17주차부터는 이 커리큘럼의 **Phase 5. 백엔드 준비**로 이어집니다. 백엔드가 목표가 아니라면 여기서 멈추고 아래 4번(자바 심화)으로 가도 됩니다.

1. **17~18주차:** SQL·JDBC, HTTP·JSON. 데이터베이스와 네트워크를 손으로
2. **19~20주차:** 설계 원칙(의존성 주입), JVM 메모리
3. **21~24주차:** Spring Boot, REST API, JPA, 최종 프로젝트
4. **자바 심화:** 『Effective Java』, 동시성 심화

### 이 커리큘럼의 한계 (솔직하게)

- 실무에서 쓰는 로깅(SLF4J), 인증·인가, 배포는 Phase 5 이후에도 다루지 않습니다. 필요해질 때 배우는 것이 효율적입니다.
- 제네릭·Stream·동시성은 입문 수준입니다. 이후에도 수준에 맞게 반복해서 만나게 됩니다.

### 학습 습관

- 매주 작은 프로그램을 하나씩 만드세요. 자바는 **쳐 본 코드의 양**이 실력입니다.
- 에러 메시지를 읽는 힘이 가장 큰 자산입니다. 처음 10초는 스스로 읽고, 다음 5분은 검색하고, 그 후에 질문하세요.
- 다른 사람의 코드를 읽으세요. JDK 소스(`ArrayList`, `HashMap`)도 IDE에서 `Cmd+클릭`으로 열어 볼 수 있습니다.

## 퀴즈

1. Gradle Wrapper(`gradlew`)를 쓰는 이유는?
2. `src/main/java`와 `src/test/java`가 분리된 이유는?
3. `@BeforeEach`의 역할은?
4. 항상 통과하는 테스트의 문제는?

<details>
<summary>정답</summary>

1. 팀원과 CI가 같은 Gradle 버전으로 빌드하게 해서, "내 컴퓨터에서는 되는데" 문제를 줄이기 위해
2. 테스트 코드는 배포되는 결과물에 포함되지 않아야 하고, 의존성(`testImplementation`)도 따로 관리한다
3. 각 테스트 직전에 준비 작업을 실행해, 테스트 간 상태 공유를 막는다
4. 기능이 망가져도 알려 주지 못하므로 안전망 역할을 하지 못한다

</details>

## 체크리스트

- [ ] Gradle 프로젝트를 직접 생성하고 `./gradlew test`를 실행했다
- [ ] 의존성을 한 개 이상 추가해 봤다
- [ ] JUnit 테스트를 작성하고, 일부러 실패하게 만들어 확인했다
- [ ] 최종 프로젝트의 모든 통과 기준을 충족했다
