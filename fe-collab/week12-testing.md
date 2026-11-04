# 12주차. 테스트 전략: 목, 계약 테스트, e2e

**예상 시간:** 6~8시간 (하루 1시간 세션 7회) | **목표:** 프론트가 API 연동을 검증하는 테스트를 층으로 나누고, 어떤 고장을 어느 층이 잡고 어느 층이 놓치는지 직접 고장을 내서 확인한다. MSW 목이 스펙·실서버와 어긋나는 것을 계약 테스트로 잡고, Playwright 로 브라우저 + Next + 백엔드를 한 번에 검증한다.

> 이 주차의 전제입니다. 7주의 `ApiError` 규약과 "가드를 지워도 통과하는 테스트" 질문, 9주의 MSW 목과 "목은 내 기억일 뿐이다" 라는 위험, 10주의 `query-design.test.ts`, 11주의 서버 경계(서버 컴포넌트는 단위 테스트로 닿기 어렵다)를 알고 있어야 합니다. 실습 대상은 같은 참조 API `../labs/backend/week25` 와 클라이언트 `../labs/frontend` 이며, 백엔드는 팀이 준 API 로 보고 호출만 합니다.
>
> 준비물: Node 22.12+, pnpm 10+, `curl`, `jq`. 백엔드(8080)는 계약 테스트의 실서버 모드와 e2e 에서만 필요하고, `cd labs/backend && ./gradlew :week25:bootRun` 으로 미리 띄워 둡니다. Playwright 는 처음이면 `pnpm -C labs/frontend exec playwright install chromium` 을 한 번 실행합니다. 명령은 별도 언급이 없으면 저장소 루트에서 실행합니다. 이 주차에서 `labs/frontend` 의 파일을 몇 개 고치거나 추가하는데, 실험이 끝나면 모두 원래대로 되돌립니다.

## 하루 1시간으로 나누면

이 주차는 4단계가 모두 필수라서 하루 1시간씩 7번의 세션으로 나눕니다. 한 세션은 50~70분이고, 1.5시간짜리 3단계는 표의 고장 번호 1~3번과 4~6번으로 나눕니다.

| 세션 | 할 일 | 끝났을 때 할 수 있어야 하는 것 |
|---|---|---|
| 1 | 1단계(지금 있는 테스트를 읽고 층으로 나누기, 30분)와 2단계(계약 테스트와 서버 경계 e2e 파일 추가하기, 30분)를 하고, 1절(테스트의 층: 무엇이 무엇을 대체하는가)을 읽는다 | `pnpm test` 가 30개(18 + 12)인 것을 확인하고, `contract.test.ts` 와 `book-detail.spec.ts` 를 추가해 통과시킬 수 있다 |
| 2 | 2절(MSW 를 두 곳에 쓰기: 브라우저와 Node)과 3절(단위 테스트 읽기)을 읽고, 3단계(일부러 고장 내고, 어느 층이 잡는지 보기, 1.5시간)의 고장 1~3번(목이 잘못된 id 에 404, 중복 isbn 에 400, `loanedAt` 에 숫자)을 한다 | `vitest.setup.ts` 의 `onUnhandledFrame: "error"` 와 `resetMockDb()` 가 하는 일을 설명하고, 고장 1~3번을 예측한 뒤 세 층에서 확인하고 원복할 수 있다 |
| 3 | 3단계의 고장 4~6번(400 인데 폼 위 알림, Next 서버가 쿠키를 전달하지 않음, 409 를 입력칸 오류로 분류)을 하고 정답 표와 대조한다 | 6가지 고장을 어느 층이 잡고 놓치는지 표 없이 설명할 수 있다 |
| 4 | 4절(계약 테스트: 목이 스펙과 실서버를 따라가는지)을 읽으며 2단계에서 추가한 `contract.test.ts` 를 따라간다 | 계약 테스트가 목을 스펙과 실서버에 어떻게 대조하는지, 무엇을 계약으로 보고 무엇을 보지 않는지 말할 수 있다 |
| 5 | 4단계(스펙이 바뀌면 어느 층이 알려 주는가, 40분)를 하고, 5절(Playwright e2e)을 읽는다 | 필수 필드가 추가된 스펙을 `typecheck` 는 목 핸들러와 테스트 데이터로, 계약 테스트는 응답으로 지목하는 것을 확인하고, 같은 e2e 를 세 구성으로 돌려 `book-detail` 이 MSW 에서 실패하는 이유를 설명할 수 있다 |
| 6 | 연습문제와 퀴즈를 푼다 (압축 코스에서는 건너뜀) | "4권째 대출은 409" 를 목과 실서버 양쪽에서 통과하는 계약 테스트 시나리오로 추가할 수 있다 |
| 7 | 6절(무엇을 어느 층에서 테스트할까), TS 와 비교, 한계 절을 읽고, 체크리스트의 각 항목을 내 말로 설명해 보고, 막히는 항목은 해당 절을 다시 읽은 뒤 학습 노트를 정리한다 | 백엔드의 `LibraryServiceTest`, `LibraryApiTest`, `SecurityAndDocsTest` 가 프론트의 어느 층에 대응하는지 말하고, 내가 띄운 서버를 모두 종료할 수 있다 |

세션을 시작하기 전에 이전 세션의 마지막 결과를 1~2분 훑어봅니다. 막히면 아래 해당 개념 절을 사전처럼 펼쳐 보세요.

## 🖐 먼저 손으로 (읽기 전에 하세요)

> 이 주차의 핵심은 "이 고장을 어느 테스트가 잡을까" 를 먼저 예측하고 직접 확인하는 것입니다. 아래쪽 설명은 막혔을 때 사전처럼 펼쳐 보세요. 1단계에서 파일을 읽고, 2단계에서 파일 두 개를 추가하고, 3단계에서 고장을 냅니다.

### 1단계. 지금 있는 테스트를 읽고 층으로 나누기 (30분)

`labs/frontend/vitest.config.ts`, `labs/frontend/vitest.setup.ts`, `labs/frontend/playwright.config.ts` 세 파일을 열어 읽고, 아래 질문에 먼저 답을 적으세요. 실행은 그다음입니다.

| # | 질문 | 내 예측 |
|---|------|------|
| 1 | `pnpm -C labs/frontend test` 는 파일 몇 개, 테스트 몇 개를 통과시키는가? | |
| 2 | 8080 의 백엔드를 끄면 결과가 달라지는가? | |
| 3 | 이 테스트 중에 `BookForm` 같은 컴포넌트를 화면에 그리는 것이 있는가? | |
| 4 | `pnpm -C labs/frontend e2e` 가 알아서 켜 주는 서버는 무엇이고, 켜 주지 않는 서버는 무엇인가? | |
| 5 | 3000 에 이미 `pnpm dev` 가 떠 있는 채로 e2e 를 실행하면 어떻게 되는가? | |

```bash
pnpm -C labs/frontend test
pnpm -C labs/frontend exec vitest run src/api/client.test.ts
pnpm -C labs/frontend exec vitest run src/lib/query-design.test.ts
```

<details>
<summary>정답 (예측을 적은 뒤에 여세요)</summary>

