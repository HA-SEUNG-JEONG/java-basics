# 9주차. 로컬 통합 개발 환경: 8080 + 3000, 그리고 백엔드 없이 개발하기

**예상 시간:** 6~8시간 (하루 1시간 세션 6회) | **목표:** Spring(8080)과 Next(3000)를 순서대로 띄워 한 화면으로 동작시키고, 누가 누구를 부르는지 설명한다. 환경 변수로 호출 대상(프록시, 직접 호출, 가짜 서버)을 바꿔 가며 차이를 비교하고, 서버가 없거나 포트가 어긋났을 때의 증상으로 문제가 어느 층에 있는지 좁힌다. MSW 로 백엔드 없이 화면을 개발하는 방법과 그 위험을 안다.

> 6주(API 계약), 7주(에러 처리), 8주(인증·CORS)를 먼저 끝내세요. 실습 대상은 같은 참조 API `../labs/backend/week25` 와 클라이언트 `../labs/frontend` 입니다. 이 API 는 백엔드 팀이 준 것으로 보고 켜서 호출만 하며, 백엔드 소스는 고치지 않습니다. 명령은 별도 언급이 없으면 저장소 루트에서 실행합니다.
>
> 준비물: JDK 21, Node 22.12+, pnpm 10+, `curl`. 프론트 패키지는 처음이라면 `pnpm -C labs/frontend install` 을 먼저 합니다. MSW 로 계약 테스트를 쓰는 일과 Playwright e2e 는 12주에서 다룹니다. 이 주차는 "켜고 끄는 법과 위험"까지만 봅니다.

## 하루 1시간으로 나누면

이 주차는 4단계가 모두 필수라서 하루 1시간씩 6번의 세션으로 나눕니다. 한 세션은 50~70분이고, 4절의 소절 "위험" 은 4단계의 실험 C 와 함께 읽도록 4번 세션에 두었습니다.

| 세션 | 할 일 | 끝났을 때 할 수 있어야 하는 것 |
|---|---|---|
| 1 | 1단계(기동 순서와 "서버가 없을 때" 예측하기, 30분)를 하고, 1절(로컬 통합: 누가 누구를 부르는가)과 2절(환경 변수)을 읽는다 | 백엔드 → 프론트 순서로 띄우고, 서버가 없을 때의 증상을 예측할 수 있다 |
| 2 | 2단계(같은 화면, 두 서버 비교, 40분)를 하고, 4절(백엔드 없이 개발하기: MSW 켜고 끄기)을 읽는다. 소절 "위험" 은 4번 세션에서 읽는다 | 같은 화면을 실서버와 MSW 두 모드로 띄워 5가지 차이를 확인할 수 있다 |
| 3 | 3단계(호출 대상 바꿔 보기: 프록시와 직접 호출, 40분)를 하고, 3절(프록시와 직접 호출: 어느 쪽으로 부를까)을 읽는다 | 프록시 모드와 직접 호출 모드에서 Network 탭의 요청 주소와 preflight 유무를 비교하고, `NEXT_PUBLIC_API_BASE` 와 `NEXT_PUBLIC_API_MOCKING` 이 각각 무엇을 바꾸는지 말할 수 있다 |
| 4 | 4단계(일부러 망가뜨리기, 50분)의 실험 A, B, C를 하고, 4절의 소절 "위험" (목은 "내 기억"일 뿐이다)과 5절(디버깅: 문제가 어느 층에 있는가)을 읽는다 | 3000 포트가 막혔을 때의 증상과 이유를 설명하고, `curl` 두 줄(8080, 3000/api)로 문제가 어느 겹에 있는지 가를 수 있다 |
| 5 | 연습문제와 퀴즈를 푼다 (압축 코스에서는 건너뜀) | 목의 상태 코드를 바꿔 테스트 1개만 실패하는 것을 확인하고 원복할 수 있다 |
| 6 | TS 와 비교 절을 읽고, 체크리스트의 각 항목을 내 말로 설명해 보고, 막히는 항목은 해당 절을 다시 읽은 뒤 학습 노트를 정리한다. 마지막으로 서버를 모두 종료한다 | 서버를 모두 종료한 뒤 `lsof` 로 8080·3000 포트가 비었는지 확인할 수 있다 |

세션을 시작하기 전에 이전 세션의 마지막 결과를 1~2분 훑어봅니다. 막히면 아래 해당 개념 절을 사전처럼 펼쳐 보세요.

## 🖐 먼저 손으로 (읽기 전에 하세요)

> 이 주차의 핵심은 아래 단계를 **직접 타이핑하면서** 깨닫는 것입니다. 아래쪽 설명 문서는 먼저 읽지 않아도 됩니다. 막혔을 때 사전처럼 펼쳐 보세요.

### 1단계. 기동 순서와 "서버가 없을 때" 예측하기 (30분)

터미널을 두 개 씁니다. 먼저 **프론트만** 켭니다. (백엔드는 아직 켜지 않습니다. 8080 포트가 비어 있어야 합니다.)

```bash
lsof -i :8080                       # 아무것도 안 나와야 함
# 터미널 B
cd labs/frontend && pnpm dev
```

아래 세 가지를 순서대로 하기 전에, 결과를 노트에 예측하세요.

