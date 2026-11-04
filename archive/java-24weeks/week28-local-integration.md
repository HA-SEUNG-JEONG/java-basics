# 28주차. 로컬 통합 개발 환경: 8080 + 3000, 그리고 백엔드 없이 개발하기

**예상 시간:** 8~10시간 | **목표:** Spring(8080)과 Next(3000)를 순서대로 띄워 한 화면으로 동작시키고, 환경 변수로 호출 대상을 바꾼다. MSW 로 백엔드 없이 화면을 개발하고, **목(mock)이 실제 서버와 어긋나는 지점**을 계약 테스트로 줄인다. Playwright 로 통합 e2e 를 실행한다.

> 명령은 백엔드는 `../../labs/backend/`, 프론트는 `../../labs/frontend/` 에서 실행합니다. 처음이라면 `../../labs/frontend` 에서 `pnpm install` 을 먼저 합니다. (Node 22.12+, pnpm 10+, JDK 21) 27주차를 먼저 끝내세요.

## 🖐 먼저 손으로 (읽기 전에 하세요)

> 이 주차의 핵심은 아래 단계를 **직접 타이핑하면서** 깨닫는 것입니다. 아래쪽 설명 문서는 먼저 읽지 않아도 됩니다. 막혔을 때 사전처럼 펼쳐 보세요.

### 1단계. 기동 순서와 "서버가 없을 때" 예측하기 (30분)

터미널을 두 개 씁니다. 먼저 **프론트만** 켭니다. (백엔드는 아직 켜지 않습니다. 8080 포트가 비어 있어야 합니다.)

```bash
# 터미널 B
cd ../../labs/frontend
pnpm dev
```

아래 세 가지를 순서대로 하기 전에, 결과를 노트에 예측하세요.

| # | 할 일 | 내 예측 |
|---|------|------|
| 1 | 터미널 C 에서 `curl -si localhost:3000/api/books \| head -1` | |
| 2 | 브라우저로 `http://localhost:3000` 을 열면 화면 위쪽(로그인 바)은? | |
| 3 | **`pnpm dev` 를 끄지 않고** 터미널 A 에서 백엔드를 켠 뒤(`cd ../../labs/backend && ./gradlew :week25:bootRun`) 1번을 다시 하면? 본문은? | |

<details>
<summary>정답 (예측을 적은 뒤에 여세요)</summary>

```text
1  HTTP/1.1 500 Internal Server Error
2  로그인 입력칸 대신 오류 알림("요청에 실패했습니다" + "요청에 실패했습니다 (500)")
3  200, 본문 []   (프론트를 다시 시작하지 않아도 된다)
```

- 1번: Next 의 프록시(`rewrites`)가 8080 에 연결하지 못하면 500 과 텍스트 `Internal Server Error` 를 돌려줍니다. 백엔드가 주는 `{"message": ...}` 형식이 아닙니다. 프론트의 `ApiError` 래퍼는 이런 응답을 `요청에 실패했습니다 (500)` 으로 바꿉니다.
- 2번: `useMe` 는 `/auth/me` 가 **401 일 때만** "비로그인"으로 봅니다. 500 은 진짜 오류라서 로그인 폼이 아니라 오류 알림이 나옵니다. (같은 이유로 이 상태의 `pnpm e2e` 는 "아이디" 입력칸을 못 찾아 30초 뒤 실패합니다.)
- 3번: 프록시는 요청이 올 때마다 8080 에 연결합니다. 그래서 **켜는 순서는 어느 쪽이 먼저든 동작은 하지만, 백엔드를 먼저 켜는 것이 오류 화면을 안 보는 순서**입니다. `[]` 인 이유는 DB 가 H2 **메모리** DB 라서 비어 있기 때문입니다.

</details>

### 2단계. 같은 화면, 두 서버 비교 (40분)

