# 26주차. 에러 처리: 상태 코드를 화면으로

**예상 시간:** 6~8시간 | **목표:** 24주차 API 의 `{"message": "..."}` 와 400/401/404/409 를 프론트가 `ApiError` 하나로 받아, 입력칸 오류·알림·로그인 안내로 갈라 보여 주는 규약을 읽고 직접 확인한다. 이 규약이 어디서 깨지는지(한계)도 말할 수 있게 된다.

> 25주차를 먼저 끝내세요. 명령은 `../../labs/backend/` 와 `../../labs/frontend/` 에서 실행합니다. 실습 대상은 25주차와 같은 참조 API `../../labs/backend/week25` 와 `../../labs/frontend` 입니다.
>
> 준비물: JDK 21, Node 22.12+, pnpm 10+, `curl`. 이 문서가 설명하는 코드는 모두 `../../labs/frontend/src` 와 `../../labs/backend/week25/src/main/java/library` 에 실제로 있는 것입니다. 저장소에 **없는** 것은 "이 저장소에서는 구현하지 않음" 이라고 표시했습니다.

## 🖐 먼저 손으로 (읽기 전에 하세요)

> 이 주차의 핵심은 아래 단계를 **직접 해 보면서** 깨닫는 것입니다. 아래쪽 설명 문서는 먼저 읽지 않아도 됩니다. 막혔을 때 사전처럼 펼쳐 보세요.

### 1단계. 상태 코드와 화면을 예측하기 → 요청을 보내고 → 비교하기 (40분)

참조 API 를 새로 켠 상태에서 아래 요청을 위에서 아래로 차례로 보냅니다. 보내기 **전에** 각 요청의 상태 코드와, 프론트가 보여 줄 **화면의 종류**를 노트에 적으세요.

화면의 종류는 넷 중 하나로 적습니다: `입력칸 옆`(어느 입력이 틀렸는지), `알림`(폼 위쪽 경고), `로그인 안내`, `없음`(성공).

| # | 요청 | 상태 코드 | 화면 |
|---|------|------|------|
| 1 | 로그인 안 한 채 `POST /books` | | |
| 2 | 틀린 비밀번호로 `POST /auth/login` | | |
| 3 | 맞는 비밀번호로 `POST /auth/login` | | |
| 4 | `POST /books` `{"isbn":"111",...}` 정상 | | |
| 5 | `POST /books` 같은 `isbn` `111` 로 다시 | | |
| 6 | `POST /books` `{"isbn":"","title":"x","author":""}` | | |
| 7 | `POST /members` `{"name":"하승"}` | | |
| 8 | `POST /members` `{"name":"민지"}` | | |
| 9 | `POST /loans` 회원 1, 책 1 | | |
| 10 | `POST /loans` 회원 2, 책 1 (이미 대출 중) | | |
| 11 | `POST /loans` 회원 1, 책 99 | | |
| 12 | `POST /loans/1/return` | | |
| 13 | `POST /loans/1/return` 한 번 더 | | |
| 14 | `GET /members/99/loans` | | |
| 15 | `GET /books/abc` | | |
| 16 | `POST /books` 본문 `{` (깨진 JSON) | | |

서버를 켭니다. 8080 이 비어 있어야 하고, 데이터가 비어 있도록 새로 켭니다.

```bash
lsof -i :8080                      # 아무것도 안 나와야 함
cd ../../labs/backend
./gradlew :week25:bootRun          # 한 터미널에서 (Ctrl+C 로 종료)
```

다른 터미널에서 (서버를 새로 켠 직후, 한 번만 실행). 쿠키를 임시 파일에 저장해 로그인 상태를 이어 갑니다. 400 이상일 때만 본문을 찍습니다.

```bash
B=localhost:8080; J='Content-Type: application/json'; CJ=$(mktemp); BODY=$(mktemp)
c() {
  n=$1; shift
  code=$(curl -s -b "$CJ" -c "$CJ" -o "$BODY" -w '%{http_code}' "$@")
  if [ "$code" -ge 400 ]; then msg=$(cat "$BODY"); else msg=''; fi
  printf '%-3s %s  %s\n' "$n" "$code" "$msg"
}
c 1  -X POST -H "$J" -d '{"isbn":"111","title":"이펙티브 자바","author":"조슈아 블로크"}' $B/books
c 2  -X POST -H "$J" -d '{"username":"demo","password":"틀림"}' $B/auth/login
c 3  -X POST -H "$J" -d '{"username":"demo","password":"demo1234"}' $B/auth/login
c 4  -X POST -H "$J" -d '{"isbn":"111","title":"이펙티브 자바","author":"조슈아 블로크"}' $B/books
c 5  -X POST -H "$J" -d '{"isbn":"111","title":"다른 책","author":"누군가"}' $B/books
c 6  -X POST -H "$J" -d '{"isbn":"","title":"x","author":""}' $B/books
c 7  -X POST -H "$J" -d '{"name":"하승"}' $B/members
c 8  -X POST -H "$J" -d '{"name":"민지"}' $B/members
c 9  -X POST -H "$J" -d '{"memberId":1,"bookId":1}' $B/loans
c 10 -X POST -H "$J" -d '{"memberId":2,"bookId":1}' $B/loans
c 11 -X POST -H "$J" -d '{"memberId":1,"bookId":99}' $B/loans
c 12 -X POST $B/loans/1/return
c 13 -X POST $B/loans/1/return
c 14 $B/members/99/loans
c 15 $B/books/abc
c 16 -X POST -H "$J" -d '{' $B/books
```

