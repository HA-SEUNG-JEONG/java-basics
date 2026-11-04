# 1주차. HTTP·DevTools·curl

**예상 시간:** 6~8시간 (하루 1시간 세션 7회) | **목표:** 브라우저 네트워크 탭과 `curl` 로 HTTP 요청·응답의 메서드, URL, 헤더, 본문, 상태 코드를 읽는다. 화면이 이상할 때 "프론트 문제인가, 서버 문제인가" 를 같은 요청을 `curl` 로 재현해서 가려낼 수 있게 된다.

> 이 과정은 Java 문법을 배우는 과정이 아닙니다. React·TypeScript·Next 프론트엔드 개발자가 **백엔드와 같은 말로 협업하고, 로컬에서 같이 띄워 디버깅**하는 것이 목표입니다. 1주에는 백엔드 코드를 한 줄도 읽지 않습니다. 서버를 "백엔드 팀이 준 API" 로 보고 요청과 응답만 관찰합니다.
>
> 실습 대상은 도서관 참조 API `../../labs/backend/week25` 입니다. Spring Boot 로 만들어져 있고 8080 포트에서 동작하며, 데이터는 H2 메모리 DB 에 있어서 서버를 껐다 켜면 비어 있습니다. 데모 로그인은 `demo` / `demo1234` 입니다.
>
> 준비물: JDK 21, `curl`, `jq`, Chrome 계열 브라우저 (Edge, Firefox 도 가능). 프론트 개발 서버 (`../../labs/frontend`)는 1주에 쓰지 않습니다. 명령은 별도 언급이 없으면 저장소 루트에서 실행합니다.

## 하루 1시간으로 나누면

이 주차는 4단계가 모두 필수라서 하루 1시간씩 7번의 세션으로 나눕니다. 한 세션은 50~70분이고, 1~2시간이 걸리는 4단계는 안의 번호로 둘로 나눕니다.

| 세션 | 할 일 | 끝났을 때 할 수 있어야 하는 것 |
|---|---|---|
| 1 | 1단계(예측하고 → 호출하고 → 비교하기, 30분)를 하고, 틀린 예측의 이유를 1절(HTTP 메시지는 텍스트다)에서 찾아 읽는다 | `curl` 출력에서 요청 한 건의 메서드·URL·헤더·본문·상태 코드를 짚을 수 있다 |
| 2 | 2단계(일부러 망가뜨리기, 1시간)를 한다 | `Content-Type` 누락·쿠키 누락·깨진 JSON·필드 누락이 각각 어떤 상태 코드로 오는지 말할 수 있다 |
| 3 | 3단계(빈칸에서 만들기 — fetch 로 읽기, 1~2시간)를 한다. 60~70분 안에 못 끝나면 같은 단계에서 이어서 한다 | 1단계의 `curl` 이 한 일을 `fetch` 로 재현하고, `fetch` 가 4xx 에서 예외를 던지지 않는 이유와 던지는 경우를 설명할 수 있다 |
| 4 | 2절(메서드와 상태 코드), 3절(URL 과 쿼리스트링), 4절(fetch 가 숨기는 것, 보여 주는 것)을 읽고, 4단계(브라우저 DevTools 로 같은 요청 읽기, 1~2시간)의 1~4번(Swagger UI 열기, DevTools 열기, `GET /books`, 없는 책 조회)을 한다 | Network 탭에서 요청의 URL·메서드·상태 코드·응답 헤더·본문을 찾고, 404 가 브라우저에는 빨갛게 보여도 `fetch` 입장에서는 응답을 받은 성공이라는 것을 설명할 수 있다 |
| 5 | 4단계의 5~8번(`Set-Cookie`, `Cookie` 요청 헤더, Console 의 `fetch`, `Copy as cURL`)을 하고, 5절(쿠키와 세션 (맛보기))과 6절(DevTools Network 탭 읽는 법), 7절("화면이 이상하다" 를 가르는 순서)을 읽는다 | `Copy as cURL` 로 브라우저의 요청을 터미널에서 재현하고, "화면이 이상하다" 를 가르는 다섯 항목의 순서를 말할 수 있다 |
| 6 | 연습문제와 퀴즈를 푼다 (압축 코스에서는 건너뜀) | 연습문제 5개를 `curl` 과 DevTools 로 직접 해 보고, 퀴즈의 정답과 대조해 틀린 이유를 말할 수 있다 |
| 7 | 체크리스트의 각 항목을 내 말로 설명해 보고, 막히는 항목은 해당 절을 다시 읽은 뒤 학습 노트를 정리한다 | 같은 요청을 `curl` 로 재현해 프론트 문제인지 서버 문제인지 가를 수 있다 |

세션을 시작하기 전에 이전 세션의 마지막 결과를 1~2분 훑어봅니다. 막히면 아래 해당 개념 절을 사전처럼 펼쳐 보세요.

## 🖐 먼저 손으로 (읽기 전에 하세요)

