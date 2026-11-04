# Java 기초 + 백엔드 입문 커리큘럼 (24주)

자바 경험이 없는 입문자가 **3~6개월** 안에 "자바로 콘솔 프로그램을 설계하고 테스트를 붙인 뒤(1~16주), Spring Boot로 테스트가 딸린 REST API를 직접 만드는 상태"(17~24주)까지 가는 로드맵입니다. 1~16주만 해도 하나의 완결된 기초 과정입니다.

프론트엔드 개발자가 백엔드와 협업하는 법을 배우고 싶다면 **Phase 6 (25~29주)** 을 이어서 진행하세요. 24주차의 도서관 REST API 를 "백엔드 팀이 준 API"로 보고, 그 위에 Next.js 클라이언트를 만듭니다. OpenAPI 명세에서 TypeScript 타입을 생성하고, 에러·인증(쿠키)·CORS 를 처리하고, 백엔드와 프론트엔드를 로컬에서 함께 띄우는 흐름을 다룹니다. 직접 만든 24주차 API 또는 제공되는 참조 API(`../../labs/backend/week25`) 어느 쪽에든 붙여 실습할 수 있습니다.

## 전제

- **Java 21 (LTS)** 기준입니다. `record`, `switch` 표현식, 패턴 매칭, 가상 스레드 같은 현대 문법을 처음부터 씁니다.
- 프로그래밍 자체가 처음은 아니고 JavaScript/TypeScript 경험이 있다고 가정해, 각 주차에 "JS와 비교" 박스를 넣었습니다. 처음 배우는 분도 따라올 수 있게 썼지만, 비교 박스는 건너뛰어도 됩니다.
- 에디터는 **IntelliJ IDEA Community**를 권장합니다. VS Code(Extension Pack for Java)도 가능합니다.
- 목표 학습량은 **주 8~10시간**(주 5일 × 하루 1.5~2시간)입니다.
- 17주차부터는 Gradle 이 라이브러리를 내려받으므로 **인터넷 연결**이 필요합니다. 17주차 SQL 실습은 `sqlite3` 명령(macOS 에는 기본 설치)을 씁니다. 18~22주차에는 `curl` 도 씁니다.
- Phase 6(25~29주차) 프론트엔드 실습은 **Node.js 22.12 이상**과 **pnpm 10 이상** 이 필요합니다.

## 페이스 선택

| 코스 | 기간 | 범위·방식 | 추천 대상 |
|------|------|------|-----------|
| 전체 표준 | 24주 (약 6개월) | 1~24주차, 1주차 = 1개 문서 | 주 8~10시간 확보 가능 |
| 전체 압축 | 20주 (약 5개월) | 5·6주차와 7·8주차를 묶고 14·15주차는 읽기만(1~16주차를 12주로), 17~24주차는 그대로 8주 | 주 12시간 이상 |
| 기초만 | 16주 (약 4개월) | 1~16주차만. Phase 5 는 건너뜀 | Java 기초가 목표인 경우 |
| 기초 여유 | 24주 (약 6개월) | 1~16주차를 문서당 1.5주로. 연습문제와 복습을 더 많이 | 주 5~6시간 |
| 프론트 협업 트랙 | 25~29주차 실습 약 5주 (17~24주차는 읽기 중심) | 17~24주차는 핵심 21·22·24 위주로 읽고, 25~29주차에서 프론트엔드 실습 | 프론트엔드 개발자 (React·TypeScript 경험자) |

여유 코스는 문서를 늘리지 않고 시간 배분만 바꿉니다. 문서 하나당 "읽기·따라 치기 → 연습문제 → 퀴즈 → 복습"을 두 번에 나눠 하세요. Phase 5 는 컬렉션·Stream(11~13주), try-with-resources(14주), Gradle·JUnit(16주)을 쓰므로 16주차를 끝낸 뒤에 시작하세요.

## 전체 로드맵

### Phase 1. 언어 기초 (1~4주)