화면(React 코드)은 하나도 바꾸지 않고 **API 응답을 누가 주느냐만** 바꿔 봅니다. 아래 5가지를 두 모드에서 각각 해 보고, 모드별 결과를 먼저 예측하세요. (브라우저 언어는 한국어로 둡니다.)

```bash
# 실서버 모드: 터미널 A 에서 백엔드를 "새로" 켠 뒤 (재시작해서 DB 를 비웁니다)
cd ../../labs/frontend && pnpm dev

# MSW 모드: 위 pnpm dev 를 Ctrl+C 로 끄고, 백엔드도 꺼도 됩니다
cd ../../labs/frontend && NEXT_PUBLIC_API_MOCKING=enabled pnpm dev
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

- **3번이 이 주차의 핵심 발견입니다.** 실서버는 `Accept-Language` 헤더(브라우저 언어)에 따라 검증 메시지 언어가 바뀌고, 목은 항상 영어입니다. 화면이 "필드: 메시지" 형식만 가정하고 문구는 가정하지 않아서 둘 다 동작합니다. 문구를 비교하는 테스트를 썼다면 한쪽에서만 통과했을 것입니다.
- 4번처럼 "미리 들어 있는 데이터"와 5번처럼 "새로고침하면 사라지는 상태"는 목이 실서버와 **다르게 행동하는 곳**입니다. 3절에서 이 차이를 어떻게 관리할지 다룹니다.

</details>

### 3단계. 일부러 망가뜨리기 (30분)

원복은 필수입니다. 목 핸들러의 상태 코드를 하나 바꿔서 "목이 거짓말을 하면" 무슨 일이 생기는지 봅니다.

| 이렇게 바꿔 보세요 | 어떻게 될까요? |
|---|---|
| `src/mocks/handlers.ts` 의 중복 isbn 응답 ``error(409, `이미 등록된 isbn 입니다: ${isbn}`)`` 에서 `409` 를 `400` 으로 → `pnpm test` | 1개 실패: `같은 isbn 으로 다시 등록 -> 409 conflict + 메시지에 isbn` (`18` 중 `17` 통과). 이 목은 실서버가 아니라 **내 기억**을 따라 만든 것이라, 테스트는 "목이 내 기억과 같은가"만 검사합니다 |
| 백엔드를 끈 채 `pnpm e2e` | 2개 모두 실패. 로그인 입력칸을 못 찾아 30초 타임아웃 |

첫 번째 행의 교훈: 단위 테스트는 **내가 목에 심은 기억과 코드가 같은지**만 검사합니다. 목의 기억이 실서버와 같은지는 아무 테스트도 보지 않습니다. 화면은 `ApiError.kind` 로 400(입력칸 옆)과 409(폼 위 알림)를 가르는데, 목이 틀리면 그 분기를 실서버 기준으로 검증하지 못한 채 통과합니다. 4절의 계약 테스트가 이 빈틈을 메웁니다.

---

## 1. 로컬 통합: 누가 누구를 부르는가

```text
브라우저 ──(http://localhost:3000)──▶ Next dev 서버 ──(/api/* 를 넘김)──▶ Spring (localhost:8080) ── H2 메모리 DB
   │                                        ▲
   └─ MSW 모드: 요청이 서비스 워커에서 가로채져 위 두 칸에 닿지 않는다
```

**기동 순서(권장)**

| 순서 | 터미널 | 명령 | 확인 |
|---|---|---|---|
| 1 | A | `cd ../../labs/backend && ./gradlew :week25:bootRun` | `curl -s localhost:8080/books` → `[]` |
| 2 | B | `cd ../../labs/frontend && pnpm install && pnpm dev` | `curl -s localhost:3000/api/books` → `[]` |
| 3 | 브라우저 | `http://localhost:3000` | 로그인 바에 입력칸이 보임 |

- 종료는 각 터미널에서 `Ctrl+C` 입니다. **끝나면 8080·3000 이 비었는지 확인하세요.** 안 닫힌 이전 서버가 새 서버 대신 응답하면 "고쳤는데 그대로"가 됩니다.

  ```bash
  lsof -iTCP:8080 -iTCP:3000 -sTCP:LISTEN     # 아무것도 안 나오면 비어 있음
  ```
- 백엔드는 H2 **메모리** DB 라서 껐다 켜면 데이터가 비어 있습니다. 대출 화면을 쓰려면 먼저 책과 회원을 등록해야 합니다(회원 id 는 등록 결과에 표시됩니다).
- 데모 로그인은 `demo` / `demo1234` 입니다. 백엔드를 `LIBRARY_DEMO_PASSWORD=원하는값 ./gradlew :week25:bootRun` 으로 켜면 비밀번호가 바뀝니다. (실행해서 확인: 기본 비밀번호는 401, 새 값은 200)
- `week25` 모듈은 24주차 도서관 명세를 구현한 참조 API 입니다. 학습자가 직접 만든 24주차 서버에 붙일 수도 있지만, **로그인(`/auth/*`)과 CORS 설정은 24주차 서버에 없습니다.** 그 부분은 `week25` 를 쓰세요.

## 2. 환경 변수

`../../labs/frontend/.env.example` 을 복사해 `.env.local` 로 씁니다. (`.env.local` 은 `.gitignore` 에 들어 있어 커밋되지 않습니다.)

```bash
cp .env.example .env.local
```

| 변수 | 기본값 | 효과 |
|---|---|---|
| `NEXT_PUBLIC_API_BASE` | 비움 → `/api` | 비우면 브라우저는 `/api/*` 만 부르고 `next.config.ts` 의 `rewrites` 가 `http://localhost:8080/*` 로 넘김. 값을 주면(`http://localhost:8080`) 브라우저가 백엔드를 **직접** 부름 (27주차의 CORS 필요) |
| `NEXT_PUBLIC_API_MOCKING` | 비움 | `enabled` 면 MSW 가 API 를 가로챔 |

- `NEXT_PUBLIC_` 접두사가 붙은 값만 **브라우저 번들에 박힙니다.** 접두사가 없는 변수는 서버 쪽 코드에서만 보입니다. 그래서 비밀 값(API 키 등)에는 `NEXT_PUBLIC_` 을 붙이면 안 됩니다. 공개되는 값입니다.
- 값은 **빌드(개발 서버 시작) 시점에 박힙니다.** `.env.local` 을 바꾸면 `pnpm dev` 를 다시 시작하세요. 시작 로그에 `Environments: .env.local` 이 찍히면 파일을 읽은 것입니다.
- 한 번만 쓸 때는 명령 앞에 붙입니다: `NEXT_PUBLIC_API_MOCKING=enabled pnpm dev`

## 3. MSW 로 백엔드 없이 개발하기

백엔드가 아직 없거나 불안정할 때, **API 응답을 흉내 내는 계층을 브라우저 안에 둡니다.** 화면 코드는 그대로이고 요청만 가로채집니다.

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
  if (!loggedIn) return needLogin();                         // 401
  const body = await readJson(request);
  if (!body) return unreadable();                            // 400
  const invalid = blankFields(body, ["author", "isbn", "title"]);
  if (invalid) return error(400, invalid);                   // 400
  const { isbn, title, author } = body as Record<"isbn" | "title" | "author", string>;
  if (books.some((b) => b.isbn === isbn)) return error(409, `이미 등록된 isbn 입니다: ${isbn}`);
  const book: Book = { id: nextId.book++, isbn, title, author };
  books.push(book);
  return HttpResponse.json(book, { status: 201, headers: { Location: `/books/${book.id}` } });
}),
```

- 경로가 `"*/books"` 인 이유: 브라우저의 `/api/books` 와 테스트의 `http://localhost:8080/books` 를 **둘 다** 맞추려고 앞을 와일드카드로 뒀습니다.
- 타입(`Book`, `Loan`, `ApiError` 응답)은 25주차에서 생성한 `schema.d.ts` 에서 왔습니다. 스펙이 바뀌어 필드가 사라지면 이 핸들러도 `pnpm typecheck` 에서 컴파일 오류가 납니다. **모양은 타입이 지켜 주지만, 상태 코드·규칙·메시지는 타입에 없습니다.**
- 개발 모드의 React `StrictMode` 는 `useEffect` 를 두 번 실행합니다. `MockGate` 가 `worker.start()` 를 한 번만 부르도록 모듈 변수(`starting ??=`)에 담아 둔 것은 그 때문입니다.
- 브라우저의 요청은 서비스 워커가 가로채지만, 핸들러 코드(목 DB 변수들)는 **페이지의 자바스크립트 안에서** 돌아갑니다. 2단계 2·5번에서 새로고침이 상태를 지운 이유입니다.

