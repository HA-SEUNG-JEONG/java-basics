## 코드 해석하기

```java
Optional<Book> findById(Long id);
List<Book> findByTitleContainingIgnoreCaseOrderByIdAsc(String keyword);
public ResponseEntity<BookResponse> create(@Valid @RequestBody BookRequest req)
public BookResponse get(@PathVariable long id)
ResponseEntity<ApiError> notFound(NotFoundException e)
static <T> T assignId(T entity, List<T> all)
```

1. Book에서 id로 책을 찾는다.
2. 오름차순으로 정렬된 Book에서 키워드로 검색을 한다.
3. 잘 모르겠음
4. 잘 모르겠음
5. 해당하는 책이 없음
6. 잘 모르겠음

---

## 해설

### 공통 배경: 시그니처 읽는 법

Java 메서드 선언은 `[접근 제어자] [static] [<제네릭>] 반환타입 메서드명(매개변수)` 순서이고, TypeScript와 타입의 위치가 반대다.

| Java | TypeScript |
|------|------------|
| `Optional<Book> findById(Long id)` | `findById(id: number): Optional<Book>` |
| `String keyword` | `keyword: string` |
| `static <T> T assignId(T entity, List<T> all)` | `function assignId<T>(entity: T, all: T[]): T` |

- 반환 타입이 이름 앞에 오고, 매개변수는 `타입 이름` 순서다.
- `@Valid`, `@RequestBody`, `@PathVariable` 같은 `@` 표기는 어노테이션이다. 코드 로직이 아니라 Spring에게 주는 지시문이다. TypeScript 데코레이터와 비슷하다.
- 1, 2번은 본문 `{ }`이 없는 `interface` 메서드다. 구현 코드는 Spring Data JPA가 실행 중에 만든다(3주 5절 `interface`, 4주 3절 서비스와 리포지토리).

### 1번. `Optional<Book> findById(Long id)`

| 항목 | 내용 |
|------|------|
| 답안 | "Book에서 id로 책을 찾는다" |
| 평가 | 방향 맞음. 반환 타입 `Optional<Book>`의 의미가 빠짐 |

- `Optional<T>`는 "값이 있을 수도 없을 수도 있는 상자"다. 없을 때 `null` 대신 비어 있는 상자 `Optional.empty()`를 돌려준다. TypeScript의 `Book | undefined`에 대응한다.
- `Long`(대문자)은 객체 타입이고 `long`(소문자)은 원시 타입이다. `Optional<Book>`처럼 제네릭 자리에는 원시 타입을 쓸 수 없어서 `Long`이 쓰인다.
- 해석: "id가 일치하는 `Book`을 조회한다. 없으면 `Optional.empty()`가 반환된다."
- 이어지는 코드: `service`에서 `.orElseThrow(...)`로 비어 있을 때 `NotFoundException`을 던진다. 이 예외가 5번 핸들러로 이어진다.

### 2번. `findByTitleContainingIgnoreCaseOrderByIdAsc`

| 항목 | 내용 |
|------|------|
| 답안 | "오름차순으로 정렬된 Book에서 키워드로 검색을 한다" |
| 평가 | 순서와 조건이 틀림. 정렬이 검색보다 먼저가 아니고, `Title`과 `IgnoreCase`가 빠짐 |

Spring Data JPA는 메서드 이름을 단어 단위로 쪼개 쿼리로 만든다. 이름이 곧 쿼리 명세다.

| 조각 | 의미 | SQL 대응 |
|------|------|----------|
| `findBy` | 조회 시작 | `SELECT ... FROM book` |
| `Title` | 대상 필드 `Book.title` | `WHERE title` |
| `Containing` | 값을 포함하는지 검사 | `LIKE '%keyword%'` |
| `IgnoreCase` | 대소문자 무시 | `UPPER(title)`로 비교 |
| `OrderByIdAsc` | `id` 오름차순 정렬 | `ORDER BY id ASC` |