| # | 할 일 | 내 예측 |
|---|------|------|
| 1 | 터미널 C 에서 `curl -si localhost:3000/api/books \| head -1` | |
| 2 | 브라우저로 `http://localhost:3000` 을 열면 화면 위쪽(로그인 바)은? | |
| 3 | **`pnpm dev` 를 끄지 않고** 터미널 A 에서 백엔드를 켠 뒤(`cd labs/backend && ./gradlew :week25:bootRun`) 1번을 다시 하면? 본문은? | |

<details>
<summary>정답 (예측을 적은 뒤에 여세요)</summary>

```text
1  HTTP/1.1 500 Internal Server Error
2  로그인 입력칸 대신 오류 알림("요청에 실패했습니다" + "요청에 실패했습니다 (500)")
3  200, 본문 []   (프론트를 다시 시작하지 않아도 된다)
```

- 1번: Next 의 프록시(`rewrites`)가 8080 에 연결하지 못하면 500 과 텍스트 `Internal Server Error` 를 돌려줍니다. 백엔드가 주는 `{"message": ...}` 형식이 아닙니다. 프론트의 `ApiError` 래퍼는 이런 응답을 `요청에 실패했습니다 (500)` 으로 바꿉니다. 이때 `pnpm dev` 를 띄운 터미널에는 `Failed to proxy http://localhost:8080/books` 로 시작하는 오류가 찍힙니다.
- 2번: `useMe` 는 `/auth/me` 가 **401 일 때만** "비로그인"으로 봅니다. 500 은 진짜 오류라서 로그인 폼이 아니라 오류 알림이 나옵니다.
- 3번: 프록시는 요청이 올 때마다 8080 에 연결합니다. 그래서 **켜는 순서는 어느 쪽이 먼저든 동작은 하지만, 백엔드를 먼저 켜는 것이 오류 화면을 안 보는 순서**입니다. `[]` 인 이유는 DB 가 H2 **메모리** DB 라서 비어 있기 때문입니다.

</details>

### 2단계. 같은 화면, 두 서버 비교 (40분)

화면(React 코드)은 하나도 바꾸지 않고 **API 응답을 누가 주느냐만** 바꿔 봅니다. 아래 5가지를 두 모드에서 각각 해 보고, 모드별 결과를 먼저 예측하세요. (브라우저 언어는 한국어로 둡니다.)

```bash
# 실서버 모드: 터미널 A 에서 백엔드를 "새로" 켠 뒤 (재시작해서 DB 를 비웁니다)
cd labs/frontend && pnpm dev

# MSW 모드: 위 pnpm dev 를 Ctrl+C 로 끄고, 백엔드도 꺼도 됩니다
cd labs/frontend && NEXT_PUBLIC_API_MOCKING=enabled pnpm dev
```

| # | 할 일 (`http://localhost:3000`) | 실서버 모드 예측 | MSW 모드 예측 |
|---|------|------|------|
| 1 | 처음 열었을 때 책 목록 | | |
| 2 | `demo` / `demo1234` 로그인 후 **새로고침** | | |
| 3 | 로그인한 상태에서 isbn 을 비우고 책 등록. 입력칸 아래 문구는? | | |
| 4 | isbn `111`, 제목·저자 아무거나로 책 등록을 **두 번** | | |
| 5 | isbn `999` 로 등록한 뒤 **새로고침**. 등록한 책은? | | |

<details>
<summary>정답</summary>

| # | 실서버 모드 | MSW 모드 |
|---|------|------|
| 1 | 비어 있음 | `이펙티브 자바`, `Clean Code` 2권 (미리 들어 있음) |
| 2 | 로그인 유지 (쿠키) | 로그아웃 상태로 돌아감 (로그인 상태가 쿠키가 아니라 코드 안의 변수 하나라서) |
| 3 | `공백일 수 없습니다` | `must not be blank` |
| 4 | 첫 번째는 등록됨, 두 번째는 409 알림 | **첫 번째부터** 409 알림 (`111` 이 미리 들어 있음) |
| 5 | 새로고침해도 목록에 남음 | 목록에서 사라짐 (목 DB 도 페이지 코드 안에 있어서 새로고침이 초기화) |

- **3번이 MSW 를 쓰기 전에 알아야 할 핵심 발견입니다.** 실서버는 `Accept-Language` 헤더(브라우저 언어)에 따라 검증 메시지 언어가 바뀌고, 목은 항상 영어입니다. 화면이 "필드: 메시지" 형식만 가정하고 문구는 가정하지 않아서 둘 다 동작합니다. 문구를 비교하는 테스트를 썼다면 한쪽에서만 통과했을 것입니다.
- 4번처럼 "미리 들어 있는 데이터"와 5번처럼 "새로고침하면 사라지는 상태"는 목이 실서버와 **다르게 행동하는 곳**입니다. 4절에서 이 차이를 어떻게 다룰지 봅니다.

</details>

### 3단계. 호출 대상 바꿔 보기: 프록시와 직접 호출 (40분)

이번에는 가짜 서버 없이, 브라우저가 **어디로 요청을 보내는지**만 바꿉니다. 백엔드를 새로 켜고 프론트를 두 가지 방식으로 차례로 띄웁니다.

```bash
# (1) 기본값: 프록시 모드
cd labs/frontend && pnpm dev

# (2) 직접 호출 모드: (1)을 Ctrl+C 로 끄고
cd labs/frontend && NEXT_PUBLIC_API_BASE=http://localhost:8080 pnpm dev
```

