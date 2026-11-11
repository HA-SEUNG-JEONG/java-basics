# 3주차. Java 코드 읽기: TypeScript 와 나란히 놓고 읽기

**예상 시간:** 6~8시간 (하루 1시간 세션 8회) | **목표:** 백엔드 PR 에 나온 Java 파일을 열어 `class`, `record`, `interface`, `enum`, 어노테이션, `Optional`, `List`/`Map`, 예외, 제네릭을 읽고, 각각이 JSON 계약과 프론트 타입에서 무엇에 해당하는지 말할 수 있다. 이 주차는 Java 를 **쓰는 법**이 아니라 **읽는 법**을 다룬다.

> 3주부터 5주까지는 백엔드 코드를 읽는 구간입니다. 읽기 자료는 참조 API `../../labs/backend/week25/src/main/java/library` 의 실제 소스이고, 이 API 는 "백엔드 팀이 준 API" 입니다. 직접 만든 코드가 아니므로 처음에는 낯선 것이 정상입니다. 이 문서에서 `Book.java`, `Dto.java`, `LibraryService.java` 처럼 파일 이름을 밝힌 Java 인용은 그 파일에서 그대로 옮긴 것이고, 참조 API 에 없는 개념을 설명하려고 따로 만든 코드 (`Predict03.java`, `Break03.java`, `EnumJson.java` 등)는 JDK 21 로 직접 실행해 확인한 것입니다.
>
> 준비물: JDK 21 (`java -version` 으로 확인), 소스를 열어 볼 에디터, 작업용 빈 폴더. 서버는 켜지 않아도 됩니다. 이 문서의 TypeScript 조각은 TypeScript 5.9 의 `--strict` 로 타입 검사를 통과한 것이고, Java 조각 중 "실행 결과" 가 적힌 것은 JDK 21 로 직접 컴파일·실행한 출력입니다.
>
> 작업 폴더는 아무 곳이나 괜찮습니다. 예: `mkdir -p ~/scratch/week03 && cd ~/scratch/week03`. 아래 `java 파일명.java` 는 파일 하나를 컴파일 없이 바로 실행하는 JDK 21 의 기능입니다.

## 하루 1시간으로 나누면

이 주차는 4단계가 모두 필수라서 하루 1시간씩 8번의 세션으로 나눕니다. 한 세션은 50~70분이고, 개념 절은 절 번호 단위로 세션에 배정했습니다.

| 세션 | 할 일 | 끝났을 때 할 수 있어야 하는 것 |
|---|---|---|
| 1 | 1단계(Java 를 읽고 TypeScript 타입으로 옮겨 적기, 30분)를 하고, 1절(파일 한 장을 읽는 순서)과 2절(TS 와 비교)을 읽는다 | Java 파일 한 장을 위에서부터 읽어 TypeScript 타입으로 옮길 수 있다 |
| 2 | 2단계(실행하지 않고 출력 예측하기, 20분)를 하고, 3절(`class`), 4절(`record`), 5절(`interface`)을 읽는다 | `class`, `record`, `interface` 가 각각 TypeScript 의 무엇에 대응하는지 말할 수 있다 |
| 3 | 3단계(일부러 망가뜨리기, 40분)를 하고, 6절(`enum`)을 읽는다 | 3단계의 고장 10가지를 컴파일 에러와 런타임 예외로 구분할 수 있다 |
| 4 | 7절(어노테이션)과 8절(null, Optional, 박싱)을 읽는다 | `long`/`Long`, `Optional`, `null` 이 JSON 과 프론트 타입의 `?`/`null` 에 어떻게 이어지는지 설명할 수 있다 |
| 5 | 9절(List, Map, stream, 람다), 10절(예외), 11절(제네릭)을 읽는다 (압축 코스에서는 건너뜀) | `stream` 체인을 TypeScript 배열 메서드 체인에 대응시켜 말하고, `throws` 가 붙은 메서드와 `<>` 가 붙은 선언을 읽을 수 있다 |
| 6 | 4단계(시그니처를 한국어 문장으로 풀기, 40분)를 하고, 12절(테스트 코드 읽기), 13절(자주 걸리는 문법 사전), 14절(한 필드가 DB 에서 화면까지 가는 길)을 읽는다 | 메서드 시그니처 한 줄만 보고 받는 것, 돌려주는 것, 없을 수 있는지를 말하고, `returnedAt` 처럼 필드 하나가 엔티티에서 화면까지 가는 길을 표로 그릴 수 있다 |
| 7 | 연습문제와 퀴즈를 푼다 (압축 코스에서는 건너뜀) | 실제 파일(`Member.java`, `LibraryService.java`)을 1절의 순서로 읽고 TypeScript 타입이나 한국어 문장으로 옮길 수 있다 |
| 8 | 체크리스트의 각 항목을 내 말로 설명해 보고, 막히는 항목은 해당 절을 다시 읽은 뒤 학습 노트를 정리한다 | Java 파일을 열었을 때 어디서부터 무엇을 읽어야 하는지 순서대로 말할 수 있다 |

세션을 시작하기 전에 이전 세션의 마지막 결과를 1~2분 훑어봅니다. 막히면 아래 해당 개념 절을 사전처럼 펼쳐 보세요.

## 🖐 먼저 손으로 (읽기 전에 하세요)

> 이 주차의 핵심은 아래 단계를 **직접 해 보면서** 깨닫는 것입니다. 아래쪽 설명 문서는 먼저 읽지 않아도 됩니다. 막혔을 때 사전처럼 펼쳐 보세요.

### 1단계. Java 를 읽고 TypeScript 타입으로 옮겨 적기 (30분)

`../../labs/backend/week25/src/main/java/library/Dto.java` 에서 가져온 아래 코드를 읽으세요. 이 파일은 "API 가 주고받는 JSON 의 모양" 을 정의합니다.

```java
public record BookRequest(@NotBlank String isbn, @NotBlank String title, @NotBlank String author) {}

public record BookResponse(
        @Schema(requiredMode = REQUIRED) long id,
        @Schema(requiredMode = REQUIRED) String isbn,
        @Schema(requiredMode = REQUIRED) String title,
        @Schema(requiredMode = REQUIRED) String author) {
    static BookResponse from(Book b) {
        return new BookResponse(b.getId(), b.getIsbn(), b.getTitle(), b.getAuthor());
    }
}

public record LoanResponse(
        @Schema(requiredMode = REQUIRED) long id,
        @Schema(requiredMode = REQUIRED) BookResponse book,
        @Schema(requiredMode = REQUIRED) MemberResponse member,
        @Schema(requiredMode = REQUIRED) Instant loanedAt,
        @Schema(requiredMode = REQUIRED, types = {"string", "null"}, format = "date-time",
                description = "반납 전에는 null") Instant returnedAt) {
    static LoanResponse from(Loan l) {
        return new LoanResponse(l.getId(), BookResponse.from(l.getBook()), MemberResponse.from(l.getMember()),
                l.getLoanedAt(), l.getReturnedAt());
    }
}
```

아래 질문의 답을 **서버도 스펙 파일도 보지 말고** 노트에 먼저 적으세요. `MemberResponse` 는 `id`(숫자)와 `name`(문자열)을 가진 같은 모양의 record 입니다.

| # | 질문 | 내 예측 |
|---|------|------|
| 1 | `BookRequest` 를 TypeScript `type` 으로 쓰면? | isbn : string; title:string; author:string; |
| 2 | `BookResponse` 의 `long id` 는 TypeScript 에서 무엇인가? `null` 일 수 있는가? | number |
| 3 | `LoanResponse.returnedAt` 의 TypeScript 타입은? (Java 타입은 `Instant`) | string, 예측 실패 string이거나 null임. |
| 4 | `LoanResponse` 의 `loanedAt` 은 JSON 에서 어떤 모양의 값인가? | 모르겠음, ISO 8601 형식의 문자열 |
| 5 | `@NotBlank` 는 TypeScript 타입에 어떤 흔적을 남기는가? | 비어있어서는 안되는 값, 예측 실패. 흔적이 없음. @NotBlank는 스펙에 minLengh: 1로 나가지만,타입스크립트 타입은 그냥 string |
| 6 | `static BookResponse from(Book b)` 는 TypeScript 의 무엇과 비슷한가? | 모르겠음 , 변환 함수입니다. BookResponse.from(book) 처럼 클래스 이름으로 호출하는 static 메서드이고, 엔티티(DB 모양)를 응답(API 모양)으로 바꿉니다. TypeScript 에서는 toBookResponse(book) 같은 함수나 클래스의 static 메서드와 같은 역할|

예측을 적은 뒤, 프론트 저장소가 스펙에서 **생성한** 타입과 비교합니다. (저장소 루트에서 실행)