> 이 주차의 핵심은 아래 단계를 **직접 해 보면서** 깨닫는 것입니다. 아래쪽 설명 문서는 먼저 읽지 않아도 됩니다. 막혔을 때 사전처럼 펼쳐 보세요.

### 1단계. 예측하고 → 호출하고 → 비교하기 (30분)

먼저 서버를 켜기 **전에**, 아래 요청 8개의 결과를 노트에 적으세요. 요청은 서버를 새로 켠 직후, 위에서 아래로 한 번씩만 보냅니다.

| # | 요청 | 상태 코드 | 응답 `Content-Type` |
|---|------|------|------|
| 1 | `GET /books` | | |
| 2 | `GET /books/1` (등록한 책이 하나도 없음) | | |
| 3 | `GET /books/abc` | | |
| 4 | `POST /books` (JSON 본문, 로그인 안 함) | | |
| 5 | `DELETE /books/1` | | |
| 6 | `GET /nope` (이런 경로는 없음) | | |
| 7 | `GET /v3/api-docs` | | |
| 8 | `GET /swagger-ui.html` | | |

그리고 2, 5, 6번은 하나 더 예측합니다. **응답 본문에 `message` 키가 있을까요?**

예측을 적었으면 서버를 켭니다. 8080 이 비어 있어야 합니다.

```bash
lsof -i :8080                      # 아무것도 안 나와야 함
cd labs/backend
./gradlew :week25:bootRun          # 한 터미널에서 (Ctrl+C 로 종료)
```

로그 끝에 `Started LibraryApplication in ... seconds` 가 보이면 준비된 것입니다.

다른 터미널에서 같은 요청을 실제로 보냅니다.

```bash
B=localhost:8080
c() { printf '%s -> ' "$1"; shift; curl -s -o /dev/null -w '%{http_code} %{content_type}\n' "$@"; }
c 1 $B/books
c 2 $B/books/1
c 3 $B/books/abc
c 4 -X POST -H 'Content-Type: application/json' -d '{"isbn":"1","title":"t","author":"a"}' $B/books
c 5 -X DELETE $B/books/1
c 6 $B/nope
c 7 $B/v3/api-docs
c 8 $B/swagger-ui.html
```

본문의 키는 이렇게 봅니다.

```bash
curl -s $B/books/1 | jq -c 'keys'
curl -s -X DELETE $B/books/1 | jq -c 'keys'
curl -s $B/nope | jq -c 'keys'
```

<details>
<summary>정답 (예측을 적은 뒤에 여세요)</summary>

```text
1 -> 200 application/json
2 -> 404 application/json
3 -> 400 application/json
4 -> 401 application/json
5 -> 405 application/json
6 -> 404 application/json
7 -> 200 application/json
8 -> 302 
```

```text
["message"]
["error","path","status","timestamp"]
["error","path","status","timestamp"]
```

- 1번: 책이 없어도 목록 조회는 성공입니다. 본문은 `[]` 입니다.
- 2번과 3번: 같은 `/books/{id}` 인데 숫자가 아닌 `abc` 는 400, 숫자지만 없는 책은 404 입니다. "요청 형식이 틀렸다" 와 "그런 자원이 없다" 는 다른 상태 코드입니다.
- 4번: 본문이 올바른 JSON 이어도 로그인하지 않았으면 401 입니다. 쓰기 요청은 로그인이 필요합니다.
- 5번: 이 API 는 `/books/{id}` 에 `GET` 만 만들어 두었습니다. `DELETE` 는 405 이고 응답 헤더 `Allow: GET` 이 "쓸 수 있는 메서드" 를 알려 줍니다.
- 6번: 서버가 모르는 경로는 404 입니다.
- 8번: 302 는 오류가 아니라 **다른 주소로 가라**는 응답입니다. `Location: /swagger-ui/index.html` 헤더에 갈 곳이 있습니다. `Content-Type` 이 비어 있는 것도 본문이 없기 때문입니다.
- 2번과 5·6번의 본문 모양이 다릅니다. 2번은 `{"message": "..."}` 이고, 5·6번은 `timestamp`, `status`, `error`, `path` 입니다. **같은 서버가 오류 본문을 두 가지 모양으로 줍니다.** 앞의 것은 이 API 가 직접 정한 오류 형식이고, 뒤의 것은 프레임워크(Spring)가 기본으로 만든 것입니다. 이 차이는 2주에서 계약 문제로 다시 다루고, 7주에서 프론트 오류 처리로 이어집니다.

</details>

### 2단계. 일부러 망가뜨리기 (1시간)

정상 요청을 하나 만들고, 한 군데씩 일부러 틀리게 보냅니다. 먼저 로그인해서 쿠키를 파일에 저장합니다. (`mktemp` 는 임시 파일 이름을 만들어 줍니다.)

```bash
B=localhost:8080
jar=$(mktemp)
curl -s -c "$jar" -X POST -H 'Content-Type: application/json' -d '{"username":"demo","password":"demo1234"}' $B/auth/login; echo
```

아래 a~h 를 보내기 전에, 각각의 **상태 코드와 응답 본문(`message` 가 있다면 그 내용)** 을 노트에 적으세요.