<details>
<summary>정답 (예측을 적은 뒤에 여세요)</summary>

실제 출력입니다. (같은 코드를 8080 이 아닌 다른 포트에서 새로 켜서 확인했습니다.)

```text
1   401  {"message":"로그인이 필요합니다"}
2   401  {"message":"아이디 또는 비밀번호가 올바르지 않습니다"}
3   200
4   201
5   409  {"message":"이미 등록된 isbn 입니다: 111"}
6   400  {"message":"author: 공백일 수 없습니다, isbn: 공백일 수 없습니다"}
7   201
8   201
9   201
10  409  {"message":"이미 대출 중인 책입니다: 1"}
11  404  {"message":"책을 찾을 수 없습니다: 99"}
12  200
13  409  {"message":"이미 반납된 대출입니다: 1"}
14  404  {"message":"회원을 찾을 수 없습니다: 99"}
15  400  {"message":"id: 형식이 올바르지 않습니다"}
16  400  {"message":"요청 본문을 읽을 수 없습니다 (JSON 형식 확인)"}
```

프론트의 `uiKindOf` (`src/api/client.ts`)가 상태 코드를 `kind` 로 바꾸고, 화면은 `kind` 로 정해집니다.

| # | 상태 | `kind` | 화면 |
|---|---|---|---|
| 1, 2 | 401 | `auth` | 로그인 안내. 1번과 2번은 **같은 종류**이고 메시지만 다릅니다 |
| 5, 10, 13 | 409 | `conflict` | 알림 ("이미 처리되었거나 규칙에 어긋납니다") |
| 6 | 400 | `field` | 입력칸 옆. `author`, `isbn` 입력칸 각각에 |
| 11, 14 | 404 | `notFound` | 알림 ("찾을 수 없습니다") |
| 15 | 400 | `field` | 400 이라 `field` 이지만 `id` 라는 입력칸이 없습니다. 화면 입력이 아니라 **호출 코드의 버그**입니다 |
| 16 | 400 | `field` | `field` 이지만 `필드: 메시지` 모양이 아니라서 `fieldErrors` 가 **비어 있습니다** (아래 "한계" 절) |

- 9번이 아니라 10번이 409 인 이유를 확인하세요. 같은 책을 다른 회원이 빌리려는 요청 자체는 문법상 올바릅니다. 입력을 고쳐서 해결되는 문제가 아니라 **상태와 충돌**하는 문제입니다. 이 차이가 `field` 와 `conflict` 를 가르는 기준입니다.
- 11번의 404 는 "책 99 가 없다" 입니다. 서버 메시지만으로는 어느 입력칸이 틀렸는지 알 수 없어서 프론트는 입력칸이 아닌 알림으로 보여 줍니다.

</details>

한 가지 더. 6번을 `Accept-Language` 헤더만 바꿔서 두 번 보내 보세요. (로그인된 쿠키가 필요하므로 새 쿠키 파일을 씁니다.)

```bash
B=localhost:8080; J='Content-Type: application/json'; CJ=$(mktemp)
curl -s -c "$CJ" -o /dev/null -X POST -H "$J" -d '{"username":"demo","password":"demo1234"}' $B/auth/login
for lang in ko en; do
  printf '%s  ' $lang
  curl -s -b "$CJ" -H "Accept-Language: $lang" -X POST -H "$J" -d '{"isbn":"","title":"x","author":""}' $B/books; echo
done
```

<details>
<summary>실제 출력</summary>

```text
ko  {"message":"author: 공백일 수 없습니다, isbn: 공백일 수 없습니다"}
en  {"message":"author: must not be blank, isbn: must not be blank"}
```

같은 서버, 같은 요청인데 **메시지 문장이 요청 헤더에 따라 달라집니다.** `필드: ` 앞부분은 같습니다. 프론트가 파싱에 기대는 부분이 바로 이 모양입니다. 아래 "한계" 절에서 이어집니다.

</details>

### 2단계. 일부러 망가뜨리기 (1시간)

실습 대상 파일을 고치므로 **원복이 필수**입니다. 이 작업 디렉터리는 git 이 아니므로 먼저 백업합니다.

