# 11주차. Next 서버 경계: 서버 컴포넌트에서 백엔드 부르기

**예상 시간:** 6~8시간 (하루 1시간 세션 7회) | **목표:** Next 서버가 브라우저 대신 백엔드(8080)를 직접 부르는 길을 만들고, 그 길에서 달라지는 것(CORS 가 없다, 쿠키는 우리가 전달한다, 에러는 `notFound()` 와 `error.tsx` 로 그린다)을 요청과 화면으로 확인한다. `"use client"` 가 어디서 서버와 브라우저를 가르는지 설명하고, Route Handler(BFF)를 언제 쓰고 언제 쓰지 말아야 하는지 말할 수 있게 된다.

> 이 주차의 전제입니다. 7주의 `ApiError` 와 `ErrorNotice`(5절의 "서버 컴포넌트에서의 처리" 예고), 8주의 세션 쿠키와 CORS, 9주의 `rewrites` 프록시와 환경 변수, 10주의 TanStack Query 캐시를 알고 있어야 합니다. 지금까지의 화면은 **브라우저가** `/api/*` 를 불렀습니다. 이번 주에는 **Next 서버가** 직접 부르는 길을 하나 더 만듭니다.
>
> 준비물: JDK 21, Node 22.12+, pnpm 10+, `curl`, `jq`, 브라우저 DevTools. 백엔드는 터미널 A 에서 `cd labs/backend && ./gradlew :week25:bootRun`(8080), 프론트는 터미널 B 에서 `cd labs/frontend && pnpm install && pnpm dev`(3000)로 띄웁니다. 명령은 별도 언급이 없으면 저장소 루트에서 실행합니다.
>
> 이 주차에 `../labs/frontend` 에 추가한 파일입니다. 모두 새 파일이고 기존 컴포넌트는 고치지 않았습니다.
>
> | 파일 | 역할 |
> |---|---|
> | `src/api/server.ts` | 서버 전용 API 호출(`serverClient`, `getBook`, `getMe`). 세션 쿠키를 백엔드 요청에 실음 |
> | `src/app/books/[id]/page.tsx` | 책 상세. 서버 컴포넌트가 `GET /books/{id}` 를 부름 |
> | `src/app/books/[id]/not-found.tsx`, `error.tsx` | 이 경로의 404 화면, 예상 못 한 실패 화면 |
> | `src/app/bff/summary/route.ts` | Route Handler(BFF) 맛보기 |
>
> 프론트 `README.md` 에 이 둘이 "서버에서 가져오지 않는다" 는 설명의 예외라는 문단을 한 개 더했고, `.env.example` 에 `API_BASE_URL` 주석을 한 줄 더했습니다.

## 하루 1시간으로 나누면

이 주차는 하루 1시간씩 7번의 세션으로 나눕니다. 한 세션은 50~70분이고, 2단계는 문서의 실험 A 와 실험 B~E 로 나눕니다.

| 세션 | 할 일 | 끝났을 때 할 수 있어야 하는 것 |
|---|---|---|
| 1 | 1단계(예측하기 → 요청을 보내고 → 비교하기, 40분)를 하고, 1절(누가 누구를 부르는가: 경로가 하나 더 생겼다)을 읽는다 | 브라우저 Network 탭에 백엔드로 가는 요청이 보이지 않는 이유를 설명할 수 있다 |
| 2 | 2절(서버 컴포넌트에서 가져오기)과 3절(쿠키를 서버 요청에 실어 주기)을 읽고, 2단계(일부러 망가뜨리기, 1시간)의 실험 A(쿠키를 서버 요청에 실어 주지 않는다)를 한다 | 서버 컴포넌트에서는 쿠키를 코드가 전달해야 한다는 것을 실험 A 로 보이고 말할 수 있다 |
| 3 | 2단계의 실험 B~E(서버 컴포넌트에서 `useState`, 서버 전용 모듈, 백엔드 끄기, `console.log`)를 하고 고친 파일을 모두 원복한 뒤, 4절(서버와 클라이언트의 경계)을 읽는다 | 실험으로 고친 파일을 모두 원복하고, `"use client"` 와 `server-only` 가 하는 일을 설명할 수 있다 |
| 4 | 5절(서버에서의 에러 처리), 6절(Route Handler (BFF) 맛보기), 7절(한계와 이 주차가 다루지 않은 것), TS 와 비교 절을 읽는다 | `notFound()` 와 `error.tsx` 를 언제 쓰는지와 상태 코드가 어떻게 다른지 설명하고, Route Handler(BFF) 를 쓸 때와 쓰지 말아야 할 때를 구분하며 서버 경로의 세 가지 한계를 말할 수 있다 |
| 5 | 3단계(빈칸에서 만들기 — 회원의 대출 목록 서버 페이지, 1시간)를 한다 | 3단계 페이지로 200, 404, 500 을 확인한 뒤 폴더를 삭제할 수 있다 |
| 6 | 연습문제와 퀴즈를 푼다 (압축 코스에서는 건너뜀) | 실험 A 의 조용한 비로그인을 잡는 확인 절차를 쓰고, `API_BASE_URL` 을 막힌 주소로 바꿨을 때 서버 경로와 브라우저 경로가 각각 어떻게 되는지 예측하고 확인할 수 있다 |
| 7 | 체크리스트의 각 항목을 내 말로 설명해 보고, 막히는 항목은 해당 절을 다시 읽은 뒤 학습 노트를 정리한다 | 서버에서 백엔드를 부르는 길에서 달라지는 것(CORS 가 없다, 쿠키는 코드가 전달한다, 오류는 `notFound()` 와 `error.tsx` 로 그린다)을 요청과 화면으로 설명할 수 있다 |

세션을 시작하기 전에 이전 세션의 마지막 결과를 1~2분 훑어봅니다. 막히면 아래 해당 개념 절을 사전처럼 펼쳐 보세요.

## 🖐 먼저 손으로 (읽기 전에 하세요)

> 이 주차의 핵심은 아래 단계를 **직접 해 보면서** 깨닫는 것입니다. 아래쪽 설명 문서는 먼저 읽지 않아도 됩니다. 막혔을 때 사전처럼 펼쳐 보세요.

### 1단계. 예측하기 → 요청을 보내고 → 비교하기 (40분)

백엔드를 **새로 켠 뒤**(재시작해서 DB 를 비웁니다) 책 2권과 회원 1명을 등록하고, 프론트를 `pnpm dev` 로 띄웁니다. 터미널 C 에서 시작합니다.

```bash
B=localhost:8080; J=/tmp/week11.cookies
curl -s -c $J -X POST -H 'Content-Type: application/json' -d '{"username":"demo","password":"demo1234"}' $B/auth/login
curl -s -b $J -X POST -H 'Content-Type: application/json' -d '{"isbn":"111","title":"이펙티브 자바","author":"조슈아 블로크"}' $B/books
curl -s -b $J -X POST -H 'Content-Type: application/json' -d '{"isbn":"222","title":"Clean Code","author":"Robert C. Martin"}' $B/books
curl -s -b $J -X POST -H 'Content-Type: application/json' -d '{"name":"하승"}' $B/members
```

아래 명령을 실행하기 **전에** 예측을 노트에 적으세요. 새 화면 `/books/{id}` 는 서버 컴포넌트가 `GET /books/{id}` 를 부르고, 응답과 함께 "로그인 여부" 문구를 그립니다. 코드는 아직 열지 않아도 됩니다.

