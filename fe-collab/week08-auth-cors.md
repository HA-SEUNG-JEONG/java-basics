# 8주차. 인증·CORS: 브라우저가 쿠키를 들고 API 를 부르기까지

**예상 시간:** 7~9시간 (하루 1시간 세션 7회) | **목표:** 프론트(`localhost:3000`)에서 백엔드(`localhost:8080`)를 부를 때 브라우저가 무엇을 막고 무엇을 허용하는지 헤더 단위로 설명한다. 세션 쿠키 로그인이 어떻게 이어지는지 확인하고, 401·CORS 실패를 화면에서 구분한다. 마지막의 마일스톤 체크에서 타입 생성, 에러 분기, 쿠키 인증이 동작하는 것을 직접 보여 준다.

> 6주(API 계약)와 7주(에러 처리)를 먼저 끝내세요. 7주에서 본 `ApiError` 래퍼를 그대로 씁니다. 실습 대상은 같은 참조 API `../labs/backend/week25` 와 클라이언트 `../labs/frontend` 이며, 이 API 에는 **Spring Security 세션 쿠키 로그인과 CORS 설정**이 들어 있습니다. 백엔드 팀이 준 API 로 보고 호출하고, 3단계에서만 백엔드 설정 파일을 고쳐 보며, 이때도 복사본만 고치고 원본은 건드리지 않습니다. 명령은 별도 언급이 없으면 저장소 루트에서 실행합니다.
>
> 준비물: JDK 21, Node 22.12+, pnpm 10+, `curl`, `jq`. 프론트 패키지는 처음이라면 `pnpm -C labs/frontend install` 을 먼저 합니다.

## 하루 1시간으로 나누면

이 주차는 마일스톤 체크가 들어 있어 하루 1시간씩 7번의 세션으로 나눕니다. 한 세션은 50~70분이고, 마일스톤 체크는 문서의 1) 터미널과 2) 브라우저 구간으로 나눕니다.

| 세션 | 할 일 | 끝났을 때 할 수 있어야 하는 것 |
|---|---|---|
| 1 | 1단계(응답 예측하기 → 보내기 → 비교하기, 40분)를 하고, 1절(Origin 과 CORS)을 읽는다. 1절의 소절 "preflight 는 왜 생기는가" 를 포함한다 | 12개 요청의 상태 코드와 `Access-Control-` 헤더 유무를 예측하고, preflight 가 생기는 이유를 설명할 수 있다 |
| 2 | 2단계(진짜 CORS 오류를 브라우저에서 보기, 30분)를 하고, 2절(해결 방식 두 가지: 직접 호출과 `rewrites` 프록시)과 3절(세션 쿠키 vs JWT)을 읽는다 | 브라우저 콘솔의 CORS 오류에서 자바스크립트가 상태 코드를 보지 못한다는 것을 보이고, 직접 호출과 `rewrites` 프록시의 차이, 세션 쿠키와 `localStorage` JWT 의 XSS 차이를 말할 수 있다 |
| 3 | 3단계(일부러 망가뜨리기, 30분)를 하고, 4절(프론트 코드: `credentials`, 401, CSRF)과 TS 와 비교 절을 읽는다 | `document.cookie` 에 `JSESSIONID` 가 안 보이는 이유를 설명하고, 이 저장소의 CSRF 가 꺼져 있는 가정을 말할 수 있다 |
| 4 | 연습문제와 퀴즈를 푼다 (압축 코스에서는 건너뜀) | 복사본 서버에 허용 origin 을 추가해 `Access-Control-` 헤더가 달라지는 것을 확인하고, 운영 배포에서 바꿔야 하는 설정 3가지를 쓸 수 있다 |
| 5 | 마일스톤 체크 (8주)의 준비와 1) 터미널: 자동 점검을 한다 | 타입 생성, 에러 분기, 쿠키 인증 세 영역의 자동 점검이 모두 `PASS` 인 출력을 남길 수 있다 |
| 6 | 마일스톤 체크 (8주)의 2) 브라우저: 화면에서 보여 주기와 마무리를 한다. 화면 녹화나 출력을 남긴다 | 브라우저 8단계를 직접 시연하고, 스펙이 바뀌었을 때 `typecheck` 가 깨지는 파일을 보며 어느 변경이 어느 화면에 영향을 주는지 말로 설명할 수 있다 |
| 7 | 체크리스트의 각 항목을 내 말로 설명해 보고, 막히는 항목은 해당 절을 다시 읽은 뒤 학습 노트를 정리한다 | 401 과 CORS 실패를 화면과 응답에서 구분해 설명할 수 있다 |

세션을 시작하기 전에 이전 세션의 마지막 결과를 1~2분 훑어봅니다. 막히면 아래 해당 개념 절을 사전처럼 펼쳐 보세요.

## 🖐 먼저 손으로 (읽기 전에 하세요)

> 이 주차의 핵심은 아래 단계를 **직접 타이핑하면서** 깨닫는 것입니다. 아래쪽 설명 문서는 먼저 읽지 않아도 됩니다. 막혔을 때 사전처럼 펼쳐 보세요.

### 1단계. 응답 예측하기 → 보내기 → 비교하기 (40분)

백엔드를 켭니다. 8080 이 비어 있어야 합니다. (Ctrl+C 로 종료)

```bash
cd labs/backend && ./gradlew :week25:bootRun
```

아래 12개 요청을 보내기 **전에**, 각각 **상태 코드**와 **`Access-Control-` 로 시작하는 응답 헤더가 붙는지**를 노트에 적으세요. 힌트는 이것뿐입니다. 이 API 는 "프론트는 `http://localhost:3000` 하나뿐이고, 메서드는 GET·POST, 헤더는 `Content-Type` 만" 허용하도록 설정되어 있습니다. 쓰기(POST)는 로그인이 필요하고 읽기(GET)는 공개입니다.

| # | 요청 | 내 예측 (상태 / `Access-Control-` 헤더) |
|---|------|------|
| 1 | `OPTIONS /loans` — `Origin: http://localhost:3000`, `Access-Control-Request-Method: POST`, `Access-Control-Request-Headers: content-type` | |
| 2 | 1번과 같지만 `Origin: http://evil.example` | |
| 3 | 1번과 같지만 `Access-Control-Request-Method: PUT` | |
| 4 | 1번과 같지만 `Access-Control-Request-Headers: x-foo` | |
| 5 | `GET /books` (`Origin` 헤더 없음) | |
| 6 | `GET /books` + `Origin: http://localhost:3000` | |
| 7 | 로그인 없이 `POST /members` | |
| 8 | `POST /auth/login` 비밀번호 `nope` | |
| 9 | `POST /auth/login` `demo` / `demo1234` — `Set-Cookie` 의 속성은? | |
| 10 | 9번에서 받은 쿠키를 싣고 `POST /members` | |
| 11 | 같은 쿠키로 `POST /auth/logout` | |
| 12 | 로그아웃 뒤 그 쿠키로 `GET /auth/me` | |