```bash
cp ../../labs/frontend/src/api/client.ts /tmp/client.backup.ts
```

각 실험 뒤에는 `cp /tmp/client.backup.ts ../../labs/frontend/src/api/client.ts` 로 되돌린 다음 다음 실험을 하세요. 결과를 노트에 먼저 예측하고 확인합니다. 아래 `pnpm` 명령은 `../../labs/frontend` 에서 실행합니다.

| 이렇게 바꿔 보세요 | 어떻게 될까요? |
|---|---|
| A. `src/api/client.ts` 의 `UI_KIND_BY_STATUS` 에서 `409: "conflict",` 줄 삭제 → `pnpm test` | 3개 실패. `409 는 conflict`, `같은 isbn 으로 다시 등록 -> 409 conflict + 메시지에 isbn`, `이미 대출 중인 책 -> 409, 없는 회원의 대출 목록 -> 404`. 메시지는 `expected 'unknown' to be 'conflict'` — 409 가 어느 종류에도 속하지 않아 `unknown` 이 되었다 |
| B. `fieldErrors` 의 `if (this.kind !== "field") return {};` 줄 삭제 → `pnpm test` | **18개 모두 통과.** 가드를 지웠는데 테스트가 못 잡는다. 아래 연습문제 2번에서 이유를 찾고 테스트를 보강한다 |
| C. `UiKind` 에 `"forbidden"` 추가 (`"conflict" \| "forbidden" \| "unknown"`) → `pnpm typecheck` | 오류 1개. `src/components/ErrorNotice.tsx(4,7): error TS2741: Property 'forbidden' is missing in type '{ field: ...; unknown: ... }' but required in type 'Record<UiKind, { title: string; box: string; }>'.` 새 종류를 만들면 화면 쪽 표(`STYLE`)를 채우지 않고는 컴파일이 안 된다 |

B 와 C 는 반대의 교훈입니다. C 에서는 **타입이 빠뜨림을 막아 주지만**, B 에서는 **테스트가 규칙을 지키고 있지 않습니다.** 24주차 4단계("테스트가 버그를 잡는지 확인")와 같은 질문입니다.

### 3단계. 빈칸에서 만들기 — 문서 안 연습 (1시간)

`../../labs/frontend` 에 새 파일을 만들지 않습니다. 노트나 임시 파일(`src/scratch.ts`)에서 작성하고 `pnpm typecheck` 로 확인한 뒤 **지웁니다.**

**(1)** 아래 `____` 를 채워, 오류를 "어디에 보여 줄지" 로 바꾸는 함수를 완성하세요. 새 `kind` 가 생기면 컴파일 오류가 나야 합니다.

```ts
import { ApiError, type UiKind } from "@/api/client";

export type Placement = "inputs" | "login" | "notice" | "retry";

export function placementOf(error: unknown): Placement {
  if (!(error instanceof ApiError)) return "retry"; // fetch 가 던진 TypeError (네트워크 끊김)
  const kind: UiKind = error.kind;
  switch (kind) {
    case "field":
      return "____";
    case "auth":
      return "____";
    case "notFound":
    case "conflict":
      return "____";
    case "unknown":
      return "retry";
    default: {
      const unreachable: never = ____;
      return unreachable;
    }
  }
}
```

**(2)** `src/app/providers.tsx` 에서, 쓰기 요청이 401 이면 로그인 상태 표시도 즉시 로그아웃으로 바꾸는 부분의 빈칸을 채우세요.

```ts
mutationCache: new MutationCache({
  onError: (error) => {
    if (error instanceof ApiError && ____) client.setQueryData(["me"], ____);
  },
}),
```

<details>
<summary>정답</summary>

(1)

```ts
    case "field":
      return "inputs";
    case "auth":
      return "login";
    case "notFound":
    case "conflict":
      return "notice";
    ...
      const unreachable: never = kind;
```

위 코드는 `pnpm typecheck` 를 통과합니다. 여기에 2단계 실험 C 처럼 `UiKind` 에 `"forbidden"` 을 더하면 `Type '"forbidden"' is not assignable to type 'never'` 오류가 납니다. `default` 의 `never` 가 "빠뜨린 종류를 컴파일러가 알려 주는" 장치입니다. 단, 이것은 이 문서의 연습이고 저장소에 있는 코드는 아닙니다.

(2)

```ts
    if (error instanceof ApiError && error.status === 401) client.setQueryData(["me"], null);
```

</details>

---

## 1. 서버의 규약: 모든 오류는 `{"message"}`

24주차 문서는 "모든 오류 응답은 `{"message": "..."}` 형식으로 통일" 이라고 정했습니다. 참조 API 에서 그 번역을 하는 곳은 `ApiExceptionHandler` (`@RestControllerAdvice`) **한 클래스**입니다.