| 주 | 주제 | 핵심 |
|----|------|------|
| 1 | [개발 환경과 첫 프로그램](week01-setup.md) | JDK/JVM, 컴파일, `main`, 출력 |
| 2 | [변수·타입·연산자](week02-types.md) | 기본형 vs 참조형, 형변환, 정수 나눗셈, `String` 비교 |
| 3 | [제어문](week03-control-flow.md) | `if`, `switch` 표현식, 반복문 |
| 4 | [메서드와 배열](week04-methods-arrays.md) | 값 전달, 오버로딩, 배열, 재귀 |

### Phase 2. 객체지향 (5~9주)

| 주 | 주제 | 핵심 |
|----|------|------|
| 5 | [클래스와 객체](week05-classes.md) | 필드, 생성자, 캡슐화, `static`, 참조 |
| 6 | [상속과 다형성](week06-inheritance.md) | `extends`, 오버라이딩, 추상 클래스 |
| 7 | [인터페이스·enum·record](week07-interface-enum-record.md) | 계약 설계, `sealed`, 패턴 매칭 switch |
| 8 | [예외·패키지·Object 메서드](week08-exceptions-object.md) | checked/unchecked, `equals`/`hashCode` |
| 9 | [중간 프로젝트: 도서 대출 시스템](week09-project-library.md) | 객체지향 종합 |

### Phase 3. 표준 라이브러리 (10~13주)

| 주 | 주제 | 핵심 |
|----|------|------|
| 10 | [문자열·날짜·래퍼 클래스](week10-string-time.md) | `StringBuilder`, text block, `java.time` |
| 11 | [컬렉션 프레임워크](week11-collections.md) | `List`/`Set`/`Map`, 정렬, `Comparator` |
| 12 | [제네릭과 Optional](week12-generics-optional.md) | 타입 안전성, 와일드카드 |
| 13 | [람다와 Stream](week13-lambda-stream.md) | 함수형 인터페이스, 데이터 파이프라인 |

### Phase 4. 실전 (14~16주)

| 주 | 주제 | 핵심 |
|----|------|------|
| 14 | [파일 I/O](week14-file-io.md) | `Path`, `Files`, UTF-8, try-with-resources |
| 15 | [동시성 기초](week15-concurrency.md) | 스레드, `ExecutorService`, `CompletableFuture`, 가상 스레드 |
| 16 | [Gradle·JUnit·최종 프로젝트](week16-gradle-junit-final.md) | 빌드, 테스트, 가계부 CLI, 다음 단계 |

### Phase 5. 백엔드 준비 (17~24주)

| 주 | 주제 | 핵심 |
|----|------|------|
| 17 | [SQL과 JDBC](week17-jdbc-sql.md) | `SELECT`/`JOIN`/`GROUP BY`, `PreparedStatement`, SQL 인젝션, 트랜잭션 |
| 18 | [HTTP와 JSON](week18-http-json.md) | 요청·응답 구조, 상태 코드, Jackson, JDK 만으로 만드는 REST 서버·클라이언트 |
| 19 | [설계 원칙과 디자인 패턴](week19-design-patterns.md) | SOLID 3가지, 의존성 주입, 전략·팩토리·옵저버 |
| 20 | [JVM과 메모리](week20-jvm-memory.md) | 스택과 힙, GC, 메모리 누수, `jps`/`jcmd`, `-Xmx` |
| 21 | [Spring Boot 입문: IoC와 DI](week21-spring-ioc-di.md) | 빈, `@Component`, `@Primary`/`@Qualifier`, `@Value`, 첫 엔드포인트 |
| 22 | [REST API 만들기](week22-rest-api.md) | CRUD, `@Valid`, `@RestControllerAdvice`, MockMvc 테스트 |
| 23 | [JPA와 데이터베이스 연동](week23-jpa.md) | 엔티티 매핑, 이름 기반 쿼리, `@DataJpaTest`, N+1 |
| 24 | [최종 프로젝트: 도서관 REST API](week24-final-project.md) | 9주차 도서 대출 시스템을 REST API 로 다시 설계·구현 |

### Phase 6. 프론트엔드 협업 (25~29주)

