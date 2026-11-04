# 4주차. Spring 코드 읽기와 백엔드 PR 읽는 법

**예상 시간:** 8~11시간 (하루 1시간 세션 10회) | **목표:** Spring 컨트롤러·서비스·리포지토리 코드를 읽고, 어노테이션이 URL·상태 코드·응답 JSON 으로 이어지는 길을 추적한다. 백엔드 PR 의 diff 를 보고 (1) 어떤 요청·응답이 바뀌는지 (2) 프론트 타입에 미치는 영향 (3) 확인할 질문 목록을 뽑아낸다.

> 3주에서 `record`, 어노테이션, `Optional`, 예외를 읽는 법을 익혔습니다. 이번 주는 그 코드가 **HTTP 요청 하나** 를 어떻게 처리하는지 따라가고, 마지막에 PR diff 를 읽는 절차를 연습합니다. 1주(HTTP, curl)와 2주(REST 설계, JSON 계약)의 내용을 전제로 합니다.
>
> 준비물: JDK 21, `curl`, `jq`, `rsync`, `patch`(macOS 기본 설치), Node 22.12+ 와 pnpm 10+(PR 읽기 연습의 타입 확인 단계에서만). 읽기 자료는 참조 API `../labs/backend/week25` 의 소스입니다. 이 API 는 백엔드 팀이 준 API 이므로 **원본은 고치지 않고**, 코드를 바꿔 보는 실험은 모두 복사본에서 합니다.
>
> 명령은 별도 언급이 없으면 저장소 루트에서 실행합니다. 서버는 `cd labs/backend && ./gradlew :week25:bootRun` 으로 8080 에 띄우고 `Ctrl+C` 로 끕니다. 처음 실행할 때 라이브러리를 내려받아 시간이 걸립니다. 이 문서에서 "서버가 실제로 돌려준 값" 이라고 쓴 출력은 모두 이 서버를 직접 호출해 얻은 것이고, 시각(`loanedAt`, `timestamp`)은 실행할 때마다 다릅니다.

## 하루 1시간으로 나누면

이 주차는 하루 1시간씩 10번의 세션으로 나눕니다. 한 세션은 50~70분이고, 개념 절은 절 번호 단위로, 9절은 PR 단위로 세션에 배정했습니다.

| 세션 | 할 일 | 끝났을 때 할 수 있어야 하는 것 |
|---|---|---|
| 1 | 1단계(요청 19개의 응답 예측하기 → 서버를 켜고 → 비교하기, 40분)를 하고, 1절(요청 하나가 지나가는 길)을 읽는다 | 요청 19개의 응답을 예측과 비교하고, 요청 한 건이 컨트롤러 → 서비스 → 리포지토리로 지나가는 길을 말할 수 있다 |
| 2 | 2단계(어노테이션을 일부러 망가뜨리기, 1시간)를 한다. 고장 8가지 중 최소 4가지를 복사본에서 하고 원복한다 | 어노테이션을 망가뜨렸을 때 응답이 어떻게 바뀌는지 설명할 수 있다 |
| 3 | 2절(컨트롤러)을 읽고 3단계(요청 한 건을 소스에서 직접 추적하기, 40분)를 한다 | `@GetMapping`, `@PathVariable`, `@RequestBody`, `@Valid` 가 HTTP 요청의 어느 부분을 정하는지 말하고, 요청 한 건을 컨트롤러 → 서비스 → 리포지토리 소스에서 추적할 수 있다 |
| 4 | 3절(서비스와 리포지토리: 규칙은 서비스에, 호출은 DI 로)을 읽는다. 소절 "의존성 주입 (DI)" 과 "리포지토리는 인터페이스뿐이다" 를 포함한다 (압축 코스에서는 건너뜀) | 규칙이 서비스에 있고 리포지토리는 인터페이스만 있다는 것, 생성자로 주입받는다는 것이 무슨 뜻인지 말할 수 있다 |
| 5 | 4절(검증과 예외 처리), 5절(응답 JSON 은 어떻게 만들어지나), 6절(보안 설정 읽기), 7절(API 지도 만들기)을 읽고, 8절(백엔드 PR 읽는 법)의 1~3단계(변경된 파일을 층으로 나눈다, 테스트 diff 부터 읽는다, 계약 층 diff 로 변화표를 만든다)를 읽는다 | 오류가 `{"message"}` 가 되기까지의 길과 어느 URL 이 로그인을 요구하는지를 소스에서 짚고, PR diff 를 계약·규칙·저장·설정·테스트 층으로 나누어 요청·응답 변화표를 만들 수 있다 |
| 6 | 8절의 4단계(프론트 타입 영향을 확인한다)와 5단계(질문 목록을 만든다)를 한다. 복사본을 8081 로 띄우는 단계가 있으므로 서버가 8080 에 켜져 있으면 먼저 끈다 | 복사본에 diff 를 적용해 스펙과 생성 타입의 차이를 확인하고, 작성자에게 물을 질문 목록을 범주별로 만들 수 있다 |
| 7 | 9절(연습: 가상의 PR 세 개 읽기)의 PR 1("대출 기한 추가")을 한다. 산출물 세 가지를 먼저 쓰고 풀이와 비교한다 | PR 1 의 요청·응답 변화, 프론트 타입에 미치는 영향, 작성자에게 물을 질문을 서면으로 정리할 수 있다 |
| 8 | PR 2("검색 파라미터 이름 정리와 isbn 형식 제한")와 PR 3("이미 반납된 대출을 다시 반납해도 오류 없이 처리")을 한다. PR 마다 산출물 세 가지를 먼저 쓰고 풀이와 비교한다 | PR 2 와 PR 3 에 대해서도 요청·응답 변화, 프론트 타입에 미치는 영향, 작성자에게 물을 질문을 서면으로 정리할 수 있다 |
| 9 | 연습문제와 퀴즈를 푼다 (압축 코스에서는 건너뜀) | 19개 요청 중 `message` 가 없는 응답이 어느 것인지와 `MemberController.create`, `LoanController.giveBack` 의 URL·로그인 필요 여부·성공 코드·실패 코드를 소스만 보고 표로 만들 수 있다 |
| 10 | 체크리스트의 각 항목을 내 말로 설명해 보고, 막히는 항목은 해당 절을 다시 읽은 뒤 학습 노트를 정리한다 | 백엔드 PR 의 diff 를 보고 어떤 요청·응답이 바뀌는지, 프론트 타입에 미치는 영향이 무엇인지, 작성자에게 물을 질문이 무엇인지 서면으로 정리할 수 있다 |

세션을 시작하기 전에 이전 세션의 마지막 결과를 1~2분 훑어봅니다. 막히면 아래 해당 개념 절을 사전처럼 펼쳐 보세요.

## 🖐 먼저 손으로 (읽기 전에 하세요)

> 이 주차의 핵심은 아래 단계를 **직접 해 보면서** 깨닫는 것입니다. 아래쪽 설명 문서는 먼저 읽지 않아도 됩니다. 막혔을 때 사전처럼 펼쳐 보세요.

### 1단계. 요청 19개의 응답 예측하기 → 서버를 켜고 → 비교하기 (40분)

서버를 켜기 **전에**, 아래 요청을 위에서 아래로 차례로 보냈을 때 각각의 **상태 코드** 와 **본문의 모양** 을 노트에 적으세요. 서버를 새로 켠 직후 순서대로 한 번만 보낸다고 가정합니다. 2주의 REST 규칙과 3주에서 읽은 코드(`Dto.java`, 예외 클래스 이름)로 추측합니다. 소스는 아직 열지 않아도 됩니다.

| # | 요청 | 내 예측 (상태 코드 / 본문) |
|---|------|------|
| 1 | `GET /books` | |
| 2 | `GET /books/abc` | |
| 3 | `GET /books/99` | |
| 4 | `POST /books` (로그인하지 않고, 올바른 본문) | |
| 5 | 로그인 후 `POST /books` (올바른 본문). `Location` 헤더 값은? | |
| 6 | 같은 `isbn` 으로 `POST /books` 한 번 더 | |
| 7 | `POST /books` 에서 `author` 를 빼고 보냄 | |
| 8 | `POST /books` 본문이 `{` 하나뿐 | |
| 9 | `PUT /books/1` | |
| 10 | `POST /books` 를 `Content-Type: text/plain` 으로 | |
| 11 | `POST /members` 로 `{"name":"하승"}` | |
| 12 | `POST /loans` 로 없는 회원(99)과 책 1 | |
| 13 | `POST /loans` 로 회원 1, 책 1 (정상) | |
| 14 | 13번을 똑같이 한 번 더 | |w
| 15 | `GET /members/1/loans` (로그인 쿠키 없이) | |
| 16 | `POST /loans/1/return` | |
| 17 | 16번을 한 번 더 | |
| 18 | `POST /loans/abc/return` | |
| 19 | `POST /books` 에 빈 `isbn` 을 `Accept-Language: en` 으로 | |

서버를 켭니다. 8080 이 비어 있어야 합니다.

```bash
lsof -i :8080                                  # 아무것도 안 나와야 함
cd labs/backend && ./gradlew :week25:bootRun   # 한 터미널에서 (Ctrl+C 로 종료)
```

다른 터미널에서, 쿠키를 저장할 작업 폴더를 정해 아래를 위에서부터 **한 번만** 실행합니다. (`mkdir -p ~/scratch/week04 && cd ~/scratch/week04` 후 실행. 로그인 쿠키가 `cj.txt` 에 저장됩니다.)