| # | 무엇을 망가뜨렸나 | 내 예측 |
|---|------|------|
| a | `POST /books` 에서 `Content-Type` 헤더를 뺀다 (`-d` 만 쓴다) | 잘 모르겠음 |
| b | 같은 요청에서 쿠키를 뺀다 (`-b "$jar"` 를 쓰지 않는다) | 401 | -> {message :  '로그인이 필요합니다.'}
| c | JSON 의 닫는 중괄호를 뺀다 | 404  | -> {"message":"요청 본문을 읽을 수 없습니다 (JSON 형식 확인)"} [400]
| d | 필수 필드 `title`, `author` 를 뺀다 | 500 | -> {"message":"author: 공백일 수 없습니다, title: 공백일 수 없습니다"} [400]
| e | `GET /auth/login` (로그인은 `POST` 인데 `GET` 으로 부른다) | 405 | -> HTTP/1.1 405 
Allow: POST
| f | 비밀번호를 틀리게 로그인한다 | 401, 아이디나 비밀번호가 틀림 | -> {"message":"아이디 또는 비밀번호가 올바르지 않습니다"} [401]
| g | d 와 같은 요청에 `Accept-Language: en` 헤더를 더한다 | 잘 모르겠음 | {"message":"author: must not be blank, title: must not be blank"} [400]
| h | 모두 바로잡은 정상 요청 | 200 | -> {"id":1,"isbn":"1","title":"t","author":"a"} [201]

```bash
echo '--- a'; curl -s -w ' [%{http_code}]\n' -b "$jar" -X POST -d '{"isbn":"1","title":"t","author":"a"}' $B/books
echo '--- b'; curl -s -w ' [%{http_code}]\n' -X POST -H 'Content-Type: application/json' -d '{"isbn":"1","title":"t","author":"a"}' $B/books
echo '--- c'; curl -s -w ' [%{http_code}]\n' -b "$jar" -X POST -H 'Content-Type: application/json' -d '{"isbn":"1","title":"t"' $B/books
echo '--- d'; curl -s -w ' [%{http_code}]\n' -b "$jar" -X POST -H 'Content-Type: application/json' -d '{"isbn":"1"}' $B/books
echo '--- e'; curl -s -i $B/auth/login | sed -n '1p;/^Allow/p'
echo '--- f'; curl -s -w ' [%{http_code}]\n' -X POST -H 'Content-Type: application/json' -d '{"username":"demo","password":"nope"}' $B/auth/login
echo '--- g'; curl -s -w ' [%{http_code}]\n' -b "$jar" -H 'Accept-Language: en' -X POST -H 'Content-Type: application/json' -d '{"isbn":"1"}' $B/books
echo '--- h'; curl -s -w ' [%{http_code}]\n' -b "$jar" -X POST -H 'Content-Type: application/json' -d '{"isbn":"1","title":"t","author":"a"}' $B/books
```

마지막으로 서버에 아예 닿지 못하는 경우도 봅니다. 8099 포트에는 아무것도 떠 있지 않습니다.

```bash
curl -sS localhost:8099/books; echo "exit=$?"
```

<details>
<summary>실제 결과 (예측을 적은 뒤에 여세요)</summary>

```text
--- a
{"timestamp":"2026-10-22T13:45:49.608+00:00","status":415,"error":"Unsupported Media Type","path":"/books"} [415]
--- b
{"message":"로그인이 필요합니다"} [401]
--- c
{"message":"요청 본문을 읽을 수 없습니다 (JSON 형식 확인)"} [400]
--- d
{"message":"author: 공백일 수 없습니다, title: 공백일 수 없습니다"} [400]
--- e
HTTP/1.1 405 
Allow: POST
--- f
{"message":"아이디 또는 비밀번호가 올바르지 않습니다"} [401]
--- g
{"message":"author: must not be blank, title: must not be blank"} [400]
--- h
{"id":1,"isbn":"1","title":"t","author":"a"} [201]
```

```text
curl: (7) Failed to connect to localhost port 8099 after 0 ms: Couldn't connect to server
exit=7
```

(a 의 `timestamp` 와 마지막 줄의 `0 ms` 는 실행마다 다릅니다. 한국어 검증 메시지는 한국어 환경에서 `Accept-Language` 없이 보낸 결과입니다. 헤더를 주지 않았을 때의 언어는 서버를 실행한 환경에 따라 다를 수 있습니다.)

