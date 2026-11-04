# 6주차. API 계약: OpenAPI 로 타입 만들기

**예상 시간:** 6~8시간 (하루 1시간 세션 6회) | **목표:** 백엔드가 내놓는 API 명세(OpenAPI)에서 TypeScript 타입을 **생성**하고, 그 타입만으로 API 를 호출한다. 백엔드가 응답 필드를 바꾸면 `pnpm typecheck` 가 어느 파일에서 깨지는지 직접 확인한다.

> 1주(HTTP·DevTools·curl)와 2주(REST 설계와 JSON 계약)를 먼저 끝내세요. 이 주차부터는 응답을 눈으로 읽는 데서 한 걸음 나아가, 백엔드가 내놓는 명세 파일에서 TypeScript 타입을 **만들어** 씁니다.
>
> 실습 대상은 1~2주와 같은 도서관 참조 API `../labs/backend/week25` 입니다. "백엔드 팀이 준 API" 로 보고 호출만 하며, 백엔드 소스는 고치지 않습니다. 그 API 를 호출하는 클라이언트가 `../labs/frontend` 입니다(Next + TanStack Query + `openapi-fetch`). 3~4주에서 읽은 컨트롤러와 DTO 를 이 주차에서는 "스펙이 만들어지는 곳"으로 다시 만납니다.
>
> 준비물: JDK 21, Node 22.12+, pnpm 10+, `curl`, `jq`. 프론트 패키지는 처음이라면 `pnpm -C labs/frontend install` 을 먼저 합니다. 명령은 별도 언급이 없으면 저장소 루트에서 실행합니다.

## 하루 1시간으로 나누면

이 주차는 하루 1시간씩 6번의 세션으로 나눕니다. 한 세션은 50~70분이고, 1~2시간이 걸리는 3단계는 (1)과 (2)로 나눕니다.

| 세션 | 할 일 | 끝났을 때 할 수 있어야 하는 것 |
|---|---|---|
| 1 | 1단계(스펙을 예측하기 → 서버를 켜고 → 비교하기, 30분)를 하고, 1절(계약이란 무엇인가)과 2절(백엔드: 스펙이 만들어지는 곳)을 읽는다 | 서버가 내놓는 스펙(`openapi.json`)이 어디서 만들어지는지 말하고, 예측한 스펙과 실제를 비교할 수 있다 |
| 2 | 2단계(일부러 망가뜨리기 — 백엔드가 계약을 바꿨다면, 1시간)의 실험 A, 실험 B를 한다 | 응답 필드 이름이 바뀌었을 때 `pnpm typecheck` 가 깨지는 파일 목록을 보고 `gen:api` 로 원복할 수 있다 |
| 3 | 3절(필수와 nullable 은 다른 문제)을 읽고, 3단계(빈칸에서 만들기 — 생성 타입만으로 호출 작성, 1~2시간)의 (1) 빈칸 채우기를 한다 | `title?` 와 `returnedAt: string` 이 왜 위험한지 설명하고, 손으로 쓴 타입 없이 생성 타입만으로 API 호출을 쓸 수 있다 |
| 4 | 3단계의 (2) `pnpm typecheck` 가 막는 것 고르기를 하고, 4절(프론트: 스펙을 타입으로, 타입으로 호출), 5절(스펙 드리프트를 CI 에서 잡기), 6절(한계: 계약이 보증하지 않는 것)을 읽는다 | `pnpm typecheck` 가 막는 호출과 그 이유를 말하고, 드리프트를 잡는 세 단계 검사가 각각 무엇을 지키는지, 계약이 보증하지 않는 것이 무엇인지 말할 수 있다 |
| 5 | 연습문제와 퀴즈를 푼다 (압축 코스에서는 건너뜀) | `GET /books/{id}` 를 부르는 함수를 생성 타입만으로 쓰고, `returnedAt` 의 `null` 허용이 사라지는 백엔드 PR 에 남길 리뷰 코멘트를 쓸 수 있다 |
| 6 | 체크리스트의 각 항목을 내 말로 설명해 보고, 막히는 항목은 해당 절을 다시 읽은 뒤 학습 노트를 정리한다 | 스펙이 바뀌었을 때 `gen:api` 와 `pnpm typecheck` 로 영향받는 파일을 찾을 수 있다 |

세션을 시작하기 전에 이전 세션의 마지막 결과를 1~2분 훑어봅니다. 막히면 아래 해당 개념 절을 사전처럼 펼쳐 보세요.

## 🖐 먼저 손으로 (읽기 전에 하세요)

> 이 주차의 핵심은 아래 단계를 **직접 해 보면서** 깨닫는 것입니다. 아래쪽 설명 문서는 먼저 읽지 않아도 됩니다. 막혔을 때 사전처럼 펼쳐 보세요.

### 1단계. 스펙을 예측하기 → 서버를 켜고 → 비교하기 (30분)

참조 API 를 켜기 **전에**, 아래 질문의 답을 노트에 적으세요. (2주의 REST·JSON 계약 지식으로 추측합니다.)