Vitest 는 같은 핸들러를 Node 에서 씁니다.

```ts
// vitest.setup.ts
server.listen({ onUnhandledFrame: "error" });   // 핸들러가 없는 요청은 조용히 통과시키지 않고 실패시킨다
afterEach(() => { server.resetHandlers(); resetMockDb(); });
afterAll(() => server.close());
```

`onUnhandledFrame: "error"` 덕에 경로 오타가 "네트워크 오류"가 아니라 즉시 테스트 실패로 드러납니다. 테스트가 끝날 때마다 목 DB 가 초기값(책 2권, 회원 1명)으로 돌아가므로 **테스트끼리 영향을 주지 않습니다.**

```bash
cd ../../labs/frontend
pnpm test            # 백엔드 필요 없음. 18개 통과
pnpm typecheck       # 스펙이 바뀌면 호출부·목 핸들러의 오류로 드러남
```

## 4. 목과 실서버가 어긋나는 위험, 그리고 계약 테스트

2단계에서 목은 이미 세 곳에서 실서버와 달랐습니다.

| 어긋남 | 실서버 | 목 |
|---|---|---|
| 초기 데이터 | 비어 있음 | 책 2권, 회원 1명 |
| 로그인 상태 | 쿠키 (새로고침해도 유지) | 변수 하나 (새로고침하면 사라짐) |
| 검증 메시지 | `Accept-Language` 에 따라 한국어/영어 | 영어 고정 |

