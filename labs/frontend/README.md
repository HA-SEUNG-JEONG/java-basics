# labs/frontend — 도서관 API 클라이언트 (Next.js)

`labs/backend/week25` 참조 API(도서관 명세 + 세션 쿠키 로그인)를 호출하는 Next.js(App Router) + TypeScript + Tailwind 클라이언트입니다.
[`fe-collab/`](../../fe-collab/) 6~14주 문서의 실습 대상이며, **API 타입은 손으로 쓰지 않고 OpenAPI 스펙에서 생성**합니다.

- 요구 사항: Node 22.12+, pnpm 10+, (백엔드를 쓸 때) JDK 21
- 데이터 패칭: TanStack Query / API 호출: `openapi-fetch` (생성된 타입만으로 경로·본문·응답이 검사됨)

## 실행 방법

```bash
# 1) 백엔드 기동 (터미널 A) — 8080
cd labs/backend && ./gradlew :week25:bootRun

# 2) 프론트 (터미널 B)
cd labs/frontend
pnpm install
pnpm gen:api     # ../backend/week25/openapi.json -> src/api/schema.d.ts
pnpm dev         # http://localhost:3000
```

데모 로그인: `demo` / `demo1234` (백엔드 환경 변수 `LIBRARY_DEMO_PASSWORD` 로 바꿀 수 있음). 읽기(책 목록·검색, 대출 목록)는 로그인 없이, 쓰기(책·회원 등록, 대출, 반납)는 로그인 후에 됩니다.
백엔드는 H2 메모리 DB 라서 껐다 켜면 데이터가 비어 있습니다. 대출 화면을 쓰려면 먼저 책과 회원을 등록하세요(회원 id 는 등록 결과에 표시됨).