- a: `curl -d` 는 `Content-Type` 을 지정하지 않으면 `application/x-www-form-urlencoded` 를 자동으로 붙입니다. 서버는 JSON 만 받으므로 415 입니다. 본문은 앞에서 본 프레임워크 모양입니다.
- b 와 f: 둘 다 401 이지만 원인이 다릅니다. 메시지가 "로그인이 필요합니다" 인지 "아이디 또는 비밀번호가 올바르지 않습니다" 인지로 구분됩니다. 상태 코드만 보고 분기하면 이 둘을 구별하지 못합니다.
- c 와 d: 둘 다 400 입니다. d 의 메시지는 `필드: 이유` 를 쉼표로 이어 붙인 한 문자열입니다. 필드 이름이 알파벳순으로 나옵니다.
- e: 405 와 `Allow: POST`. 메서드를 틀렸을 때 서버가 올바른 메서드를 알려 줍니다.
- g: **같은 오류인데 `Accept-Language` 에 따라 메시지 문구가 한국어에서 영어로 바뀝니다.** 화면이 문구를 `===` 로 비교하면 언어 설정에 따라 깨집니다. 문구가 아니라 상태 코드와 `필드: ` 형식에 기대야 합니다.
- h: 201 입니다. 이 책이 서버 DB 에 들어갔으므로, 같은 요청을 다시 보내면 409 가 됩니다. (2주에서 직접 확인합니다.)
- 8099: HTTP 응답 자체가 없습니다. 그래서 **상태 코드가 없고** `curl` 은 종료 코드 7 로 끝납니다. 브라우저에서는 이 경우 `fetch` 가 오류로 거부(reject)됩니다. 3단계에서 직접 봅니다.

</details>

### 3단계. 빈칸에서 만들기 — fetch 로 읽기 (1~2시간)

1단계의 `curl` 이 한 일을 `fetch` 로 합니다. 임시 폴더에 `scratch.mjs` 파일을 만들고 `____` 를 채우세요. (Node 22 에는 `fetch` 가 들어 있습니다. 저장소 안에 파일을 만들지 않습니다.)

```js
const B = "http://localhost:8080";

// (1) 없는 책 조회
const res = await fetch(`${B}/books/999`);
console.log(res.status, res.ok);                  // 상태 코드, 성공 여부(boolean)
console.log(res.headers.get("content-type"));    // 응답 헤더 한 개 읽기
const body = await res.json();                    // 본문을 JSON 으로
console.log(body.message);                           // 오류 문구

// (2) 아무것도 안 떠 있는 포트
try {
  await fetch("http://localhost:8099/books");
} catch (e) {
  console.log(e.name, e.message);
}
```

서버를 켠 채로 `node scratch.mjs` 를 실행합니다. (2) 에서 `catch` 로 들어가는지 여부를 먼저 예측하세요. 그리고 `res.status` 가 404 일 때 `await fetch(...)` 줄 자체는 예외를 던질까요?

<details>
<summary>정답</summary>

```js
console.log(res.status, res.ok);
console.log(res.headers.get("content-type"));
const body = await res.json();
console.log(body.message);
```

```text
404 false
application/json
책을 찾을 수 없습니다: 999
TypeError fetch failed
```

- **`fetch` 는 404, 400, 500 에서도 예외를 던지지 않습니다.** 응답을 받았으면 `await fetch(...)` 는 성공하고, `res.ok`(상태 코드 200~299 여부)로 직접 판단해야 합니다. 예외(reject)는 8099 처럼 응답 자체를 받지 못했을 때만 납니다.
- 마지막 줄의 `fetch failed` 는 Node 의 문구입니다. 브라우저에서는 문구가 다르지만(예: `Failed to fetch`) 같은 `TypeError` 로 거부됩니다. 문구에 기대는 코드는 쓰지 않습니다.
- `res.json()` 은 본문이 비어 있으면 실패합니다. 이 API 의 `POST /auth/logout` 은 204 와 빈 본문을 돌려주므로, 그 응답에 `res.json()` 을 부르면 `SyntaxError: Unexpected end of JSON input` 입니다. 본문이 있는지는 상태 코드(204)가 알려 줍니다.

</details>

### 4단계. 브라우저 DevTools 로 같은 요청 읽기 (1~2시간)

여기서부터는 브라우저에서 직접 합니다. 프론트 개발 서버를 켜지 않아도 백엔드가 Swagger UI 라는 화면을 직접 제공하므로, 그 화면에서 보낸 요청을 DevTools 로 관찰합니다. 서버는 켜 둔 상태여야 합니다.

1. 브라우저로 `http://localhost:8080/swagger-ui.html` 을 엽니다. 주소가 `/swagger-ui/index.html` 로 바뀝니다. 1단계 8번의 302 가 바로 이것입니다.
2. DevTools 를 엽니다. (Windows/Linux `F12`, macOS `Cmd+Option+I`) **Network** 탭에서 `Preserve log` 를 켜고, 필터는 `Fetch/XHR` 로 둡니다.
3. 화면에서 `GET /books` 를 펼치고 `Try it out` → `Execute`. Network 탭에 `books` 요청이 하나 생깁니다. 클릭해서 다음을 노트에 적으세요.
   - `Headers` 탭의 General 에서 Request URL, Request Method, Status Code.
   - Response Headers 에서 `Content-Type`. 1단계에서 `curl -i` 로 본 헤더 중 무엇이 보이는지.
   - `Response`(또는 `Preview`) 탭의 본문.
