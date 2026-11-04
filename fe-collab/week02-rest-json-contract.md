# 2주차. REST 설계와 JSON 계약

**예상 시간:** 6~8시간 (하루 1시간 세션 8회) | **목표:** API 를 "엔드포인트 목록" 이 아니라 **계약**으로 읽는다. 리소스와 URL, 메서드의 의미와 멱등성, 상태 코드, JSON 필드의 필수·`null`·생략, 오류 본문의 모양을 실제 응답으로 확인하고, 엔드포인트 하나의 계약을 한 장으로 요약할 수 있게 된다. 페이지네이션은 개념과 비교만 다룬다.

> 1주(HTTP·DevTools·curl)를 먼저 끝내세요. 이 주차는 `curl` 로 요청을 보내고 응답을 읽는 것에 익숙하다고 가정합니다.
>
> 실습 대상은 1주와 같은 도서관 참조 API `../labs/backend/week25` 입니다. "백엔드 팀이 준 API" 로 보고 호출만 합니다. 데모 로그인은 `demo` / `demo1234` 입니다.
>
> 준비물: JDK 21, `curl`, `jq`. 서버는 1주처럼 `cd labs/backend && ./gradlew :week25:bootRun` 으로 켭니다. 명령은 별도 언급이 없으면 저장소 루트에서 실행합니다.
>
> **서버를 새로 켠 뒤 이 문서의 명령을 위에서부터 순서대로 실행**하세요. 아래 출력의 `id` 와 개수는 그렇게 했을 때의 값입니다. 서버를 껐다 켜면 데이터가 비어 처음부터 다시 시작합니다. 한국어 검증 메시지는 한국어 환경에서 `Accept-Language` 없이 요청한 결과이고, 시각 값(`loanedAt`, `returnedAt`)은 실행마다 다릅니다.

## 하루 1시간으로 나누면

이 주차는 하루 1시간씩 8번의 세션으로 나눕니다. 한 세션은 50~70분이고, 2~3시간이 걸리는 3단계는 (1)과 (2)로 나눕니다.

| 세션 | 할 일 | 끝났을 때 할 수 있어야 하는 것 |
|---|---|---|
| 1 | 1단계(화면 동작으로 API 를 예측하고 → 스펙과 비교하기, 40분)를 하고, 1절(리소스와 URL)을 읽는다 | 화면 동작을 보고 어떤 API 가 불리는지 예측하고, 리소스 이름이 URL 이 되는 규칙을 말할 수 있다 |
| 2 | 2단계(같은 요청을 두 번 보내기 — 멱등성, 1시간)를 한다 | 같은 요청을 두 번 보냈을 때 달라지는 것과 달라지지 않는 것을 상태 기준으로 설명할 수 있다 |
| 3 | 3단계(계약을 읽고, 스펙과 실제를 대조하기, 2~3시간)의 (1) `POST /loans` 계약서 채우기를 한다. 60~70분 안에 못 끝나면 같은 단계에서 이어서 한다 | `POST /loans` 의 계약(로그인, 요청 필드, 성공, 실패 5가지 이상)을 응답만으로 채울 수 있다 |
| 4 | 3단계의 (2) 스펙이 말하는 것과 실제가 같은가를 하고, 2절(메서드의 의미: 안전하고 멱등한가)과 3절(상태 코드를 고르는 기준)을 읽는다 | 스펙에 없는 응답을 직접 만나 스펙이 틀릴 수 있다는 것을 설명하고, 안전한 메서드와 멱등한 메서드를 구분할 수 있다 |
| 5 | 4절(JSON 계약: 필드를 정확히 읽기)과 5절(오류 형식)을 읽는다 | "항상 있음 / `null` 가능 / 생략 가능" 을 JSON, 스펙, TS 타입에서 구분해 읽고, 이 API 의 오류 본문이 두 가지 모양인 것을 말할 수 있다 |
| 6 | 6절(페이지네이션 (개념)), 7절(계약이 바뀔 때: 호환되는 변경과 깨지는 변경), 8절(엔드포인트 하나를 볼 때 묻는 질문)을 읽고, 8절의 질문을 3단계에서 채운 `POST /loans` 계약에 적용해 본다 | 엔드포인트 하나의 계약을 한 장으로 요약하고, 오프셋과 커서 방식의 차이를 한 문장씩 말할 수 있다 |
| 7 | 연습문제와 퀴즈를 푼다 (압축 코스에서는 건너뜀) | `POST /books` 와 `GET /books/{id}` 의 계약을 응답만으로 한 장씩 요약할 수 있다 |
| 8 | 체크리스트의 각 항목을 내 말로 설명해 보고, 막히는 항목은 해당 절을 다시 읽은 뒤 학습 노트를 정리한다 | 엔드포인트 하나를 처음 볼 때 8절의 질문 순서대로 계약을 확인할 수 있다 |

세션을 시작하기 전에 이전 세션의 마지막 결과를 1~2분 훑어봅니다. 막히면 아래 해당 개념 절을 사전처럼 펼쳐 보세요.

## 🖐 먼저 손으로 (읽기 전에 하세요)

> 이 주차의 핵심은 아래 단계를 **직접 해 보면서** 깨닫는 것입니다. 아래쪽 설명 문서는 먼저 읽지 않아도 됩니다. 막혔을 때 사전처럼 펼쳐 보세요.

### 1단계. 화면 동작으로 API 를 예측하고 → 스펙과 비교하기 (40분)

서버를 켜기 **전에**, 도서관 화면에서 일어나는 동작 10개에 대응하는 "메서드 + 경로" 를 노트에 적으세요. 이런 API 가 아예 없을 것 같으면 "없음" 이라고 적어도 됩니다.

| # | 화면 동작 | 내 예측 (메서드 + 경로) |
|---|------|------|
| 1 | 책 목록 보기 | GET + /books|
| 2 | 제목에 포함된 글자로 책 검색 | GET + /books |
| 3 | 책 한 권 상세 보기 | GET + /books/:id|
| 4 | 책 등록 | POST + /books |
| 5 | 회원 등록 | POST + /users|
| 6 | 책 대출 | POST + /loans |
| 7 | 대출 반납 | DELETE + /loans |
| 8 | 회원이 지금 빌린 책 목록 | 모르겠음 |
| 9 | 책 삭제 | DELETE + /books|
| 10 | 책 정보 수정 | PATCH + /books|

서버를 켜고, API 가 스스로 밝히는 목록(OpenAPI 스펙)과 비교합니다. 각 줄은 `메서드 경로 -> 스펙에 적힌 응답 코드` 입니다. 스펙은 6주에 본격적으로 다룹니다. 지금은 읽기만 합니다.

```bash
B=localhost:8080
curl -s $B/v3/api-docs | jq -r '.paths | to_entries[] | .key as $p | .value | to_entries[] | "\(.key|ascii_upcase) \($p) -> \(.value.responses|keys|join(","))"' | sort -k2,2 -k1,1
```

<details>
<summary>정답 (예측을 적은 뒤에 여세요)</summary>

```text
POST /auth/login -> 200,400,401
POST /auth/logout -> 204
GET /auth/me -> 200,401
GET /books -> 200
POST /books -> 201,400,401,409
GET /books/{id} -> 200,400,404
POST /loans -> 201,400,401,404,409
POST /loans/{id}/return -> 200,401,404,409
POST /members -> 201,400,401
GET /members/{id}/loans -> 200,404
```

| # | 답 |
|---|----|
| 1 | `GET /books` |
| 2 | `GET /books?keyword=...` (검색은 별도 경로가 아니라 **같은 목록의 쿼리 파라미터**) |
| 3 | `GET /books/{id}` |
| 4 | `POST /books` |
| 5 | `POST /members` |
| 6 | `POST /loans` (대출이라는 새 자원을 만든다) |
| 7 | `POST /loans/{id}/return` (**동사가 경로에 들어갑니다.** `PUT`, `PATCH` 가 아닙니다) |
| 8 | `GET /members/{id}/loans` (회원 아래에 중첩된 컬렉션) |
| 9 | 없음 |
| 10 | 없음 |

- 9번과 10번이 없습니다. 이 API 에서는 책을 지우거나 고칠 수 없고, 따라서 `DELETE`, `PUT`, `PATCH` 도 하나도 쓰이지 않습니다. "REST API 는 CRUD 4개" 라는 기억은 이 API 에서 맞지 않습니다.
- 회원 목록(`GET /members`)과 대출 목록(`GET /loans`)도 없습니다. 화면이 필요로 하는 것만 있습니다.
- 스펙의 응답 코드 목록은 이 문서의 3단계에서 "실제와 같은가" 를 따져 봅니다.

**항목별 해설**