다른 터미널에서, 저장소 루트에서 위에서부터 한 번에 실행합니다. (서버를 새로 켠 직후 한 번만. 10번이 회원을 만듭니다.)

```bash
O='Origin: http://localhost:3000'
H='Content-Type: application/json'
JAR=$(mktemp)
echo "1"; curl -si -X OPTIONS localhost:8080/loans -H "$O" -H 'Access-Control-Request-Method: POST' -H 'Access-Control-Request-Headers: content-type' | grep -iE '^HTTP|^Access-Control'
echo "2"; curl -si -X OPTIONS localhost:8080/loans -H 'Origin: http://evil.example' -H 'Access-Control-Request-Method: POST' | grep -iE '^HTTP|^Access-Control|^Invalid'
echo "3"; curl -si -X OPTIONS localhost:8080/loans -H "$O" -H 'Access-Control-Request-Method: PUT' | grep -iE '^HTTP|^Access-Control|^Invalid'
echo "4"; curl -si -X OPTIONS localhost:8080/loans -H "$O" -H 'Access-Control-Request-Method: POST' -H 'Access-Control-Request-Headers: x-foo' | grep -iE '^HTTP|^Access-Control|^Invalid'
echo "5"; curl -si localhost:8080/books | grep -iE '^HTTP|^Access-Control'
echo "6"; curl -si localhost:8080/books -H "$O" | grep -iE '^HTTP|^Access-Control'
echo "7"; curl -s -w ' [%{http_code}]\n' -X POST localhost:8080/members -H "$H" -d '{"name":"하승"}'
echo "8"; curl -s -w ' [%{http_code}]\n' -X POST localhost:8080/auth/login -H "$H" -d '{"username":"demo","password":"nope"}'
echo "9"; curl -si -c $JAR -X POST localhost:8080/auth/login -H "$H" -d '{"username":"demo","password":"demo1234"}' | grep -iE '^HTTP|^Set-Cookie'
echo "10"; curl -s -b $JAR -w ' [%{http_code}]\n' -X POST localhost:8080/members -H "$H" -d '{"name":"하승"}'
echo "11"; curl -si -b $JAR -X POST localhost:8080/auth/logout | grep -iE '^HTTP'
echo "12"; curl -s -b $JAR -w ' [%{http_code}]\n' localhost:8080/auth/me
```

(`-c` 는 받은 쿠키를 파일에 저장하고 `-b` 는 파일의 쿠키를 보냅니다. 브라우저가 자동으로 하는 일을 손으로 한 것입니다.)

<details>
<summary>정답 (예측을 적은 뒤에 여세요)</summary>

```text
1  200   Access-Control-Allow-Origin: http://localhost:3000
         Access-Control-Allow-Methods: GET,POST
         Access-Control-Allow-Headers: content-type
         Access-Control-Allow-Credentials: true
2  403   Invalid CORS request          (Access-Control- 헤더 없음)
3  403   Invalid CORS request
4  403   Invalid CORS request
5  200   (Access-Control- 헤더 없음)
6  200   Access-Control-Allow-Origin: http://localhost:3000
         Access-Control-Allow-Credentials: true
7  401   {"message":"로그인이 필요합니다"}
8  401   {"message":"아이디 또는 비밀번호가 올바르지 않습니다"}
9  200   Set-Cookie: JSESSIONID=<매번 다른 값>; Path=/; HttpOnly; SameSite=Lax
10 201   {"id":1,"name":"하승"}
11 204
12 401   {"message":"로그인이 필요합니다"}
```

- 2~4번: **서버가 허용하지 않은 것(다른 Origin, PUT, `x-foo` 헤더)은 모두 403.** 허용 여부는 서버가 "이 목록에 있는 것만 된다"고 정해 둔 설정(`SecurityConfig`)이 결정합니다.
- 5번 vs 6번: `Origin` 헤더가 있어야 CORS 헤더가 붙습니다. `Origin` 은 **브라우저가** 붙여 보내는 헤더입니다. curl 은 안 붙이니 CORS 와 무관하게 통과합니다.
- 9번의 `HttpOnly` 와 `SameSite=Lax` 는 `application.properties` 두 줄의 결과입니다. `Secure` 는 없습니다. 로컬은 http 이기 때문입니다.
- 12번이 401 인 이유: 11번이 서버의 세션을 지웠습니다. 쿠키 파일에는 옛 값이 남아 있지만 서버가 모르는 값이 되었습니다.

</details>

### 2단계. 진짜 CORS 오류를 브라우저에서 보기 (30분)

curl 은 CORS 를 모릅니다. **막는 쪽은 브라우저**입니다. 백엔드를 켜 둔 채로 브라우저가 백엔드를 **직접** 부르는 모드로 프론트를 띄웁니다.

```bash
cd labs/frontend
NEXT_PUBLIC_API_BASE=http://localhost:8080 pnpm dev
```

`http://localhost:3000` 을 열고 DevTools 의 Console 에 아래를 차례로 붙여 넣습니다. (화면 위의 로그인 바에서 `demo` / `demo1234` 로 먼저 로그인합니다.)

```js
// A. 허용된 호출
await fetch("http://localhost:8080/auth/me", { credentials: "include" }).then(r => r.json())

// B. 서버가 허용하지 않은 메서드
await fetch("http://localhost:8080/loans", { method: "PUT", credentials: "include" })

// C. 서버가 허용하지 않은 헤더
await fetch("http://localhost:8080/books", { headers: { "X-Foo": "1" } })

// D. 세션 쿠키를 자바스크립트가 읽을 수 있나
document.cookie
```

예측해서 적고 실행하세요. 노트에 적을 것은 세 가지입니다. ① B·C 에서 콘솔에 뭐라고 뜨는가 ② `catch` 에서 받는 에러에 상태 코드(403)가 들어 있는가 ③ D 의 결과.

<details>
<summary>정답</summary>

```text
A  {username: 'demo'}
B  [error] Access to fetch at 'http://localhost:8080/loans' from origin 'http://localhost:3000' has been blocked by CORS policy:
   Response to preflight request doesn't pass access control check: No 'Access-Control-Allow-Origin' header is present on the requested resource.
   [error] Failed to load resource: net::ERR_FAILED
   fetch 가 던진 값: TypeError: Failed to fetch
C  (B 와 같은 형식, 주소만 /books)
D  ""
```