```bash
B=localhost:8080
H='Content-Type: application/json'
c() { printf '%s -> ' "$1"; shift; curl -s -w ' [%{http_code}]\n' "$@"; }
c 1  $B/books
c 2  $B/books/abc
c 3  $B/books/99
c 4  -X POST -H "$H" -d '{"isbn":"111","title":"이펙티브 자바","author":"조슈아 블로크"}' $B/books
curl -s -c cj.txt -H "$H" -d '{"username":"demo","password":"demo1234"}' $B/auth/login; echo
echo '5 ->'; curl -s -i -b cj.txt -H "$H" -d '{"isbn":"111","title":"이펙티브 자바","author":"조슈아 블로크"}' $B/books | grep -iE '^HTTP|^Location'
c 6  -b cj.txt -H "$H" -d '{"isbn":"111","title":"다른 책","author":"누군가"}' $B/books
c 7  -b cj.txt -H "$H" -d '{"isbn":"222","title":"Clean Code"}' $B/books
c 8  -b cj.txt -H "$H" -d '{' $B/books
c 9  -b cj.txt -X PUT -H "$H" -d '{}' $B/books/1
c 10 -b cj.txt -H 'Content-Type: text/plain' -d 'hi' $B/books
c 11 -b cj.txt -H "$H" -d '{"name":"하승"}' $B/members
c 12 -b cj.txt -H "$H" -d '{"memberId":99,"bookId":1}' $B/loans
c 13 -b cj.txt -H "$H" -d '{"memberId":1,"bookId":1}' $B/loans
c 14 -b cj.txt -H "$H" -d '{"memberId":1,"bookId":1}' $B/loans
c 15 $B/members/1/loans
c 16 -b cj.txt -X POST $B/loans/1/return
c 17 -b cj.txt -X POST $B/loans/1/return
c 18 -b cj.txt -X POST $B/loans/abc/return
c 19 -b cj.txt -H 'Accept-Language: en' -H "$H" -d '{"isbn":"","title":"x","author":"y"}' $B/books
```

<details>
<summary>정답 (예측을 적은 뒤에 여세요)</summary>

서버가 실제로 돌려준 출력입니다. (`timestamp`, `loanedAt`, `returnedAt` 의 값은 실행할 때마다 다릅니다.)

```text
1 -> [] [200]
2 -> {"message":"id: 형식이 올바르지 않습니다"} [400]
3 -> {"message":"책을 찾을 수 없습니다: 99"} [404]
4 -> {"message":"로그인이 필요합니다"} [401]
{"username":"demo"}
5 ->
HTTP/1.1 201 
Location: /books/1
6 -> {"message":"이미 등록된 isbn 입니다: 111"} [409]
7 -> {"message":"author: 공백일 수 없습니다"} [400]
8 -> {"message":"요청 본문을 읽을 수 없습니다 (JSON 형식 확인)"} [400]
9 -> {"timestamp":"2026-10-22T14:16:39.815+00:00","status":405,"error":"Method Not Allowed","path":"/books/1"} [405]
10 -> {"timestamp":"2026-10-22T14:16:39.855+00:00","status":415,"error":"Unsupported Media Type","path":"/books"} [415]
11 -> {"id":1,"name":"하승"} [201]
12 -> {"message":"회원을 찾을 수 없습니다: 99"} [404]
13 -> {"id":1,"book":{"id":1,"isbn":"111","title":"이펙티브 자바","author":"조슈아 블로크"},"member":{"id":1,"name":"하승"},"loanedAt":"2026-10-22T14:16:39.956902Z","returnedAt":null} [201]
14 -> {"message":"이미 대출 중인 책입니다: 1"} [409]
15 -> [{"id":1,"book":{"id":1,"isbn":"111","title":"이펙티브 자바","author":"조슈아 블로크"},"member":{"id":1,"name":"하승"},"loanedAt":"2026-10-22T14:16:39.956902Z","returnedAt":null}] [200]
16 -> {"id":1,"book":{"id":1,"isbn":"111","title":"이펙티브 자바","author":"조슈아 블로크"},"member":{"id":1,"name":"하승"},"loanedAt":"2026-10-22T14:16:39.956902Z","returnedAt":"2026-10-22T14:16:40.295594Z"} [200]
17 -> {"message":"이미 반납된 대출입니다: 1"} [409]
18 -> {"message":"id: 형식이 올바르지 않습니다"} [400]
19 -> {"message":"isbn: must not be blank"} [400]
```

- 2번, 18번: 경로의 `abc` 를 `long` 으로 바꾸지 못해서 컨트롤러 메서드에 들어가기 전에 400 이 됩니다. 메시지는 `필드: 설명` 형식입니다.
- 4번: 읽기(GET)는 로그인 없이 되고, 쓰기(POST)는 로그인이 필요합니다. 15번이 쿠키 없이 200 인 것도 같은 이유입니다(`SecurityConfig`, 아래 6절).
- 9번, 10번: 응답이 `{"message": ...}` 가 아닙니다. 우리 코드가 처리하지 않는 오류(지원하지 않는 메서드 405, 지원하지 않는 `Content-Type` 415)는 Spring 기본 오류 JSON(`timestamp`, `status`, `error`, `path`)으로 나갑니다. **프론트가 "오류 본문은 항상 `message`" 라고 가정하면 안 되는 이유** 입니다. 프론트의 `ApiError` 가 `message` 가 없는 본문에 대비해 기본 문구를 두는 것도 이 때문입니다.
- 12번과 14번: 존재 확인(404)은 12번에서, 규칙 검사(409)는 14번에서 나옵니다. 서비스의 검사 순서(회원 → 책 → 대출 중 → 한도)를 따릅니다(3단계 6번).
- 16번: 같은 대출 객체에 `returnedAt` 만 채워져 돌아옵니다. 17번은 한 번 더 하면 409 입니다.
- 18번: `POST /loans/{id}/return` 의 스펙에는 400 이 선언돼 있지 않은데(`@ApiResponse` 를 적지 않았기 때문) 실제로는 400 이 나옵니다. **스펙에 없는 응답이 실제로는 올 수 있습니다.**
- 19번: 같은 오류인데 `Accept-Language: en` 이면 영어 문장입니다(`must not be blank`). 한국어 문장은 서버가 요청의 언어 헤더에 따라 고르는 것이므로, 프론트가 문구를 비교하는 코드를 쓰면 깨집니다. `필드: ` 형식만 믿으세요.

</details>

### 2단계. 어노테이션을 일부러 망가뜨리기 (1시간)

참조 API 를 직접 고치지 않도록 **복사본** 을 만듭니다. 복사본의 서버는 8080 이 아니라 8081 에서 띄웁니다. 8080 의 서버가 켜져 있으면 먼저 `Ctrl+C` 로 끕니다.

```bash
rsync -a --exclude build --exclude .gradle labs/backend/ ~/scratch/backend-copy/
cd ~/scratch/backend-copy
./gradlew :week25:bootRun --args='--server.port=8081'      # 한 터미널에서
```

아래 표의 고장을 **하나씩** 적용합니다. 순서는 (1) 먼저 어떻게 될지 예측해 적고 (2) 복사본의 파일을 고치고 (3) 서버를 다시 켜서 표의 요청을 보내고 (4) 원본에서 파일을 다시 복사해 원복하는 것입니다. 고치는 파일은 모두 `~/scratch/backend-copy/week25/src/main/java/library/` 아래에 있습니다. 원복 예: `cp labs/backend/week25/src/main/java/library/BookController.java ~/scratch/backend-copy/week25/src/main/java/library/`.

| # | 파일과 고치는 곳 | 보낼 요청 | 어떻게 될까요? |
|---|---|---|---|
| a | `BookController.create` 의 `@Valid` 삭제 (`@RequestBody` 는 남김) | 로그인 후 `POST /books` 로 `{"isbn":"","title":"x","author":"y"}` | |
| b | `ApiExceptionHandler` 의 `@RestControllerAdvice` 한 줄 삭제 | `GET /books/99`, 그리고 로그인 후 빈 `isbn` 으로 `POST /books` | |
| c | `BookController` 맨 위(`create` 위)의 `@PostMapping` 을 `@PutMapping` 으로 | 로그인 후 `POST /books` | |
| d | `BookController.get` 의 `@PathVariable long id` 에서 `@PathVariable ` 삭제 | `GET /books/1` | |
| e | `BookController` 의 `@RequestMapping("/books")` 를 `"/book"` 으로 | `GET /books` | |
| f | `BookController.create` 의 `@RequestBody ` 삭제 (`@Valid` 는 남김) | 로그인 후 올바른 JSON 본문으로 `POST /books` | |
| g | `BookController` 위의 `@RestController` 를 `@org.springframework.stereotype.Controller` 로 | `GET /books`, `GET /books/1` | |
| h | `LibraryService` 의 `@Transactional` 모두 삭제 (클래스 위 1곳 + 메서드 3곳) | 대출을 만든 뒤 `POST /loans/1/return` | |

<details>
<summary>실제 결과 (예측을 적은 뒤에 여세요)</summary>

위와 같은 변경을 가한 복사본으로 확인한 결과입니다. 앞에서 정상 동작하던 요청은 그대로 정상이었고, 표의 요청만 달라졌습니다.