1. **책 목록 `GET /books`**: 컬렉션은 복수 명사이고 조회는 `GET` 입니다. 스펙 응답은 `200` 하나이고 본문은 배열이며, 로그인 없이 호출할 수 있습니다.
2. **검색 `GET /books?keyword=...`**: 검색용 경로(`/books/search`)를 따로 만들지 않습니다. 경로는 "무엇", 쿼리는 "어떻게 거를까" 입니다. 모르는 쿼리 이름은 에러 없이 무시되므로(1절), 검색이 안 먹는데 에러가 없으면 파라미터 이름 오타를 먼저 의심합니다.
3. **책 상세 `GET /books/{id}`**: `:id` 와 `{id}` 는 표기법 차이입니다. 응답은 `200, 400, 404` 이고, `400` 은 `id` 가 숫자가 아닐 때, `404` 는 그 책이 없을 때입니다.
4. **책 등록 `POST /books`**: 성공은 `201` 에 `Location: /books/{id}` 가 붙고 본문은 새 책입니다. 실패는 `400`(필드 누락·공백), `401`(로그인 필요), `409`(isbn 중복)입니다. 같은 요청을 두 번 보내면 2회째가 `409` 이지만 서버 상태는 그대로라서 멱등으로 안전합니다.
5. **회원 등록 `POST /members`**: 화면에서 "회원" 이라고 부르면 리소스 이름도 `members` 일 가능성이 높지만, 이름은 백엔드가 정하므로 `/users` 일 수도 있습니다. 스펙을 봐야 확정됩니다. 응답은 `201, 400, 401` 입니다. 2단계 c 처럼 두 번 보내면 회원이 둘 생기므로 프론트가 이중 제출을 막아야 합니다.
6. **책 대출 `POST /loans`**: 대출은 새 자원을 만드는 동작이라 `POST` 입니다. 요청 본문은 `{memberId, bookId}` 이고 둘 다 숫자이며 필수입니다. 성공은 `201` 에 `Location: /loans/{id}` 이고 `returnedAt` 은 `null` 입니다. 실패는 `400`(필드 누락), `401`(로그인 필요), `404`(없는 회원·책), `409`(이미 대출 중, 3권 한도)입니다.
7. **대출 반납 `POST /loans/{id}/return`**: `DELETE /loans` 로 예측하기 쉽지만 두 군데가 다릅니다. 첫째, 반납은 대출 기록을 지우는 것이 아니라 `returnedAt` 을 채우는 **상태 변경**이라 `DELETE` 가 아닙니다. 기록은 남습니다. 둘째, 어느 대출인지 가리키는 `{id}` 가 경로에 있어야 합니다. 동작 이름 `return` 이 경로에 들어가며, 순수한 방식인 `PATCH /loans/{id}` 는 이 API 에 없습니다. 응답은 `200`(갱신된 대출), `401`, `404`, `409`(이미 반납됨)입니다.
8. **회원이 지금 빌린 책 `GET /members/{id}/loans`**: 소속 관계는 중첩 경로로 표현합니다. "반납하지 않은 대출만" 이라는 기본 필터는 URL 에 드러나지 않아서 스펙 설명이나 백엔드에 물어야 압니다. 2단계 끝에서 두 회원의 결과가 모두 `[]` 였던 이유입니다. 응답은 `200, 404` 이고 로그인이 필요 없습니다.
9. **책 삭제 (없음)**: 한 권을 가리키는 동작이라 만들어졌다면 `DELETE /books/{id}` 였을 것입니다. 이 API 에는 없습니다. `DELETE /books/1` 을 보내면 경로 `/books/{id}` 는 있지만 `GET` 만 허용해서 `405` 와 `Allow: GET` 이 옵니다(1절).
10. **책 수정 (없음)**: 9번과 같은 구조입니다. 만들어졌다면 `PATCH /books/{id}` 였을 것이고 이 API 에는 없습니다. `PATCH` 도 `GET` 만 있는 경로에는 같은 이유로 `405` 가 될 것으로 보이지만, 이 문서가 직접 확인한 것은 `DELETE` 뿐입니다.

예측이 자주 어긋나는 곳은 세 가지입니다.

- **CRUD 습관**: 7, 9, 10번. 화면 동작을 곧바로 `DELETE`, `PATCH` 로 옮기지 말고 서버가 상태를 어떻게 바꾸는지 봅니다.
- **`{id}` 누락**: 7, 9, 10번. 한 건을 가리키는 동작은 경로에 id 가 있어야 합니다.
- **이름과 존재 여부는 추측 불가**: 5번의 `users` 대 `members`, 9·10번의 "없음". 서버를 켜기 전에는 알 수 없고 스펙으로만 확정됩니다. 이 단계의 목적은 예측을 맞히는 것이 아니라 예측과 스펙의 차이를 확인하는 것입니다.

</details>

### 2단계. 같은 요청을 두 번 보내기 — 멱등성 (1시간)

네트워크가 느리거나 사용자가 버튼을 두 번 누르면 **같은 요청이 두 번** 갑니다. 아래 5가지를 각각 두 번씩 연속으로 보내기 전에, 표에 **첫 번째와 두 번째의 상태 코드**, 그리고 **두 번 보낸 뒤 서버의 데이터가 한 번 보냈을 때와 같은지** 적으세요. 서버는 새로 켠 상태입니다.

| # | 요청 (두 번 연속) | 1회째 | 2회째 | 서버 데이터는 1회와 같은가 |
|---|------|------|------|------|
| a | `GET /books` | [] | []| 같음 |
| b | `POST /books` (같은 isbn, 같은 본문) | {"id":1,"isbn":"string","title":"string","author":"string"} | {"message":"이미 등록된 isbn 입니다: string"}| 다름/ 틀렸음. "서버 데이터"는 한권으로 동일함, 응답이 달라진 것과 서버 상태가 달라진 것은 별개 |
| c | `POST /members` (같은 이름, 같은 본문) |{"id":1,"name":"하승"} | {"id":2,"name":"하승"}| 이름은 같고, id만 다름 |
| d | `POST /loans` (같은 회원, 같은 책) | {"id":1,"book":{"id":1,"isbn":"string","title":"string","author":"string"},"member":{"id":1,"name":"하승"},"loanedAt":"2026-10-30T01:06:15.987066Z","returnedAt":null}|{"message":"이미 대출 중인 책입니다: 1"}  | 2회째에는 전에 이미 빌린 대출이 있으므로 409 에러가 난다.|
| e | `POST /loans/1/return` | {"id":1,"book":{"id":1,"isbn":"string","title":"string","author":"string"},"member":{"id":1,"name":"하승"},"loanedAt":"2026-10-30T01:06:15.987066Z","returnedAt":"2026-10-30T01:07:33.593212Z"}| {"message":"이미 반납된 대출입니다: 1"}| 이미 반납을 했으므로 409 에러|

```bash
B=localhost:8080
H='Content-Type: application/json'
jar=$(mktemp)
curl -s -c "$jar" -X POST -H "$H" -d '{"username":"demo","password":"demo1234"}' $B/auth/login; echo
r() { printf '%s -> ' "$1"; shift; curl -s -b "$jar" -w ' [%{http_code}]\n' "$@"; }

r 'a GET /books' $B/books
r 'a GET /books' $B/books
r 'b POST /books' -X POST -H "$H" -d '{"isbn":"978-1","title":"클린 코드","author":"로버트 마틴"}' $B/books
r 'b POST /books' -X POST -H "$H" -d '{"isbn":"978-1","title":"클린 코드","author":"로버트 마틴"}' $B/books
r 'c POST /members' -X POST -H "$H" -d '{"name":"하승"}' $B/members
r 'c POST /members' -X POST -H "$H" -d '{"name":"하승"}' $B/members
r 'd POST /loans' -X POST -H "$H" -d '{"memberId":1,"bookId":1}' $B/loans
r 'd POST /loans' -X POST -H "$H" -d '{"memberId":1,"bookId":1}' $B/loans
r 'e POST /loans/1/return' -X POST $B/loans/1/return
r 'e POST /loans/1/return' -X POST $B/loans/1/return
```

그리고 서버에 남은 것을 봅니다. 회원 목록 API 는 없으므로 `/members/{id}/loans` 로 두 회원이 존재하는지만 확인합니다.

```bash
curl -s $B/books | jq -c 'length'
curl -s $B/members/1/loans; echo
curl -s $B/members/2/loans; echo
```

<details>
<summary>실제 결과 (예측을 적은 뒤에 여세요)</summary>