세 번째를 터미널에서 직접 확인합니다. (백엔드가 떠 있어야 합니다)

```bash
H='Content-Type: application/json'
curl -s -c /tmp/jar.txt -o /dev/null -X POST localhost:8080/auth/login -H "$H" -d '{"username":"demo","password":"demo1234"}'
for l in ko-KR en-US; do curl -s -b /tmp/jar.txt -H "Accept-Language: $l" -X POST localhost:8080/books -H "$H" -d '{"isbn":"","title":"t","author":"a"}'; echo; done
```

```text
{"message":"isbn: 공백일 수 없습니다"}
{"message":"isbn: must not be blank"}
```

(`Accept-Language` 를 아예 안 보내면 서버 JVM 의 기본 로케일을 따릅니다. 한국어 환경이면 한국어입니다.)

**이런 차이가 위험해지는 때**는 "문구 차이"가 아니라 **규칙 차이**입니다. 백엔드가 규칙을 바꿨는데(예: 중복 isbn 을 409 에서 400 으로) 목은 그대로면, 화면은 목에서 멀쩡하고 실서버에서 깨집니다. 줄이는 방법을 위에서부터 비용 순으로 정리합니다.

| 방법 | 잡는 것 | 못 잡는 것 | 이 저장소 |
|---|---|---|---|
| 생성된 타입 (`schema.d.ts`) | 필드 이름·타입이 바뀜 | 상태 코드·규칙·메시지 | 25주차에서 적용 |
| **계약 테스트**: 같은 시나리오를 목과 실서버에 모두 보냄 | 상태 코드·`ApiError` 종류·필드 오류 키가 다름 | 시나리오에 안 넣은 것 | 아래에서 작성 |
| e2e (`pnpm e2e`) | 화면까지 포함한 실제 흐름 | 느리고 시나리오가 적음 | 5절 |
| 백엔드가 목 핸들러를 같이 유지 / 스펙에서 목 생성 | 어긋남 자체를 없앰 | 팀 합의와 도구 필요 | (개념만) |