| 예외 | 상태 | 메시지 |
|---|---|---|
| `NotFoundException` | 404 | 서비스가 만든 문장 (`책을 찾을 수 없습니다: 99`) |
| `ConflictException` | 409 | 서비스가 만든 문장 (`이미 대출 중인 책입니다: 1`) |
| `DataIntegrityViolationException` | 409 | `이미 존재하는 데이터와 충돌합니다` (동시 요청이 `existsByIsbn` 검사를 함께 통과했을 때 DB `unique` 제약이 여기로 옴) |
| `MethodArgumentNotValidException` | 400 | `필드: 메시지` 를 `, ` 로 이어 붙임 (필드 이름순) |
| `HttpMessageNotReadableException` | 400 | `요청 본문을 읽을 수 없습니다 (JSON 형식 확인)` |
| `MethodArgumentTypeMismatchException` | 400 | `이름: 형식이 올바르지 않습니다` |
| `BadCredentialsException` | 401 | `아이디 또는 비밀번호가 올바르지 않습니다` |
| `AuthenticationException` | 401 | `로그인이 필요합니다` |

로그인 안 한 요청의 401 은 스프링 시큐리티 필터에서 먼저 나옵니다. `SecurityConfig` 가 그 진입점을 `resolver.resolveException(...)` 으로 넘겨서 위 핸들러가 `{"message"}` 로 만들게 합니다. 그래서 401 도 같은 모양입니다.

**규약 밖의 응답도 있습니다.** 이 핸들러에 없는 오류는 스프링의 기본 오류 JSON 이 나갑니다. 실제로 확인한 응답입니다.

```text
PATCH /books/1                         405  {"timestamp":"...","status":405,"error":"Method Not Allowed","path":"/books/1"}
POST /books (로그인 상태, form 본문)    415  {"timestamp":"...","status":415,"error":"Unsupported Media Type","path":"/books"}
GET /nope                              404  {"timestamp":"...","status":404,"error":"Not Found","path":"/nope"}
```

세 응답 모두 `message` 키가 없습니다. 500 처럼 핸들러가 없는 서버 오류도 같은 부류로 보고 설계해야 합니다. (500 응답은 직접 만들어 보지 못했습니다.) 프론트가 "본문은 항상 `{"message"}`" 라고 가정하면 안 되는 이유입니다.

**Spring/Java 용어 ↔ 프론트 용어** (비유이므로 완전히 같지는 않습니다)

| Spring / Java 쪽 | 프론트에서 비슷한 것 |
|---|---|
| `@RestControllerAdvice` + `@ExceptionHandler` | 모든 오류를 한 모양으로 바꾸는 fetch 래퍼(`unwrap`)나 axios 인터셉터. React 의 error boundary 와는 다릅니다. boundary 는 **렌더링 중** 예외용이고, 이것은 **응답**을 바꾸는 곳입니다 |
| `NotFoundException`, `ConflictException` (업무 예외) | `class ApiError extends Error` |
| `ResponseEntity.status(404)` | `ApiError.status` |
| `@Valid` + `MethodArgumentNotValidException` | zod `safeParse` 의 실패(`issues`). 서버 검증은 프론트 검증을 대체하지 못하고, 프론트 검증도 서버를 대체하지 못합니다 |
| `AuthenticationEntryPoint` (401 진입점) | 401 인터셉터 → 로그인 화면. 이 저장소에서는 `MutationCache` 의 `onError` |
| `e.getMessage()` | `error.message` |

## 2. 프론트의 규약: `ApiError` 와 `uiKindOf`

`src/api/client.ts` 의 핵심입니다.

```ts
export type UiKind = "field" | "auth" | "notFound" | "conflict" | "unknown";

const UI_KIND_BY_STATUS: Record<number, UiKind> = {
  400: "field", // 입력 검증 실패 -> 해당 입력칸 옆에
  401: "auth", // 로그인 필요 -> 로그인 안내
  404: "notFound", // 대상 없음 -> 목록/상세 위치에 "없음" 안내
  409: "conflict", // 규칙 충돌(중복 isbn, 이미 대출 중...) -> 폼 위쪽 경고
};

export function uiKindOf(status: number): UiKind {
  return UI_KIND_BY_STATUS[status] ?? "unknown";
}

export class ApiError extends Error {
  constructor(readonly status: number, message: string) { ... }
  get kind(): UiKind { return uiKindOf(this.status); }
  get fieldErrors(): Record<string, string> { ... }
}
```

세 가지 장치가 있습니다.

1. **`unwrap`**: openapi-fetch 의 `{ data, error, response }` 를 "성공이면 `data`, 아니면 `ApiError` 를 throw" 로 바꿉니다. TanStack Query 의 `queryFn`/`mutationFn` 이 기대하는 모양입니다.
2. **`messageOf`**: 응답 본문에 `message` 문자열이 있으면 그것을, 없으면 `요청에 실패했습니다 (405)` 같은 문장을 만듭니다. 위 1절의 "규약 밖 응답"을 위한 대비입니다. 프록시가 돌려준 텍스트 502 도 `ApiError(502)` 가 됩니다 (`client.test.ts` 가 확인).
3. **`fieldErrors`**: 400 일 때 `"isbn: ..., title: ..."` 를 `, ` 로 쪼개 `{ isbn: "...", title: "..." }` 로 만듭니다. `^(\w+): (.+)$` 에 맞는 조각만 필드로 인정합니다.

