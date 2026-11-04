# 27주차. 인증·CORS: 브라우저가 쿠키를 들고 API 를 부르기까지

**예상 시간:** 8~10시간 | **목표:** 프론트(`localhost:3000`)에서 백엔드(`localhost:8080`)를 부를 때 브라우저가 무엇을 막고 무엇을 허용하는지 헤더 단위로 설명한다. 세션 쿠키 로그인이 어떻게 이어지는지 확인하고, 401·CORS 실패를 화면에서 구분한다.

> 백엔드 명령은 `../../labs/backend/`, 프론트 명령은 `../../labs/frontend/` 에서 실행합니다. 25·26주차에서 쓴 `../../labs/frontend` 의 `ApiError` 래퍼를 그대로 씁니다. 이번 주 백엔드는 24주차 API 에 **Spring Security 로그인과 CORS 설정**이 붙은 `week25` 모듈입니다.

## 🖐 먼저 손으로 (읽기 전에 하세요)

> 이 주차의 핵심은 아래 단계를 **직접 타이핑하면서** 깨닫는 것입니다. 아래쪽 설명 문서는 먼저 읽지 않아도 됩니다. 막혔을 때 사전처럼 펼쳐 보세요.

### 1단계. 응답 예측하기 → 보내기 → 비교하기 (40분)

백엔드를 켭니다. (Ctrl+C 로 종료)

```bash
cd ../../labs/backend
./gradlew :week25:bootRun
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

다른 터미널에서 위에서부터 한 번에 실행합니다. (서버를 새로 켠 직후 한 번만. 10번이 회원을 만듭니다.)

```bash
O='Origin: http://localhost:3000'
H='Content-Type: application/json'
echo "1"; curl -si -X OPTIONS localhost:8080/loans -H "$O" -H 'Access-Control-Request-Method: POST' -H 'Access-Control-Request-Headers: content-type' | grep -iE '^HTTP|^Access-Control'
echo "2"; curl -si -X OPTIONS localhost:8080/loans -H 'Origin: http://evil.example' -H 'Access-Control-Request-Method: POST' | grep -iE '^HTTP|^Access-Control|^Invalid'
echo "3"; curl -si -X OPTIONS localhost:8080/loans -H "$O" -H 'Access-Control-Request-Method: PUT' | grep -iE '^HTTP|^Access-Control|^Invalid'
echo "4"; curl -si -X OPTIONS localhost:8080/loans -H "$O" -H 'Access-Control-Request-Method: POST' -H 'Access-Control-Request-Headers: x-foo' | grep -iE '^HTTP|^Access-Control|^Invalid'
echo "5"; curl -si localhost:8080/books | grep -iE '^HTTP|^Access-Control'
echo "6"; curl -si localhost:8080/books -H "$O" | grep -iE '^HTTP|^Access-Control'
echo "7"; curl -s -w ' [%{http_code}]\n' -X POST localhost:8080/members -H "$H" -d '{"name":"하승"}'
echo "8"; curl -s -w ' [%{http_code}]\n' -X POST localhost:8080/auth/login -H "$H" -d '{"username":"demo","password":"nope"}'
echo "9"; curl -si -c /tmp/jar.txt -X POST localhost:8080/auth/login -H "$H" -d '{"username":"demo","password":"demo1234"}' | grep -iE '^HTTP|^Set-Cookie'
echo "10"; curl -s -b /tmp/jar.txt -w ' [%{http_code}]\n' -X POST localhost:8080/members -H "$H" -d '{"name":"하승"}'
echo "11"; curl -si -b /tmp/jar.txt -X POST localhost:8080/auth/logout | grep -iE '^HTTP'
echo "12"; curl -s -b /tmp/jar.txt -w ' [%{http_code}]\n' localhost:8080/auth/me
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
cd ../../labs/frontend
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
B  Access to fetch at 'http://localhost:8080/loans' from origin 'http://localhost:3000' has been blocked by CORS policy:
   Response to preflight request doesn't pass access control check: No 'Access-Control-Allow-Origin' header is present on the requested resource.
   -> Uncaught TypeError: Failed to fetch