4. 같은 방법으로 `GET /books/{id}` 에 `id` 로 `999` 를 넣고 실행합니다. 상태 코드가 빨간색으로 표시되는지, 본문은 무엇인지 적으세요.
5. `POST /auth/login` 을 실행합니다. 본문은 `{"username": "demo", "password": "demo1234"}` 로 고칩니다. 그리고 `Headers` 탭의 Response Headers 에서 `Set-Cookie` 를 찾으세요. **`HttpOnly` 가 붙어 있는지** 확인합니다.
6. 이어서 `GET /auth/me` 를 실행하고, 이번에는 **Request Headers** 에 `Cookie` 가 있는지 봅니다. 값은 방금 `Set-Cookie` 로 받은 것과 같아야 합니다. 브라우저가 알아서 실어 보낸 것입니다.
7. Console 탭에서 직접 `fetch` 를 부릅니다. 같은 출처(origin)의 화면이므로 경로만 적어도 됩니다.

   ```js
   const res = await fetch("/books/999");
   [res.status, res.ok, await res.json()]
   ```

   그리고 `document.cookie` 를 입력해 보세요. 로그인했는데도 `JSESSIONID` 가 보일까요?
8. Network 탭에서 3번의 `books` 요청을 우클릭 → `Copy` → `Copy as cURL`. 복사한 명령을 터미널에 붙여 넣어 실행하고, 브라우저가 본 응답과 같은지 비교합니다. 이 방법이 "브라우저에서 이상한 요청을 터미널로 옮기는" 기본 동작입니다.

<details>
<summary>무엇이 보여야 하는가</summary>

서버 쪽 사실은 모두 위에서 `curl` 로 확인한 것과 같습니다. 브라우저 화면의 표시 방식은 브라우저와 버전에 따라 조금 다를 수 있습니다.

- 3번: `GET`, `200`, `Content-Type: application/json`, 본문은 지금까지 등록한 책의 배열입니다. 2단계 h 에서 등록한 책 한 권이 들어 있고, 그 사이에 서버를 껐다 켰다면 `[]` 입니다.
- 4번: 404 와 `{"message":"책을 찾을 수 없습니다: 999"}`. 브라우저가 4xx 를 실패로 표시해도, `fetch` 입장에서는 응답을 받은 성공입니다.
- 5번: `Set-Cookie: JSESSIONID=...; Path=/; HttpOnly; SameSite=Lax` 형태입니다. 값은 로그인할 때마다 달라집니다.
- 6번: 요청 헤더에 `Cookie: JSESSIONID=...` 가 실립니다. 응답은 `{"username":"demo"}`.
- 7번: `HttpOnly` 쿠키는 자바스크립트가 읽지 못하므로 `document.cookie` 에 `JSESSIONID` 가 나오지 않습니다. 쿠키는 **Application** 탭의 Cookies 에서만 확인할 수 있습니다. 이것이 세션 쿠키가 XSS 로 훔쳐지지 않게 하는 장치입니다. 8주에서 자세히 다룹니다.
- 8번: 복사된 명령에는 브라우저가 붙인 헤더가 많이 들어 있지만(`User-Agent`, `Accept-Language` 등) 상태 코드와 본문은 같습니다.

</details>

---

## 1. HTTP 메시지는 텍스트다

HTTP 는 요청 하나, 응답 하나로 이루어진 텍스트 규약입니다. 먼저 응답 전체를 봅니다. 서버를 새로 켜서 책이 하나도 없을 때 `curl -i` 를 보낸 결과입니다. (`-i` 는 응답 헤더를 본문 앞에 함께 찍습니다.)

```bash
curl -i localhost:8080/books/1
```

```text
HTTP/1.1 404 
Vary: Origin
Vary: Access-Control-Request-Method
Vary: Access-Control-Request-Headers
X-Content-Type-Options: nosniff
X-XSS-Protection: 0
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
Pragma: no-cache
Expires: 0
X-Frame-Options: DENY
Content-Type: application/json
Transfer-Encoding: chunked
Date: Thu, 22 Oct 2026 13:43:29 GMT

{"message":"책을 찾을 수 없습니다: 1"}
```

- 첫 줄은 **상태 줄**입니다. 프로토콜, 상태 코드(404), 사유 문구입니다. 이 서버는 사유 문구를 비워 두므로 `HTTP/1.1 404 ` 만 나옵니다. 코드 숫자만 믿으면 됩니다.
- 빈 줄 앞까지가 **헤더**(`이름: 값`)입니다. 빈 줄 뒤가 **본문**입니다.
- `Content-Type: application/json` 은 본문을 어떻게 읽을지 알려 줍니다.
- `Transfer-Encoding: chunked` 는 본문 길이를 미리 모르고 조각으로 보낸다는 뜻입니다. `Content-Length` 가 없는 것이 이상한 일이 아닙니다.
- `Vary`, `X-Content-Type-Options`, `X-XSS-Protection`, `Cache-Control`, `Pragma`, `Expires`, `X-Frame-Options` 는 백엔드 보안 설정(Spring Security)이 자동으로 붙이는 헤더입니다. 이 문서의 이후 출력에서는 읽기 쉽게 생략합니다. 프론트에 영향이 있는 것은 `Cache-Control: no-cache, no-store, ...` 입니다. 이 API 의 응답은 브라우저가 캐시하지 않습니다.
- `Date` 값은 실행한 시각입니다.