각 모드에서 `http://localhost:3000` 을 열고 DevTools → Network 탭을 연 채(필터는 Fetch/XHR) 새로고침한 다음 `demo` / `demo1234` 로 로그인합니다. 보기 전에 노트에 적으세요.

| # | 질문 | (1) 프록시 | (2) 직접 호출 |
|---|------|------|------|
| 1 | 책 목록 요청의 URL(origin 포함)은? | | |
| 2 | 로그인 때 `POST` 앞에 `OPTIONS` 요청이 있는가? | | |
| 3 | Application → Cookies 에서 `JSESSIONID` 의 도메인 칸은? | | |

<details>
<summary>정답 (예측을 적은 뒤에 여세요)</summary>

```text
(1) 프록시
  GET  http://localhost:3000/api/auth/me      -> 401
  GET  http://localhost:3000/api/books        -> 200
  POST http://localhost:3000/api/auth/login   -> 200        (OPTIONS 없음)
  쿠키: JSESSIONID, 도메인 localhost, HttpOnly, SameSite=Lax

(2) 직접 호출
  GET     http://localhost:8080/auth/me       -> 401
  GET     http://localhost:8080/books         -> 200
  OPTIONS http://localhost:8080/auth/login    (preflight)
  POST    http://localhost:8080/auth/login    -> 200
  쿠키: JSESSIONID, 도메인 localhost, HttpOnly, SameSite=Lax
```

- 1번: 프록시 모드에서는 브라우저가 `localhost:3000/api/...` 하나만 부릅니다. 직접 호출 모드에서는 `localhost:8080` 을 부릅니다.
- 2번: 직접 호출에서는 JSON `POST` 마다 preflight 가 먼저 나갑니다. 프록시 모드는 same-origin 이라 preflight 가 없습니다. (GET 은 두 모드 모두 preflight 가 없습니다.)
- 3번: 두 모드 모두 `localhost` 입니다. 쿠키는 포트로 구분되지 않기 때문입니다. 그래서 `3000` 과 `8080` 이 쿠키를 같이 씁니다.
- 위 목록은 같은 요청을 Chromium 의 네트워크 이벤트로 기록한 값입니다. DevTools 화면의 표시 순서는 조금 다를 수 있습니다.

</details>

### 4단계. 일부러 망가뜨리기 (50분)

#### 실험 A. 직접 호출 모드에서 백엔드를 끈다

3단계 (2)의 직접 호출 모드로 프론트를 띄운 채 백엔드를 끕니다. 1단계의 프록시 모드 증상(`요청에 실패했습니다 (500)`)과 **화면의 문구가 어떻게 다를지** 먼저 예측하세요.

<details>
<summary>실제 결과</summary>

오류 알림 제목은 같고 본문이 다릅니다.

```text
프록시 모드      요청에 실패했습니다 / 요청에 실패했습니다 (500)
직접 호출 모드   요청에 실패했습니다 / Failed to fetch
```

- 프록시 모드에서는 Next 서버가 대신 `500` 을 돌려주므로 HTTP 응답이 있고, `ApiError(500)` 가 됩니다.
- 직접 호출 모드에서는 브라우저가 8080 에 연결하지 못해 응답이 없습니다. `fetch` 가 `TypeError: Failed to fetch` 로 reject 되고 `ApiError` 가 아니므로 브라우저가 정한 문장이 그대로 보입니다. CORS 오류(8주)와 **같은 문구**라서, 이 문구만으로는 "서버가 꺼졌다"와 "CORS 에 막혔다"를 구분할 수 없습니다. Network 탭과 `curl` 로 가려야 합니다.

</details>

#### 실험 B. 3000 포트를 다른 프로세스가 쓰고 있다

3000 을 이미 다른 프로그램이 쓰는 상황을 만듭니다. 백엔드는 켜 둡니다.

```bash
node -e "require('http').createServer((q, s) => s.end('dummy')).listen(3000)" &
cd labs/frontend && pnpm dev
```

`pnpm dev` 의 시작 로그를 읽고, 알려 준 주소로 접속해 `demo` / `demo1234` 로그인을 시도하세요. 책 목록은 보이는데 로그인만 안 될 것입니다. 왜일까요? 먼저 예측하세요.

<details>
<summary>실제 결과</summary>

```text
⚠ Port 3000 is in use by process 11426, using available port 3001 instead.
▲ Next.js 16.4.2 (Turbopack)
- Local:         http://localhost:3001
```

- 책 목록(GET)은 정상으로 보입니다. 로그인 시도는 `요청에 실패했습니다 (403)` 알림으로 실패합니다.
- 원인: 백엔드의 허용 origin 은 `http://localhost:3000` 하나입니다. 브라우저는 same-origin `POST` 에도 `Origin: http://localhost:3001` 헤더를 붙이고, 프록시가 그대로 넘기므로 Spring 이 `Invalid CORS request` 403 을 줍니다. curl 로 `Origin: http://localhost:3001` 을 붙여 `POST /api/members` 를 보내도 같은 403 이 나옵니다. 읽기(GET)는 브라우저가 `Origin` 을 붙이지 않아 통과합니다.
- 해결은 프론트를 3000 에서 띄우는 것입니다. 3000 을 쓰는 프로세스를 찾아(`lsof -iTCP:3000 -sTCP:LISTEN`) 끄세요. 로그의 프로세스 번호 숫자는 환경마다 다릅니다.