| # | 결과 |
|---|---|
| a | `201 {"id":2,"isbn":"","title":"x","author":"y"}`. **빈 `isbn` 이 그대로 저장됩니다.** `@NotBlank` 는 `@Valid` 가 있어야 검사됩니다. DB 의 `nullable = false` 는 `null` 만 막고 빈 문자열은 막지 못합니다 |
| b | `GET /books/99` 는 `{"timestamp":...,"status":500,"error":"Internal Server Error","path":"/books/99"} [500]`, 빈 `isbn` 은 `400` 이지만 본문이 `{"timestamp":...,"status":400,"error":"Bad Request","path":"/books"}`. 서버 로그에는 `library.NotFoundException: 책을 찾을 수 없습니다: 99` 가 ERROR 로 남습니다. **업무 예외가 404 가 되는 것은 이 한 클래스 덕분** 이고, `{"message"}` 형식도 여기서 만들어집니다 |
| c | `405 {"timestamp":...,"status":405,"error":"Method Not Allowed","path":"/books"}`. URL 은 맞는데 메서드가 달라서 막힙니다. `GET /books` 는 여전히 200 입니다 |
| d | `500`. 서버 로그: `IllegalStateException: Optional long parameter 'id' is present but cannot be translated into a null value due to being declared as a primitive type.` 어노테이션이 없는 단순 타입 매개변수는 경로가 아니라 쿼리 파라미터(`?id=`)로 해석되는데 값이 없습니다. `/books/abc` 도 400 이 아니라 500 입니다 |
| e | `404 {"timestamp":...,"status":404,"error":"Not Found","path":"/books"}`. **URL 경로가 곧 `@RequestMapping` 문자열** 이므로 이 한 줄이 바뀌면 프론트의 모든 `/books` 호출이 404 가 됩니다 |
| f | `400 {"message":"author: 공백일 수 없습니다, isbn: 공백일 수 없습니다, title: 공백일 수 없습니다"}`. 올바른 JSON 본문을 보냈는데도 본문을 읽지 않으니 모든 필드가 비어 있다고 검증됩니다 |
| g | `GET /books/1` 은 `404`, `GET /books` 는 `500` (로그: `Circular view path [books]: would dispatch back to the current handler URL [/books] again`). `@Controller` 에서는 반환값이 JSON 본문이 아니라 **화면 이름** 으로 해석됩니다. 이 변경 후에도 `POST /books` 는 `ResponseEntity` 를 돌려줘서 정상 201 이라 증상이 뒤섞여 보입니다 |
| h | `500`. 서버 로그: `org.hibernate.LazyInitializationException: Could not initialize proxy [library.Book#1] - no session`. 트랜잭션이 없으면 응답을 만드는 중에 책 정보를 DB 에서 가져올 수 없습니다(5주) |

</details>

### 3단계. 요청 한 건을 소스에서 직접 추적하기 (빈칸 채우기, 40분)

`POST /loans` 한 건이 지나가는 곳을 아래 표의 빈칸에 채우세요. 모든 답은 `../labs/backend/week25/src/main/java/library/` 의 소스에 있습니다. 파일 이름과 줄을 찾아 적는 연습입니다.

| 단계 | 질문 | 내 답 (파일과 코드) |
|---|---|---|
| 1 | 이 요청을 받는 메서드는 어느 클래스의 어느 메서드인가? URL 은 어떤 두 어노테이션이 합쳐져 만들어지는가? | |
| 2 | 로그인이 필요한가? 어디에 그렇게 적혀 있는가? | |
| 3 | 본문 JSON 은 어떤 타입으로 바뀌는가? 필수 필드는? | |
| 4 | 본문이 `{"memberId":1}` 이면 어떤 예외가 나고, 누가 어떤 상태 코드로 바꾸는가? | |
| 5 | 컨트롤러는 업무 규칙을 어디에 맡기는가? | |
| 6 | 실패할 수 있는 조건 4가지와 각각의 예외는? | |
| 7 | 성공하면 무엇을 저장하는가? 저장 시각은 어디서 오는가? | |
| 8 | 성공 응답의 상태 코드, 헤더, 본문은 어디서 정해지는가? | |

<details>
<summary>정답</summary>

1. `LoanController.lend`. 클래스 위의 `@RequestMapping("/loans")` 와 메서드 위의 `@PostMapping` 이 합쳐져 `POST /loans` 입니다.
2. 필요합니다. `SecurityConfig` 의 `.requestMatchers(HttpMethod.POST, "/books", "/members", "/loans", "/loans/*/return").authenticated()`.
3. `@RequestBody LoanRequest` 입니다. `record LoanRequest(@NotNull Long memberId, @NotNull Long bookId)` 로 두 필드 모두 필수입니다.
4. 컨트롤러 매개변수의 `@Valid` 가 검사해서 `MethodArgumentNotValidException` 이 나고, `ApiExceptionHandler.invalid` 가 `bookId: 널이어서는 안됩니다` 같은 메시지와 함께 400 으로 바꿉니다. 서버가 실제로 돌려준 값은 `{"message":"bookId: 널이어서는 안됩니다"} [400]` 입니다.
5. `LibraryService.lend(memberId, bookId)` 입니다. 컨트롤러는 HTTP 변환만 합니다.
6. 회원 없음(`NotFoundException`, 404), 책 없음(`NotFoundException`, 404), 이미 대출 중인 책(`ConflictException`, 409), 회원이 이미 3권 대출 중(`ConflictException`, 409). 한도 3은 `LOAN_LIMIT` 상수입니다.
7. `loans.save(new Loan(book, member, clock.instant()))` 로 `Loan` 한 행을 저장합니다. 시각은 주입받은 `Clock` 에서 옵니다.
8. 컨트롤러의 `ResponseEntity.created(URI.create("/loans/" + loan.id())).body(loan)` 입니다. 상태 코드는 201, `Location: /loans/{id}` 헤더, 본문은 `LoanResponse` 입니다.

</details>

---

## 1. 요청 하나가 지나가는 길

`POST /loans` 요청이 들어오면 서버 안에서 일어나는 일입니다. 위에서 아래로 읽으세요.

```text
브라우저 / curl
   |  POST /loans   Cookie: JSESSIONID=...   {"memberId":1,"bookId":1}
   v
보안 필터 (SecurityConfig)       로그인했는가? CORS 허용 origin 인가?      실패 -> 401 / CORS 오류
   v
URL 표 (HandlerMapping)          POST /loans  ->  LoanController.lend
   v
인자 변환                         JSON 본문 -> LoanRequest, @Valid 로 검증      실패 -> 400
   v
LoanController.lend              HTTP 를 자바 호출로 바꾸기만 함
   v
LibraryService.lend              업무 규칙, @Transactional (트랜잭션 시작)
   v
*Repository                      Spring Data 가 만든 구현이 SQL 실행 (5주)
   v
LoanResponse (record)            엔티티 -> API 모양
   v
JSON 변환 (Jackson)               record 필드 이름 -> JSON 키                  성공 -> 201 + Location
```

어느 단계에서든 예외가 던져지면 `@RestControllerAdvice` 클래스(`ApiExceptionHandler`)가 받아 상태 코드와 `{"message": ...}` 로 바꿉니다. 이 길에서 프론트가 보는 것은 **상태 코드, 헤더, 본문 JSON** 셋뿐이고, 이 셋을 정하는 코드가 컨트롤러·`Dto`·예외 처리기입니다. PR 을 읽을 때 이 세 곳이 바뀌었는지가 첫 번째 질문입니다.

**TS 와 비교** (비유이므로 완전히 같지는 않습니다)

| Spring / Java 쪽 | 프론트 쪽에서 비슷한 것 |
|---|---|
| `@RestController` 클래스의 `@GetMapping`/`@PostMapping` 메서드 | Next 의 Route Handler(`app/api/books/route.ts` 가 `GET`, `POST` 함수를 내보내는 것) |
| `@RequestMapping("/books")` + `@GetMapping("/{id}")` | 폴더 `app/api/books/[id]/route.ts` 의 `GET` |
| 지원하지 않는 메서드는 405 | Route Handler 도 지원하지 않는 메서드에는 405 |
| `@PathVariable long id` | `params.id` 를 `Number()` 로 바꾼 값(실패하면 400) |
| `@RequestParam String keyword` | `searchParams.get("keyword")` |
| `@RequestBody BookRequest` | `await request.json()` |
| `@Valid` + `@NotBlank` | zod 스키마의 `.min(1)` 과 `safeParse` |
| `ResponseEntity.created(uri).body(x)` | `Response.json(x, { status: 201, headers: { Location: ... } })` |
| `@RestControllerAdvice` + `@ExceptionHandler` | 에러를 한 곳에서 응답으로 바꾸는 전역 핸들러. 화면용 `error.tsx` 가 아니라 API 응답을 만드는 쪽 |
| `@Service` | 업무 규칙을 모은 모듈 |
| `SecurityFilterChain` | Next 16 의 `proxy.ts`(이전 이름 `middleware.ts`)처럼 요청이 핸들러에 닿기 전에 지나가는 층 |
| 생성자 주입, `@Bean` | 의존하는 것을 인자로 받는 함수. `Provider` 에 넣고 `useContext` 로 받는 것과 비슷한 발상 |
| `application.properties` | `.env` 와 설정 파일 |

> Route Handler 와 `proxy.ts` 이름은 이 저장소의 Next 16 문서(`labs/frontend/node_modules/next/dist/docs`)에서 확인한 것입니다.

## 2. 컨트롤러: 어노테이션이 URL·상태 코드를 정한다

`BookController.java` 전체입니다.

```java
@RestController
@RequestMapping("/books")
public class BookController {
    private final LibraryService service;

    public BookController(LibraryService service) {
        this.service = service;
    }

    @PostMapping
    @ApiResponse(responseCode = "201", description = "등록됨. Location 헤더에 /books/{id}")
    @ApiResponse(responseCode = "400", description = "검증 실패", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "401", description = "로그인 필요", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "409", description = "isbn 중복", content = @Content(schema = @Schema(implementation = ApiError.class)))
    public ResponseEntity<BookResponse> create(@Valid @RequestBody BookRequest req) {
        BookResponse book = service.createBook(req);
        return ResponseEntity.created(URI.create("/books/" + book.id())).body(book);
    }

    @GetMapping
    @ApiResponse(responseCode = "200", description = "keyword 가 있으면 제목에 포함된 책만 (대소문자 무시)")
    public List<BookResponse> search(@RequestParam(required = false) String keyword) {
        return service.searchBooks(keyword);
    }

    @GetMapping("/{id}")
    @ApiResponse(responseCode = "200", description = "책")
    @ApiResponse(responseCode = "400", description = "id 가 숫자가 아님", content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "404", description = "책 없음", content = @Content(schema = @Schema(implementation = ApiError.class)))
    public BookResponse get(@PathVariable long id) {
        return service.getBook(id);
    }
}
```

어노테이션이 HTTP 의 어느 부분을 정하는지 정리하면 이렇습니다.