```text
 Test Files  2 passed (2)
      Tests  30 passed (30)

      Tests  18 passed (18)      <- src/api/client.test.ts
      Tests  12 passed (12)      <- src/lib/query-design.test.ts
```

1. 파일 2개, 테스트 30개입니다. `client.test.ts` 18개와 `query-design.test.ts` 12개입니다.
2. 달라지지 않습니다. `vitest.setup.ts` 가 MSW node 서버를 켜고 `onUnhandledFrame: "error"` 로 핸들러가 없는 요청을 실패시키므로, 모든 요청은 핸들러가 가로채고 8080 으로 나가지 않습니다.
3. 없습니다. `vitest.config.ts` 의 `environment` 가 `"node"` 이고 `include` 가 `src/**/*.test.ts` 라서 `.tsx` 테스트도, 브라우저 DOM 도 없습니다. 이 테스트들은 호출 모음(`library`)과 `QueryClient` 를 직접 부릅니다.
4. `webServer` 가 켜는 것은 Next dev 서버(`pnpm dev`) 하나입니다. 백엔드는 켜 주지 않으므로 미리 띄워 둬야 합니다.
5. `reuseExistingServer: true` 라서 3000 의 서버를 그대로 씁니다. 그 서버가 MSW 모드로 떠 있으면 e2e 는 목을 상대로 돌아갑니다. 어느 서버를 상대로 하는지 늘 확인해야 합니다.

</details>

### 2단계. 계약 테스트와 서버 경계 e2e 파일 추가하기 (30분)

이 주차의 실험을 위해 파일 두 개를 추가합니다. 코드는 이 문서의 4절(`contract.test.ts`)과 5절(`book-detail.spec.ts`)에 있습니다. **코드 블록 전체를 그대로** 아래 위치에 저장하세요. 저장하기 전에 코드를 한 번 읽고, `problems` 함수가 무엇을 "어긋남" 으로 보는지 한 문장으로 적어 보세요.

| 파일 | 하는 일 |
|---|---|
| `labs/frontend/src/mocks/contract.test.ts` | 같은 시나리오를 보내고 응답이 `openapi.json` 과 맞는지 검사 |
| `labs/frontend/e2e/book-detail.spec.ts` | 로그인한 브라우저가 서버 컴포넌트 화면(`/books/{id}`)을 열 때 쿠키가 전달되는지 검사 |

```bash
pnpm -C labs/frontend test src/mocks/contract.test.ts                    # 목에 보냄
CONTRACT=real pnpm -C labs/frontend test src/mocks/contract.test.ts     # 실서버(8080)에 보냄
pnpm -C labs/frontend e2e                                                # 기존 2개 + 새 1개
```

```text
# 목
 Test Files  1 passed (1)
      Tests  1 passed (1)
# 실서버
 Test Files  1 passed (1)
      Tests  1 passed (1)
# e2e
  ✓  1 [chromium] › e2e/book-detail.spec.ts:5:1 › 책 상세: 로그인하면 서버가 쿠키를 전달하고, 없는 책은 404 화면
  ✓  2 [chromium] › e2e/book-registration.spec.ts:3:1 › 로그인 -> 책 등록 -> 같은 isbn 은 409 -> 로그아웃하면 쓰기 차단
  ✓  3 [chromium] › e2e/lending.spec.ts:3:1 › 대출: 없는 책은 404(notFound), 이미 대출 중이면 409(conflict), 반납하면 목록에서 사라진다
  3 passed
```

### 3단계. 일부러 고장 내고, 어느 층이 잡는지 보기 (1.5시간)

아래 여섯 가지를 **하나씩** 적용합니다. 적용 전에 세 열(단위 테스트 `pnpm test` 30개 / 계약 테스트(목) / e2e(실서버))에 "통과" 또는 "실패" 를 먼저 적으세요. 하나를 끝내면 원래대로 되돌리고 다음으로 넘어갑니다. 세 번째 열의 e2e 는 3개(기존 2개와 `book-detail`)이고 기본 모드(프록시 + 실서버)입니다.

| # | 고장 | 파일 | 바꾸는 곳 | 단위 | 계약 | e2e |
|---|------|------|-----------|------|------|-----|
| 1 | 목이 잘못된 id 에 404 를 준다 | `src/mocks/handlers.ts` | `const badId = () => error(400,` 를 `error(404,` 로 | | | |
| 2 | 목이 중복 isbn 에 400 을 준다 | `src/mocks/handlers.ts` | ``return error(409, `이미 등록된 isbn 입니다: ${isbn}`);`` 의 `409` 를 `400` 으로 | | | |
| 3 | 목이 `loanedAt` 에 문자열 대신 숫자를 준다 | `src/mocks/handlers.ts` | `loanedAt: new Date().toISOString(), returnedAt: null };` 를 `loanedAt: Date.now() as unknown as string, returnedAt: null };` 로 | | | |
| 4 | 400 인데도 폼 위 알림을 띄운다 | `src/components/BookForm.tsx` | `const showNotice = ...` 줄 전체를 `const showNotice = Boolean(create.error);` 로 | | | |
| 5 | Next 서버가 백엔드에 세션 쿠키를 전달하지 않는다 | `src/api/server.ts` | ``headers: session ? { cookie: `${session.name}=${session.value}` } : {},`` 를 `headers: {},` 로 | | | |
| 6 | 409 를 입력칸 오류로 분류한다 | `src/api/client.ts` | `409: "conflict",` 를 `409: "field",` 로 | | | |

시작 전에 원본을 백업해 두면 되돌리기 쉽습니다.

```bash
BACKUP=$(mktemp -d)
cp labs/frontend/src/mocks/handlers.ts labs/frontend/src/components/BookForm.tsx labs/frontend/src/api/server.ts labs/frontend/src/api/client.ts $BACKUP/
# 고장 1개를 적용한 뒤:
pnpm -C labs/frontend test src/api src/lib                              # 단위 30개
pnpm -C labs/frontend test src/mocks/contract.test.ts                   # 계약(목)
pnpm -C labs/frontend e2e                                               # e2e 3개 (백엔드 필요)
# 확인이 끝나면 원복 (파일 이름에 맞게):
cp $BACKUP/handlers.ts labs/frontend/src/mocks/handlers.ts
```

<details>
<summary>정답 (예측을 적은 뒤에 여세요)</summary>

고장 하나씩 적용하고 되돌리며 세 가지를 모두 실행한 결과입니다. 단위 테스트는 기존 30개, e2e 는 3개입니다.

| # | 단위 (30개) | 계약 (목) | e2e (실서버, 3개) |
|---|------|------|-----|
| 1 | 통과 | **실패** | 통과 |
| 2 | **1개 실패** (29 통과) | **실패** | 통과 |
| 3 | 통과 | **실패** | 통과 |
| 4 | 통과 | 통과 | **1개 실패** (2 통과) |
| 5 | 통과 | 통과 | **1개 실패** (2 통과) |
| 6 | **3개 실패** (27 통과) | 통과 | **2개 실패** (1 통과) |

읽는 법입니다.