### 계약 테스트 작성하기

`../../labs/frontend/src/api/contract.test.ts` 를 만듭니다. 한 시나리오 파일을 두 번 돌립니다: 기본은 **목**에, `CONTRACT=real` 이면 MSW 를 우회해 **실서버(8080)**에 보냅니다.

```ts
import createClient from "openapi-fetch";
import { http, passthrough } from "msw";
import { beforeEach, describe, expect, it } from "vitest";
import { server } from "@/mocks/server";
import { ApiError } from "./client";
import { createLibrary } from "./library";
import type { paths } from "./schema";

// CONTRACT=real 이면 MSW 를 우회해 실제 백엔드(8080)로 같은 시나리오를 보낸다. 기본값은 목.
// Node 의 fetch 는 쿠키를 저장하지 않으므로 JSESSIONID 를 직접 들고 다니는 fetch 를 끼운다.
let cookie = "";
const jarFetch: typeof fetch = async (input, init) => {
  const req = new Request(input, init);
  if (cookie) req.headers.set("cookie", cookie);
  const res = await fetch(req);
  const set = res.headers.get("set-cookie");
  if (set) cookie = set.split(";")[0];
  return res;
};
const lib = createLibrary(createClient<paths>({ baseUrl: "http://localhost:8080", fetch: jarFetch }));

beforeEach(() => {
  cookie = "";
  if (process.env.CONTRACT === "real") server.use(http.all("*", () => passthrough()));
});

async function statusOf(call: Promise<unknown>): Promise<number> {
  try {
    await call;
    return 200;
  } catch (e) {
    return (e as ApiError).status;
  }
}

describe("계약: 목과 실서버가 같은 상태 코드를 낸다", () => {
  const isbn = `c-${Date.now()}`;

  it("로그인 전 쓰기는 401", async () => {
    expect(await statusOf(lib.createMember("x"))).toBe(401);
  });

  it("틀린 비밀번호는 401", async () => {
    expect(await statusOf(lib.login("demo", "nope"))).toBe(401);
  });

  it("빈 필드는 400, 중복 isbn 은 409, 없는 책 대출은 404, 없는 대출 반납은 404", async () => {
    await lib.login("demo", "demo1234");
    const bad = await lib.createBook({ isbn: "", title: "t", author: "a" }).catch((e: ApiError) => e);
    expect((bad as ApiError).status).toBe(400);
    expect(Object.keys((bad as ApiError).fieldErrors)).toEqual(["isbn"]);

    await lib.createBook({ isbn, title: "t", author: "a" });
    expect(await statusOf(lib.createBook({ isbn, title: "t", author: "a" }))).toBe(409);

    const member = await lib.createMember("계약");
    expect(await statusOf(lib.lend(member.id, 999999))).toBe(404);
    expect(await statusOf(lib.giveBack(999999))).toBe(404);
    expect(await statusOf(lib.activeLoans(999999))).toBe(404);
  });
});
```

설계 포인트:

- **비교하는 것은 상태 코드, `ApiError` 의 종류, 필드 오류의 키뿐**이고 **메시지 문구는 비교하지 않습니다.** 문구는 로케일에 따라 달라서 계약이 아니기 때문입니다. 무엇이 계약이고 무엇이 아닌지 정하는 일이 계약 테스트의 대부분입니다.
- 실서버 DB 는 계속 남으므로 `isbn` 에 `Date.now()` 를 섞어 반복 실행해도 충돌하지 않게 했습니다. 회원도 새로 만들어 쓰고, 존재하지 않을 `999999` 를 씁니다.
- Node 의 `fetch` 는 브라우저와 달리 쿠키를 저장하지 않습니다. 그래서 `Set-Cookie` 를 직접 들고 다니는 `jarFetch` 를 `openapi-fetch` 클라이언트에 끼웠습니다.