| 보이는 것 | 정하는 것 | 이 파일에서 |
|---|---|---|
| `@RestController` | 이 클래스의 반환값이 **화면 이름이 아니라 응답 본문(JSON)** | 2단계 g 에서 지워 본 것 |
| `@RequestMapping("/books")` + `@GetMapping("/{id}")` | URL 경로와 HTTP 메서드. 클래스 + 메서드가 합쳐진다 | `GET /books/{id}` |
| `@PathVariable long id` | 경로의 `{id}` 를 `long` 으로. 숫자가 아니면 메서드에 들어오기 전에 400 | 2번 요청 |
| `@RequestParam(required = false) String keyword` | 쿼리 `?keyword=`. `required = false` 라서 없어도 되고, 없으면 `null` | 프론트 타입의 `keyword?: string` |
| `@RequestBody BookRequest` | JSON 본문을 `record` 로 | 2단계 f |
| `@Valid` | `record` 안의 `@NotBlank` 같은 규칙을 **검사하라** 는 지시 | 2단계 a |
| 반환 타입 `List<BookResponse>`/`BookResponse` | 그대로 JSON, 상태 코드는 **200** | `search`, `get` |
| 반환 타입 `ResponseEntity<...>` + `ResponseEntity.created(uri)` | 상태 코드 **201** 과 `Location` 헤더를 직접 정함 | `create` |
| `@ApiResponse(...)` | **문서(스펙)에만** 쓰이는 선언. 실제 상태 코드를 바꾸지 않음 | 18번 요청의 400 이 스펙에 없었던 이유 |

마지막 줄이 특히 중요합니다. `@ApiResponse(responseCode = "409", ...)` 를 지워도 서버는 여전히 409 를 내고, 반대로 이것을 적었다고 409 가 나오는 것이 아닙니다. 409 는 서비스가 던진 `ConflictException` 에서 옵니다. 그래서 스펙의 오류 코드 목록은 **사람이 적어 둔 약속** 이고, 코드와 어긋날 수 있습니다(6주).

## 3. 서비스와 리포지토리: 규칙은 서비스에, 호출은 DI 로

업무 규칙은 모두 서비스에 있다고 `LibraryService` 위 주석이 말합니다. `lend` 입니다.

```java
    public LoanResponse lend(long memberId, long bookId) {
        Member member = members.findById(memberId)
                .orElseThrow(() -> new NotFoundException("회원을 찾을 수 없습니다: " + memberId));
        Book book = findBook(bookId);
        if (loans.existsByBookIdAndReturnedAtIsNull(bookId)) {
            throw new ConflictException("이미 대출 중인 책입니다: " + bookId);
        }
        if (loans.countByMemberIdAndReturnedAtIsNull(memberId) >= LOAN_LIMIT) {
            throw new ConflictException("한 사람은 " + LOAN_LIMIT + "권까지 빌릴 수 있습니다");
        }
        // ponytail: 같은 책·같은 회원의 동시 요청은 검사와 저장 사이에서 둘 다 통과할 수 있다.
        // 동시성이 문제가 되면 책·회원 행에 비관적 락(@Lock)을 걸거나 DB 부분 유니크 인덱스를 둔다.
        return LoanResponse.from(loans.save(new Loan(book, member, clock.instant())));
    }
```

- 서비스는 HTTP 를 모릅니다. 상태 코드 숫자도, `ResponseEntity` 도 없고 `NotFoundException`/`ConflictException` 만 던집니다. 숫자로의 번역은 4절의 처리기가 합니다.
- 검사 순서가 곧 **응답 우선순위** 입니다. 12번 요청에서 없는 회원(404)이 이미 대출 중인 책(409)보다 먼저 걸리는 이유입니다.
- 주석의 `ponytail:` 은 알려진 한계를 적어 둔 것입니다. 이 코드는 동시에 두 요청이 오면 둘 다 통과할 수 있다고 스스로 밝히고 있습니다. 프론트에서 "대출" 버튼을 연타하는 시나리오가 이 한계에 닿습니다.

### 의존성 주입 (DI)

컨트롤러와 서비스 생성자를 보면 필요한 것을 **스스로 만들지 않고 받습니다.**

```java
    public BookController(LibraryService service) {
        this.service = service;
    }
```

```java
    public LibraryService(BookRepository books, MemberRepository members, LoanRepository loans, Clock clock) {
```

누가 이것들을 넘겨줄까요? 앱이 시작될 때 Spring 이 `@Service`, `@RestController` 같은 표시가 붙은 클래스를 찾아 객체를 하나씩 만들고, 생성자의 매개변수 타입을 보고 알맞은 객체를 넣어 줍니다. `Clock` 은 `LibraryApplication` 의 `@Bean Clock clock()` 이 만든 것입니다. 이 구조의 이점은 3주에서 본 테스트입니다. 서비스가 `new LibraryService(가짜들, 고정 시계)` 로 만들어지므로 DB 도 서버도 없이 규칙만 검사할 수 있습니다.

Spring 이 "표시가 붙은 클래스를 찾아 생성자 인자를 채워 객체를 만든다" 는 것을 직접 보려면 아래 작은 코드를 실행하세요(`Predict21.java`로 저장). 실제 Spring 이 아니라 원리를 50줄로 흉내 낸 것입니다. 실행하기 전에 "생성" 이 찍히는 **순서** 와 마지막 세 줄을 예측하세요.

```java
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

public class Predict21 {
    @Retention(RetentionPolicy.RUNTIME)
    @interface Component {}

    @Component
    static class Engine {
        Engine() { System.out.println("Engine 생성"); }
    }

    @Component
    static class Car {
        final Engine engine;
        Car(Engine engine) {
            this.engine = engine;
            System.out.println("Car 생성");
        }
    }

    @Component
    static class Driver {
        final Car car;
        final Engine engine;
        Driver(Car car, Engine engine) {
            this.car = car;
            this.engine = engine;
            System.out.println("Driver 생성");
        }
    }

    static final Map<Class<?>, Object> beans = new HashMap<>();

    static <T> T get(Class<T> type) {
        try {
            if (beans.containsKey(type)) return type.cast(beans.get(type));
            if (!type.isAnnotationPresent(Component.class)) {
                throw new IllegalStateException("빈이 아닙니다: " + type.getSimpleName());
            }
            Constructor<?> ctor = type.getDeclaredConstructors()[0];
            Class<?>[] types = ctor.getParameterTypes();
            Object[] args = new Object[types.length];
            for (int i = 0; i < types.length; i++) args[i] = get(types[i]);
            Object bean = ctor.newInstance(args);
            beans.put(type, bean);
            return type.cast(bean);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        Driver d = get(Driver.class);
        System.out.println(d.car.engine == d.engine);
        System.out.println(get(Car.class) == d.car);
        try {
            get(String.class);
        } catch (IllegalStateException e) {
            System.out.println(e.getMessage());
        }
    }
}
```

```text
Engine 생성
Car 생성
Driver 생성
true
true
빈이 아닙니다: String
```

- `Driver` 를 달라고 했는데 `Engine` 이 **먼저** 만들어집니다. 의존하는 것부터 만들어 넣어 주기 때문입니다.
- `Engine` 은 한 번만 만들어져 `Car` 와 `Driver` 가 **같은 객체** 를 공유합니다(`true`). Spring 의 기본인 싱글톤 빈이고, 그래서 서비스에 요청마다 바뀌는 상태를 필드로 두면 안 됩니다.
- 표시가 없는 클래스(`String`)는 컨테이너가 모릅니다. Spring 이 "그 타입의 빈이 없다" 고 시작하지 못하는 이유와 같습니다.

더 알고 싶다면 [Spring IoC/DI](../archive/java-24weeks/week21-spring-ioc-di.md) 를 참고하세요. 그 문서는 직접 만드는 관점입니다.

### 리포지토리는 인터페이스뿐이다

3주에서 본 `BookRepository` 는 본문이 없는 인터페이스였습니다. 실제로 누가 구현할까요? 실행 중인 앱에 물어본 결과입니다. 아래 `Peek.java` 는 앱을 띄운 뒤 빈 몇 개와 **URL 표** 를 출력합니다. 직접 실행하려면 3주 6절 끝의 안내대로 `week25.jar` 를 푼 작업 폴더(`jar/` 가 있는 곳)에서 합니다. 포트(8081)를 다른 프로그램이 쓰고 있지 않아야 하고, 선택 사항입니다.

```java
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.TreeMap;
import library.BookRepository;
import library.LibraryApplication;
import library.LibraryService;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

public class Peek {
    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(LibraryApplication.class,
                "--server.port=" + args[0], "--spring.main.banner-mode=off", "--logging.level.root=ERROR");

        BookRepository repo = ctx.getBean(BookRepository.class);
        System.out.println("BookRepository 는 프록시인가: " + Proxy.isProxyClass(repo.getClass()));
        System.out.println("LibraryService 의 실제 클래스: " + ctx.getBean(LibraryService.class).getClass().getName());

        Map<String, String> routes = new TreeMap<>();
        RequestMappingHandlerMapping mapping = ctx.getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class);
        for (Map.Entry<RequestMappingInfo, HandlerMethod> e : mapping.getHandlerMethods().entrySet()) {
            HandlerMethod m = e.getValue();
            if (m.getBeanType().getPackageName().equals("library")) {
                routes.put(e.getKey().getPathPatternsCondition().getPatterns() + " " + e.getKey().getMethodsCondition().getMethods(),
                        m.getBeanType().getSimpleName() + "." + m.getMethod().getName());
            }
        }
        routes.forEach((k, v) -> System.out.println(k + " -> " + v));
        ctx.close();
    }
}
```

```bash
javac -parameters -cp "jar/BOOT-INF/classes:jar/BOOT-INF/lib/*" -d out Peek.java
java -cp "out:jar/BOOT-INF/classes:jar/BOOT-INF/lib/*" Peek 8081
```

```text
BookRepository 는 프록시인가: true
LibraryService 의 실제 클래스: library.LibraryService$$SpringCGLIB$$0
[/auth/login] [POST] -> AuthController.login
[/auth/logout] [POST] -> AuthController.logout
[/auth/me] [GET] -> AuthController.me
[/books/{id}] [GET] -> BookController.get
[/books] [GET] -> BookController.search
[/books] [POST] -> BookController.create
[/loans/{id}/return] [POST] -> LoanController.giveBack
[/loans] [POST] -> LoanController.lend
[/members/{id}/loans] [GET] -> MemberController.activeLoans
[/members] [POST] -> MemberController.create
```