- **1·3번은 단위 테스트가 못 잡습니다.** 단위 테스트가 부르는 시나리오에 "잘못된 id" 요청이 없고, `loanedAt` 의 값을 검사하지 않기 때문입니다. 계약 테스트는 스펙의 모든 엔드포인트를 부르고 본문의 모양까지 보므로 잡습니다. 3번은 `as unknown as string` 으로 타입 검사(`pnpm typecheck`)를 속인 경우입니다. 타입은 컴파일 시점에만 있고 실행 중 값은 모릅니다.
- **2번은 단위와 계약이 같이 잡습니다.** 이 규칙은 `client.test.ts` 가 이미 검사하고 있었습니다. 겹쳐서 잡는 것은 낭비가 아니라 안전망입니다.
- **1~3번은 e2e 가 통과합니다.** 기본 모드의 e2e 는 목이 아니라 실서버를 쓰므로, 목을 고쳐도 영향이 없습니다. 목이 틀린 것은 목을 쓰는 테스트(단위, 계약)에서만 드러납니다.
- **4번은 화면의 조합 오류입니다.** 컴포넌트를 그리는 테스트가 없어서 단위 테스트는 못 보고, e2e 의 `book-registration` 이 "400 일 때 폼 위 알림이 없다" 를 확인하다가 실패합니다.
- **5번은 서버 경계의 오류입니다.** 서버 컴포넌트가 쓰는 `next/headers` 의 `cookies()` 는 Next 서버 요청 안에서만 값이 있어서 단위 테스트로 부르기 어렵습니다(11주). 실제 브라우저로 로그인한 뒤 화면을 여는 e2e 만 "로그인하지 않은 요청입니다" 로 바뀐 것을 잡습니다.
- **6번은 단위와 e2e 가 모두 잡고, 계약은 못 잡습니다.** 상태 코드를 화면 종류로 바꾸는 규약은 서버가 아니라 프론트의 몫이라, 서버와의 계약을 보는 테스트와 무관합니다.

한 줄로: 하나의 테스트 층이 모든 고장을 잡지 못합니다. 층마다 **잡는 고장의 종류가 다릅니다.**

</details>

### 4단계. 스펙이 바뀌면 어느 층이 알려 주는가 (40분)

6주에서는 필드 이름을 바꾼 스펙을 생성해 `typecheck` 가 깨지는 것을 봤습니다. 이번에는 백엔드가 책 응답에 필수 필드 `status` 를 추가했다고 가정합니다(13주의 변경 요청서 예와 같은 변경입니다). 백엔드 파일은 건드리지 않고 스펙 **사본**을 만듭니다.

```bash
SPECS=$(mktemp -d)
jq '.components.schemas.BookResponse.properties.status = {"type":"string","enum":["AVAILABLE","ON_LOAN"]} | .components.schemas.BookResponse.required += ["status"]' \
  labs/backend/week25/openapi.json > $SPECS/status.json
```

먼저 예측하세요. (a) 이 스펙으로 타입을 다시 생성하면 `pnpm typecheck` 는 어느 파일을 지목하는가? 화면 컴포넌트가 깨지는가? (b) 그때 `pnpm test`(단위 30개)는? (c) 이 스펙을 읽는 계약 테스트는 목과 실서버에서 각각?

```bash
# (a), (b): 생성 타입만 바꿔서 확인 (끝나면 gen:api 로 원복)
pnpm -C labs/frontend exec openapi-typescript $SPECS/status.json -o src/api/schema.d.ts
pnpm -C labs/frontend typecheck
pnpm -C labs/frontend test src/api src/lib
pnpm -C labs/frontend gen:api && pnpm -C labs/frontend typecheck       # 원복, 오류 없이 끝나야 함

# (c): 계약 테스트에 새 스펙을 읽게 한다
OPENAPI=$SPECS/status.json pnpm -C labs/frontend test src/mocks/contract.test.ts
CONTRACT=real OPENAPI=$SPECS/status.json pnpm -C labs/frontend test src/mocks/contract.test.ts
```

<details>
<summary>정답 (예측을 적은 뒤에 여세요)</summary>

```text
src/lib/query-design.test.ts(140,39): error TS2741: Property 'status' is missing in type '{ id: number; isbn: string; title: string; author: string; }' but required in type ...
src/mocks/handlers.ts(21,5): error TS2741: Property 'status' is missing in type ...
src/mocks/handlers.ts(22,5): error TS2741: Property 'status' is missing in type ...
src/mocks/handlers.ts(89,11): error TS2741: Property 'status' is missing in type ...
```

- (a) 오류는 4곳이고 **전부 목 핸들러(`handlers.ts`)와 테스트 데이터** 입니다. 화면 컴포넌트는 `status` 를 읽지 않으므로 깨지지 않습니다. 응답에 필드를 추가하는 것은 소비자에게 호환되는 변경이지만, 그 필드를 **필수로 표시**하면 타입으로 흉내 낸 가짜 데이터가 깨집니다.
- (b) 단위 테스트는 30개 모두 통과합니다. `pnpm test` 는 타입을 검사하지 않기 때문입니다(그래서 CI 에는 `typecheck` 와 `test` 가 둘 다 필요합니다).
- (c) 계약 테스트는 목과 실서버 **둘 다 실패**합니다. 목은 `$.status: 필수인데 없음` 이 8곳(예: `POST /books -> 201`, `GET /books/{id} -> 200`, `POST /loans -> 201` 의 `$.book.status`)으로 나옵니다. 실서버에서도 같은 메시지가 나옵니다. 새 스펙을 아직 실서버가 지키지 않는다는 뜻이고, 실제 팀에서라면 "스펙은 갱신됐는데 서버는 아직 배포 전" 이라는 신호입니다. 목만 실패하고 실서버는 통과하는 경우(서버는 필드를 주는데 내 목이 안 줌)와 구별해서 읽어야 합니다.

</details>

---

## 1. 테스트의 층: 무엇이 무엇을 대체하는가

프론트에서 API 연동을 검증하는 테스트는 네 층으로 나눌 수 있습니다. 위로 갈수록 실제에 가깝고, 느리고, 깨지기 쉽습니다.

| 층 | 이 저장소의 예 | 서버 | 잡는 것 | 놓치는 것 |
|---|---|---|---|---|
| 순수 함수 | `uiKindOf`, `ApiError.fieldErrors` (`client.test.ts` 앞부분) | 없음 | 규칙 하나하나 | 연결 |
| 클라이언트 + 목 서버 | `client.test.ts` 18개, `query-design.test.ts` 12개 | MSW (Node) | 호출 모음이 4xx 를 `ApiError` 로 바꾸는지, 캐시가 의도대로 동작하는지 | 목이 틀린 경우, 화면 조합, 서버 경계 |
| 계약 테스트 | 4절의 `contract.test.ts` | MSW 또는 실서버 | 목·실서버 응답이 스펙(`openapi.json`)과 같은지 | 시나리오에 없는 요청 |
| e2e | `e2e/*.spec.ts` | 실서버 + 실제 브라우저 + Next | 화면까지 이어진 실제 흐름, 쿠키, 프록시, 서버 컴포넌트 | 느리고, 시나리오가 적고, 원인 위치를 말해 주지 않음 |

원칙은 세 가지입니다.