```text
{"username":"demo"}
a GET /books -> [] [200]
a GET /books -> [] [200]
b POST /books -> {"id":1,"isbn":"978-1","title":"클린 코드","author":"로버트 마틴"} [201]
b POST /books -> {"message":"이미 등록된 isbn 입니다: 978-1"} [409]
c POST /members -> {"id":1,"name":"하승"} [201]
c POST /members -> {"id":2,"name":"하승"} [201]
d POST /loans -> {"id":1,"book":{"id":1,"isbn":"978-1","title":"클린 코드","author":"로버트 마틴"},"member":{"id":1,"name":"하승"},"loanedAt":"2026-10-22T13:51:47.817814Z","returnedAt":null} [201]
d POST /loans -> {"message":"이미 대출 중인 책입니다: 1"} [409]
e POST /loans/1/return -> {"id":1,"book":{"id":1,"isbn":"978-1","title":"클린 코드","author":"로버트 마틴"},"member":{"id":1,"name":"하승"},"loanedAt":"2026-10-22T13:51:47.817814Z","returnedAt":"2026-10-22T13:51:47.860771Z"} [200]
e POST /loans/1/return -> {"message":"이미 반납된 대출입니다: 1"} [409]
```

```text
1
[]
[]
```

| # | 1회째 | 2회째 | 서버 데이터는 1회와 같은가 |
|---|------|------|------|
| a | 200 | 200 | 같다 |
| b | 201 | **409** | 같다. 책은 한 권뿐 |
| c | 201 | **201** | **다르다.** 같은 이름의 회원이 `id` 1, 2 로 둘이 생겼다 |
| d | 201 | **409** | 같다. 대출은 한 건 |
| e | 200 | **409** | 같다. 반납은 한 번만 일어났다 |

- **멱등(idempotent)** 은 "같은 요청을 여러 번 보내도 **서버 상태**가 한 번 보낸 것과 같다" 는 뜻입니다. **응답이 같다는 뜻이 아닙니다.** b, d, e 는 2회째 응답이 409 로 달라도 서버 상태는 같으므로 상태 기준으로는 안전합니다.
- c 가 위험한 경우입니다. 사용자가 "등록" 버튼을 두 번 누르면 같은 회원이 둘 생깁니다. 서버가 막아 주지 않으면 프론트가 막아야 합니다(요청이 끝날 때까지 버튼을 비활성화하는 식으로. 10주).
- 요청 후 응답이 오기 전에 연결이 끊기면 프론트는 서버에서 성공했는지 알 수 없습니다. 이때 d 나 e 를 다시 보냈다가 409 를 받으면 "첫 번째가 이미 성공했다" 는 신호일 수 있습니다. 409 를 단순한 실패로 취급하지 말고 서버의 현재 상태를 다시 읽어 판단해야 합니다.
- 회원 두 명의 `/members/{id}/loans` 가 모두 `[]` 인 것은, 이 API 가 **반납하지 않은 대출만** 돌려주기 때문입니다(스펙의 설명). `id` 1 대출은 이미 반납됐습니다.

</details>

### 3단계. 계약을 읽고, 스펙과 실제를 대조하기 (2~3시간)

#### (1) `POST /loans` 계약서 채우기

아래 표를 `curl` 로 직접 호출해서 채우세요. 문서나 코드를 보지 않고 응답만으로 채웁니다. 2단계에서 이어서 `jar`, `H`, `r` 을 그대로 씁니다. (책 2권이 필요하므로 먼저 등록합니다.)

```bash
curl -s -i -b "$jar" -X POST -H "$H" -d '{"isbn":"978-2","title":"리팩터링","author":"마틴 파울러"}' $B/books | grep -E '^(HTTP|Location|\{)'
curl -s -i -b "$jar" -X POST -H "$H" -d '{"memberId":1,"bookId":2}' $B/loans | grep -E '^(HTTP|Location|\{)'
```

| 항목 | 내가 확인한 것 |
|---|---|
| 로그인이 필요한가 | O |
| 요청 본문 필드와 JSON 타입 | X |
| 성공 코드와 `Location` 헤더 | 책 첫 등록 시 /books/1|
| 성공 본문에서 `null` 일 수 있는 필드 | 없음 |
| 실패 ① `bookId` 를 빼고 보냈을 때 (코드, `message`) |bookId는 null이면 안됨 |
| 실패 ② 없는 회원 `99` 로 보냈을 때 | 회원을 찾을 수 없습니다 |
| 실패 ③ 없는 책 `99` 로 보냈을 때 | 요청애 있는 memberId 자체가 없는 회원이라 나와서 확인 제대로 불가 |
| 실패 ④ 이미 대출 중인 책 `2` 를 다른 회원(`2`)이 빌릴 때 |요청애 있는 memberId 자체가 없는 회원이라 나와서 확인 제대로 불가 |
| 실패 ⑤ 로그인 없이 보냈을 때 |401 에러|

이런 요청을 보내 확인합니다.

```bash
r 'bookId 없음' -X POST -H "$H" -d '{"memberId":1}' $B/loans
r 'member 99' -X POST -H "$H" -d '{"memberId":99,"bookId":2}' $B/loans
r 'book 99' -X POST -H "$H" -d '{"memberId":1,"bookId":99}' $B/loans
r 'already' -X POST -H "$H" -d '{"memberId":2,"bookId":2}' $B/loans
curl -s -w ' [%{http_code}]\n' -X POST -H "$H" -d '{"memberId":1,"bookId":2}' $B/loans
```

한 가지 더, 한 회원이 이미 3권을 빌린 경우를 만듭니다. 책 3권을 더 등록하고, 회원 `1` 이 2번 책을 포함해 3권을 빌린 뒤 넷째 책을 빌려 보세요.

```bash
r 'book3' -X POST -H "$H" -d '{"isbn":"978-3","title":"t3","author":"a"}' $B/books
r 'book4' -X POST -H "$H" -d '{"isbn":"978-4","title":"t4","author":"a"}' $B/books
r 'book5' -X POST -H "$H" -d '{"isbn":"978-5","title":"t5","author":"a"}' $B/books
r 'loan 3' -X POST -H "$H" -d '{"memberId":1,"bookId":3}' $B/loans
r 'loan 4' -X POST -H "$H" -d '{"memberId":1,"bookId":4}' $B/loans
r 'limit' -X POST -H "$H" -d '{"memberId":1,"bookId":5}' $B/loans
```

<details>
<summary>정답</summary>

```text
HTTP/1.1 201 
Location: /books/2
{"id":2,"isbn":"978-2","title":"리팩터링","author":"마틴 파울러"}
HTTP/1.1 201 
Location: /loans/2
{"id":2,"book":{"id":2,"isbn":"978-2","title":"리팩터링","author":"마틴 파울러"},"member":{"id":1,"name":"하승"},"loanedAt":"2026-10-22T13:51:48.254799Z","returnedAt":null}
```

```text
bookId 없음 -> {"message":"bookId: 널이어서는 안됩니다"} [400]
member 99 -> {"message":"회원을 찾을 수 없습니다: 99"} [404]
book 99 -> {"message":"책을 찾을 수 없습니다: 99"} [404]
already -> {"message":"이미 대출 중인 책입니다: 2"} [409]
{"message":"로그인이 필요합니다"} [401]
```

```text
book3 -> {"id":3,"isbn":"978-3","title":"t3","author":"a"} [201]
book4 -> {"id":4,"isbn":"978-4","title":"t4","author":"a"} [201]
book5 -> {"id":5,"isbn":"978-5","title":"t5","author":"a"} [201]
loan 3 -> {"id":3,"book":{"id":3,"isbn":"978-3","title":"t3","author":"a"},"member":{"id":1,"name":"하승"},"loanedAt":"2026-10-22T13:51:48.407496Z","returnedAt":null} [201]
loan 4 -> {"id":4,"book":{"id":4,"isbn":"978-4","title":"t4","author":"a"},"member":{"id":1,"name":"하승"},"loanedAt":"2026-10-22T13:51:48.424046Z","returnedAt":null} [201]
limit -> {"message":"한 사람은 3권까지 빌릴 수 있습니다"} [409]
```

| 항목 | 계약 |
|---|---|
| 로그인 | 필요. 없으면 401 |
| 요청 본문 | `memberId`: 숫자, `bookId`: 숫자. 둘 다 필수 |
| 성공 | 201, `Location: /loans/{id}`, 본문은 새 대출 |
| `null` 가능 필드 | `returnedAt` (반납 전) |
| 실패 ① | 400, `bookId: 널이어서는 안됩니다` (필드 이름이 앞에 붙음) |
| 실패 ②③ | 404, 어느 대상이 없는지가 메시지에 있음 |
| 실패 ④ | 409, `이미 대출 중인 책입니다: 2` |
| 실패 ⑤ | 401, `로그인이 필요합니다` |
| 추가 | 한 회원이 3권을 빌린 상태에서 또 빌리면 409, `한 사람은 3권까지 빌릴 수 있습니다` |