상태 코드가 화면 종류로 이어지는 표는 `../../labs/frontend/README.md` 의 "오류 표시 규약" 과 `ErrorNotice.tsx` 의 `STYLE` 에 있습니다.

| 상태 | `kind` | `ErrorNotice` 제목 | 색 |
|---|---|---|---|
| 400 | `field` | 입력을 확인하세요 | `warning` |
| 401 | `auth` | 로그인이 필요합니다 | `info` |
| 404 | `notFound` | 찾을 수 없습니다 | `info` |
| 409 | `conflict` | 이미 처리되었거나 규칙에 어긋납니다 | `danger` |
| 그 외 | `unknown` | 요청에 실패했습니다 | `danger` |

## 3. 필드 에러 vs 알림 vs 로그인 유도: 가르는 기준

한 질문으로 가릅니다. **"사용자가 입력을 고치면 같은 요청이 성공하는가, 그리고 어느 입력인지 서버가 말해 주는가?"**

| 종류 | 상태 | 기준 | 이 저장소의 표시 |
|---|---|---|---|
| 필드 에러 | 400 | 입력이 틀렸고, 메시지가 `필드: 이유` 로 어느 입력인지 알려 준다 | 입력칸 옆 메시지 (`TextField` 의 `error`, `aria-invalid`) |
| 알림 | 409 | 입력은 올바른데 **서버의 현재 상태와 충돌** (중복, 이미 대출 중, 3권 한도, 이미 반납) | `ErrorNotice` (폼 위쪽, `role="alert"`) |
| 알림 | 404 | 대상이 없다. 어느 입력이 원인인지 메시지로는 알 수 없다 | `ErrorNotice` ("찾을 수 없습니다") |
| 로그인 유도 | 401 | 입력과 무관하게 **신원**이 없다 | `ErrorNotice` ("로그인이 필요합니다") + 로그인 상태 표시를 로그아웃으로 전환 |

401 에 대한 이 저장소의 방식은 이렇습니다.

- 쓰기 버튼(`SubmitButton`)은 로그인하지 않았으면 **누르기 전에** 비활성이고 "로그인 후 사용할 수 있습니다" 를 보여 줍니다. 서버의 401 은 안전망입니다.
- 로그인 상태는 `useMe` 가 `GET /auth/me` 로 조회합니다. 401 이면 **오류가 아니라 `null`** 을 돌려줘서, 화면이 "비로그인" 을 정상 상태로 다룹니다.
- 쓰기 요청이 401 을 받으면(세션 만료 등) `Providers` 의 `MutationCache.onError` 가 `["me"]` 를 `null` 로 바꿔 `AuthBar` 가 곧바로 로그인 폼을 보여 줍니다.
- 로그인 **페이지로 이동(리다이렉트)** 하는 것은 이 저장소에서는 구현하지 않음. 라우트가 `/`, `/members`, `/loans` 뿐이고 `/login` 은 없습니다.

기준의 한계도 보세요. 상태 코드는 거친 구분입니다. 같은 409 라도 원인이 네 가지(중복 isbn, 이미 대출 중, 3권 한도, 이미 반납)이고, 프론트는 `message` 를 그대로 보여 주기만 합니다. 원인에 따라 다른 버튼을 보여 주려면 서버가 오류 **코드**를 줘야 하는데, 이 저장소의 `ApiError` 는 `message` 하나뿐입니다. 이 저장소에서는 구현하지 않음.

## 4. 클라이언트 컴포넌트에서의 처리

데이터 패칭은 모두 클라이언트(TanStack Query)에서 합니다. 폼 하나를 따라가 봅니다. `BookForm.tsx` 의 핵심입니다.

```tsx
const create = useMutation({
  mutationFn: library.createBook,
  onSuccess: () => { setForm(EMPTY); return queryClient.invalidateQueries({ queryKey: ["books"] }); },
});

// 400 은 입력칸 옆에, 그 외(401/409/...)는 폼 위쪽 알림으로 — 서로 다르게 보인다.
const fieldErrors = create.error instanceof ApiError ? create.error.fieldErrors : {};
const showNotice = create.error instanceof ApiError ? create.error.kind !== "field" : Boolean(create.error);
```

```tsx
{showNotice && <ErrorNotice error={create.error} />}
<TextField label="isbn" ... error={fieldErrors.isbn} />
```

읽을 점입니다.