- **규칙은 가장 낮은 층에서 검사합니다.** "409 는 `conflict`" 는 순수 함수 테스트면 충분하고, 브라우저를 띄울 이유가 없습니다.
- **층이 올라갈수록 시나리오를 적게, 중요한 흐름만** 둡니다. 로그인 → 등록 → 중복 → 로그아웃 같은 대표 경로 몇 개입니다.
- **같은 고장이 여러 층에서 잡혀도 괜찮습니다.** 3단계의 2번처럼 겹치면 안전망이고, 어느 층도 못 잡는 고장이 문제입니다.

이 저장소에는 없는 층도 있습니다. 컴포넌트를 DOM 에 그리는 테스트(React Testing Library 등)는 의존성에 없고, `vitest.config.ts` 의 `environment` 도 `"node"` 입니다. 그래서 3단계의 4번(화면 조합 오류)을 e2e 만 잡았습니다. 컴포넌트 테스트를 추가할 수 있지만, 이 주차는 API 연동 쪽 층에 집중하므로 추가하지 않습니다.

## 2. MSW 를 두 곳에 쓰기: 브라우저와 Node

9주에서 MSW 를 브라우저(서비스 워커)로 켜 봤습니다. 같은 핸들러(`src/mocks/handlers.ts`)를 Vitest 도 Node 에서 씁니다.

| 쓰는 곳 | 파일 | 가로채는 방법 |
|---|---|---|
| 브라우저(`pnpm dev` + `NEXT_PUBLIC_API_MOCKING=enabled`) | `src/mocks/browser.ts` | 서비스 워커 `public/mockServiceWorker.js` |
| Vitest | `src/mocks/server.ts` | Node 의 `fetch` 를 가로채는 `setupServer` |

Vitest 설정은 두 파일입니다. 전부 그대로 옮깁니다.

```ts
import { fileURLToPath } from "node:url";
import { defineConfig } from "vitest/config";

export default defineConfig({
  resolve: { alias: { "@": fileURLToPath(new URL("./src", import.meta.url)) } },
  test: {
    environment: "node",
    include: ["src/**/*.test.ts"],
    setupFiles: ["./vitest.setup.ts"],
  },
});
```

```ts
import { afterAll, afterEach } from "vitest";
import { resetMockDb } from "@/mocks/handlers";
import { server } from "@/mocks/server";

// 핸들러가 없는 요청은 조용히 통과시키지 않고 실패시킨다(오타 난 경로를 바로 알 수 있게).
// openapi-fetch 는 createClient 시점의 fetch 를 붙잡으므로, 테스트 파일이 클라이언트를 만들기 전(여기, 최상위)에 가로채기를 켠다.
server.listen({ onUnhandledFrame: "error" });
afterEach(() => {
  server.resetHandlers();
  resetMockDb();
});
afterAll(() => server.close());
```

읽을 점입니다.

- `onUnhandledFrame: "error"`: 핸들러가 없는 요청은 조용히 실제 네트워크로 나가지 않고 테스트를 실패시킵니다. 경로를 잘못 쓰면 "네트워크 오류" 가 아니라 바로 실패로 드러납니다. 브라우저 쪽 `MockGate` 는 반대로 `"bypass"` 입니다(개발 중에는 Next 자체 요청이 지나가야 하기 때문입니다).
- `afterEach` 의 `resetMockDb()`: 테스트마다 목 DB 가 초기값(책 2권, 회원 1명, 로그아웃 상태)으로 돌아갑니다. 테스트가 서로의 상태에 기대지 않게 합니다.
- 주석처럼 `server.listen` 이 파일 최상위에 있는 이유는 `openapi-fetch` 가 `createClient` 시점의 `fetch` 를 붙잡기 때문입니다. 클라이언트를 만든 뒤에 가로채기를 켜면 가로채지 못합니다.
- 핸들러 경로가 `"*/books"` 처럼 앞이 와일드카드인 이유는 브라우저의 `/api/books` 와 테스트의 `http://localhost:8080/books` 를 모두 맞추기 위해서입니다.
- 목 핸들러의 반환 타입에는 스펙에서 생성한 타입이 붙어 있습니다(`HttpResponse.json<components["schemas"]["ApiError"]>`). 4단계에서 본 것처럼 필드의 모양은 타입이 지켜 줍니다. 타입이 못 지키는 것은 **상태 코드, 규칙, 실행 중의 값** 입니다.

## 3. 단위 테스트 읽기: 무엇이 무엇을 지키는가

`client.test.ts` 18개의 구성입니다.

| 묶음 | 개수 | 보는 것 |
|---|---|---|
| 상태 코드 → UI 종류 매핑 | 5 | `uiKindOf` 의 표 (400/401/404/409 와 규약 밖) |
| `ApiError.fieldErrors` | 2 | 400 메시지를 필드별로 나눔, 400 이 아니면 비어 있음 |
| 클라이언트가 4xx 를 `ApiError` 로 바꾼다 | 9 | 성공 응답, 검색, 401, 400, 409, 404, 4권째 대출, 로그아웃 |
| `{"message"}` 형식이 아닌 실패 | 2 | 프록시의 텍스트 502, 네트워크 오류(`TypeError`) |

`query-design.test.ts` 12개는 10주에서 읽었습니다: 캐시 키·무효화 2, 로딩 상태 2, `staleTime` 1, 낙관적 업데이트 2, 페이지네이션 4(참조 API 에는 없어 이 파일 안의 핸들러가 흉내), 재시도 1입니다.

테스트 한 개를 읽는 방법은 7주에서 배운 질문입니다. **"이 테스트가 지키는 규칙을 지우면 이 테스트가 실패하는가?"** 3단계의 2번과 6번에서 단위 테스트가 실패한 것은 그 규칙이 검사되고 있었기 때문이고, 1번과 3번에서 통과한 것은 검사되고 있지 않았기 때문입니다. 통과하는 테스트는 "아무 규칙도 깨지지 않았다" 가 아니라 "이 테스트가 보는 규칙은 깨지지 않았다" 입니다.

서버 컴포넌트와 `cookies()` 는 단위 테스트로 닿기 어렵고(11주), 브라우저 DOM 도 없습니다. 이 둘은 e2e 의 몫입니다(5절).

## 4. 계약 테스트: 목이 스펙과 실서버를 따라가는지

9주에서 "목은 내 기억일 뿐이다" 라고 했습니다. 목 기반 테스트가 모두 통과해도 목 자체가 틀렸다면 실서버에서 깨집니다. 이 틈을 줄이는 방법이 **계약 테스트** 입니다. 이 문서에서는 두 가지 어긋남을 한 파일로 봅니다.

| 비교 | 어긋나면 | 어떻게 |
|---|---|---|
| 스펙(`openapi.json`) ↔ 목의 응답 | 목이 낡았다 | 기본 모드: 목에 시나리오를 보내고 응답을 스펙과 대조 |
| 스펙 ↔ 실서버의 응답 (그리고 목과 같은 기대 상태 코드) | 서버가 스펙을 안 지킨다 또는 서버가 계약을 바꿨다 | `CONTRACT=real`: MSW 를 우회해 같은 시나리오를 8080 에 보냄 |

