# 24주차. 최종 프로젝트: 도서관 REST API

**예상 시간:** 12~16시간 | **목표:** 9주차 콘솔 도서 대출 시스템을 **Spring Boot + JPA + 테스트**를 갖춘 REST API 로 다시 만든다. 이 주차에는 빈칸도 채점기도 없습니다. **명세와 시나리오만 주어지고, 구현은 처음부터 직접** 합니다. **마일스톤 점검 주차**입니다.

## 🖐 먼저 손으로 (코드를 치기 전에)

> 9주차와 같은 방식입니다. 이 문서의 아래쪽은 **명세와 힌트**이지 정답 코드가 아닙니다. 막혔을 때는 21~23주차의 해당 실습 코드를 보세요. 이미 만들어 본 것들의 조합입니다.

### 1단계. 시나리오 먼저 적기 (30분)

아래 요청을 위에서 아래로 차례로 보냈다고 가정하고, 응답 **상태 코드**를 노트에 먼저 적으세요. (회원·책·대출의 id 는 처음 생성한 것이 1번이라고 칩니다.) 규칙은 아래 "명세"를 먼저 읽으세요.

| # | 요청 | 내 예측 |
|---|------|------|
| 1 | `POST /books` `{"isbn":"111","title":"이펙티브 자바","author":"조슈아 블로크"}` | |
| 2 | `POST /books` 같은 `isbn` `111` 로 다시 | |
| 3 | `POST /books` `{"isbn":"","title":"x","author":"y"}` | |
| 4 | `POST /members` `{"name":"하승"}` | |
| 5 | `POST /members` `{"name":"민지"}` | |
| 6 | `POST /loans` `{"memberId":1,"bookId":1}` | |
| 7 | `POST /loans` `{"memberId":2,"bookId":1}` (이미 대출 중인 책) | |
| 8 | `POST /loans` `{"memberId":1,"bookId":99}` | |
| 9 | 하승이 책 3권을 더 등록해 빌려서 **4번째** 대출 시도 | |
| 10 | `POST /loans/1/return` | |
| 11 | `POST /loans/1/return` (한 번 더) | |
| 12 | `POST /loans` `{"memberId":2,"bookId":1}` (반납된 책을 다시) | |
| 13 | `GET /members/1/loans` | |

<details>
<summary>명세에 따른 기대값 (예측을 적은 뒤에 여세요)</summary>

```text
1  201 + Location: /books/{id}
2  409
3  400
4  201
5  201
6  201
7  409
8  404
9  409
10 200 (returnedAt 이 채워짐)
11 409
12 201
13 200 (현재 대출 중인 책만)
```

이 값은 구현하면서 검증할 **명세**입니다. 실제 서버의 출력이 아니라 이 프로젝트의 요구사항입니다. 구현 후 직접 호출해서 일치하는지 확인하세요.

</details>

### 2단계. 설계를 종이에 (30분)

코드 전에 노트에 그립니다.

1. 엔티티 3개(`Book`, `Member`, `Loan`)의 필드와 관계. **외래 키는 어느 테이블에?** (힌트: 23주차의 "외래 키가 있는 쪽이 관계의 주인")
2. 대출 규칙 두 개(이미 대출 중, 3권 한도)를 **어느 클래스가 판단**하는가? 9주차의 "규칙은 도메인, 입출력은 `Main`" 을 이번에는 "규칙은 서비스, HTTP 는 컨트롤러" 로 옮깁니다.
3. 업무 예외 클래스 목록과 각각의 상태 코드.

### 3단계. 테스트를 먼저 쓰고 구현 (6~10시간)

아래 "구현 순서"를 따릅니다. **한 단계마다 테스트를 먼저 쓰고, 실패하는 것을 눈으로 본 뒤 구현해 통과시킵니다.** (22주차 테스트 코드를 참고합니다.)

### 4단계. 버그를 심어서 테스트가 잡는지 확인 (30분)

9주차 4단계의 Spring 버전입니다. 완성한 대출 서비스에서 **"한도 검사"를 대출 저장 뒤로 옮겨** 보세요. 어느 테스트가 실패하는지 확인하고 원복합니다. 아무 테스트도 실패하지 않는다면 **테스트가 규칙을 지키고 있지 않은 것**입니다. 테스트를 보강하세요.

---

## 명세

### 도메인

| 엔티티 | 필드 |
|---|---|
| `Book` | `id`, `isbn`(유일), `title`, `author` |
| `Member` | `id`, `name` |
| `Loan` | `id`, `book`, `member`, `loanedAt`, `returnedAt`(반납 전 `null`) |

### 엔드포인트