C  (B 와 같은 형식, 주소만 /books)
D  ""
```

- ② 자바스크립트는 **상태 코드도 본문도 못 봅니다.** 서버는 403 `Invalid CORS request` 를 보냈지만 브라우저가 응답을 감추고 `TypeError: Failed to fetch` 만 줍니다. 그래서 "CORS 오류는 서버 로그와 DevTools Network 탭에서 찾는다"가 규칙입니다.
- D: 로그인을 했는데도 `document.cookie` 가 빈 문자열입니다. `HttpOnly` 쿠키는 스크립트에서 보이지 않습니다. 그래서 XSS 로 스크립트가 심겨도 세션 쿠키 값을 읽어 가지 못합니다. (Application 탭에서는 보입니다.)
- 이 모드에서 페이지를 열면 콘솔에 `/auth/me` 의 401 (`Failed to load resource: ... 401`)이 한 줄 뜹니다. 로그인 전에 현재 사용자를 물어봤기 때문이며 정상입니다.

</details>

### 3단계. 일부러 망가뜨리기 (30분)

백엔드 테스트가 이 설정을 지키고 있습니다. 하나씩 고장 내고 어느 테스트가 실패하는지 확인한 뒤 **반드시 원복**하세요.

```bash
cd ../../labs/backend
./gradlew :week25:test --tests 'library.SecurityAndDocsTest'    # 먼저 7개 모두 통과하는지 확인
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
// ../../labs/backend/week25/src/main/java/library/SecurityConfig.java
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

## 2. 해결 방식 두 가지 비교

`../../labs/frontend` 는 두 방식을 모두 지원합니다. 환경 변수 하나(`NEXT_PUBLIC_API_BASE`)로 바뀝니다.

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

| | (a) 직접 호출 + CORS 허용 | (b) Next `rewrites` 프록시 |
|---|---|---|
| 설정 | `NEXT_PUBLIC_API_BASE=http://localhost:8080` | 비워 둠 (기본). 브라우저는 `/api/...` 만 호출 |
| 브라우저가 보는 origin | 프론트와 백엔드 두 개 | 프론트 하나 (same-origin) |
| preflight | JSON POST 마다 `OPTIONS` 가 먼저 나감 | 없음 |
| 백엔드 CORS 설정 | 필요 (`SecurityConfig`). 틀리면 2단계 같은 오류 | 브라우저 입장에서는 불필요. 단 아래 주의 참고 |
| 쿠키 | 응답이 `localhost:8080` 에서 옴. **SameSite·도메인 규칙을 이해해야 함** | `localhost:3000` 의 쿠키처럼 보임 |
| 운영 구성 | 프론트·백엔드 도메인이 따로. 허용 origin 관리 | 프론트 서버가 백엔드 주소를 알아야 함. 요청이 프론트 서버를 한 번 더 지남 |
| 막히는 곳 | CORS 오류 (브라우저에서만 재현) | 백엔드가 꺼지면 프록시가 500 (아래) |

두 모드 모두 Playwright e2e 두 개가 통과합니다. (`pnpm e2e` 는 기본이 프록시 모드이고, `NEXT_PUBLIC_API_BASE=http://localhost:8080 pnpm e2e` 가 직접 호출 모드입니다. 28주차에서 실행합니다.)

**프록시 모드의 주의 두 가지**

1. **백엔드 쪽 `Origin` 검사는 남아 있습니다.** 브라우저는 same-origin `POST` 에도 `Origin` 헤더를 보내고, 프록시는 그것을 그대로 넘깁니다. 그래서 프론트를 `localhost:3000` 이 아닌 주소(다른 호스트·포트, 배포 도메인)로 열면 프록시를 거쳐도 Spring 이 403 을 줍니다. 아래는 이를 curl 로 흉내 낸 것입니다 (`pnpm dev` 와 백엔드가 떠 있고 로그인한 쿠키 `/tmp/jar2.txt` 가 있을 때).

   ```bash
   curl -si -c /tmp/jar2.txt -X POST localhost:3000/api/auth/login -H 'Content-Type: application/json' -d '{"username":"demo","password":"demo1234"}' | grep -iE '^HTTP|^set-cookie'
   curl -si -b /tmp/jar2.txt -X POST localhost:3000/api/members -H 'Origin: http://other.example:3000' -H 'Content-Type: application/json' -d '{"name":"x"}' | grep -iE '^HTTP|Invalid'
   ```

   ```text
   HTTP/1.1 200 OK
   set-cookie: JSESSIONID=<값>; Path=/; HttpOnly; SameSite=Lax
   HTTP/1.1 403 Forbidden
   Invalid CORS request
   ```

   배포 도메인이 정해지면 `allowedOrigins` 에 넣는다는 사실을 백엔드 담당자와 합의해 두세요.
2. **백엔드가 꺼져 있으면 프록시가 500 `Internal Server Error`(텍스트)를 돌려줍니다.** `{"message"}` 형식이 아니므로 `client.ts` 의 `messageOf` 가 `요청에 실패했습니다 (500)` 로 바꿉니다. 화면에서 "서버가 꺼졌다"와 "내가 잘못 보냈다"를 구분하려면 이 형식 차이를 이용합니다.