`pnpm gen:api` 와의 관계입니다. `gen:api` 는 `openapi.json` 에서 **타입** 을 만들고(6주), 계약 테스트는 같은 `openapi.json` 을 읽어 **실행 중의 응답** 을 검사합니다. 입력이 같은 파일이므로 스펙이 한 번 바뀌면 두 곳이 함께 반응합니다.

| | 생성 타입 (`gen:api` + `typecheck`) | 계약 테스트 |
|---|---|---|
| 검사 시점 | 컴파일 | 테스트 실행 |
| 필드 이름·필수 여부 | 잡음 (호출부와 타입으로 만든 목 데이터) | 잡음 (실제 응답 본문) |
| 상태 코드·시나리오 규칙 | **못 잡음** | 잡음 (시나리오가 기대한 상태 코드, 스펙이 선언한 상태 코드) |
| 타입을 속인 값 (`as`) | 못 잡음 | 잡음 |
| 스펙에 없는 필드·엔드포인트 | 못 잡음 | 잡음 |

아래가 3단계에서 추가한 `labs/frontend/src/mocks/contract.test.ts` 전체입니다.

```ts
import { readFileSync } from "node:fs";
import { http, passthrough } from "msw";
import { beforeEach, describe, expect, it } from "vitest";
import { server } from "./server";

// 계약 테스트: 같은 시나리오를 보내고, 받은 응답이 openapi.json(= pnpm gen:api 의 입력)과 맞는지 본다.
// 기본은 MSW 목에 보내고, CONTRACT=real 이면 MSW 를 우회해 실제 백엔드(8080)에 보낸다.
// OPENAPI=경로 로 다른 스펙 파일을 읽게 할 수 있다(스펙이 바뀐 상황을 흉내 낼 때).
const specPath = process.env.OPENAPI ?? new URL("../../../backend/week25/openapi.json", import.meta.url);
const spec = JSON.parse(readFileSync(specPath, "utf-8"));

type Schema = { $ref?: string; type?: string | string[]; properties?: Record<string, Schema>; required?: string[]; items?: Schema; format?: string };

const resolve = (s: Schema): Schema => (s.$ref ? resolve(spec.components.schemas[s.$ref.split("/").pop()!]) : s);

/** 값이 스키마와 어긋나는 곳을 문장으로 돌려준다. 스펙에 없는 필드도 어긋남으로 본다. */
function problems(schema: Schema, value: unknown, at = "$"): string[] {
  const s = resolve(schema);
  const actual = value === null ? "null" : Array.isArray(value) ? "array" : Number.isInteger(value) ? "integer" : typeof value;
  const allowed = [s.type ?? []].flat();
  if (allowed.length && !allowed.includes(actual)) return [`${at}: ${allowed.join("|")} 이어야 하는데 ${actual}`];
  if (s.format === "date-time" && typeof value === "string" && Number.isNaN(Date.parse(value))) return [`${at}: date-time 형식이 아님`];
  if (actual === "array") return (value as unknown[]).flatMap((item, i) => problems(s.items!, item, `${at}[${i}]`));
  if (actual !== "object") return [];
  const object = value as Record<string, unknown>;
  const known = s.properties ?? {};
  return [
    ...(s.required ?? []).filter((k) => !(k in object)).map((k) => `${at}.${k}: 필수인데 없음`),
    ...Object.keys(object).filter((k) => !(k in known)).map((k) => `${at}.${k}: 스펙에 없는 필드`),
    ...Object.keys(known).filter((k) => k in object).flatMap((k) => problems(known[k], object[k], `${at}.${k}`)),
  ];
}

// Node 의 fetch 는 쿠키를 저장하지 않으므로 JSESSIONID 를 직접 들고 다니는 fetch 를 쓴다.
let cookie = "";
const seen = new Set<string>();
const found: string[] = [];

/** 호출 하나. 기대한 상태 코드인지, 그 상태 코드와 본문이 스펙에 선언된 것인지 확인한다. */
async function hit(method: string, template: string, expected: number, options: { id?: unknown; body?: unknown } = {}) {
  const path = template.replace("{id}", String(options.id));
  const res = await fetch(`http://localhost:8080${path}`, {
    method,
    headers: { "content-type": "application/json", ...(cookie ? { cookie } : {}) },
    body: options.body === undefined ? undefined : JSON.stringify(options.body),
  });
  const set = res.headers.get("set-cookie");
  if (set) cookie = set.split(";")[0];
  const text = await res.text();
  const body = text ? JSON.parse(text) : undefined;

  const label = `${method} ${template}`;
  seen.add(label);
  if (res.status !== expected) found.push(`${label}: 기대 ${expected}, 실제 ${res.status}`);
  const declared = spec.paths[template]?.[method.toLowerCase()]?.responses?.[String(res.status)];
  if (!declared) {
    found.push(`${label}: 스펙에 선언되지 않은 상태 코드 ${res.status}`);
    return body;
  }
  const schema = declared.content?.["application/json"]?.schema;
  if (schema) found.push(...problems(schema, body).map((p) => `${label} -> ${res.status}: ${p}`));
  else if (body !== undefined) found.push(`${label} -> ${res.status}: 스펙은 본문이 없다는데 본문이 있음`);
  return body;
}

beforeEach(() => {
  cookie = "";
  seen.clear();
  found.length = 0;
  if (process.env.CONTRACT === "real") server.use(http.all("*", () => passthrough()));
});

