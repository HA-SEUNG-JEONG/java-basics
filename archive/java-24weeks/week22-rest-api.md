# 22주차. REST API 만들기

**예상 시간:** 10시간 | **목표:** 18주차에 손으로 만든 REST 서버를 Spring MVC 로 다시 만든다. CRUD 5개 엔드포인트, 요청 검증, 에러 응답을 한 곳에서 처리하는 구조까지 완성한다.

> 명령은 모두 `../../labs/backend/` 에서 실행합니다. 21주차를 먼저 끝내세요.

## 🖐 먼저 손으로 (읽기 전에 하세요)

> 이 주차의 핵심은 아래 단계를 **직접 타이핑하면서** 깨닫는 것입니다. 아래쪽 설명 문서는 먼저 읽지 않아도 됩니다. 막혔을 때 사전처럼 펼쳐 보세요.

### 1단계. 상태 코드 예측하기 → 서버 만들고 → 비교하기 (30분 + 3단계 후 확인)

도서 API 를 만들기 **전에**, 아래 요청을 위에서 아래로 차례로 보냈을 때 각각의 **상태 코드**를 노트에 적으세요. 요청은 서버를 새로 켠 상태에서 순서대로 보냅니다. (18주차의 상태 코드 표를 참고해도 됩니다.)

| # | 요청 | 내 예측 |
|---|------|------|
| 1 | `GET /books` | |
| 2 | `POST /books` `{"title":"클린 코드","author":"로버트 마틴"}` | |
| 3 | `POST /books` `{"title":"TDD","author":"켄트 벡"}` — 이때 `Location` 헤더 값은? | |
| 4 | `POST /books` `{"title":"","author":"x"}` | |
| 5 | `GET /books/1` | |
| 6 | `GET /books/99` | |
| 7 | `PUT /books/1` `{"title":"새 제목","author":"x"}` | |
| 8 | `DELETE /books/1` | |
| 9 | `DELETE /books/1` (한 번 더) | |
| 10 | `GET /books/abc` | |
| 11 | `POST /books` 본문 `{` (깨진 JSON) | |
| 12 | `POST /books` 에 `Content-Type` 헤더 없이 JSON 전송 | |
| 13 | `PATCH /books/2` | |

3단계에서 서버를 완성한 뒤, 아래 명령으로 같은 요청을 실제로 보내 비교합니다. 틀린 줄에 ★를 치세요.

```bash
./gradlew :week22:bootRun        # 한 터미널에서 (Ctrl+C 로 종료)
```

다른 터미널에서 (서버를 새로 켠 직후, 위에서부터 한 번만 실행):

```bash
H='Content-Type: application/json'
c() { printf '%s -> ' "$1"; shift; curl -s -o /dev/null -w "%{http_code}\n" "$@"; }
c 1  localhost:8080/books
c 2  -X POST -H "$H" -d '{"title":"클린 코드","author":"로버트 마틴"}' localhost:8080/books
curl -s -i -X POST -H "$H" -d '{"title":"TDD","author":"켄트 벡"}' localhost:8080/books | grep -iE "^HTTP|^Location"
c 4  -X POST -H "$H" -d '{"title":"","author":"x"}' localhost:8080/books
c 5  localhost:8080/books/1
c 6  localhost:8080/books/99
c 7  -X PUT -H "$H" -d '{"title":"새 제목","author":"x"}' localhost:8080/books/1
c 8  -X DELETE localhost:8080/books/1
c 9  -X DELETE localhost:8080/books/1
c 10 localhost:8080/books/abc
c 11 -X POST -H "$H" -d '{' localhost:8080/books
c 12 -X POST -d '{"title":"a","author":"b"}' localhost:8080/books
c 13 -X PATCH -H "$H" -d '{}' localhost:8080/books/2
```

<details>
<summary>정답 (예측을 적은 뒤에 여세요)</summary>

```text
1  200   (본문 [])
2  201
3  201   Location: /books/2
4  400   {"message":"title: 공백일 수 없습니다"}
5  200
6  404   {"message":"책을 찾을 수 없습니다: 99"}
7  200
8  204
9  404
10 400
11 400
12 415
13 405
```

- 4 번 메시지 문구는 서버의 언어 설정에 따라 달라질 수 있습니다. 필드 이름(`title`)이 들어 있는지가 중요합니다.
- 10~13 번은 **내가 코드를 한 줄도 쓰지 않았는데** Spring 이 알아서 400·415·405 로 답한 것입니다. 숫자가 아닌 `abc` 를 `long id` 로 바꾸지 못한 것(400), JSON 이 아닌 본문(400), `Content-Type` 이 없는 요청(415 Unsupported Media Type), 매핑에 없는 메서드(405).