| # | 질문 | 내 예측 |
|---|------|------|
| 1 | `GET /v3/api-docs` 의 상태 코드와 `Content-Type` 은? | |
| 2 | 스펙에 적힌 `POST /loans` 의 응답 코드는 몇 개이고 무엇인가? | |
| 3 | `LoanResponse` 에서 "반드시 있는 필드" 는 무엇인가? `returnedAt` 은 거기에 들어가는가? | |
| 4 | `returnedAt` 의 타입은 TypeScript 로 무엇이 될까? | |
| 5 | `GET /books` 의 `keyword` 쿼리 파라미터는 필수인가? | |
| 6 | `BookRequest` 의 `isbn` 에 붙은 `@NotBlank` 는 스펙에서 어떻게 보일까? | |

서버를 켭니다. 8080 이 비어 있어야 합니다.

```bash
lsof -i :8080                                  # 아무것도 안 나와야 함
cd labs/backend && ./gradlew :week25:bootRun   # 한 터미널에서 (Ctrl+C 로 종료)
```

다른 터미널에서, **저장소 루트**로 돌아와 같은 질문을 실제로 확인합니다.

```bash
B=localhost:8080
curl -s -o /dev/null -w '%{http_code} %{content_type}\n' $B/v3/api-docs
curl -s $B/v3/api-docs | jq -c '.paths["/loans"].post.responses | keys'
curl -s $B/v3/api-docs | jq -c '.components.schemas.LoanResponse | {required, returnedAt: .properties.returnedAt.type}'
curl -s $B/v3/api-docs | jq -c '.paths["/books"].get.parameters[0] | {name, required}'
curl -s $B/v3/api-docs | jq -c '.components.schemas.BookRequest.properties.isbn'
grep -n "returnedAt: \|keyword?" labs/frontend/src/api/schema.d.ts
```

<details>
<summary>정답 (예측을 적은 뒤에 여세요)</summary>

```text
200 application/json
["201","400","401","404","409"]
{"required":["book","id","loanedAt","member","returnedAt"],"returnedAt":["string","null"]}
{"name":"keyword","required":false}
{"type":"string","minLength":1}
190:            returnedAt: string | null;
367:                keyword?: string;
```

- 2번: 성공(201)과 실패 4개(400/401/404/409)가 **스펙에 전부 적혀 있습니다.** 2주에서 손으로 정리한 엔드포인트별 계약이 기계가 읽는 형태로 존재하는 셈입니다.
- 3~4번: `returnedAt` 은 "반드시 있는 필드" 목록에 **들어 있으면서** 타입이 `["string","null"]` 입니다. 키는 항상 오고 값만 `null` 일 수 있다는 뜻이고, TypeScript 에서는 `string | null` 입니다. "필드가 없을 수 있다" 는 `?`(옵셔널)로 따로 표현됩니다. 두 개념은 다릅니다.
- 5번: `keyword?: string`. 필수가 아니라서 호출할 때 생략할 수 있습니다.
- 6번: `@NotBlank` 는 `minLength: 1` 로 나옵니다. 그러나 TypeScript 타입은 그냥 `string` 입니다. 타입으로는 "빈 문자열 금지" 를 표현할 수 없으므로, 이 검사는 여전히 서버(400)에 기댑니다.
- 이 스펙은 저장소에 `../labs/backend/week25/openapi.json` 으로 들어 있고, 서버가 지금 내놓는 것과 같은 내용입니다.

</details>

### 2단계. 일부러 망가뜨리기 — 백엔드가 계약을 바꿨다면 (1시간)

백엔드 팀이 응답 모양을 바꾸면 프론트에서는 무슨 일이 생기는지 직접 봅니다. 백엔드 소스는 고치지 않습니다. 대신 **스펙 파일의 사본을 `jq` 로 고쳐서** 그 상황을 흉내 냅니다. 고치는 파일은 프론트의 생성 파일 `labs/frontend/src/api/schema.d.ts` 하나뿐이고, 언제든 `pnpm -C labs/frontend gen:api` 로 원래대로 되돌릴 수 있습니다.

(진짜로 백엔드 DTO 를 고쳐서 서버를 띄웠을 때 `/v3/api-docs` 가 이 사본과 같은 스펙을 내놓는 것도 확인했습니다. 아래 사본은 그 결과를 재현한 것입니다.)

#### 실험 A. 응답 필드 이름이 바뀐다 (`title` → `name`)

백엔드 개발자가 `BookResponse` 의 `title` 을 `name` 으로 바꿨다고 가정합니다.

```bash
SPECS=$(mktemp -d)       # 고친 스펙 사본을 둘 임시 폴더
jq '.components.schemas.BookResponse |= (.properties |= with_entries(if .key=="title" then .key="name" else . end) | .required |= map(if .=="title" then "name" else . end))' \
  labs/backend/week25/openapi.json > $SPECS/title-to-name.json
pnpm -C labs/frontend exec openapi-typescript $SPECS/title-to-name.json -o src/api/schema.d.ts
pnpm -C labs/frontend typecheck
```