- ②③ 의 404 는 **요청 본문이 올바른 형식인데 가리키는 자원이 없는 경우**입니다. 400 이 아니라 404 입니다. ④ 의 409 는 자원은 있지만 **현재 상태가 허락하지 않는 경우**입니다.
- 이 표 한 장이 프론트가 알아야 할 `POST /loans` 의 전부입니다. 6주에서는 이 표를 스펙에서 기계가 만들어 줍니다.

</details>

#### (2) 스펙이 말하는 것과 실제가 같은가

스펙(`/v3/api-docs`)이 `GET /members/{id}/loans` 와 `POST /loans/{id}/return` 에 어떤 응답 코드를 적어 두었는지 보고, **숫자가 아닌 `id` 를 넣어** 호출해 보세요. 그리고 스펙에 없는 `DELETE` 와 `Content-Type` 없는 `POST` 도 호출합니다.

```bash
curl -s $B/v3/api-docs | jq -c '.paths["/members/{id}/loans"].get.responses | keys'
curl -s $B/v3/api-docs | jq -c '.paths["/loans/{id}/return"].post.responses | keys'
curl -s -w ' [%{http_code}]\n' $B/members/abc/loans
curl -s -w ' [%{http_code}]\n' -b "$jar" -X POST $B/loans/abc/return
curl -s -w ' [%{http_code}]\n' -X DELETE $B/loans/1
curl -s -w ' [%{http_code}]\n' -b "$jar" -X POST -d '{}' $B/books
```

스펙에 적혀 있지 않은 응답이 몇 개 나오는지 세어 보세요.

<details>
<summary>정답</summary>

```text
["200","404"]
["200","401","404","409"]
{"message":"id: 형식이 올바르지 않습니다"} [400]
{"message":"id: 형식이 올바르지 않습니다"} [400]
{"timestamp":"2026-10-22T13:51:48.517+00:00","status":404,"error":"Not Found","path":"/loans/1"} [404]
{"timestamp":"2026-10-22T13:51:48.533+00:00","status":415,"error":"Unsupported Media Type","path":"/books"} [415]
```

- 4건 모두 스펙에 없는 응답입니다. 앞의 두 건은 400 이 실제로 오지만 스펙에는 없고(`GET /books/{id}` 에는 400 이 적혀 있는데 같은 종류의 `id` 오류를 다른 엔드포인트는 적지 않았습니다), 뒤의 두 건은 프레임워크의 기본 오류입니다.
- 스펙은 사람이 선언한 것을 모아 만들기 때문에 **빠진 응답이 있을 수 있습니다.** 스펙에 없는 코드가 오면 스펙이 틀린 것이지 서버가 이상한 것이 아닐 수 있습니다. 백엔드에 알려서 스펙을 고치게 하는 것이 협업입니다.
- 마지막 두 건의 본문은 `{"message": "..."}` 가 아닙니다. 5절에서 이어 다룹니다.

</details>

---

## 1. 리소스와 URL

REST 의 출발점은 URL 이 **동작이 아니라 대상(리소스)** 을 가리키고, 동작은 HTTP 메서드가 맡는다는 것입니다. 이 API 의 리소스입니다.

| 리소스 | URL | 메서드 | 비고 |
|---|---|---|---|
| 책 컬렉션 | `/books` | `GET`, `POST` | `GET` 은 `keyword` 쿼리로 거름 |
| 책 한 권 | `/books/{id}` | `GET` | |
| 회원 컬렉션 | `/members` | `POST` | 목록 조회는 없음 |
| 회원의 대출 | `/members/{id}/loans` | `GET` | 반납하지 않은 대출만 |
| 대출 컬렉션 | `/loans` | `POST` | |
| 대출의 반납 | `/loans/{id}/return` | `POST` | 동사가 경로에 있음 |
| 로그인 | `/auth/login`, `/auth/logout`, `/auth/me` | `POST`, `POST`, `GET` | |

- **컬렉션은 복수 명사**(`/books`)이고, 하나를 가리킬 때는 뒤에 id 를 붙입니다(`/books/{id}`). `{id}` 는 경로 변수라고 부릅니다.
- **필터, 검색, 정렬은 쿼리스트링**(`?keyword=`)에 둡니다. 경로는 "무엇인가", 쿼리는 "어떻게 거를까" 입니다. `GET /books/search` 같은 경로를 따로 만들지 않습니다.
- **소속 관계는 중첩**으로 표현합니다. `/members/{id}/loans` 는 "그 회원의 대출" 입니다.
- 현실에서는 **동작이 경로에 들어가는 예외**가 흔합니다. `/loans/{id}/return` 은 "대출의 상태를 바꾸는 동작" 입니다. 이 방식을 비판하는 사람도 있습니다. 순수한 방식은 `PATCH /loans/{id}` 로 `returnedAt` 을 채우는 것인데, **이 참조 API 에는 없는 방식**입니다. 팀이 정한 규칙을 계약에 적어 두는 것이 더 중요합니다.
- `GET /members/{id}/loans` 처럼 **이름에 드러나지 않는 기본 필터**가 있습니다(반납한 대출은 빼고 돌려줍니다). 이런 규칙은 URL 로는 알 수 없어서 스펙의 설명이나 백엔드에 물어야 합니다.
- **모르는 쿼리 파라미터는 조용히 무시됩니다.** 페이지를 요청하는 `page`, `size` 를 보내도 에러가 나지 않고 그냥 전부 돌려줍니다.

  ```bash
  curl -s "$B/books" | jq length
  curl -s "$B/books?page=2&size=1" | jq length
  ```

  ```text
  5
  5
  ```

  오타를 낸 쿼리 이름도 같은 방식으로 무시되므로, 검색이 안 먹는데 에러가 없다면 파라미터 이름부터 의심하세요.

- **경로가 없는 것과 메서드가 안 되는 것은 다릅니다.** `DELETE /books/1` 은 `/books/{id}` 에 `GET` 만 있어서 405 이고 `Allow: GET` 을 돌려줍니다. 반면 `DELETE /loans/1` 은 `/loans/{id}` 라는 경로 자체가 없어서 404 입니다.

  ```bash
  curl -s -i -X DELETE $B/books/1 | sed -n '1p;/^Allow/p'
  ```

  ```text
  HTTP/1.1 405 
  Allow: GET
  ```

**TS 와 비교** (백엔드 용어를 프론트의 익숙한 것에 대응시킨 표입니다. 완전히 같지는 않습니다.)

| 백엔드 쪽 용어 | 프론트에서 만나는 모습 |
|---|---|
| 리소스 / 컬렉션 | 데이터 모델 / 배열 (`Book`, `Book[]`) |
| 경로 변수 `/books/{id}` | Next 의 동적 경로 `app/books/[id]`, 요청 함수의 `params.path.id` |
| 쿼리 파라미터 | `URLSearchParams`, Next 의 `searchParams` |
| 컬렉션 URL 의 `GET` | 목록 조회 `useQuery` 의 대상 |
| 컬렉션 URL 의 `POST` | 생성 `useMutation` 의 대상 |

## 2. 메서드의 의미: 안전하고 멱등한가

| 메서드 | 의미 | 안전 (서버 상태를 안 바꿈) | 멱등 (여러 번 = 한 번) | 이 API 에서 |
|---|---|---|---|---|
| `GET` | 조회 | 예 | 예 | 있음 |
| `POST` | 생성, 또는 그 밖의 동작 | 아니오 | **보장 없음** | 있음 |
| `PUT` | 통째로 교체 | 아니오 | 예 | 참조 API 에는 없음 |
| `PATCH` | 일부 수정 | 아니오 | 보장 없음 | 참조 API 에는 없음 |
| `DELETE` | 삭제 | 아니오 | 예 | 참조 API 에는 없음 |

- `PUT`, `PATCH`, `DELETE` 행은 HTTP 의 일반 규칙입니다. 이 API 에서 실행해 본 것이 아닙니다(실제로는 405 와 404 만 확인했습니다). 팀의 다른 API 에서는 쓰일 수 있습니다.
- 2단계에서 본 대로 **멱등은 서버 상태 기준**입니다. 두 번째 요청이 409 를 받아도 멱등일 수 있습니다.
- `POST` 는 멱등이 보장되지 않아서, 같은 요청을 두 번 받아 알아서 한 번처럼 처리하는지는 **그 API 의 규칙**입니다. 책 등록은 isbn 중복 검사 덕분에 안전했고, 회원 등록은 그런 검사가 없어 안전하지 않았습니다.
- 프론트에서 실질적으로 중요한 것은 **자동 재시도가 안전한가**입니다. TanStack Query 의 쿼리(`useQuery`)는 기본으로 실패 시 재시도하지만(`retry` 기본값 3, 브라우저 기준), 변경(`useMutation`)은 기본으로 재시도하지 않습니다(`retry` 기본값 0). `GET` 은 재시도해도 안전하고, `POST` 는 그렇지 않기 때문입니다. 설계는 10주에 이어집니다.