| 주 | 주제 | 핵심 |
|----|------|------|
| 25 | [API 계약](week25-api-contract.md) | springdoc-openapi 로 명세 노출, `openapi-typescript` 로 TS 타입 생성, 명세 변경을 타입 에러로 확인 |
| 26 | [에러·상태 코드 처리](week26-error-handling.md) | `{"message"}` 응답을 `ApiError` 로 매핑, fetch 래퍼, 폼 필드 에러와 알림(`ErrorNotice`) 구분 |
| 27 | [인증·CORS·쿠키](week27-auth-cors.md) | preflight, Next `rewrites` 프록시, httpOnly 쿠키 세션, Spring Security 최소 설정 |
| 28 | [로컬 통합 개발 환경](week28-local-integration.md) | Spring 8080 + Next 3000 동시 기동, `.env`, MSW 목킹, Playwright 통합 E2E |
| 29 | [최종 프로젝트: 프론트엔드 클라이언트](week29-final-frontend.md) | Next.js 도서관 클라이언트, 24주차 시나리오를 UI 에서 재현, 마일스톤 점검 |

## 각 주차 문서 구성

1. 학습 목표와 예상 시간
2. **🖐 먼저 손으로** — 예측해서 치기, 일부러 망가뜨리기, 빈칸에서 만들기 (자동 채점)
3. 개념 설명 + 실행 가능한 예제 코드 (손 실습에서 막혔을 때 펼쳐 보는 부분)
4. JS/TS와 비교 (해당하는 경우)
5. 연습문제, 퀴즈 (정답은 접어 둠), 체크리스트

## 손으로 배우는 방식 (이 커리큘럼의 핵심)

읽기만 하면 "알 것 같은 느낌"만 남습니다. 이 커리큘럼은 각 주차를 **읽기 → 실습**이 아니라 **실습 → 읽기** 순서로 설계했습니다. 설명 문서는 막혔을 때 펼쳐 보는 사전입니다.

| 단계 | 하는 일 | 얻는 것 |
|------|---------|---------|
| 1. 예측 → 치기 → 비교 | 코드를 실행하기 전에 출력을 노트에 적고, 타이핑해 실행해서 틀린 줄에 ★ | 내 머릿속 모델과 실제 동작의 차이. **틀린 예측이 가장 값진 학습** |
| 2. 일부러 망가뜨리기 | 정상 코드를 하나씩 고장 내고 에러 메시지를 확인 | 에러 메시지 읽는 힘, 컴파일러와 친해지기 |
| 3. 빈칸에서 만들기 | `../../labs/weekNN/` 의 비어 있는 메서드를 구현하고 채점기로 ✅ 늘리기 | 읽은 것을 직접 쓸 수 있는지 확인 |

### 실습 채점기 사용법

```bash
cd labs
./check.sh 05        # 5주차 실습 채점 (주차 번호만 바꾸면 됩니다)
```

- `../../labs/weekNN/` 의 파일에서 `// 여기를 고치세요` 가 붙은 곳을 채웁니다. (이 표시는 학습자가 채우라는 뜻입니다.)
- 처음에는 대부분 ❌ 입니다. 정상입니다. ❌ 아래의 `기대 / 실제` 를 읽고 위에서부터 하나씩 고치세요.
- 6·7·8·13·15주차는 소스 코드의 모양도 검사합니다 (예: Stream 사용, `instanceof` 금지). 풀이 방식까지 연습하라는 의도입니다.
- 정답은 `../../labs/solutions/weekNN/` 에 있습니다. **15분 넘게 막혔을 때만** 열고, 본 뒤에는 덮고 **직접 다시 치세요.**
- 1주차는 `../../labs/week01/` 의 고장난 프로그램 3개를 고치는 실습입니다. 9주차와 16주차는 채점기 대신 문서 안의 단계별 절차로 진행합니다.

### Phase 5 실습 (17~23주차)

17주차부터는 `../../labs/backend/` 의 Gradle 프로젝트에서 같은 방식으로 실습합니다. 채점기는 JUnit 테스트이고, 주차 번호만 바꿔 실행합니다.