- ② 자바스크립트는 **상태 코드도 본문도 못 봅니다.** 서버는 403 `Invalid CORS request` 를 보냈지만 브라우저가 응답을 감추고 `TypeError: Failed to fetch` 만 줍니다. 그래서 "CORS 오류는 서버 로그와 DevTools Network 탭에서 찾는다"가 규칙입니다. Network 탭에는 본 요청이 아니라 **`OPTIONS` 요청(preflight)이 403** 으로 보입니다.
- D: 로그인을 했는데도 `document.cookie` 가 빈 문자열입니다. `HttpOnly` 쿠키는 스크립트에서 보이지 않습니다. 그래서 XSS 로 스크립트가 심겨도 세션 쿠키 값을 읽어 가지 못합니다. (Application 탭에서는 보입니다.)
- 이 모드에서 페이지를 열면 콘솔에 `/auth/me` 의 401 (`Failed to load resource: ... 401`)이 한 줄 뜹니다. 로그인 전에 현재 사용자를 물어봤기 때문이며 정상입니다.
- 위 값은 같은 요청을 Chromium 156 에서 보내 확인한 것입니다. 오류 문구는 브라우저 종류와 버전에 따라 조금 다를 수 있습니다.

</details>

### 3단계. 일부러 망가뜨리기 (30분)

백엔드 테스트가 이 설정을 지키고 있습니다. 백엔드 소스는 평소에는 읽기만 하지만, 이 단계에서는 4주와 같은 방식으로 **내 컴퓨터의 복사본에서만** 설정 파일 두 개를 하나씩 고장 내고 어느 테스트가 실패하는지 확인합니다. 원본 `labs/backend/week25` 는 고치지 않습니다. 먼저 복사본을 만들고(4주에서 이미 만들었다면 같은 명령으로 원본 상태로 되돌려도 됩니다), 고장 내기 전에 7개가 모두 통과하는지 확인하세요.

```bash
rsync -a --exclude build --exclude .gradle labs/backend/ ~/scratch/backend-copy/
cd ~/scratch/backend-copy && ./gradlew :week25:test --tests 'library.SecurityAndDocsTest'
```

고치는 파일은 복사본의 `~/scratch/backend-copy/week25/src/main/java/library/SecurityConfig.java` 와 `~/scratch/backend-copy/week25/src/main/resources/application.properties` 입니다. 표의 한 행을 실험할 때마다 아래 명령으로 복사본을 원본 상태로 되돌려 놓고 다음 행으로 넘어가세요. (저장소 루트에서 실행합니다.)

```bash
cp labs/backend/week25/src/main/java/library/SecurityConfig.java ~/scratch/backend-copy/week25/src/main/java/library/
cp labs/backend/week25/src/main/resources/application.properties ~/scratch/backend-copy/week25/src/main/resources/
```

고친 뒤의 테스트는 복사본에서 실행합니다.

```bash
cd ~/scratch/backend-copy && ./gradlew :week25:test --tests 'library.SecurityAndDocsTest'
```

| 이렇게 바꿔 보세요 | 어떻게 될까요? |
|---|---|
| `SecurityConfig` 의 `setAllowCredentials(true)` 를 `false` 로 | `corsPreflight_allowsFrontendOriginWithCredentials` 1개 실패. `Access-Control-Allow-Credentials` 헤더가 사라진다. 쿠키를 싣는 요청은 브라우저가 거부한다 |
| `setAllowedOrigins(List.of("http://localhost:3000"))` 를 `List.of("*")` 로 | 2개 실패 (`corsPreflight_allowsFrontendOriginWithCredentials`, `corsPreflight_rejectsOtherOrigin`). 쿠키를 허용하면서 모든 origin 을 허용하는 조합은 안 된다 |
| `.cors(Customizer.withDefaults())` 줄 삭제 | `corsPreflight_allowsFrontendOriginWithCredentials` 실패. CORS 설정 빈(`corsConfigurationSource`)이 있어도 **보안 필터에 연결하지 않으면** preflight 가 200 으로 끝나지 않는다 |
| `setAllowedMethods(List.of("GET", "POST"))` 를 `List.of("GET")` 로 | `corsPreflight_allowsFrontendOriginWithCredentials` 실패. `POST` preflight 가 거절된다 |
| `application.properties` 에서 `server.servlet.session.cookie.same-site=lax` 줄 삭제 | `realServer_sessionCookieIsHttpOnlyAndLax` 실패. 쿠키 속성은 실제 톰캣을 띄워야 보이므로 이 테스트만 `RANDOM_PORT` 로 서버를 띄운다 |
| 같은 파일에서 `cookie.http-only=true` 를 `false` 로 | 같은 테스트 실패. 이제 2단계의 `document.cookie` 에 `JSESSIONID` 가 보인다 |

---

## 1. Origin 과 CORS: 누가 무엇을 막는가

**Origin = 스킴 + 호스트 + 포트.** `http://localhost:3000` 과 `http://localhost:8080` 은 포트가 달라서 **다른 origin** 입니다. 브라우저는 한 origin 의 스크립트가 다른 origin 의 응답을 읽지 못하게 하는 기본 규칙(동일 출처 정책)을 갖고 있고, **CORS 는 서버가 "이 origin 은 읽어도 된다"고 응답 헤더로 알려서 그 규칙을 푸는 방법**입니다.

핵심 세 가지를 1·2단계 결과로 다시 확인하세요.

1. **집행은 브라우저가 합니다.** 서버는 헤더로 허용 목록을 알릴 뿐입니다. curl·서버 간 호출·Postman 은 CORS 에 걸리지 않습니다. (1단계 5번)
2. **실패하면 스크립트는 이유를 모릅니다.** 상태 코드도 본문도 가려지고 `TypeError: Failed to fetch` 만 받습니다. (2단계 B·C)
3. **허용 설정은 백엔드 몫입니다.** 프론트 코드로는 고칠 수 없습니다. 그래서 백엔드 담당자에게 "어느 origin·메서드·헤더가 막혔는지"를 정확히 전달해야 합니다. 이 문서의 헤더 표가 그 언어입니다.

### preflight 는 왜 생기는가

브라우저는 "서버에 부작용을 줄 수 있는" 요청을 **본 요청을 보내기 전에** 먼저 `OPTIONS` 로 물어봅니다. 이것이 preflight 입니다. 아래 중 하나라도 해당하면 물어봅니다.