## 3. 상태 코드를 고르는 기준

이 API 의 선택을 기준 삼아 정리합니다.

| 상황 | 코드 | 이 API 의 예 |
|---|---|---|
| 새 자원을 만들었다 | 201 + `Location` | `POST /books`, `POST /members`, `POST /loans` |
| 조회 또는 동작 성공, 본문 있음 | 200 | `GET /books`, `POST /loans/{id}/return` |
| 성공, 본문 없음 | 204 | `POST /auth/logout` |
| 요청 형식이 틀렸다 | 400 | 필수 값 누락, 깨진 JSON, `id` 가 숫자 아님 |
| 로그인이 필요하다 | 401 | 로그인 없이 `POST` |
| 가리키는 자원이 없다 | 404 | 없는 책·회원·대출 |
| 형식은 맞는데 현재 상태와 충돌한다 | 409 | 중복 isbn, 이미 대출 중, 3권 한도, 이미 반납 |

- 같은 "실패" 라도 **프론트가 해야 할 일이 다릅니다.** 400 은 입력칸 옆에 문구, 401 은 로그인 안내, 404 는 "없음" 표시, 409 는 현재 상태를 다시 읽어 보여 주기입니다. 이 분기가 `labs/frontend/src/api/client.ts` 에 있고 7주에 자세히 다룹니다.
- 404 와 409 를 가르는 질문: "그 대상이 **존재하는가**?" 아니면 "존재하지만 **지금 상태에서 안 되는가**?"

## 4. JSON 계약: 필드를 정확히 읽기

### 4-1. 타입과 날짜

- JSON 의 값은 문자열, 숫자, 불리언, `null`, 객체, 배열뿐입니다. `Date`, `undefined` 는 없습니다.
- `id` 는 숫자입니다. 2단계 응답의 `"id":1`.
- 시각은 **ISO 8601 문자열**입니다. 2단계의 `"loanedAt":"2026-10-22T13:51:47.817814Z"` 처럼 UTC 를 뜻하는 `Z` 가 붙습니다. TS 에서는 `string` 입니다. 날짜로 다루려면 직접 `new Date(...)` 로 변환합니다. 초 아래 자릿수는 고정이 아니므로 문자열 자르기로 파싱하지 마세요.

### 4-2. 값이 없다는 것의 세 가지 모양

| 모양 | JSON | 스펙 | 생성된 TS | 이 API 의 예 |
|---|---|---|---|---|
| 항상 있다 | `"id":1` | `required` 에 포함 | `id: number` | `BookResponse.id` |
| 키는 있고 값이 `null` | `"returnedAt":null` | `required` 에 포함 + `type: ["string","null"]` | `returnedAt: string \| null` | 반납 전 `LoanResponse.returnedAt` |
| 키가 없을 수 있다 | (키 없음) | `required` 에서 빠짐 | `keyword?: string` | `GET /books` 의 `keyword` 쿼리 |

같은 대출이 반납 전후에 어떻게 달라지는지는 2단계 d, e 의 응답에 있습니다.

```text
"returnedAt":null                                   (대출 직후)
"returnedAt":"2026-10-22T13:51:47.860771Z"          (반납 후)
```

`labs/frontend/src/api/schema.d.ts` 에서 실제로 생성된 타입입니다.

```ts
            /**
             * Format: date-time
             * @description 반납 전에는 null
             */
            returnedAt: string | null;
```

이 세 가지는 프론트 코드가 다릅니다. `returnedAt` 은 `=== null` 로 검사하고, `keyword` 는 생략하고, `id` 는 검사 없이 씁니다. 세 모양을 구분하지 않고 "비어 있을 수 있음" 으로 뭉뚱그리면 백엔드와 말이 어긋납니다. 이 구분은 6주에서 타입 생성으로 이어집니다.

**TS 와 비교**

| 백엔드·계약 쪽 용어 | 프론트에서 만나는 모습 |
|---|---|
| nullable (값이 `null` 일 수 있음) | `string \| null` |
| optional / 생략 가능 | `keyword?: string` |
| required (필수) | 타입에 `?` 가 없음 |
| 요청 DTO / 응답 DTO | 요청 `interface` 와 응답 `interface` 를 따로 둔다 |
| `@NotBlank` 같은 값 검증 | zod 의 `.min(1)` 과 비슷. 타입은 `string` 으로만 보인다 |

### 4-3. 서버에 보내는 JSON

필수 필드가 비었을 때의 결과입니다. 아래 요청은 `title` 을 `null`, 공백, 아예 생략한 세 경우입니다. 서버 규칙(`@NotBlank`)상 셋 다 같은 결과입니다.

```bash
r 'null' -X POST -H "$H" -d '{"isbn":"978-12","title":null,"author":"a"}' $B/books
r 'blank' -X POST -H "$H" -d '{"isbn":"978-12","title":"   ","author":"a"}' $B/books
r 'missing' -X POST -H "$H" -d '{"isbn":"978-12","author":"a"}' $B/books
```

```text
null -> {"message":"title: 공백일 수 없습니다"} [400]
blank -> {"message":"title: 공백일 수 없습니다"} [400]
missing -> {"message":"title: 공백일 수 없습니다"} [400]
```

그런데 서버는 **관대한 곳도 있습니다.** 다음 요청이 모두 성공합니다. 모르는 필드 `pages`, 서버가 정하는 `id` 를 보낸 경우, 문자열 자리에 숫자를 보낸 경우, 숫자 자리에 숫자 모양 문자열을 보낸 경우입니다.

```bash
r 'extra' -X POST -H "$H" -d '{"isbn":"978-9","title":"t","author":"a","pages":300}' $B/books
r 'id' -X POST -H "$H" -d '{"id":99,"isbn":"978-10","title":"t","author":"a"}' $B/books
r 'title 123' -X POST -H "$H" -d '{"isbn":"978-11","title":123,"author":"a"}' $B/books
r 'memberId str' -X POST -H "$H" -d '{"memberId":"2","bookId":5}' $B/loans
r 'memberId abc' -X POST -H "$H" -d '{"memberId":"abc","bookId":5}' $B/loans
```

```text
extra -> {"id":6,"isbn":"978-9","title":"t","author":"a"} [201]
id -> {"id":7,"isbn":"978-10","title":"t","author":"a"} [201]
title 123 -> {"id":8,"isbn":"978-11","title":"123","author":"a"} [201]
memberId str -> {"id":5,"book":{"id":5,"isbn":"978-5","title":"t5","author":"a"},"member":{"id":2,"name":"하승"},"loanedAt":"2026-10-22T13:51:48.667977Z","returnedAt":null} [201]
memberId abc -> {"message":"요청 본문을 읽을 수 없습니다 (JSON 형식 확인)"} [400]
```

- 모르는 필드 `pages` 는 조용히 버려지고, `id` 도 무시되어 서버가 `7` 을 붙였습니다(요청에 보낸 `99` 가 아닙니다). 필드 이름을 잘못 보내도 에러가 나지 않으므로, 저장이 안 된 것처럼 보이면 필드 이름부터 의심하세요.
- 숫자 `123` 은 문자열 `"123"` 으로, 문자열 `"2"` 는 숫자 `2` 로 자동 변환되었습니다. **서버가 관대하다고 해서 계약이 그런 것이 아닙니다.** 계약은 `memberId` 가 숫자라고 말합니다. 폼 입력값은 문자열이므로 `Number(...)` 로 바꿔 보내야 하고, 서버의 관대함에 기대면 다른 서버나 다음 버전에서 깨집니다.
- 모양이 아예 안 맞는 `"abc"` 는 400 입니다.

## 5. 오류 형식

오류 응답의 모양이 일정해야 프론트가 한 곳에서 처리할 수 있습니다. 이 API 가 직접 만든 오류는 항상 `{"message": "..."}` 입니다.

| 상황 | 코드 | 본문 `message` |
|---|---|---|
| 필수 값 누락·공백 | 400 | `title: 공백일 수 없습니다` (필드가 여럿이면 `author: ..., title: ...`) |
| 깨진 JSON, 빈 본문, 타입 불일치 | 400 | `요청 본문을 읽을 수 없습니다 (JSON 형식 확인)` |
| 경로 변수가 숫자 아님 | 400 | `id: 형식이 올바르지 않습니다` |
| 로그인 필요 | 401 | `로그인이 필요합니다` |
| 아이디·비밀번호 틀림 | 401 | `아이디 또는 비밀번호가 올바르지 않습니다` |
| 대상 없음 | 404 | `책을 찾을 수 없습니다: 999` |
| 규칙 충돌 | 409 | `이미 등록된 isbn 입니다: 978-1`, `이미 대출 중인 책입니다: 1`, `이미 반납된 대출입니다: 1`, `한 사람은 3권까지 빌릴 수 있습니다` |