```bash
cd ../../labs/backend
./gradlew :week17:test      # 17주차. 처음 한 번은 Gradle 과 라이브러리를 내려받느라 오래 걸립니다
```

- 스텁은 `../../labs/backend/weekNN/src/main/java/` 에 있고, 정답은 `../../labs/solutions/weekNN/` 에 있습니다. 규칙은 이전과 같습니다. **15분 넘게 막혔을 때만** 보고, 덮고 다시 직접 치세요.
- 처음에는 모든 테스트가 빨간색이어야 정상입니다. 실패 메시지의 `expected / but was` 를 읽으세요.
- 24주차는 채점기가 없는 최종 프로젝트입니다.

### Phase 6 실습 (25~29주차)

백엔드 참조 API(8080)와 Next.js 개발 서버(3000)를 함께 띄워 실습합니다.

```bash
# 터미널 1: 참조 API (8080)
cd ../../labs/backend && ./gradlew :week25:bootRun

# 터미널 2: 프론트엔드 개발 서버 (3000)
cd ../../labs/frontend && pnpm dev

# 테스트
cd ../../labs/frontend && pnpm test    # Vitest 단위 테스트
cd ../../labs/frontend && pnpm e2e     # Playwright 통합 E2E. 백엔드가 8080 에서 떠 있어야 합니다
```

### 타이핑 규칙

1. **복사·붙여넣기 금지.** 문서의 코드도 손으로 칩니다. 오타를 내고 컴파일 에러를 보는 것까지가 학습입니다.
2. **자동 완성은 3주차까지 끄거나 최소화**하세요. 메서드 이름을 손이 기억하게 하는 시기입니다. (IntelliJ: Settings → Editor → General → Code Completion 에서 자동 팝업 끄기)
3. **코드는 매번 실행합니다.** 다섯 줄 쓰고 한 번 실행하세요. 한 번에 오십 줄을 쓰고 돌리면 어디가 틀렸는지 모릅니다.
4. **예측 없이 실행하지 않습니다.** "어떤 결과가 나올까?"를 먼저 한 줄로 적고 실행합니다.

### 학습 노트 (`my-notes.md`)

각 주차가 끝날 때 아래 틀로 **5분만** 적습니다. 3개월 뒤 내가 어디서 막혔는지 보여 주는 가장 정직한 기록이 됩니다.

```markdown
## N주차
- 예측이 틀렸던 것: (코드/예측/실제/왜)
- 오늘 처음 본 에러 메시지: (원문 + 원인)
- 채점기에서 가장 오래 막힌 문제: (무엇이 틀렸고 어떻게 풀었나)
- 내 말로 한 문단 설명: (오늘 배운 개념을 JS/TS 와 비교해서)
```

## 학습 방법 (이것만 지키세요)

1. **코드는 반드시 손으로 쳐서 실행합니다.** 복사·붙여넣기로는 문법이 몸에 붙지 않습니다.
2. **예제는 한 파일씩 실행할 수 있습니다.** (손 실습 단계는 아래 "손으로 배우는 방식"을 먼저 보세요.) 파일 이름은 코드 첫 줄의 `// File: 이름.java`를 따르세요.

   ```bash
   java Hello.java
   ```

   JDK 11 이상은 `javac` 없이 단일 파일을 바로 실행합니다. 단, **Java 21에서는 파일의 첫 번째 클래스를 실행**합니다. 5주차 이후 예제처럼 `main`이 없는 클래스가 앞에 오는 파일은 컴파일 후 실행하세요.

   ```bash
   javac AccountDemo.java && java AccountDemo
   ```

   `main`이 있는 클래스 이름이 파일 이름과 같으니, 파일 이름을 그대로 쓰면 됩니다.