**SameSite 와 "같은 사이트"**: 포트는 site 판정에 들어가지 않습니다. `localhost:3000` 과 `localhost:8080` 은 포트만 다른 **same-site** 라서, 직접 호출 모드에서도 `SameSite=Lax` 쿠키가 실립니다. 직접 호출 e2e 가 통과하는 이유입니다. 프론트와 백엔드가 **서로 다른 사이트**(예: `a.com` 과 `b.com`)가 되면 `Lax` 쿠키는 fetch 요청에 실리지 않으므로 `SameSite=None; Secure` 가 필요합니다. (이 저장소에서 실행해 보지는 않았습니다. 규칙만 알아 두세요.)

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

`../../labs/frontend` 는 401 을 상황마다 다르게 다룹니다.

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

## 5. Next Route Handler(BFF)는 어디에 놓이나 (개념 비교)

> 이 저장소에는 **구현되어 있지 않습니다.** 선택지를 이해하는 용도입니다.

BFF(Backend For Frontend)는 브라우저와 Spring 사이에 프론트 팀이 소유하는 얇은 서버를 두는 구조입니다. Next 에서는 Route Handler(`app/api/**/route.ts`)가 그 자리입니다.

| | 직접 호출 (a) | `rewrites` 프록시 (b, 현재) | Route Handler BFF |
|---|---|---|---|
| 브라우저 → | Spring | Next → Spring (요청을 그대로 전달) | Next 코드 → Spring (요청을 해석하고 다시 만듦) |
| 토큰·세션 | 브라우저가 쿠키로 | 브라우저가 쿠키로 | **Next 서버가 보관** 가능. 브라우저에는 Next 쿠키만 |
| JWT 를 쓸 때 | 저장 위치 고민이 프론트에 남음 | 동일 | 토큰이 브라우저에 아예 안 내려감 |
| 응답 가공·여러 API 합치기 | 불가 | 불가 | 가능 |
| 비용 | 없음 | 설정 한 줄 | 엔드포인트마다 코드·테스트·배포 구성 |
| 이 저장소 | 지원 | 기본값 | 없음 |

프록시는 "경로만 다리를 놓는" 것이고, BFF 는 "프론트 팀이 API 모양을 다시 정하는" 것입니다. 한 사람이 프론트·백엔드를 모두 하는 소규모에서는 (b)로 충분하고, 백엔드가 토큰을 JWT 로 주는데 브라우저에 두기 싫을 때 BFF 가 고려 대상이 됩니다.

## Spring/Java 용어 ↔ 프론트 용어

| Spring/Java 쪽 | 프론트 쪽에서 닮은 것 |
|---|---|
| `SecurityFilterChain` (요청이 컨트롤러에 닿기 전에 지나는 필터 줄) | Express/Next 의 미들웨어 체인 |
| `CorsConfigurationSource` | 프론트에는 설정 대상이 없다. `Access-Control-*` 응답 헤더와 `Failed to fetch` 로 **결과로만 만난다** |
| `HttpSession` + `JSESSIONID` | 서버가 상태를 들고, 브라우저는 열쇠만 쥔다. `document.cookie` 에 안 보이는 쿠키 |
| `authenticationEntryPoint` (401 을 만드는 곳) | `fetch` 래퍼가 401 을 한 곳에서 `ApiError(auth)` 로 바꾸는 곳 |
| `@RestControllerAdvice` | `ApiError` 래퍼 (22주차) |
| `InMemoryUserDetailsManager` (고정 데모 사용자) | MSW 핸들러의 `demo`/`demo1234` 로그인 |
| `CookieCsrfTokenRepository` + `X-XSRF-TOKEN` | 쿠키에서 토큰을 읽어 쓰기 요청 헤더에 싣는 `fetch` 래퍼 |
| 리버스 프록시 (`nginx proxy_pass`) | `next.config.ts` 의 `rewrites`, Vite 의 `server.proxy` |

## 연습문제