describe("계약: 응답이 openapi.json 과 맞는다", () => {
  it("시나리오의 모든 응답이 스펙에 선언된 상태 코드와 모양이다", async () => {
    const isbn = `c-${Date.now()}`; // 실서버 DB 는 계속 남으므로 매번 새 isbn
    const NOBODY = 999999;

    await hit("GET", "/auth/me", 401);
    await hit("POST", "/books", 401, { body: { isbn, title: "t", author: "a" } });
    await hit("POST", "/auth/login", 400, { body: { username: "", password: "" } });
    await hit("POST", "/auth/login", 401, { body: { username: "demo", password: "nope" } });
    await hit("POST", "/auth/login", 200, { body: { username: "demo", password: "demo1234" } });
    await hit("GET", "/auth/me", 200);

    await hit("POST", "/books", 400, { body: { isbn: "", title: "t", author: "a" } });
    const book = await hit("POST", "/books", 201, { body: { isbn, title: "t", author: "a" } });
    await hit("POST", "/books", 409, { body: { isbn, title: "t", author: "a" } });
    await hit("GET", "/books", 200);
    await hit("GET", "/books/{id}", 200, { id: book.id });
    await hit("GET", "/books/{id}", 404, { id: NOBODY });
    await hit("GET", "/books/{id}", 400, { id: "abc" });

    await hit("POST", "/members", 400, { body: { name: "" } });
    const member = await hit("POST", "/members", 201, { body: { name: "계약" } });
    const loan = await hit("POST", "/loans", 201, { body: { memberId: member.id, bookId: book.id } });
    await hit("POST", "/loans", 409, { body: { memberId: member.id, bookId: book.id } });
    await hit("POST", "/loans", 404, { body: { memberId: member.id, bookId: NOBODY } });
    await hit("POST", "/loans", 400, { body: {} });
    await hit("GET", "/members/{id}/loans", 200, { id: member.id });
    await hit("GET", "/members/{id}/loans", 404, { id: NOBODY });
    await hit("POST", "/loans/{id}/return", 200, { id: loan.id });
    await hit("POST", "/loans/{id}/return", 409, { id: loan.id });
    await hit("POST", "/loans/{id}/return", 404, { id: NOBODY });

    await hit("POST", "/auth/logout", 204);
    await hit("POST", "/members", 401, { body: { name: "x" } });

    expect(found).toEqual([]);
    // 스펙에 엔드포인트가 새로 생겼는데 이 시나리오가 부르지 않으면 여기서 드러난다.
    const inSpec = Object.entries(spec.paths).flatMap(([path, ops]) => Object.keys(ops as object).map((m) => `${m.toUpperCase()} ${path}`));
    expect([...seen].sort()).toEqual(inSpec.sort());
  });
});
```

설계 포인트입니다.

- **무엇이 계약이고 무엇이 아닌가를 정하는 것이 이 테스트의 대부분입니다.** 이 파일은 상태 코드, 본문의 모양(필수 필드, 타입, `date-time`, 스펙에 없는 필드)만 비교하고 **메시지 문구는 비교하지 않습니다.** 문구는 `Accept-Language` 에 따라 달라지기 때문입니다. 아래 확인을 보세요.
- `hit` 은 호출마다 **기대 상태 코드** 를 받습니다. 스펙에 선언된 코드이기만 하면 통과하는 검사만으로는 "중복 isbn 이 409 가 아니라 400 이 되는" 변경을 못 잡기 때문입니다(스펙에는 `POST /books` 의 400 도 선언돼 있습니다). 3단계의 1번과 2번이 이 줄에서 걸립니다.
- 마지막 `expect` 는 스펙의 모든 엔드포인트를 시나리오가 한 번 이상 불렀는지 확인합니다. 스펙에 엔드포인트가 새로 생기면 이 테스트가 실패해서, 목과 시나리오에 추가하라고 알려 줍니다.
- 실서버 DB 는 계속 남으므로 `isbn` 에 `Date.now()` 를 섞고, 없는 id 는 `999999` 를 씁니다. 이 파일은 실서버에 책 1권, 회원 1명, 대출 1건을 남깁니다.
- Node 의 `fetch` 는 브라우저와 달리 쿠키를 저장하지 않습니다. 그래서 `Set-Cookie` 를 직접 들고 다니는 변수 `cookie` 를 둡니다.
- `problems` 는 20줄 안팎의 간이 검사기입니다. `$ref`, `type`, `required`, `properties`, `items`, `date-time` 만 이해합니다. `enum`, `minLength` 같은 키워드는 검사하지 않으므로 필요하면 연습문제 2번처럼 늘립니다. 간이 검사기를 쓴 이유는 의존성을 늘리지 않기 위해서이고, 스펙이 커지면 검증 라이브러리를 쓰는 편이 낫습니다(개념 설명이며 이 저장소에서 실행하지 않았습니다).

문구가 로케일에 따라 달라지는 것은 이렇게 확인합니다. 백엔드를 띄운 상태에서 같은 요청에 `Accept-Language` 만 바꿔 보냅니다.

```bash
J='Content-Type: application/json'; JAR=$(mktemp)
curl -s -c $JAR -o /dev/null -X POST localhost:8080/auth/login -H "$J" -d '{"username":"demo","password":"demo1234"}'
for l in ko-KR en-US; do curl -s -b $JAR -H "Accept-Language: $l" -X POST localhost:8080/books -H "$J" -d '{"isbn":"","title":"t","author":"a"}'; echo; done
```

```text
{"message":"isbn: 공백일 수 없습니다"}
{"message":"isbn: must not be blank"}
```

`Accept-Language` 를 아예 안 보내면 서버 JVM 의 기본 로케일을 따릅니다. 목은 영어로 고정돼 있으므로, 문구까지 비교하는 계약 테스트는 목과 실서버 사이에서 항상 실패하게 됩니다.

백엔드 쪽에도 짝이 되는 테스트가 있습니다. `SecurityAndDocsTest.openApiSpec_documentsErrorCodes` 는 서버가 내놓는 `/v3/api-docs` 에 `POST /loans` 의 201·400·401·404·409 같은 상태 코드가 선언돼 있는지 확인합니다. 서버는 "스펙이 거짓말하지 않는다" 를, 프론트는 "내 목과 화면이 스펙을 따른다" 를 각자 검사하는 구조입니다. 여러 소비자가 있는 팀에서는 소비자가 기대하는 계약을 서버 쪽 빌드에서 검증하는 방식(소비자 주도 계약 테스트)도 쓰이지만, 이 문서에서는 다루지 않습니다.

## 5. Playwright e2e

단위 테스트는 **화면 코드 + 가짜 서버**를 검사하고, e2e 는 **브라우저 + Next + 프록시 + 실제 Spring + 쿠키** 를 한 번에 검사합니다. 설정 파일은 이렇습니다.

```ts
import { defineConfig } from "@playwright/test";

// 백엔드(./gradlew :week25:bootRun, 8080)는 미리 띄워 둔다. 여기서는 Next dev 서버만 기동한다.
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

- `webServer` 는 Next dev 서버만 켭니다. 백엔드는 프론트 저장소가 소유하지 않는 별도 시스템이므로, 어떤 버전·데이터의 백엔드에 붙었는지를 사람이 정하게 둔 것입니다.
- `reuseExistingServer: true` 라서 3000 에 이미 서버가 있으면 그것을 씁니다. MSW 모드로 띄운 서버가 남아 있으면 e2e 가 목을 상대로 돕니다.
- `workers: 1`: 테스트들이 같은 백엔드 DB(H2 메모리)를 공유하므로 병렬로 돌리지 않습니다.

테스트가 단단한 이유를 `e2e/book-registration.spec.ts` 에서 읽습니다.

- **책마다 새 `isbn`**: `` `e2e-${Date.now()}` ``. 백엔드 DB 가 실행 중 계속 남으므로 반복 실행해도 충돌하지 않습니다.
- **역할·레이블 선택자**: `getByRole("button", { name: "책 등록" })`, `getByLabel("아이디")`. CSS 클래스가 바뀌어도 깨지지 않고, 접근성 이름을 검사하는 효과도 있습니다.
- **오류의 종류를 문구가 아니라 `data-kind` 로 검증**: `ErrorNotice` 가 `data-kind="conflict"` 같은 속성을 달기 때문에 "409 는 폼 위 알림, 400 은 입력칸 옆" 구분을 문구에 기대지 않고 확인합니다. `[role="alert"][data-kind]` 로 고르는 이유는 Next 자체의 라우트 안내(`role="alert"`)와 겹치지 않게 하기 위해서입니다.
- 기다림은 `expect(...).toBeVisible()` 처럼 조건을 기다리는 단언으로 합니다. 고정 시간 `waitForTimeout` 은 느리고 흔들립니다.

서버 컴포넌트는 브라우저가 보낸 쿠키를 Next 서버가 백엔드로 전달해야 로그인 상태를 압니다(11주). 이것을 검사하는 e2e 가 3단계의 `book-detail.spec.ts` 입니다.