| 메서드·경로 | 요청 본문 | 성공 | 실패 |
|---|---|---|---|
| `POST /books` | `isbn`, `title`, `author` (모두 공백 불가) | 201 + `Location` + 책 | 400 검증 실패, 409 `isbn` 중복 |
| `GET /books?keyword=` | | 200 + 책 배열. `keyword` 가 있으면 제목에 포함된 것만(대소문자 무시) | |
| `GET /books/{id}` | | 200 + 책 | 404 |
| `POST /members` | `name` (공백 불가) | 201 + `Location` + 회원 | 400 |
| `POST /loans` | `memberId`, `bookId` | 201 + 대출 | 400, 404(회원·책 없음), 409(이미 대출 중 / 회원이 이미 3권 대출 중) |
| `POST /loans/{id}/return` | 없음 | 200 + 대출(`returnedAt` 채움) | 404, 409(이미 반납) |
| `GET /members/{id}/loans` | | 200 + **반납하지 않은** 대출 배열 | 404(회원 없음) |

모든 오류 응답은 `{"message": "..."}` 형식으로 통일합니다.

### 품질 기준 (마일스톤 통과 조건)

- [ ] 위 엔드포인트와 상태 코드를 모두 구현했다
- [ ] 테스트 **15개 이상**이 통과한다. 구성: `@DataJpaTest` 2개 이상(리포지토리 쿼리), `@SpringBootTest`+`MockMvc` 10개 이상(엔드포인트·오류), 서비스 단위 테스트 3개 이상(가짜 객체를 `new` 로 넣어 조립, Spring 없이)
- [ ] 1단계 시나리오 13개가 모두 명세대로 동작한다
- [ ] `GET /members/{id}/loans` 에서 대출이 여러 건이어도 **쿼리 수가 대출 건수에 비례하지 않음**을 테스트로 확인했다 (23주차의 Hibernate Statistics)
- [ ] 컨트롤러에 규칙을 판단하는 `if` 가 없다 (규칙은 서비스에)
- [ ] 업무 오류는 사용자 정의 예외 + `@RestControllerAdvice` 로 변환한다
- [ ] README 에 실행 방법, 엔드포인트 표, 내가 내린 설계 결정 3개와 그 이유를 적었다

## 시작하기: 프로젝트 만들기

`../../labs/backend/` 에 `week24` 모듈을 추가합니다.

1. `../../labs/backend/settings.gradle.kts` 의 `include(...)` 에 `"week24"` 를 추가합니다.
2. `../../labs/backend/week24/build.gradle.kts` 를 만듭니다. `week23/build.gradle.kts` 를 복사한 뒤 의존성에 **웹과 검증**을 더하면 됩니다.

   ```kotlin
   dependencies {
       implementation("org.springframework.boot:spring-boot-starter-web")
       implementation("org.springframework.boot:spring-boot-starter-validation")
       implementation("org.springframework.boot:spring-boot-starter-data-jpa")
       runtimeOnly("com.h2database:h2")
       testImplementation("org.springframework.boot:spring-boot-starter-test")
       testRuntimeOnly("org.junit.platform:junit-platform-launcher")
   }
   ```

   (플러그인·자바 21·`tasks.test` 설정은 `week23` 파일 그대로)
3. `week24/src/main/java/library/LibraryApplication.java` 에 `@SpringBootApplication` 클래스를 만듭니다. (22주차 파일을 참고)
4. 확인: `./gradlew :week24:test`  — 테스트가 하나도 없어도 빌드가 성공해야 합니다.

DB 는 H2 메모리 DB 를 씁니다. 임베디드 DB 라서 별도 설정 없이 테이블이 자동으로 만들어집니다. 서버를 껐다 켜면 데이터가 사라집니다. 이 점은 README 에 적습니다.

## 구현 순서 (제안)

각 단계의 끝에서 `./gradlew :week24:test` 가 초록이어야 다음으로 갑니다.

| 단계 | 할 일 | 참고 |
|---|---|---|
| ① | `Book` 엔티티 + `BookRepository` + 리포지토리 테스트(`@DataJpaTest`) | 23주차 |
| ② | `POST /books`, `GET /books/{id}` + 검증(400) + 중복 `isbn`(409) | 22주차 |
| ③ | `GET /books?keyword=` | 23주차의 이름 기반 쿼리 |
| ④ | `Member` + `POST /members` | |
| ⑤ | `Loan` 엔티티 + `POST /loans` (404 → 409 순서로 규칙을 하나씩) | 9주차의 `lend` |
| ⑥ | `POST /loans/{id}/return` | 9주차의 `giveBack` |
| ⑦ | `GET /members/{id}/loans` + **쿼리 수 테스트** | 23주차의 N+1 |
| ⑧ | 서비스 단위 테스트: 가짜 리포지토리를 `new` 로 넣어 Spring 없이 | 19주차 |
| ⑨ | README, 정리 | |

### 첫 테스트 예시 (②단계)

엔티티와 컨트롤러가 아직 없으므로 처음에는 **컴파일조차 되지 않거나 실패**해야 정상입니다. 이 빨간불이 출발점입니다.