```bash
grep -n -A6 "BookResponse: {" labs/frontend/src/api/schema.d.ts
grep -n -A12 "LoanResponse: {" labs/frontend/src/api/schema.d.ts
grep -n -A5 "BookRequest: {" labs/frontend/src/api/schema.d.ts
```

<details>
<summary>정답 (예측을 적은 뒤에 여세요)</summary>

`schema.d.ts` 의 해당 부분입니다.

```ts
        BookResponse: {
            /** Format: int64 */
            id: number;
            isbn: string;
            title: string;
            author: string;
        };
        LoanResponse: {
            /** Format: int64 */
            id: number;
            book: components["schemas"]["BookResponse"];
            member: components["schemas"]["MemberResponse"];
            /** Format: date-time */
            loanedAt: string;
            /**
             * Format: date-time
             * @description 반납 전에는 null
             */
            returnedAt: string | null;
        };
        BookRequest: {
            isbn: string;
            title: string;
            author: string;
        };
```

- 1번: 세 필드 모두 필수인 `{ isbn: string; title: string; author: string }` 입니다. Java 에서는 필드를 비워 둘 수 없고(`null` 이 들어와도 검증이 막음), TypeScript 의 `?` 에 해당하는 표시가 없습니다.
- 2번: `number` 입니다. `long` 은 소수점 없는 정수 타입이고 JSON 에서는 그냥 숫자입니다. 소문자 `long` 은 `null` 이 될 수 없는 기본형이고, 대문자 `Long` 은 `null` 이 될 수 있는 객체형입니다(아래 8절). `Book` 엔티티의 `id` 가 `Long` 인 이유는 저장되기 전에는 값이 없기 때문입니다.
- 3번: `string | null` 입니다. `Instant` 라는 Java 타입이 TypeScript 에서는 문자열이 됩니다. `@Schema(... types = {"string", "null"} ...)` 와 주석 "반납 전에는 null" 이 그 근거입니다.
- 4번: ISO 8601 형식의 문자열입니다. 서버가 실제로 돌려준 값은 `"2026-10-22T13:46:04.203082Z"` 모양이고, 4주에 직접 호출해서 봅니다. 프론트에서 날짜로 쓰려면 `new Date(문자열)` 로 변환해야 합니다.
- 5번: 흔적이 없습니다. `@NotBlank` 는 스펙에 `minLength: 1` 로 나가지만 TypeScript 타입은 그냥 `string` 입니다. "빈 문자열은 안 된다" 는 규칙은 타입이 아니라 서버(400 응답)에 있습니다.
- 6번: 변환 함수입니다. `BookResponse.from(book)` 처럼 클래스 이름으로 호출하는 `static` 메서드이고, 엔티티(DB 모양)를 응답(API 모양)으로 바꿉니다. TypeScript 에서는 `toBookResponse(book)` 같은 함수나 클래스의 `static` 메서드와 같은 역할입니다.

</details>

### 2단계. 실행하지 않고 출력 예측하기 (20분)

아래 코드를 `Predict03.java` 로 **한 글자씩 타이핑**합니다. 타이핑하기 전에 먼저 각 `println` 의 출력 10줄을 노트에 적으세요. 틀린 줄에 ★를 치고, 왜 그렇게 나왔는지 한 문장으로 적습니다. 그 ★가 이 주차에서 가장 값진 부분입니다.

```java
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Predict03 {
    record Book(long id, String title) {}

    static Optional<Book> find(List<Book> books, long id) {
        return books.stream().filter(b -> b.id() == id).findFirst();
    }

    public static void main(String[] args) {
        List<Book> books = List.of(new Book(1, "Clean Code"), new Book(2, "TDD"));
        System.out.println(books.get(0));
        System.out.println(books.get(0).equals(new Book(1, "Clean Code")));
        System.out.println(books.get(0) == new Book(1, "Clean Code"));
        System.out.println(find(books, 2).map(Book::title).orElse("없음"));
        System.out.println(find(books, 9).map(Book::title).orElse("없음"));

        Map<String, Integer> stock = new HashMap<>();
        stock.put("TDD", 2);
        System.out.println(stock.get("TDD"));
        System.out.println(stock.get("Clean Code"));

        try {
            books.add(new Book(3, "Refactoring"));
        } catch (UnsupportedOperationException e) {
            System.out.println("읽기 전용 목록");
        }

        Long a = 127L, b = 127L;
        Long c = 1000L, d = 1000L;
        System.out.println((a == b) + " " + (c == d) + " " + c.equals(d));

        String s1 = "TDD";
        String s2 = new String("TDD");
        System.out.println((s1 == s2) + " " + s1.equals(s2));
    }
}
```

```bash
java Predict03.java
```

<details>
<summary>실행 결과 (예측을 적은 뒤에 여세요)</summary>

```text
Book[id=1, title=Clean Code]
true
false
TDD
없음
2
null
읽기 전용 목록
true false true
false true
```

- `record` 의 `toString()` 은 `Book[id=1, title=Clean Code]` 모양입니다. 로그에서 이 모양을 보면 record 객체입니다.
- `equals` 는 **내용** 비교(`true`)이고 `==` 는 **같은 객체인지** 비교(`false`)입니다. JavaScript 에서 객체끼리 `===` 가 참조 비교인 것과 같습니다. 다른 점은 Java 에는 `equals` 라는 내용 비교 방법이 따로 있다는 것입니다.
- `find` 는 값이 없으면 예외가 아니라 빈 `Optional` 을 돌려주고, `map(...).orElse("없음")` 으로 기본값을 줍니다. TypeScript 의 `books.find(...)?.title ?? "없음"` 과 같은 흐름입니다.
- `Map.get` 은 없는 키에 `null` 을 돌려줍니다(`undefined` 가 아닙니다).
- `List.of` 로 만든 목록은 수정할 수 없고, `add` 는 `UnsupportedOperationException` 을 던집니다.
- `Long` 객체끼리 `==` 로 비교하면 값이 작을 때(127)는 우연히 `true`, 클 때(1000)는 `false` 입니다. 그래서 박싱된 숫자와 문자열은 `==` 가 아니라 `equals` 로 비교합니다. 코드 리뷰에서 `==` 로 문자열이나 `Long` 을 비교한 줄을 보면 의심하세요.

</details>

### 3단계. 일부러 망가뜨리기 (40분)

아래 `Break03.java` 를 타이핑해 실행해 보고(`[TDD] 대출 중` 이 나와야 합니다), 한 번에 하나씩 고장 내 보세요. 먼저 **컴파일 에러(실행 전에 막힘)인지, 런타임 예외(실행 중에 터짐)인지** 예상한 뒤 실행합니다. 메시지를 외울 필요는 없고, 어떤 모양이고 어디를 가리키는지 눈에 익히는 것이 목표입니다. 한 곳을 고장 낸 뒤에는 원래대로 되돌립니다.

```java
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Break03 {
    record Book(long id, String title) {}

    enum Status { AVAILABLE, ON_LOAN, LOST }

    static Optional<Book> find(long id) {
        return id == 1 ? Optional.of(new Book(1, "TDD")) : Optional.empty();
    }

    static String label(Status s) {
        return switch (s) {
            case AVAILABLE -> "대출 가능";
            case ON_LOAN -> "대출 중";
            case LOST -> "분실";
        };
    }

    public static void main(String[] args) {
        Book b = find(1).orElseThrow();
        List<String> titles = new ArrayList<>();
        titles.add(b.title());
        System.out.println(titles + " " + label(Status.ON_LOAN));
    }
}
```

| # | 이렇게 바꿔 보세요 | 어떻게 될까요? |
|---|---|---|
| a | `titles.add(b.title());` 를 `titles.add(b.getTitle());` 로 | 컴파일 에러 `cannot find symbol` / `symbol:   method getTitle()`. record 의 접근자는 `get` 접두사 없이 `title()` 입니다 |
| b | `Book b = find(1).orElseThrow();` 를 `Book b = find(1);` 로 | 컴파일 에러 `incompatible types: Optional<Book> cannot be converted to Book`. `Optional` 은 값을 감싼 상자라서 꺼내야 합니다 |
| c | `titles.add(b.title());` 를 `titles.add(b.id());` 로 | 컴파일 에러 `no suitable method found for add(long)` ... `long cannot be converted to String`. `List<String>` 에는 문자열만 들어갑니다 |
| d | `case LOST -> "분실";` 줄을 지움 | 컴파일 에러 `the switch expression does not cover all possible input values`. enum 에 값이 늘면 `switch` 가 컴파일 단계에서 알려 줍니다 |
| e | `titles.add(b.title());` 를 `b.id = 5;` 로 | 컴파일 에러 `cannot assign a value to final variable id`. record 의 필드는 바꿀 수 없습니다 |
| f | `find(1).orElseThrow()` 를 `find(2).orElseThrow()` 로 | 런타임 예외 `java.util.NoSuchElementException: No value present` |
| g | `Book b = ...;` 다음 줄에 `Long x = null; long y = x;` 추가 | 런타임 예외 `java.lang.NullPointerException: Cannot invoke "java.lang.Long.longValue()" because "<local2>" is null`. `null` 인 `Long` 을 기본형 `long` 에 넣다가 터집니다 |
| h | `titles.add(b.title());` 다음 줄에 `System.out.println(titles.get(5));` 추가 | 런타임 예외 `java.lang.IndexOutOfBoundsException: Index 5 out of bounds for length 1` |
| i | `label(Status.ON_LOAN)` 을 `label(Status.valueOf("BROKEN"))` 으로 | 런타임 예외 `java.lang.IllegalArgumentException: No enum constant Break03.Status.BROKEN` |
| j | `main` 의 첫 줄에 `if (args.length == 0) throw new Exception("x");` 추가 | 컴파일 에러 `unreported exception Exception; must be caught or declared to be thrown`. `Exception` 은 checked 예외라서 `main` 이 `throws Exception` 을 선언하거나 `try-catch` 로 잡아야 컴파일됩니다 |