```ts
import { expect, test } from "@playwright/test";

// 서버 컴포넌트(books/[id])는 브라우저가 보낸 JSESSIONID 를 Next 서버가 백엔드에 실어 준다.
// 이 경로는 단위 테스트(MSW)로 닿지 않으므로 e2e 로 확인한다. 백엔드가 8080 에 떠 있어야 한다.
test("책 상세: 로그인하면 서버가 쿠키를 전달하고, 없는 책은 404 화면", async ({ page }) => {
  const isbn = `e2e-detail-${Date.now()}`;

  await page.goto("/");
  await page.getByLabel("아이디").fill("demo");
  await page.getByLabel("비밀번호").fill("demo1234");
  await page.getByRole("button", { name: "로그인", exact: true }).click();
  await expect(page.getByTestId("current-user")).toBeVisible();

  await page.getByLabel("isbn", { exact: true }).fill(isbn);
  await page.getByLabel("제목", { exact: true }).fill("상세용 책");
  await page.getByLabel("저자", { exact: true }).fill("테스터");
  await page.getByRole("button", { name: "책 등록" }).click();
  const bookId = (await page.getByText(/등록했습니다: 상세용 책/).textContent())!.match(/#(\d+)/)![1];

  await page.goto(`/books/${bookId}`);
  await expect(page.getByRole("heading", { name: "상세용 책" })).toBeVisible();
  await expect(page.getByTestId("viewer")).toHaveText("demo 님으로 로그인한 요청입니다");

  const missing = await page.goto("/books/999999");
  expect(missing?.status()).toBe(404);
  await expect(page.locator('[role="alert"][data-kind]')).toHaveAttribute("data-kind", "notFound");
});
```

이 파일은 로그인하고 책을 등록한 뒤 `/books/{id}` 를 열어 서버가 그린 문구가 "demo 님으로 로그인한 요청입니다" 인지 봅니다. 마지막에는 없는 책의 상태 코드가 404 이고 화면이 `notFound` 종류인지 확인합니다. 3단계의 5번처럼 쿠키 전달을 끊으면 "로그인하지 않은 요청입니다" 가 나와서 실패합니다.

같은 e2e 를 세 구성으로 실행해 봤습니다.

| 실행 | 명령 | 결과 |
|---|---|---|
| 프록시 + 실서버 (기본) | `pnpm -C labs/frontend e2e` | 3개 통과 |
| 직접 호출 + CORS + 실서버 | `NEXT_PUBLIC_API_BASE=http://localhost:8080 pnpm -C labs/frontend e2e` | 기존 2개 통과, `book-detail` 통과 |
| MSW | `NEXT_PUBLIC_API_MOCKING=enabled pnpm -C labs/frontend e2e` | 기존 2개 통과, `book-detail` **실패** |

(MSW 와 직접 호출 모드는 백엔드가 떠 있는 상태에서 실행했습니다. 백엔드를 끄고도 MSW 모드가 도는지는 연습문제 3번에서 직접 확인합니다.)

기존 두 e2e 는 세 구성 모두에서 통과합니다. **화면이 서버 구현(프록시, 직접 호출, 목)에 의존하지 않는다** 는 뜻이고, 목이 화면이 필요로 하는 흐름만큼은 실서버처럼 행동한다는 증거이기도 합니다. 반면 `book-detail` 이 MSW 모드에서 실패하는 것은 정상입니다. 서버 컴포넌트는 브라우저가 아니라 Next 서버가 백엔드를 직접 부르므로(11주) 브라우저의 서비스 워커가 가로채지 못하고, 목이 만든 책은 그 백엔드에 없기 때문입니다. 서버 경계를 검사하는 e2e 는 실서버 구성으로만 돌립니다.

## 6. 무엇을 어느 층에서 테스트할까

| 검증하려는 것 | 알맞은 층 | 이유 |
|---|---|---|
| 상태 코드 → 화면 종류 규칙, 파싱 | 순수 함수 | 가장 싸고, 실패 원인이 한 줄 |
| 호출 모음이 4xx 를 `ApiError` 로 바꾸는지 | 클라이언트 + MSW | HTTP 경계만 가짜 |
| 캐시 키·무효화·낙관적 업데이트의 동작 | `QueryClient` 직접 (10주) | 화면 없이 확인 |
| 목이 스펙·실서버와 같은지 | 계약 테스트 | 목은 기억일 뿐이므로 |
| 400 은 입력칸, 409 는 알림 같은 화면 조합 | e2e | 컴포넌트 테스트가 없음 |
| 쿠키, 프록시, CORS, 서버 컴포넌트 | e2e (실서버) | 목이 대신할 수 없음 |
| 스펙 변경의 영향 범위 | `typecheck` + 계약 테스트 | 4단계 |

백엔드 팀의 테스트와 나란히 놓으면 층의 모양이 같습니다. 아래 "TS 와 비교" 표를 보세요.

## TS 와 비교

백엔드 테스트는 이 주차에서 실행하지 않았고, `labs/backend/week25/src/test/java/library/` 의 클래스 주석과 `@Test` 이름을 읽은 것입니다. `@Test` 개수는 `grep -c '@Test'` 로 센 값입니다.

| 백엔드(Spring) 쪽 | 프론트 쪽에서 닮은 것 |
|---|---|
| `LibraryServiceTest` (6개): "Spring 없이: 가짜 리포지토리와 고정 시계를 `new` 로 넣어 규칙만 검사" | 순수 함수 테스트. 네트워크와 시간을 밖에서 넣어 규칙만 본다 |
| `LibraryApiTest` (14개): `@SpringBootTest` + `MockMvc`, 엔드포인트·상태 코드·오류 본문 | Vitest + MSW. HTTP 경계만 가짜로 두고 호출 모음을 검사 |
| `LibraryRepositoryTest` (4개): `@DataJpaTest` | 대응하는 층이 없다. 프론트에는 DB 가 없고, 라이브러리 동작 확인은 `query-design.test.ts` 가 가장 비슷 |
| `SecurityAndDocsTest` (7개): `RANDOM_PORT` 로 실제 톰캣을 띄워 쿠키 속성·CORS·스펙을 확인 | Playwright e2e. 실제 서버와 실제 브라우저 |
| `openApiSpec_documentsErrorCodes`: 서버가 스펙에 상태 코드를 선언했는지 | 계약 테스트: 내 목·화면이 스펙을 따르는지 (서로 반대편에서 같은 파일을 지킨다) |
| `./gradlew :week25:test` | `pnpm test` + `pnpm typecheck` (+ `pnpm e2e`) |

백엔드가 `Clock` 을 고정해서 시간을 주입하듯, 프론트도 `Date.now()` 나 네트워크처럼 바뀌는 것을 밖에서 넣을 수 있게 만들면 테스트가 쉬워집니다. 이 저장소의 `createLibrary(client)` 가 같은 생각입니다.

## 한계: 이 주차가 다루지 않은 것