실험 뒤에는 `dummy` 서버를 끕니다. (`kill $(lsof -tiTCP:3000 -sTCP:LISTEN)`)

</details>

#### 실험 C. 가짜 서버가 거짓말을 한다

```bash
BACKUP=$(mktemp)
cp labs/frontend/src/mocks/handlers.ts $BACKUP
```

`labs/frontend/src/mocks/handlers.ts` 의 중복 isbn 응답 ``error(409, `이미 등록된 isbn 입니다: ${isbn}`)`` 에서 `409` 를 `400` 으로 바꾸고 `pnpm -C labs/frontend test` 를 실행합니다. 결과를 예측하세요. 확인한 뒤 `cp $BACKUP labs/frontend/src/mocks/handlers.ts` 로 원복합니다.

<details>
<summary>실제 결과</summary>

1개 실패하고 29개가 통과합니다.

```text
× 같은 isbn 으로 다시 등록 -> 409 conflict + 메시지에 isbn
AssertionError: expected 400 to be 409 // Object.is equality
Tests  1 failed | 29 passed (30)
```

이 목은 실서버가 아니라 **내 기억**을 따라 만든 것이라, 테스트는 "목이 내 기억과 같은가"만 검사합니다. 목의 기억이 실서버와 같은지는 아무 테스트도 보지 않습니다. 이 빈틈을 메우는 방법은 4절 끝에서 소개하고 12주에서 다룹니다.

</details>

---

## 1. 로컬 통합: 누가 누구를 부르는가

```text
브라우저 ──(http://localhost:3000)──▶ Next dev 서버 ──(/api/* 를 넘김)──▶ Spring (localhost:8080) ── H2 메모리 DB
   │                                        ▲
   │   직접 호출 모드: 브라우저 ──(http://localhost:8080, CORS 필요)──────────▶ Spring
   │
   └─ MSW 모드: 요청이 서비스 워커에서 가로채져 위 두 칸에 닿지 않는다
```

**기동 순서(권장)**

| 순서 | 터미널 | 명령 | 확인 |
|---|---|---|---|
| 1 | A | `cd labs/backend && ./gradlew :week25:bootRun` | `curl -s localhost:8080/books` → `[]` |
| 2 | B | `cd labs/frontend && pnpm install && pnpm dev` | `curl -s localhost:3000/api/books` → `[]` |
| 3 | 브라우저 | `http://localhost:3000` | 로그인 바에 입력칸이 보임 |

- 종료는 각 터미널에서 `Ctrl+C` 입니다. **끝나면 8080·3000 이 비었는지 확인하세요.** 안 닫힌 이전 서버가 새 서버 대신 응답하면 "고쳤는데 그대로"가 됩니다.

  ```bash
  lsof -iTCP:8080 -iTCP:3000 -sTCP:LISTEN     # 아무것도 안 나오면 비어 있음
  ```
- 8080 을 다른 프로세스가 쓰고 있으면 백엔드가 뜨지 못합니다. 로그에 `Web server failed to start. Port 8080 was already in use.` 와 `Identify and stop the process that's listening on port 8080 ...` 가 나옵니다. 위 `lsof` 로 쓰는 쪽을 찾아 끄세요.
- 백엔드는 H2 **메모리** DB 라서 껐다 켜면 데이터가 비어 있습니다. 대출 화면을 쓰려면 먼저 책과 회원을 등록해야 합니다(회원 id 는 등록 결과에 표시됩니다).
- 데모 로그인은 `demo` / `demo1234` 입니다. 백엔드를 `LIBRARY_DEMO_PASSWORD=원하는값 ./gradlew :week25:bootRun` 으로 켜면 비밀번호가 바뀝니다. (실행해서 확인: 기본 비밀번호는 401, 새 값은 200)
- 이 두 서버 사이의 약속은 이미 배웠습니다. 응답의 모양은 `openapi.json` 과 `schema.d.ts`(6주), 오류의 모양은 `ApiError`(7주), 쿠키와 허용 origin 은 8주입니다. 이 주차는 그 약속들이 **한 컴퓨터 위에서 실제로 만나는 자리**입니다.

## 2. 환경 변수

`labs/frontend/.env.example` 을 복사해 `.env.local` 로 씁니다. (`.env.local` 은 `.gitignore` 에 들어 있어 커밋되지 않습니다.)

```bash
cp labs/frontend/.env.example labs/frontend/.env.local
```

| 변수 | 기본값 | 효과 |
|---|---|---|
| `NEXT_PUBLIC_API_BASE` | 비움 → `/api` | 비우면 브라우저는 `/api/*` 만 부르고 `next.config.ts` 의 `rewrites` 가 `http://localhost:8080/*` 로 넘김. 값을 주면(`http://localhost:8080`) 브라우저가 백엔드를 **직접** 부름 (8주의 CORS 필요) |
| `NEXT_PUBLIC_API_MOCKING` | 비움 | `enabled` 면 MSW 가 API 를 가로챔 |