- a~e 와 j 는 코드를 실행하기도 전에 막힙니다. TypeScript 에서 `tsc` 가 막는 것과 같은 단계입니다. f~i 는 실행해야 드러납니다. 서버에서 이런 예외가 처리되지 않고 새어 나오면 프론트는 **500 응답**을 받습니다(4주에서 직접 확인합니다).
- 예외 출력의 `at Break03.main(Break03.java:23)` 같은 줄은 스택 트레이스입니다. (줄 번호는 고친 위치에 따라 달라서 f 는 23, g 는 24, h 는 26, i 는 26 입니다.) 위에서부터 읽으면 "어디서 터졌는지" 가 나옵니다. 백엔드 개발자가 로그를 붙여 줄 때 가장 먼저 볼 곳이 맨 위 예외 이름과 메시지, 그리고 내 패키지(`library.`)로 시작하는 첫 줄입니다.
- g 의 `"<local2>"` 는 컴파일 옵션에 따라 변수 이름(`x`)으로 나오기도 합니다. 중요한 것은 "`null` 을 풀다가 터졌다" 는 사실입니다.

### 4단계. 시그니처를 한국어 문장으로 풀기 (40분)

메서드의 본문은 읽지 않고 **한 줄(시그니처)만 보고** 의미를 말하는 연습입니다. 아래는 모두 참조 API 와 그 테스트에서 가져온 줄입니다. 각각을 "무엇을 받아서, 무엇을 돌려주고, 어떤 점을 조심해야 하는지" 한 문장으로 적으세요.

```java
Optional<Book> findById(Long id);
List<Book> findByTitleContainingIgnoreCaseOrderByIdAsc(String keyword);
public ResponseEntity<BookResponse> create(@Valid @RequestBody BookRequest req)
public BookResponse get(@PathVariable long id)
ResponseEntity<ApiError> notFound(NotFoundException e)
static <T> T assignId(T entity, List<T> all)
```

<details>
<summary>정답 (예시 문장)</summary>

1. `Optional<Book> findById(Long id)`: 숫자 id 를 받아 책을 찾습니다. 없을 수 있어서 `Book` 이 아니라 `Optional<Book>` 입니다. TypeScript 로는 `findById(id: number): Book | undefined` 입니다.
2. `List<Book> findByTitleContainingIgnoreCaseOrderByIdAsc(String keyword)`: 제목에 `keyword` 가 들어 있는 책(`Containing`)을 대소문자 구분 없이(`IgnoreCase`) 찾아 id 오름차순(`OrderByIdAsc`)으로 돌려줍니다. 메서드 이름이 곧 조회 조건이고, 5주에서 SQL 로 바뀌는 모습을 봅니다. 결과가 없으면 `null` 이 아니라 빈 목록입니다.
3. `create(@Valid @RequestBody BookRequest req)`: 요청 본문의 JSON 을 `BookRequest` 로 바꿔(`@RequestBody`) 검증한 뒤(`@Valid`) 받습니다. 돌려주는 `ResponseEntity<BookResponse>` 는 상태 코드와 헤더까지 직접 정할 수 있는 응답입니다.
4. `get(@PathVariable long id)`: URL 경로의 `{id}` 부분을 `long` 으로 받아 `BookResponse` 를 돌려줍니다. 숫자가 아닌 값이 오면 이 메서드까지 오지 못하고 400 이 됩니다.
5. `ResponseEntity<ApiError> notFound(NotFoundException e)`: `NotFoundException` 을 받아 `ApiError`(`{"message": ...}`)를 담은 응답으로 바꿉니다. 앞에 `public` 이 없어서 같은 패키지 안에서만 보이는 메서드이고, 어떤 상태 코드가 될지는 본문에 있습니다.
6. `static <T> T assignId(T entity, List<T> all)`: `<T>` 는 "어떤 타입이든 되는데 들어간 것과 나오는 것이 같은 타입" 이라는 선언입니다. TypeScript 의 `function assignId<T>(entity: T, all: T[]): T` 입니다.

</details>

---

## 1. 파일 한 장을 읽는 순서

모르는 Java 파일을 열면 아래 순서로 읽으세요. 본문은 마지막입니다. `Book.java` 전체입니다.

```java
package library;

import jakarta.persistence.*;

@Entity
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 서비스가 먼저 existsByIsbn 으로 409 를 돌려주고, 동시 요청은 이 제약이 마지막으로 막는다.
    @Column(nullable = false, unique = true)
    private String isbn;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    protected Book() {}

    public Book(String isbn, String title, String author) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
    }

    public Long getId() { return id; }
    public String getIsbn() { return isbn; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
}
```

1. **`package` / `import`**: 이 파일이 어느 폴더에 있고(`library`) 무엇에 기대는지 보여 줍니다. `jakarta.persistence.*` 는 DB 저장 표준(JPA)이므로 이 파일은 DB 와 관련된 클래스입니다.
2. **클래스 위의 어노테이션**: 이 클래스의 **역할**을 알려 줍니다. `@Entity` 는 "DB 테이블에 대응하는 클래스" 입니다.
3. **선언의 종류**: `class` 인지 `record` 인지 `interface` 인지 `enum` 인지 봅니다. 종류마다 읽는 방법이 다릅니다.
4. **필드**: 이 객체가 가진 데이터입니다. 필드 위의 어노테이션은 그 필드의 규칙입니다(`nullable = false` 는 "비어 있으면 안 되는 열").
5. **생성자**: 객체를 만드는 방법입니다. `protected Book() {}` 는 프레임워크가 쓰는 입구이고, `public Book(...)` 가 코드에서 쓰는 입구입니다.
6. **메서드 시그니처**: 외부에 약속한 동작입니다. 본문은 궁금할 때만 읽습니다.

파일 이름에서도 역할을 짐작할 수 있습니다. 참조 API 의 파일은 아래 규칙을 따릅니다.

| 이름 | 역할 | 이 API 의 예 |
|---|---|---|
| `*Controller` | HTTP 요청이 들어오는 입구. URL 과 상태 코드가 여기서 정해진다 | `BookController`, `LoanController` |
| `*Service` | 업무 규칙(대출 한도, 중복 검사) | `LibraryService` |
| `*Repository` | DB 조회·저장 | `BookRepository` |
| `Dto` (요청·응답 `record`) | API 가 주고받는 JSON 의 모양 = 계약 | `Dto.java` |
| `Book`, `Loan`, `Member` | 엔티티. DB 테이블의 모양 | `Book.java` |
| `*Exception` | 업무상 실패를 나타내는 예외 | `NotFoundException`, `ConflictException` |
| `*Config` | 보안·CORS 같은 설정 | `SecurityConfig` |

이 역할 나누기는 4주에서 요청 하나를 따라가며 다시 봅니다. 지금은 "프론트 타입에 영향을 주는 파일은 `Dto.java` 와 컨트롤러" 라는 것만 기억하세요.

## 2. TS 와 비교

완전히 같지는 않은 비유입니다. 오른쪽 열의 "다른 점" 이 읽다가 걸리는 곳입니다.