- 컴포넌트 렌더링 테스트, 스냅샷, 시각 회귀, 접근성 자동 검사는 다루지 않았습니다.
- CI 에서 어떤 층을 어느 시점에 돌릴지(풀 리퀘스트마다 단위·계약·`typecheck`, 배포 전에 e2e 등)는 개념 제안일 뿐 이 저장소에 CI 설정은 없습니다. 실행해 확인하지 않았습니다.
- 계약 테스트의 간이 검사기는 스펙 일부만 이해합니다. 소비자 주도 계약 테스트 도구는 쓰지 않았습니다.
- e2e 는 데이터를 공유하는 실서버에 의존해서 느리고 흔들릴 수 있습니다. 시나리오마다 새 데이터를 만들고 조건을 기다리는 단언을 쓰는 것이 이 저장소의 대응이고, 완전한 해법은 아닙니다.
- MSW 의 로그인 상태는 쿠키가 아니라 모듈 전역 값 하나입니다. 여러 사용자나 쿠키 속성은 목으로 검증할 수 없습니다.
- `pnpm test` 는 타입을 검사하지 않습니다. `typecheck` 와 함께 돌려야 합니다.

## 연습문제

1. 계약 테스트의 시나리오에 "4권째 대출은 409" 를 추가하세요. 실서버 DB 가 어지럽혀져 있어도 통과하도록 **매번 새 회원 1명과 새 책 4권** 을 만드세요. 목과 실서버 양쪽에서 통과시키세요.
2. `problems` 가 `enum` 을 검사하도록 늘리세요. 먼저 4단계의 `status` 스펙에서 목이 `status: "SOLD_OUT"` 을 주는 경우를 테스트로 재현해(실패를 눈으로 본 뒤) 구현하세요.
3. 백엔드를 끈 채 `NEXT_PUBLIC_API_MOCKING=enabled pnpm -C labs/frontend e2e` 를 실행해 보세요. 기존 2개가 통과하는지, `book-detail` 이 왜 실패하는지 설명하세요. (3000 에 다른 `pnpm dev` 가 떠 있지 않을 때 해야 합니다. 끝나면 백엔드를 다시 켜는 것을 잊지 마세요.)
4. 3단계의 4번 고장을 잡는 **컴포넌트 수준** 테스트를 만든다면 어떤 도구와 설정이 더 필요한지(`environment`, `include` 패턴, 의존성) 적고, e2e 와 비교해 비용을 평가하세요.
5. `LibraryApiTest` 의 `@Test` 이름 목록(`grep -A1 '@Test' labs/backend/week25/src/test/java/library/LibraryApiTest.java`)과 `contract.test.ts` 의 시나리오를 나란히 놓고, 백엔드 테스트에는 있고 프론트 시나리오에는 없는 요청 두 가지를 찾으세요. 그 요청이 빠져 있어서 생길 수 있는 어긋남은 무엇인가요?
6. 팀에서 "백엔드가 아직 안 나왔는데 화면을 먼저 만들어야 한다" 는 상황입니다. 스펙 → 타입 → 목 → 계약 테스트 → 화면 → 실서버 연결 순서에서 각 단계에 쓰는 파일과 명령을 적고, 계약 테스트는 어느 단계에서 처음 실서버 모드로 돌려야 하는지 쓰세요.

## 퀴즈

1. 3단계의 3번처럼 `as unknown as string` 으로 타입을 속인 목은 어느 층이 잡나요? 그리고 `typecheck` 가 못 잡는 이유는?
2. 계약 테스트가 메시지 문구를 비교하지 않는 이유는?
3. `CONTRACT=real` 에서만 실패할 때와 목에서만 실패할 때, 각각 무엇을 의심하나요?
4. 기본 모드의 e2e 가 3단계의 1번 고장(목의 `badId`)에 통과하는 이유는?
5. `reuseExistingServer: true` 가 위험해지는 상황을 하나 드세요.
6. 서버 컴포넌트의 쿠키 전달을 단위 테스트가 아니라 e2e 로 검사하는 이유는?
7. `pnpm test` 가 30개 모두 통과했는데, 스펙 파일이 갱신된 사실을 어떤 명령이 알려 주나요?

<details>
<summary>정답</summary>

1. 계약 테스트(실행 중의 값을 봄). `typecheck` 는 컴파일 시점의 선언만 보고, `as` 로 덮은 값은 믿어 버리기 때문에 못 잡는다
2. 문구는 `Accept-Language` 에 따라 달라서 계약이 아니기 때문이다. 문구까지 비교하면 로케일만 바뀌어도 실패한다
3. 목에서만 실패: 목이 낡았거나 틀렸다(`handlers.ts` 를 고친다). `CONTRACT=real` 에서만 실패: 서버가 스펙이나 기존 동작을 바꿨다. 의도한 변경인지 백엔드와 확인하고, 의도라면 스펙·목·화면을 함께 고친다. 둘 다 실패하면 스펙이 서버보다 새것이거나 시나리오가 틀렸다
4. 기본 모드의 e2e 는 MSW 를 켜지 않고 실서버를 상대로 하므로 목을 고쳐도 영향이 없다. 목의 오류는 목을 쓰는 단위·계약 테스트나 MSW 모드의 e2e 에서만 드러난다
5. 3000 에 MSW 모드로 띄운 `pnpm dev` 가 남아 있을 때. e2e 가 그 서버를 그대로 써서 실서버가 아니라 목을 상대로 돈다
6. `cookies()` 는 Next 서버의 요청 안에서만 값이 있고, 브라우저가 쿠키를 보내고 서버가 백엔드로 전달하는 전체 경로가 있어야 고장이 드러나기 때문이다(3단계의 5번)
7. `pnpm -C labs/frontend gen:api && pnpm -C labs/frontend typecheck`. 테스트는 타입을 검사하지 않는다

</details>

## 체크리스트

- [ ] 1단계의 5개 질문에 먼저 답을 적었고, `pnpm test` 가 30개(18 + 12)인 것을 확인했다
- [ ] `contract.test.ts` 와 `book-detail.spec.ts` 를 추가해 목·실서버·e2e 에서 모두 통과시켰다
- [ ] 3단계의 6가지 고장을 예측한 뒤 세 층에서 확인하고 모두 원복했다. 어느 고장을 어느 층이 잡고 놓치는지 표 없이 설명할 수 있다
- [ ] 4단계에서 필수 필드가 추가된 스펙이 `typecheck` 에서는 목 핸들러·테스트 데이터를, 계약 테스트에서는 응답을 지목하는 것을 확인했다
- [ ] `vitest.setup.ts` 의 `onUnhandledFrame: "error"` 와 `resetMockDb()` 가 하는 일을 설명할 수 있다
- [ ] 계약 테스트가 비교하는 것과 비교하지 않는 것(메시지 문구)을 구분하고 그 이유를 안다
- [ ] 같은 e2e 를 프록시·직접 호출·MSW 세 구성으로 돌려 보고, `book-detail` 이 MSW 에서 실패하는 이유를 설명할 수 있다
- [ ] 백엔드의 `LibraryServiceTest`, `LibraryApiTest`, `SecurityAndDocsTest` 가 프론트의 어느 층에 대응하는지 말할 수 있다
- [ ] `labs/frontend` 에 추가한 파일은 실험이 끝났다면 삭제했거나, 남겼다면 `pnpm test` 와 `pnpm typecheck` 가 통과한다
- [ ] 8080·3000 포트에 내가 띄운 서버를 모두 종료했다