요청 쪽은 `-v` 로 봅니다. `>` 로 시작하는 줄이 보낸 것, `<` 가 받은 것입니다.

```bash
curl -sv -o /dev/null "localhost:8080/books?keyword=clean"
```

```text
> GET /books?keyword=clean HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/8.7.1
> Accept: */*
> 
< HTTP/1.1 200 
< Content-Type: application/json
< Transfer-Encoding: chunked
< Date: Thu, 22 Oct 2026 13:43:58 GMT
< 
```

(`-v` 의 `*` 로 시작하는 연결 정보와, 위에서 설명한 보안 헤더는 생략했습니다. `User-Agent` 의 버전은 설치된 `curl` 에 따라 다릅니다.)

요청은 **요청 줄**(메서드 + 경로·쿼리 + 프로토콜), 헤더, (있다면) 본문으로 이루어집니다. `Host` 는 어느 서버에게 보내는지, `Accept` 는 어떤 응답을 받고 싶은지입니다.

**TS 와 비교** (백엔드·HTTP 쪽 용어를 프론트의 익숙한 것에 대응시킨 표입니다. 완전히 같지는 않습니다.)

| HTTP / 백엔드 쪽 | 프론트에서 만나는 모습 |
|---|---|
| 요청 줄의 메서드 | `fetch(url, { method: "POST" })` |
| 요청 헤더 | `fetch(url, { headers: { "Content-Type": "application/json" } })` |
| 요청 본문 | `fetch(url, { body: JSON.stringify(data) })`. 문자열이므로 직접 직렬화한다 |
| 상태 코드 | `res.status`, `res.ok` |
| 응답 헤더 | `res.headers.get("content-type")` |
| 응답 본문 | `await res.json()` 또는 `await res.text()` |
| 쿼리스트링 | `new URLSearchParams({ keyword: "클린" })` |
| 연결 실패(응답 없음) | `fetch` 가 `TypeError` 로 reject |
| `Set-Cookie` 의 `HttpOnly` | `document.cookie` 에 보이지 않는 쿠키 |

## 2. 메서드와 상태 코드

메서드는 "무엇을 하려는가", 상태 코드는 "어떻게 됐는가" 입니다. 이 API 에서 실제로 본 것들입니다.

| 코드 | 이 API 에서 만나는 경우 | 확인한 요청 |
|---|---|---|
| 200 | 조회, 로그인, 반납 성공 | `GET /books` |
| 201 | 생성 성공. `Location` 헤더에 새 자원의 경로 | `POST /books` (로그인 후) |
| 204 | 성공했지만 본문이 없음 | `POST /auth/logout` |
| 302 | 다른 주소로 이동 | `GET /swagger-ui.html` |
| 400 | 요청 형식이 틀림 (깨진 JSON, 필수 값 누락, `id` 가 숫자가 아님) | 2단계 c·d, 1단계 3번 |
| 401 | 로그인 필요, 또는 아이디·비밀번호 틀림 | 2단계 b·f |
| 404 | 그런 자원·경로가 없음 | 1단계 2번·6번 |
| 405 | 그 경로에서 쓸 수 없는 메서드. `Allow` 헤더가 알려 줌 | 1단계 5번, 2단계 e |
| 409 | 요청은 올바른데 서버의 현재 상태와 충돌 (중복 isbn 등) | 2주에서 직접 확인 |
| 415 | 서버가 받을 수 없는 `Content-Type` | 2단계 a |

- **4xx 는 "요청을 고쳐서 다시 보내라", 5xx 는 "서버가 고장 났다"** 입니다. 이번 실습에서는 5xx 가 나오지 않습니다. 5xx 를 만나면 요청과 응답 본문을 그대로 백엔드에 전달하세요. 프론트가 고칠 수 있는 문제가 아닙니다.
- 401 과 404 처럼 한 코드가 여러 원인을 가집니다. 코드만이 아니라 `message` 도 같이 봅니다.
- 201 의 `Location` 헤더는 2주에서 `curl -i` 로 직접 확인합니다.

## 3. URL 과 쿼리스트링

```text
http://localhost:8080/books?keyword=클린
└──────┬──────────┘└─┬──┘└───┬────────┘
     origin         경로   쿼리스트링
```

- **origin** = 프로토콜 + 호스트 + 포트(`http://localhost:8080`). 이 셋 중 하나라도 다르면 다른 origin 입니다. 브라우저의 보안 규칙(CORS, 쿠키)이 이 단위로 동작합니다. 8주에서 다룹니다.
- 쿼리스트링에 한글 같은 문자를 넣으려면 인코딩해야 합니다. 브라우저와 `URLSearchParams` 는 자동으로 합니다. `curl` 에서는 `--get --data-urlencode` 가 같은 일을 합니다.