</details>

### 2단계. 일부러 망가뜨리기 (30분)

3단계 실습이 끝난 뒤에 하나씩 고장 내고 어느 테스트가 어떤 이유로 실패하는지 확인하세요. 원복은 필수입니다.

| 이렇게 바꿔 보세요 | 어떻게 될까요? |
|---|---|
| `BookRequest` 에서 `@NotBlank` 두 개 삭제 | 빈 제목·빈 작성자도 통과해 `create_blankTitle_is400…`, `create_missingAuthor_is400`, `update_invalidBody_is400` 3개 실패 |
| `ApiExceptionHandler` 의 `@RestControllerAdvice` 삭제 | 6개 실패. 메시지는 `ServletException: Request processing failed: library.BookNotFoundException: ...` — 업무 예외가 응답으로 바뀌지 못하고 그대로 새어 나온다. 예외를 응답으로 바꿔 주는 곳이 이 한 클래스뿐이라는 증거 |
| `delete` 메서드의 `@ResponseStatus(HttpStatus.NO_CONTENT)` 삭제 | 204 가 아니라 200 이 나가 `delete_is204_thenGetIs404` 1개 실패 |
| `@PathVariable long id` 에서 `@PathVariable` 만 삭제 | 5개 실패. `IllegalStateException: Optional long parameter 'id' is present but cannot be translated into a null value ...` — 어노테이션이 없는 단순 타입 매개변수는 URL 경로가 아니라 쿼리 파라미터(`?id=`)로 해석되어 값이 없다 |
| `create` 가 `Book` 만 반환하게 (`ResponseEntity` 제거) | 201·`Location` 대신 200. `create_returns201WithLocationAndBody` 실패 |

### 3단계. 빈칸에서 만들기 — 자동 채점 (3~5시간)

`../../labs/backend/week22/src/main/java/library/` 의 세 파일을 채웁니다. `BookService`(메모리 저장소), `Book`, `ApiError`, `BookNotFoundException` 은 주어진 코드입니다.

| 파일 | 해야 할 일 |
|---|---|
| `BookRequest` | `title`, `author` 가 비면 거부하는 검증 표시 |
| `BookController` | REST 컨트롤러 + CRUD 5개 메서드 |
| `ApiExceptionHandler` | 예외 → 상태 코드·JSON 본문 변환 |

```bash
cd ../../labs/backend
./gradlew :week22:test
```

- 처음에는 13개가 모두 실패합니다. 정상입니다. 컨트롤러가 빈이 아니면 모든 경로가 404 로 보이기 때문입니다.
- 정답은 `../../labs/solutions/week22/` 에 있습니다. **15분 넘게 막혔을 때만** 열고, 본 뒤에는 덮고 **직접 다시 치세요.**

---

## 1. 18주차 코드와 비교

| 18주차에 손으로 한 일 | 22주차에 Spring 이 해 주는 일 |
|---|---|
| `handle` 에서 메서드·경로로 `switch` | `@GetMapping("/{id}")` 등 어노테이션 |
| `path.substring(...)` 과 `Long.parseLong` | `@PathVariable long id` (변환 실패는 400) |
| `mapper.readValue(ex.getRequestBody(), NewTodo.class)` | `@RequestBody BookRequest req` |
| `mapper.writeValueAsBytes`, Content-Type, 길이, `write` | 반환 객체를 JSON 으로 자동 변환 |
| `title == null \|\| isBlank` 를 `if` 로 검사 | `@NotBlank` + `@Valid` |
| `try/catch` 로 모든 예외를 500 처리 | `@RestControllerAdvice` 로 한 곳에서 변환 |

**손으로 만든 경험 덕분에 마법이 아니게 됩니다.** 위 표의 왼쪽을 이미 직접 해 봤습니다.

## 2. 컨트롤러

```java
@RestController
@RequestMapping("/books")
public class BookController {
    @GetMapping("/{id}")
    public Book get(@PathVariable long id) { ... }

    @PostMapping
    public ResponseEntity<Book> create(@Valid @RequestBody BookRequest req) {
        Book b = service.create(req);
        return ResponseEntity.created(URI.create("/books/" + b.id())).body(b);   // 201 + Location
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)                                       // 204
    public void delete(@PathVariable long id) { ... }
}
```