- 메서드가 `GET`·`HEAD`·`POST` 가 아니다 (`PUT`, `DELETE`, `PATCH`)
- `Authorization` 같은 커스텀 헤더를 보낸다
- `Content-Type` 이 `application/json` 이다 (폼 전송용 값 세 가지만 예외)

이 저장소에서 `POST /loans` 에 `{"memberId":1,"bookId":1}` 을 JSON 으로 보내면 브라우저는 먼저 아래를 보냅니다. 1단계 1번이 정확히 이것입니다.

```text
OPTIONS /loans
Origin: http://localhost:3000
Access-Control-Request-Method: POST
Access-Control-Request-Headers: content-type
```

서버가 200 과 아래 헤더로 답해야 브라우저가 본 요청을 보냅니다.

| 응답 헤더 | 의미 | `SecurityConfig` 의 설정 |
|---|---|---|
| `Access-Control-Allow-Origin` | 읽어도 되는 origin | `setAllowedOrigins(List.of("http://localhost:3000"))` |
| `Access-Control-Allow-Methods` | 허용 메서드 | `setAllowedMethods(List.of("GET", "POST"))` |
| `Access-Control-Allow-Headers` | 허용 요청 헤더 | `setAllowedHeaders(List.of("Content-Type"))` |
| `Access-Control-Allow-Credentials` | 쿠키를 실은 요청 허용 | `setAllowCredentials(true)` |

```java
// ../labs/backend/week25/src/main/java/library/SecurityConfig.java
@Bean
CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration cors = new CorsConfiguration();
    cors.setAllowedOrigins(List.of("http://localhost:3000"));
    cors.setAllowedMethods(List.of("GET", "POST"));
    cors.setAllowedHeaders(List.of("Content-Type"));
    cors.setAllowCredentials(true); // 쿠키를 실은 요청 허용. 이때 origin 에 "*" 는 쓸 수 없다
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", cors);
    return source;
}
```

- **JWT 를 `Authorization` 헤더에 싣는 방식으로 바꾸면** 1단계 4번과 같은 이유로 preflight 가 403 이 됩니다. `setAllowedHeaders` 에 `Authorization` 을 추가해야 합니다. 이 저장소의 허용 헤더가 `Content-Type` 하나인 이유는 쿠키 인증이라 다른 헤더가 필요 없기 때문입니다.
- **Origin 이 목록에 없으면 단순 `GET` 도 403 입니다.** 이 서버는 `Origin` 헤더가 붙은 요청이면 curl 로 보낸 것이라도 판정합니다 (`Origin: http://evil.example` 로 `GET /books` 를 보내면 403 `Invalid CORS request`). `Origin` 헤더가 없으면(5번) 통과합니다.
- `Access-Control-Allow-Origin: *` 와 `Allow-Credentials: true` 는 함께 못 씁니다. 쿠키를 허용하려면 origin 을 정확히 적어야 하고, 그래서 환경(개발·스테이징·운영)마다 허용 origin 을 설정으로 빼는 일이 실무에서 자주 생깁니다.

## 2. 해결 방식 두 가지: 직접 호출과 `rewrites` 프록시

`../labs/frontend` 는 두 방식을 모두 지원합니다. 환경 변수 하나(`NEXT_PUBLIC_API_BASE`)로 바뀝니다.

```ts
// src/api/client.ts
export function createApiClient(baseUrl: string) {
  return createClient<paths>({ baseUrl, credentials: "include" });
}

/** 기본값 "/api" = next.config.ts 의 rewrites 프록시. NEXT_PUBLIC_API_BASE 가 있으면 백엔드를 직접 호출. */
export const api = createApiClient(process.env.NEXT_PUBLIC_API_BASE || "/api");
```

```ts
// next.config.ts
async rewrites() {
  return [{ source: "/api/:path*", destination: "http://localhost:8080/:path*" }];
},
```

| | (a) 직접 호출 + CORS 허용 | (b) Next `rewrites` 프록시 (기본값) |
|---|---|---|
| 설정 | `NEXT_PUBLIC_API_BASE=http://localhost:8080` | 비워 둠. 브라우저는 `/api/...` 만 호출 |
| 브라우저가 보는 origin | 프론트와 백엔드 두 개 | 프론트 하나 (same-origin) |
| JSON POST 마다 preflight | 있음 | 없음 |
| 백엔드 CORS 설정 | 필요. 틀리면 2단계 같은 오류 | 브라우저 입장에서는 불필요. 그래도 백엔드의 `Origin` 검사는 남음 |

이 주차의 실험은 (a)로 CORS 를 겪어 보는 것이 목적입니다. 두 방식을 같은 화면에서 나란히 띄워 비교하는 실습, 프록시 모드에서 `Origin` 검사가 남는 이유, 백엔드가 꺼졌을 때 화면에 보이는 증상은 [9주 로컬 통합 환경](week09-local-integration.md)에서 다룹니다.

**SameSite 와 "같은 사이트"**: 포트는 site 판정에 들어가지 않습니다. `localhost:3000` 과 `localhost:8080` 은 포트만 다른 **same-site** 라서, 직접 호출 모드에서도 `SameSite=Lax` 쿠키가 실립니다. 직접 호출 모드에서도 로그인이 유지되는 이유이고, e2e 도 통과합니다. (`NEXT_PUBLIC_API_BASE=http://localhost:8080 pnpm -C labs/frontend e2e` 가 통과하는 것을 확인했습니다.) 프론트와 백엔드가 **서로 다른 사이트**(예: `a.com` 과 `b.com`)가 되면 `Lax` 쿠키는 fetch 요청에 실리지 않으므로 `SameSite=None; Secure` 가 필요합니다. (이 저장소에서 실행해 보지는 않았습니다. 규칙만 알아 두세요.)

## 3. 세션 쿠키 vs JWT: 로그인 상태를 어디에 두나

이 저장소는 **서버가 로그인 상태를 들고 있는 세션 방식**입니다. `AuthController.login` 이 성공하면 서버 메모리에 세션이 생기고, 브라우저는 그 열쇠(`JSESSIONID`)만 쿠키로 받습니다.

```java
// AuthController.login 의 핵심
Authentication auth = authenticationManager.authenticate(
        UsernamePasswordAuthenticationToken.unauthenticated(req.username(), req.password())); // 틀리면 401
request.getSession();
request.changeSessionId(); // 세션 고정 공격 방지: 로그인 전 세션 id 를 버린다
...
contexts.saveContext(context, request, response);
```