- `BookRepository` 의 실제 객체는 Spring 이 실행 중에 만든 **프록시** 입니다. 메서드 이름(`findByTitleContainingIgnoreCaseOrderByIdAsc`)을 읽어 SQL 을 만들어 실행합니다(5주).
- `LibraryService` 의 실제 클래스 이름 끝의 `$$SpringCGLIB$$0` 은 Spring 이 `@Transactional` 을 적용하려고 서비스를 한 겹 감싼 흔적입니다. 2단계 h 에서 `@Transactional` 을 지우면 이 감싸기가 사라지고 `LazyInitializationException` 이 났습니다.
- 아래쪽 URL 표가 **이 앱의 모든 입구** 입니다. 10개이고, 스펙(`/v3/api-docs`)의 경로와 같습니다. `curl -s localhost:8080/v3/api-docs | jq -r '.paths | keys[]'` 로도 경로 목록을 볼 수 있습니다. 컨트롤러 파일을 하나하나 열지 않고 "이 앱에 어떤 입구가 있는가" 를 알아내는 가장 빠른 방법입니다. PR 이 입구를 추가하거나 지우면 이 표가 달라집니다.

## 4. 검증과 예외 처리: 오류가 `{"message"}` 가 되기까지

`@Valid` 가 붙은 `@RequestBody` 는 컨트롤러 메서드에 **들어오기 전에** 검사됩니다. 실패하면 `MethodArgumentNotValidException` 이 던져지고, 아래 처리기가 받습니다. `ApiExceptionHandler.java` 의 일부입니다.

```java
/**
 * 모든 오류를 {"message": "..."} 로 바꾸는 단 한 곳.
 * 보안 필터의 401(로그인 안 함)도 SecurityConfig 가 이 클래스로 넘긴다.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<ApiError> notFound(NotFoundException e) {
        return error(HttpStatus.NOT_FOUND, e.getMessage());
    }
```

```java
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> invalid(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .sorted()
                .collect(Collectors.joining(", "));
        return error(HttpStatus.BAD_REQUEST, message);
    }
```

`@ExceptionHandler(예외클래스)` 가 붙은 메서드가 "이 예외가 던져지면 이렇게 응답하라" 는 규칙이고, `@RestControllerAdvice` 는 이 규칙들을 **모든 컨트롤러에 적용** 합니다. 이 클래스의 모든 규칙을 표로 정리하면 프론트의 오류 처리 표(7주)가 거의 그대로 나옵니다.

| 던져지는 예외 | 상태 | `message` | 어디서 던지는가 |
|---|---|---|---|
| `NotFoundException` | 404 | 던질 때 적은 문장 (`책을 찾을 수 없습니다: 99`) | `LibraryService` |
| `ConflictException` | 409 | 던질 때 적은 문장 (`이미 대출 중인 책입니다: 1`) | `LibraryService` |
| `DataIntegrityViolationException` | 409 | `이미 존재하는 데이터와 충돌합니다` | DB 제약 위반(동시 요청이 isbn 검사를 함께 통과한 경우) |
| `MethodArgumentNotValidException` | 400 | `필드: 설명, 필드: 설명` (필드 이름순) | `@Valid` 검증 실패 |
| `HttpMessageNotReadableException` | 400 | `요청 본문을 읽을 수 없습니다 (JSON 형식 확인)` | 깨진 JSON, 형식이 안 맞는 값 |
| `MethodArgumentTypeMismatchException` | 400 | `id: 형식이 올바르지 않습니다` | 경로·쿼리 값의 타입 변환 실패 |
| `BadCredentialsException` | 401 | `아이디 또는 비밀번호가 올바르지 않습니다` | 로그인 실패 |
| `AuthenticationException` | 401 | `로그인이 필요합니다` | 로그인 없이 보호된 URL 요청 |

이 표에 **없는** 것이 중요합니다. 405(메서드 불일치), 415(`Content-Type` 불일치), 처리기에 등록되지 않은 예외로 인한 500 은 `{"message": ...}` 가 아니라 Spring 기본 오류 JSON 으로 나갑니다(1단계 9번, 10번, 2단계 b). 프론트는 이 두 모양을 모두 받을 준비를 해야 합니다.

## 5. 응답 JSON 은 어떻게 만들어지나

- 컨트롤러가 돌려준 `record` 를 JSON 라이브러리(Jackson)가 JSON 으로 바꿉니다. **필드 이름이 키** 입니다. `Instant` 는 ISO 8601 문자열이 됩니다.
- `null` 인 필드는 키가 사라지지 않고 `null` 로 나갑니다. 13번 요청의 `"returnedAt":null` 이 그 예이고, 프론트 타입의 `string | null` 이 이 약속에 기대고 있습니다.
- `ResponseEntity.created(...)` 는 `Location` 헤더를 붙입니다(5번 요청). 본문이 없는 응답은 `ResponseEntity.noContent().build()` 로 204 를 만듭니다(`AuthController.logout`).
- 에러 본문 `ApiError` 도 `record ApiError(@Schema(requiredMode = REQUIRED) String message)` 로 같은 방식입니다.

## 6. 보안 설정 읽기: 어느 URL 이 로그인을 요구하는가

`SecurityConfig.java` 의 핵심 세 줄입니다.

```java
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/books", "/members", "/loans", "/loans/*/return").authenticated()
                        .requestMatchers("/auth/me").authenticated()
                        .anyRequest().permitAll())
```

- 읽기: 위에서부터 맞는 첫 규칙이 적용됩니다. 쓰기(POST) 네 개와 `/auth/me` 는 로그인이 필요하고, **나머지 모든 요청은 로그인 없이 허용** (`anyRequest().permitAll()`)입니다. 그래서 1단계 15번의 `GET /members/1/loans` 가 쿠키 없이 200 이었습니다.
- PR 읽기에서의 의미: **새 쓰기 엔드포인트를 추가하면서 이 목록에 넣지 않으면 로그인 없이 호출됩니다.** 컨트롤러에 `@PostMapping` 이 새로 보이면 `SecurityConfig` 도 같이 바뀌었는지 확인하세요. 새 읽기 엔드포인트가 개인정보를 돌려준다면 마찬가지입니다.
- 세션 쿠키 로그인, CORS(`cors.setAllowedOrigins(List.of("http://localhost:3000"))`), CSRF 가 꺼져 있는 이유는 8주의 주제입니다. 지금은 URL 별 로그인 요구가 이 파일에 있다는 것만 기억하세요.

## 7. API 지도 만들기

위 내용을 합치면 이 API 의 지도가 나옵니다. 컨트롤러 코드와 스펙(`jq` 로 뽑은 응답 코드 목록)을 대조해 만든 표입니다.

| 엔드포인트 | 로그인 | 요청 | 스펙에 선언된 응답 코드 | 컨트롤러 → 서비스 |
|---|---|---|---|---|
| `POST /auth/login` | - | 본문 `LoginRequest` | 200 400 401 | `AuthController.login` |
| `POST /auth/logout` | - | - | 204 | `AuthController.logout` |
| `GET /auth/me` | 필요 | - | 200 401 | `AuthController.me` |
| `GET /books` | - | 쿼리 `keyword?` | 200 | `BookController.search` → `searchBooks` |
| `GET /books/{id}` | - | 경로 `id` | 200 400 404 | `BookController.get` → `getBook` |
| `POST /books` | 필요 | 본문 `BookRequest` | 201 400 401 409 | `BookController.create` → `createBook` |
| `POST /members` | 필요 | 본문 `MemberRequest` | 201 400 401 | `MemberController.create` → `createMember` |
| `GET /members/{id}/loans` | - | 경로 `id` | 200 404 | `MemberController.activeLoans` → `activeLoans` |
| `POST /loans` | 필요 | 본문 `LoanRequest` | 201 400 401 404 409 | `LoanController.lend` → `lend` |
| `POST /loans/{id}/return` | 필요 | 경로 `id` | 200 401 404 409 | `LoanController.giveBack` → `giveBack` |

스펙의 응답 코드 목록은 `jq -r '.paths | to_entries[] | .key as $p | .value | to_entries[] | "\(.key|ascii_upcase) \($p) -> \(.value.responses | keys | join(","))"' labs/backend/week25/openapi.json | sort` 로 뽑을 수 있습니다. 이 표를 PR 을 읽을 때 옆에 펼쳐 두면 "이 PR 이 어느 칸을 바꾸는가" 를 바로 찾을 수 있습니다.

---

## 8. 백엔드 PR 읽는 법

PR 을 받으면 코드를 위에서부터 읽지 말고, 아래 다섯 단계로 **프론트 관점의 세 가지 산출물** 을 만듭니다.

- 산출물 1: **요청·응답 변화표** (경로, 메서드, 파라미터, 본문 필드, 성공·실패 코드, 헤더·쿠키)
- 산출물 2: **프론트 타입 영향** (생성 타입이 어떻게 바뀌는가, 어느 파일이 깨지는가, 타입에 안 나타나는 변화)
- 산출물 3: **확인할 질문 목록** (PR 작성자에게 물을 것)

### 1단계. 변경된 파일을 층으로 나눈다

diff 의 파일 목록을 보고 아래 표대로 분류합니다. 어느 층이 바뀌었는지가 읽을 순서와 중요도를 정합니다.