```bash
curl -s --get --data-urlencode 'keyword=클린' localhost:8080/books
```

`'클린'` 이 `%ED%81%B4%EB%A6%B0` 으로 바뀌어 전달됩니다. Node 에서 확인합니다.

```bash
node -e 'console.log(new URLSearchParams({keyword:"클린"}).toString())'
```

```text
keyword=%ED%81%B4%EB%A6%B0
```

이 API 의 `GET /books` 는 `keyword` 가 있으면 제목에 그 글자가 포함된 책만(영문은 대소문자 무시) 돌려주고, 없거나 빈 문자열이면 전부 돌려줍니다.

## 4. fetch 가 숨기는 것, 보여 주는 것

3단계에서 본 것을 정리합니다.

| 상황 | `fetch` 의 동작 |
|---|---|
| 200~299 | resolve, `res.ok === true` |
| 400·401·404·409 등 4xx, 5xx | **resolve**, `res.ok === false`. 직접 확인해야 한다 |
| 서버에 닿지 못함, DNS 실패, 연결 거부 | reject (`TypeError`) |
| 204 의 `res.json()` | 본문이 비어 있어 `SyntaxError`. 상태 코드로 먼저 걸러야 한다 |

그래서 프론트 코드는 "예외가 안 났으니 성공" 이 아니라 `res.ok` 를 확인합니다. 이 저장소의 `../../labs/frontend/src/api/client.ts` 의 `unwrap` 함수가 바로 그 일을 하는 곳입니다. 지금은 열어 보지 않아도 되고, 6주와 7주에 다시 만납니다.

## 5. 쿠키와 세션 (맛보기)

로그인은 "쿠키를 받아서, 이후 요청마다 돌려보내는" 일입니다. 서버가 `Set-Cookie` 로 주고, 브라우저가 이후 요청의 `Cookie` 헤더에 자동으로 싣습니다.

```bash
jar=$(mktemp)
curl -s -i -c "$jar" -X POST -H 'Content-Type: application/json' -d '{"username":"demo","password":"demo1234"}' localhost:8080/auth/login
cat "$jar"
```

```text
HTTP/1.1 200 
Set-Cookie: JSESSIONID=77C1B93F8AD4CE679250E3D64EAE8EB8; Path=/; HttpOnly; SameSite=Lax
Content-Type: application/json
Transfer-Encoding: chunked
Date: Thu, 22 Oct 2026 13:43:40 GMT

{"username":"demo"}
```

```text
# Netscape HTTP Cookie File
# https://curl.se/docs/http-cookies.html
# This file was generated by libcurl! Edit at your own risk.

#HttpOnly_localhost	FALSE	/	FALSE	0	JSESSIONID	77C1B93F8AD4CE679250E3D64EAE8EB8
```

(`JSESSIONID` 값은 로그인할 때마다 다릅니다. 위 출력은 보안 헤더를 생략했습니다.)

- `curl` 은 브라우저와 달리 쿠키를 자동으로 저장·전송하지 않습니다. `-c 파일` 로 저장하고 `-b 파일` 로 보냅니다.
- `HttpOnly` 는 자바스크립트가 못 읽게 하는 표시이고, `SameSite=Lax` 는 다른 사이트에서 시작된 `POST` 에 쿠키를 싣지 않게 합니다. 파일에서 `#HttpOnly_` 접두사로 보입니다.
- 로그아웃은 `POST /auth/logout` 이고 204 입니다. 그 뒤에 같은 쿠키로 `GET /auth/me` 를 부르면 401 입니다. 서버 쪽 세션이 사라졌기 때문입니다.

## 6. DevTools Network 탭 읽는 법

4단계에서 쓴 기능을 정리합니다. 이름은 브라우저와 버전에 따라 조금 다를 수 있습니다.

| 기능 | 용도 |
|---|---|
| `Fetch/XHR` 필터 | 이미지, CSS 를 빼고 `fetch` 로 보낸 API 요청만 보기 |
| `Preserve log` | 페이지를 이동하거나 새로고침해도 이전 요청을 지우지 않기 |
| `Headers` 탭 | General(URL, 메서드, 상태 코드), Response Headers, Request Headers |
| `Payload` 탭 | 내가 보낸 쿼리스트링과 요청 본문 |
| `Response` / `Preview` 탭 | 서버가 보낸 본문 (Preview 는 JSON 을 접어서 보여 줌) |
| `Timing` 탭 | 요청이 대기·다운로드에 쓴 시간 |
| `Copy` → `Copy as cURL` | 요청을 터미널에서 그대로 재현 |
| `Copy` → `Copy as fetch` | 요청을 `fetch` 코드로 복사 |

## 7. "화면이 이상하다" 를 가르는 순서

백엔드와 협업하는 디버깅의 첫걸음은 **어느 쪽 문제인지 가르는 것**입니다. 이 순서를 외우세요.