| Java | TypeScript 에서 비슷한 것 | 다른 점 |
|---|---|---|
| `class` | `class` | 필드를 본문에 선언하고, `private`/`protected`/`public`, 아무것도 안 쓰면 **같은 패키지에서만 보임** |
| `record` | 읽기 전용 필드의 `type` + 생성 함수 | 불변이고, 접근자는 `id()` 처럼 `get` 없이, `equals` 는 내용 비교 |
| `interface` | `interface` | 구현 클래스가 `implements` 로 **이름을 밝혀야** 함. 구조가 같아도 자동으로 호환되지 않음 |
| `enum` | 문자열 리터럴 유니언 `"A" \| "B"` | 실행 중에도 존재하는 객체, `values()` 와 `valueOf()` 가 있음. JSON 에서는 이름 문자열 |
| `@어노테이션` | 데코레이터 `@x` | 그 자체는 아무것도 실행하지 않는 **표시**. 읽고 동작하는 쪽은 프레임워크 |
| `Optional<T>` | `T \| undefined` | 값을 상자에 담은 객체. `map`, `orElse`, `orElseThrow` 로 꺼냄 |
| `null` | `null` / `undefined` | `undefined` 가 없고 `null` 하나뿐 |
| `List<T>` / `Map<K,V>` | `T[]` / `Map<K,V>` | `List.of(...)` 는 수정 불가. 없는 키는 `null` |
| `long`, `int`, `double` | `number` | 정수/실수가 구분됨. JSON 에서는 모두 숫자 |
| `Long`, `Integer` | `number \| null` | 소문자는 `null` 불가인 기본형, 대문자는 `null` 가능한 객체형 |
| `String` | `string` | 내용 비교는 `==` 가 아니라 `equals` |
| `Instant` | ISO 8601 `string` | JSON 으로 나가면 문자열(`2026-10-22T13:46:04.203082Z`) |
| `<T>` 제네릭 | `<T>` 제네릭 | 실행 시점에는 타입 정보가 지워짐 |
| `throw new X()` / `try-catch` | `throw` / `try-catch` | 선언해야 하는 예외(checked)와 안 해도 되는 예외(unchecked)가 있음 |
| `static` | `static` 메서드, 모듈의 함수 | 클래스 이름으로 호출 (`BookResponse.from(b)`) |
| `final` | `readonly` / `const` | 한 번 대입하면 바꿀 수 없음 |
| `package` | 폴더/모듈 | 파일 맨 위에 선언, `import library.Dto.ApiError;` 처럼 안쪽 타입까지 지정 가능 |

## 3. class: 필드, 생성자, 접근자

1절의 `Book` 을 TypeScript 로 옮기면 이렇습니다.

```ts
class Book {
  private id: number | null = null; // Long id: 저장되기 전에는 null
  private isbn: string;
  private title: string;
  private author: string;

  constructor(isbn: string, title: string, author: string) {
    this.isbn = isbn;
    this.title = title;
    this.author = author;
  }

  getId() { return this.id; }
  getIsbn() { return this.isbn; }
  getTitle() { return this.title; }
  getAuthor() { return this.author; }
}
```

- 필드는 `private` 이고 읽기는 `getXxx()` 로만 합니다. Java 에서는 이것이 오래된 관례(JavaBean)여서, 화면에 안 보이는 `getId()` 같은 줄이 많습니다. **게터가 모두 있다고 JSON 필드가 그 이름으로 나가는 것은 아닙니다.** 엔티티 `Book` 은 응답으로 나가지 않고 `BookResponse` 로 바뀌어 나갑니다.
- 생성자가 `protected Book() {}` 와 `public Book(...)` 둘입니다. 앞의 것은 DB 에서 읽은 행으로 객체를 만들 때 JPA 가 쓰는 입구이고, 직접 호출하는 것은 뒤의 것입니다(5주).
- `Loan.java` 에는 필드 옆에 짧은 설명이 있습니다. 코드가 말해 주지 않는 이유가 있으면 주석을 먼저 읽으세요.

```java
    private Instant returnedAt; // 반납 전에는 null
```

이 한 줄이 DB 의 `returned_at` 열이 비어 있을 수 있다는 뜻이고, 응답 JSON 의 `"returnedAt": null` 이 되고, 프론트 타입의 `string | null` 이 됩니다.

## 4. record: 값만 담는 불변 객체

`Dto.java` 의 맨 위는 이렇습니다.

```java
public final class Dto {
    private Dto() {}

    public record BookRequest(@NotBlank String isbn, @NotBlank String title, @NotBlank String author) {}

    public record MemberRequest(@NotBlank String name) {}
```

- `Dto` 는 `record` 들을 담아 두는 **상자** 입니다. 생성자를 `private` 으로 막아서 `new Dto()` 를 못 하게 했고, 안쪽 타입을 `Dto.BookRequest` 로 씁니다. 다른 파일에서는 `import library.Dto.BookRequest;` 로 가져옵니다. TypeScript 로는 한 파일에서 여러 타입을 `export` 하는 것과 같습니다.
- `record BookRequest(String isbn, ...)` 한 줄이 필드, 생성자, 접근자(`isbn()`), `equals`, `toString` 을 모두 만듭니다. 2단계의 `Book[id=1, title=Clean Code]` 가 그 결과입니다.
- record 에는 `set` 이 없습니다. 만든 뒤에 값을 바꿀 수 없으므로 "한 번 만들어 JSON 으로 내보내는" 응답 용도에 맞습니다.
- 요청용 record 와 응답용 record 가 따로 있는 이유를 `BookRequest`(id 없음)와 `BookResponse`(id 있음)에서 보세요. 클라이언트가 `id` 를 정해서 보낼 수 없게 하고, 서버 내부의 필드를 실수로 내보내지 않기 위해서입니다. 프론트의 `components["schemas"]["BookRequest"]` 와 `["BookResponse"]` 도 따로 생성됩니다.

record 이름과 필드는 **그대로 JSON 의 키** 가 됩니다. 필드 이름을 바꾼 PR 은 곧 API 계약을 바꾼 PR 입니다(4주).

```ts
type BookRequest = { isbn: string; title: string; author: string };
type BookResponse = { id: number; isbn: string; title: string; author: string };
type MemberResponse = { id: number; name: string };
type LoanResponse = {
  id: number;
  book: BookResponse;
  member: MemberResponse;
  loanedAt: string;
  returnedAt: string | null;
};
```

## 5. interface: 구현이 없는 약속

`BookRepository.java` 입니다.

```java
public interface BookRepository extends Repository<Book, Long> {
    Book save(Book book);

    Optional<Book> findById(Long id);

    boolean existsByIsbn(String isbn);

    List<Book> findAllByOrderByIdAsc();

    List<Book> findByTitleContainingIgnoreCaseOrderByIdAsc(String keyword);
}
```

- 메서드에 본문이 없습니다. 이 저장소 어디에도 이 인터페이스를 `implements` 하는 운영 코드가 없습니다. 실행할 때 Spring Data 가 구현체를 **만들어 줍니다**(4주).
- `extends Repository<Book, Long>` 의 `<Book, Long>` 은 "다루는 엔티티는 `Book`, 키 타입은 `Long`" 이라는 제네릭 인자입니다.
- 테스트에서는 이 인터페이스를 직접 구현한 가짜가 등장합니다. `LibraryServiceTest.java` 의 일부입니다.

```java
    static class FakeBooks implements BookRepository {
        final List<Book> all = new ArrayList<>();
        public Book save(Book b) { return assignId(b, all); }
        public Optional<Book> findById(Long id) { return all.stream().filter(b -> b.getId().equals(id)).findFirst(); }
        public boolean existsByIsbn(String isbn) { return all.stream().anyMatch(b -> b.getIsbn().equals(isbn)); }
        public List<Book> findAllByOrderByIdAsc() { return List.copyOf(all); }
        public List<Book> findByTitleContainingIgnoreCaseOrderByIdAsc(String k) {
            return all.stream().filter(b -> b.getTitle().toLowerCase().contains(k.toLowerCase())).toList();
        }
    }
```

TypeScript 로는 이렇게 읽으면 됩니다. 프론트에서 API 호출 함수를 테스트용 가짜로 갈아 끼우는 것과 같은 발상입니다.

```ts
interface BookRepository {
  save(book: Book): Book;
  findById(id: number): Book | undefined;
  existsByIsbn(isbn: string): boolean;
}

class FakeBooks implements BookRepository {
  private all: Book[] = [];
  save(book: Book) { this.all.push(book); return book; }
  findById(id: number) { return this.all.find((b) => b.getId() === id); }
  existsByIsbn(isbn: string) { return this.all.some((b) => b.getIsbn() === isbn); }
}
```

`BookRepository` 는 `JpaRepository` 대신 `Repository` 를 상속해서 필요한 메서드만 선언했고, 파일 위 주석에 "서비스 단위 테스트의 가짜 리포지토리가 이 몇 개만 구현하면 된다" 는 이유가 적혀 있습니다. 이처럼 **서비스가 구체 클래스가 아니라 인터페이스에 기대면 테스트에서 갈아 끼울 수 있습니다.** 12주에서 이 구조를 테스트 전략으로 다시 봅니다.

## 6. enum: 정해진 값의 집합