| | 세션 쿠키 (이 저장소) | JWT 를 `localStorage` 에 | JWT 를 HttpOnly 쿠키에 |
|---|---|---|---|
| 로그인 상태가 있는 곳 | 서버 (세션 저장소) | 토큰 자체 (서버 저장 없음) | 토큰 자체 |
| 요청에 싣는 방법 | 브라우저가 쿠키를 자동으로 | 프론트 코드가 `Authorization` 헤더에 직접 | 브라우저가 쿠키를 자동으로 |
| XSS (악성 스크립트 실행) | 쿠키 값은 스크립트가 못 읽음. 단, 스크립트가 **그 페이지에서 요청을 보내는 것**은 막지 못함 | **토큰을 읽어 가져감.** 가장 큰 위험 | 값은 못 읽음 |
| CSRF (다른 사이트가 내 쿠키로 요청 유도) | 대책 필요 (아래 4절) | 해당 없음 (자동으로 안 실림) | 대책 필요 |
| CORS | `Allow-Credentials` + 정확한 origin | `Allow-Headers: Authorization` | 쿠키와 동일 |
| 서버 여러 대 | 세션 공유 필요 | 서버가 상태 없이 검증 가능 | 상태 없이 검증 가능 |
| 강제 로그아웃·폐기 | 세션을 지우면 끝 (12번에서 확인) | 만료 전까지 유효. 폐기 목록을 따로 관리해야 | 동일 |

"JWT 는 모던하고 세션은 낡았다"가 아닙니다. 서버를 여러 대 두고 세션 저장소를 공유하기 싫을 때 JWT 가 편해지지만, **저장 위치를 `localStorage` 로 하면 XSS 한 번에 토큰을 통째로 잃습니다.** 프론트 개발자가 이 선택에 영향을 주는 지점이 바로 저장 위치입니다. 이 저장소에서 JWT 는 비교만 합니다. 구현은 없습니다.

**쿠키 속성 세 가지** (9번 `Set-Cookie` 의 `Path=/; HttpOnly; SameSite=Lax`):

| 속성 | 하는 일 | 이 저장소 |
|---|---|---|
| `HttpOnly` | 자바스크립트가 못 읽음 (2단계 D) | `server.servlet.session.cookie.http-only=true` |
| `SameSite=Lax` | 다른 사이트에서 일어난 `POST` 같은 요청에는 쿠키를 안 실음 | `server.servlet.session.cookie.same-site=lax` |
| `Secure` | https 에서만 전송 | **없음.** 로컬이 http 라서. 운영에서는 필수 |

## 4. 프론트 코드: `credentials`, 401, CSRF

### `credentials: "include"`

`fetch` 는 **다른 origin 으로 갈 때 기본적으로 쿠키를 보내지도 받지도 않습니다.** 직접 호출 모드에서 로그인 상태를 유지하려면 `credentials: "include"` 가 필요하고, 그러면 서버도 `Allow-Credentials: true` 로 응답해야 브라우저가 받아들입니다. 양쪽이 짝입니다. `createApiClient` 가 한 줄(`credentials: "include"`)로 두 모드를 모두 다룹니다.

### 401 을 화면에서 다루기

`../labs/frontend` 는 401 을 상황마다 다르게 다룹니다.

| 상황 | 코드 | 화면 |
|---|---|---|
| 처음 열었을 때 "내가 누구지?" | `useMe` — `/auth/me` 가 401 이면 에러가 아니라 **`null`** | "로그인 안 함"은 정상 상태. 로그인 바가 입력칸을 보여 줌 |
| 쓰기 버튼 | `SubmitButton` — 로그인 전에는 `disabled` + "로그인 후 사용할 수 있습니다" | 서버가 어차피 401 이지만 눌러 보기 전에 막음 |
| 세션이 중간에 만료된 쓰기 요청 | `providers.tsx` 의 `MutationCache.onError` — `ApiError` 401 이면 `["me"]` 캐시를 `null` 로 | 로그인 상태 표시가 즉시 로그아웃으로 바뀜 |
| 그 외 401 | `ErrorNotice` 의 `kind === "auth"` | "로그인이 필요합니다" 알림 |

**401 과 403 을 구분하세요.** 401 은 "로그인(인증)이 안 됐다", 403 은 "로그인은 했는데 이 일은 못 한다(권한)". 이 API 에서 403 은 권한 때문이 아니라 위에서 본 **`Invalid CORS request`** 로 나옵니다. 프론트가 403 을 만나면 사용자가 아닌 설정 문제일 가능성부터 의심하세요.

### CSRF 가 이 저장소에서 꺼져 있는 이유

쿠키 인증은 브라우저가 쿠키를 **자동으로** 붙입니다. 그래서 악성 사이트가 사용자 몰래 우리 API 로 `POST` 를 쏘게 만드는 공격(CSRF)이 가능합니다. `SecurityConfig` 의 주석이 이 저장소의 선택입니다.

```java
// 데모라서 CSRF 를 끈다(SameSite=Lax 쿠키가 다른 사이트의 POST 에 안 실리는 것에만 기댐).
// 운영에서는 CookieCsrfTokenRepository 로 켜고 프론트가 X-XSRF-TOKEN 헤더로 보낸다.
.csrf(AbstractHttpConfigurer::disable)
```

- 지금은 `SameSite=Lax` 가 "다른 사이트에서 시작된 POST" 에 쿠키를 싣지 않는 것에 기대고 있습니다. 한 겹뿐입니다.
- `Lax` 는 **링크 클릭 같은 최상위 이동의 `GET`** 에는 쿠키를 싣습니다. 그래서 `GET` 이 데이터를 바꾸면 안 됩니다. (이 API 의 쓰기는 전부 `POST` 입니다.)
- 운영 대안: Spring 이 CSRF 토큰을 쿠키(`XSRF-TOKEN`)로 내려주고, **프론트가 모든 쓰기 요청에 `X-XSRF-TOKEN` 헤더로 되돌려 보내게** 합니다. 다른 사이트는 이 쿠키 값을 읽지 못하므로 헤더를 만들 수 없습니다. 이때 `X-XSRF-TOKEN` 은 커스텀 헤더이므로 **CORS `Allow-Headers` 에도 추가**해야 합니다(1절의 `x-foo` 와 같은 원리). 이 구성은 이 저장소에 구현되어 있지 않고 실행해 보지 않았습니다. 백엔드와 협의할 때의 체크 항목입니다.

브라우저와 Spring 사이에 프론트 팀이 소유하는 얇은 서버(BFF, Next 의 Route Handler)를 두는 선택지는 프론트 저장소의 `src/app/bff/summary/route.ts` 에 맛보기로 구현되어 있습니다. 11주에서 서버 경계의 한 가지로 다룹니다.

## TS 와 비교

백엔드 용어를 프론트의 익숙한 것에 대응시킨 표입니다. 완전히 같지는 않습니다.