| 층 | 파일 | 바뀌면 프론트에 | 읽는 방법 |
|---|---|---|---|
| 계약 | `*Controller`, `Dto.java`, `ApiExceptionHandler`, `SecurityConfig` | **직접 영향.** URL, 필드, 상태 코드, 로그인 요구 | 산출물 1 을 여기서 만든다 |
| 규칙 | `*Service` | 응답 모양은 그대로여도 **언제 어떤 코드가 나가는가** 가 바뀜 | 조건문, 던지는 예외가 바뀌었는지 |
| 저장 | `Book`/`Loan`/`Member` (엔티티), `*Repository`, 마이그레이션 | 응답엔 안 보이지만 데이터·성능이 바뀜 (5주) | 열 추가·제약·쿼리 횟수 |
| 설정 | `application.properties`, `build.gradle.kts` | 쿠키, 시간대, 의존성 변경 | 값이 바뀐 줄 |
| 테스트 | `src/test/...` | 바뀐 동작을 **문장으로** 보여 줌 | 가장 먼저 읽기 |
| 스펙 파일 | `openapi.json` | 프론트 타입 생성의 입력 | 코드 변경과 일치하는지 |

### 2단계. 테스트 diff 부터 읽는다

테스트는 작성자가 "이 PR 이 무엇을 약속하는가" 를 가장 명확하게 적은 곳입니다. 테스트 이름과 단언을 읽으세요(3주 12절). 그리고 두 가지를 확인합니다.

- **코드는 바뀌었는데 테스트 diff 가 없다.** 이 경우 아래 연습의 PR 들처럼 기존 테스트가 깨지거나(PR 이 CI 를 못 통과), 반대로 새 동작이 아무 테스트로도 보호되지 않습니다.
- **테스트가 약속을 바꿨다.** `assertThrows`, 상태 코드, `jsonPath` 단언이 바뀐 줄은 곧 계약 변경입니다.

### 3단계. 계약 층 diff 로 변화표를 만든다

계약 층 파일의 변경 줄마다 아래를 확인해 변화표를 채웁니다.

| 질문 | 어디를 보나 |
|---|---|
| URL·메서드가 바뀌었나, 추가·삭제되었나 | `@RequestMapping`, `@GetMapping` 등의 문자열 |
| 파라미터가 추가·삭제·이름 변경·필수 여부 변경되었나 | `@RequestParam`, `@PathVariable`, 요청 `record` 의 필드와 `@NotBlank`/`@NotNull` |
| 응답 필드가 추가·삭제·이름 변경·null 허용 변경되었나 | 응답 `record` 의 필드, `@Schema(requiredMode = ...)` |
| 새 검증 규칙이 생겼나 (`@Pattern`, `@Size`) | 요청 `record` 의 어노테이션 — 기존에 통과하던 값이 400 이 될 수 있음 |
| 상태 코드가 바뀌거나 새 오류가 생겼나 | `ResponseEntity` 의 상태, `@ExceptionHandler`, 서비스의 `throw` |
| 로그인 요구가 바뀌었나 | `SecurityConfig` 의 `requestMatchers` |

### 4단계. 프론트 타입 영향을 **확인한다**

diff 를 읽고 예측한 뒤, 실제로 스펙과 타입을 만들어 확인합니다. 읽기만으로는 놓치는 것이 있기 때문입니다. 아래는 PR diff 를 `~/scratch/pra.diff` 로 저장했다는 가정의 절차입니다. 이 문서 2단계에서 만든 복사본 `~/scratch/backend-copy` 를 씁니다(원본은 고치지 않습니다). 2단계에서 고친 곳이 남아 있으면 PR 의 효과와 섞이므로, 먼저 복사본이 원본과 같은지 확인하세요. 아무것도 출력되지 않아야 합니다.

```bash
diff -r labs/backend/week25/src ~/scratch/backend-copy/week25/src
```

```bash
# 1) 복사본에 diff 적용. 서버가 켜져 있으면 먼저 끈다
cd ~/scratch/backend-copy/week25 && patch -p1 < ~/scratch/pra.diff

# 2) 복사본을 8081 로 띄운다 (한 터미널에서)
cd ~/scratch/backend-copy && ./gradlew :week25:bootRun --args='--server.port=8081'

# 3) 다른 터미널, 저장소 루트에서: 커밋된 스펙과 새 스펙의 차이 (servers 항목은 포트가 달라 제외)
diff <(jq -S 'del(.servers)' labs/backend/week25/openapi.json) <(curl -s localhost:8081/v3/api-docs | jq -S 'del(.servers)')

# 4) 생성 타입의 차이. 출력을 저장소 밖에 저장하므로 원본 schema.d.ts 는 그대로
cd labs/frontend
pnpm exec openapi-typescript http://localhost:8081/v3/api-docs -o ~/scratch/schema.new.d.ts
diff src/api/schema.d.ts ~/scratch/schema.new.d.ts
```

호출하는 코드가 얼마나 깨지는지(타입 검사)까지 보려면 프론트 복사본에서 합니다.

```bash
rsync -a --exclude node_modules --exclude .next --exclude test-results labs/frontend/ ~/scratch/frontend-copy/
cd ~/scratch/frontend-copy && pnpm install --frozen-lockfile
pnpm exec openapi-typescript http://localhost:8081/v3/api-docs -o src/api/schema.d.ts
pnpm typecheck && pnpm test
```

다른 PR 을 이어서 연습할 때는 복사본에서 `cd ~/scratch/backend-copy/week25 && patch -R -p1 < ~/scratch/pra.diff` 로 되돌린 뒤 다음 diff 를 적용하세요.

`pnpm test` 가 통과해도 안심하면 안 됩니다. 이 저장소의 테스트는 MSW 가짜 서버를 쓰고 타입을 검사하지 않으므로, 스펙이 바뀌어도 통과합니다(`pnpm typecheck` 가 따로 필요합니다).

### 5단계. 질문 목록을 만든다

변화표와 타입 영향에서 아래 범주별로 질문을 뽑습니다. 질문에는 **PR 의 줄 번호나 파일** 을 함께 적으세요.

| 범주 | 질문 틀 |
|---|---|
| 호환성 | 기존 클라이언트가 깨지는가? 필드 이름 변경·필수 추가·형식 제한은 프론트와 배포 순서를 맞춰야 하는가? 옛 이름을 한동안 같이 받을 수 있는가? |
| 오류 계약 | 새로 생기는 실패는 어떤 상태 코드와 `message` 인가? `{"message"}` 형식인가? 스펙(`@ApiResponse`)에 반영되었는가? |
| 보안 | 새 엔드포인트가 로그인을 요구하는가? 응답에 개인정보(이메일 등)가 새로 실리는가? |
| 데이터 | 열이 추가되면 기존 데이터는? 필수 열이면 마이그레이션이 있는가? |
| 성능 | 목록이 커져도 괜찮은가(페이지네이션)? 쿼리 수가 늘 수 있는 변경인가? (5주) |
| 테스트 | 바뀐 동작을 보호하는 테스트가 있는가? 스펙 파일(`openapi.json`)은 갱신되었는가? |

## 9. 연습: 가상의 PR 세 개 읽기

아래 세 diff 는 **실제 PR 이 아니라, 참조 API 의 소스에 대고 이 문서를 위해 만든 가상의 diff** 입니다. 각 diff 를 실제로 복사본에 적용해서 서버를 띄우고, 스펙 차이와 프론트 타입 검사 결과, 백엔드 테스트 결과를 확인한 값이 풀이에 적혀 있습니다. 각각 8절의 세 산출물(변화표, 프론트 타입 영향, 질문 목록)을 먼저 노트에 쓰고, 풀이와 비교하세요. diff 본문을 파일로 저장해 `patch -p1` 로 적용해 볼 수 있습니다(8절 4단계).

### PR 1. "대출 기한 추가"

```diff
--- a/src/main/java/library/Loan.java
+++ b/src/main/java/library/Loan.java
@@ -1,6 +1,7 @@
 package library;
 
 import jakarta.persistence.*;
+import java.time.Duration;
 import java.time.Instant;
 
 /** 외래 키(book_id, member_id)를 가진 쪽이 관계의 주인이다. */
@@ -19,6 +20,9 @@
     @Column(nullable = false)
     private Instant loanedAt;
 
+    @Column(nullable = false)
+    private Instant dueAt;
+
     private Instant returnedAt; // 반납 전에는 null
 
     protected Loan() {}
@@ -27,6 +31,7 @@
         this.book = book;
         this.member = member;
         this.loanedAt = loanedAt;
+        this.dueAt = loanedAt.plus(Duration.ofDays(14));
     }
 
     void markReturned(Instant at) {
@@ -37,5 +42,6 @@
     public Book getBook() { return book; }
     public Member getMember() { return member; }
     public Instant getLoanedAt() { return loanedAt; }
+    public Instant getDueAt() { return dueAt; }
     public Instant getReturnedAt() { return returnedAt; }
 }
--- a/src/main/java/library/Dto.java
+++ b/src/main/java/library/Dto.java
@@ -45,11 +45,12 @@
             @Schema(requiredMode = REQUIRED) BookResponse book,
             @Schema(requiredMode = REQUIRED) MemberResponse member,
             @Schema(requiredMode = REQUIRED) Instant loanedAt,
+            @Schema(requiredMode = REQUIRED) Instant dueAt,
             @Schema(requiredMode = REQUIRED, types = {"string", "null"}, format = "date-time",
                     description = "반납 전에는 null") Instant returnedAt) {
         static LoanResponse from(Loan l) {
             return new LoanResponse(l.getId(), BookResponse.from(l.getBook()), MemberResponse.from(l.getMember()),
-                    l.getLoanedAt(), l.getReturnedAt());
+                    l.getLoanedAt(), l.getDueAt(), l.getReturnedAt());
         }
     }
 
```

<details>
<summary>풀이 (노트에 쓴 뒤에 여세요)</summary>

**산출물 1. 변화표**

| 항목 | 변화 |
|---|---|
| 요청 | 변화 없음 |
| 응답 | `LoanResponse` 에 필수 필드 `dueAt` (날짜 시각 문자열) 추가. `LoanResponse` 를 쓰는 세 곳 — `POST /loans`(201), `POST /loans/{id}/return`(200), `GET /members/{id}/loans`(200) — 의 본문에 나타남 |
| 상태 코드·헤더 | 변화 없음 |
| 저장 | `loan` 테이블에 `due_at timestamp(6) with time zone not null` 열 추가 (복사본의 서버 시작 로그에서 확인한 DDL). 기존 행이 있는 DB 에서는 이 `not null` 열을 추가하는 마이그레이션이 필요 |