백엔드 없이 화면만 개발하려면 [MSW](#백엔드-없이-개발-msw) 를 켜세요.

## 스크립트

| 명령 | 하는 일 |
|---|---|
| `pnpm gen:api` | `openapi.json` 에서 `src/api/schema.d.ts` 생성 |
| `pnpm dev` / `pnpm build` / `pnpm start` | Next 개발 서버 / 프로덕션 빌드 / 실행 |
| `pnpm typecheck` | `tsc --noEmit` — 스펙이 바뀌면 여기서 호출부 오류로 드러남 |
| `pnpm test` | Vitest (ApiError 매핑, 클라이언트의 4xx 변환 등. MSW node 서버 사용, 백엔드 불필요) |
| `pnpm e2e` | Playwright 통합 E2E (**백엔드가 8080 에 떠 있어야 함**) |

> TypeScript 는 5.9.3 에 고정합니다. TypeScript 7 은 `openapi-typescript` 가 쓰는 컴파일러 JS API 를 제공하지 않아 `gen:api` 가 실패합니다.

## 환경 변수 (`.env.example` → `.env.local`)

| 변수 | 기본값 | 설명 |
|---|---|---|
| `NEXT_PUBLIC_API_BASE` | (비움 → `/api`) | 비우면 브라우저는 same-origin `/api/*` 를 호출하고, `next.config.ts` 의 rewrites 가 `http://localhost:8080/*` 로 넘깁니다(CORS 불필요). 값을 주면(`http://localhost:8080`) 브라우저가 백엔드를 **직접** 호출하며, 이때는 백엔드 CORS 설정(허용 origin `http://localhost:3000` + credentials)에 기댑니다. |
| `NEXT_PUBLIC_API_MOCKING` | (비움) | `enabled` 면 MSW 가 API 를 가로채 가짜 응답을 줍니다. |

`NEXT_PUBLIC_*` 값은 빌드 시점에 번들에 박힙니다. 바꾸면 dev 서버를 다시 시작하세요.

## 백엔드 없이 개발 (MSW)

```bash
NEXT_PUBLIC_API_MOCKING=enabled pnpm dev
```

- `src/mocks/handlers.ts` 가 week25 와 같은 상태 코드·메시지(400 필드 검증, 401 로그인 필요, 404, 409 중복 isbn·이미 대출 중·3권 한도·이미 반납)를 흉내 냅니다. 책 2권·회원 1명이 미리 들어 있고 로그인은 `demo`/`demo1234` 입니다.
- 브라우저에서는 `public/mockServiceWorker.js`(서비스 워커, `msw init` 생성물)가 요청을 가로챕니다. `src/mocks/MockGate.tsx` 가 워커가 준비된 뒤에 화면을 그립니다.
- 같은 핸들러를 Vitest 가 node 서버(`src/mocks/server.ts`)로 재사용합니다.
- 핸들러 경로가 `*/books` 처럼 앞이 와일드카드인 이유: 브라우저의 `/api/books` 와 테스트의 `http://localhost:8080/books` 를 모두 맞추기 위해서입니다.

## E2E (Playwright)

`playwright.config.ts` 의 `webServer` 는 **Next dev 서버만** 기동합니다. 백엔드는 미리 띄워 두어야 합니다.

```bash
# 터미널 A
cd labs/backend && ./gradlew :week25:bootRun
# 터미널 B
cd labs/frontend && pnpm exec playwright install chromium   # 최초 1회
pnpm e2e
```

- `e2e/book-registration.spec.ts`: 로그인 전 쓰기 버튼 차단 → 로그인 → 책 등록 → 같은 isbn 은 409(알림) → 빈 isbn 은 400(입력칸 오류) → 로그아웃하면 다시 차단
- `e2e/lending.spec.ts`: 없는 책 대출 404 / 이미 대출 중 409 가 서로 다른 알림으로 표시, 반납하면 목록에서 사라짐
- 백엔드 DB 가 실행 중 계속 남으므로 isbn 에 `Date.now()` 를 섞어 반복 실행해도 충돌하지 않습니다.

## 오류 표시 규약

`src/api/client.ts` 의 `ApiError(status, message)` 가 서버 오류 본문 `{"message": "..."}` 를 담고, 상태 코드를 UI 종류로 바꿉니다.

| 상태 | `kind` | 화면에서 |
|---|---|---|
| 400 | `field` | 입력칸 옆 메시지 (`fieldErrors` 가 `"isbn: ..., title: ..."` 를 필드별로 나눔) |
| 401 | `auth` | "로그인이 필요합니다" 알림 (+ 쓰기 버튼은 로그인 전에 이미 비활성) |
| 404 | `notFound` | "찾을 수 없습니다" 알림 |
| 409 | `conflict` | "이미 처리되었거나 규칙에 어긋납니다" 알림 |
| 그 외 | `unknown` | "요청에 실패했습니다" 알림 |

## 폴더 구조

```
labs/frontend
├─ next.config.ts          /api/:path* -> http://localhost:8080/:path* 프록시
├─ .env.example
├─ vitest.config.ts / vitest.setup.ts   MSW node 서버를 모든 테스트에 켬
├─ playwright.config.ts / e2e/          통합 E2E
├─ public/mockServiceWorker.js          MSW 서비스 워커 (msw init 생성물)
└─ src
   ├─ api
   │  ├─ schema.d.ts       생성 파일 (pnpm gen:api). 직접 고치지 않음
   │  ├─ client.ts         openapi-fetch 클라이언트, ApiError, 상태 코드 -> UI 종류
   │  ├─ library.ts        화면이 쓰는 호출 모음 (listBooks, createBook, lend ...)
   │  └─ client.test.ts
   ├─ mocks                handlers.ts (가짜 API), browser.ts, server.ts, MockGate.tsx
   ├─ lib/useMe.ts         로그인 사용자 조회 (401 은 null)
   ├─ components           BookList, BookForm, MemberForm, LendForm, MyLoans, AuthBar, ErrorNotice ...
   └─ app                  layout(서버), providers(클라이언트), page / members / loans (서버 페이지)
```

서버/클라이언트 구분: 레이아웃·페이지·`Nav` 는 서버 컴포넌트이고, 상태나 이벤트가 필요한 폼·목록·로그인 바만 `"use client"` 입니다. 데이터 패칭은 모두 클라이언트(TanStack Query)에서 합니다. 쿠키를 싣는 요청과 목킹을 한 경로로 두기 위한 단순화입니다(서버 컴포넌트에서 가져오려면 브라우저 쿠키를 서버 요청에 직접 전달해야 하고, 목킹은 브라우저 서비스 워커에서만 동작합니다).

예외가 두 곳 있습니다(11주 실습). `src/app/books/[id]` 는 서버 컴포넌트가 `src/api/server.ts` 로 백엔드(`API_BASE_URL`, 기본 `http://localhost:8080`)를 직접 부르고 브라우저의 `JSESSIONID` 쿠키를 실어 보냅니다. `src/app/bff/summary/route.ts` 는 두 호출을 묶는 Route Handler(BFF) 맛보기입니다. 이 둘은 서버에서 백엔드를 부르므로 MSW 목킹(브라우저 서비스 워커)이 적용되지 않고, 백엔드가 떠 있어야 합니다.

## Tailwind 규칙

`src/app/globals.css` 에 디자인 토큰(CSS 변수 → `@theme inline`)을 정의하고 컴포넌트는 토큰 이름(`bg-surface`, `text-danger`, `p-gutter`, `max-w-page` ...)과 기본 스케일만 씁니다. `p-[13px]`, `bg-[#fff]` 같은 임의값은 쓰지 않으며, 다크 모드는 변수만 바꿔서 대응합니다.

## 알려진 한계

- `pnpm test` 는 타입 검사를 하지 않습니다. 스펙 변경은 `pnpm typecheck` 에서 잡힙니다(둘 다 CI 에서 돌리세요).
- MSW 의 로그인 상태는 쿠키가 아니라 모듈 전역 값 하나입니다(여러 사용자·새로고침 간 유지 안 함).
- 400 검증 메시지는 요청의 `Accept-Language` 헤더에 따라 영어/한국어로 달라집니다. 화면은 문구가 아니라 `필드: 메시지` 형식만 가정합니다.
- CSRF 는 백엔드에서 꺼져 있습니다(데모). 운영에서는 켜고 `X-XSRF-TOKEN` 헤더를 보내야 합니다.