```bash
cd ../../labs/frontend
pnpm test src/api/contract.test.ts                    # 목에 대해 (백엔드 불필요): 3개 통과
CONTRACT=real pnpm test src/api/contract.test.ts      # 실서버에 대해 (백엔드를 켜 둔 상태): 3개 통과
pnpm test                                             # 전체: 기존 18개 + 3개 = 21개
```

이제 3단계의 망가뜨리기를 다시 합니다. `handlers.ts` 의 중복 isbn `409` 를 `400` 으로 바꾸고:

| 명령 | 결과 |
|---|---|
| `pnpm test src/api/contract.test.ts` (목) | 1개 실패: `expected 400 to be 409` |
| `CONTRACT=real pnpm test src/api/contract.test.ts` (실서버) | 3개 통과 |

**목에서만 실패하고 실서버에서는 통과**하면 "목이 틀렸다"는 뜻입니다. 반대로 실서버에서만 실패하면 "백엔드가 계약을 바꿨다"는 신호입니다. 어느 쪽이든 CI 에서 두 번 돌리면 어긋남이 배포 전에 드러납니다. 확인한 뒤 `handlers.ts` 를 원복하세요.

## 5. Playwright 통합 e2e

단위 테스트(Vitest + MSW)는 **화면 코드 + 가짜 서버**를 검사하고, e2e 는 **브라우저 + Next + 프록시 + 실제 Spring + 쿠키**를 한 번에 검사합니다.

```bash
# 터미널 A: 백엔드 (e2e 가 켜 주지 않습니다. 반드시 미리)
cd ../../labs/backend && ./gradlew :week25:bootRun

# 터미널 B
cd ../../labs/frontend
pnpm exec playwright install chromium     # 최초 1회
pnpm e2e
```

```text
Running 2 tests using 1 worker

  ✓  1 [chromium] › e2e/book-registration.spec.ts:3:1 › 로그인 -> 책 등록 -> 같은 isbn 은 409 -> 로그아웃하면 쓰기 차단
  ✓  2 [chromium] › e2e/lending.spec.ts:3:1 › 대출: 없는 책은 404(notFound), 이미 대출 중이면 409(conflict), 반납하면 목록에서 사라진다

  2 passed
```

(소요 시간은 환경마다 다릅니다.)

`playwright.config.ts` 의 핵심:

```ts
export default defineConfig({
  testDir: "./e2e",
  workers: 1, // 백엔드 DB(H2 메모리)를 모든 테스트가 공유한다
  use: { baseURL: "http://localhost:3000" },
  projects: [{ name: "chromium", use: { browserName: "chromium" } }],
  webServer: {
    command: "pnpm dev",
    url: "http://localhost:3000",
    reuseExistingServer: true,
  },
});
```

- `webServer` 는 **Next dev 서버만** 켭니다. 백엔드는 미리 켜 둬야 합니다.
- `reuseExistingServer: true` 라서 3000 에 이미 `pnpm dev` 가 떠 있으면 **그것을 그대로 씁니다.** MSW 모드로 띄워 둔 서버가 남아 있으면 e2e 가 목을 대상으로 돌아갑니다. 어느 서버를 대상으로 하는지 늘 의식하세요.
- `workers: 1`: 테스트들이 같은 백엔드 DB 를 쓰기 때문에 병렬로 돌리지 않습니다.

테스트가 단단한 이유를 `e2e/book-registration.spec.ts` 에서 읽으세요.