1. `SecurityConfig` 의 허용 origin 에 `http://127.0.0.1:3000` 을 추가하고, 1단계 1번을 `Origin: http://127.0.0.1:3000` 으로 다시 보내 200 이 되는지 확인하세요. 확인 후 원복합니다.
2. 직접 호출 모드(`NEXT_PUBLIC_API_BASE=http://localhost:8080`)에서 `src/api/client.ts` 의 `credentials: "include"` 를 지우고, 로그인 직후 새로고침했을 때 로그인 상태가 어떻게 되는지 확인하세요. 왜 그런지 `Set-Cookie` 와 연결해 한 문장으로 쓰세요. (원복)
3. 프록시 모드에서 로그인한 뒤 DevTools 의 Application → Cookies 에서 `JSESSIONID` 를 찾아 `HttpOnly` 와 `SameSite` 칸을 읽으세요. 쿠키의 도메인이 어디로 표시되나요?
4. 운영 배포가 프론트 `https://app.example.com`, 백엔드 `https://api.example.com` 이라고 가정합니다. 이 문서의 설정 중 **바꿔야 하는 것 3가지**를 쓰세요. (힌트: 허용 origin, 쿠키 속성, 그리고 `app` 과 `api` 가 같은 사이트인지)
5. 로그아웃 후 이전 `JSESSIONID` 로 `GET /auth/me` 를 보내면 401 입니다(1단계 12번). 이것이 JWT 방식에서는 왜 더 어려운 일인지 3절 표를 참고해 설명하세요.

## 퀴즈

1. `localhost:3000` 에서 `localhost:8080` 으로 `fetch` 하면 "다른 origin" 인 이유는?
2. curl 로는 되는 요청이 브라우저에서만 `Failed to fetch` 가 됩니다. 왜 그런가요? 이 에러에서 자바스크립트가 알 수 있는 것은?
3. JSON 본문을 가진 `POST` 에 preflight 가 생기는 이유와, 그 응답에 들어가야 하는 헤더 네 가지는?
4. `Access-Control-Allow-Origin: *` 로는 쿠키 인증을 못 하는 이유는?
5. `HttpOnly` 쿠키는 XSS 에서 무엇을 막고 무엇을 막지 못하나요?
6. 이 저장소가 CSRF 를 꺼도 되는 근거와, 그 근거의 약점은?
7. `rewrites` 프록시를 쓰면 CORS 설정이 완전히 필요 없어지나요?

<details>
<summary>정답</summary>

1. origin 은 스킴·호스트·포트의 조합이고 포트가 다르기 때문
2. CORS 는 브라우저가 집행한다. curl 은 `Origin` 을 보내지도 응답 헤더를 검사하지도 않는다. 자바스크립트는 `TypeError` 가 났다는 사실만 알고 상태 코드·본문은 못 본다. 원인은 Network 탭과 서버 로그에서 찾는다
3. JSON 은 폼 전송용이 아닌 `Content-Type` 이라 브라우저가 먼저 `OPTIONS` 로 허용 여부를 묻는다. 응답에는 `Allow-Origin`, `Allow-Methods`, `Allow-Headers`, (쿠키를 쓰면) `Allow-Credentials`
4. 브라우저가 `credentials: include` 요청의 응답에서 `*` 를 거부한다. 쿠키를 허용하려면 origin 을 정확히 적어야 한다
5. 막는 것: 스크립트가 쿠키 값을 읽어 가져가는 것. 못 막는 것: 이미 실행 중인 스크립트가 그 페이지에서 요청을 보내 로그인 상태를 이용하는 것
6. 근거: `SameSite=Lax` 가 다른 사이트에서 시작된 `POST` 에 쿠키를 싣지 않는다. 약점: 방어가 한 겹이고, `Lax` 는 최상위 이동 `GET` 에는 쿠키를 싣는다(그래서 `GET` 이 상태를 바꾸면 안 됨). 같은 사이트의 다른 서브도메인에서 오는 공격은 못 막는다. 운영에서는 CSRF 토큰을 켠다
7. 아니다. 브라우저 입장에서 preflight 는 없어지지만, 프록시가 `Origin` 헤더를 그대로 넘기므로 백엔드의 `Origin` 검사는 남는다. 프론트가 열리는 주소가 허용 목록에 있어야 한다

</details>

## 체크리스트

- [ ] 12개 요청의 상태 코드와 `Access-Control-` 헤더 유무를 예측하고, 틀린 것의 이유를 적었다
- [ ] 브라우저 콘솔에서 CORS 오류를 직접 만들었고, 자바스크립트가 상태 코드를 못 본다는 것을 확인했다
- [ ] `document.cookie` 에 `JSESSIONID` 가 안 보이는 이유(`HttpOnly`)를 설명할 수 있다
- [ ] 망가뜨리기 6개를 해 보고 모두 원복했다. `SecurityAndDocsTest` 7개가 다시 통과한다
- [ ] 직접 호출과 `rewrites` 프록시의 차이를 preflight·쿠키·운영 구성 세 가지로 말할 수 있다
- [ ] 세션 쿠키와 `localStorage` JWT 의 XSS 차이를 설명할 수 있다
- [ ] 이 저장소의 CSRF 가 어떤 가정 위에 꺼져 있는지, 운영에서는 어떻게 켜는지 말할 수 있다
- [ ] 8080·3000 포트의 서버를 모두 종료했다