| # | 질문 | 내 예측 |
|---|------|------|
| 1 | `curl -s -o /dev/null -w '%{http_code}\n' localhost:3000/books/1` 의 상태 코드는? |  |
| 2 | 같은 명령으로 `/books/999`, `/books/abc` 의 상태 코드는? |  |
| 3 | 브라우저(로그인하지 않은 상태)에서 `http://localhost:3000/books/1` 을 직접 열고 Network 탭을 보면, `/api/books/1` 이나 `localhost:8080` 으로 가는 요청이 보이는가? 어떤 요청이 보이는가? |  |
| 4 | 화면의 "로그인하지 않은 요청입니다" 문구는, 브라우저에서 로그인(`demo`/`demo1234`)한 뒤 새로고침하면 어떻게 바뀌는가? |  |
| 5 | `curl -s localhost:3000/books/1 \| grep -o 'data-testid="viewer">[^<]*<'` 와 `curl -s -b $J localhost:3000/books/1 \| grep -o 'data-testid="viewer">[^<]*<'` 는 각각 무엇을 출력하는가? |  |
| 6 | `curl -s localhost:3000/bff/summary` 와 `curl -s -b $J localhost:3000/bff/summary` 는 각각 무엇을 출력하는가? |  |

<details>
<summary>정답 (예측을 적은 뒤에 여세요)</summary>

```text
1  200
2  404, 404
4  "demo 님으로 로그인한 요청입니다"
5  data-testid="viewer">로그인하지 않은 요청입니다<
   data-testid="viewer">demo 님으로 로그인한 요청입니다<
6  {"bookCount":2,"username":null}
   {"bookCount":2,"username":"demo"}
```

- 2번: `/books/999` 는 백엔드가 404 를 줘서 `notFound()` 가 되고, `/books/abc` 는 숫자가 아닌 id 라서 이 화면이 백엔드를 부르기도 전에 `notFound()` 를 부릅니다. 백엔드에 `/books/abc` 를 직접 부르면 400 입니다. 둘 다 사용자 입장에서는 "그 주소에는 책이 없다" 라서 404 로 맞췄습니다.
- 3번: 요청은 문서 하나(`GET /books/1`)와 `GET /api/auth/me` 뿐입니다. `/api/auth/me` 는 레이아웃의 `AuthBar`(클라이언트 컴포넌트)가 부릅니다. **책 데이터를 가져오는 요청은 브라우저에 보이지 않습니다.** Next 서버가 안에서 백엔드를 불렀기 때문입니다. (`pnpm dev` 상태에서 Playwright 로 확인했습니다.)
- 4~5번: "로그인했는가" 를 서버가 안 것은 **브라우저가 `/books/1` 문서 요청에 실어 보낸 `JSESSIONID` 쿠키를 Next 서버가 다시 백엔드 요청에 실었기** 때문입니다. 쿠키 없이 부르면 백엔드의 `/auth/me` 가 401 이고, 화면은 그것을 "로그인하지 않음" 으로 그립니다.
- 6번: 응답 모양을 프론트가 정한 BFF 입니다. 백엔드의 `GET /books` 와 `GET /auth/me` 를 묶어서 `{ bookCount, username }` 만 내려 줍니다.

</details>

### 2단계. 일부러 망가뜨리기 (1시간)

실험 대상 파일을 고치므로 **원복이 필수**입니다. 이 작업 디렉터리는 git 이 아니므로 먼저 백업을 만드세요.

```bash
cp labs/frontend/src/api/server.ts /tmp/server.backup.ts
cp "labs/frontend/src/app/books/[id]/page.tsx" /tmp/page.backup.tsx
cp labs/frontend/src/components/BookList.tsx /tmp/BookList.backup.tsx
```

#### 실험 A. 쿠키를 서버 요청에 실어 주지 않는다

`src/api/server.ts` 의 `serverClient` 에서 `headers: session ? { cookie: ... } : {},` 줄을 `headers: {},` 로 바꿉니다. 저장하고 1단계 5번과 6번의 두 번째 명령(쿠키 포함)을 다시 실행합니다. 그리고 같은 쿠키로 `curl -s -b $J localhost:3000/api/auth/me` 도 실행합니다.

먼저 예측하세요. 쿠키를 보내고 있는데도 서버 컴포넌트는 로그인 상태를 알 수 있을까요? 그리고 `/api/auth/me`(브라우저가 쓰는 길)는요?

<details>
<summary>실제 결과 (예측을 적은 뒤에 여세요)</summary>

```text
data-testid="viewer">로그인하지 않은 요청입니다<
{"bookCount":2,"username":null}
{"username":"demo"}
```

- 서버 컴포넌트와 BFF 는 로그인 상태를 **모릅니다.** 쿠키는 Next 서버까지는 왔지만, Next 서버가 백엔드를 부를 때는 브라우저가 아니라서 아무것도 자동으로 붙지 않기 때문입니다.
- 같은 쿠키로 `rewrites` 길(`/api/auth/me`)을 부르면 `{"username":"demo"}` 입니다. `rewrites` 는 들어온 요청을 그대로 넘기는 프록시라서 헤더가 따라갑니다. 서버 컴포넌트의 `fetch` 는 **새 요청**을 만드는 코드입니다. 이 차이가 이번 주의 핵심입니다.
- 이 버그의 증상은 에러가 아니라 **"조용히 비로그인"** 입니다. 화면 위쪽 `AuthBar` 는 "demo 님" 이라고 하는데 상세 화면은 "로그인하지 않은 요청입니다" 라고 하는 모순으로만 보입니다.

</details>

원복: `cp /tmp/server.backup.ts labs/frontend/src/api/server.ts`

#### 실험 B. 서버 컴포넌트에서 `useState` 를 쓴다

`src/app/books/[id]/page.tsx` 맨 위에 `import { useState } from "react";` 를 더하고 컴포넌트 첫 줄에 `const [n] = useState(0);` 를 넣은 뒤 `/books/1` 을 엽니다. 터미널 B 와 브라우저에 무엇이 보입니까?

<details>
<summary>실제 결과</summary>

상태 코드는 500 이고, `pnpm dev` 터미널에 이렇게 나옵니다.

```text
⨯ ./src/app/books/[id]/page.tsx:1:10
Error: You're importing a module that depends on `useState` into a React Server Component module. This API is only available in Client Components. To fix, mark the file (or its parent) with the `"use client"` directive.
```

(`import` 줄 없이 `useState` 만 썼다면 `ReferenceError: useState is not defined` 였습니다.) 이 오류를 없애려고 `page.tsx` 맨 위에 `"use client";` 를 붙이면 이번에는 실험 C 와 같은 `'server-only' cannot be imported from a Client Component module` 오류가 납니다. 이 파일이 `@/api/server` 를 import 하는데, 클라이언트 컴포넌트가 되면 그 import 가 브라우저 쪽 번들로 들어가기 때문입니다. 서버에서 가져오는 코드와 상태가 같은 파일에 있을 수 없으므로 **상태가 필요한 부분만 작은 클라이언트 컴포넌트로 빼고** 서버 컴포넌트가 그것을 불러오는 것이 해법입니다.

</details>

원복: `cp /tmp/page.backup.tsx "labs/frontend/src/app/books/[id]/page.tsx"`

#### 실험 C. 서버 전용 모듈을 클라이언트 컴포넌트가 가져온다

`src/components/BookList.tsx`(`"use client"`)에 `import { getBook } from "@/api/server";` 한 줄을 더하고, 컴포넌트 안에서 `void getBook;` 를 한 번 참조한 뒤 `http://localhost:3000/` 을 엽니다.