- `NEXT_PUBLIC_` 접두사가 붙은 값만 **브라우저 번들에 박힙니다.** 접두사가 없는 변수는 서버 쪽 코드에서만 보입니다. 그래서 비밀 값(API 키 등)에는 `NEXT_PUBLIC_` 을 붙이면 안 됩니다. 공개되는 값입니다.
- 값은 **빌드 시점에 번들에 박힙니다.** Next 문서에 따르면 `next build` 를 한 뒤에는 환경 변수를 바꿔도 반영되지 않으므로, 배포 환경마다 빌드할 때 값을 맞춰야 합니다. 개발 서버(`pnpm dev`)는 `.env.local` 이 바뀌면 `Reload env: .env.local` 을 찍고 **다음 새로고침부터** 새 값을 씁니다. (`NEXT_PUBLIC_API_MOCKING` 을 켜 둔 `.env.local` 의 줄을 주석으로 바꾸고 새로고침하면 실서버 데이터가 보이는 것을 확인했습니다. 프론트 README 는 안전하게 `pnpm dev` 를 다시 시작하라고 적습니다.)
- 읽는 순서는 **셸 환경 변수 → `.env.local` → ...** 입니다(Next 문서의 로드 순서). 그래서 `NEXT_PUBLIC_API_MOCKING=enabled pnpm dev` 처럼 명령 앞에 붙인 값이 `.env.local` 보다 우선합니다. 한 번만 쓸 때는 이렇게 명령 앞에 붙이고, 매번 쓸 값은 `.env.local` 에 둡니다. `.env.local` 을 읽었다면 `pnpm dev` 시작 로그에 `- Environments: .env.local` 이 찍힙니다.

## 3. 프록시와 직접 호출: 어느 쪽으로 부를까

`labs/frontend` 는 두 방식을 모두 지원합니다. 환경 변수 하나(`NEXT_PUBLIC_API_BASE`)로 바뀝니다.

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
| 백엔드 CORS 설정 | 필요 (`SecurityConfig`). 틀리면 8주 2단계 같은 오류 | 브라우저 입장에서는 불필요. 단 아래 주의 참고 |
| 쿠키 | 응답이 `localhost:8080` 에서 옴. **SameSite·도메인 규칙을 이해해야 함** | `localhost:3000` 의 쿠키처럼 보임 |
| 운영 구성 | 프론트·백엔드 도메인이 따로. 허용 origin 관리 | 프론트 서버가 백엔드 주소를 알아야 함. 요청이 프론트 서버를 한 번 더 지남 |
| 서버가 꺼지면 | 화면에 `Failed to fetch` (CORS 오류와 같은 문구) | 프록시가 500 텍스트를 돌려줘서 `요청에 실패했습니다 (500)` |

두 모드 모두 Playwright e2e 두 개가 통과합니다. (`pnpm -C labs/frontend e2e` 는 기본이 프록시 모드이고, `NEXT_PUBLIC_API_BASE=http://localhost:8080 pnpm -C labs/frontend e2e` 가 직접 호출 모드입니다. 3000 에 이미 `pnpm dev` 가 떠 있으면 e2e 는 그 서버를 그대로 쓰므로, 어느 모드로 띄웠는지 확인하세요. e2e 는 12주에서 다룹니다.)

**프록시 모드의 주의 두 가지**

1. **백엔드 쪽 `Origin` 검사는 남아 있습니다.** 브라우저는 same-origin `POST` 에도 `Origin` 헤더를 보내고, 프록시는 그것을 그대로 넘깁니다. 그래서 프론트를 `localhost:3000` 이 아닌 주소(다른 호스트·포트, 배포 도메인)로 열면 프록시를 거쳐도 Spring 이 403 을 줍니다. 4단계 실험 B 에서 3001 로 밀린 프론트가 로그인에 실패한 것이 바로 이것입니다. 아래는 이를 curl 로 흉내 낸 것입니다 (`pnpm dev` 와 백엔드가 떠 있을 때).

   ```bash
   JAR=$(mktemp)
   curl -si -c $JAR -X POST localhost:3000/api/auth/login -H 'Content-Type: application/json' -d '{"username":"demo","password":"demo1234"}' | grep -iE '^HTTP|^set-cookie'
   curl -si -b $JAR -X POST localhost:3000/api/members -H 'Origin: http://other.example:3000' -H 'Content-Type: application/json' -d '{"name":"x"}' | grep -iE '^HTTP|Invalid'
   ```

   ```text
   HTTP/1.1 200 OK
   set-cookie: JSESSIONID=<값>; Path=/; HttpOnly; SameSite=Lax
   HTTP/1.1 403 Forbidden
   Invalid CORS request
   ```

   배포 도메인이 정해지면 `allowedOrigins` 에 넣는다는 사실을 백엔드 담당자와 합의해 두세요.
2. **백엔드가 꺼져 있으면 프록시가 500 `Internal Server Error`(텍스트)를 돌려줍니다.** `{"message"}` 형식이 아니므로 `client.ts` 의 `messageOf` 가 `요청에 실패했습니다 (500)` 로 바꿉니다. 화면에서 "서버가 꺼졌다"와 "내가 잘못 보냈다"를 구분하려면 이 형식 차이를 이용합니다.

쿠키가 두 모드에서 모두 따라오는 이유(`localhost:3000` 과 `localhost:8080` 은 포트만 다른 same-site)는 8주의 "SameSite 와 같은 사이트"에서 설명했습니다. 브라우저와 Spring 사이에 프론트 팀이 소유하는 서버(BFF)를 두는 세 번째 선택지는 프론트 저장소의 `src/app/bff/summary/route.ts` 에 맛보기로 구현되어 있으며, 11주에서 서버 경계의 한 가지로 다룹니다.

## 4. 백엔드 없이 개발하기: MSW 켜고 끄기