복사본에서 `POST /loans` 를 호출한 실제 응답입니다.

```text
{"id":1,"book":{"id":1,"isbn":"111","title":"TDD","author":"켄트 벡"},"member":{"id":1,"name":"하승"},"loanedAt":"2026-10-22T13:47:19.717006Z","dueAt":"2026-11-05T13:47:19.717006Z","returnedAt":null}
```

**산출물 2. 프론트 타입 영향**

스펙 차이(`dueAt` 속성과 `required` 목록에 `dueAt` 추가)이고, 생성된 `schema.d.ts` 의 차이는 두 줄입니다.

```text
185a186,187
>             /** Format: date-time */
>             dueAt: string;
```

`pnpm typecheck` 는 오류 **한 곳** 입니다. 화면 컴포넌트는 깨지지 않고, MSW 가짜 서버가 `Loan` 객체를 직접 만드는 곳에서 필수 필드가 빠졌다고 알려 줍니다. `pnpm test` 는 `client.test.ts` 의 18개 모두 통과했습니다.

```text
src/mocks/handlers.ts(127,11): error TS2741: Property 'dueAt' is missing in type '{ id: number; book: { id: number; isbn: string; title: string; author: string; }; member: { id: number; name: string; }; loanedAt: string; returnedAt: null; }' but required in type '{ id: number; book: { id: number; isbn: string; title: string; author: string; }; member: { id: number; name: string; }; loanedAt: string; dueAt: string; returnedAt: string | null; }'.
```

필드를 **추가** 하는 변경은 읽는 쪽 코드를 깨뜨리지 않습니다. 깨지는 것은 그 타입의 객체를 직접 만드는 곳(가짜 서버, 테스트 데이터)입니다.

**백엔드 테스트**: 이 diff 를 적용해 `./gradlew :week25:test` 를 돌리면 31개가 모두 통과합니다. 즉 `dueAt` 을 검사하는 테스트는 PR 에 없습니다.

**산출물 3. 질문**

- `dueAt` 은 14일 상수로 고정되어 있습니다. 정책인가요? 설정으로 빼야 하나요? 연체 처리는 어디서 하나요?
- 이미 운영 DB 에 있는 대출 행의 `due_at` 은 어떻게 채우나요? `not null` 열 추가 마이그레이션이 PR 에 있나요?
- `dueAt` 은 반납 후에도 응답에 남나요? 화면에서 어떻게 보여줄지 정해야 합니다.
- 새 필드를 검사하는 테스트가 없습니다. 추가할 수 있나요?
- 시간대는 UTC 입니다. 프론트가 로컬 시간으로 보여 줘야 하나요?

</details>

### PR 2. "검색 파라미터 이름 정리와 isbn 형식 제한"

```diff
--- a/src/main/java/library/BookController.java
+++ b/src/main/java/library/BookController.java
@@ -32,9 +32,9 @@
     }
 
     @GetMapping
-    @ApiResponse(responseCode = "200", description = "keyword 가 있으면 제목에 포함된 책만 (대소문자 무시)")
-    public List<BookResponse> search(@RequestParam(required = false) String keyword) {
-        return service.searchBooks(keyword);
+    @ApiResponse(responseCode = "200", description = "q 가 있으면 제목에 포함된 책만 (대소문자 무시)")
+    public List<BookResponse> search(@RequestParam(required = false) String q) {
+        return service.searchBooks(q);
     }
 
     @GetMapping("/{id}")
--- a/src/main/java/library/Dto.java
+++ b/src/main/java/library/Dto.java
@@ -5,6 +5,7 @@
 import io.swagger.v3.oas.annotations.media.Schema;
 import jakarta.validation.constraints.NotBlank;
 import jakarta.validation.constraints.NotNull;
+import jakarta.validation.constraints.Pattern;
 import java.time.Instant;
 
 /**
@@ -14,7 +15,7 @@
 public final class Dto {
     private Dto() {}
 
-    public record BookRequest(@NotBlank String isbn, @NotBlank String title, @NotBlank String author) {}
+    public record BookRequest(@NotBlank @Pattern(regexp = "\\d{10}|\\d{13}") String isbn, @NotBlank String title, @NotBlank String author) {}
 
     public record MemberRequest(@NotBlank String name) {}
 
```

<details>
<summary>풀이 (노트에 쓴 뒤에 여세요)</summary>

**산출물 1. 변화표**

| 항목 | 변화 |
|---|---|
| 요청 | `GET /books` 의 쿼리 파라미터 이름이 `keyword` → `q`. `POST /books` 의 `isbn` 은 숫자 10자리 또는 13자리만 허용 |
| 응답 | 성공 응답의 모양은 변화 없음. `POST /books` 에 **새 400 경우** 가 생김 |
| 저장 | 변화 없음. 이미 저장된 `isbn` 은 그대로 |

복사본으로 확인한 실제 동작입니다. 검색은 isbn 이 아니라 **제목**을 기준으로 하므로 책 두 권의 제목을 위와 같이 두고 "Architecture" 로 찾았습니다.

```text
POST /books  isbn 9780132350884, title "Clean Code"          -> 201 (id 1)
POST /books  isbn 0132350882, title "Clean Architecture"  -> 201 (id 2)
POST /books  isbn 111            -> {"message":"isbn: \"\\d{10}|\\d{13}\"와 일치해야 합니다"} [400]
(Accept-Language: en)            -> {"message":"isbn: must match \"\\d{10}|\\d{13}\""} [400]
GET /books?keyword=Architecture  -> 책 2권 모두 (id 1, 2)
GET /books?q=Architecture        -> 책 1권 (id 2)
```

가장 위험한 줄은 `?keyword=` 입니다. 옛 이름은 **오류 없이 무시되고** 전체 목록이 돌아옵니다. `required = false` 라서 모르는 파라미터가 와도 400 이 아니기 때문에, 이름을 바꾼 PR 은 프론트가 알아채지 못하면 "검색이 갑자기 안 먹는" 증상으로만 나타납니다.

**산출물 2. 프론트 타입 영향**

스펙 차이: 파라미터 `name` 이 `keyword` → `q`, 설명 문장, 그리고 `BookRequest.isbn` 에 `"pattern": "\\d{10}|\\d{13}"` 추가.

생성된 `schema.d.ts` 의 차이는 **쿼리 파라미터 이름뿐** 입니다.

```text
367c367
<                 keyword?: string;
---
>                 q?: string;
375c375
<             /** @description keyword 가 있으면 제목에 포함된 책만 (대소문자 무시) */
---
>             /** @description q 가 있으면 제목에 포함된 책만 (대소문자 무시) */
```

`pnpm typecheck` 는 호출하는 곳 한 군데를 정확히 가리킵니다.

```text
src/api/library.ts(8,56): error TS2353: Object literal may only specify known properties, and 'keyword' does not exist in type '{ q?: string | undefined; }'.
```

`pnpm test` 는 `client.test.ts` 의 18개가 통과합니다. **`pattern` 은 TypeScript 타입에 나타나지 않습니다.** `isbn` 은 여전히 `string` 이라서 타입으로는 새 규칙을 알 수 없고, 폼의 입력 검증과 400 처리(`fieldErrors`)를 따로 점검해야 합니다.

**백엔드 테스트**: 이 diff 를 적용해 `./gradlew :week25:test` 를 돌리면 31개 중 10개가 실패합니다. 테스트가 `isbn` 에 `"111"` 같은 값을 쓰고 `keyword` 로 검색하기 때문입니다. PR 에 테스트 수정이 없으므로 이 PR 은 CI 를 통과하지 못합니다.

**산출물 3. 질문**

- `keyword` 를 한동안 `q` 와 함께 받을 수 있나요? 프론트와 백엔드 중 누가 먼저 배포되나요? (옛 이름은 400 도 없이 조용히 무시됩니다)
- `isbn` 형식 제한은 기존 클라이언트를 깨뜨립니다. 하이픈이 들어간 ISBN(`978-0-13-235088-4`)은 허용하나요? 이미 저장된 `111` 같은 데이터는 어떻게 하나요?
- 400 오류 문장은 `Accept-Language` 에 따라 한국어·영어로 바뀝니다. 프론트는 `isbn: ` 형식만 믿어도 되나요?
- 테스트가 수정되지 않았습니다. 의도된 계약 변경이라면 테스트와 `openapi.json` 갱신이 PR 에 있어야 하지 않나요?

</details>

### PR 3. "이미 반납된 대출을 다시 반납해도 오류 없이 처리"

```diff
--- a/src/main/java/library/LibraryService.java
+++ b/src/main/java/library/LibraryService.java
@@ -70,10 +70,9 @@
     public LoanResponse giveBack(long loanId) {
         Loan loan = loans.findById(loanId)
                 .orElseThrow(() -> new NotFoundException("대출을 찾을 수 없습니다: " + loanId));
-        if (loan.getReturnedAt() != null) {
-            throw new ConflictException("이미 반납된 대출입니다: " + loanId);
+        if (loan.getReturnedAt() == null) {
+            loan.markReturned(clock.instant());
         }
-        loan.markReturned(clock.instant());
         return LoanResponse.from(loan);
     }
 
```

<details>
<summary>풀이 (노트에 쓴 뒤에 여세요)</summary>

**산출물 1. 변화표**

| 항목 | 변화 |
|---|---|
| 요청 | 변화 없음 |
| 응답 모양 | 변화 없음 |
| 상태 코드 | `POST /loans/{id}/return` 을 **두 번째** 호출하면 409 `이미 반납된 대출입니다` 가 아니라 **200** 과 같은 본문이 돌아옴 |

복사본에서 같은 대출을 두 번 반납한 실제 결과는 두 번 모두 200 이었고 `returnedAt` 값이 같았습니다(두 번째 호출이 시각을 덮어쓰지 않습니다).