<details>
<summary>실제 결과</summary>

상태 코드 500 이고, 터미널에 이렇게 나옵니다.

```text
Error: 'server-only' cannot be imported from a Client Component module
> 1 | import "server-only";
...
It should only be used from a Server Component.

Import traces:
  Client Component Browser:
    ./src/api/server.ts [Client Component Browser]
    ./src/components/BookList.tsx [Client Component Browser]
    ./src/components/BookList.tsx [Server Component]
    ./src/app/page.tsx [Server Component]
```

`src/api/server.ts` 맨 위의 `import "server-only";` 한 줄이 이 오류를 만듭니다. 이 줄을 지우고 같은 실험을 하면 대신 `next/headers` 를 가져오는 줄에서 `You're importing a module that depends on "next/headers". This API is only available in Server Components in the App Router, but you are using it in the Pages Router.` 라는 오류가 납니다(메시지가 "Pages Router" 를 말해서 원인을 찾기 어렵습니다). 더 중요한 차이는 `next/headers` 를 쓰지 않는 서버 전용 모듈입니다. 환경 변수만 읽는 한 줄짜리 모듈(`export const base = process.env.API_BASE_URL;`)을 `server-only` 없이 `BookList` 가 import 하면 **오류 없이 200 으로 빌드되고**, 브라우저 콘솔에는 `base: undefined` 가 찍혔습니다(`NEXT_PUBLIC_` 이 아닌 환경 변수는 브라우저에서 비어 있습니다). 서버 전용인 모듈은 `server-only` 로 표시해 두어야 이런 실수를 빌드가 막아 줍니다. 이 줄은 다른 패키지를 설치하지 않아도 동작합니다(Next 가 내부에서 처리합니다).

</details>

원복: `cp /tmp/BookList.backup.tsx labs/frontend/src/components/BookList.tsx`

#### 실험 D. 백엔드를 끈다

터미널 A 에서 백엔드를 Ctrl+C 로 끕니다. 그리고 `curl -s -i localhost:3000/bff/summary`, 브라우저로 `http://localhost:3000/books/1` 을 엽니다. 먼저 상태 코드와 화면을 예측하세요. 9주에서는 같은 상황에서 화면에 "요청에 실패했습니다 (500)" 알림이 보였습니다.

<details>
<summary>실제 결과</summary>

```text
HTTP/1.1 502 Bad Gateway
{"message":"백엔드에 연결할 수 없습니다"}
```

- `/books/1`: 상태 코드는 **500** 이고, `error.tsx` 가 그린 화면에 알림 제목 "요청에 실패했습니다", 본문 `fetch failed`, "다시 시도" 버튼이 있습니다(`pnpm dev`). 터미널 B 에는 `TypeError: fetch failed` 와 `ECONNREFUSED 127.0.0.1:8080`, 그리고 `digest: '...'` 가 출력됩니다.
- 화면 위쪽의 `AuthBar` 는 9주와 같습니다. 브라우저가 `/api/auth/me` 를 부르고 프록시가 500 을 주므로 "요청에 실패했습니다 (500)" 알림이 따로 보입니다. **같은 화면에 서로 다른 두 경로의 실패가 따로 그려집니다.**
- BFF 는 `502 Bad Gateway`. "내 뒤의 서버가 응답하지 않았다" 는 뜻이고, 이 코드를 우리가 직접 정했습니다(5절).
- 백엔드를 다시 켜고(DB 가 비므로 1단계의 등록 명령을 다시) "다시 시도" 를 누르면 같은 화면에서 복구됩니다. `error.tsx` 의 `retry()` 가 그 경로를 다시 가져와 그립니다.
- **프로덕션 빌드는 다릅니다.** `pnpm build && pnpm start` 로 같은 실험을 하면 화면 본문의 `fetch failed` 자리에 원래 메시지 대신 React 의 `Minified React error #441; visit https://react.dev/errors/441 ...` 문구가 나옵니다. 서버 터미널에는 원래 오류 `TypeError: fetch failed` 가 `digest: '...'` 와 함께 남습니다. 개발 중에는 원래 메시지가 보이지만, 운영에서는 서버 컴포넌트가 던진 오류의 내용이 브라우저로 가지 않습니다.

</details>

#### 실험 E. `console.log` 는 어디에 찍히는가

`page.tsx` 의 `const { id } = await params;` 위에 `console.log("[서버 컴포넌트] 책 상세 렌더링");` 를 더하고 `/books/1` 을 엽니다. 터미널 B 와 브라우저 콘솔 중 어디에 보입니까?

<details>
<summary>실제 결과</summary>

터미널 B 에 `[서버 컴포넌트] 책 상세 렌더링` 이 찍힙니다. `pnpm dev` 에서는 브라우저 콘솔에도 같은 줄이 `Server` 라는 표식과 함께 보입니다(개발 도구가 서버 로그를 복사해 보여 주는 기능입니다). 이 컴포넌트는 브라우저에서 실행되지 않았습니다. 확인하려면 `BookList.tsx` 같은 클라이언트 컴포넌트에 `console.log` 를 넣어 보세요. 그쪽은 브라우저 콘솔에 표식 없이 나오고, 첫 방문 때는 서버가 한 번 그리기 때문에 터미널에도 찍힙니다. 클라이언트 컴포넌트가 서버에서도 한 번 실행된다는 뜻입니다.

</details>

실험 E 의 줄은 지우고 원복합니다.

### 3단계. 빈칸에서 만들기 — 회원의 대출 목록 서버 페이지 (1시간)

`labs/frontend/src/app/members/[id]/page.tsx` 를 새로 만듭니다. 회원 id 를 주소에서 받아 `GET /members/{id}/loans` 를 서버에서 부르고 대출 중인 책 제목을 나열합니다. 없는 회원이면 404 화면입니다. `____` 를 채우세요. (`serverClient`, `createLibrary`, `ApiError` 는 이미 있는 것입니다.)

```tsx
import { notFound } from "next/navigation";
import { ApiError } from "@/api/client";
import { createLibrary } from "@/api/library";
import { serverClient } from "@/api/server";

export default async function MemberLoansPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await ____;
  const loans = await createLibrary(await serverClient())
    .activeLoans(Number(id))
    .catch((e: unknown) => {
      if (e instanceof ApiError && e.kind === "____") ____();
      throw e;
    });

  return (
    <section className="flex flex-col gap-gutter">
      <h2 className="text-xl font-bold">회원 #{id} 의 대출 중인 책</h2>
      {loans.length === 0 && <p className="text-muted">대출 중인 책이 없습니다</p>}
      <ul className="flex flex-col gap-field">
        {loans.map((loan) => (
          <li key={loan.id}>{loan.book.title}</li>
        ))}
      </ul>
    </section>
  );
}
```

확인: 백엔드를 다시 켜고 1단계 등록 명령을 실행한 뒤, `curl -s -b $J -X POST -H 'Content-Type: application/json' -d '{"memberId":1,"bookId":1}' $B/loans` 로 대출을 한 건 만듭니다. 그리고 아래 세 주소의 상태 코드와 `/members`(원래 있던 회원 등록 화면)를 확인합니다.

```bash
curl -s localhost:3000/members/1 | grep -o '<li>[^<]*'
for p in /members/1 /members/999 /members/abc /members; do curl -s -o /dev/null -w "$p %{http_code}\n" localhost:3000$p; done
```

<details>
<summary>정답과 확인 결과</summary>

```tsx
const { id } = await params;
...
if (e instanceof ApiError && e.kind === "notFound") notFound();
```