3. **연습문제를 풀기 전에 정답을 보지 마세요.** 막히면 15분 고민한 뒤 에러 메시지를 읽고, 그래도 막히면 검색하거나 질문하세요.
4. **컴파일 에러를 두려워하지 마세요.** 자바의 컴파일러는 런타임 전에 실수를 알려 주는 가장 친절한 선생님입니다. 에러 메시지의 첫 줄과 줄 번호를 먼저 읽으세요.
5. **매주 마지막에 "오늘 배운 것을 한 문단으로 설명"해 보세요.** 설명이 안 되면 이해한 게 아닙니다.
6. **4주, 9주, 13주, 16주, 24주는 마일스톤**입니다. 해당 주차의 종합 문제를 통과한 뒤 다음으로 넘어가세요.

## 마일스톤 통과 기준

| 시점 | 통과 기준 |
|------|-----------|
| 4주 끝 | 입력 없이도 동작하는 숫자 야구·구구단·성적 계산기 같은 50줄 프로그램을 도움 없이 작성 |
| 9주 끝 | 클래스 4개 이상으로 구성된 콘솔 프로그램을 설계하고, 각 클래스의 책임을 설명 |
| 13주 끝 | 반복문으로 짠 컬렉션 처리 코드를 Stream으로 바꾸고, 반대로도 바꿀 수 있음 |
| 16주 끝 | Gradle 프로젝트로 JUnit 테스트가 통과하는 CLI 앱을 완성 |
| 24주 끝 | 도서관 REST API 를 명세만 보고 직접 구현. 테스트 15개 이상 통과, 대출 규칙은 서비스에서 지키고 N+1 이 없음을 테스트로 확인 |
| 29주 끝 | 생성된 타입만으로 API 를 호출하고, 400/404/409 가 UI 에서 서로 다르게 표시되며, 로그인 후 쿠키로 보호 API 를 호출. 백엔드 없이 MSW 로 화면 개발 가능. Vitest 8개 이상·e2e 1개 이상 통과, README 에 설계 결정 3개 |

## 보조 자료

- [dev.java](https://dev.java/learn/) — Oracle이 운영하는 공식 학습 사이트. 최신 문법 기준입니다.
- [The Java Tutorials](https://docs.oracle.com/javase/tutorial/) — 공식 튜토리얼.
- [Java SE 21 API 문서](https://docs.oracle.com/en/java/javase/21/docs/api/) — 클래스·메서드를 찾을 때 항상 여기를 먼저 보세요.
- [Spring Boot 공식 문서](https://docs.spring.io/spring-boot/index.html) — 21~24주차의 어노테이션이 궁금할 때 찾아보는 곳입니다.
- [Next.js 공식 문서](https://nextjs.org/docs) — 25~29주차의 App Router, Route Handler, `rewrites` 설정을 찾아볼 때 보세요.
- [TanStack Query 공식 문서](https://tanstack.com/query/latest) — 서버 상태 패칭과 캐시 설정을 찾아볼 때 보세요.
- [openapi-typescript 공식 문서](https://openapi-ts.dev/) — 명세에서 TypeScript 타입을 생성하는 방법을 찾아볼 때 보세요.
- [MSW 공식 문서](https://mswjs.io/docs/) — 백엔드 없이 API 응답을 목킹할 때 보세요.
- [Playwright 공식 문서](https://playwright.dev/docs/intro) — 통합 E2E 테스트를 작성할 때 보세요.
- 책은 『Effective Java』(3판)를 Phase 5 와 병행해 읽는 것을 권합니다.

## 다음 단계 (24주 이후)

24주를 마치면 "테스트가 딸린 REST API 를 만들 수 있는" 상태입니다. 실무로 가려면 아래를 이어서 배우세요. 이 커리큘럼은 여기까지 다루지 않습니다. 인증은 27주차에서 Spring Security 최소 설정만 다룹니다.

- 운영용 DB(PostgreSQL 등)와 마이그레이션 도구
- 인증·인가(Spring Security) 심화
- Docker 와 배포, 로그·모니터링
- 테스트 심화(Mockito, 테스트 컨테이너)

Phase 5 를 마친 뒤 두 주 이상 쉬었다면 19주차(DI)와 21주차(IoC)만 먼저 복습하고 이어가세요.