먼저 노트에 적으세요. **`title` 을 쓰는 파일은 몇 개이고, 어디가 깨질까요?**

<details>
<summary>실제 결과 (예측을 적은 뒤에 여세요)</summary>

`pnpm typecheck` 가 아래 11개 오류를 냅니다. (`pnpm test` 의 `client.test.ts` 18개와 `query-design.test.ts` 12개는 **모두 통과**합니다. 아래 "스펙 드리프트" 절 참고.)

```text
src/api/client.test.ts(54,31): error TS2339: Property 'title' does not exist on type '{ id: number; isbn: string; name: string; author: string; }'.
src/app/books/[id]/page.tsx(22,15): error TS2339: Property 'title' does not exist on type '{ id: number; isbn: string; name: string; author: string; }'.
src/components/BookForm.tsx(47,79): error TS2339: Property 'title' does not exist on type '{ id: number; isbn: string; name: string; author: string; }'.
src/components/BookList.tsx(37,45): error TS2339: Property 'title' does not exist on type '{ id: number; isbn: string; name: string; author: string; }'.
src/components/LendForm.tsx(38,80): error TS2339: Property 'title' does not exist on type '{ id: number; isbn: string; name: string; author: string; }'.
src/components/MyLoans.tsx(40,55): error TS2339: Property 'title' does not exist on type '{ id: number; isbn: string; name: string; author: string; }'.
src/lib/query-design.test.ts(140,61): error TS2353: Object literal may only specify known properties, and 'title' does not exist in type '{ id: number; isbn: string; name: string; author: string; }'.
src/mocks/handlers.ts(21,27): error TS2353: Object literal may only specify known properties, and 'title' does not exist in type '{ id: number; isbn: string; name: string; author: string; }'.
src/mocks/handlers.ts(22,27): error TS2353: Object literal may only specify known properties, and 'title' does not exist in type '{ id: number; isbn: string; name: string; author: string; }'.
src/mocks/handlers.ts(78,62): error TS2339: Property 'title' does not exist on type '{ id: number; isbn: string; name: string; author: string; }'.
src/mocks/handlers.ts(89,51): error TS2353: Object literal may only specify known properties, and 'title' does not exist in type '{ id: number; isbn: string; name: string; author: string; }'.
```

- 화면 5곳(`BookForm`, `BookList`, `LendForm`, `MyLoans`, `books/[id]/page.tsx`), 테스트 파일 2곳(`client.test.ts`, `query-design.test.ts`), MSW 가짜 서버 4곳이 한꺼번에 드러납니다. 문자열 `"title"` 을 검색해서는 못 찾을 곳(`b.title`, `create.data.title`, `loan.book.title`)까지 잡힙니다.
- `src/api/schema.d.ts` 의 변화는 한 줄입니다. `title: string;` 이 `name: string;` 이 됩니다. 요청 쪽 `BookRequest.title` 은 그대로입니다. 요청과 응답은 다른 타입이기 때문입니다.
- 이 11개를 고치고 나면 화면은 다시 정상입니다. 계약을 따라 고치는 일이 "찾기" 가 아니라 "컴파일러가 알려 주는 목록 처리" 가 됩니다.

</details>

원복합니다. 생성 파일을 원래 스펙에서 다시 만들면 됩니다.

```bash
pnpm -C labs/frontend gen:api && pnpm -C labs/frontend typecheck      # 오류 없이 끝나야 함
```

#### 실험 B. "필수" 표시를 빼먹는다

백엔드 DTO 의 필수 표시(`@Schema(requiredMode = REQUIRED)`)를 빼먹으면 어떻게 될까요? 아래처럼 스펙 사본을 고칩니다.

| 백엔드 DTO 에서 이렇게 바뀌면 | 스펙 사본에서 흉내 내는 방법 |
|---|---|
| `BookResponse` 의 `title` 에서 `@Schema(requiredMode = REQUIRED)` 가 빠진다 | `required` 목록에서 `"title"` 을 뺀다 |
| `LoanResponse` 의 `returnedAt` 에서 `types = {"string", "null"}` 가 빠진다 | `returnedAt` 의 `type` 을 `["string","null"]` 에서 `"string"` 으로 |

```bash
jq '.components.schemas.BookResponse.required |= map(select(. != "title")) | .components.schemas.LoanResponse.properties.returnedAt.type = "string"' \
  labs/backend/week25/openapi.json > $SPECS/required-lost.json
pnpm -C labs/frontend exec openapi-typescript $SPECS/required-lost.json -o src/api/schema.d.ts
pnpm -C labs/frontend typecheck
```

<details>
<summary>실제 결과</summary>

`schema.d.ts` 가 이렇게 바뀝니다.

```text
title: string;            ->  title?: string;
returnedAt: string | null;  ->  returnedAt: string;
```

`pnpm typecheck` 의 오류는 겨우 2개입니다.