```text
<li>이펙티브 자바
/members/1 200
/members/999 404
/members/abc 500
/members 200
```

- `params` 는 Next 16 에서 `Promise` 입니다. `await` 없이 `params.id` 를 읽으면 `undefined` 입니다. (이 버전 문서가 `cookies()`, `headers()` 와 함께 비동기 API 로 정리한 것입니다.)
- `/members/abc` 는 **500** 입니다. 숫자가 아닌 id 가 백엔드에서 400 이 되고, 이 코드는 404 만 `notFound()` 로 바꾸므로 400 은 그대로 던져져서 `error.tsx` 쪽으로 갑니다(이 경로에는 `error.tsx` 가 없어서 Next 기본 오류 화면). `books/[id]/page.tsx` 가 첫 줄에서 `/^\d+$/` 로 걸러 `notFound()` 를 부르는 이유입니다. 연습문제 2번에서 같은 처리를 넣어 봅니다.
- `/members` 는 기존 정적 화면 그대로 200 입니다. `members/[id]` 폴더를 더해도 `members/page.tsx` 는 영향받지 않습니다.
- `.catch` 안에서 `notFound()` 를 불러도 동작하는 이유는 그 `Promise` 를 `await` 하고 있어서, 던진 예외가 렌더링 경로로 올라가기 때문입니다. `await` 하지 않은 Promise 안에서 부르면 어디서도 잡히지 않습니다(이 버전 문서의 `notFound` 설명).

</details>

끝나면 이 `members/[id]` 폴더는 **삭제**하세요(실습용이고 이 저장소에 남기지 않습니다).

---

## 1. 누가 누구를 부르는가: 경로가 하나 더 생겼다

9주의 그림에 한 줄이 더해집니다.

```text
(a) 클라이언트 컴포넌트 + TanStack Query  (BookList, MyLoans ...)
    브라우저 --/api/books--> Next 서버(3000, rewrites 프록시) --> Spring(8080)

(b) 서버 컴포넌트  (이번 주: /books/[id])
    브라우저 --/books/1 (문서 요청)--> Next 서버: 렌더링 중 fetch --> Spring(8080)

(c) Route Handler = BFF  (이번 주: /bff/summary)
    브라우저 --/bff/summary--> Next 서버: 핸들러가 fetch 둘 --> Spring(8080)
```

| | (a) 프록시 | (b), (c) 서버가 직접 |
|---|---|---|
| 브라우저가 보는 요청 | `/api/*` | `/books/1`, `/bff/summary` 만. 백엔드로 가는 요청은 **보이지 않음** |
| 호출 주소 | 상대 경로 `/api` (`NEXT_PUBLIC_API_BASE` 로 변경 가능) | 절대 주소 `http://localhost:8080`(서버 전용 `API_BASE_URL`) |
| CORS | 브라우저 입장에서는 same-origin 이라 불필요 | **검사 자체가 없음** |
| 쿠키 | 브라우저가 붙인 `Cookie` 를 프록시가 그대로 넘김 | **우리 코드가 꺼내서 실어야 함** |
| MSW 목킹 | 됨 (브라우저 서비스 워커) | **안 됨** (서버에서는 서비스 워커가 없음). 백엔드가 떠 있어야 함 |
| 캐시·무효화 | TanStack Query (10주) | 요청마다 새로 가져옴(2절). 클라이언트 캐시와는 별개 |

"CORS 가 없다" 는 말을 정확히 하겠습니다. CORS 는 **브라우저가 다른 origin 의 응답을 JS 에 넘길지** 정하는 규칙입니다(8주). Node 의 `fetch` 는 서버 프로세스가 보내는 요청이고 `Origin` 헤더를 붙이지 않습니다. 직접 확인합니다.

```bash
node --input-type=module -e '
import http from "node:http";
const srv = http.createServer((req, res) => { console.log("받은 Origin 헤더:", req.headers.origin, "| Cookie 헤더:", req.headers.cookie); res.end("ok"); }).listen(0, async () => {
  await fetch(`http://localhost:${srv.address().port}/`, { headers: { cookie: "JSESSIONID=abc" } });
  srv.close();
});'
```

```text
받은 Origin 헤더: undefined | Cookie 헤더: JSESSIONID=abc
```

`Origin` 은 `undefined` 이고, 우리가 넣은 `Cookie` 만 갑니다. 백엔드(`week25`)의 CORS 검사는 요청에 `Origin` 이 있을 때 일어납니다. 8주에서 `Origin: http://localhost:3001` 을 붙여 부르면 403 이었던 것을 떠올리세요(`curl -s -o /dev/null -w '%{http_code}\n' localhost:8080/books -H 'Origin: http://localhost:3001'` 가 403, `Origin` 없이는 200 이었습니다). 서버가 부를 때는 이 문제가 없습니다. 반대로 말하면 **백엔드의 CORS 설정은 서버 경로를 하나도 지켜 주지 않습니다.** 서버에서 부르는 경로의 접근 통제는 다른 수단(인증, 네트워크 분리)이 맡습니다.

## 2. 서버 컴포넌트에서 가져오기

`src/app/books/[id]/page.tsx` 전체입니다.

```tsx
import Link from "next/link";
import { notFound } from "next/navigation";
import { ApiError } from "@/api/client";
import { getBook, getMe } from "@/api/server";

export default async function BookDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;
  if (!/^\d+$/.test(id)) notFound(); // 숫자가 아닌 id 는 백엔드에서도 400 이다: 사용자가 고칠 입력이 아니라 틀린 주소

  // 서로 기다릴 이유가 없는 두 요청은 함께 시작한다.
  const [book, me] = await Promise.all([
    getBook(Number(id)).catch((e: unknown) => {
      if (e instanceof ApiError && e.kind === "notFound") notFound(); // 예상된 실패: not-found.tsx
      throw e; // 예상 못 한 실패: error.tsx
    }),
    getMe(),
  ]);

  return ( ... );
}
```

읽을 점입니다.

- **컴포넌트가 `async` 입니다.** `useQuery` 도 `useEffect` 도 없이 `await` 로 값을 받아 그립니다. 로딩 상태 변수가 없습니다. 값이 준비될 때까지 이 경로의 렌더링이 기다리기 때문입니다. 기다리는 동안 먼저 보여 줄 UI 는 `loading.tsx` 나 `<Suspense>` 로 만듭니다(이 주차에서는 다루지 않았습니다).
- **`params` 도 `await` 합니다.** Next 16 에서 동적 세그먼트 값은 `Promise` 입니다. 3단계에서 본 것처럼 `cookies()`, `headers()` 도 비동기입니다.
- **병렬로 가져옵니다.** `getBook` 과 `getMe` 는 서로의 결과가 필요 없으므로 `Promise.all` 로 동시에 시작합니다. `const book = await getBook(...); const me = await getMe();` 로 쓰면 두 번째 요청이 첫 번째가 끝난 뒤에 시작해서 시간이 더해집니다. 앞 결과가 필요할 때만 순차로 씁니다.
- **TanStack Query 가 없습니다.** 이 값은 이 요청의 렌더링 동안만 쓰이고 브라우저에는 HTML 로 갑니다. 캐시 키도, 무효화도 없습니다. Next 에도 자체 캐시가 있지만 이 버전 문서는 "`fetch` 요청은 기본으로 캐시되지 않는다" 고 설명하고, 이 저장소는 캐시 관련 설정을 하나도 켜지 않았습니다(`next.config.ts`). 서버 경로의 응답이 캐시되지 않는 것은 BFF 로 확인했습니다. `curl -s -b $J localhost:3000/bff/summary` 의 `bookCount` 가 책을 한 권 등록하기 전에는 2, 등록한 직후에는 곧바로 3 이었습니다(`pnpm dev`). 서버 컴포넌트의 `fetch` 도 같은 방식이라 새로고침할 때마다 백엔드로 요청이 갑니다.
- **이 경로는 요청마다 실행됩니다.** `pnpm build` 결과의 경로 표에서 `/books/[id]` 는 `ƒ (Dynamic) server-rendered on demand`, 반면 `/`, `/members`, `/loans` 는 `○ (Static) prerendered as static content` 입니다. `cookies()` 를 읽는 순간 이 경로는 요청마다 다른 결과가 나올 수 있는 동적 경로가 됩니다. 요청 시점의 값(쿠키, 헤더)을 쓰면 빌드 때 미리 만들어 둘 수 없습니다.