- SQL에서 `WHERE`(조건)가 먼저 적용되고 `ORDER BY`(정렬)는 결과에 적용된다. 이름 끝의 `OrderBy...`는 필터링된 결과의 정렬 기준이다.
- 반환 타입 `List<Book>`은 0개 이상의 결과를 담는다. 일치하는 책이 없어도 `null`이 아니라 빈 리스트다.
- 해석: "제목에 `keyword`가 포함된 `Book`을 대소문자 구분 없이 찾아 `id` 오름차순 `List`로 반환한다."
- 필요한 지식: 메서드 이름 규칙(`findBy` / `Containing` / `IgnoreCase` / `OrderBy`)은 4주 3절과 5주 DB·JPA에서 SQL과 함께 다룬다.

### 3번. `create(@Valid @RequestBody BookRequest req)`

| 항목 | 내용 |
|------|------|
| 답안 | 잘 모르겠음 |
| 필요한 지식 | 어노테이션 두 개, `ResponseEntity<T>`, `BookRequest`가 DTO라는 것 |

| 요소 | 의미 | 프론트 관점 |
|------|------|-------------|
| `@RequestBody` | 요청 본문 JSON을 `BookRequest` 객체로 변환 | `await request.json()` 결과를 타입에 맞춰 받는 것 |
| `@Valid` | `BookRequest` 안의 검증 규칙(`@NotBlank` 등)을 실행. 위반 시 메서드 본문 전에 예외가 나고 400 응답 | zod 스키마 검증과 비슷하나 서버에서 실행 |
| `ResponseEntity<BookResponse>` | 응답 본문(`BookResponse`)과 상태 코드·헤더를 함께 담는 응답 객체 | `new Response(body, { status, headers })` |
| `BookRequest` / `BookResponse` | 요청·응답 전용 DTO. `record`로 정의된다 | 요청·응답 TypeScript 타입 |

- 같은 클래스의 본문은 `ResponseEntity.created(URI.create("/books/" + book.id())).body(book)`이다. 상태 코드 **201 Created**, 헤더 `Location: /books/{id}`, 본문 `BookResponse`가 한 번에 만들어진다.
- 상태 코드를 직접 정해야 해서 `ResponseEntity`를 쓴다. 4번처럼 객체만 반환하면 항상 200이다.
- 해석: "요청 JSON을 `BookRequest`로 변환·검증한 뒤 책을 등록하고, 201과 `Location` 헤더, `BookResponse` 본문으로 응답한다."

### 4번. `get(@PathVariable long id)`

| 항목 | 내용 |
|------|------|
| 답안 | 잘 모르겠음 |
| 필요한 지식 | URL 경로 변수와 반환 타입에 따른 응답 차이 |

- 같은 클래스에서 `@GetMapping("/{id}")`와 `@RequestMapping("/books")`가 합쳐져 `GET /books/{id}`가 된다. `{id}` 자리의 값이 `id` 매개변수로 들어온다. Next.js `app/books/[id]`의 `params.id`에 대응한다.
- 경로 값은 문자열로 오고 Spring이 `long`으로 변환한다. 숫자가 아니면(`/books/abc`) 변환이 실패해 400이 난다(코드의 `@ApiResponse 400 "id 가 숫자가 아님"`).
- `@RequestParam`(`?keyword=`, 쿼리 문자열)과 구분된다. `@PathVariable`은 경로 안, `@RequestParam`은 `?` 뒤다.
- 반환 타입이 `BookResponse`이고 `ResponseEntity`가 아니므로 성공 시 상태 코드는 항상 200이고 본문은 JSON이다. 책이 없으면 서비스가 `NotFoundException`을 던진다.
- 해석: "`GET /books/{id}`의 `id`로 책을 찾아 `BookResponse`를 200으로 반환한다. 없으면 404."