모두 이 문서와 1주에서 실제로 받아 본 응답입니다.

- **400 의 검증 메시지는 `필드: 이유` 를 쉼표로 이은 한 문자열**입니다. 필드가 알파벳순으로 나옵니다. 프론트는 이것을 쪼개서 입력칸마다 보여 줍니다. `labs/frontend/src/api/client.ts` 의 일부입니다.

  ```ts
    /** 400 의 "isbn: must not be blank, title: ..." 를 { isbn: "...", title: "..." } 로 나눈다. */
    get fieldErrors(): Record<string, string> {
      if (this.kind !== "field") return {};
      const fields: Record<string, string> = {};
      for (const part of this.message.split(", ")) {
        const match = /^(\w+): (.+)$/.exec(part);
        if (match) fields[match[1]] = match[2];
      }
      return fields;
    }
  ```

  이 코드는 `필드: 이유` 라는 **형식**에 기댑니다. 문구가 한국어든 영어든 동작하는 이유입니다. 문구 자체(`공백일 수 없습니다`)는 `Accept-Language` 에 따라 바뀌므로 코드에서 문구를 비교하면 안 됩니다. 1주 2단계 g 에서 확인했습니다.

- **오류 본문이 두 가지 모양입니다.** 앞의 표에 없는 경우(없는 경로의 404, 안 되는 메서드의 405, 지원하지 않는 `Content-Type` 의 415)는 프레임워크가 기본으로 만든 본문이 옵니다.

  ```text
  {"timestamp":"2026-10-22T13:51:48.517+00:00","status":404,"error":"Not Found","path":"/loans/1"}
  ```

  이 본문에는 `message` 키가 없습니다. 프론트 클라이언트가 이 경우를 따로 처리하는 것을 `labs/frontend/src/api/client.ts` 에서 볼 수 있습니다.

  ```ts
  function messageOf(error: unknown, status: number): string {
    if (typeof error === "object" && error !== null && "message" in error && typeof error.message === "string") {
      return error.message;
    }
    // 프록시 오류(백엔드 꺼짐 등)처럼 {"message"} 형식이 아닌 응답
    return `요청에 실패했습니다 (${status})`;
  }
  ```

  정상적인 화면에서는 이 오류들(없는 경로, 틀린 메서드)이 거의 나지 않지만, **개발 중에 경로를 틀리면 이 모양을 만나게 됩니다.** 오류 본문을 하나로 통일할지는 백엔드와 협의할 계약 항목입니다.

## 6. 페이지네이션 (개념)

**참조 API 에는 페이지네이션이 없습니다.** `GET /books` 는 항상 전부를 돌려줍니다(1절에서 `page`, `size` 가 무시되는 것을 확인했습니다). 아래는 다른 백엔드 API 에서 만나게 될 개념이고, 이 저장소에서 실행해 본 것이 아닙니다.

| | 오프셋(offset) | 커서(cursor) |
|---|---|---|
| 요청 | "몇 번째부터 몇 개": `?offset=40&limit=20` 또는 `?page=3&size=20` | "이 표식 다음부터 몇 개": `?cursor=표식&limit=20` |
| 응답 | 항목 + 총 개수를 담는 경우가 많음 | 항목 + 다음 커서(없으면 끝) |
| 장점 | 임의의 페이지로 바로 이동, "총 N페이지" 표시 쉬움 | 데이터가 중간에 추가·삭제되어도 중복·누락이 적음, 많은 데이터에서 일정한 성능 |
| 단점 | 페이지를 넘기는 사이 데이터가 바뀌면 같은 항목이 두 번 나오거나 건너뜀, 큰 offset 일수록 느려질 수 있음 | 임의 페이지로 이동 불가, 총 개수를 알기 어려움 |
| 어울리는 UI | 페이지 번호 | 무한 스크롤, "더 보기" |

응답 모양의 예시입니다. **개념 예시이며 참조 API 에는 없는 형식**입니다.

```json
{ "items": [ { "id": 41, "title": "..." } ], "total": 120, "page": 3, "size": 20 }
```

```json
{ "items": [ { "id": 41, "title": "..." } ], "nextCursor": "eyJpZCI6NDF9" }
```

- 프론트 쪽 대응: 오프셋은 `page` 를 상태(또는 URL 쿼리)에 두고 쿼리 키에 넣는 방식이 자연스럽고, 커서는 `useInfiniteQuery` 가 이 모양에 맞춰져 있습니다. 둘 다 10주에서 다룹니다.
- 백엔드에 반드시 물어볼 계약 질문입니다: 한 번에 몇 개가 기본인가? 최대는? 정렬 기준은 안정적인가(같은 값이 있을 때 순서)? 총 개수가 응답에 있는가? 범위를 벗어난 페이지는 빈 배열인가 오류인가? 커서는 불투명한 문자열로 취급해도 되는가?

## 7. 계약이 바뀔 때: 호환되는 변경과 깨지는 변경

| 변경 | 프론트에 미치는 영향 |
|---|---|
| 응답에 새 필드 추가 | 대개 안전. 프론트가 모르는 필드는 무시하면 된다 |
| 응답 필드 이름 변경·삭제 | **깨진다.** 그 필드를 쓰던 화면이 `undefined` 를 받는다 |
| 응답 필드 타입 변경 (숫자 → 문자열 등) | 깨진다 |
| 필드가 `null` 불가에서 `null` 가능으로 | 깨질 수 있다. 검사 없이 쓰던 코드가 터진다 |
| 요청에 새 필수 필드 추가 | 깨진다. 기존 요청이 400 이 된다 |
| 새 오류 코드 추가 | 프론트가 모르는 코드를 "알 수 없음" 으로 처리하는지에 달렸다 |

이런 변경이 말로만 전달되면 화면이 깨지고 나서 알게 됩니다. 6주에서는 스펙에서 타입을 생성해, 이런 변경이 `pnpm typecheck` 의 컴파일 오류로 드러나게 합니다.

## 8. 엔드포인트 하나를 볼 때 묻는 질문

새 API 를 받으면 아래를 순서대로 확인합니다.

1. 메서드와 경로가 의미에 맞는가? 경로 변수와 쿼리가 구분되어 있는가?
2. 로그인이 필요한가?
3. 요청 본문의 각 필드는 필수인가, 타입은 무엇인가, 값 규칙(공백 금지 등)이 있는가?
4. 성공 코드는 무엇이고, 응답에서 `null` 일 수 있거나 생략될 수 있는 필드는 무엇인가?
5. 실패 코드는 어떤 상황에서 오고, `message` 형식은 일정한가?
6. 같은 요청을 두 번 보내면 어떻게 되는가? 재시도해도 안전한가?
7. 목록이라면 개수 제한이나 페이지네이션이 있는가?
8. 이 질문들의 답이 스펙에 적혀 있는가, 아니면 백엔드에 물어야 하는가?

서버 쪽에서 같은 규칙을 어떻게 구현했는지 궁금하다면 [REST API 만들기](../archive/java-24weeks/week22-rest-api.md) 를 참고하세요. 필수는 아닙니다.

## 연습문제

1. 3단계 (1) 의 표를 `POST /books` 에 대해 같은 방법으로 채우세요. 응답만으로 채우고 실패는 400, 401, 409 를 포함하세요.
2. `GET /books/{id}` 의 계약을 한 장으로 요약하세요. 성공 본문의 필드 4개가 모두 "항상 있다" 로 분류되는지 판단하세요. (`jq -c '.components.schemas.BookResponse | {required}'` 로 스펙을 확인해도 됩니다.)
3. 책에 `publishedYear`(출간 연도, 모르는 책도 있음)를 추가한다고 가정하세요. 응답 필드로 어떤 모양 세 가지(항상 있음 / `null` 가능 / 생략 가능) 중 무엇을 제안하겠고, 프론트 코드는 어떻게 달라지는지 쓰세요. 기존 화면이 깨지는 변경인지도 판단하세요.
4. 오프셋 방식 페이지네이션에서 사용자가 2페이지를 보는 동안 누군가 1페이지의 책을 한 권 등록하면 3페이지로 넘어갔을 때 무슨 일이 생기는지 설명하세요. 커서 방식은 어떻게 다른가요?
5. 이 API 의 오류 본문이 두 가지 모양인 것을 백엔드 팀에게 어떻게 전달하겠습니까? 재현 요청(`curl`) 1개와 기대하는 동작을 포함한 두 문장으로 쓰세요.

## 퀴즈