- **책 등록마다 새 `isbn`**: `` `e2e-${Date.now()}` `` — 백엔드 DB 는 실행 중 계속 남으므로 반복 실행해도 충돌하지 않습니다.
- **역할 기반 선택자**: `getByRole("button", { name: "책 등록" })`, `getByLabel("아이디")`. CSS 클래스가 바뀌어도 깨지지 않고, 접근성 이름을 검사하는 효과도 있습니다.
- **오류 종류를 `data-kind` 로 검증**: `ErrorNotice` 가 `data-kind="conflict"` 처럼 속성을 달아 두었기 때문에 "409 는 폼 위 알림, 400 은 입력칸 옆(`aria-invalid`)"이라는 구분을 문구가 아니라 종류로 확인합니다. `[role="alert"][data-kind]` 로 고르는 이유는 Next 자체의 라우트 안내(`role="alert"`)와 겹치지 않게 하기 위해서입니다.

세 가지 방식 모두 같은 e2e 를 통과합니다.

| 실행 | 명령 | 백엔드 |
|---|---|---|
| 프록시 + 실서버 (기본) | `pnpm e2e` | 필요 |
| 직접 호출 + CORS + 실서버 | `NEXT_PUBLIC_API_BASE=http://localhost:8080 pnpm e2e` | 필요 |
| MSW | `NEXT_PUBLIC_API_MOCKING=enabled pnpm e2e` | **불필요** (3000 에 다른 `pnpm dev` 가 떠 있지 않을 때) |

같은 e2e 가 세 구성에서 모두 통과한다는 것은 **화면이 서버 구현(프록시/직접 호출/목)에 의존하지 않는다**는 뜻입니다. 세 번째는 목이 화면이 필요로 하는 흐름만큼은 실서버와 같게 행동한다는 증거이기도 합니다. 다만 e2e 가 문구를 비교하지 않고 `data-kind` 와 `aria-invalid` 만 보기 때문에, 2단계 3번의 문구 차이는 e2e 로는 드러나지 않습니다.

## Spring/Java 용어 ↔ 프론트 용어

| Spring/Java 쪽 | 프론트 쪽에서 닮은 것 |
|---|---|
| `application.properties` / 환경 변수 (`LIBRARY_DEMO_PASSWORD`) | `.env.local` / `NEXT_PUBLIC_*` |
| `@SpringBootTest` + `MockMvc` (가짜 HTTP) | Vitest + MSW node 서버 |
| `@MockBean` 으로 의존성 대체 | MSW 핸들러로 네트워크 대체 (경계가 HTTP 라는 차이) |
| `@SpringBootTest(webEnvironment = RANDOM_PORT)` (실제 서버를 띄움) | Playwright e2e (실제 브라우저 + 실제 서버) |
| `./gradlew :week25:bootRun` | `pnpm dev` |
| `./gradlew :week25:test` | `pnpm test` + `pnpm typecheck` |
| H2 메모리 DB (껐다 켜면 초기화) | MSW 의 목 DB (새로고침하면 초기화) |

## 연습문제

1. `handlers.ts` 의 `GET */books` 를 고쳐 `keyword` 가 대소문자를 구분하게 만들고(일부러), `client.test.ts` 의 어느 테스트가 실패하는지 확인하세요. 확인 후 원복합니다.
2. 계약 테스트에 "4권째 대출은 409" 시나리오를 추가하세요. 실서버 DB 가 이전 실행으로 어지럽혀져 있어도 통과하도록 **매번 새 회원과 새 책 4권**을 만드세요.
3. 계약 테스트가 **문구를 비교하지 않는 것**이 옳은 이유를 `Accept-Language` 실험과 엮어 두 문장으로 쓰세요.
4. `pnpm dev` 를 MSW 모드로 띄운 채 `pnpm e2e` 를 돌리면 어떤 일이 생기는지 `reuseExistingServer` 와 연결해 설명하세요. (직접 해 보세요.)
5. 팀에 "백엔드가 아직 안 나왔는데 화면을 먼저 만들어야 한다"는 상황이 왔습니다. 이 주차의 도구로 어떤 순서로 일하겠습니까? (스펙 → 타입 → 목 → 화면 → 실서버 연결)에서 각 단계에 쓰는 파일을 적으세요.