### 5번. `ResponseEntity<ApiError> notFound(NotFoundException e)`

| 항목 | 내용 |
|------|------|
| 답안 | "해당하는 책이 없음" |
| 평가 | 상황은 맞으나 메서드의 역할이 빠짐. 이 메서드는 "책이 없음"이라는 상황을 처리하는 **예외 핸들러**다 |

- 이 메서드는 `@RestControllerAdvice` 클래스 안에 있고 `@ExceptionHandler(NotFoundException.class)`가 붙어 있다(시그니처 위의 줄). 어디서든 `NotFoundException`이 던져지면 Spring이 이 메서드를 호출한다. try/catch를 한 곳에 모은 것에 해당한다.
- 매개변수 `NotFoundException e`가 잡힌 예외다. 메서드 본문은 `error(HttpStatus.NOT_FOUND, e.getMessage())`를 반환한다.
- 반환 타입은 `ResponseEntity<ApiError>`다. `ApiError`는 `{"message": "..."}` 형태의 오류 본문이고 상태 코드는 404다.
- 해석: "`NotFoundException`이 발생하면 404 상태 코드와 `{"message": "..."}` 본문의 응답을 만든다."
- 필요한 지식: 예외가 던져지면 호출 스택을 거슬러 올라가며 처리자를 찾는다. 컨트롤러가 직접 잡지 않아도 `@ExceptionHandler`가 처리한다(3주 10절 예외, 4주 4절 검증과 예외 처리). 프론트에서는 이 본문이 7주 `ApiError` 분기의 입력이다.

### 6번. `static <T> T assignId(T entity, List<T> all)`

| 항목 | 내용 |
|------|------|
| 답안 | 잘 모르겠음 |
| 필요한 지식 | 제네릭 메서드(`<T>`), `static` |

- `static`은 객체를 만들지 않고 `클래스명.assignId(...)`로 부르는 메서드다. 인스턴스 상태에 의존하지 않는 유틸 함수에 쓰인다.
- 첫 번째 `<T>`는 "이 메서드 안에서 `T`라는 타입 이름을 쓴다"는 선언이다. 두 번째 `T`가 반환 타입이다. `<T>`가 없으면 `T`를 알 수 없는 타입으로 취급해 컴파일되지 않는다.
- `T entity`와 `List<T> all`의 `T`는 같은 타입이다. `Book`을 넘기면 `all`은 `List<Book>`이어야 하고 반환도 `Book`이다.
- 같은 규칙을 `Book`, `Member`, `Loan`에 각각 쓰려고 `Object` 대신 제네릭으로 한 번만 작성한 것이다. 반환값을 형변환 없이 받을 수 있다.
- 실제 코드(`LibraryServiceTest`)에서는 `id`가 `null`이면 `all.size() + 1`을 `id`로 정하고 리스트에 추가한 뒤 같은 객체를 돌려준다. 테스트용 가짜 리포지토리에서 DB의 `id` 자동 생성을 흉내 낸다.
- 해석: "어떤 타입 `T`의 엔티티와 같은 타입의 목록을 받아, `id`를 채워 목록에 넣고 같은 `T` 엔티티를 반환한다."

### 틀린 이유 요약

| 번호 | 부족했던 지식 | 참고 |
|------|---------------|------|
| 1 | `Optional`의 의미 | 3주 8절 `Optional` |
| 2 | 메서드 이름 → 쿼리 규칙, 필터 뒤 정렬 | 4주 3절, 5주 DB·JPA |
| 3 | `@RequestBody` / `@Valid` / `ResponseEntity` | 4주 2절 컨트롤러 |
| 4 | `@PathVariable`과 `@RequestParam`의 차이 | 4주 2절 컨트롤러 |
| 5 | `@ExceptionHandler`의 역할 | 4주 4절 검증과 예외 처리 |
| 6 | 제네릭 메서드 `<T>` 문법, `static` | 3주 11절 제네릭 |