백엔드가 아직 없거나 불안정할 때, **API 응답을 흉내 내는 계층을 브라우저 안에 둡니다.** 화면 코드는 그대로이고 요청만 가로채집니다.

```bash
cd labs/frontend && NEXT_PUBLIC_API_MOCKING=enabled pnpm dev    # 켜기. 백엔드는 꺼도 된다
cd labs/frontend && pnpm dev                                     # 끄기. 값을 비우고 다시 띄운다
```

켜짐 여부는 화면에서 바로 알 수 있습니다. 백엔드를 끈 채로도 책 목록에 `이펙티브 자바`, `Clean Code` 2권이 보이면 MSW 모드입니다. (`.env.local` 에 값을 두었다면 2절의 규칙대로 그 줄을 지우거나 주석으로 바꿉니다.)

| 파일 | 하는 일 |
|---|---|
| `src/mocks/handlers.ts` | 경로·메서드별 가짜 응답. `week25` 와 같은 상태 코드·메시지(400 검증, 401, 404, 409)를 흉내 냄 |
| `src/mocks/browser.ts` | 브라우저용 워커: `setupWorker(...handlers)` |
| `public/mockServiceWorker.js` | MSW 가 생성한 서비스 워커(`msw init` 생성물). 직접 고치지 않음 |
| `src/mocks/MockGate.tsx` | 워커가 준비된 **뒤에** 화면을 그림 (첫 요청부터 가짜 응답을 받도록) |
| `src/mocks/server.ts` | Node 용: `setupServer(...handlers)`. Vitest 가 같은 핸들러를 재사용 |

핸들러 하나는 이렇게 생겼습니다.

```ts
http.post("*/books", async ({ request }) => {
  if (!loggedIn) return needLogin();
  const body = await readJson(request);
  if (!body) return unreadable();
  const invalid = blankFields(body, ["author", "isbn", "title"]);
  if (invalid) return error(400, invalid);
  const { isbn, title, author } = body as Record<"isbn" | "title" | "author", string>;
  if (books.some((b) => b.isbn === isbn)) return error(409, `이미 등록된 isbn 입니다: ${isbn}`);
  const book: Book = { id: nextId.book++, isbn, title, author };
  books.push(book);
  return HttpResponse.json(book, { status: 201, headers: { Location: `/books/${book.id}` } });
}),
```

- 경로가 `"*/books"` 인 이유: 브라우저의 `/api/books` 와 테스트의 `http://localhost:8080/books` 를 **둘 다** 맞추려고 앞을 와일드카드로 뒀습니다.
- 타입(`Book`, `Loan`, `ApiError` 응답)은 6주에서 생성한 `schema.d.ts` 에서 왔습니다. 스펙이 바뀌어 필드가 사라지면 이 핸들러도 `pnpm typecheck` 에서 컴파일 오류가 납니다. **모양은 타입이 지켜 주지만, 상태 코드·규칙·메시지는 타입에 없습니다.**
- 브라우저의 요청은 서비스 워커가 가로채지만, 핸들러 코드(목 DB 변수들)는 **페이지의 자바스크립트 안에서** 돌아갑니다. 2단계 2·5번에서 새로고침이 상태를 지운 이유입니다.
- Vitest 는 같은 핸들러를 Node 에서 씁니다. 그래서 `pnpm -C labs/frontend test` 는 백엔드 없이 30개가 통과합니다.

### 위험: 목은 "내 기억"일 뿐이다

2단계에서 목은 이미 세 곳에서 실서버와 달랐습니다.

| 어긋남 | 실서버 | 목 |
|---|---|---|
| 초기 데이터 | 비어 있음 | 책 2권, 회원 1명 |
| 로그인 상태 | 쿠키 (새로고침해도 유지) | 변수 하나 (새로고침하면 사라짐) |
| 검증 메시지 | `Accept-Language` 에 따라 한국어/영어 (7주 1단계에서 확인) | 영어 고정 |

이런 차이가 위험해지는 때는 "문구 차이"가 아니라 **규칙 차이**입니다. 백엔드가 규칙을 바꿨는데(예: 중복 isbn 을 409 에서 400 으로) 목은 그대로면, 화면은 목에서 멀쩡하고 실서버에서 깨집니다. 4단계 실험 C 에서 본 것처럼 목의 응답을 바꿔도 그 응답을 단언한 테스트 1개만 실패하고 나머지 29개는 그대로 통과합니다. 그 1개도 "목이 내 기억과 달라졌다" 고 알릴 뿐 실서버와 다르다고 알리지는 못하므로, 기억과 목과 테스트를 함께 고치면 실서버와 달라도 모두 통과합니다. 화면은 `ApiError.kind` 로 400(입력칸 옆)과 409(폼 위 알림)를 가르는데, 목이 틀리면 그 분기를 실서버 기준으로 검증하지 못한 채 통과합니다.

그래서 목으로 개발할 때는 다음을 지킵니다.

- 목으로 만든 화면은 **실서버에 한 번은 붙여서** 본다. 이 주차의 3단계처럼 `NEXT_PUBLIC_API_MOCKING` 만 끄면 같은 화면이 실서버로 바뀐다
- 스펙이 바뀌면 `pnpm typecheck` 로 목 핸들러의 모양부터 맞춘다 (6주)
- 목과 실서버에 **같은 시나리오**를 보내 상태 코드와 `ApiError` 종류가 같은지 검사하는 계약 테스트, 화면까지 포함한 e2e 는 12주에서 만든다