```text
Route (app)
┌ ○ /
├ ○ /_not-found
├ ƒ /bff/summary
├ ƒ /books/[id]
├ ○ /loans
└ ○ /members
```

**서버 컴포넌트에서 자기 Route Handler 를 부르지 마세요.** 이 버전의 BFF 가이드가 같은 뜻을 적습니다: 서버 컴포넌트는 데이터를 Route Handler 를 거치지 말고 원천에서 직접 가져와야 하며, 빌드 때 미리 렌더링되는 서버 컴포넌트가 Route Handler 를 부르면 빌드 중에는 그 서버가 없어서 실패합니다. 이 화면이 `/bff/summary` 를 부르지 않고 `getBook()` 으로 백엔드를 직접 부르는 이유입니다. Route Handler 는 **브라우저(또는 다른 클라이언트)가 부르는 입구**입니다.

## 3. 쿠키를 서버 요청에 실어 주기

`src/api/server.ts` 의 핵심입니다.

```ts
import "server-only";
import { cookies } from "next/headers";
...
const API_BASE = process.env.API_BASE_URL || "http://localhost:8080";
const SESSION_COOKIE = "JSESSIONID"; // Spring 세션 쿠키 이름

/** 요청마다 만든다. 브라우저가 이 요청에 실어 보낸 세션 쿠키를 백엔드 요청에 그대로 실어야 하기 때문이다. */
export async function serverClient() {
  const session = (await cookies()).get(SESSION_COOKIE);
  return createClient<paths>({
    baseUrl: API_BASE,
    headers: session ? { cookie: `${session.name}=${session.value}` } : {},
  });
}
```

- **왜 직접 실어야 하는가.** 브라우저의 `fetch` 는 `credentials` 설정에 따라 쿠키 저장소에서 쿠키를 알아서 붙입니다(8주). 서버의 `fetch` 에는 쿠키 저장소가 없습니다. `client.ts` 의 `credentials: "include"` 는 서버에서 아무 일도 하지 않습니다. 실험 A 의 "조용한 비로그인" 이 그 결과입니다.
- **`cookies()` 와 `headers()`.** 둘 다 들어온 요청을 읽는 비동기 함수입니다. `cookies()` 는 쿠키를 이름으로 꺼내고, `headers()` 는 `Cookie` 헤더를 통째로 줍니다(`(await headers()).get("cookie")`). 이 문서는 **필요한 쿠키 하나만** 꺼내는 쪽을 골랐습니다. 브라우저는 `localhost` 로 가는 모든 쿠키를 보내고(포트가 달라도 같은 호스트입니다), 그중에는 이 앱과 무관한 다른 개발 서버의 쿠키도 섞여 있을 수 있습니다. 헤더를 통째로 넘기면 그것까지 백엔드로 갑니다. 쿠키 이름이 `JSESSIONID` 라는 사실은 백엔드가 정한 계약의 일부입니다. 백엔드가 이름을 바꾸면 이 상수도 바뀝니다(13주의 변경 협의에서 다룹니다).
- **요청마다 만듭니다.** 모듈 최상단에서 `const client = createClient(...)` 로 한 번 만들어 두면 **첫 요청자의 쿠키가 다음 요청자에게 새어 갑니다.** 서버 프로세스는 여러 사용자의 요청이 공유합니다. 쿠키를 읽는 객체는 요청 안에서 만들어야 합니다.
- **서버 컴포넌트는 쿠키를 읽기만 합니다.** 이 버전 문서가 `cookies()` 를 서버 컴포넌트에서는 읽기용으로, 쓰기(`set`, `delete`)는 Server Function 이나 Route Handler 에서만 되는 것으로 설명합니다. 그래서 로그인(쿠키를 **받아야** 하는 일)은 여전히 브라우저가 `/api/auth/login` 으로 하고, 응답의 `Set-Cookie` 를 브라우저가 저장합니다. 서버 컴포넌트가 하는 일은 그 쿠키를 **전달**하는 것까지입니다.
- **서버 전용 주소.** `API_BASE_URL` 에는 `NEXT_PUBLIC_` 접두사가 없습니다. 환경 변수는 기본적으로 서버에서만 보이고, `NEXT_PUBLIC_` 이 붙은 것만 브라우저 번들에 들어갑니다(9주). 브라우저에 알려 줄 이유가 없는 내부 주소는 접두사를 붙이지 않습니다. `import "server-only";` 는 이 파일이 클라이언트 번들에 섞이지 않게 막는 안전장치입니다(실험 C).

`getMe` 는 10주의 `useMe` 와 **같은 규칙**입니다.

```ts
/** useMe 와 같은 규칙: 로그인하지 않았으면(401) 오류가 아니라 null. */
export async function getMe() {
  try {
    return await createLibrary(await serverClient()).me();
  } catch (e) {
    if (e instanceof ApiError && e.status === 401) return null;
    throw e;
  }
}
```

비로그인은 오류가 아니라 정상 상태라는 규약이 서버에도 그대로 옮겨졌습니다. 클라이언트와 서버가 같은 `ApiError`(`client.ts`)와 `createLibrary`(`library.ts`)를 쓰기 때문입니다. 호출 코드가 한 벌이라는 것이 6주에서 타입을 생성한 이득이 서버로도 이어지는 모습입니다.

## 4. 서버와 클라이언트의 경계

기본값은 **서버 컴포넌트**입니다. `"use client"` 는 "여기서부터는 브라우저로도 가는 코드" 라는 경계선입니다. 파일 맨 위에 쓰면 그 파일과, 그 파일이 import 하는 것이 모두 브라우저 번들에 들어갑니다.

| | 서버 컴포넌트 | 클라이언트 컴포넌트 |
|---|---|---|
| 실행 위치 | Next 서버에서만 | 서버에서 한 번 그린 뒤 브라우저에서도 (하이드레이션) |
| `useState`, `useEffect`, 이벤트 핸들러, 브라우저 API | 쓸 수 없음 (실험 B) | 씀 |
| `async` 컴포넌트, `await` | 가능 | 불가 |
| 쿠키·헤더 읽기(`cookies()`), 서버 전용 환경 변수 | 가능 | 불가 |
| 코드가 브라우저에 전달되는가 | 아니요 | 예 |
| TanStack Query | 쓸 수 없음 | 씀 (`Providers` 가 `"use client"` 인 이유) |

이 저장소의 구성을 이 표로 읽어 보세요.