```text
src/mocks/handlers.ts(78,60): error TS18048: 'b.title' is possibly 'undefined'.
src/mocks/handlers.ts(127,95): error TS2322: Type 'null' is not assignable to type 'string'.
```

- `title?` 은 "없을 수 있다" 로 읽혀서 **원래는 안전하던 모든 사용처에 `undefined` 가능성이 번집니다.** 그런데 JSX 에 `{b.title}` 로 찍는 곳은 `undefined` 를 받아도 컴파일되므로 오류가 거의 나지 않습니다. 오류가 적다고 안전한 것이 아닙니다.
- `returnedAt: string` 은 더 위험합니다. 실제 서버는 반납 전에 `null` 을 보내는데 타입은 "항상 문자열" 이라고 거짓말합니다. 프론트가 `loan.returnedAt.slice(0, 10)` 같은 코드를 써도 컴파일러는 막지 못하고, 런타임에 터집니다. **계약이 틀리면 생성된 타입이 오히려 거짓 안심을 줍니다.**

</details>

같은 방법으로 원복합니다.

```bash
pnpm -C labs/frontend gen:api && pnpm -C labs/frontend typecheck
```

### 3단계. 빈칸에서 만들기 — 생성 타입만으로 호출 작성 (1~2시간)

`labs/frontend` 에 채울 파일은 새로 만들지 않습니다. 노트나 임시 파일에서 작성하고, 확인하고 싶으면 `labs/frontend/src/scratch.ts` 를 만들어 `pnpm -C labs/frontend typecheck` 를 돌린 뒤 **지웁니다.**

**(1) 아래 `____` 를 채우세요.** 손으로 쓴 타입은 한 줄도 없어야 합니다. 경로 문자열, 파라미터, 필드 이름은 모두 `schema.d.ts` 에서 옵니다.

```ts
import { api } from "@/api/client";
import type { components } from "@/api/schema";

type Loan = components["schemas"]["LoanResponse"];

// 회원 id 를 받아, 그 회원이 지금 빌린 책 제목 배열을 돌려준다
async function titlesOnLoan(memberId: number): Promise<string[]> {
  const { data, error } = await api.GET("____", { params: { path: { ____ } } });
  if (error) throw new Error(error.message);
  return data.map((loan) => ____);
}

// 반납일 문자열. 아직 반납 전이면 "대출 중"
const returnedLabel = (loan: Loan) => ____;
```

<details>
<summary>정답</summary>

```ts
async function titlesOnLoan(memberId: number): Promise<string[]> {
  const { data, error } = await api.GET("/members/{id}/loans", { params: { path: { id: memberId } } });
  if (error) throw new Error(error.message);
  return data.map((loan) => loan.book.title);
}

const returnedLabel = (loan: Loan) => (loan.returnedAt === null ? "대출 중" : loan.returnedAt.slice(0, 10));
```

- 위 코드는 `pnpm typecheck` 를 통과합니다.
- `if (error) throw` 뒤에서는 `data` 가 `undefined` 가 아닌 것으로 좁혀집니다. 이 줄을 지우면 `data.map` 에서 `'data' is possibly 'undefined'` 가 납니다.
- `returnedAt` 은 `null` 검사를 해야 `.slice` 를 쓸 수 있습니다.

</details>

**(2) 아래 7개 중 `pnpm typecheck` 가 막는 것을 고르세요.** (`api` 와 `Loan` 은 위와 같습니다.)

| # | 코드 | 막힐까? |
|---|------|------|
| a | `(loan: Loan) => loan.returnedAt.slice(0, 10)` | |
| b | `api.POST("/loans", { body: { memberId: 1 } })` | |
| c | `api.GET("/book")` | |
| d | `api.GET("/members/{id}/loans")` | |
| e | `const { data } = await api.GET("/books"); return data.length;` | |
| f | `(loan: Loan) => loan.returnedAt === null ? "대출 중" : loan.returnedAt.slice(0, 10)` | |
| g | `api.POST("/loans", { body: { memberId: "1", bookId: 2 } })` | |

<details>
<summary>정답</summary>

f 만 통과하고 나머지 6개는 모두 막힙니다. 실제 메시지입니다.

```text
a  error TS18047: 'loan.returnedAt' is possibly 'null'.
b  error TS2322: Type '{ memberId: number; }' is not assignable to type '{ memberId: number; bookId: number; } & {}'.
   Property 'bookId' is missing in type '{ memberId: number; }' but required in type '{ memberId: number; bookId: number; }'.
c  error TS2554: Expected 2 arguments, but got 1.
d  error TS2554: Expected 2 arguments, but got 1.
e  error TS18048: 'data' is possibly 'undefined'.
g  error TS2322: Type 'string' is not assignable to type 'number'.
```

- c 는 경로 오타입니다. 존재하지 않는 경로를 부르면 막히지만 메시지가 "인자 개수" 로 나와서 원인을 짐작하기 어렵습니다. 경로 문자열을 먼저 의심하세요.
- d 는 경로 파라미터 `{id}` 를 빼먹은 경우입니다. 같은 메시지가 나옵니다.
- e 는 요청이 실패했을 때 `data` 가 없기 때문입니다. 응답을 쓰기 전에 `error` 를 처리하게 만드는 것이 이 타입의 의도입니다.