참조 API 에는 `enum` 이 없으므로 개념 설명용 코드로 봅니다. 실제 PR 에서 "대출 상태" 같은 필드가 추가되면 이런 모양일 것입니다. 아래 `EnumJson.java` 는 참조 API 가 쓰는 JSON 라이브러리(Jackson)로 변환해 본 것입니다.

```java
import com.fasterxml.jackson.databind.ObjectMapper;

public class EnumJson {
    enum Status { AVAILABLE, ON_LOAN, LOST }

    record BookView(long id, String title, Status status) {}

    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(new BookView(1, "TDD", Status.ON_LOAN));
        System.out.println(json);

        BookView back = mapper.readValue("{\"id\":1,\"title\":\"TDD\",\"status\":\"LOST\"}", BookView.class);
        System.out.println(back);

        System.out.println(Status.valueOf("ON_LOAN") + " " + Status.ON_LOAN.ordinal());
        for (Status s : Status.values()) System.out.print(s + " ");
        System.out.println();

        try {
            mapper.readValue("{\"id\":1,\"title\":\"TDD\",\"status\":\"BROKEN\"}", BookView.class);
        } catch (Exception e) {
            System.out.println(e.getClass().getSimpleName());
            System.out.println(e.getMessage().lines().findFirst().orElse(""));
        }
    }
}
```

실행 결과입니다.

```text
{"id":1,"title":"TDD","status":"ON_LOAN"}
BookView[id=1, title=TDD, status=LOST]
ON_LOAN 1
AVAILABLE ON_LOAN LOST 
InvalidFormatException
Cannot deserialize value of type `EnumJson$Status` from String "BROKEN": not one of the values accepted for Enum class: [ON_LOAN, AVAILABLE, LOST]
```

- `enum` 은 JSON 에서 **이름 문자열** 이 됩니다. 프론트에서는 `"AVAILABLE" | "ON_LOAN" | "LOST"` 유니언으로 받습니다. OpenAPI 스펙에서는 `enum` 목록으로 나가고 생성된 TypeScript 도 유니언이 됩니다.
- 목록에 없는 문자열을 보내면 서버가 읽지 못하고 실패합니다. 참조 API 에서는 이런 실패가 400 응답이 됩니다(`HttpMessageNotReadableException` 을 `ApiExceptionHandler` 가 400 으로 바꿉니다. 4주).
- 위 마지막 줄의 값 목록 순서(`[ON_LOAN, AVAILABLE, LOST]`)는 선언 순서와 다릅니다. 에러 메시지의 순서는 믿지 마세요.
- 새 값을 하나 추가하는 PR 은 **프론트의 유니언도 늘어나는 변경** 입니다. 프론트에서 `switch` 나 `Record<Status, string>` 로 모든 값을 처리하고 있다면 타입 검사가 빠진 곳을 알려 줍니다.

참조 API 의 **복사본** 에 이 `enum` 을 `BookRequest`/`BookResponse` 의 `status` 필드로 임시로 붙여(원본은 건드리지 않았습니다) 서버를 띄워 확인한 결과입니다.

- 스펙(`/v3/api-docs`)에는 `{"type":"string","enum":["AVAILABLE","ON_LOAN","LOST"]}` 로 나갔습니다.
- `openapi-typescript` 로 생성한 타입은 응답 쪽이 `status: "AVAILABLE" | "ON_LOAN" | "LOST";`, 요청 쪽이 `status?: "AVAILABLE" | "ON_LOAN" | "LOST";` 였습니다. 요청의 `?` 는 그 필드에 `@NotNull` 같은 필수 표시를 붙이지 않았기 때문입니다.
- 요청에 `"status":"LOST"` 를 보내면 201 이었고, `"status":"BROKEN"` 을 보내면 `{"message":"요청 본문을 읽을 수 없습니다 (JSON 형식 확인)"}` 와 함께 400 이었습니다.

```ts
type Status = "AVAILABLE" | "ON_LOAN" | "LOST";

const label: Record<Status, string> = {
  AVAILABLE: "대출 가능",
  ON_LOAN: "대출 중",
  LOST: "분실",
};
```

> 위 `EnumJson.java` 를 직접 실행하려면 Jackson 라이브러리가 필요합니다. 저장소 루트에서 `export REPO=$PWD` 를 해 두고, 작업 폴더에서 `mkdir jar && (cd jar && unzip -q "$REPO/labs/backend/week25/build/libs/week25.jar")` 로 이미 빌드된 jar 를 풀어 둔 뒤 `javac -cp "jar/BOOT-INF/lib/*" -d out EnumJson.java && java -cp "out:jar/BOOT-INF/lib/*" EnumJson` 으로 실행합니다. jar 가 없다면 `cd "$REPO/labs/backend" && ./gradlew :week25:bootJar` 로 만듭니다. 선택 사항이며, 출력은 위와 같아야 합니다.

## 7. 어노테이션: 코드가 아니라 표시

어노테이션(`@이름`)은 **그 자체로는 아무것도 실행하지 않습니다.** 클래스·필드·메서드·매개변수 앞에 붙는 표시이고, 그 표시를 읽고 동작하는 쪽은 Spring, JPA, 검증 라이브러리 같은 프레임워크입니다. 아래 `MyAnnotation.java` 는 그 원리를 직접 보여 주는 작은 예입니다. 어노테이션을 직접 정의하고, 표시된 메서드를 찾아 출력합니다.

```java
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;

public class MyAnnotation {
    @Retention(RetentionPolicy.RUNTIME)
    @interface Route {
        String method();
        String path();
    }

    static class BookApi {
        @Route(method = "GET", path = "/books")
        void list() { System.out.println("list 실행"); }

        @Route(method = "POST", path = "/books")
        void create() { System.out.println("create 실행"); }

        void helper() { System.out.println("helper 는 표시가 없어서 라우트가 아님"); }
    }

    public static void main(String[] args) throws Exception {
        BookApi api = new BookApi();
        Method[] methods = BookApi.class.getDeclaredMethods();
        Arrays.sort(methods, Comparator.comparing(Method::getName));
        for (Method m : methods) {
            Route r = m.getAnnotation(Route.class);
            if (r != null) System.out.println(r.method() + " " + r.path() + " -> " + m.getName());
        }
        BookApi.class.getDeclaredMethod("list").invoke(api);
        api.helper();
    }
}
```

```text
POST /books -> create
GET /books -> list
list 실행
helper 는 표시가 없어서 라우트가 아님
```

Spring 이 `@GetMapping("/books")` 로 URL 과 메서드를 연결하는 것도 원리는 이것입니다. 표시를 찾아 URL 표에 올립니다(4주에서 그 표를 실제로 봅니다).

실제 어노테이션이 어떻게 남는지 참조 API 의 `BookRequest` 필드로 확인할 수도 있습니다. 6절 끝의 안내대로 jar 를 풀어 둔 작업 폴더에서 하는 선택 사항입니다.

```java
import java.lang.reflect.Field;

public class ReadAnnotations {
    public static void main(String[] args) throws Exception {
        for (Field f : Class.forName("library.Dto$BookRequest").getDeclaredFields()) {
            System.out.println(f.getName() + " -> " + java.util.Arrays.toString(f.getAnnotations()));
        }
    }
}
```

```bash
javac -cp "jar/BOOT-INF/classes:jar/BOOT-INF/lib/*" -d out ReadAnnotations.java
java -cp "out:jar/BOOT-INF/classes:jar/BOOT-INF/lib/*" ReadAnnotations
```

```text
isbn -> [@jakarta.validation.constraints.NotBlank(message="{jakarta.validation.constraints.NotBlank.message}", payload={}, groups={})]
title -> [@jakarta.validation.constraints.NotBlank(message="{jakarta.validation.constraints.NotBlank.message}", payload={}, groups={})]
author -> [@jakarta.validation.constraints.NotBlank(message="{jakarta.validation.constraints.NotBlank.message}", payload={}, groups={})]
```

읽을 때는 어노테이션을 **다섯 묶음** 으로 나누면 쉽습니다. 참조 API 에 실제로 나오는 것들입니다.

| 묶음 | 예 | 읽는 법 |
|---|---|---|
| 웹 (URL·응답) | `@RestController`, `@RequestMapping("/books")`, `@GetMapping`, `@PostMapping`, `@PathVariable`, `@RequestParam`, `@RequestBody`, `@ExceptionHandler` | 요청이 어느 URL·메서드로 들어와 어떻게 변환되는가. 4주에서 자세히 |
| 검증 | `@Valid`, `@NotBlank`, `@NotNull` | 잘못된 요청이 400 으로 막히는 규칙 |
| DB (JPA) | `@Entity`, `@Id`, `@GeneratedValue`, `@Column(nullable = false, unique = true)`, `@ManyToOne`, `@Table(name = "members")` | 클래스 ↔ 테이블, 필드 ↔ 열. 5주에서 자세히 |
| 구성 요소 | `@Service`, `@Configuration`, `@Bean`, `@SpringBootApplication`, `@Transactional` | Spring 이 만들고 관리하는 객체와 트랜잭션 경계 |
| 문서·테스트 | `@Schema`, `@ApiResponse`, `@Test`, `@BeforeEach` | OpenAPI 스펙에 쓰이는 정보, 테스트 메서드 표시 |