## 퀴즈

1. 백엔드를 먼저 켜는 편이 좋은 이유는? 프론트를 먼저 켜면 무슨 일이 생기나요?
2. `NEXT_PUBLIC_API_MOCKING` 을 `.env.local` 에서 바꿨는데 화면이 그대로입니다. 왜 그럴까요?
3. MSW 핸들러의 경로가 `"*/books"` 인 이유는?
4. 목 기반 단위 테스트가 모두 통과해도 실서버에서 깨질 수 있는 경우를 두 가지 드세요.
5. 계약 테스트가 목에서만 실패할 때와 실서버에서만 실패할 때, 각각 무엇을 고쳐야 하나요?
6. Playwright `webServer` 가 백엔드를 켜 주지 않는 설정은 어떤 면에서 의도적일까요?

<details>
<summary>정답</summary>

1. 프록시는 요청 시점에 연결하므로 어느 쪽이 먼저든 결국 동작하지만, 백엔드가 없으면 `/api/*` 가 500 `Internal Server Error` 를 줘서 첫 화면이 오류 알림으로 뜬다. 켜지면 프론트를 다시 시작하지 않아도 회복된다
2. `NEXT_PUBLIC_*` 는 개발 서버 시작 시점에 번들에 박힌다. `pnpm dev` 를 다시 시작해야 한다 (그리고 브라우저 새로고침)
3. 브라우저에서는 `/api/books`, 테스트(Node)에서는 `http://localhost:8080/books` 로 요청이 나가므로, 앞부분을 와일드카드로 두어 둘 다 맞춘다
4. 목이 상태 코드·규칙을 잘못 흉내 낼 때(중복 isbn 이 409 가 아니라 400 인 것처럼), 목에는 있는 초기 데이터나 로그인 상태가 실서버에는 없을 때. 또한 CORS·쿠키 속성·프록시 설정은 목이 대신해 주지 못하므로 e2e 에서만 드러난다
5. 목에서만 실패: 목(`handlers.ts`)을 실서버에 맞춘다. 실서버에서만 실패: 백엔드가 계약을 바꾼 것이므로 의도된 변경인지 확인하고, 의도라면 목·타입·화면을 함께 고친다. 아니면 백엔드 버그다
6. 백엔드는 프론트 저장소가 소유하지 않는 별도 시스템이다. 어떤 버전·데이터의 백엔드에 붙었는지 사람이 정하게 해서, e2e 가 "프론트가 켜 주는 가짜 환경"이 되지 않게 한다

</details>

## 체크리스트

- [ ] 기동 순서(백엔드 → 프론트)와 서버가 없을 때의 증상(프록시 500, 로그인 폼 대신 오류 알림)을 예측했고, 틀린 것의 이유를 적었다
- [ ] 같은 화면을 실서버/MSW 두 모드로 띄워 5가지 차이를 확인했다
- [ ] `.env.local` 에서 `NEXT_PUBLIC_API_BASE` 와 `NEXT_PUBLIC_API_MOCKING` 이 각각 무엇을 바꾸는지 안다
- [ ] 백엔드 없이 `pnpm test` 18개가 통과하는 것을 확인했다
- [ ] `contract.test.ts` 를 만들고 목·실서버 양쪽에서 3개 통과시켰다
- [ ] 목의 상태 코드를 바꿔 계약 테스트가 목에서만 실패하는 것을 확인하고 원복했다
- [ ] `pnpm e2e` 가 백엔드를 켠 상태에서 2개 통과한다
- [ ] 8080·3000 포트가 비었는지 `lsof` 로 확인했다