- 상태 코드를 바꾸고 싶으면 `ResponseEntity` 를 반환하거나, 고정이면 `@ResponseStatus` 를 붙입니다.
- `record` 는 요청·응답 본문으로 그대로 쓸 수 있습니다. 요청 전용 타입(`BookRequest`)과 응답 타입(`Book`)을 **나누는 이유**: 클라이언트가 `id` 를 정해서 보내지 못하게 하고, 내부 필드를 노출하지 않기 위해서입니다.

## 3. 검증

```java
public record BookRequest(@NotBlank String title, @NotBlank String author) {}
```

`@Valid` 가 붙은 `@RequestBody` 는 컨트롤러 메서드에 **들어오기 전에** 검사하고, 실패하면 `MethodArgumentNotValidException` 을 던집니다. `@NotBlank` 는 `null`, `""`, `"   "` 모두 거부합니다. (`@NotNull` 은 `null` 만, `@NotEmpty` 는 `""` 까지)  18주차의 "빠진 필드는 `null`" 이 여기서 막힙니다. `spring-boot-starter-validation` 의존성이 필요하고 `week22/build.gradle.kts` 에 이미 들어 있습니다.

## 4. 예외를 응답으로

```java
@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<ApiError> notFound(BookNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiError(e.getMessage()));
    }
}
```

서비스는 `BookNotFoundException`(업무 예외)만 던지고 HTTP 를 모릅니다. "이 예외는 404" 라는 번역은 이 클래스가 담당합니다. 서비스 코드가 `404` 라는 숫자를 몰라도 되는 것이 핵심입니다. (9주차의 "입출력은 `Main`, 규칙은 도메인" 과 같은 분리)

## 5. API 설계 체크

| 질문 | 이번 API 의 답 |
|---|---|
| 자원은 명사 복수형인가 | `/books`, `/books/{id}` |
| 메서드가 의미에 맞는가 | 조회 GET, 생성 POST, 교체 PUT, 삭제 DELETE |
| 성공·실패 코드가 구분되는가 | 200/201/204 와 400/404 |
| 에러 응답의 모양이 일정한가 | 항상 `{"message": "..."}` |
| 없는 것을 삭제하면? | 이번에는 404. (18주차는 204 로 함 — 둘 다 근거가 있는 선택이다. **팀이 정하고 문서에 적는 것**이 중요) |

## 연습문제

1. `GET /books?author=켄트 벡` 처럼 작성자로 거르는 기능을 추가하세요. (`@RequestParam(required = false) String author`)
2. `title` 길이를 100자 이하로 제한(`@Size(max = 100)`)하고, 101자를 보내 400 이 나오는지 `curl` 로 확인하세요.
3. 같은 제목 책을 등록하면 `409 Conflict` 를 돌려주도록 `DuplicateBookException` 과 핸들러를 추가하세요.
4. 응답 에러 본문에 상태 코드도 넣어(`{"status":404,"message":"..."}`) 보세요. 어느 파일 몇 개를 고쳐야 하나요?

## 퀴즈

1. `@Valid` 가 없으면 `@NotBlank` 는 어떻게 되나?
2. 서비스가 `ResponseEntity` 나 상태 코드를 직접 다루지 않는 편이 좋은 이유는?
3. 400 과 404 와 415 가 각각 언제 나오나?
4. `@RestControllerAdvice` 를 지우면 `BookNotFoundException` 은 어떤 응답이 되나?

<details>
<summary>정답</summary>

1. 검사하지 않는다. 어노테이션은 표시일 뿐 `@Valid` 가 검사를 시작시킨다
2. 서비스가 웹에 묶이면 다른 곳(배치, 테스트, 다른 API)에서 재사용하기 어렵고, HTTP 번역은 한 곳에서 일관되게 하는 편이 낫다
3. 400: 요청 내용이 잘못됨(검증 실패, 깨진 JSON, 타입 불일치). 404: 자원이 없음. 415: 서버가 받을 수 없는 `Content-Type`
4. 처리되지 않은 예외로 취급되어 실제 서버에서는 500 오류가 된다

</details>

## 체크리스트

- [ ] 13개 요청의 상태 코드를 예측하고, 틀린 것의 이유를 적었다
- [ ] 13개 테스트를 모두 통과했고 `curl` 로 직접 호출해 봤다
- [ ] 18주차 코드의 어느 부분을 Spring 어노테이션이 대신하는지 표 없이 말할 수 있다
- [ ] 검증 실패와 업무 예외가 어느 클래스에서 응답으로 바뀌는지 안다