1. Network 탭에 **요청이 있는가?** 없으면 요청을 보내는 프론트 코드 문제입니다.
2. 있다면 **URL, 메서드, 쿼리, 요청 본문**이 의도한 그대로인가? 틀렸다면 프론트 문제입니다.
3. **상태 코드**가 무엇인가? 4xx 면 응답 `message` 를 읽습니다. 5xx 면 서버 문제로 보고 요청·응답을 백엔드에 전달합니다.
4. 응답 본문은 정상인데 화면이 이상하다면, 본문의 모양과 프론트가 기대한 모양이 같은지 비교합니다. (2주와 6주)
5. 판단이 안 서면 **Copy as cURL 로 터미널에서 재현**합니다. 터미널에서도 같은 오류면 서버 쪽 규칙이고, 터미널에서는 정상인데 브라우저에서만 오류면 쿠키·헤더·CORS 같은 브라우저 쪽 조건입니다. (8주)

서버를 직접 만들어 보면서 요청 처리의 안쪽을 보고 싶다면 [HTTP와 JSON](../../archive/java-24weeks/week18-http-json.md) 을 참고하세요. 필수는 아닙니다.

## 연습문제

1. `curl -i localhost:8080/books/999` 의 응답에서 `Content-Type` 과 상태 줄을 찾아 노트에 적고, 본문만 받아 `curl -s localhost:8080/books/999 | jq .` 로 보기 좋게 출력하세요.
2. 2단계 d 의 요청을 `Accept-Language: ko` 와 `Accept-Language: en` 으로 각각 보내 문구가 어떻게 달라지는지 비교하세요. 그리고 1단계 2번(없는 책)의 문구는 같은 헤더를 줘도 바뀌지 않는다는 것을 확인하세요. 4xx 오류 문구를 그대로 사용자에게 보여 줄 때 어떤 문제가 생길 수 있는지 한 문장으로 적으세요.
3. `curl -s -o /dev/null -w '%{http_code} %{time_total}\n' localhost:8080/books` 를 세 번 실행해 응답 시간을 비교하세요. `-w` 의 서식 변수가 하는 일을 설명하세요.
4. DevTools 에서 `GET /books` 요청의 `Copy as fetch` 를 복사해 Console 에 붙여 넣고 실행하세요. 결과를 `.then((r) => r.json())` 으로 읽어 보세요.
5. 서버를 `Ctrl+C` 로 끄고 Swagger UI 화면에서 `GET /books` 를 다시 실행하세요. Network 탭에서 이 요청이 어떻게 표시되는지 적고, 상태 코드가 있는지 확인하세요.

## 퀴즈

1. `fetch` 로 `GET /books/999` 를 부르면 `await fetch(...)` 줄에서 예외가 날까?
2. 401 과 403 은 어떻게 다른가? 이 API 의 401 두 가지는 어떻게 구분되나?
3. 응답 헤더 `Allow: POST` 가 붙은 405 는 프론트에게 무엇을 알려 주나?
4. 로그인 후 `document.cookie` 에 `JSESSIONID` 가 안 보이는데 이것은 버그인가?
5. 브라우저에서는 되는데 `curl` 로는 401 이 난다. 가장 먼저 의심할 것은?

<details>
<summary>정답</summary>

1. 나지 않는다. 응답을 받았으므로 `fetch` 는 resolve 하고, 404 는 `res.status`, `res.ok` 로 확인한다. 예외는 응답을 받지 못했을 때만 난다.
2. 401 은 "누구인지 확인되지 않았다(로그인 필요)", 403 은 "누구인지는 알지만 권한이 없다" 이다. 이 API 에서 본 것은 401 뿐이고, 응답 `message` 가 "로그인이 필요합니다" 인지 "아이디 또는 비밀번호가 올바르지 않습니다" 인지로 구분한다.
3. 요청한 경로는 있지만 그 메서드는 안 된다는 것이다. 쓸 수 있는 메서드는 `POST` 이므로, 호출하는 메서드를 바로잡으면 된다.
4. 버그가 아니다. 쿠키에 `HttpOnly` 가 붙어 있어 자바스크립트가 읽지 못하게 막은 것이다. Application 탭에서는 보인다.
5. 쿠키다. 브라우저는 로그인 때 받은 쿠키를 자동으로 보내지만, `curl` 은 `-c` 로 저장하고 `-b` 로 보내야 한다.

</details>

## 체크리스트

- [ ] 1단계 8개 요청의 상태 코드를 먼저 예측했고, 틀린 것의 이유를 적었다
- [ ] 2단계에서 `Content-Type` 누락, 쿠키 누락, 깨진 JSON, 필드 누락이 각각 어떤 상태 코드로 오는지 직접 보았다
- [ ] `fetch` 가 4xx 에서 예외를 던지지 않는 이유와, 던지는 경우를 설명할 수 있다
- [ ] DevTools Network 탭에서 요청의 URL, 메서드, 상태 코드, 요청·응답 헤더, 본문을 찾을 수 있다
- [ ] `Copy as cURL` 로 브라우저의 요청을 터미널에서 재현해 보았다
- [ ] "화면이 이상하다" 를 가르는 5단계 순서를 말할 수 있다