| 백엔드(Java·Spring) 쪽 | 프론트에서 만나는 모습 |
|---|---|
| `SecurityFilterChain` (요청이 컨트롤러에 닿기 전에 지나는 필터 줄) | Express/Next 의 미들웨어 체인 |
| `CorsConfigurationSource` | 프론트에는 설정 대상이 없다. `Access-Control-*` 응답 헤더와 `Failed to fetch` 로 **결과로만 만난다** |
| `HttpSession` + `JSESSIONID` | 서버가 상태를 들고, 브라우저는 열쇠만 쥔다. `document.cookie` 에 안 보이는 쿠키 |
| `authenticationEntryPoint` (401 을 만드는 곳) | `fetch` 래퍼가 401 을 한 곳에서 `ApiError(auth)` 로 바꾸는 곳 |
| `@RestControllerAdvice` | `ApiError` 래퍼 (7주) |
| `InMemoryUserDetailsManager` (고정 데모 사용자) | MSW 핸들러의 `demo`/`demo1234` 로그인 |
| `CookieCsrfTokenRepository` + `X-XSRF-TOKEN` | 쿠키에서 토큰을 읽어 쓰기 요청 헤더에 싣는 `fetch` 래퍼 |
| 리버스 프록시 (`nginx proxy_pass`) | `next.config.ts` 의 `rewrites`, Vite 의 `server.proxy` |

## 연습문제

1. 3단계의 복사본(`~/scratch/backend-copy`)에서 `SecurityConfig` 의 허용 origin 에 `http://127.0.0.1:3000` 을 추가하세요. 복사본 서버를 `./gradlew :week25:bootRun --args='--server.port=8081'` 로 띄우고(8080 의 원본 서버는 그대로 둡니다), 1단계 1번을 `localhost:8081` 로, `Origin: http://127.0.0.1:3000` 으로 다시 보내 200 이 되는지 확인하세요. 확인 후 3단계의 원복 명령으로 복사본을 되돌립니다.
2. 직접 호출 모드(`NEXT_PUBLIC_API_BASE=http://localhost:8080`)에서 `labs/frontend/src/api/client.ts` 의 `credentials: "include"` 를 지우고, 로그인 직후 새로고침했을 때 로그인 상태가 어떻게 되는지 확인하세요. 왜 그런지 `Set-Cookie` 와 연결해 한 문장으로 쓰세요. (원복)
3. 프록시 모드에서 로그인한 뒤 DevTools 의 Application → Cookies 에서 `JSESSIONID` 를 찾아 `HttpOnly` 와 `SameSite` 칸을 읽으세요. 쿠키의 도메인이 어디로 표시되나요?
4. 운영 배포가 프론트 `https://app.example.com`, 백엔드 `https://api.example.com` 이라고 가정합니다. 이 문서의 설정 중 **바꿔야 하는 것 3가지**를 쓰세요. (힌트: 허용 origin, 쿠키 속성, 그리고 `app` 과 `api` 가 같은 사이트인지)
5. 로그아웃 후 이전 `JSESSIONID` 로 `GET /auth/me` 를 보내면 401 입니다(1단계 12번). 이것이 JWT 방식에서는 왜 더 어려운 일인지 3절 표를 참고해 설명하세요.

<details>
<summary>1번, 2번, 3번 확인 결과</summary>

- 1번: 허용 목록에 `"http://127.0.0.1:3000"` 을 더하고 서버를 다시 켜면 `HTTP/1.1 200` 과 `Access-Control-Allow-Origin: http://127.0.0.1:3000` 이 나옵니다. `localhost` 와 `127.0.0.1` 은 같은 컴퓨터를 가리키지만 호스트 문자열이 다르므로 **다른 origin** 입니다.
- 2번: `credentials: "include"` 를 지우면 로그인 직후에는 응답 본문으로 `demo 님` 이 보이지만, 브라우저가 다른 origin 의 응답에 담긴 `Set-Cookie` 를 저장하지 않아서 쿠키 목록이 비어 있고, 새로고침하면 로그인 폼으로 돌아갑니다.
- 3번: `HttpOnly` 가 체크되어 있고 `SameSite` 는 `Lax` 입니다. 도메인은 `localhost` 로 표시됩니다. 쿠키는 포트로 구분되지 않기 때문에 직접 호출 모드에서도 같은 `localhost` 로 표시됩니다.

</details>

## 퀴즈

1. `localhost:3000` 에서 `localhost:8080` 으로 `fetch` 하면 "다른 origin" 인 이유는?
2. curl 로는 되는 요청이 브라우저에서만 `Failed to fetch` 가 됩니다. 왜 그런가요? 이 에러에서 자바스크립트가 알 수 있는 것은?
3. JSON 본문을 가진 `POST` 에 preflight 가 생기는 이유와, 그 응답에 들어가야 하는 헤더 네 가지는?
4. `Access-Control-Allow-Origin: *` 로는 쿠키 인증을 못 하는 이유는?
5. `HttpOnly` 쿠키는 XSS 에서 무엇을 막고 무엇을 막지 못하나요?
6. 이 저장소가 CSRF 를 꺼도 되는 근거와, 그 근거의 약점은?

<details>
<summary>정답</summary>

1. origin 은 스킴·호스트·포트의 조합이고 포트가 다르기 때문
2. CORS 는 브라우저가 집행한다. curl 은 `Origin` 을 보내지도 응답 헤더를 검사하지도 않는다. 자바스크립트는 `TypeError` 가 났다는 사실만 알고 상태 코드·본문은 못 본다. 원인은 Network 탭과 서버 로그에서 찾는다
3. JSON 은 폼 전송용이 아닌 `Content-Type` 이라 브라우저가 먼저 `OPTIONS` 로 허용 여부를 묻는다. 응답에는 `Allow-Origin`, `Allow-Methods`, `Allow-Headers`, (쿠키를 쓰면) `Allow-Credentials`
4. 브라우저가 `credentials: include` 요청의 응답에서 `*` 를 거부한다. 쿠키를 허용하려면 origin 을 정확히 적어야 한다
5. 막는 것: 스크립트가 쿠키 값을 읽어 가져가는 것. 못 막는 것: 이미 실행 중인 스크립트가 그 페이지에서 요청을 보내 로그인 상태를 이용하는 것
6. 근거: `SameSite=Lax` 가 다른 사이트에서 시작된 `POST` 에 쿠키를 싣지 않는다. 약점: 방어가 한 겹이고, `Lax` 는 최상위 이동 `GET` 에는 쿠키를 싣는다(그래서 `GET` 이 상태를 바꾸면 안 됨). 같은 사이트의 다른 서브도메인에서 오는 공격은 못 막는다. 운영에서는 CSRF 토큰을 켠다