```text
layout.tsx            (서버)  <html>, <body>
 └ Providers          (클라이언트) QueryClientProvider ← children 을 받음
    ├ Nav             (서버)  children 으로 들어옴. 링크만 있어서 상태가 필요 없음
    ├ AuthBar         (클라이언트) useMe, 로그인 폼
    └ page.tsx        (서버)  children 으로 들어옴 ← 이번 주 books/[id]/page.tsx 가 여기
       └ BookList     (클라이언트)
```

`Providers` 는 클라이언트인데 그 안의 `page.tsx` 는 서버 컴포넌트입니다. `page.tsx` 를 `Providers` 가 **import 한 것이 아니라 `children` 으로 받은 것**이기 때문입니다. 경계는 "무엇을 import 하는가" 로 정해지고, `children` 으로 끼워 넣은 것은 영향을 받지 않습니다. 이 패턴 덕분에 최상위에 클라이언트 제공자를 두고도 페이지를 서버 컴포넌트로 둘 수 있습니다. 실험 E 에서 `page.tsx` 의 `console.log` 가 터미널에 찍힌 것이 증거입니다.

서버에서 클라이언트로 값을 넘길 때(서버 컴포넌트가 클라이언트 컴포넌트에 props 를 줄 때)는 값이 직렬화되어 브라우저로 갑니다. 이 버전 문서는 비밀이나 민감한 필드를 props 에 싣지 말라고 경고합니다. 서버 컴포넌트가 백엔드 응답 전체를 클라이언트 컴포넌트에 그대로 넘기면, 화면에 안 쓰는 필드도 전부 브라우저에 전달됩니다(문서의 설명이고, 이 저장소에서 실행해 보지 않았습니다).

**서버와 클라이언트, 어느 쪽에서 가져오는가.**

| 이럴 때 | 이쪽 |
|---|---|
| 첫 화면에 필요한 읽기 데이터, 검색 엔진에 보여야 하는 내용 | 서버 컴포넌트 |
| 쿠키·토큰·비밀 키가 필요한 호출, 브라우저에 노출하면 안 되는 주소 | 서버 (서버 컴포넌트 또는 Route Handler) |
| 사용자 조작 뒤 갱신, 무효화, 낙관적 업데이트, 폴링 | 클라이언트 + TanStack Query (10주) |
| MSW 로 백엔드 없이 개발해야 하는 화면 | 클라이언트 (서버에서는 목킹이 안 됨) |
| 둘 다 | 서버에서 가져온 값을 TanStack Query 캐시의 초기값으로 넘기는 방법이 있음(`HydrationBoundary`). 이 버전의 Next 문서 `app/guides/client-side-data-fetching/tanstack-query.md` 에 있고, 이 저장소에서 실행해 보지 않았습니다 |

`labs/frontend/README.md` 가 "데이터 패칭은 모두 클라이언트에서 합니다" 라고 정한 것은 **의도한 단순화**였습니다. 쿠키를 싣는 요청과 목킹을 한 경로로 두기 위해서였고, 이번 주에는 그 선택의 반대편을 추가했습니다. 두 방식이 한 저장소에 공존하므로 새 화면을 만들 때마다 위 표로 하나를 고릅니다.

## 5. 서버에서의 에러 처리

7주 5절은 서버 컴포넌트에서 오류가 갈라지는 방식을 `books/[id]` 한 곳을 예로 비교표로 정리했습니다. 그 표를 이번 주 코드로 실제로 확인했습니다.

| 상황 | 이 코드의 처리 | 결과 |
|---|---|---|
| 백엔드 404 (책 없음) | `notFound()` | 상태 **404**, `not-found.tsx` 화면, `<meta name="robots" content="noindex">` |
| id 가 숫자가 아님 | 요청 전에 `notFound()` | 위와 같음 (백엔드는 400 을 줄 입력) |
| `/auth/me` 401 | `null` 반환 | 오류가 아님. "로그인하지 않은 요청입니다" |
| 그 밖의 실패 (백엔드 꺼짐, 5xx) | 그대로 `throw` | 상태 **500**, `error.tsx` 화면 |

**`notFound()` 는 던지는 함수입니다.** 이 버전 문서가 `NEXT_HTTP_ERROR_FALLBACK;404` 오류를 던져서 해당 경로의 렌더링을 끝낸다고 설명합니다. 그래서 `getBook(...).catch(...)` 안에서 불러도, 그 결과를 `await` 하고 있어서 렌더링 경로 안에 있으면 동작합니다. 반대로 `try { ... } catch { ... }` 가 이 오류까지 삼켜 버리면 404 화면이 사라집니다. 예상 못 한 실패를 잡으려는 `catch` 를 쓸 때는 이 오류를 다시 던지는지 확인하세요.

`not-found.tsx` 는 7주의 `ErrorNotice` 를 그대로 씁니다.

```tsx
import { ApiError } from "@/api/client";
import { ErrorNotice } from "@/components/ErrorNotice";

// 클라이언트의 404 와 같은 ErrorNotice 를 쓴다: 서버에서 그려도 화면 규약은 하나다.
export default function BookNotFound() {
  return <ErrorNotice error={new ApiError(404, "요청한 책이 없습니다")} />;
}
```

클라이언트에서 404 를 받으면 `ErrorNotice` 가 `notFound` 종류(제목 "찾을 수 없습니다", `data-kind="notFound"`)로 그립니다. 서버에서도 같은 컴포넌트로 같은 모양이 나옵니다. `ErrorNotice` 는 훅이 없어서 서버·클라이언트 양쪽에서 쓸 수 있습니다. 7주 5절이 "`ApiError` 를 그대로 던지지 말고 `kind` 별로 갈라야 한다" 고 예고한 것이 이 `page.tsx` 의 `.catch` 입니다.

`error.tsx` 입니다.

```tsx
"use client"; // 에러 바운더리는 클라이언트 컴포넌트여야 한다

import { Button } from "@/components/Button";
import { ErrorNotice } from "@/components/ErrorNotice";

// 예상 못 한 실패(백엔드 꺼짐, 500 ...)의 마지막 화면. 운영 빌드에서는 error.message 가 일반 문구로 바뀐다.
export default function BookError({ error, retry }: { error: unknown; retry: () => void }) {
  ...
}
```

- 이 버전의 `error.tsx` 는 `error` 와 **`retry`** 를 props 로 받습니다. 예전 자료의 `reset` 이 아닙니다. `retry()` 는 해당 경로의 자식을 다시 가져와 그립니다(실험 D 에서 복구 확인).
- 개발 중에는 서버 컴포넌트가 던진 오류의 원래 메시지(`fetch failed`)가 보이지만, **운영 빌드에서는 메시지가 일반화되고 `digest` 만 남습니다.** 실험 D 의 프로덕션 결과입니다. 사용자에게 보일 문구를 살리려면 던지지 말고 **값으로 돌려줘서** 그리거나(401 을 `null` 로 돌려주는 `getMe` 가 그 방식입니다), 던지되 `error.tsx` 에서 사용자 문구를 따로 정합니다. 서버 로그의 `digest` 와 화면의 `digest` 가 같은 오류를 가리키므로 사용자 제보에서 로그를 찾는 열쇠가 됩니다.
- "예상한 실패" 와 "예상 못 한 실패" 를 나누는 기준은 7주와 같습니다. 404(없는 책)는 예상한 실패라서 `notFound()` 로 **의도해서** 그리고, 백엔드 다운은 예상 못 한 실패라서 `error.tsx` 가 받습니다.
- 에러 바운더리는 렌더링 중의 오류만 잡습니다. 버튼 클릭 핸들러의 오류는 잡지 못하므로, 그쪽은 10주처럼 `mutation.error` 상태로 그립니다.