- 컴포넌트는 `onError` 콜백을 쓰지 않고, `mutation.error` **상태**를 렌더링에서 읽습니다. 이 저장소에서 `onError` 는 `providers.tsx` 의 `MutationCache` 한 곳에만 있습니다 (`grep onError src` 로 확인).
- `create.error` 의 타입은 `Error | null` 입니다. 그래서 `instanceof ApiError` 로 좁혀서 `kind` 와 `fieldErrors` 를 씁니다. `ApiError` 가 아닌 오류(네트워크 끊김의 `TypeError` 등)는 `kind` 가 없으므로 알림 쪽으로 갑니다.
- `ErrorNotice` 는 `role="alert"` 와 `data-kind={kind}` 를 갖습니다. 사용자는 색과 제목으로, 테스트는 `data-kind` 로 구분합니다. `e2e/lending.spec.ts` 가 "없는 책은 `notFound`, 이미 대출 중이면 `conflict`" 를 이 속성으로 확인합니다. (이 E2E 는 브라우저와 백엔드가 필요해서 이 문서를 쓰며 실행하지 못했습니다.)
- `QueryClient` 는 `retry: false` 입니다. 4xx 는 다시 해도 같기 때문입니다.
- `MyLoans` 는 `loans.error` 와 `giveBack.error` 를 각각 `ErrorNotice` 로 그대로 보여 줍니다. 입력칸으로 나누는 코드가 없으므로 400 도 "입력을 확인하세요" 알림이 됩니다.
- 로그인 실패(2번 요청)도 `kind` 는 `auth` 라서, 제목은 "로그인이 필요합니다", 본문은 서버 메시지 "아이디 또는 비밀번호가 올바르지 않습니다" 로 나옵니다. 종류가 상태 코드로만 정해지므로 제목이 상황에 딱 맞지 않을 수 있습니다.

## 5. 서버 컴포넌트에서의 처리: 이 저장소에서는 구현하지 않음

`../../labs/frontend` 의 `layout.tsx` 와 `page.tsx` 는 서버 컴포넌트이지만 데이터를 가져오지 않고, 가져오는 컴포넌트(`BookList` 등)가 `"use client"` 입니다. `error.tsx`, `not-found.tsx`, `notFound()` 호출은 **없습니다.** (`src` 에서 검색해 확인했습니다.) 프론트 README 는 이유를 이렇게 적습니다: 서버에서 가져오려면 브라우저 쿠키를 서버 요청에 직접 전달해야 하고, 목킹은 브라우저 서비스 워커에서만 동작하기 때문입니다.

그래서 아래는 이 저장소의 코드가 아니라, 이 저장소의 Next 문서(`node_modules/next/dist/docs/01-app/` 아래 `01-getting-started/10-error-handling.md`, `03-api-reference/03-file-conventions/error.md`, `04-functions/not-found.md`)를 읽고 정리한 비교입니다. 직접 실행해 확인하지는 않았습니다.

| | 클라이언트 (이 저장소) | 서버 컴포넌트 (미구현) |
|---|---|---|
| 오류가 도착하는 곳 | `mutation.error`, `query.error` **상태** → 직접 렌더링 | throw 하면 가장 가까운 `error.tsx` (React error boundary) |
| `ApiError` 의 `status`, `message` | 그대로 보존 (`instanceof` 가능) | 문서에 따르면 서버 컴포넌트가 던진 오류는 `error.tsx` 에서 일반화된 메시지와 `digest` 로만 보임. 사용자 문구를 살리려면 던지지 말고 **값으로 반환** |
| 404 | `ErrorNotice` (`notFound` 종류) | `notFound()` 를 호출하면 `not-found.tsx` 의 404 UI 를 렌더링 |
| 409/400 같은 "예상된" 오류 | 폼 옆/위에 표시 | 문서는 예상된 오류를 `try`/`catch` 가 아니라 **반환값**으로 다루라고 안내 (서버 함수는 `useActionState`) |
| 용도 | 사용자 행동에 대한 응답 | 예상 못 한 렌더링 실패의 안전망 |

한 문장으로: `error.tsx` 는 "예상 못 한 실패의 마지막 화면" 이고, `ApiError` 규약은 "예상한 실패를 종류별로 보여 주는 장치" 입니다. 서로 대체하지 않습니다. 이 문서의 `ErrorNotice` 방식을 서버 컴포넌트로 옮기려면 `ApiError` 를 그대로 던지지 말고 `kind` 별로 `notFound()` 호출, 값 반환, 던지기를 나눠야 합니다.

## 6. 한계: 이 규약이 깨지는 곳

### 6-1. 400 메시지 문자열 파싱

`fieldErrors` 는 서버가 만든 **문장**을 정규식으로 쪼갭니다. 서버가 필드 정보를 구조화해서 주지 않기 때문입니다. 25주차에서 스펙에 `ApiError` 가 `message: string` 하나뿐임을 이미 봤습니다. 아래는 `ApiError(400, ...)` 의 `fieldErrors` 를 실제로 계산해 본 결과입니다.