읽기 규칙 몇 가지입니다.

- 괄호 안의 `이름 = 값` 은 속성입니다. `@Column(nullable = false, unique = true)` 는 "비어 있으면 안 되고 중복되면 안 되는 열" 입니다.
- 값이 하나뿐이면 이름을 생략할 수 있습니다. `@RequestMapping("/books")` 는 `@RequestMapping(value = "/books")` 와 같습니다.
- 값이 여러 개인 배열은 중괄호입니다. `types = {"string", "null"}` 은 `["string", "null"]` 과 같습니다.
- 매개변수 앞에 붙은 어노테이션은 그 매개변수가 **어디서 오는지** 를 알려 줍니다. `@PathVariable long id` 는 URL 경로에서, `@RequestParam String keyword` 는 쿼리 문자열에서, `@RequestBody BookRequest req` 는 JSON 본문에서 옵니다.
- `@Schema(requiredMode = REQUIRED)` 같은 문서용 어노테이션은 동작을 바꾸지 않고 **스펙에 나가는 정보** 만 바꿉니다. 그러나 이 정보가 프론트 타입의 `?` 유무를 정하므로 PR 에서 빠지면 중요한 변경입니다(6주).

## 8. null, Optional, 박싱: 값이 없을 수 있음을 읽기

Java 에서 "값이 없음" 은 세 가지 모양으로 나타납니다. 모두 참조 API 에 있습니다.

| 모양 | 예 | TypeScript 에서 | 읽는 법 |
|---|---|---|---|
| `null` 필드 | `Loan.returnedAt` (`// 반납 전에는 null`) | `string \| null` | 주석과 JSON 을 확인 |
| `Optional<T>` 반환 | `Optional<Book> findById(Long id)` | `Book \| undefined` | 호출부가 없을 때를 처리하는지 확인 |
| `null` 매개변수 | `searchBooks(String keyword)` 의 `keyword` | `keyword?: string` | `keyword == null \|\| keyword.isBlank()` 같은 검사를 찾기 |

`LibraryService.java` 에서 그 처리를 봅니다.

```java
    @Transactional(readOnly = true)
    public List<BookResponse> searchBooks(String keyword) {
        List<Book> found = (keyword == null || keyword.isBlank())
                ? books.findAllByOrderByIdAsc()
                : books.findByTitleContainingIgnoreCaseOrderByIdAsc(keyword);
        return found.stream().map(BookResponse::from).toList();
    }

    private Book findBook(long id) {
        return books.findById(id).orElseThrow(() -> new NotFoundException("책을 찾을 수 없습니다: " + id));
    }
```

- `a ? b : c` 는 TypeScript 와 같은 삼항 연산자입니다.
- `findById(id).orElseThrow(() -> new NotFoundException(...))` 는 "있으면 꺼내고, 없으면 이 예외를 던진다" 입니다. `() -> ...` 는 인자 없는 화살표 함수입니다. 아래 TypeScript 와 같은 흐름입니다.

```ts
class NotFoundException extends Error {}

function orElseThrow<T>(value: T | undefined, make: () => Error): T {
  if (value === undefined) throw make();
  return value;
}

const book = orElseThrow(books.findById(99), () => new NotFoundException("책을 찾을 수 없습니다: 99"));
```

- 이 줄이 던지는 `NotFoundException` 이 어떻게 **404 응답** 이 되는지는 4주의 주제입니다. 지금은 "서비스는 상태 코드를 모르고 예외만 던진다" 만 기억하세요.

`long` 과 `Long` 의 차이도 읽을 때마다 걸립니다.

```java
    private Long id;                    // Book.java: 엔티티의 id. 저장 전에는 null
    @Schema(requiredMode = REQUIRED) long id,   // Dto.java: 응답의 id. 항상 값이 있음
```

엔티티의 `Long id` 는 저장 전에 `null` 이어야 해서 대문자이고, 응답 `record` 의 `long id` 는 항상 값이 있어서 소문자입니다. 이것이 JSON 계약에서 `id` 가 "항상 있는 숫자" 인 근거입니다. 위 3단계 g 가 보여 준 것처럼 `null` 인 `Long` 을 `long` 에 넣으면 `NullPointerException` 입니다.

```ts
function describe(loan: LoanResponse): string {
  return loan.returnedAt === null ? "대출 중" : `반납 ${loan.returnedAt}`;
}
```

## 9. List, Map, stream, 람다: 컬렉션 코드 읽기

`LibraryService` 와 `ApiExceptionHandler` 에는 `for` 문 대신 이런 줄이 나옵니다.

```java
        return found.stream().map(BookResponse::from).toList();
```

```java
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .sorted()
                .collect(Collectors.joining(", "));
```

`.stream()` 으로 시작해 `.map(...)`, `.filter(...)`, `.sorted()` 를 이어 붙이고 마지막에 결과를 모으는 방식입니다. JavaScript 의 배열 메서드 체인과 거의 같습니다.

| Java | TypeScript |
|---|---|
| `list.stream().map(f -> ...)` | `list.map((f) => ...)` |
| `.filter(b -> b.id() == id)` | `.filter((b) => b.id === id)` |
| `.findFirst()` (결과가 `Optional`) | `.find(...)` (결과가 `T \| undefined`) |
| `.anyMatch(...)` | `.some(...)` |
| `.sorted()` | `.sort()` |
| `.collect(Collectors.joining(", "))` | `.join(", ")` |
| `BookResponse::from` | `BookResponse.from` (함수 자체를 넘김) |
| `.toList()` | (결과가 수정 불가한 목록) |

`a -> a + 1` 이 화살표 함수, `Type::method` 가 메서드 참조입니다. `->` 는 `switch` 의 각 분기에서도 쓰입니다(3단계의 `case LOST -> "분실"`).

아래는 `ApiExceptionHandler.invalid` 와 같은 구조를 따로 실행해 본 `Stream03.java` 입니다. 제네릭 상자와 제네릭 메서드(11절)도 함께 들어 있습니다.

```java
import java.util.List;
import java.util.stream.Collectors;

public class Stream03 {
    record FieldError(String field, String message) {}

    static class Box<T> {
        private final T value;
        Box(T value) { this.value = value; }
        T get() { return value; }
    }

    static <T> T firstOrNull(List<T> list) {
        return list.isEmpty() ? null : list.get(0);
    }

    public static void main(String[] args) {
        List<FieldError> errors = List.of(
                new FieldError("title", "공백일 수 없습니다"),
                new FieldError("isbn", "공백일 수 없습니다"));

        String message = errors.stream()
                .map(f -> f.field() + ": " + f.message())
                .sorted()
                .collect(Collectors.joining(", "));
        System.out.println(message);

        Box<String> box = new Box<>("TDD");
        String title = box.get();
        System.out.println(title.length());
        System.out.println(firstOrNull(List.of(3, 1, 2)));
        System.out.println(firstOrNull(List.<Integer>of()));
    }
}
```

```text
isbn: 공백일 수 없습니다, title: 공백일 수 없습니다
3
3
null
```

첫 줄의 모양 `필드: 메시지, 필드: 메시지` 는 **400 응답의 `message` 형식** 입니다. 프론트의 `ApiError.fieldErrors` 가 `", "` 로 나누고 `필드: 메시지` 로 해석하는 규칙이 바로 이 Java 코드에서 나옵니다(7주). 같은 처리를 TypeScript 로 쓰면 이렇습니다.

```ts
type FieldError = { field: string; message: string };

const errors: FieldError[] = [
  { field: "title", message: "공백일 수 없습니다" },
  { field: "isbn", message: "공백일 수 없습니다" },
];

const message = errors
  .map((f) => `${f.field}: ${f.message}`)
  .sort()
  .join(", ");
```

`Map<String, Integer>` 는 `Map<string, number>` 입니다. 2단계에서 본 것처럼 없는 키는 `null` 이므로 `stock.get("x")` 뒤에 바로 메서드를 호출하면 `NullPointerException` 이 날 수 있습니다.

## 10. 예외: 던져지는 것과 선언해야 하는 것

참조 API 의 업무 예외는 아주 작습니다. `NotFoundException.java` 전체입니다.

```java
/** 404 로 바뀌는 업무 예외 */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
```