</details>

## 마일스톤 체크 (8주): 타입 생성 · 에러 분기 · 쿠키 인증

여기까지가 "백엔드와 같은 말로 협업하는" 첫 번째 마일스톤입니다. 말로 설명하는 것으로 끝내지 않고, 아래 세 가지가 **동작하는 것을 직접 보여 줍니다.** 동료나 리뷰어 앞에서 시연하거나, 터미널 출력과 화면 녹화를 남기세요.

| 보여 줄 것 | 어떻게 | 통과 기준 |
|---|---|---|
| 타입 생성 | 스펙에서 `schema.d.ts` 를 만들고, 스펙이 바뀌었을 때 `typecheck` 가 깨지는 곳을 목록으로 보여 준다 (6주) | `gen:api` → `typecheck` 통과. 필드 이름을 바꾼 스펙에서는 오류 목록이 나오고, 원복하면 다시 통과 |
| 에러 분기 | 400/401/404/409 가 서로 다른 처리로 이어지는 것을 요청과 화면에서 보여 준다 (7주) | 상태 코드 4종이 나오고, 화면에서 400 은 입력칸 옆, 409·404 는 폼 위 알림, 401 은 로그인 안내로 갈라진다 |
| 쿠키 인증 | 로그인 → 쿠키를 싣고 쓰기 → 로그아웃 → 다시 401 의 흐름과 쿠키 속성, CORS 허용 목록을 보여 준다 (이번 주) | 쿠키 속성이 `HttpOnly; SameSite=Lax`, 쓰기는 쿠키가 있을 때만 201, 로그아웃 뒤에는 401, 허용 안 된 origin 은 403 |

### 준비

백엔드를 새로 켜고(`cd labs/backend && ./gradlew :week25:bootRun`), 프론트 패키지를 설치(`pnpm -C labs/frontend install`)해 두세요. 이 스크립트는 `labs/frontend/src/api/schema.d.ts` 를 잠깐 바꿨다가 되돌립니다.

### 1) 터미널: 자동 점검

저장소 루트에서 아래를 그대로 붙여 넣어 실행합니다. 세 영역에서 요청을 보내고 `PASS`/`FAIL` 로 결과를 알려 줍니다. (bash 와 zsh 에서 모두 실행됩니다.)

```bash
B=localhost:8080; J='Content-Type: application/json'
fe() { pnpm -C labs/frontend "$@"; }
JAR=$(mktemp); SPECS=$(mktemp -d); FAILS=0
ok()   { echo "PASS  $1"; }
fail() { echo "FAIL  $1"; FAILS=$((FAILS+1)); }
check() { if [ "$2" = "$3" ]; then ok "$1 ($3)"; else fail "$1 (기대 $3, 실제 $2)"; fi; }
status() { curl -s -o /dev/null -w '%{http_code}' "$@"; }

echo "== 1. 타입 생성"
curl -s $B/v3/api-docs | jq . | diff -q - labs/backend/week25/openapi.json >/dev/null && ok "서버가 내놓는 스펙 = 커밋된 openapi.json" || fail "스펙이 어긋났다"
fe gen:api >/dev/null 2>&1 && fe typecheck >/dev/null 2>&1 && ok "gen:api 후 typecheck 통과" || fail "gen:api 또는 typecheck 실패"
jq '.components.schemas.BookResponse |= (.properties |= with_entries(if .key=="title" then .key="name" else . end) | .required |= map(if .=="title" then "name" else . end))' \
  labs/backend/week25/openapi.json > $SPECS/title-to-name.json
fe exec openapi-typescript $SPECS/title-to-name.json -o src/api/schema.d.ts >/dev/null 2>&1
N=$(fe typecheck 2>&1 | grep -c 'error TS')
fe gen:api >/dev/null 2>&1
[ "$N" -gt 0 ] && ok "필드 이름 변경(title->name)을 typecheck 가 $N 곳에서 잡았다" || fail "typecheck 가 변경을 잡지 못했다"
fe typecheck >/dev/null 2>&1 && ok "원복 후 typecheck 통과" || fail "원복되지 않았다"

echo "== 2. 에러 분기"
ISBN="m-$(date +%s)"; BOOK="{\"isbn\":\"$ISBN\",\"title\":\"t\",\"author\":\"a\"}"
check "로그인 없이 책 등록 -> auth" "$(status -X POST -H "$J" -d "$BOOK" $B/books)" 401
check "틀린 비밀번호 -> auth" "$(status -X POST -H "$J" -d '{"username":"demo","password":"nope"}' $B/auth/login)" 401
check "로그인" "$(status -c $JAR -X POST -H "$J" -d '{"username":"demo","password":"demo1234"}' $B/auth/login)" 200
check "빈 isbn -> field" "$(status -b $JAR -X POST -H "$J" -d '{"isbn":"","title":"t","author":"a"}' $B/books)" 400
check "새 책 등록" "$(status -b $JAR -X POST -H "$J" -d "$BOOK" $B/books)" 201
check "같은 isbn 다시 -> conflict" "$(status -b $JAR -X POST -H "$J" -d "$BOOK" $B/books)" 409
check "없는 책 조회 -> notFound" "$(status $B/books/999999)" 404
fe test 2>&1 | grep -q 'Tests  30 passed' && ok "pnpm test: Vitest 30개 통과" || fail "pnpm test 실패"

echo "== 3. 쿠키 인증"
JAR2=$(mktemp)
LOGIN=$(curl -si -c $JAR2 -X POST -H "$J" -d '{"username":"demo","password":"demo1234"}' $B/auth/login)
echo "$LOGIN" | grep -i '^set-cookie: JSESSIONID' | grep -q 'HttpOnly; SameSite=Lax' && ok "Set-Cookie: JSESSIONID, HttpOnly, SameSite=Lax" || fail "쿠키 속성이 다르다"
check "쿠키 없이 쓰기 -> 막힘" "$(status -X POST -H "$J" -d '{"name":"m"}' $B/members)" 401
check "쿠키를 싣고 쓰기" "$(status -b $JAR2 -X POST -H "$J" -d '{"name":"m"}' $B/members)" 201
check "로그아웃" "$(status -b $JAR2 -X POST $B/auth/logout)" 204
check "로그아웃한 쿠키로 /auth/me" "$(status -b $JAR2 $B/auth/me)" 401
check "허용 origin 의 preflight" "$(status -X OPTIONS -H 'Origin: http://localhost:3000' -H 'Access-Control-Request-Method: POST' $B/loans)" 200
check "허용 안 된 origin 의 preflight" "$(status -X OPTIONS -H 'Origin: http://evil.example' -H 'Access-Control-Request-Method: POST' $B/loans)" 403

[ $FAILS -eq 0 ] && echo "마일스톤 자동 점검: 모두 통과" || echo "마일스톤 자동 점검: 실패 $FAILS 개"
```