| 메시지 | `fieldErrors` |
|---|---|
| `author: 공백일 수 없습니다, isbn: 공백일 수 없습니다` | `{author, isbn}` 둘 다 (정상) |
| `author: must not be blank, isbn: must not be blank` | 같은 구조로 파싱됨 (언어가 달라도 `필드: ` 형식이 같으면 동작) |
| `id: 형식이 올바르지 않습니다` | `{id: ...}` (필드처럼 보이지만 화면에 그 입력칸이 없음) |
| `요청 본문을 읽을 수 없습니다 (JSON 형식 확인)` | `{}` — **필드 에러가 하나도 없습니다** |
| `title: 길이는 1에서 100 사이여야 합니다, 또는 비어 있으면 안 됩니다, isbn: 필수` | `{title: "길이는 1에서 100 사이여야 합니다", isbn: "필수"}` — 메시지 안의 `, ` 때문에 뒷부분이 **조용히 버려짐** |
| `items[0].name: 공백일 수 없습니다` | `{}` — `\w+` 가 `[0]` 과 `.` 을 못 받아서 중첩 필드가 **버려짐** |

정리하면 다섯 가지 위험입니다.

1. **언어(로케일)**: 1단계에서 봤듯이 검증 메시지 문장은 `Accept-Language` 에 따라 한국어/영어로 달라집니다. 프론트는 문장이 아니라 `필드: ` 형식만 가정하므로 언어에는 안전하지만, 서버가 메시지를 다듬다가 이 형식을 바꾸면 **조용히** 깨집니다. 타입(OpenAPI)이 이 형식을 보증하지 못합니다. (curl 은 헤더가 없으면 이 환경에서 한국어를 받았습니다. 브라우저는 자기 언어 설정을 보내므로 사용자마다 다를 수 있고, `/api` 프록시가 그 헤더를 그대로 넘기는지는 확인하지 않았습니다. 헤더를 보내지 않으면 서버 기본 로케일을 따릅니다.)
2. **`, ` 가 들어 있는 메시지**는 잘립니다.
3. **중첩 필드**(`items[0].name`)는 버려집니다.
4. **필드 모양이 아닌 400**(깨진 JSON, 숫자 자리에 문자열)은 `kind` 가 `field` 인데 `fieldErrors` 가 `{}` 입니다. `BookForm` 은 `field` 종류에는 알림을 띄우지 않으므로(`showNotice` 가 `false`), 코드대로라면 **아무것도 보이지 않습니다.** 화면에서 보낸 요청은 JSON 이 항상 올바르고 `LendForm` 의 숫자 칸은 `Number(...)` 로 바꿔 보내므로 실제로는 잘 일어나지 않지만(이 상황을 브라우저에서 재현해 보지는 않았습니다), 규약의 구멍입니다.
5. **MSW 가짜 서버의 문구**: `src/mocks/handlers.ts` 는 `must not be blank` 라는 **영어**를 하드코딩합니다. 실제 서버의 기본 응답(한국어)과 문장이 다릅니다. `client.test.ts` 가 문장이 아니라 필드 키(`["author", "isbn"]`)를 검사하는 이유입니다.

개선 방향은 서버가 구조화된 본문(예: `{"message": "...", "fieldErrors": [{"field": "isbn", "message": "..."}]}`)을 주는 것입니다. 이는 백엔드와 합의하고 스펙(`ApiError`)에 반영해야 하는 변경이라서, 이 저장소에서는 구현하지 않음. 연습문제 4번에서 설계해 봅니다.

### 6-2. 그 밖의 한계

- **상태 코드만으로 종류를 정함**: 같은 409 를 원인별로 가를 수 없습니다 (3절).
- **네트워크 실패**: `fetch` 가 던지는 `TypeError` 는 `ApiError` 가 아닙니다. `ErrorNotice` 는 이를 `unknown` 으로 보이고 메시지는 브라우저가 정한 문장 그대로입니다 (`client.test.ts` 가 `TypeError` 임을 확인).
- **401 처리의 범위**: `MutationCache.onError` 는 **쓰기** 요청의 401 만 처리합니다. 읽기(`query`)의 401 은 전역 처리가 없습니다. 현재 읽기 API 는 로그인 없이 열려 있고 `useMe` 만 직접 401 을 `null` 로 바꿉니다.
- **CSRF**: 백엔드에서 꺼져 있습니다 (데모, `SecurityConfig` 주석 참고). 오류 규약과는 별개의 주제입니다.

## 연습문제