`ConflictException` 도 같은 모양이고, 주석에 "409 로 바뀌는 업무 예외 (isbn 중복, 이미 대출 중, 한도 초과, 이미 반납)" 라고 쓰여 있습니다. TypeScript 의 `class NotFoundException extends Error {}` 와 같습니다.

Java 에는 예외가 두 종류 있어서, 읽을 때 구분해야 합니다.

| 종류 | 대표 | 규칙 |
|---|---|---|
| unchecked (`RuntimeException` 의 자식) | `NotFoundException`, `NullPointerException`, `IllegalArgumentException` | 메서드가 던진다고 선언하지 않아도 됨. 호출부가 안 잡아도 컴파일됨 |
| checked (`Exception` 의 직접 자식) | `Exception`, `IOException` | 메서드 시그니처에 `throws Exception` 을 적어야 하고, 호출부는 `try-catch` 하거나 다시 `throws` 해야 함 |

3단계 j 의 `unreported exception Exception; must be caught or declared to be thrown` 이 checked 예외를 처리하지 않았을 때의 에러입니다. 참조 API 에서는 `SecurityConfig.securityFilterChain(...) throws Exception` 처럼 설정 코드에만 `throws` 가 보이고, 업무 코드는 unchecked 예외만 씁니다. 그래서 `LibraryService` 의 메서드 시그니처에는 `throws` 가 없는데도 `NotFoundException` 을 던질 수 있습니다.

처리되지 않은 unchecked 예외가 끝까지 올라오면 어떻게 되는지 직접 봅니다. `Uncaught03.java` 를 실행하세요. 컴파일 에러 없이 실행되고, 실행하면 터집니다.

```java
public class Uncaught03 {
    static class NotFoundException extends RuntimeException {
        NotFoundException(String message) { super(message); }
    }

    static String find(long id) {
        throw new NotFoundException("책을 찾을 수 없습니다: " + id);
    }

    public static void main(String[] args) {
        System.out.println(find(99));
    }
}
```

```text
Exception in thread "main" Uncaught03$NotFoundException: 책을 찾을 수 없습니다: 99
	at Uncaught03.find(Uncaught03.java:7)
	at Uncaught03.main(Uncaught03.java:11)
```

웹 서버에서는 이 상황이 **500 응답** 이 됩니다. 참조 API 가 `NotFoundException` 을 404 로 바꾸는 장치(`@RestControllerAdvice`)를 4주에서 확인하고, 그 장치를 일부러 빼면 500 이 되는 것도 직접 봅니다.

## 11. 제네릭: `<>` 읽기

꺾쇠 안은 "무엇의" 입니다. 참조 API 에서 마주치는 것들입니다.

| 코드 | 읽는 법 |
|---|---|
| `List<BookResponse>` | `BookResponse` 의 목록 = `BookResponse[]` |
| `Optional<Book>` | 있을 수도 없을 수도 있는 `Book` |
| `ResponseEntity<BookResponse>` | 본문이 `BookResponse` 인 응답(상태 코드·헤더 포함) |
| `Repository<Book, Long>` | `Book` 엔티티를 다루고 키가 `Long` 인 저장소 |
| `static <T> T assignId(T entity, List<T> all)` | 어떤 타입 `T` 든 받는 함수. 들어온 `T` 와 나가는 `T` 가 같음 |
| `new ArrayList<>()` | 오른쪽의 `<>` 는 왼쪽 선언에서 추론 (다이아몬드) |

`Stream03.java` 의 `Box<T>` 와 `firstOrNull` 이 위 TypeScript 제네릭과 같은 역할임을 확인했습니다. TypeScript 로 쓰면 이렇습니다.

```ts
function firstOrNull<T>(list: T[]): T | null {
  return list.length === 0 ? null : list[0];
}
```

차이는 두 가지만 알면 읽는 데 충분합니다.

- Java 의 제네릭은 컴파일이 끝나면 지워집니다(타입 소거). 그래서 실행 중에는 `List<String>` 과 `List<Integer>` 를 구분할 수 없습니다. TypeScript 의 타입이 컴파일 후 사라지는 것과 비슷하다고 생각하면 됩니다. 읽는 데는 영향이 없습니다.
- `? extends Number` 같은 와일드카드 표기를 만나면 "`Number` 또는 그 자식 타입의 목록" 이라고만 읽으세요. 직접 쓸 일은 없습니다.

## 12. 테스트 코드 읽기: 동작 설명서

백엔드 PR 에서 가장 읽기 쉬운 코드는 테스트입니다. 테스트 이름과 단언(assert)이 "이 코드가 무엇을 약속하는가" 를 문장처럼 보여 줍니다. `LibraryServiceTest.java` 의 일부입니다.

```java
    @BeforeEach
    void setUp() {
        books = new FakeBooks();
        members = new FakeMembers();
        loans = new FakeLoans();
        service = new LibraryService(books, members, loans, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void lend_fourthBook_conflictsAndSavesNothing() {
        long member = newMember("하승");
        for (int i = 1; i <= LibraryService.LOAN_LIMIT; i++) service.lend(member, newBook("b" + i));
        long fourth = newBook("b4");
        assertThrows(ConflictException.class, () -> service.lend(member, fourth));
        assertEquals(LibraryService.LOAN_LIMIT, loans.all.size(), "한도 초과 대출은 저장되면 안 된다");
    }
```

| Java 테스트 | Vitest 에서 비슷한 것 |
|---|---|
| `@Test` | `it("...", () => { ... })` |
| `@BeforeEach` | `beforeEach(() => { ... })` |
| `assertEquals(기대값, 실제값)` | `expect(실제값).toBe(기대값)` (**기대값이 앞**이라는 점이 다름) |
| `assertThrows(ConflictException.class, () -> ...)` | `expect(() => ...).toThrow(ConflictException)` |
| `assertTrue(...)`, `assertNull(...)` | `expect(...).toBe(true)`, `expect(...).toBeNull()` |
| `Clock.fixed(NOW, ...)` 를 `new` 로 넣음 | 시간을 `vi.setSystemTime` 으로 고정하는 것과 같은 목적 |

위 테스트는 "한 사람이 3권(`LOAN_LIMIT`)까지 빌리고, 4번째는 `ConflictException` 이며, 그때 아무것도 저장되지 않는다" 를 말합니다. 업무 규칙이 코드로 적힌 문서입니다. PR 이 이런 규칙을 바꾸면 이 테스트도 같이 바뀌어야 하고, **테스트가 바뀌지 않은 PR 은 의심할 이유가 됩니다**(4주).

테스트 이름의 `lend_fourthBook_conflictsAndSavesNothing` 은 "메서드_상황_결과" 순서의 관례입니다. HTTP 단위 테스트(`LibraryApiTest`)에서는 `createBook_duplicateIsbn_409` 처럼 상태 코드가 이름에 들어 있어 계약을 읽기에 좋습니다.

## 13. 자주 걸리는 문법 사전

읽다가 막히는 기호들입니다.

| 보이는 것 | 뜻 |
|---|---|
| `@Override` | 부모/인터페이스의 메서드를 재정의한다는 표시 |
| `public` / `private` / `protected` / (없음) | 접근 범위. 없음은 같은 패키지만. `ApiExceptionHandler` 의 메서드가 `ResponseEntity<ApiError> notFound(...)` 처럼 `public` 이 없는 이유 |
| `static` | 객체 없이 클래스 이름으로 호출 (`BookResponse.from(...)`) |
| `final` | 한 번 대입하면 변경 불가. `private final LibraryService service;` 는 생성자에서 한 번만 채움 |
| `new X(...)` | 객체 생성. `new` 가 보이는 곳이 값이 만들어지는 곳 |
| `this.x = x;` | 필드 `x` 에 매개변수 `x` 를 대입 (생성자에서 흔함) |
| `import static ...` | 클래스 이름 없이 메서드를 바로 쓰겠다는 선언 (`assertEquals(...)`) |
| `var` | 타입 추론. 오른쪽 식으로 타입이 정해짐 |
| `int i = 1; i++` | 정수 증가. `for (int i = 1; i <= N; i++)` 는 흔한 반복문 |
| `for (Book b : books)` | `for (const b of books)` |
| `/** ... */` | 문서 주석. 의도를 설명하므로 먼저 읽을 것 |
| `// ponytail: ...` | 알려진 한계를 적어 둔 주석. `LibraryService.lend` 안에서 동시 요청 한계를 설명 |

## 14. 한 필드가 DB 에서 화면까지 가는 길

`returnedAt` 하나를 끝까지 따라가 봅니다. 4주에서 같은 방식으로 요청 전체를 따라갑니다.