```java
@SpringBootTest
@AutoConfigureMockMvc
@Transactional                       // 테스트가 끝나면 DB 변경을 되돌림
class BookApiTest {
    @Autowired MockMvc mvc;

    @Test
    void createBook_returns201WithLocation() throws Exception {
        mvc.perform(post("/books").contentType(APPLICATION_JSON)
                .content("{\"isbn\":\"111\",\"title\":\"이펙티브 자바\",\"author\":\"조슈아 블로크\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern(".*/books/\\d+")));
    }
}
```

Location 의 id 를 `1` 로 못 박지 않는 이유: 자동 증가 번호는 트랜잭션이 롤백되어도 되돌아가지 않아, 테스트 순서에 따라 번호가 달라집니다. **테스트는 실행 순서에 의존하면 안 됩니다.**

## 힌트

- **중복 `isbn`:** DB 에 `unique` 제약을 걸어도 되지만, 서비스가 먼저 `existsByIsbn` 으로 확인해 의미 있는 409 를 돌려주는 것이 사용자 친화적입니다. 동시에 두 요청이 오면 어떻게 될까요? (제약은 마지막 방어선) 설계 결정으로 README 에 적을 만한 주제입니다.
- **한도 검사:** 회원의 반납하지 않은 대출 수는 `countByMemberIdAndReturnedAtIsNull` 같은 이름 기반 쿼리로 셀 수 있습니다.
- **이미 대출 중인 책:** 같은 방식으로 책 기준 `existsByBookIdAndReturnedAtIsNull`.
- **오류 응답:** 22주차의 `ApiExceptionHandler` 를 확장합니다. 상태 코드가 같은 예외를 한 부모 예외로 묶어도 되고, 예외마다 `@ExceptionHandler` 를 둬도 됩니다. 선택의 이유를 README 에.
- **시간:** `loanedAt` 은 `Instant` 또는 `LocalDateTime`. 서비스가 `Clock` 을 주입받으면 테스트에서 시간을 고정할 수 있습니다 (19주차 DI 의 응용).
- **JSON 직렬화:** 엔티티를 그대로 응답으로 내보내면 지연 로딩 연관 때문에 문제가 생기거나 순환 참조가 생깁니다. 응답 전용 `record` DTO 로 변환하세요.

## 확장 과제 (필수 아님, 마일스톤 후)

1. **대출 기한:** 14일이 지나면 연체. `GET /loans/overdue`. `Clock` 으로 시간을 고정해 테스트
2. **페이징:** `GET /books?page=0&size=10` (`Pageable`)
3. **도서 분류:** `enum Category` 를 `@Enumerated(EnumType.STRING)` 으로 저장하고 분류별 조회
4. **프로파일:** `application-dev.yml` 과 `application-test.yml` 로 설정을 분리
5. **실제 DB:** H2 대신 PostgreSQL 을 도커로 띄워 연결 (설정 파일의 URL 과 드라이버만 바꿔 봅니다)

## 점검 질문 (자기 설명)

코드를 다 짠 뒤 소리 내어 설명해 보세요.

1. 대출 규칙이 컨트롤러가 아니라 서비스에 있는 이유는?
2. 엔티티를 그대로 응답으로 내보내지 않고 DTO 로 바꾸는 이유는?
3. `GET /members/{id}/loans` 에서 N+1 이 생기는 경로와, 내가 막은 방법은?
4. 서비스 단위 테스트(Spring 없음)와 `@SpringBootTest` 테스트는 각각 무엇을 보증하는가?
5. 이 프로젝트에서 내가 한 설계 결정 중 가장 마음에 들지 않는 것은? 시간이 있으면 어떻게 바꾸겠는가?

<details>
<summary>답변 힌트</summary>

1. 규칙은 HTTP 와 무관해야 다른 입구(배치, 다른 API)에서도 같은 규칙이 적용된다. 컨트롤러는 변환만
2. 엔티티는 DB 모양이고 응답은 계약이다. 직렬화 문제(지연 로딩, 순환 참조)를 피하고, 내부 필드를 숨기고, 둘을 따로 바꿀 수 있다
3. 대출마다 책·회원 연관에 접근하면 대출 수만큼 쿼리가 늘어난다. 처음 쿼리에서 함께 가져오기(`@EntityGraph`/`join fetch`)로 막고, 통계로 쿼리 수를 검사한다
4. 단위 테스트: 규칙 자체(빠르고 원인이 좁음). 통합 테스트: 어노테이션·매핑·JSON·상태 코드가 실제로 연결되는지
5. 정답은 없다. 이유를 말할 수 있으면 통과

</details>

## 마일스톤 통과 기준 (24주)

- [ ] 위 "품질 기준" 7개를 모두 충족했다
- [ ] 1단계의 예측 13개를 적었고, 틀린 것의 이유를 노트에 남겼다
- [ ] 4단계에서 일부러 넣은 버그를 테스트가 잡았다
- [ ] 이 프로젝트를 처음 보는 사람에게 15분 안에 구조를 설명할 수 있다