</details>

---

## 1. 계약이란 무엇인가

백엔드와 프론트는 서로의 코드를 보지 않습니다. 둘 사이에는 **API 명세**라는 약속이 있습니다. 약속을 어디에 적느냐가 협업의 품질을 정합니다.

| | 사람에게 받는 명세 | 계약 기반 개발 |
|---|---|---|
| 형태 | 위키, 메신저, Swagger UI 화면을 눈으로 읽기 | 기계가 읽는 파일(`openapi.json`) |
| 프론트의 타입 | 응답을 보고 손으로 `interface` 를 씀 | 스펙에서 **생성** (`schema.d.ts`) |
| 백엔드가 필드를 바꾸면 | 화면이 깨지고 나서 알게 됨 | `pnpm typecheck` 가 깨지는 곳을 목록으로 알려 줌 |
| 틀리는 곳 | 손으로 옮기다 틀림, 갱신을 놓침 | 스펙 자체가 틀렸을 때만 |

Swagger UI(`/swagger-ui.html`)는 **같은 스펙을 사람이 눌러 보기 좋게 그린 화면**입니다. 서버를 켜고 브라우저로 열면 `/swagger-ui/index.html` 로 넘어갑니다. 화면을 읽고 타입을 손으로 옮기는 순간 계약이 아니라 "구전" 이 됩니다. 계약 기반 개발은 화면이 아니라 **파일을 원본으로 삼는 것**입니다.

이번 주차의 전체 흐름입니다.

```text
Java (컨트롤러 + DTO + @ApiResponse)
   |  springdoc-openapi
   v
GET /v3/api-docs  ----curl | jq---->  ../labs/backend/week25/openapi.json
                                          |  pnpm gen:api (openapi-typescript)
                                          v
                              ../labs/frontend/src/api/schema.d.ts   (생성 파일, 손대지 않음)
                                          |  openapi-fetch
                                          v
                        client.GET("/books", ...)  -> 경로·본문·응답이 모두 타입 검사됨
```

**TS 와 비교** (백엔드 용어를 프론트의 익숙한 것에 대응시킨 표입니다. 완전히 같지는 않습니다.)

| 백엔드(Java·Spring) 쪽 용어 | 프론트에서 만나는 모습 |
|---|---|
| `record BookResponse(...)` (응답 DTO) | `interface` / `type`. 이 저장소에서는 `components["schemas"]["BookResponse"]` |
| `@NotBlank` | zod 의 `.min(1)`. 스펙에는 `minLength: 1` 로 나가지만 TS 타입은 `string` 그대로 |
| `@Schema(requiredMode = REQUIRED)` | TS 의 `?` 를 지우는 것. 빠지면 `title?: string` |
| `Instant`, `long` | 각각 `string`(ISO 8601), `number`. `Date` 객체가 아님 |
| `@ApiResponse(responseCode = "409")` | 호출 결과의 `error` 쪽 타입에 실리는 오류 응답 선언 |
| springdoc 의 `/v3/api-docs` | GraphQL 의 `schema.graphql`, tRPC 의 라우터 타입 같은 "타입의 원본" |
| Swagger UI | Storybook 처럼 사람이 눌러 보는 카탈로그. 원본이 아니라 뷰 |

## 2. 백엔드: 스펙이 만들어지는 곳

의존성은 `../labs/backend/week25/build.gradle.kts` 한 줄입니다.

```kotlin
implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.17")
```

이 파일의 주석대로 springdoc 2.8.x 가 Spring Boot 3.5 용입니다. 컨트롤러에 별도 설정 없이도 경로·메서드·파라미터가 스펙에 나옵니다. **사람이 적어 줘야 하는 것**은 두 가지입니다.

**(1) 응답 코드와 오류 본문의 모양** — `BookController` 의 일부입니다.

```java
@PostMapping
@ApiResponse(responseCode = "201", description = "등록됨. Location 헤더에 /books/{id}")
@ApiResponse(responseCode = "400", description = "검증 실패", content = @Content(schema = @Schema(implementation = ApiError.class)))
@ApiResponse(responseCode = "401", description = "로그인 필요", content = @Content(schema = @Schema(implementation = ApiError.class)))
@ApiResponse(responseCode = "409", description = "isbn 중복", content = @Content(schema = @Schema(implementation = ApiError.class)))
public ResponseEntity<BookResponse> create(@Valid @RequestBody BookRequest req) { ... }
```

이 저장소는 오류 응답을 코드마다 직접 선언합니다. 스펙에 나온 응답 코드는 선언한 것뿐입니다. 예를 들어 선언이 없는 `GET /books` 는 `200` 하나만 갖습니다. "이 엔드포인트는 409 를 낸다" 는 선언이 있어야 프론트 타입에 409 가 생기고, 선언을 빼먹으면 계약에서 그 오류가 사라집니다.