| 층 | 파일 | 코드 | 값의 모양 |
|---|---|---|---|
| 엔티티 | `Loan.java` | `private Instant returnedAt; // 반납 전에는 null` | `Instant` 또는 `null` |
| 응답 record | `Dto.java` | `... types = {"string", "null"} ... Instant returnedAt` | 같은 값 |
| 서비스 | `LibraryService.java` | `loan.markReturned(clock.instant());` | 반납하면 현재 시각 |
| JSON | 응답 본문 | `"returnedAt":null` 또는 `"returnedAt":"2026-10-22T13:46:04.419152Z"` | 문자열 또는 `null` |
| 생성 타입 | `schema.d.ts` | `returnedAt: string \| null;` | TypeScript |
| 화면 | 프론트 | `loan.returnedAt === null ? "대출 중" : ...` | 분기 |

위 JSON 두 값은 참조 API 를 직접 호출해 얻은 실제 응답입니다. 한 층에서 이름·타입·`null` 가능성이 바뀌면 아래 층이 모두 영향을 받습니다. **PR 을 읽는다는 것은 이 길의 어느 층이 바뀌었는지 찾는 일** 입니다.

더 깊이 알고 싶다면 옛 Java 커리큘럼의 [인터페이스·enum·record](../../archive/java-24weeks/week07-interface-enum-record.md), [클래스](../../archive/java-24weeks/week05-classes.md), [제네릭과 Optional](../../archive/java-24weeks/week12-generics-optional.md) 을 참고하세요. 그 문서들은 코드를 직접 쓰는 관점입니다.

## 연습문제

1. `../../labs/backend/week25/src/main/java/library/Member.java` 를 1절의 순서 (패키지 → 어노테이션 → 선언 → 필드 → 생성자 → 메서드)로 읽고, 이 엔티티를 TypeScript `class` 로 옮기세요. `@Table(name = "members")` 위의 주석은 무슨 말인가요? 응답용 `MemberResponse` 와 무엇이 다른가요?
2. `LibraryService.lend` 의 앞부분 네 가지 실패 조건(`orElseThrow` 두 곳, `if` 두 곳)을 위에서부터 순서대로 적고, 각각 어떤 예외를 던지는지 쓰세요. 순서를 바꾸면 클라이언트가 받는 오류가 어떻게 달라질 수 있나요?
3. `LibraryServiceTest.FakeLoans.countByMemberIdAndReturnedAtIsNull` 이 하는 일을 TypeScript 한 줄(배열 메서드 체인)로 쓰세요.
4. `Predict03.java` 의 `find` 를 `null` 을 돌려주는 방식으로 바꾸면 호출부는 어떻게 달라져야 하나요? `Optional` 이 호출부에 강제하는 것은 무엇인가요?
5. `EnumJson.java` 의 `Status` 에 값 `RESERVED` 를 추가하는 PR 이 왔다고 가정하세요. 서버, 스펙, 프론트 타입, 프론트 화면에서 각각 무엇이 바뀌거나 바뀌지 않나요?
6. (선택) 같은 폴더의 다른 파일 하나(`LoanController.java` 또는 `AuthController.java`)를 골라 클래스 위 어노테이션, 메서드마다의 URL, 매개변수의 출처(`@PathVariable`/`@RequestBody`/기타)만 표로 만드세요. 4주의 준비입니다.

<details>
<summary>정답 예시</summary>

1. `Member` 는 `id: number | null`, `name: string` 을 가진 클래스입니다. 주석은 "MEMBER 는 SQL 예약어와 겹칠 수 있어 복수형으로" 이고, 테이블 이름을 `members` 로 정한 이유입니다. `MemberResponse` 는 `id`(항상 값 있음, `long`)와 `name` 만 가진 API 모양이고, 엔티티는 DB 모양이라 `id` 가 `Long`(null 가능)입니다.
2. 순서는 회원 조회(없으면 `NotFoundException`) → 책 조회(없으면 `NotFoundException`) → 책이 이미 대출 중인지 확인(`ConflictException`) → 회원의 대출 건수가 `LOAN_LIMIT` 이상인지 확인(`ConflictException`) 입니다. 없는 회원과 이미 대출 중인 책을 함께 보내면 먼저 검사하는 쪽의 오류(404)만 받습니다. 검사 순서가 곧 응답 우선순위입니다.
3. `loans.filter((l) => l.memberId === memberId && l.returnedAt === null).length` 입니다.
4. 호출부가 `null` 검사를 잊어도 컴파일되므로 `NullPointerException` 이 런타임에 납니다. `Optional` 은 "없을 수 있음" 을 반환 타입에 드러내서, 호출부가 `map`/`orElse`/`orElseThrow` 로 처리하도록 유도합니다. TypeScript 에서 `strictNullChecks` 가 `T | undefined` 를 처리하게 만드는 것과 같습니다.
5. 서버는 `RESERVED` 를 JSON 으로 내보낼 수 있게 되고, 스펙의 `enum` 목록에 값이 늘며, 생성 타입의 유니언에 `"RESERVED"` 가 추가됩니다. 프론트 화면은 `Record<Status, string>` 같은 완전한 매핑이 있으면 `tsc` 가 빠진 곳을 알려 주고, 문자열을 직접 비교하는 `if` 체인이라면 컴파일러가 알려 주지 못해 런타임에야 문제가 드러납니다.
6. 표의 예: `LoanController` 는 `@RestController` + `@RequestMapping("/loans")`, `lend` 는 `@PostMapping` 에 `@Valid @RequestBody LoanRequest`, `giveBack` 은 `@PostMapping("/{id}/return")` 에 `@PathVariable long id` 입니다.

</details>

## 퀴즈

1. `record` 의 필드 `title` 값을 읽는 코드는 `b.getTitle()` 인가 `b.title()` 인가? 이유는?
2. `Optional<Book>` 을 반환하는 메서드와 `Book` 을 반환하면서 `null` 일 수 있는 메서드의 차이는?
3. 어노테이션 `@NotBlank` 를 붙이면 코드가 직접 검사를 하는가?
4. `Long a = 1000L, b = 1000L;` 에서 `a == b` 의 결과는? 어떻게 비교해야 하는가?
5. `Instant` 타입 필드는 JSON 에서 어떤 모양으로 나가고, TypeScript 에서는 어떤 타입인가?
6. `NotFoundException extends RuntimeException` 인 예외를 메서드가 던질 때 시그니처에 `throws` 를 적어야 하는가?
7. 새 `enum` 값을 하나 추가하는 PR 은 프론트에 왜 영향을 주는가?

<details>
<summary>정답</summary>

1. `b.title()`. record 의 접근자는 `get` 접두사가 없다. 3단계의 `cannot find symbol` 이 그 증거다
2. `Optional` 은 "없을 수 있음" 이 타입에 드러나서 호출부가 처리하도록 유도한다. `null` 반환은 호출부가 잊어도 컴파일되고 런타임에 `NullPointerException` 이 난다
3. 아니다. 어노테이션은 표시일 뿐이다. 검증 라이브러리가 표시를 읽고, `@Valid` 가 있는 곳에서 검사를 시작한다(4주)
4. `false`. 큰 값은 서로 다른 객체이기 때문이다. `a.equals(b)` 로 비교한다(`true`)
5. ISO 8601 문자열(`"2026-10-22T13:46:04.203082Z"`)이고, TypeScript 에서는 `string` 이다
6. 아니다. `RuntimeException` 의 자식(unchecked)은 선언하지 않아도 된다. checked 예외(`Exception`)는 선언하거나 잡아야 한다
7. JSON 으로 나가는 값의 집합이 늘기 때문이다. 스펙과 생성된 유니언 타입이 바뀌고, 모든 값을 처리하도록 써 둔 프론트 코드는 컴파일 단계에서 빠진 곳이 드러난다

</details>

## 체크리스트

- [ ] 1단계의 6개 질문에 먼저 답을 적었고, `schema.d.ts` 와 비교해 틀린 이유를 적었다
- [ ] 2단계의 출력 10줄을 예측했고, ★가 붙은 줄의 이유를 한 문장으로 적었다
- [ ] 3단계의 고장 10가지를 컴파일 에러와 런타임 예외로 구분해서 말할 수 있다
- [ ] 메서드 시그니처 한 줄만 보고 받는 것, 돌려주는 것, 없을 수 있는지를 말할 수 있다
- [ ] `record`, `class`, `interface`, `enum` 을 보고 각각 TypeScript 의 무엇에 대응하는지 말할 수 있다
- [ ] 어노테이션이 코드를 실행하는 것이 아니라 표시라는 것과, 다섯 묶음 중 어디에 속하는지 구분할 수 있다
- [ ] `long`/`Long`, `Optional`, `null` 이 JSON 과 프론트 타입의 `?`/`null` 에 어떻게 이어지는지 설명할 수 있다
- [ ] `returnedAt` 처럼 필드 하나가 엔티티에서 화면까지 가는 길을 표로 그릴 수 있다