## 5. 디버깅: 문제가 어느 층에 있는가

화면 → (Next 프록시) → Spring 은 세 겹입니다. 증상만 보고 짐작하지 말고, 겹을 하나씩 건너뛰며 어디서 끊기는지 가립니다.

```bash
curl -s -o /dev/null -w '%{http_code}\n' localhost:8080/books        # 백엔드만
curl -s -o /dev/null -w '%{http_code}\n' localhost:3000/api/books    # 프론트 프록시를 거쳐
```

| 8080 | 3000/api | 뜻 |
|---|---|---|
| 200 | 200 | 두 겹 모두 정상. 문제는 화면 코드나 브라우저(쿠키·CORS)에 있다 |
| 000 | 500 | 백엔드가 꺼져 있다. (`000` 은 curl 이 연결하지 못했다는 뜻) 프론트의 Next 터미널에 `Failed to proxy http://localhost:8080/books` 가 찍힌다 |
| 000 | 000 | 프론트도 꺼져 있다 |

그다음 브라우저 쪽 증상을 읽습니다.

| 화면 증상 | 의심할 것 | 확인 |
|---|---|---|
| 모든 호출이 `요청에 실패했습니다 (500)` | 프록시 모드에서 백엔드가 꺼짐 | 위 curl 두 줄 |
| `Failed to fetch` | 직접 호출 모드에서 연결 실패 또는 CORS 거절 | Network 탭의 `OPTIONS` 응답, 백엔드 기동 여부 |
| 책 목록은 보이는데 로그인·쓰기만 `(403)` | 프론트 주소가 백엔드의 허용 origin(`http://localhost:3000`)이 아님. 3000 이 사용 중이라 3001 로 밀렸을 수 있음 | `pnpm dev` 시작 로그의 `Port 3000 is in use ... using available port 3001` |
| 로그인은 된 것 같은데 새로고침하면 풀림 | 쿠키가 저장되지 않음 (`credentials` 누락 등) | Application → Cookies (8주 연습문제 2) |
| 항상 같은 책 2권이 나오고 새로고침하면 등록한 책이 사라짐 | MSW 모드가 켜져 있음 | 명령 앞의 `NEXT_PUBLIC_API_MOCKING`, `.env.local` |
| 검증 메시지가 어떤 때는 한국어, 어떤 때는 영어 | 브라우저의 `Accept-Language` 또는 목 (7주) | 입력칸 아래 문구와 브라우저 언어 설정 |

## TS 와 비교

백엔드 용어를 프론트의 익숙한 것에 대응시킨 표입니다. 완전히 같지는 않습니다.

| 백엔드(Java·Spring) 쪽 | 프론트에서 만나는 모습 |
|---|---|
| `application.properties` / 환경 변수 (`LIBRARY_DEMO_PASSWORD`) | `.env.local` / `NEXT_PUBLIC_*` |
| `./gradlew :week25:bootRun` | `pnpm dev` |
| `./gradlew :week25:test` | `pnpm test` + `pnpm typecheck` |
| `@SpringBootTest` + `MockMvc` (가짜 HTTP) | Vitest + MSW node 서버 |
| `@MockBean` 으로 의존성 대체 | MSW 핸들러로 네트워크 대체 (경계가 HTTP 라는 차이) |
| H2 메모리 DB (껐다 켜면 초기화) | MSW 의 목 DB (새로고침하면 초기화) |
| 리버스 프록시 (`nginx proxy_pass`) | `next.config.ts` 의 `rewrites`, Vite 의 `server.proxy` |
| 서버 포트(`server.port`, 8080) | `pnpm dev` 의 포트(3000). 이미 쓰이면 다음 번호로 밀림 |

## 연습문제

1. `labs/frontend/src/mocks/handlers.ts` 의 `GET */books` 를 고쳐 `keyword` 가 대소문자를 구분하게 만들고(일부러), `client.test.ts` 의 어느 테스트가 실패하는지 확인하세요. 확인 후 원복합니다.
2. 백엔드를 켠 채 프론트를 기본 모드로 띄운 뒤 `curl -si localhost:3000/api/books` 와 `curl -si localhost:8080/books` 의 응답 헤더를 비교하세요. 어떤 헤더가 같고, 어떤 헤더가 다를까요? 프록시가 응답을 어떻게 다시 전달하는지 한 문장으로 쓰세요.
3. `curl -si localhost:8080/books -H 'Origin: http://localhost:3000'` 과 `-H 'Origin: http://localhost:3001'` 의 상태 코드를 비교하고, 4단계 실험 B 의 증상과 연결해 설명하세요.
4. 팀에 "백엔드가 아직 안 나왔는데 화면을 먼저 만들어야 한다"는 상황이 왔습니다. 이 주차까지의 도구로 어떤 순서로 일하겠습니까? (스펙 → 타입 → 목 → 화면 → 실서버 연결)에서 각 단계에 쓰는 파일을 적으세요.
5. 새 팀원이 "프론트에서 로그인이 안 돼요. 책 목록은 되는데요" 라고 합니다. 5절의 표만 보고 물어볼 것 세 가지를 쓰세요.

<details>
<summary>2번과 3번 확인 결과</summary>