**(2) 필수와 null** — `Dto.java` 의 일부입니다.

```java
public record BookResponse(
        @Schema(requiredMode = REQUIRED) long id,
        @Schema(requiredMode = REQUIRED) String isbn,
        @Schema(requiredMode = REQUIRED) String title,
        @Schema(requiredMode = REQUIRED) String author) { ... }

public record LoanResponse(
        ...
        @Schema(requiredMode = REQUIRED, types = {"string", "null"}, format = "date-time",
                description = "반납 전에는 null") Instant returnedAt) { ... }
```

`application.properties` 의 `springdoc.default-produces-media-type=application/json` 한 줄도 있습니다. 이것이 없으면 응답 content 키가 `*/*` 로 나와 생성된 타입이 지저분해진다고 주석에 적혀 있습니다.

## 3. 필수와 nullable 은 다른 문제

| 의미 | 스펙 | 생성된 TS | 이 저장소의 예 |
|---|---|---|---|
| 항상 있고 값이 있다 | `required` 에 포함 | `id: number` | `BookResponse.id` |
| 키는 항상 오고 값이 `null` 일 수 있다 | `required` 에 포함 + `type: ["string","null"]` | `returnedAt: string \| null` | `LoanResponse.returnedAt` |
| 키가 없을 수 있다 | `required` 에서 빠짐 | `keyword?: string` | `GET /books` 의 `keyword` |

세 가지는 호출하는 쪽의 코드가 다릅니다. `returnedAt` 은 `=== null` 로 검사하고, `keyword` 는 `undefined` 로 두면 되고, `id` 는 검사 없이 씁니다. 백엔드 `Loan` 엔티티의 "반납 전 `null`" 이 이 줄 하나로 프론트까지 전달됩니다.

두 가지를 알아 두세요.

- 스펙의 `format: int64`(`Long`)는 TS 에서 그냥 `number` 입니다. 이 도서관의 id 크기에서는 문제가 없지만, 아주 큰 `long` 은 JS 의 `number` 로 정확히 표현되지 않습니다.
- `operationId` 는 컨트롤러 메서드 이름에서 자동으로 만들어집니다. `BookController.create` 와 `MemberController.create` 가 겹치면 `create`, `create_1` 처럼 번호가 붙어 `schema.d.ts` 에 `operations["create_1"]` 이 생깁니다. 프론트는 이름이 아니라 경로로 호출하므로 영향이 없습니다.

## 4. 프론트: 스펙을 타입으로, 타입으로 호출

`../labs/frontend/package.json` 의 스크립트입니다.

```json
"gen:api": "openapi-typescript ../backend/week25/openapi.json -o src/api/schema.d.ts",
"typecheck": "tsc --noEmit"
```

- `pnpm gen:api` 는 **파일**을 읽습니다. 서버가 떠 있지 않아도 됩니다. 대신 `openapi.json` 이 최신이어야 합니다. 서버에서 직접 받고 싶으면 파일 대신 주소를 줘도 같은 결과가 나옵니다. (서버가 켜진 상태에서 `pnpm -C labs/frontend exec openapi-typescript http://localhost:8080/v3/api-docs -o $SPECS/from-server.d.ts` 를 실행하면 결과가 `schema.d.ts` 와 같습니다. `$SPECS` 는 2단계에서 만든 임시 폴더이고, `diff $SPECS/from-server.d.ts labs/frontend/src/api/schema.d.ts` 로 비교했습니다.)
- `src/api/schema.d.ts` 는 생성 파일입니다. 직접 고치지 않습니다.
- `openapi.json` 은 백엔드 쪽이 코드를 바꿀 때 서버가 켜진 상태에서 이렇게 갱신해 함께 커밋하는 파일입니다. 출력이 현재 파일과 같게 만드는 `jq .` 까지 포함합니다. 지금 저장소의 파일은 이미 서버가 내놓는 스펙과 같으므로 실행할 필요가 없습니다.

```bash
curl -s localhost:8080/v3/api-docs | jq . > labs/backend/week25/openapi.json
```

호출은 `src/api/client.ts` 의 클라이언트가 합니다.

```ts
export type Book = components["schemas"]["BookResponse"];
...
export function createApiClient(baseUrl: string) {
  return createClient<paths>({ baseUrl, credentials: "include" });
}
```

`createClient<paths>` 에 생성된 `paths` 타입을 넘기는 것이 전부입니다. 이후 `client.GET("/books", ...)` 의 경로 문자열, `params`, `body`, 응답이 모두 `schema.d.ts` 에서 추론됩니다. 화면이 쓰는 호출은 `src/api/library.ts` 에 모여 있습니다.

```ts
listBooks: (keyword?: string) =>
  unwrap(client.GET("/books", { params: { query: { keyword: keyword || undefined } } })),
lend: (memberId: number, bookId: number) => unwrap(client.POST("/loans", { body: { memberId, bookId } })),
giveBack: (loanId: number) =>
  unwrap(client.POST("/loans/{id}/return", { params: { path: { id: loanId } } })),
```