## 6. Route Handler (BFF) 맛보기

`src/app/bff/summary/route.ts` 전체입니다.

```ts
import { ApiError } from "@/api/client";
import { createLibrary } from "@/api/library";
import { getMe, serverClient } from "@/api/server";

// BFF 맛보기: 화면 하나가 필요로 하는 호출 둘(책 목록, 내 정보)을 서버에서 묶고, 응답 모양도 프론트가 정한다.
export async function GET() {
  try {
    const [books, me] = await Promise.all([createLibrary(await serverClient()).listBooks(), getMe()]);
    return Response.json({ bookCount: books.length, username: me?.username ?? null });
  } catch (e) {
    if (e instanceof ApiError) return Response.json({ message: e.message }, { status: e.status });
    return Response.json({ message: "백엔드에 연결할 수 없습니다" }, { status: 502 }); // 502: 우리 뒤의 서버가 응답하지 않음
  }
}
```

`route.ts` 에서 `GET`, `POST` 같은 이름의 함수를 내보내면 그 경로(`/bff/summary`)가 HTTP 엔드포인트가 됩니다. 요청과 응답은 웹 표준 `Request`, `Response` 입니다. Express 의 핸들러와 비슷하지만 `(req, res)` 가 아니라 `Request` 를 받아 `Response` 를 **돌려줍니다.**

이 코드가 BFF(Backend For Frontend, 프론트를 위한 백엔드)인 이유입니다.

1. **호출을 묶습니다.** 화면이 필요한 두 값을 브라우저가 2번 부르는 대신 서버가 부릅니다. 브라우저와 Next 서버 사이는 사용자의 네트워크라 느릴 수 있고, Next 서버와 백엔드 사이는 같은 곳에 배치하면 빠른 경우가 많습니다(이 저장소에서 시간을 재 보지는 않았습니다).
2. **응답 모양을 프론트가 정합니다.** 백엔드의 `MemberResponse` 가 아니라 `{ bookCount, username }` 만 내려 줍니다. 화면에 필요 없는 필드는 아예 브라우저에 가지 않습니다.
3. **에러 계약을 번역합니다.** 백엔드 오류(`ApiError`)는 상태 코드와 `{"message"}` 를 그대로 전달하고, 백엔드에 닿지 못한 것은 `502` 로 바꿉니다. 이 변환은 이제 프론트 팀이 지켜야 하는 새 계약입니다. 호출하는 쪽(브라우저 코드)은 `{"message"}` 와 상태 코드 규약(7주)을 그대로 쓸 수 있습니다.

**쓰지 말아야 할 때.**

| 상황 | 대신 |
|---|---|
| 요청을 그대로 넘기기만 한다 | `next.config.ts` 의 `rewrites`(9주). 코드 0줄 |
| 서버 컴포넌트가 데이터를 얻으려고 | 직접 `getBook()` 처럼 부른다 (2절) |
| 백엔드가 이미 화면에 맞는 응답을 준다 | 그대로 쓴다. 층이 하나 늘 때마다 장애 지점과 이중 에러 처리가 늘어난다 |

**비용도 계산합니다.**

- **새 서버를 운영합니다.** 배포·장애 대응·로그·타임아웃이 프론트 팀 몫이 됩니다. 이 핸들러는 백엔드가 느리면 같이 느려지는데, 타임아웃을 정하지 않았습니다. 운영에서는 정해야 합니다.
- **Route Handler 는 공개 엔드포인트입니다.** 이 버전 문서가 "모든 클라이언트가 접근할 수 있다" 고 적습니다. 이 핸들러는 쿠키를 그대로 백엔드에 전달해서 **인증은 백엔드가 결정**하지만, 응답에 무엇을 싣는지는 이제 핸들러의 책임입니다. 비로그인 요청에도 `bookCount` 가 나갑니다(읽기 API 가 원래 공개라서 맞춘 것입니다). 백엔드가 인증을 요구하는 데이터라면 BFF 가 그것을 비로그인에게 흘려보내면 안 됩니다.
- **쓰기를 만들면 CSRF(8주)가 프론트 서버에도 생깁니다.** 이 핸들러는 `GET` 만 있어서 문제가 없습니다. `POST` 를 만들어 쿠키를 그대로 넘기면, 8주에서 본 "쿠키가 자동으로 붙는 인증" 의 위험이 BFF 에도 똑같이 적용됩니다(개념이며 이 저장소에서 실행해 보지 않았습니다).
- **경로가 겹치지 않게 접두사를 나눕니다.** `/api/*` 는 이미 `rewrites` 가 Spring 으로 넘기는 경로라서, BFF 는 `/bff/*` 로 두었습니다.

## 7. 한계와 이 주차가 다루지 않은 것

- **MSW 목킹이 서버 경로에는 적용되지 않습니다.** `NEXT_PUBLIC_API_MOCKING=enabled` 여도 `/books/1`, `/bff/summary` 는 실제 백엔드를 부릅니다. 프론트 README 가 이 이유로 데이터 패칭을 클라이언트에 모았습니다. 서버 경로를 테스트하려면 MSW 의 node 서버를 서버 쪽에서 켜거나 백엔드를 띄워야 하는데, 이 저장소에는 없습니다. 이 주차의 서버 코드는 `pnpm test` 가 아니라 브라우저와 `curl` 로만 확인했습니다.
- **Next 의 캐시(`use cache`, `revalidate`)와 스트리밍(`loading.tsx`, `<Suspense>`), Server Actions** 는 다루지 않았습니다. 이 저장소는 캐시 관련 설정을 켜지 않았고, 서버 경로는 요청마다 백엔드를 부릅니다.
- **배포 환경.** 이 주차의 `API_BASE_URL` 기본값 `http://localhost:8080` 은 로컬 개발용입니다. 배포하면 Next 서버가 백엔드에 닿는 주소와 네트워크가 달라집니다. 이 차이는 [13주 협업 실무](week13-collab-practice.md)에서 다룹니다.
- **서버 컴포넌트의 인증 가드**(로그인하지 않으면 다른 화면으로 보내기)와 **세션 만료 처리**는 다루지 않았습니다. 이 화면은 읽기 API 라서 비로그인도 그대로 보여 줍니다.

## TS 와 비교

백엔드 용어를 프론트의 익숙한 것에 대응시킨 표입니다. 완전히 같지는 않습니다.

| 백엔드(Java·Spring) 쪽 | 프론트에서 만나는 모습 |
|---|---|
| 서버가 HTML 을 그리는 컨트롤러 + 템플릿(Thymeleaf 같은) | 서버 컴포넌트 |
| `RestClient` / `WebClient` (서버가 다른 서버를 부름) | 서버 컴포넌트의 `fetch`, `serverClient()` |
| `HttpServletRequest.getCookies()` | `await cookies()` |
| 컨트롤러가 던지는 `ResponseStatusException(NOT_FOUND)` | `notFound()` |
| 기본 오류 페이지(Whitelabel) | `error.tsx`, `not-found.tsx` |
| API 게이트웨이, BFF | Route Handler, `rewrites` |
| 서비스 사이 호출에는 CORS 가 없음 | 서버가 부를 때는 `Origin` 이 없어서 CORS 가 없음 |
| `application.properties` 의 비공개 값 | `NEXT_PUBLIC_` 이 없는 환경 변수(`API_BASE_URL`) |
| 빈이 요청 범위(request scope)인지 싱글턴인지 | `serverClient()` 를 요청마다 만들어야 하는 이유(싱글턴에 쿠키를 두면 새어 나감) |