이 diff 에는 컨트롤러도 `Dto` 도 없습니다. **계약 층 파일이 하나도 바뀌지 않았는데 계약이 바뀐** 경우입니다. 서비스가 던지는 예외가 곧 상태 코드를 정하기 때문입니다.

**산출물 2. 프론트 타입 영향**

스펙은 **바뀌지 않았고**(복사본의 스펙과 원본이 같았습니다), 컨트롤러의 `@ApiResponse(responseCode = "409", description = "이미 반납됨")` 선언이 남아 있어 스펙은 여전히 409 를 약속합니다. 즉 **스펙과 실제 동작이 어긋났습니다.** 생성 타입은 달라지지 않으므로 `pnpm typecheck` 는 아무것도 알려 주지 않습니다. 프론트의 409 처리(`conflict` 알림)는 이제 이 경우에 실행되지 않고, 반납 버튼을 연타하면 오류 없이 성공으로 보입니다.

**백엔드 테스트**: 이 diff 를 적용해 `./gradlew :week25:test` 를 돌리면 3개가 실패합니다. 서비스 단위 테스트 `giveBack_setsReturnedAtFromClock_andSecondReturnConflicts`, HTTP 테스트 `giveBack_200Then409_unknown404`, 전체 시나리오 테스트입니다. 모두 "두 번째 반납은 409" 를 단언하던 것들입니다. 이 PR 은 테스트도 고치지 않았습니다.

**산출물 3. 질문**

- 두 번째 반납이 성공하는 것은 의도된 멱등성 설계인가요, 아니면 409 를 제거하려던 건가요?
- 의도된 변경이라면 `@ApiResponse(409)` 선언과 `openapi.json`, 그리고 위 테스트 3개가 같이 수정되어야 하지 않나요?
- 프론트의 "이미 반납됨" 알림은 어떻게 되나요? 화면에서 중복 반납을 어떻게 구분해야 하나요?
- 두 번째 호출은 `returnedAt` 을 그대로 두고 200 을 돌려줍니다. 이 응답이 "방금 반납됨" 으로 오해되지는 않나요?

</details>

위 세 PR 에서 얻는 교훈입니다.

- PR 1: 필드 추가는 읽는 코드를 깨뜨리지 않지만, 객체를 직접 만드는 곳(가짜 서버)을 깨뜨리고, **DB 열 추가는 응답에 안 보이는 변경** 입니다.
- PR 2: 이름 변경은 타입 검사가 잡지만(`typecheck`), 선택적 파라미터라서 **런타임에는 조용히 무시** 됩니다. 값 규칙(`pattern`)은 타입에 안 나타납니다.
- PR 3: 계약 층 파일이 안 바뀌어도 계약이 바뀔 수 있고, 그 변화는 타입 검사로도 스펙 비교로도 잡히지 않습니다. **테스트 diff 와 서비스의 `throw` 를 읽어야** 합니다.

더 알고 싶다면 [REST API 만들기](../archive/java-24weeks/week22-rest-api.md) 를 참고하세요. 그 문서는 같은 구조를 직접 만드는 관점입니다.

## 연습문제

1. 1단계 19개 요청 중 응답 본문에 `message` 가 **없는** 것은 몇 번인가요? 프론트의 `ApiError` 는 그런 응답을 받으면 어떻게 하나요? (`../labs/frontend/src/api/client.ts` 의 `messageOf` 를 읽어 보세요)
2. `MemberController.create` 와 `LoanController.giveBack` 을 7절의 표를 보지 않고 읽어서, URL, 로그인 필요 여부, 성공 코드, 실패 코드를 표로 만들고 7절의 표와 비교하세요.
3. `GET /books` 의 `keyword` 를 `@RequestParam String keyword`(필수)로 바꾸는 PR 이 왔다고 가정하세요. 프론트에서 `keyword` 없이 호출하면 어떻게 되나요? 생성 타입의 `keyword?: string` 은 어떻게 바뀌나요? 복사본에서 확인해 보세요.
4. 컨트롤러에 `@DeleteMapping("/{id}")` 로 책 삭제를 추가하는 PR 을 상상하세요. 이 PR 이 올바르게 완성되려면 최소 어느 파일 어느 부분이 같이 바뀌어야 하나요? (힌트: 6절)
5. 8절 5단계의 질문 틀을 사용해 PR 1 의 질문을 범주별로 다시 분류해 보세요. 빠진 범주가 있나요?
6. (도전) 자신이 읽은 적 없는 PR 한 개를 골라(팀의 PR 이나 오픈소스 Spring 프로젝트) 8절의 세 산출물을 작성하세요.

<details>
<summary>정답 예시</summary>

1. 9번(405)과 10번(415)입니다. `messageOf` 는 본문에 문자열 `message` 가 있으면 그것을 쓰고, 없으면(프록시 오류나 Spring 기본 오류 JSON) `요청에 실패했습니다 (상태코드)` 로 대체합니다.
2. `MemberController.create`: `POST /members`, 로그인 필요, 201, 400/401. `LoanController.giveBack`: `POST /loans/{id}/return`, 로그인 필요, 200, 401/404/409 (스펙에는 400 이 없지만 실제로는 가능).
3. 복사본으로 확인한 결과입니다. `keyword` 없이 `GET /books` 를 호출하면 `{"timestamp":...,"status":400,"error":"Bad Request","path":"/books"} [400]` 입니다. 처리기에 등록되지 않은 오류라서 `{"message"}` 형식이 아닙니다. 스펙의 파라미터는 `"required":true` 가 되고, 생성 타입은 `query?: { keyword?: string }` 에서 `query: { keyword: string }` 으로 바뀌며, `pnpm typecheck` 는 `src/api/library.ts(8,56): error TS2322: Type 'string | undefined' is not assignable to type 'string'.` 으로 호출하는 한 곳을 가리킵니다.
4. `BookController` 에 메서드, 서비스에 삭제 규칙(대출 중인 책을 지우면?), `SecurityConfig` 의 `requestMatchers` 에 `HttpMethod.DELETE` 추가(안 하면 로그인 없이 삭제 가능), CORS 의 `setAllowedMethods` 에 `DELETE` 추가(안 하면 브라우저에서 다른 origin 호출이 막힘), `@ApiResponse` 와 스펙, 테스트입니다.
5. PR 1 의 질문은 데이터(마이그레이션), 호환성(필드 이름), 테스트(새 필드 검증)가 있고, 보안과 오류 계약 범주는 해당 사항이 거의 없습니다. "해당 없음" 도 결론입니다.
6. 답이 없는 열린 문제입니다.

</details>

## 퀴즈

1. `@ApiResponse(responseCode = "409", ...)` 를 지우면 서버의 동작이 바뀌는가?
2. `@RestControllerAdvice` 클래스가 없으면 `NotFoundException` 은 프론트에게 어떤 응답이 되는가?
3. 405 와 415 응답의 본문은 왜 `{"message": ...}` 가 아닌가?
4. 새 `@PostMapping` 이 추가된 PR 에서 `SecurityConfig` 가 바뀌지 않았다면 무엇을 의심해야 하는가?
5. 필수가 아닌 `@RequestParam` 의 이름을 바꾸면 옛 이름으로 호출한 요청은 어떻게 되는가?
6. 계약 층 파일(Controller, Dto)이 바뀌지 않은 PR 도 API 계약을 바꿀 수 있는가?
7. 응답 `record` 에 필드를 추가하는 PR 이 `pnpm typecheck` 를 깨뜨린다면 어느 곳이 깨지는가?

<details>
<summary>정답</summary>

1. 바뀌지 않는다. 그 어노테이션은 문서(스펙)에만 쓰인다. 409 는 서비스가 던진 `ConflictException` 에서 온다
2. 처리되지 않은 예외가 되어 500 과 Spring 기본 오류 JSON(`timestamp`, `status`, `error`, `path`)이 나간다
3. 우리 `ApiExceptionHandler` 에 등록되지 않은 오류라서 Spring 이 기본 형식으로 응답하기 때문이다
4. 새 엔드포인트가 로그인 없이 호출 가능할 수 있다(`anyRequest().permitAll()`). 의도된 것인지 물어야 한다
5. 오류 없이 무시되고 파라미터가 없는 것처럼 처리된다(전체 목록이 돌아오는 식)
6. 그렇다. 서비스가 던지는 예외나 조건이 바뀌면 상태 코드가 바뀐다(PR 3). 그리고 이런 변화는 스펙 비교로도 타입 검사로도 드러나지 않는다
7. 그 타입의 객체를 직접 만드는 곳(MSW 가짜 서버, 테스트 데이터)이다. 값을 읽기만 하는 화면 코드는 깨지지 않는다

</details>

## 체크리스트

- [ ] 1단계의 19개 요청에 먼저 예측을 적었고, 틀린 것의 이유를 적었다
- [ ] 2단계의 고장 8가지 중 최소 4가지를 복사본에서 직접 해 보고, 원복했다
- [ ] 3단계 표를 소스를 보고 채웠고 `POST /loans` 의 실패 조건 4가지를 외우지 않고 찾을 수 있다
- [ ] `@GetMapping`, `@PathVariable`, `@RequestBody`, `@Valid`, `@RestControllerAdvice` 가 각각 HTTP 의 어느 부분을 정하는지 말할 수 있다
- [ ] `@ApiResponse` 가 동작이 아니라 문서에만 쓰인다는 것과, 그래서 스펙과 동작이 어긋날 수 있다는 것을 설명할 수 있다
- [ ] 405/415/500 의 본문이 `{"message"}` 형식이 아니라는 것을 알고, 프론트가 어떻게 대비하는지 안다
- [ ] PR diff 를 계약·규칙·저장·설정·테스트 층으로 분류하고, 요청·응답 변화표를 만들 수 있다
- [ ] 가상의 PR 세 개에서 각각 산출물 세 가지를 먼저 쓰고 풀이와 비교했다
- [ ] 복사본에서 diff 를 적용하고 스펙 차이와 생성 타입 차이를 확인하는 절차를 직접 해 봤다