MSW 가짜 서버(`src/mocks/handlers.ts`)도 같은 타입을 씁니다. `Book`, `Loan`, `components["schemas"]["ApiError"]` 를 가져다 쓰므로, 스펙이 바뀌면 가짜 서버도 타입 오류로 알려 줍니다. 실험 A 의 오류 11개 중 4개가 여기서 나왔습니다. (`unwrap` 과 `ApiError` 는 7주에서, MSW 는 9주에서 다룹니다.)

## 5. 스펙 드리프트를 CI 에서 잡기

"드리프트" 는 백엔드 코드, 커밋된 스펙, 생성된 타입, 프론트 코드가 서로 어긋나는 일입니다. 어긋남이 생기는 지점이 세 곳이므로 검사도 세 단계입니다.

| 어긋남 | 잡는 검사 | 어디서 |
|---|---|---|
| 백엔드 코드가 바뀌었는데 `openapi.json` 은 그대로 | 서버가 지금 내놓는 스펙과 파일을 비교 (아래 명령) | 백엔드 CI |
| `openapi.json` 은 바뀌었는데 `schema.d.ts` 는 그대로 | `pnpm gen:api` 후 생성 파일에 변경이 생기는지 검사 | 프론트 CI |
| 타입이 바뀌었는데 호출하는 코드는 그대로 | `pnpm typecheck` | 프론트 CI |

1. 서버를 켠 상태에서, 스펙이 파일과 같은지 비교합니다. (저장소 루트에서. 같으면 `MATCH`)

   ```bash
   curl -s localhost:8080/v3/api-docs | jq . | diff - labs/backend/week25/openapi.json && echo MATCH
   ```

2. 프론트에서는 **한 줄**입니다.

   ```bash
   pnpm -C labs/frontend gen:api && pnpm -C labs/frontend typecheck
   ```

   `gen:api` 가 실패하면 스펙이 잘못된 것이고, `typecheck` 가 실패하면 호출하는 코드가 계약을 못 따라간 것입니다. 실험 A 에서 직접 본 상황입니다.

3. `schema.d.ts` 를 커밋하는 저장소라면 `gen:api` 뒤에 `git diff --exit-code labs/frontend/src/api/schema.d.ts` 를 붙여 "생성 파일이 최신인가" 도 검사할 수 있습니다. 변경이 없으면 종료 코드가 0, 있으면 1 이라서 CI 가 실패로 처리합니다. (임시 git 저장소에서 이 종료 코드를 확인했습니다. 이 저장소 자체는 git 저장소가 아닙니다.)

**`pnpm test` 만으로는 부족합니다.** 실험 A 의 상태에서도 Vitest 의 `client.test.ts` 18개와 `query-design.test.ts` 12개는 모두 통과했습니다. MSW 가짜 서버가 런타임에는 여전히 `title` 을 돌려주기 때문입니다. Vitest 는 타입을 검사하지 않으므로(프론트 README 의 "알려진 한계"), CI 에는 `pnpm typecheck` 가 따로 있어야 합니다.

> 이 저장소에는 CI 설정 파일(GitHub Actions 등)이 없습니다. 위 명령들은 CI 에 넣을 내용이지, 이 저장소에 이미 연결된 파이프라인이 아닙니다.

## 6. 한계: 계약이 보증하지 않는 것

- **타입은 컴파일 시점에만 존재합니다.** 서버가 스펙과 다른 응답을 내면(스펙 갱신을 잊은 경우 등) 런타임에는 아무도 모릅니다. 위 5절의 1번 검사가 그 틈을 메우는 용도입니다.
- **스펙이 틀리면 타입도 틀립니다.** 실험 B 에서 봤듯이 필수 표시나 `null` 선언이 빠지면 오히려 거짓 안심을 줍니다. 스펙을 코드에서 만드는 만큼, DTO 의 `@Schema` 를 리뷰 대상에 넣어야 합니다.
- **`@NotBlank` 같은 값 규칙은 타입에 안 옵니다.** `minLength: 1` 은 스펙에 있지만 `string` 으로만 번역됩니다.
- **스펙 갱신은 수동입니다.** 이 저장소에서는 `curl ... | jq .` 로 파일을 직접 갱신합니다. 백엔드 빌드가 `openapi.json` 을 자동으로 생성해 커밋하는 설정은 구현하지 않았습니다.

## 연습문제