## 연습문제

1. 실험 A 의 증상(조용한 비로그인)을 **테스트나 확인 절차로** 어떻게 잡을 수 있을지 쓰세요. 이 저장소의 `pnpm test` 가 그것을 못 잡는 이유와 함께 쓰세요.
2. `members/[id]` 실습 페이지에 `/^\d+$/` 검사를 더해 `/members/abc` 가 404 가 되게 하세요. 그리고 이 처리를 `books/[id]` 와 `members/[id]` 양쪽에 반복하지 않으려면 어디에 두면 좋을지 한 문장으로 쓰세요. (끝나면 원복·삭제.)
3. `/bff/summary` 가 비로그인에게는 `bookCount` 만 주고 로그인한 사용자에게는 `username` 도 주고 있습니다. 백엔드의 `GET /books` 가 인증을 요구하도록 바뀌었다고 가정하면 이 핸들러는 무엇을 바꿔야 합니까?
4. 서버 컴포넌트가 백엔드 응답(`Book` 객체 전체)을 그대로 클라이언트 컴포넌트에 props 로 넘기면 어떤 위험이 있습니까? 이 저장소의 `BookResponse` 로 이야기해 보세요. 지금 위험이 거의 없는 이유도 쓰세요.
5. `API_BASE_URL=http://localhost:9999 pnpm dev` 로 띄우고 `/books/1` 을 열면 어떻게 됩니까? 예측하고 확인하세요. 같은 환경에서 브라우저의 책 목록(`/`)은 어떻게 됩니까? 이유를 설명하세요.
6. `/books/[id]` 를 TanStack Query 로 바꾸는 것(클라이언트 컴포넌트에서 `useQuery`)과 지금의 서버 컴포넌트를 비교하세요. 세 가지 기준(첫 화면 속도, 로그인 쿠키 처리, 목킹 가능 여부)으로 한 줄씩 쓰세요.

<details>
<summary>1번과 5번 확인 결과</summary>

- 1번: `pnpm test` 는 Vitest 가 MSW 로 **브라우저와 같은 클라이언트 코드**를 검사하는 것이고, `next/headers` 의 `cookies()` 는 Next 서버 요청 안에서만 값이 있어서 단위 테스트로 부르기 어렵습니다. 잡는 방법은 서버를 띄우고 쿠키를 넣은 요청과 안 넣은 요청의 응답 문구를 비교하는 확인 절차(1단계 5번의 `curl` 두 줄)이거나, 브라우저로 로그인 뒤 상세 화면을 여는 E2E(12주)입니다. 로그인 뒤 `/books/1` 에서 "demo 님으로 로그인한 요청입니다" 를 기대하는 테스트가 실험 A 에서 실패합니다.
- 5번: `/books/1` 은 상태 500 이고 `error.tsx` 가 그려집니다(연결을 거절당한 `fetch failed`). 브라우저의 `/` 책 목록은 **정상**입니다. 클라이언트 컴포넌트는 `API_BASE_URL` 이 아니라 `rewrites` 의 8080 으로 가기 때문입니다. 두 경로의 주소가 서로 독립이라는 점이 환경을 바꿀 때 놓치기 쉽습니다(13주).

</details>

## 퀴즈

1. 서버 컴포넌트가 백엔드를 부를 때 CORS 설정이 필요 없는 이유는?
2. 브라우저가 `/books/1` 문서를 요청할 때 보낸 쿠키가 백엔드 요청에 자동으로 실리지 않는 이유는? `credentials: "include"` 가 서버에서 하는 일은?
3. `const client = createClient(...)` 를 모듈 최상단에 한 번만 만들면 안 되는 이유는?
4. `"use client"` 컴포넌트 안에 서버 컴포넌트를 `children` 으로 끼워 넣을 수 있는 이유는?
5. `notFound()` 와 `throw` 는 화면과 상태 코드가 어떻게 다릅니까?
6. 이 버전의 `error.tsx` 가 받는 props 두 개는? 운영 빌드에서 원래 오류 메시지는 어디서 볼 수 있습니까?
7. 서버 컴포넌트가 자기 앱의 Route Handler(`/bff/summary`)를 `fetch` 하면 안 되는 이유는?
8. `rewrites` 로 충분한 경우와 BFF 가 필요한 경우를 한 줄씩 쓰세요.

<details>
<summary>정답</summary>

1. CORS 는 브라우저가 다른 origin 의 응답을 JS 에 넘길지 정하는 규칙이고, 서버(Node)가 보내는 요청에는 `Origin` 헤더가 붙지 않아 백엔드의 CORS 검사가 일어나지 않습니다.
2. 서버의 `fetch` 에는 쿠키 저장소가 없어서 아무것도 자동으로 붙지 않습니다. 서버에서 `credentials` 는 아무 일도 하지 않으므로, `cookies()` 로 꺼내서 `cookie` 헤더에 직접 실어야 합니다.
3. 서버 프로세스를 여러 사용자의 요청이 공유하므로, 첫 요청의 쿠키가 담긴 객체가 다음 요청에도 쓰여서 세션이 새어 나갑니다. 요청마다 만들어야 합니다.
4. 경계는 import 로 정해지고, `children` 으로 받은 요소는 그 클라이언트 컴포넌트가 import 한 것이 아니라 서버가 이미 만든 결과이기 때문입니다.
5. `notFound()` 는 상태 404 와 `not-found.tsx`(`noindex` 포함), `throw` 는 상태 500 과 `error.tsx` 입니다.
6. `error` 와 `retry`. 서버 터미널(로그)에서 `digest` 와 함께 봅니다.
7. 서버 컴포넌트는 데이터를 원천에서 직접 가져오는 것이 맞고, 자기 Route Handler 를 거치면 빌드 중에는 그 서버가 없어 실패하며, 요청 시점에도 불필요한 HTTP 왕복이 생깁니다.
8. `rewrites`: 요청을 그대로 넘기기만 할 때. BFF: 여러 호출을 묶거나 응답을 화면에 맞게 줄이거나 에러를 번역해야 할 때.

</details>

## 체크리스트

- [ ] 1단계 6줄을 예측했고, 브라우저 Network 탭에서 백엔드로 가는 요청이 보이지 않는 것을 직접 확인했다
- [ ] 실험 A 로 "쿠키를 안 실으면 조용히 비로그인" 을 재현하고, `rewrites` 길과 무엇이 다른지 설명할 수 있다
- [ ] 실험 B, C 의 오류 메시지를 읽고 `"use client"` 와 `server-only` 가 하는 일을 설명할 수 있다
- [ ] 백엔드를 끈 상태에서 `error.tsx` 의 개발·운영 화면 차이와 BFF 의 502 를 확인했다
- [ ] `console.log` 가 서버 컴포넌트에서는 터미널에 찍히는 것을 확인했다
- [ ] `notFound()` 와 `error.tsx` 를 언제 쓰는지, 상태 코드가 어떻게 다른지 설명할 수 있다
- [ ] 3단계 페이지를 만들어 200, 404, 500 을 확인하고 폴더를 삭제했다
- [ ] 서버 경로의 세 가지 한계(MSW 안 됨, 쿠키 직접 전달, 요청마다 실행)를 말할 수 있다
- [ ] Route Handler(BFF) 를 쓸 때와 쓰지 말아야 할 때를 구분하고 비용을 열거할 수 있다
- [ ] 실험으로 고친 파일(`server.ts`, `page.tsx`, `BookList.tsx`)을 모두 백업본으로 원복했다