1. 서버가 `403` 을 내기 시작했다고 가정하고 `kind` 에 `forbidden` 을 추가하세요. `client.ts` 외에 `pnpm typecheck` 가 어느 파일을 지목하나요? 그 파일에 어떤 제목과 색을 줄지 정하세요. (2단계 실험 C 의 확장)
2. 2단계 실험 B 를 설명하세요. 가드를 지워도 `client.test.ts` 의 기존 테스트가 통과하는 이유를 정규식 `^(\w+): (.+)$` 로 설명하고, **가드가 없을 때 실패하는** 테스트를 하나 추가하세요. (힌트: 409 메시지 `isbn: 중복`)
3. `fieldErrors` 의 정규식을 고쳐 `items[0].name: 메시지` 같은 중첩 필드도 받게 하세요. 어떤 문자를 허용해야 하고, 그래도 해결되지 않는 6-1 의 위험은 무엇인가요? 테스트를 먼저 쓰세요.
4. 400 응답에 구조화된 `fieldErrors` 를 담는 설계를 노트에 쓰세요. (a) `ApiError` 와 `ApiExceptionHandler` 의 어느 부분이 바뀌는지, (b) 25주차의 순서(`curl` → `gen:api` → `typecheck`)로 프론트의 어느 파일이 지목될지, (c) 이전 버전 프론트와 호환되려면 `message` 를 유지해야 하는 이유를 쓰세요.
5. `LendForm` 에서 책 id 칸에 `abc` 를 넣고 대출을 누르면 어떤 요청이 나가고 무엇이 보일지 코드를 읽고 예측하세요. (`Number("abc")` 의 결과가 JSON 으로 직렬화되면 어떻게 되는지 생각하세요.) 확인하려면 `NEXT_PUBLIC_API_MOCKING=enabled pnpm dev` 로 백엔드 없이 띄우고 로그인한 뒤 해 보세요.

## 퀴즈

1. 409 와 400 을 화면에서 다르게 보이는 이유는?
2. 쓰기 버튼을 로그인 전에 비활성으로 두는데도 401 처리 코드가 필요한 이유는?
3. 서버 컴포넌트에서 `ApiError` 를 그대로 throw 하면 `error.tsx` 가 어떤 정보를 받나? (이 저장소의 Next 문서 기준)
4. 400 메시지의 언어가 바뀌어도 `fieldErrors` 가 동작하는 이유와, 그래도 깨질 수 있는 경우는?
5. `pnpm test` 는 18개 통과하는데 `fieldErrors` 의 가드를 지워도 통과했다. 이것이 알려 주는 것은?
6. `PATCH /books/1` 이 405 일 때 프론트의 `ApiError.message` 는 무엇이 되나?

<details>
<summary>정답</summary>

1. 400 은 "입력이 틀렸고 어느 칸인지 안다" 라서 입력칸 옆에, 409 는 "입력은 맞는데 서버 상태와 충돌" 이라 입력을 고쳐도 해결되지 않을 수 있어서 폼 위 알림에 보인다
2. 세션 만료 등으로 로그인한 상태에서도 401 이 올 수 있고, 그때 화면의 로그인 표시를 서버 상태에 맞춰야 한다. 버튼 비활성은 UX 이고 서버 검사가 진짜 방어선이다
3. 문서에 따르면 서버 컴포넌트가 던진 오류는 일반화된 메시지와 `digest` 로만 전달된다(프로덕션). `status` 와 서버가 준 `message` 를 기대하면 안 된다
4. 파싱이 문장이 아니라 `필드: ` 형식만 보기 때문이다. 메시지에 `, ` 가 들어가거나, 중첩 필드 이름이거나, 서버가 형식을 바꾸면 깨진다
5. 테스트가 그 규칙을 지키고 있지 않다는 것(24주차 4단계와 같은 신호). `isbn: 중복` 같은 409 메시지로 가드를 직접 검사하는 테스트가 필요하다
6. `요청에 실패했습니다 (405)`. 응답 본문이 `{"timestamp",...}` 형태라 `message` 키가 없어서 `messageOf` 가 대비 문장을 만든다

</details>

## 체크리스트

- [ ] 16개 요청의 상태 코드와 화면 종류를 먼저 예측하고, 틀린 것의 이유를 적었다
- [ ] `ApiExceptionHandler` 의 예외 → 상태 코드 표와 `uiKindOf` 의 상태 코드 → `kind` 표를 보지 않고 말할 수 있다
- [ ] 필드 에러, 알림, 로그인 유도를 가르는 기준을 한 문장으로 설명할 수 있다
- [ ] 2단계 실험 A, B, C 를 하고 원복했으며, B 에서 테스트가 가드를 못 잡는 이유를 설명했다
- [ ] 서버 컴포넌트 처리가 이 저장소에서 구현되지 않았음을 알고, `error.tsx` 와 `ApiError` 규약의 역할 차이를 설명할 수 있다
- [ ] 400 메시지 파싱의 위험 5가지(언어, `, `, 중첩 필드, 필드 모양이 아닌 400, 가짜 서버 문구)를 말할 수 있다