1. `GET /books/{id}` 를 부르는 함수를 `library.ts` 와 같은 스타일로 노트에 쓰세요. 임시 파일에서 `pnpm typecheck` 로 확인합니다. (`unwrap` 과 `api` 를 `@/api/client` 에서 가져옵니다.)
2. 백엔드 PR 에서 `LoanResponse` 의 `returnedAt` 에 붙은 `types = {"string", "null"}` 를 지우는 변경을 봤다고 가정하세요. 실험 B 의 결과를 근거로, 이 PR 에 남길 리뷰 코멘트를 두세 문장으로 쓰세요. (코멘트에는 "무엇이 바뀌고, 프론트의 어떤 타입이 어떻게 달라지며, 왜 `typecheck` 로는 잡히지 않는지"가 들어가야 합니다.)
3. `LoanResponse` 에 `dueAt`(대출 기한, `Instant`) 필드를 추가한다고 가정하세요. 백엔드와 프론트에서 각각 어떤 파일의 어떤 줄을 건드려야 하나요? 실험 A 의 순서(스펙 갱신 → `gen:api` → `typecheck`)로 설명하세요. 새 필드는 기존 화면의 `typecheck` 를 깨뜨릴까요, 아닐까요? 스펙 사본에 `dueAt` 을 필수로 넣을 때와 필수에서 뺄 때를 `jq` 로 각각 만들어 확인해 보세요.
4. `pnpm gen:api` 대신 서버 주소를 직접 주는 방법과 파일을 읽는 방법의 장단점을 CI 관점에서 비교하세요. 어느 쪽이 CI 에서 더 재현 가능한가요?

<details>
<summary>3번 확인 결과</summary>

스펙 사본의 `LoanResponse` 에 `dueAt` 을 `{"type":"string","format":"date-time"}` 으로 넣고 `required` 에도 추가하면, `schema.d.ts` 에 `dueAt: string;` 이 생기고 `pnpm typecheck` 는 오류 1개를 냅니다.

```text
src/mocks/handlers.ts(127,11): error TS2741: Property 'dueAt' is missing in type '{ id: number; book: { id: number; isbn: string; title: string; author: string; }; member: { id: number; name: string; }; loanedAt: string; returnedAt: null; }' but required in type '{ id: number; book: { id: number; isbn: string; title: string; author: string; }; member: { id: number; name: string; }; loanedAt: string; returnedAt: string | null; dueAt: string; }'.
```

화면 컴포넌트는 `dueAt` 을 읽지 않으므로 깨지지 않고, `Loan` 객체를 **직접 만들어 내는** MSW 가짜 서버만 깨집니다. 응답을 읽기만 하는 코드에는 필드 추가가 안전하고, 그 타입의 값을 만들어 내는 코드에는 안전하지 않습니다. `required` 에 넣지 않으면(`dueAt?: string`) 오류는 0개입니다.

</details>

## 퀴즈

1. Swagger UI 와 `openapi.json` 중 "원본" 은 어느 쪽인가?
2. `returnedAt: string | null` 과 `keyword?: string` 은 무엇이 다른가?
3. 이 저장소처럼 오류 응답을 `@ApiResponse` 로 선언하는 방식에서, 409 선언을 빼먹으면 프론트에서 무슨 일이 생기나?
4. 스펙의 `title` 이 `name` 으로 바뀌었는데 `pnpm test` 는 통과하고 `pnpm typecheck` 만 실패했다. 왜인가?
5. `pnpm gen:api && pnpm typecheck` 는 "백엔드 코드는 바뀌었는데 `openapi.json` 은 안 바뀐" 경우를 잡는가?

<details>
<summary>정답</summary>

1. `openapi.json`(서버의 `/v3/api-docs`). Swagger UI 는 그 파일을 사람이 읽기 좋게 그린 화면이다
2. `returnedAt` 은 키는 항상 오고 값이 `null` 일 수 있다. `keyword` 는 키 자체가 없을 수 있다(생략 가능). 호출하는 쪽 검사 방법이 다르다(`=== null` 과 `undefined`)
3. 스펙에서 409 응답이 빠진다. 서버는 여전히 409 를 내지만, 스펙을 읽는 프론트는 그 응답이 있다는 것을 타입으로 알 수 없다
4. Vitest 는 타입을 검사하지 않고, MSW 가짜 서버가 런타임에 여전히 `title` 을 돌려주기 때문이다. 타입 불일치는 `tsc --noEmit` 만 잡는다
5. 잡지 못한다. `gen:api` 는 커밋된 `openapi.json` 파일을 읽는다. 서버가 내놓는 스펙과 파일을 비교하는 `curl ... | jq . | diff - ...` 가 따로 필요하다

</details>

## 체크리스트

- [ ] 1단계의 6개 질문에 먼저 답을 적었고, 틀린 것의 이유를 적었다
- [ ] 실험 A 에서 `typecheck` 가 깨지는 파일 목록을 보고, `gen:api` 로 원복했다
- [ ] 실험 B 로 `title?` 와 `returnedAt: string` 이 왜 위험한지 설명할 수 있다
- [ ] "반드시 있는 필드", "`null` 일 수 있는 필드", "없을 수 있는 필드" 를 스펙과 TS 타입에서 구분해서 읽을 수 있다
- [ ] 생성 타입만으로 호출을 써 보고(3단계), 막히는 호출 6개의 이유를 말할 수 있다
- [ ] 드리프트를 잡는 세 단계 검사가 각각 무엇을 지키는지 안다