1. `POST /members` 를 같은 본문으로 두 번 보내면 회원이 둘 생겼다. 이것은 REST 의 규칙 위반인가?
2. 존재하지 않는 회원 id 로 대출을 요청하면 400 이 아니라 404 인 이유는?
3. `returnedAt: string | null` 과 `keyword?: string` 의 차이는?
4. 스펙에 400 이 적혀 있지 않은 엔드포인트에서 400 이 왔다. 누구의 문제인가?
5. `fetch` 로 `POST /loans` 를 보냈는데 응답이 오기 전에 연결이 끊겼다. 다시 보낸 요청이 409 를 받았다면 무엇을 의심해야 하는가?

<details>
<summary>정답</summary>

1. 아니다. `POST` 는 멱등이 보장되지 않는다. 중복을 막을지는 그 API 의 규칙이고, 이 API 의 회원 등록은 막지 않는다. 그래서 프론트가 이중 제출을 막아야 한다.
2. 요청 형식은 올바르고(400 이 아님) 가리키는 자원이 없기 때문이다. 404 는 "대상이 존재하지 않는다" 이다.
3. `returnedAt` 은 키가 항상 오고 값이 `null` 일 수 있다(`=== null` 로 검사). `keyword` 는 키 자체를 생략할 수 있다(`undefined`).
4. 스펙이 불완전한 것이다. 서버는 규칙대로 400 을 냈고, 스펙에 그 응답을 적지 않은 것이 문제다. 백엔드에 알려 스펙을 고치게 한다.
5. 첫 번째 요청이 서버에서 이미 성공했을 가능성이다. 대출 목록을 다시 조회해 현재 상태를 확인한다.

</details>

## 체크리스트

- [ ] 1단계에서 10개 화면 동작의 API 를 예측하고, 책 삭제·수정이 없다는 것을 확인했다
- [ ] 2단계에서 5가지 요청의 1회째·2회째 상태와 서버 데이터를 비교하고, 멱등이 "상태 기준" 이라는 것을 설명할 수 있다
- [ ] `POST /loans` 의 계약(로그인, 요청 필드, 성공, 실패 5가지 이상)을 응답만으로 채웠다
- [ ] 스펙에 없는 응답 4건을 직접 만나 보고, 스펙이 틀릴 수 있다는 것을 안다
- [ ] "항상 있음 / `null` 가능 / 생략 가능" 을 JSON, 스펙, TS 타입에서 구분해서 읽을 수 있다
- [ ] 오류 본문이 두 가지 모양이라는 것과 프론트가 이를 어떻게 처리하는지 설명할 수 있다
- [ ] 오프셋과 커서 방식의 차이를 한 문장씩으로 말할 수 있다

---

## 부록. 실행 검증 기록 (2026-11-01)

이 문서의 명령을 서버에서 위에서부터 순서대로 실행하고, 문서에 적힌 출력과 비교한 기록입니다. 문서의 설명과 정답이 실제 서버 동작과 맞는지 확인하는 것이 목적이었습니다.

### 실행 환경

| 항목 | 값 |
|---|---|
| 실행일 | 2026-11-01 |
| 서버 | `cd labs/backend && ./gradlew :week25:bootRun` (포트 8080, 새로 켜서 데이터가 빈 상태) |
| JDK | OpenJDK 21.0.11 |
| 도구 | `curl`, `jq` |
| 로그인 | `demo` / `demo1234`, 쿠키는 `jar` 파일에 저장 |
| 실행 방법 | 문서의 명령을 같은 순서로 하나의 셸 스크립트에 담아 실행. 2단계에서 만든 `jar`, `H`, `r` 을 3단계 이후에도 그대로 사용 |
| 데이터 상태 | 1단계 → 2단계 → 3단계 → 1절 → 4-3절 → 5절 순서로 이어서 실행해서, 책 id 는 3단계에서 1~5, 4-3절에서 6~8 이 붙음 |

### 결과 요약

| 구간 | 비교한 내용 | 결과 |
|---|---|---|
| 1단계 | `/v3/api-docs` 의 경로·메서드·응답 코드 10줄 | 문서의 정답과 일치 |
| 2단계 | a~e 두 번씩의 상태 코드와 본문, 남은 데이터 | 일치 (시각 값만 다름) |
| 3단계 (1) | 성공 응답과 `Location`, 실패 ①~⑤, 3권 한도 | 일치 (시각 값만 다름) |
| 3단계 (2) | 스펙의 응답 코드, 스펙에 없는 응답 4건 | 일치 (오류 본문의 `timestamp` 만 다름) |
| 1절 | `page`/`size` 무시, `DELETE /books/1` 의 405 | 일치 |
| 4-3절 | 필수 값 누락 3가지, 관대한 입력 5가지 | 일치 |
| 5절 | 오류 표의 각 행, 영어 메시지 | 일치 |
| 연습문제 2 | `BookResponse`, `LoanResponse` 의 `required` | 일치 |

문서의 출력과 달랐던 것은 실행 시각에 따라 달라지는 값(`loanedAt`, `returnedAt`, 오류 본문의 `timestamp`)뿐입니다. 문서가 이미 "시각 값은 실행마다 다릅니다" 라고 적어 두었습니다.

### 1단계. OpenAPI 스펙의 경로 목록

```text
POST /auth/login -> 200,400,401
POST /auth/logout -> 204
GET /auth/me -> 200,401
GET /books -> 200
POST /books -> 201,400,401,409
GET /books/{id} -> 200,400,404
POST /loans -> 201,400,401,404,409
POST /loans/{id}/return -> 200,401,404,409
POST /members -> 201,400,401
GET /members/{id}/loans -> 200,404
```

경로 9개, 메서드 10개입니다. `DELETE`, `PUT`, `PATCH` 는 하나도 없고, 회원 목록과 대출 목록 경로도 없습니다. 문서의 1단계 정답 표(9번과 10번이 "없음")와 같습니다.

### 2단계. 같은 요청을 두 번 보내기

```text
{"username":"demo"}
a GET /books -> [] [200]
a GET /books -> [] [200]
b POST /books -> {"id":1,"isbn":"978-1","title":"클린 코드","author":"로버트 마틴"} [201]
b POST /books -> {"message":"이미 등록된 isbn 입니다: 978-1"} [409]
c POST /members -> {"id":1,"name":"하승"} [201]
c POST /members -> {"id":2,"name":"하승"} [201]
d POST /loans -> {"id":1,"book":{"id":1,"isbn":"978-1","title":"클린 코드","author":"로버트 마틴"},"member":{"id":1,"name":"하승"},"loanedAt":"2026-11-01T00:09:13.059543Z","returnedAt":null} [201]
d POST /loans -> {"message":"이미 대출 중인 책입니다: 1"} [409]
e POST /loans/1/return -> {"id":1,"book":{"id":1,"isbn":"978-1","title":"클린 코드","author":"로버트 마틴"},"member":{"id":1,"name":"하승"},"loanedAt":"2026-11-01T00:09:13.059543Z","returnedAt":"2026-11-01T00:09:13.092755Z"} [200]
e POST /loans/1/return -> {"message":"이미 반납된 대출입니다: 1"} [409]
```

서버에 남은 것:

```text
1
[]
[]
```

| # | 1회째 | 2회째 | 서버 데이터는 1회와 같은가 | 확인 |
|---|---|---|---|---|
| a | 200 | 200 | 같다 | 문서와 일치 |
| b | 201 | 409 | 같다 (책은 `length` 1) | 문서와 일치 |
| c | 201 | 201 | 다르다 (`id` 1, 2 두 명) | 문서와 일치 |
| d | 201 | 409 | 같다 (대출 한 건) | 문서와 일치 |
| e | 200 | 409 | 같다 (반납은 한 번) | 문서와 일치 |

- 두 회원의 `/members/{id}/loans` 가 모두 `[]` 인 것은 `id` 1 대출이 이미 반납됐고, 이 엔드포인트가 반납하지 않은 대출만 돌려주기 때문입니다.

### 3단계 (1). `POST /loans` 계약

성공 응답(`-i` 로 헤더 포함):

```text
HTTP/1.1 201 
Location: /books/2
{"id":2,"isbn":"978-2","title":"리팩터링","author":"마틴 파울러"}
HTTP/1.1 201 
Location: /loans/2
{"id":2,"book":{"id":2,"isbn":"978-2","title":"리팩터링","author":"마틴 파울러"},"member":{"id":1,"name":"하승"},"loanedAt":"2026-11-01T00:09:13.273525Z","returnedAt":null}
```

실패 ①~⑤:

```text
bookId 없음 -> {"message":"bookId: 널이어서는 안됩니다"} [400]
member 99 -> {"message":"회원을 찾을 수 없습니다: 99"} [404]
book 99 -> {"message":"책을 찾을 수 없습니다: 99"} [404]
already -> {"message":"이미 대출 중인 책입니다: 2"} [409]
{"message":"로그인이 필요합니다"} [401]
```