- 2번: 백엔드의 보안 관련 헤더(`x-content-type-options`, `cache-control`, `x-frame-options` 등)는 프록시를 지나도 그대로 전달되고, HTTP 헤더 이름은 대소문자를 구분하지 않아서 소문자로 바뀌어 보입니다. 다른 점은 `Vary` 가 하나로 합쳐지며 `Accept-Encoding` 이 붙고, 프론트 서버가 `date`, `keep-alive`, `connection` 을 더하며, 상태 줄이 `200` 에서 `200 OK` 가 되는 것입니다. 프록시는 응답의 상태와 본문, 대부분의 헤더를 그대로 넘기되 자기 서버의 헤더를 일부 덧붙입니다.
- 3번: `Origin: http://localhost:3000` 은 200 이고 `Access-Control-Allow-Origin: http://localhost:3000` 헤더가 붙습니다. `Origin: http://localhost:3001` 은 403 `Invalid CORS request` 입니다. 허용 목록이 3000 하나뿐이기 때문입니다.

</details>

## 퀴즈

1. 백엔드를 먼저 켜는 편이 좋은 이유는? 프론트를 먼저 켜면 무슨 일이 생기나요?
2. `NEXT_PUBLIC_API_MOCKING=enabled pnpm dev` 로 띄운 뒤 `.env.local` 에서 `NEXT_PUBLIC_API_MOCKING` 을 비워도 계속 목 데이터가 보입니다. 왜 그럴까요?
3. MSW 핸들러의 경로가 `"*/books"` 인 이유는?
4. 목 기반 단위 테스트가 모두 통과해도 실서버에서 깨질 수 있는 경우를 두 가지 드세요.
5. `rewrites` 프록시를 쓰면 CORS 설정이 완전히 필요 없어지나요?
6. 3000 이 사용 중이라 프론트가 3001 로 떴을 때, 책 목록은 보이는데 로그인만 실패하는 이유는?
7. `curl -s -o /dev/null -w '%{http_code}' localhost:8080/books` 가 `000` 을 출력했습니다. 무슨 뜻이고, 같은 상황에서 프록시 모드 화면에는 무엇이 보이나요?

<details>
<summary>정답</summary>

1. 프록시는 요청 시점에 연결하므로 어느 쪽이 먼저든 결국 동작하지만, 백엔드가 없으면 `/api/*` 가 500 `Internal Server Error` 를 줘서 첫 화면이 오류 알림으로 뜬다. 켜지면 프론트를 다시 시작하지 않아도 회복된다
2. 명령 앞에 붙인 값은 그 프로세스의 환경 변수라서 `.env.local` 보다 우선한다(Next 문서의 로드 순서). `.env.local` 에 `# NEXT_PUBLIC_API_MOCKING=enabled` 처럼 주석으로 둔 상태에서 명령 앞에 값을 붙여 띄웠을 때도 목 데이터가 보이는 것을 확인했다. `pnpm dev` 를 그 값 없이 다시 시작해야 한다
3. 브라우저에서는 `/api/books`, 테스트(Node)에서는 `http://localhost:8080/books` 로 요청이 나가므로, 앞부분을 와일드카드로 두어 둘 다 맞춘다
4. 목이 상태 코드·규칙을 잘못 흉내 낼 때(중복 isbn 이 409 가 아니라 400 인 것처럼), 목에는 있는 초기 데이터나 로그인 상태가 실서버에는 없을 때. 또한 CORS·쿠키 속성·프록시 설정은 목이 대신해 주지 못하므로 실서버에 붙여 봐야만 드러난다
5. 아니다. 브라우저 입장에서 preflight 는 없어지지만, 프록시가 `Origin` 헤더를 그대로 넘기므로 백엔드의 `Origin` 검사는 남는다. 프론트가 열리는 주소가 허용 목록에 있어야 한다
6. 읽기(GET)는 브라우저가 `Origin` 을 붙이지 않아 통과하지만, 쓰기(POST)는 `Origin: http://localhost:3001` 이 붙고 프록시가 그대로 넘겨서 백엔드가 허용 origin(3000 하나)이 아니라고 403 을 준다
7. curl 이 8080 에 연결하지 못했다는 뜻이다(백엔드가 꺼짐). 같은 상황에서 프록시 모드 화면에는 `요청에 실패했습니다 (500)` 알림이 보이고, 직접 호출 모드에서는 `Failed to fetch` 가 보인다

</details>

## 체크리스트

- [ ] 기동 순서(백엔드 → 프론트)와 서버가 없을 때의 증상(프록시 500, 로그인 폼 대신 오류 알림)을 예측했고, 틀린 것의 이유를 적었다
- [ ] 같은 화면을 실서버/MSW 두 모드로 띄워 5가지 차이를 확인했다
- [ ] 프록시 모드와 직접 호출 모드에서 Network 탭의 요청 주소와 preflight 유무를 비교했다
- [ ] `NEXT_PUBLIC_API_BASE` 와 `NEXT_PUBLIC_API_MOCKING` 이 각각 무엇을 바꾸는지, 명령 앞에 붙인 값과 `.env.local` 중 어느 쪽이 우선인지 안다
- [ ] 3000 포트가 막혔을 때의 증상(로그인만 403)과 이유를 설명할 수 있다
- [ ] 목의 상태 코드를 바꿔 테스트 1개만 실패하고 29개는 통과하는 것을 확인하고 원복했다
- [ ] `curl` 두 줄(8080, 3000/api)로 문제가 어느 겹에 있는지 가를 수 있다
- [ ] 8080·3000 포트가 비었는지 `lsof` 로 확인했다