모두 통과하면 이렇게 출력됩니다. 책 `isbn` 은 실행 시각으로 만들기 때문에 다시 실행해도 `409` 와 `201` 이 어긋나지 않습니다.

```text
== 1. 타입 생성
PASS  서버가 내놓는 스펙 = 커밋된 openapi.json
PASS  gen:api 후 typecheck 통과
PASS  필드 이름 변경(title->name)을 typecheck 가 11 곳에서 잡았다
PASS  원복 후 typecheck 통과
== 2. 에러 분기
PASS  로그인 없이 책 등록 -> auth (401)
PASS  틀린 비밀번호 -> auth (401)
PASS  로그인 (200)
PASS  빈 isbn -> field (400)
PASS  새 책 등록 (201)
PASS  같은 isbn 다시 -> conflict (409)
PASS  없는 책 조회 -> notFound (404)
PASS  pnpm test: Vitest 30개 통과
== 3. 쿠키 인증
PASS  Set-Cookie: JSESSIONID, HttpOnly, SameSite=Lax
PASS  쿠키 없이 쓰기 -> 막힘 (401)
PASS  쿠키를 싣고 쓰기 (201)
PASS  로그아웃 (204)
PASS  로그아웃한 쿠키로 /auth/me (401)
PASS  허용 origin 의 preflight (200)
PASS  허용 안 된 origin 의 preflight (403)
마일스톤 자동 점검: 모두 통과
```

`FAIL` 이 나오면 줄의 설명이 가리키는 영역으로 돌아가세요. 1) 영역의 `FAIL` 은 6주, 2) 영역은 7주, 3) 영역은 이번 주의 해당 절을 다시 보면 됩니다.

### 2) 브라우저: 화면에서 보여 주기

터미널 점검은 상태 코드까지만 보여 줍니다. 화면에서 갈라지는 모습은 브라우저에서 직접 보여 줍니다. 프론트를 기본 모드(프록시)로 띄웁니다.

```bash
pnpm -C labs/frontend dev      # http://localhost:3000
```

| # | 할 일 | 화면에서 보여야 하는 것 |
|---|------|------|
| 1 | 로그인 전에 `책 등록` 버튼을 본다 | 버튼이 비활성이고 `로그인 후 사용할 수 있습니다` |
| 2 | 아이디 `demo`, 비밀번호 `틀림` 으로 로그인 | `로그인이 필요합니다` 제목 아래에 서버 메시지 `아이디 또는 비밀번호가 올바르지 않습니다` (`auth` 종류) |
| 3 | `demo` / `demo1234` 로 로그인한 뒤 새로고침 | `demo 님` 이 그대로 보임 (쿠키로 이어진 로그인) |
| 4 | isbn 을 비우고 제목·저자를 채워 `책 등록` | isbn 입력칸 아래에 `공백일 수 없습니다`. 폼 위 알림은 없음 (`field` 종류) |
| 5 | 같은 isbn 으로 책을 두 번 등록 | 두 번째에 폼 위 알림 `이미 처리되었거나 규칙에 어긋납니다` + `이미 등록된 isbn 입니다: ...`. isbn 입력칸은 오류 표시가 아님 (`conflict` 종류) |
| 6 | `회원` 화면에서 회원을 한 명 등록한 뒤(결과에 회원 id 가 표시됨), `대출·반납` 화면에서 그 회원 id 와 없는 책 id `99` 로 대출 | `찾을 수 없습니다` 제목 아래에 `책을 찾을 수 없습니다: 99` (`notFound` 종류) |
| 7 | DevTools → Application → Cookies 에서 `JSESSIONID` 를 보고, Console 에 `document.cookie` 를 친다 | `HttpOnly` 체크, `SameSite` 는 `Lax`, 도메인은 `localhost`. `document.cookie` 는 `""` |
| 8 | `로그아웃` 을 누른다 | 로그인 폼이 돌아오고 `대출` 같은 쓰기 버튼이 다시 비활성 |

(위 표의 문구와 상태는 백엔드를 새로 켠 직후 같은 순서를 브라우저 자동화로 실행해 확인한 값입니다. 서버에 이미 데이터가 있어도 결과는 같지만, 6번은 회원 id 가 실제로 있어야 책 쪽 `404` 가 나옵니다. 없는 회원 id 를 넣으면 `회원을 찾을 수 없습니다` 가 먼저 나옵니다.)

화면 확인을 자동으로 하고 싶다면 Playwright e2e 가 같은 흐름(1, 3~5, 8번)을 `data-kind` 속성으로 확인합니다. 처음이라면 `pnpm -C labs/frontend exec playwright install chromium` 을 한 번 실행한 뒤 `pnpm -C labs/frontend e2e` 를 실행하세요. 12주에서 자세히 다룹니다.

### 마무리

- [ ] 자동 점검이 모두 `PASS` 이고, 그 출력을 남겼다
- [ ] 브라우저 8단계를 직접 시연했다
- [ ] 스펙이 바뀌었을 때 `typecheck` 가 깨지는 파일 목록을 보고, 어느 변경이 어느 화면에 영향을 주는지 말로 설명했다
- [ ] 8080·3000 포트의 서버를 모두 종료했다

## 체크리스트

- [ ] 12개 요청의 상태 코드와 `Access-Control-` 헤더 유무를 예측하고, 틀린 것의 이유를 적었다
- [ ] 브라우저 콘솔에서 CORS 오류를 직접 만들었고, 자바스크립트가 상태 코드를 못 본다는 것을 확인했다
- [ ] `document.cookie` 에 `JSESSIONID` 가 안 보이는 이유(`HttpOnly`)를 설명할 수 있다
- [ ] 망가뜨리기 6개를 해 보고 모두 원복했다. `SecurityAndDocsTest` 7개가 다시 통과한다
- [ ] 세션 쿠키와 `localStorage` JWT 의 XSS 차이를 설명할 수 있다
- [ ] 이 저장소의 CSRF 가 어떤 가정 위에 꺼져 있는지, 운영에서는 어떻게 켜는지 말할 수 있다
- [ ] 마일스톤 체크(타입 생성 · 에러 분기 · 쿠키 인증)를 직접 보여 줬다
- [ ] 8080·3000 포트의 서버를 모두 종료했다