3권 한도:

```text
book3 -> {"id":3,"isbn":"978-3","title":"t3","author":"a"} [201]
book4 -> {"id":4,"isbn":"978-4","title":"t4","author":"a"} [201]
book5 -> {"id":5,"isbn":"978-5","title":"t5","author":"a"} [201]
loan 3 -> {"id":3,"book":{"id":3,"isbn":"978-3","title":"t3","author":"a"},"member":{"id":1,"name":"하승"},"loanedAt":"2026-11-01T00:09:13.391481Z","returnedAt":null} [201]
loan 4 -> {"id":4,"book":{"id":4,"isbn":"978-4","title":"t4","author":"a"},"member":{"id":1,"name":"하승"},"loanedAt":"2026-11-01T00:09:13.405496Z","returnedAt":null} [201]
limit -> {"message":"한 사람은 3권까지 빌릴 수 있습니다"} [409]
```

실패 ③ 과 ④ 는 **회원 `1`, `2` 가 이미 있어야** 의도한 오류가 나옵니다. 이 실행에서는 2단계 c 에서 회원 두 명이 만들어졌기 때문에 ③ 은 책 `99` 가 없다는 404, ④ 는 책 `2` 가 이미 대출 중이라는 409 가 나왔습니다. 2단계를 건너뛰어 회원이 없으면 ③, ④ 에서도 "회원을 찾을 수 없습니다" (404) 가 먼저 나와서 책 쪽 오류를 확인할 수 없습니다.

### 3단계 (2). 스펙과 실제의 차이

```text
["200","404"]
["200","401","404","409"]
{"message":"id: 형식이 올바르지 않습니다"} [400]
{"message":"id: 형식이 올바르지 않습니다"} [400]
{"timestamp":"2026-11-01T00:09:13.484+00:00","status":404,"error":"Not Found","path":"/loans/1"} [404]
{"timestamp":"2026-11-01T00:09:13.496+00:00","status":415,"error":"Unsupported Media Type","path":"/books"} [415]
```

| 호출 | 스펙의 응답 코드 | 실제 | 스펙에 있는가 |
|---|---|---|---|
| `GET /members/abc/loans` | 200, 404 | 400 | 없음 |
| `POST /loans/abc/return` | 200, 401, 404, 409 | 400 | 없음 |
| `DELETE /loans/1` | (경로 없음) | 404 | 없음 |
| `POST /books` (Content-Type 없이) | 201, 400, 401, 409 | 415 | 없음 |

스펙에 없는 응답은 4건이고, 마지막 두 건의 본문에는 `message` 키가 없습니다.

### 1절. 리소스와 URL

```text
5
5
```

`GET /books` 와 `GET /books?page=2&size=1` 이 모두 5권을 돌려줍니다. 모르는 쿼리는 에러 없이 무시됩니다. 참고로 4-3절 이후(책 8권)에 실행한 `GET /books?keyword=t` 는 제목에 `t` 가 들어간 5권(`t3`, `t4`, `t5`, `t`, `t`)만 돌려줬습니다. `keyword` 가 제목을 거릅니다.

```text
HTTP/1.1 405 
Allow: GET
```

`DELETE /books/1` 은 405 와 `Allow: GET` 입니다.

### 4-3절. 서버에 보내는 JSON

```text
null -> {"message":"title: 공백일 수 없습니다"} [400]
blank -> {"message":"title: 공백일 수 없습니다"} [400]
missing -> {"message":"title: 공백일 수 없습니다"} [400]
extra -> {"id":6,"isbn":"978-9","title":"t","author":"a"} [201]
id -> {"id":7,"isbn":"978-10","title":"t","author":"a"} [201]
title 123 -> {"id":8,"isbn":"978-11","title":"123","author":"a"} [201]
memberId str -> {"id":5,"book":{"id":5,"isbn":"978-5","title":"t5","author":"a"},"member":{"id":2,"name":"하승"},"loanedAt":"2026-11-01T00:09:23.291950Z","returnedAt":null} [201]
memberId abc -> {"message":"요청 본문을 읽을 수 없습니다 (JSON 형식 확인)"} [400]
```

- 요청에 `id` 99 를 보냈지만 서버가 7 을 붙였습니다. 모르는 필드 `pages` 는 조용히 버려졌습니다.
- `"title":123` 은 `"123"` 으로, `"memberId":"2"` 는 숫자 `2` 로 변환됐습니다.
- 문서의 출력과 `id` 까지 모두 같습니다.

### 5절. 오류 형식

문서의 오류 표 각 행을 다시 호출한 결과입니다.

| 상황 | 요청 | 코드 | 본문 |
|---|---|---|---|
| 필드 여럿이 비어 있음 | `POST /books` `{"isbn":"","title":"","author":""}` | 400 | `{"message":"author: 공백일 수 없습니다, isbn: 공백일 수 없습니다, title: 공백일 수 없습니다"}` |
| 깨진 JSON | `POST /books` `{bad` | 400 | `{"message":"요청 본문을 읽을 수 없습니다 (JSON 형식 확인)"}` |
| 빈 본문 | `POST /books` (본문 없음, JSON 헤더만) | 400 | 위와 같음 |
| 경로 변수가 숫자 아님 | `GET /books/abc` | 400 | `{"message":"id: 형식이 올바르지 않습니다"}` |
| 로그인 실패 | `POST /auth/login` 비밀번호 틀림 | 401 | `{"message":"아이디 또는 비밀번호가 올바르지 않습니다"}` |
| 로그아웃 후 `GET /auth/me` | | 401 | `{"message":"로그인이 필요합니다"}` |
| 책 없음 | `GET /books/999` | 404 | `{"message":"책을 찾을 수 없습니다: 999"}` |
| 회원 없음 | `GET /members/99/loans` | 404 | `{"message":"회원을 찾을 수 없습니다: 99"}` |
| 로그아웃 | `POST /auth/logout` | 204 | (본문 없음) |
| 없는 경로 | `GET /nope` | 404 | `{"timestamp":"2026-11-01T00:09:23.506+00:00","status":404,"error":"Not Found","path":"/nope"}` |
| 안 되는 메서드 | `PUT /books/1` | 405 | `{"timestamp":"2026-11-01T00:09:23.516+00:00","status":405,"error":"Method Not Allowed","path":"/books/1"}` |

- 400 검증 메시지는 `필드: 이유` 를 쉼표로 이었고, 필드가 알파벳순(`author`, `isbn`, `title`)으로 나옵니다.
- 404, 405 의 프레임워크 본문에는 `message` 키가 없습니다.

**`Accept-Language`**: 로그인한 뒤 `Accept-Language: en` 으로 `title` 이 빠진 `POST /books` 를 보내면 `{"message":"title: must not be blank"}` (400) 이 옵니다. 같은 요청의 한국어 메시지는 `title: 공백일 수 없습니다` 입니다. 문구는 언어에 따라 바뀌고 `필드: 이유` 형식은 그대로입니다.

**주의**: 이 확인을 `POST /auth/logout` 직후에 하면 세션이 끊겨서 `{"message":"로그인이 필요합니다"}` (401) 이 옵니다. 로그아웃 후에는 다시 로그인해서 새 쿠키를 받아야 합니다.

### 연습문제 2. 스펙의 `required`

```text
{"required":["author","id","isbn","title"]}
{"required":["book","id","loanedAt","member","returnedAt"],"r":{"type":["string","null"],"format":"date-time","description":"반납 전에는 null"}}
```

- `BookResponse` 의 네 필드(`id`, `isbn`, `title`, `author`)는 모두 `required` 에 있어 "항상 있다" 로 분류됩니다.
- `LoanResponse.returnedAt` 은 `required` 에 있으면서 `type` 이 `["string","null"]` 이라서 "키는 있고 값이 `null`" 모양입니다. 4-2절 표와 같습니다.

### 추가로 확인한 것

- `POST /members` 의 성공 응답에 `Location: /members/{id}` 가 붙습니다 (이 실행에서는 `/members/3`). 3절 표의 "201 + `Location`" 과 같습니다.
- `PATCH /books/1` 은 `PUT` 과 마찬가지로 405 (`Method Not Allowed`) 입니다. 1단계 해설 10번과 2절은 "직접 확인한 것은 `DELETE` 뿐" 이라고 적고 있는데, `PUT` 과 `PATCH` 도 같은 경로에서 405 인 것이 이번에 확인됐습니다.

### 결론

- 문서의 명령과 출력, 해설이 실제 서버 동작과 어긋나는 곳은 없었습니다.
- 서버를 새로 켠 뒤 위에서부터 순서대로 실행해야 `id` 와 개수가 문서와 같아집니다. 순서를 건너뛰면 3단계의 실패 ③·④ 처럼 다른 오류가 먼저 나올 수 있습니다.
