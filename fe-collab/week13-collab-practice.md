# 13주차. 협업 실무: API 변경 협의, 장애 재현 정보, 배포 환경 차이

**예상 시간:** 12~16시간 (하루 1시간 세션 14회) | **목표:** (1) API 변경이 소비자(프론트)에게 호환되는지 깨지는지 분류하고 변경 요청서로 협의한다. (2) 장애가 났을 때 요청 URL·상태·응답 본문·시각·상관 ID 를 모아 백엔드에게 전달하고, 백엔드 로그 한 줄을 읽어 같은 요청을 찾는다. (3) Docker 와 dev/staging/prod 환경 차이(`NEXT_PUBLIC_*`, 프록시, CORS, 쿠키 도메인)가 어디서 문제를 만드는지 설명한다.

> 이 주차는 코드를 새로 만드는 주차가 아니라 **사람과 사람 사이를 오가는 정보의 품질**을 다루는 주차입니다. 앞 주차의 도구(6주의 타입 생성, 7주의 에러 분기, 8주의 인증·CORS·쿠키, 9주의 로컬 통합 환경)가 모두 이 주차의 재료입니다. 도서관 참조 API `../labs/backend/week25` 를 "백엔드 팀이 준 API" 로 보고, 프론트 `../labs/frontend` 를 그 소비자로 봅니다.
>
> 준비물: JDK 21, Node 22.12+, pnpm 10+, `curl`, `jq`, Chrome 계열 브라우저. 프론트 패키지는 처음이라면 `cd labs/frontend && pnpm install` 을 먼저 합니다. 명령은 별도 언급이 없으면 **저장소 루트**에서 실행합니다. 백엔드는 `./gradlew :week25:bootRun` 대신 이미 만들어진 jar 로 띄워도 됩니다(`java -jar labs/backend/week25/build/libs/week25.jar`, jar 가 없으면 `cd labs/backend && ./gradlew :week25:bootJar`). 백엔드를 띄우는 실험은 모두 이 jar 로 확인했습니다. 3.1절의 Docker 실습에는 `docker` CLI 와 Colima 가 필요합니다(Docker Desktop 이 아니라 Colima 를 쓰는 환경 기준).
>
> **이 문서에서 직접 확인하지 못한 것:** Docker 는 이 문서를 쓴 머신(Apple Silicon, Colima)에서 3.1절의 명령을 실제로 실행해 확인했습니다. 다만 3.2절의 "해결 방향 세 가지", 프록시 뒤의 `X-Forwarded-Proto`, `docker compose` 는 실행해 보지 않은 **개념 설명**이고, 해당 위치에 그렇게 적었습니다. `docker context use colima` 와 `credsStore` 해결은 독자의 실제 설정을 바꾸지 않으려고 설정 디렉터리를 복사해 그 위에서 실행해 확인했습니다.

## 하루 1시간으로 나누면

이 주차는 Docker 실습이 들어 있어 하루 1시간씩 14번의 세션으로 나눕니다. 한 세션은 50~70분이고, 1.5~2시간이 걸리는 1단계와 2단계는 문서 안의 번호(a~k, 2-A, 2-B)나 구간으로 나누고, 3.1절의 Docker 실습은 `####` 소제목 단위로 4개 세션에 배정했습니다. 3단계의 3-D 는 선택입니다.

| 세션 | 할 일 | 끝났을 때 할 수 있어야 하는 것 |
|---|---|---|
| 1 | 1단계(API 변경 11가지를 분류하고 → 컴파일러에 물어보기, 1.5~2시간)의 a~k 11가지를 예측하고 `pnpm typecheck` 로 확인한 뒤, 1.1절(변경은 "소비자가 깨지는가" 로 분류한다)과 1.2절(분류표)을 읽는다 | 11가지 변경의 `pnpm typecheck` 오류 개수를 예측과 비교하고, 각 변경이 소비자를 깨는지 분류표(1.2절)로 설명할 수 있다 |
| 2 | 1단계의 나머지(enum 값 추가, 서버가 어디까지 너그러운지)를 하고, 1.3절(컴파일러가 못 잡는 변경), 1.4절(Deprecation: 지우기 전에 알린다), 1.5절(버전: URL, 헤더, 그리고 "버전 없이 추가만")을 읽는다 | 컴파일러가 잡지 못하는데 깨지는 변경(e, f)을 말하고, 응답 enum 값 추가가 컴파일 시점에 잡히는 코드와 런타임에야 드러나는 코드를 구분할 수 있다 |
| 3 | 1.6절(변경 요청서 템플릿)을 읽고 변경 요청서의 빈칸을 채운 뒤, 2단계(로그를 읽고 → 내 요청을 찾기, 1.5~2시간)의 2-A(기본 설정에서 어떤 요청이 로그를 남기는가)를 한다 | 변경 요청서(1.6절)의 빈칸을 채우고, 기본 설정에서 어떤 요청이 서버 로그를 남기고 어떤 요청이 남기지 않는지 말할 수 있다 |
| 4 | 2.1절(프론트가 모아서 전달할 재현 정보)과 2.2절(상관 ID(correlation ID))을 읽고, 2단계의 2-B(요청 하나를 세 군데에서 찾기)를 한다 | 상관 ID 로 내 요청의 로그 한 줄을 찾을 수 있다 |
| 5 | 2.3절(참조 API 의 로그는 무엇을 남기는가), 2.4절(로그 한 줄 읽기), 2.5절(응답 본문의 모양이 `{"message"}` 가 아닌 경우), 2.6절(재현 보고서 템플릿)을 읽고, 3단계(환경이 바뀌면 깨지는 것을 재현하기, 1~1.5시간)의 3-A(다른 출처(origin)에서 온 요청)를 한다 | 로그 한 줄에서 시각과 요청을 읽어 재현 정보(2.1절)를 빠짐없이 모으고, 출처가 바뀌면 같은 요청의 응답이 달라지는 것을 재현할 수 있다 |
| 6 | 3단계의 3-B(쿠키가 "받았는데 안 실리는" 경우)와 3-C(환경 변수로 비밀번호 바꾸기)를 하고, 3.3절(프록시, CORS, 쿠키 도메인)과 3.4절(환경 차이로 생긴 문제를 구분하는 질문)을 읽는다 | 쿠키 설정이 바뀔 때 쿠키가 실리지 않는 경우를 재현하고, 환경 변수로 백엔드 설정을 바꿀 수 있으며, 환경 차이로 생긴 문제를 질문으로 구분할 수 있다 |
| 7 | 4단계(`NEXT_PUBLIC_*` 가 언제 정해지는가, 30분~1시간)를 하고, 3.2절(환경 변수: 백엔드는 실행 때, 프론트는 빌드 때)을 읽는다 | `NEXT_PUBLIC_*` 가 빌드 때 정해지는 것을 빌드 산출물에서 보이고, 백엔드는 실행 때, 프론트는 빌드 때 환경 변수가 정해진다는 것을 설명할 수 있다 |
| 8 | 3.1절(Docker 개념)의 표와 개념 세 가지를 읽고, 소절 "준비: Colima 와 docker context" 를 따라 한다 | `docker run --rm hello-world` 가 성공하는 것을 확인하고, 이미지·컨테이너·`Dockerfile` 을 프론트의 빌드 결과물·실행 중인 프로세스·빌드 스크립트에 대응시켜 말할 수 있다 |
| 9 | 3.1절의 소절 "백엔드 이미지" 를 따라 한다 | 백엔드 jar 로 이미지를 만들어 컨테이너로 띄우고, `curl` 로 호출하며, `-e` 로 비밀번호를 바꿔 다시 띄울 수 있다 |
| 10 | 3단계의 3-D(같은 jar 를 컨테이너로 띄우기, 선택, 30분~1시간)를 한다 (압축 코스에서는 건너뜀) | 호스트와 컨테이너 안에서 8080 과 8090 이 각각 열려 있는지 예측하고 `curl` 로 확인할 수 있다 |
| 11 | 3.1절의 소절 "포트 매핑의 방향과 컨테이너 안의 localhost" 와 "일상 시나리오: 프론트는 호스트에서, 백엔드는 컨테이너로" 를 따라 한다 | 컨테이너로 띄운 백엔드를 `curl` 과 호스트의 `pnpm dev` 로 호출하고, 컨테이너 안의 `localhost` 가 왜 자기 자신인지 설명할 수 있다 |
| 12 | 3.1절의 소절 "프론트 이미지" 를 따라 한다. 이미지 빌드에 시간이 걸리므로 빌드를 먼저 시작해 둔다 | 빌드 인자로 준 `NEXT_PUBLIC_API_BASE` 가 번들에 박히고 실행 때의 `-e` 값은 영향이 없다는 것을 보이고, 프론트 컨테이너 안에서 `/api` 호출이 실패하는 이유를 로그로 설명하며, 만든 컨테이너와 이미지를 정리할 수 있다 |
| 13 | 연습문제와 퀴즈를 푼다 (압축 코스에서는 건너뜀) | `dueAt` 필드를 추가하는 변경 요청서와 2단계 요청 3번의 장애 제보를 양식대로 쓰고, 컨테이너의 포트 매핑 방향을 `curl` 로 확인할 수 있다 |
| 14 | 체크리스트의 각 항목을 내 말로 설명해 보고, 막히는 항목은 해당 절을 다시 읽은 뒤 학습 노트를 정리한다 | 환경별로 필요한 프록시·CORS·쿠키 설정을 표로 설명하고, 환경 차이로 생긴 문제를 질문으로 구분할 수 있다 |

세션을 시작하기 전에 이전 세션의 마지막 결과를 1~2분 훑어봅니다. 막히면 아래 해당 개념 절을 사전처럼 펼쳐 보세요.

## 🖐 먼저 손으로 (읽기 전에 하세요)

> 이 주차의 핵심은 아래 단계를 **직접 해 보면서** 깨닫는 것입니다. 아래쪽 설명 문서는 먼저 읽지 않아도 됩니다. 막혔을 때 사전처럼 펼쳐 보세요.

### 1단계. API 변경 11가지를 분류하고 → 컴파일러에 물어보기 (1.5~2시간)

백엔드 팀이 스펙(`labs/backend/week25/openapi.json`)을 아래처럼 바꿨다고 가정합니다. 실제로 바꾸고 `pnpm typecheck` 가 오류를 몇 개 내는지 보기 **전에**, 표의 두 칸을 노트에 적으세요.

| # | 변경 | 호환? (호환 / 깨짐 / 애매) | `pnpm typecheck` 오류 개수 예측 |
|---|------|------|------|
| a | `BookResponse` 에 선택 필드 `publisher` 추가 | | |
| b | `BookResponse` 에 **필수** 필드 `publisher` 추가 | | |
| c | `BookResponse` 에서 `author` 삭제 | | |
| d | `BookResponse.id` 의 타입을 정수에서 문자열로 변경 | | |
| e | `BookResponse.author` 가 `null` 이 될 수 있게 변경 | | |
| f | `BookResponse` 의 필수 필드 `author` 를 선택 필드로 변경 | | |
| g | `BookRequest` 에 선택 필드 `publisher` 추가 | | |
| h | `BookRequest` 에 **필수** 필드 `publisher` 추가 | | |
| i | `/books` 경로를 `/book-list` 로 변경 | | |
| j | 새 엔드포인트 `GET /books/{id}/reviews` 추가 | | |
| k | `POST /books` 에 새 오류 응답 `403` 추가 | | |

예측을 적었으면 실행합니다. 이 저장소는 git 이 아니므로 **먼저 백업**을 만들고, 끝나면 반드시 원복합니다.

```bash
SPEC=labs/backend/week25/openapi.json
cp $SPEC /tmp/openapi.backup.json
cp labs/frontend/src/api/schema.d.ts /tmp/schema.backup.d.ts

# 사용법: try '<jq 필터>' -> 스펙을 바꾸고, 타입을 다시 생성하고, 타입 오류 개수를 출력한다
try() { jq "$1" /tmp/openapi.backup.json > $SPEC; (cd labs/frontend && pnpm -s gen:api >/dev/null && pnpm -s typecheck 2>&1 | grep -c 'error TS'); }

try '.components.schemas.BookResponse.properties.publisher={"type":"string"}'                                                    # a
try '.components.schemas.BookResponse.properties.publisher={"type":"string"} | .components.schemas.BookResponse.required+=["publisher"]'   # b
try 'del(.components.schemas.BookResponse.properties.author) | .components.schemas.BookResponse.required-=["author"]'            # c
try '.components.schemas.BookResponse.properties.id={"type":"string"}'                                                           # d
try '.components.schemas.BookResponse.properties.author={"type":["string","null"]}'                                              # e
try '.components.schemas.BookResponse.required-=["author"]'                                                                      # f
try '.components.schemas.BookRequest.properties.publisher={"type":"string"}'                                                     # g
try '.components.schemas.BookRequest.properties.publisher={"type":"string"} | .components.schemas.BookRequest.required+=["publisher"]'    # h
try '.paths["/book-list"]=.paths["/books"] | del(.paths["/books"])'                                                              # i
try '.paths["/books/{id}/reviews"]={"get":{"operationId":"reviews","parameters":[{"name":"id","in":"path","required":true,"schema":{"type":"integer","format":"int64"}}],"responses":{"200":{"description":"OK","content":{"application/json":{"schema":{"type":"array","items":{"type":"string"}}}}}}}}'   # j
try '.paths["/books"].post.responses["403"]={"description":"권한 없음","content":{"application/json":{"schema":{"$ref":"#/components/schemas/ApiError"}}}}'   # k

# 원복
cp /tmp/openapi.backup.json $SPEC
(cd labs/frontend && pnpm -s gen:api >/dev/null && pnpm -s typecheck | grep -c 'error TS')      # 0 이어야 함
```

<details>
<summary>실제 결과 (예측을 적은 뒤에 여세요)</summary>

위 순서대로 `try` 가 출력한 값입니다.

| # | 오류 개수 | 어디서 | 분류 |
|---|------|------|------|
| a | 0 | | 호환 |
| b | 4 | `src/mocks/handlers.ts` 3곳 + `src/lib/query-design.test.ts` 1곳 (MSW 가짜 서버와 테스트 데이터가 `publisher` 없이 `Book` 을 만듦) | 소비자에게는 호환. 가짜 서버와 테스트 데이터만 고치면 됨 |
| c | 6 | 화면 2곳(`BookList.tsx`, `books/[id]/page.tsx`) + `query-design.test.ts` 1곳 + `handlers.ts` 3곳 | 깨짐 |
| d | 6 | `handlers.ts` 4곳 + `query-design.test.ts` 2곳 (화면 컴포넌트는 `id` 를 `key` 와 출력에만 써서 안 걸림) | 깨짐 |
| e | **0** | | **깨짐인데 컴파일러가 못 잡음** |
| f | **0** | | **깨짐인데 컴파일러가 못 잡음** |
| g | 0 | | 호환 |
| h | 5 | `client.test.ts` 4곳 + `BookForm.tsx` 1곳 | 깨짐 |
| i | 11 | `library.ts` 2곳, `BookForm.tsx` 2곳, `BookList.tsx` 3곳, `client.test.ts` 3곳, `bff/summary/route.ts` 1곳 | 깨짐 |
| j | 0 | | 호환 |
| k | 0 | | 호환 |

- e 와 f 가 핵심입니다. `BookList.tsx` 는 `author` 를 JSX 에 찍기만 하므로 `string | null` 이나 `string | undefined` 가 되어도 컴파일됩니다. 하지만 같은 필드로 `book.author.toLowerCase()` 를 호출하는 코드가 어딘가에 있었다면 런타임에 터집니다. **오류 개수는 "이 저장소의 호출 코드가 우연히 어떻게 쓰고 있는가" 일 뿐, 변경이 호환인지 아닌지의 기준이 아닙니다.** 분류는 계약(스펙)을 보고 합니다.
- b 의 4개는 "소비자가 깨졌다" 가 아니라 "소비자 쪽에 있는 가짜 서버와 테스트 데이터가 새 계약을 못 따라간다" 는 뜻입니다. 이것도 6주에서 본, 계약이 바뀌면 가짜 서버가 알려 주는 효과입니다.
- d 에서 화면 컴포넌트가 걸리지 않은 것도 같은 이유입니다. 숫자가 문자열이 되어도 `key` 와 JSX 출력에는 둘 다 들어갑니다. `book.id + 1` 같은 코드가 있었다면 의미가 조용히 바뀌었을 것입니다(숫자 덧셈이 문자열 연결이 됩니다).
- 오류 개수는 이 문서를 쓴 시점의 `labs/frontend` 기준이며, 컴포넌트가 바뀌면 달라질 수 있습니다. 호환/깨짐 분류는 달라지지 않습니다.

</details>

이번에는 **enum 값 추가**입니다. 현재 스펙에는 enum 이 없으므로 `status` 필드를 새로 만들어 봅니다. 먼저 임시 파일을 만듭니다(끝나면 지웁니다).

```bash
cat > labs/frontend/src/scratch-status.ts <<'EOF'
import type { components } from "@/api/schema";

type Book = components["schemas"]["BookResponse"];

export function statusLabel(book: Book): string {
  switch (book.status) {
    case undefined:
      return "알 수 없음";
    case "AVAILABLE":
      return "대출 가능";
    case "ON_LOAN":
      return "대출 중";
    default: {
      const unreachable: never = book.status;
      return unreachable;
    }
  }
}
EOF

try '.components.schemas.BookResponse.properties.status={"type":"string","enum":["AVAILABLE","ON_LOAN"]}'
try '.components.schemas.BookResponse.properties.status={"type":"string","enum":["AVAILABLE","ON_LOAN","LOST"]}'

rm labs/frontend/src/scratch-status.ts
cp /tmp/openapi.backup.json $SPEC
(cd labs/frontend && pnpm -s gen:api >/dev/null && pnpm -s typecheck | grep -c 'error TS')      # 0 이어야 함
```

먼저 노트에 적으세요. **첫 번째 `try` 와 두 번째 `try` 는 각각 몇 개를 출력할까요? 응답에 enum 값이 하나 늘어난 것은 호환일까요? 요청 쪽 enum 에 값이 하나 늘어난 것은요?**

<details>
<summary>실제 결과</summary>

첫 번째는 `0`, 두 번째는 `1` 입니다. 오류는 이렇게 납니다.

```text
src/scratch-status.ts(14,13): error TS2322: Type '"LOST"' is not assignable to type 'never'.
```

- 응답 enum 에 값이 늘어나면, 소비자가 `switch` 로 모든 값을 처리하고 `default` 에서 `never` 검사를 하는 코드(exhaustive check)는 **컴파일 시점에** 깨지고, 그런 검사를 하지 않는 코드는 새 값을 만나는 순간 **런타임에** 조용히 잘못 동작합니다. 그래서 응답 enum 값 추가는 "호환" 이 아니라 "소비자와 합의가 필요한 변경" 입니다.
- 같은 방법으로 enum 에서 값을 빼면(`["AVAILABLE"]` 만 남기면) `case "ON_LOAN"` 에서 `Type '"ON_LOAN"' is not comparable to type '"AVAILABLE"'` 오류가 납니다.
- 요청 enum 에 값이 늘어나는 것은 호환입니다. 프론트는 아는 값만 보내기 때문입니다. 반대로 요청 enum 에서 값을 빼면 그 값을 보내던 호출이 400 이 됩니다.

</details>

(위 `try` 를 정의한 터미널을 그대로 쓰세요. 새 터미널이라면 `SPEC=...` 와 `try() {...}` 두 줄을 다시 실행해야 합니다.)

마지막으로 **서버가 어디까지 너그러운지**를 봅니다. 백엔드를 띄우고(`java -jar labs/backend/week25/build/libs/week25.jar`) 로그인한 뒤 모르는 필드를 보내 보세요.

```bash
B=localhost:8080
curl -s -o /dev/null -c /tmp/w13.jar -X POST $B/auth/login -H 'Content-Type: application/json' -d '{"username":"demo","password":"demo1234"}'
curl -si -b /tmp/w13.jar -X POST $B/books -H 'Content-Type: application/json' -d '{"isbn":"978-1","title":"Clean Code","author":"Martin","publisher":"x"}' | sed -n '1p;$p'
curl -si -b /tmp/w13.jar -X POST $B/books -H 'Content-Type: application/json' -d '{"isbn":"978-2","title":"t"}' | sed -n '1p;$p'
curl -si "$B/books?foo=1" | sed -n '1p;$p'
```

<details>
<summary>실제 결과</summary>

```text
HTTP/1.1 201 
{"id":1,"isbn":"978-1","title":"Clean Code","author":"Martin"}
HTTP/1.1 400 
{"message":"author: 공백일 수 없습니다"}
HTTP/1.1 200 
[{"id":1,"isbn":"978-1","title":"Clean Code","author":"Martin"}]
```

- 모르는 요청 필드(`publisher`)와 모르는 쿼리(`foo=1`)는 **조용히 무시됩니다.** 그래서 "프론트가 새 필드를 먼저 보내기 시작하는 것" 은 서버가 아직 모르는 동안에는 아무 일도 일으키지 않습니다. 그러나 이것은 **이 서버의 기본 동작일 뿐 계약이 아닙니다.** 조용히 무시되므로 "보냈는데 저장이 안 된 것" 을 아무도 알아채지 못합니다.
- 필수 필드가 빠지면 400 입니다. 요청에 필수 필드를 추가하는 변경(h)이 깨짐인 이유입니다.

</details>

### 2단계. 로그를 읽고 → 내 요청을 찾기 (1.5~2시간)

#### 2-A. 기본 설정에서 어떤 요청이 로그를 남기는가

먼저 노트에 적으세요. 백엔드를 **옵션 없이** 띄우고 아래 8개 요청을 차례로 보낼 때, 서버 터미널에 **새로 생기는 로그 줄은 요청마다 몇 줄**일까요?

```bash
java -jar labs/backend/week25/build/libs/week25.jar        # 터미널 A (8080 이 비어 있어야 함)
```

```bash
B=localhost:8080                                           # 터미널 B
curl -s -o /dev/null -c /tmp/w13.jar -X POST $B/auth/login -H 'Content-Type: application/json' -d '{"username":"demo","password":"demo1234"}'
curl -s -o /dev/null -b /tmp/w13.jar -X POST $B/books -H 'Content-Type: application/json' -d '{"isbn":"1","title":"t","author":"a"}'   # 준비: 책 한 권 등록 (201)
```

| # | 요청 | 상태 | 새 로그 줄 예측 |
|---|------|------|------|
| 1 | `curl -s -o /dev/null -w '%{http_code}\n' -X POST $B/books -H 'Content-Type: application/json' -d '{"isbn":"9","title":"t","author":"a"}'` | 401 | |
| 2 | `curl -s -o /dev/null -w '%{http_code}\n' -b /tmp/w13.jar -X POST $B/books -H 'Content-Type: application/json' -d '{"isbn":'` | 400 | |
| 3 | `curl -s -o /dev/null -w '%{http_code}\n' -b /tmp/w13.jar -X POST $B/loans -H 'Content-Type: application/json' -d '{"memberId":1,"bookId":1}'` | 404 | |
| 4 | `curl -s -o /dev/null -w '%{http_code}\n' -b /tmp/w13.jar -X POST $B/books -H 'Content-Type: application/json' -d '{"isbn":"1","title":"t","author":"a"}'` | 409 | |
| 5 | `curl -s -o /dev/null -w '%{http_code}\n' -X POST $B/auth/login -H 'Content-Type: text/plain' -d x` | 415 | |
| 6 | `curl -s -o /dev/null -w '%{http_code}\n' -X PUT $B/books` | 405 | |
| 7 | `curl -s -o /dev/null -w '%{http_code}\n' $B/nope` | 404 | |
| 8 | `curl -s -o /dev/null -w '%{http_code}\n' $B/books` | 200 | |

<details>
<summary>실제 결과 (예측을 적은 뒤에 여세요)</summary>

새 로그 줄이 생긴 것은 **5번과 6번뿐**이고 나머지는 모두 0줄이었습니다. 두 줄은 이렇습니다.

```text
2026-10-22T22:52:48.391+09:00  WARN 88935 --- [nio-8083-exec-2] .w.s.m.s.DefaultHandlerExceptionResolver : Resolved [org.springframework.web.HttpMediaTypeNotSupportedException: Content-Type 'text/plain;charset=UTF-8' is not supported]
2026-10-22T22:52:48.742+09:00  WARN 88935 --- [nio-8083-exec-4] .w.s.m.s.DefaultHandlerExceptionResolver : Resolved [org.springframework.web.HttpRequestMethodNotSupportedException: Request method 'PUT' is not supported]
```

- 401, 400, 404, 409 는 `ApiExceptionHandler` 가 정상적인 응답으로 바꿔 돌려주므로 서버는 "처리했다" 고 보고 **아무것도 기록하지 않습니다.** 이 저장소의 `src/main` 에는 로그를 남기는 코드가 한 줄도 없습니다.
- 성공(200)도 기본 설정에서는 기록되지 않습니다. 즉 **기본 설정의 백엔드 로그만으로는 "그 시각에 누가 어떤 요청을 했는지" 를 알 수 없습니다.** 장애 제보를 받은 백엔드 개발자가 로그에서 단서를 못 찾는 흔한 이유입니다.
- 7번(없는 경로)은 로그 0줄이지만 응답 본문의 모양이 다릅니다. 아래 2.5절에서 다룹니다.
- 위 로그의 `nio-8083`, `88935` 는 이 문서를 쓴 환경에서 서버를 8083 포트로 띄웠기 때문에 나온 값입니다. 독자 환경에서는 `nio-8080` 과 다른 프로세스 번호가 나옵니다.

</details>

#### 2-B. 요청 하나를 세 군데에서 찾기

서버를 끄고, 이번에는 접근 로그와 요청 로그를 켜서 띄웁니다. 접근 로그에는 프론트가 보낸 `X-Request-Id` 헤더를 함께 남기도록 패턴을 지정합니다.

```bash
mkdir -p /tmp/w13-tomcat
java -jar labs/backend/week25/build/libs/week25.jar \
  --server.tomcat.basedir=/tmp/w13-tomcat \
  --server.tomcat.accesslog.enabled=true \
  --server.tomcat.accesslog.buffered=false \
  '--server.tomcat.accesslog.pattern=%t "%r" %s %b %{ms}Tms rid=%{X-Request-Id}i' \
  --logging.level.org.springframework.web.servlet.DispatcherServlet=DEBUG
```

다른 터미널에서 같은 isbn 을 두 번 등록하는 요청을 보냅니다. 두 번째 요청에만 `X-Request-Id` 를 붙입니다.

```bash
B=localhost:8080
curl -s -o /dev/null -c /tmp/w13.jar -X POST $B/auth/login -H 'Content-Type: application/json' -d '{"username":"demo","password":"demo1234"}'
curl -s -o /dev/null -b /tmp/w13.jar -X POST $B/books -H 'Content-Type: application/json' -d '{"isbn":"978-1","title":"Clean Code","author":"Martin"}'
curl -si -b /tmp/w13.jar -H 'X-Request-Id: fe-20261022-0001' -X POST $B/books -H 'Content-Type: application/json' -d '{"isbn":"978-1","title":"Clean Code","author":"Martin"}'
cat /tmp/w13-tomcat/logs/access_log*
```

아래 질문에 답을 적고 나서 펼치세요.

1. 응답(헤더와 본문)에 `X-Request-Id` 가 돌아오는가?
2. "이미 등록된 isbn 입니다: 978-1" 이라는 문장은 서버 로그 두 종류(접근 로그, 애플리케이션 로그) 중 어디에 있는가?
3. 응답의 `Date` 헤더와 로그의 시각은 같은 시각인가? 표기가 어떻게 다른가?
4. 백엔드 개발자가 "프론트가 본 그 요청" 을 로그에서 찾을 때 쓸 수 있는 단서는 무엇인가?

<details>
<summary>실제 결과</summary>

응답입니다(일부 헤더 생략).

```text
HTTP/1.1 409 
Content-Type: application/json
Date: Thu, 22 Oct 2026 13:48:44 GMT

{"message":"이미 등록된 isbn 입니다: 978-1"}
```

접근 로그(`/tmp/w13-tomcat/logs/access_log.<날짜>.log`)의 마지막 줄입니다.

```text
[22/Oct/2026:22:48:44 +0900] "POST /books HTTP/1.1" 409 63 5ms rid=fe-20261022-0001
```

애플리케이션 로그(서버를 띄운 터미널)입니다.

```text
2026-10-22T22:48:44.569+09:00 DEBUG 74338 --- [nio-8083-exec-5] o.s.web.servlet.DispatcherServlet        : POST "/books", parameters={}
2026-10-22T22:48:44.573+09:00 DEBUG 74338 --- [nio-8083-exec-5] o.s.web.servlet.DispatcherServlet        : Completed 409 CONFLICT
```

1. 돌아오지 않습니다. 참조 API 는 `X-Request-Id` 를 만들지도, 응답에 되돌려 주지도 않습니다. 위 접근 로그에 `rid=fe-20261022-0001` 이 남은 것은 **서버가 아는 척한 것이 아니라 접근 로그 패턴에 `%{X-Request-Id}i`(요청 헤더 출력)를 우리가 적어 넣었기 때문**입니다. 패턴을 빼면 같은 요청이 로그에 `rid=-` 로 남습니다.
2. 둘 다 아닙니다. 두 로그에는 상태 코드(`409`)만 있고 문장이 없습니다. 문장은 **응답 본문에만** 있습니다. 그래서 장애 제보에는 응답 본문을 그대로 붙여야 합니다.
3. 같은 순간입니다. `Date` 는 항상 GMT(`13:48:44 GMT`)이고, 로그는 서버 머신의 시간대(`22:48:44 +0900`)입니다. 서로 9시간 차이로 보여서 헷갈리기 쉽습니다.
4. 시각(초 단위)+경로+메서드+상태 코드가 기본 단서이고, 접근 로그에 헤더를 남기도록 설정했다면 상관 ID 로 한 번에 찾습니다. 같은 요청의 애플리케이션 로그 줄은 **스레드 이름 `[nio-8083-exec-5]`** 로 이어집니다(두 줄이 같은 스레드). 다만 스레드는 재사용되므로 시각이 가까운 줄끼리만 같은 요청입니다.

</details>

### 3단계. 환경이 바뀌면 깨지는 것을 재현하기 (1~1.5시간)

#### 3-A. 다른 출처(origin)에서 온 요청

백엔드를 옵션 없이 띄운 상태에서, 허용된 출처(`http://localhost:3000`)와 허용되지 않은 출처(`https://staging.example.com`)로 preflight 와 단순 요청을 보냅니다.

```bash
B=localhost:8080
pf() { echo "## Origin=$1 headers=$2"; curl -si -X OPTIONS $B/books -H "Origin: $1" -H 'Access-Control-Request-Method: POST' -H "Access-Control-Request-Headers: $2" | sed -n '1p;/^Access-Control/p;/^Invalid/p'; }
pf http://localhost:3000 content-type
pf http://localhost:3000 content-type,x-request-id
pf https://staging.example.com content-type
curl -si $B/books -H 'Origin: https://staging.example.com' | sed -n '1p;$p'
```

먼저 노트에 적으세요. 세 번째와 네 번째 요청의 상태 코드는 무엇이고, 두 번째 요청의 `Access-Control-Allow-Headers` 에는 무엇이 들어 있을까요?

<details>
<summary>실제 결과</summary>

```text
## Origin=http://localhost:3000 headers=content-type
HTTP/1.1 200 
Access-Control-Allow-Origin: http://localhost:3000
Access-Control-Allow-Methods: GET,POST
Access-Control-Allow-Headers: content-type
Access-Control-Allow-Credentials: true
## Origin=http://localhost:3000 headers=content-type,x-request-id
HTTP/1.1 200 
Access-Control-Allow-Origin: http://localhost:3000
Access-Control-Allow-Methods: GET,POST
Access-Control-Allow-Headers: content-type
Access-Control-Allow-Credentials: true
## Origin=https://staging.example.com headers=content-type
HTTP/1.1 403 
Invalid CORS request
HTTP/1.1 403 
Invalid CORS request
```

- 허용 출처는 `SecurityConfig.corsConfigurationSource()` 에 `http://localhost:3000` 한 개가 **코드에 박혀** 있습니다. 스테이징 프론트의 주소가 다르면 이 서버는 모든 요청을 403 으로 막습니다. 환경마다 프론트 출처가 다르다는 사실은 백엔드 코드(또는 설정) 변경 요청의 대표적인 사례입니다.
- 두 번째 요청에서 서버는 200 으로 답하지만 `Access-Control-Allow-Headers` 에 `x-request-id` 가 **없습니다.** 허용 헤더가 `Content-Type` 하나로 고정되어 있기 때문입니다. 브라우저는 preflight 응답에 허용되지 않은 요청 헤더가 있으면 본 요청을 보내지 않고 막습니다(브라우저의 규칙이라 curl 로는 재현되지 않습니다). 프론트가 사용자 정의 헤더를 보내려면 **백엔드 CORS 설정에 그 헤더를 추가해 달라고 요청해야 합니다.**
- 이 상황은 프론트가 백엔드를 직접 호출할 때(`NEXT_PUBLIC_API_BASE=http://localhost:8080`)만 생깁니다. 기본값인 `/api` 프록시를 쓰면 브라우저 입장에서 같은 출처이므로 CORS 검사 자체가 없습니다.

</details>

#### 3-B. 쿠키가 "받았는데 안 실리는" 경우

환경 변수로 쿠키 설정을 바꿔 서버를 띄웁니다(Spring 은 `server.servlet.session.cookie.domain` 을 `SERVER_SERVLET_SESSION_COOKIE_DOMAIN` 으로도 받습니다). 환경 변수 3개로 각각 한 번씩 띄워 로그인하고, 받은 쿠키로 `/auth/me` 를 불러 봅니다.

```bash
# 서버를 끄고, 아래 중 하나로 띄운다 (한 번에 하나씩, 세 번 모두 해 본다)
java -jar labs/backend/week25/build/libs/week25.jar
SERVER_SERVLET_SESSION_COOKIE_DOMAIN=example.com java -jar labs/backend/week25/build/libs/week25.jar
SERVER_SERVLET_SESSION_COOKIE_SECURE=true        java -jar labs/backend/week25/build/libs/week25.jar

# 다른 터미널에서
B=localhost:8080
rm -f /tmp/w13c.jar
curl -s -o /dev/null -D - -c /tmp/w13c.jar -X POST $B/auth/login -H 'Content-Type: application/json' -d '{"username":"demo","password":"demo1234"}' | grep -i -E '^HTTP|^Set-Cookie'
grep -v '^# ' /tmp/w13c.jar | grep -v '^$'
curl -s -o /dev/null -w '%{http_code}\n' -b /tmp/w13c.jar $B/auth/me
```

먼저 적으세요. 옵션 없이 띄운 경우, `Domain=example.com` 인 경우, `Secure` 인 경우에 마지막 줄의 상태 코드는 각각 무엇일까요?

<details>
<summary>실제 결과</summary>

| 서버 설정 | `Set-Cookie` | 쿠키 jar | `/auth/me` |
|---|---|---|---|
| 옵션 없음 | `JSESSIONID=...; Path=/; HttpOnly; SameSite=Lax` | 1줄 | 200 |
| `Domain=example.com` | `JSESSIONID=...; Domain=example.com; Path=/; HttpOnly; SameSite=Lax` | **비어 있음** | **401** |
| `Secure` | `JSESSIONID=...; Path=/; Secure; HttpOnly; SameSite=Lax` | 1줄 | 200 |

- `Domain=example.com` 인 쿠키는 `localhost` 에서 받았으므로 클라이언트가 **저장 자체를 거부**합니다. 로그인 응답은 200 인데 다음 요청은 401 이 됩니다. 응답 본문만 보면 "로그인이 성공했는데 왜 로그인이 필요하다고 하지?" 가 됩니다. 쿠키 도메인 설정이 환경과 맞지 않을 때의 전형적인 증상입니다.
- `Secure` 쿠키는 이 실험에서 **그대로 동작했습니다.** `curl` 은 `localhost` 를 예외로 취급해 http 에서도 `Secure` 쿠키를 돌려보냅니다. 즉 **로컬에서는 멀쩡한데, http 로 서비스되는 스테이징에서만 로그인이 안 풀리는** 문제를 개발 머신에서는 발견할 수 없습니다. 이 마지막 문장(http 의 비로컬 호스트에서 `Secure` 쿠키가 안 실린다)은 쿠키 표준의 설명이며 이 문서에서 직접 재현하지는 않았습니다.
- 환경 변수 `SERVER_SERVLET_SESSION_COOKIE_DOMAIN` 으로 `application.properties` 의 설정을 코드 수정 없이 바꿀 수 있다는 점도 확인되었습니다(3.2절).

</details>

#### 3-C. 환경 변수로 비밀번호 바꾸기

```bash
LIBRARY_DEMO_PASSWORD=s3cret java -jar labs/backend/week25/build/libs/week25.jar          # 서버 (다른 설정은 기본)
curl -s -o /dev/null -w '%{http_code}\n' -X POST localhost:8080/auth/login -H 'Content-Type: application/json' -d '{"username":"demo","password":"demo1234"}'
curl -s -o /dev/null -w '%{http_code}\n' -X POST localhost:8080/auth/login -H 'Content-Type: application/json' -d '{"username":"demo","password":"s3cret"}'
```

<details>
<summary>실제 결과</summary>

첫 번째는 `401`, 두 번째는 `200` 입니다. 같은 jar 가 환경 변수만 다르게 주면 다른 비밀번호로 동작합니다. 이 성질이 "한 번 만든 이미지를 여러 환경에서 쓴다" 는 Docker 사용법의 바탕입니다.

</details>

#### 3-D. 같은 jar 를 컨테이너로 띄우기 (선택, 30분~1시간)

3.1절의 "백엔드 이미지" 를 따라 `library-api` 이미지를 만들고 `docker run -d --name library-api -p 8090:8080 library-api` 로 띄운 뒤, 먼저 적으세요. 호스트에서 `curl localhost:8080/books` 와 `curl localhost:8090/books` 의 결과는 각각 어떨까요? 컨테이너 **안에서** `localhost:8090` 은 어떨까요?

<details>
<summary>실제 결과</summary>

호스트의 `8080` 은 연결 실패(`curl` 종료 코드 7, 상태 코드 `000`), `8090` 은 `200` 입니다. 컨테이너 안에서는 반대로 `8080` 이 `200`, `8090` 이 `000` 입니다. 포트 매핑 `-p 8090:8080` 의 왼쪽이 밖, 오른쪽이 안이라는 뜻입니다. 전체 출력은 3.1절에 있습니다.

</details>

### 4단계. `NEXT_PUBLIC_*` 가 언제 정해지는가 (30분~1시간)

이 단계는 프론트를 **빌드**하므로, 다른 사람이 같은 폴더에서 개발 서버를 쓰고 있지 않을 때 하세요. 먼저 예측합니다.

1. `NEXT_PUBLIC_API_BASE` 를 주지 않고 `pnpm build` 한 뒤, 브라우저로 내려가는 JS(`.next/static`)에서 이 변수를 찾으면 무엇이 보일까요? 빌드가 끝난 뒤 서버를 띄울 때 `NEXT_PUBLIC_API_BASE=http://localhost:8080 pnpm start` 로 값을 주면 브라우저 동작이 바뀔까요?
2. `next.config.ts` 의 `rewrites` 의 목적지(`http://localhost:8080`)는 빌드 때 정해질까요, 서버를 띄울 때 정해질까요?

```bash
cd labs/frontend
pnpm build
grep -rho 'NEXT_PUBLIC_API_BASE[^;]\{0,30\}' .next/static | sort -u
jq -c '.rewrites.afterFiles[] | {source,destination}' .next/routes-manifest.json
```

<details>
<summary>실제 결과</summary>

환경 변수를 주지 않은 빌드 산출물(`.next/`)에서 읽은 결과입니다.

```text
NEXT_PUBLIC_API_BASE||"/api",credentials:"include"
{"source":"/api/:path*","destination":"http://localhost:8080/:path*"}
```

- 값을 주지 않았으므로 번들 안에는 `process.env.NEXT_PUBLIC_API_BASE||"/api"` 라는 **식이 그대로 남아** 있고, 브라우저에는 `process.env` 에 그 변수가 없으므로 항상 `"/api"` 가 선택됩니다. 서버를 띄울 때(`pnpm start`) 환경 변수를 줘도 브라우저에 내려간 JS 는 이미 만들어진 파일이므로 달라지지 않습니다.
- `rewrites` 의 목적지 `http://localhost:8080` 은 `.next/routes-manifest.json` 에 **빌드 결과로 기록**되어 있습니다. 목적지를 바꾸려면 다시 빌드해야 합니다.
- `NEXT_PUBLIC_API_BASE` 에 **값을 주고** 빌드하면 번들이 어떻게 바뀌는지는 이 폴더의 `.next` 가 아니라 Docker 이미지 안에서 빌드해 확인했습니다. 결과는 3.1절의 "프론트 이미지" 에 있습니다. Next 공식 문서(`labs/frontend/node_modules/next/dist/docs/01-app/02-guides/environment-variables.md`)도 빌드 시점에 `process.env.NEXT_PUBLIC_*` 참조가 값으로 **치환(inline)** 된다고 설명합니다. 이 폴더에서 직접 비교하려면 `NEXT_PUBLIC_API_BASE=https://api.example.com pnpm build` 뒤에 위 `grep` 을 다시 실행하세요(`.next` 를 다른 사람이 쓰고 있다면 빌드 전에 합의하세요).

</details>

---

## 1. API 변경을 협의하는 법

### 1.1 변경은 "소비자가 깨지는가" 로 분류한다

백엔드 개발자에게 "이 필드 이름 바꿔도 돼?" 는 코드 한 줄짜리 질문이지만, 프론트에게는 **배포되어 사용자 손에 있는 앱이 깨지는가** 라는 질문입니다. 프론트는 서버처럼 한꺼번에 갱신되지 않습니다. 새 서버와 옛 프론트가 함께 떠 있는 시간이 반드시 있습니다. 그래서 변경을 두 가지로 나눕니다.

- **하위 호환(backward compatible) 변경:** 옛 프론트가 그대로 동작합니다. 먼저 서버를 배포해도 됩니다.
- **깨지는(breaking) 변경:** 옛 프론트가 오류를 내거나 조용히 잘못 동작합니다. 순서·기간·합의가 필요합니다.

방향에 따라 규칙이 반대가 됩니다.

- **응답(서버가 보내고 프론트가 읽는다):** 소비자는 모르는 필드를 무시하면 되므로 **추가는 안전하고, 삭제·이름 변경·타입 변경이 위험합니다.**
- **요청(프론트가 보내고 서버가 읽는다):** 서버는 모르는 필드를 무시하고 누락된 필수 필드를 거부하므로 **선택 필드 추가는 안전하고, 필수 필드 추가·삭제가 위험합니다.** 1단계에서 본 대로 이 서버는 모르는 필드를 조용히 버립니다.

### 1.2 분류표

1단계에서 이 저장소로 측정한 값을 함께 적었습니다. "오류 개수" 는 호환 여부의 증거가 아니라 **이 저장소의 호출 코드가 우연히 걸린 개수**입니다.

| 변경 | 응답(서버→프론트) | 요청(프론트→서버) | 1단계 오류 개수 | 비고 |
|---|---|---|---|---|
| 선택 필드 추가 | 호환 | 호환 | 0 (a, g) | 서버가 모르는 요청 필드를 버리는지는 서버 설정에 달림 |
| **필수** 필드 추가 | 호환 | **깨짐** | 응답 4 (b, 가짜 서버·테스트 데이터만), 요청 5 (h) | 응답 쪽은 소비자 쪽 목업·테스트 데이터가 걸림 |
| 필드 삭제 | **깨짐** | 대개 호환(서버가 안 읽으면). 프론트가 계속 보내면 버려짐 | 응답 6 (c) | 요청 쪽은 "보냈는데 안 쓰임" 을 아무도 모름 |
| 필드 이름 변경 | **깨짐** (삭제+추가) | **깨짐** | 11 (`title` → `name` 을 스펙 사본으로 확인) | 한 번에 바꾸지 말고 새 이름을 추가 → 옛 이름 deprecated → 제거 |
| 타입 변경 (정수→문자열 등) | **깨짐** | **깨짐** | 6 (d) | 같은 JSON 값 표기여도 의미가 바뀔 수 있음 |
| nullable 로 변경 | **깨짐** | 호환 | **0** (e) | 컴파일러가 못 잡을 수 있음 |
| 필수를 선택으로 변경 | **깨짐** | 호환 | **0** (f) | 응답에서 "키가 없을 수 있다" 가 됨 |
| 선택을 필수로 변경 | 호환 | **깨짐** | | 요청에서 누락하던 호출이 400 |
| enum 값 추가 | **합의 필요**(소비자의 분기 방식에 따라 깨짐) | 호환 | 1 (enum 실험, exhaustive 검사가 있을 때) | 응답 enum 은 "알 수 없는 값" 처리 방침을 합의 |
| enum 값 삭제 | 호환 | **깨짐** | | 그 값을 보내던 호출이 400 |
| 엔드포인트 추가 | 호환 | 호환 | 0 (j) | |
| 엔드포인트 삭제·경로 변경 | **깨짐** | **깨짐** | 11 (i) | |
| 새 오류 응답 코드 추가 | 대체로 호환 | | 0 (k) | 7주의 `unknown` 분기가 처리 방침이 되어야 함 |
| 성공 코드 변경 (201→200) | 대체로 호환 | | 0 | `status === 201` 처럼 정확히 비교하는 코드는 깨짐 |
| 필드의 **의미·형식** 변경 (단위, 날짜 형식, 정렬 순서) | **깨짐** | **깨짐** | **0** (타입이 그대로) | 스펙의 `description` 변경도 변경 요청서에 적어야 함 |

### 1.3 컴파일러가 못 잡는 변경

6주에서 만든 파이프라인(스펙 → 타입 → `pnpm typecheck`)은 강력하지만 다음은 잡지 못합니다. 변경 요청서 단계에서 사람이 잡아야 합니다.

- nullable·optional 변경이 **우연히 안전한 사용처**에서만 쓰인 경우(1단계 e, f). 오류 개수가 0 이라고 호환이 아닙니다.
- 의미·형식·단위 변경. 예를 들어 `loanedAt` 이 ISO 8601 문자열에서 epoch 초로 바뀌어도 둘 다 `string` 또는 `number` 라서 타입이 같으면 통과합니다(여기서는 `string` → `number` 면 걸리지만 `string` → `string` 형식 변경은 안 걸립니다).
- 스펙을 갱신하지 않은 채 서버 코드만 바뀐 경우. 서버가 지금 내놓는 스펙과 커밋된 `openapi.json` 을 비교하는 검사가 필요합니다.
- 서버가 **모르는 요청 필드를 무시**하는 경우. 프론트는 보냈다고 믿는데 서버는 쓰지 않습니다.

### 1.4 Deprecation: 지우기 전에 알린다

깨지는 변경을 호환 변경 두 단계로 쪼개는 방법입니다. 필드 이름을 `title` 에서 `name` 으로 바꾼다면 이렇게 합니다.

1. 서버가 `name` 을 **추가**하고 `title` 도 계속 내려줍니다(둘 다 같은 값). 호환 변경입니다.
2. 스펙에서 `title` 에 `deprecated: true` 를 표시합니다. 제거 예정일을 합의합니다.
3. 프론트가 `name` 으로 옮깁니다. 서버는 `title` 을 부르는 요청이 사라졌는지 접근 로그나 지표로 확인합니다.
4. 합의한 날짜 뒤에 서버가 `title` 을 **삭제**합니다.

스펙에 `deprecated: true` 를 표시하면 6주의 타입 생성기가 이를 생성된 타입에 그대로 반영합니다. `GET /books` 와 `BookResponse.isbn` 에 `deprecated: true` 를 넣고 `pnpm gen:api` 를 실행했을 때 `schema.d.ts` 에 실제로 이렇게 나왔습니다.

```ts
/** @deprecated */
get: operations["search"];
...
            id: number;
            /** @deprecated */
            isbn: string;
```

편집기는 이 표시가 붙은 필드를 취소선으로 보여 줍니다(편집기 표시는 이 문서에서 직접 확인하지 못했습니다). 그리고 이때도 `pnpm typecheck` 오류는 0 개였습니다. **deprecated 는 컴파일러를 깨뜨리지 않고 사람에게만 알리는 표시**입니다. 그래서 제거일을 정하는 합의가 따로 필요합니다.

서버가 응답 헤더로도 알릴 수 있습니다. `Sunset` 헤더(RFC 8594)는 "이 엔드포인트가 사라지는 시각", `Deprecation` 헤더는 "이미 사용 중단 상태" 를 뜻하도록 표준화되어 있습니다. 참조 API 는 이 헤더를 내지 않으며, 이 절은 개념 설명입니다.

### 1.5 버전: URL, 헤더, 그리고 "버전 없이 추가만"

깨지는 변경을 피할 수 없을 때 옛 계약과 새 계약을 동시에 서비스하는 방법입니다. 참조 API 는 버전이 없습니다. 아래는 개념 비교입니다.

| | URL 버전 (`/v2/books`) | 헤더 버전 (`Accept: application/vnd.library.v2+json` 또는 `API-Version: 2`) | 버전 없이 추가만 |
|---|---|---|---|
| 프론트에서 | `baseUrl` 에 `/v2` 를 붙이면 끝. `src/api/client.ts` 의 `createClient({ baseUrl })` 한 곳 | 모든 요청에 헤더를 붙이는 미들웨어 필요. 사용자 정의 헤더는 CORS 허용 헤더에 등록해야 함(3-A) | 신경 쓸 것 없음 |
| 눈에 보임 | DevTools·로그·`curl` 한 줄에서 바로 보임 | 헤더를 열어야 보임 | |
| 캐시·프록시 | 경로가 다르니 자연스럽게 분리 | `Vary` 헤더를 서버가 정확히 내야 함 | |
| 스펙 | 버전마다 `openapi.json` 하나씩 → 타입 생성도 둘 | 같은 경로에 버전별 분기를 스펙에 표현하기 어려움 | 스펙 하나 |
| 단점 | 옛 버전을 서버가 오래 같이 유지해야 함 | 디버깅할 때 헤더를 빠뜨리기 쉬움 | 깨지는 변경은 결국 못 함 |

실무 기본값은 "**호환 변경만 하고 버전을 안 올리다가, 어쩔 수 없을 때 URL 버전**" 입니다. 이 도서관 API 처럼 소비자가 우리 프론트 하나뿐이라면 deprecation 절차(1.4)만으로 대부분 해결되고, 모바일 앱처럼 배포를 통제할 수 없는 소비자가 있을 때 버전이 필요해집니다.

### 1.6 변경 요청서 템플릿

변경을 제안하는 쪽(보통 백엔드, 때로는 프론트)이 채워서 이슈나 PR 설명에 붙입니다. 아래는 가상의 요청을 채운 예입니다(참조 API 에는 `status` 가 없습니다).

```markdown
## API 변경 요청: 책 응답에 대출 상태(status) 추가

- 요청자 / 날짜: 홍길동(백엔드) / 2026-10-22
- 배경(왜): 목록 화면에서 책마다 대출 중인지 알려면 지금은 대출 목록 API 를 따로 불러 대조해야 함

### 변경 내용
| | 전 | 후 |
|---|---|---|
| `GET /books` 응답의 `BookResponse` | `{id, isbn, title, author}` | `{id, isbn, title, author, status}` |
| `status` | (없음) | 문자열 enum: `AVAILABLE`, `ON_LOAN` |

### 분류
- [x] 응답에 필드 추가 -> 호환
- [x] enum 값을 새로 도입 -> 이후 값 추가는 "합의 필요" 변경으로 취급 (아래 합의 사항 참조)
- [ ] 깨지는 변경 없음

### 영향 범위
- 프론트: `BookList` 에서 `status` 를 표시. MSW 가짜 서버(`handlers.ts`)의 `Book` 객체에 `status` 추가 필요 (필수 필드이므로 타입 오류로 드러남)
- 모바일/다른 소비자: 없음

### 합의 사항
- 알 수 없는 enum 값을 받으면 프론트는 "상태 알 수 없음" 으로 표시한다 (서버가 값을 늘릴 때 프론트 배포를 기다리지 않아도 됨)
- 스펙의 `status` 에 `@Schema(requiredMode = REQUIRED)` 를 적용한다 (필수 표시를 빼먹으면 `status?:` 로 생성되어 모든 사용처에 `undefined` 가능성이 번진다)

### 일정 / 롤백
- 스펙 갱신 PR: 10/23, 프론트 대응 PR: 10/24, 서버 배포: 프론트 배포 뒤
- 롤백: 필드 추가만이므로 서버만 되돌리면 됨 (프론트는 `status` 가 없으면 "알 수 없음")

### 확인 요청
- 프론트: 위 합의 사항에 동의하는지, `openapi.json` 갱신 후 `pnpm gen:api && pnpm typecheck` 결과 공유
```

깨지는 변경(예: `title` → `name`)은 같은 양식에 "deprecation 기간", "제거일", "옛 필드를 아직 쓰는 호출 수(접근 로그 기준)" 줄을 추가합니다.

## 2. 장애가 났을 때 로그와 요청 ID 읽기

### 2.1 프론트가 모아서 전달할 재현 정보

"로그인이 안 돼요" 는 제보가 아니고 감상입니다. 백엔드 개발자가 로그에서 그 요청을 찾으려면 아래 정보가 필요합니다. 대부분 DevTools 네트워크 탭의 실패한 요청을 우클릭해 **Copy as cURL** 로 한 번에 복사할 수 있습니다(1주). 단, 복사한 `curl` 에는 쿠키나 `Authorization` 헤더에 **실제 세션 값이 들어 있으므로** 공유 전에 지웁니다.

| 항목 | 왜 필요한가 | 어디서 얻나 |
|---|---|---|
| 환경 | 어느 서버의 로그를 볼지 | 프론트 주소(dev/staging/prod), 빌드 버전 |
| 시각(타임존 포함) | 로그를 시각으로 좁힘 | 응답 `Date` 헤더(GMT) 또는 `new Date().toISOString()` (UTC, `Z`). 서버 로그는 `+0900` 일 수 있음(2.4) |
| 메서드 + URL (쿼리 포함) | 어느 엔드포인트인지 | 네트워크 탭의 Headers |
| 요청 본문 | 검증 오류(400)·충돌(409)의 원인 | Payload 탭. 비밀번호 등은 가림 |
| 상태 코드 | 서버가 판단한 결과 | Headers |
| **응답 본문 원문** | 오류 문장은 로그에 없고 본문에만 있음(2-B) | Response 탭에서 **그대로 복사**. 요약하지 않음 |
| 상관 ID (`X-Request-Id` 등) | 로그를 한 번에 찾음 | 요청 또는 응답 헤더. 있을 때만 |
| 로그인 상태 | 401/403 의 단서 | "로그인했다/안 했다" 와 시점. 쿠키 **값**은 붙이지 않음 |
| 재현 절차와 빈도 | 같은 증상을 다시 만들기 위해 | 항상/가끔, 어떤 입력에서 |
| 직전 변경 | 원인 후보를 줄임 | 최근 배포·스펙 변경 |

### 2.2 상관 ID(correlation ID)

한 요청이 프론트, 프록시, 백엔드, DB 를 거치는 동안 같은 문자열을 로그에 남기면, 그 문자열 하나로 모든 곳의 로그를 찾을 수 있습니다. 이 문자열이 상관 ID 입니다. 이름은 조직마다 다르며, 대표적인 것이 `X-Request-Id`(관례)와 W3C 의 `traceparent` 헤더(표준 분산 추적)입니다.

**참조 API 에는 이 기능이 없습니다.** 직접 확인한 사실입니다.

- 요청에 `X-Request-Id` 를 붙여도 응답 헤더로 돌아오지 않습니다(2-B 질문 1).
- 기본 설정에서는 이 헤더를 어떤 로그에도 남기지 않습니다. 2-B 처럼 접근 로그 패턴에 `%{X-Request-Id}i` 를 적어 넣어야 로그에 보입니다.
- 서버 CORS 설정의 허용 헤더가 `Content-Type` 하나라서, 브라우저가 프론트에서 직접 이 헤더를 보내려면 서버 설정을 먼저 바꿔야 합니다(3-A). 프록시(`/api`) 경유라면 CORS 와 무관합니다.

따라서 현실적인 합의는 이런 형태입니다. "프론트는 요청마다 `X-Request-Id` 를 만들어 보낸다. 백엔드(또는 앞단 프록시)는 받은 값을 로그에 남기고 응답 헤더에도 그대로 되돌려 준다. 값이 없으면 서버가 새로 만든다." 이것은 **이 저장소에 구현된 것이 아니라 협의할 대상**입니다.

프론트 쪽 구현은 `openapi-fetch` 의 미들웨어 하나입니다. 아래 코드는 `pnpm typecheck` 를 통과하고, `fetch` 를 가짜로 바꾼 Node 스크립트로 요청마다 서로 다른 UUID 가 헤더에 실리고 실패 응답에서 `console.warn` 이 한 줄 나오는 것을 확인했습니다(브라우저 CORS 동작은 확인하지 않았습니다).

```ts
import createClient, { type Middleware } from "openapi-fetch";
import type { paths } from "@/api/schema";

const trace: Middleware = {
  onRequest({ request }) {
    request.headers.set("X-Request-Id", crypto.randomUUID());
    return request;
  },
  onResponse({ request, response }) {
    if (!response.ok) {
      const { pathname } = new URL(request.url);
      console.warn(`[api] ${request.method} ${pathname} -> ${response.status}`, request.headers.get("X-Request-Id"), new Date().toISOString());
    }
  },
};

export const client = createClient<paths>({ baseUrl: "http://localhost:8080", credentials: "include" });
client.use(trace);
```

실행하면 실패 응답마다 이런 한 줄이 나옵니다(UUID 와 시각은 실행마다 다릅니다).

```text
[api] POST /books -> 409 288225f7-114d-4df7-893d-2005b8409fb4 2026-10-22T13:52:03.774Z
```

이 줄을 제보에 그대로 붙이면 "무슨 요청이, 어느 시각(UTC)에, 어떤 ID 로" 가 한 번에 전달됩니다. `src/api/client.ts` 의 `api` 에 `api.use(trace)` 를 붙이는 방식으로 쓰려면 위 합의와 서버 CORS 허용 헤더 변경이 먼저 필요하다는 점을 기억하세요.

### 2.3 참조 API 의 로그는 무엇을 남기는가

2단계에서 직접 확인한 사실을 정리합니다.

| 상황 | 기본 설정 | 켜면 |
|---|---|---|
| 서버 시작 | 설정·DB·포트 같은 `INFO` 줄 (예: `Tomcat started on port 8080 (http) with context path '/'`) | |
| 정상 처리된 모든 요청(200, 201) | 기록 없음 | 접근 로그(`server.tomcat.accesslog.enabled=true`) 또는 `DispatcherServlet` DEBUG |
| `ApiExceptionHandler` 가 바꾼 4xx (400, 401, 404, 409) | 기록 없음 | 위와 같음 |
| 프레임워크 기본 처리 (405, 415) | `WARN ... DefaultHandlerExceptionResolver : Resolved [...]` 한 줄 | |
| 존재하지 않는 경로 (404) | 기록 없음 | |
| 응답 본문의 오류 문장 | 어디에도 없음 | 어디에도 없음 |

이 표의 의미는 분명합니다. **기본 설정의 로그는 "서버가 죽었는가" 를 알려 주지 "누가 무엇을 했는가" 를 알려 주지 않습니다.** 프론트가 제보에 시각과 요청 정보를 정확히 적어야 하는 이유이고, 백엔드에게 "접근 로그를 켜 달라" 고 요청하는 것이 정당한 협업 요청인 이유입니다. 참조 API 에서 500 응답을 만드는 요청은 쉽게 재현되지 않아서 500 의 로그 예시는 싣지 않았습니다. 500 은 보통 위와 달리 `ERROR` 줄과 예외 스택 트레이스가 서버 로그에 남는다는 점만 개념으로 알아 두세요.

### 2.4 로그 한 줄 읽기

애플리케이션 로그(Spring Boot 기본 형식)의 한 줄을 구성 요소로 나눕니다.

```text
2026-10-22T22:48:44.573+09:00 DEBUG 74338 --- [nio-8083-exec-5] o.s.web.servlet.DispatcherServlet        : Completed 409 CONFLICT
|---------------------------| |---| |---|     |--------------| |------------------------------------------|   |-----------------------|
        시각 + 오프셋          레벨   프로세스    스레드 이름       로거 이름(40자로 줄임)                      메시지
```

- **시각:** `+09:00` 은 서버 머신의 시간대입니다. 응답의 `Date: Thu, 22 Oct 2026 13:48:44 GMT` 와 같은 순간입니다. 프론트가 `toISOString()` 으로 찍은 값(`...Z`)도 UTC 입니다. 세 표기가 모두 같은 순간을 가리키는지 먼저 계산하세요.
- **레벨:** `ERROR` > `WARN` > `INFO` > `DEBUG`. 기본은 `INFO` 이상만 나옵니다.
- **프로세스 번호:** 같은 서버를 여러 대 띄운 환경에서 어느 프로세스의 로그인지 구분합니다.
- **스레드 이름:** `http-nio-8083-exec-5` 가 15자로 앞이 잘려 `nio-8083-exec-5` 가 됩니다. 한 요청은 한 스레드에서 처리되므로 **같은 스레드 이름·가까운 시각의 줄들이 한 요청**입니다.
- **로거 이름:** 어느 클래스가 남겼는지입니다. `DispatcherServlet` 은 스프링 MVC 의 진입점, `DefaultHandlerExceptionResolver` 는 프레임워크의 기본 오류 처리기입니다. 예외 클래스 이름(`HttpMediaTypeNotSupportedException` 등)이 메시지에 있으면 4주에서 본 컨트롤러·예외 처리 코드에서 바로 찾을 수 있습니다.

접근 로그(Tomcat) 한 줄입니다. 패턴은 2-B 에서 우리가 지정한 것이라 서버마다 다릅니다.

```text
[22/Oct/2026:22:48:44 +0900] "POST /books HTTP/1.1" 409 63 5ms rid=fe-20261022-0001
 시각 (+0900)                  메서드 경로 프로토콜    상태 응답바이트 처리시간 상관 ID(요청 헤더)
```

### 2.5 응답 본문의 모양이 `{"message"}` 가 아닌 경우

7주의 `ApiError` 는 오류 본문이 항상 `{"message": "..."}` 라고 가정합니다. 컨트롤러까지 가서 `ApiExceptionHandler` 가 처리한 오류는 맞습니다. 하지만 **컨트롤러에 닿기 전에 프레임워크가 만든 오류**는 모양이 다릅니다. 직접 확인한 응답입니다.

```text
GET  /nope               -> 404  {"timestamp":"2026-10-22T13:43:28.050+00:00","status":404,"error":"Not Found","path":"/nope"}
PUT  /books              -> 405  {"timestamp":"2026-10-22T13:43:28.070+00:00","status":405,"error":"Method Not Allowed","path":"/books"}
                                 (Allow: GET, POST)
POST /auth/login (text/plain) -> 415  {"timestamp":"2026-10-22T13:43:27.689+00:00","status":415,"error":"Unsupported Media Type","path":"/auth/login"}
```

`src/api/client.ts` 의 `messageOf` 는 `message` 문자열이 없는 본문을 `요청에 실패했습니다 (404)` 로 바꿉니다. 화면에는 안전하지만 **원문 정보를 잃습니다.** 장애 제보에는 화면 문구가 아니라 네트워크 탭의 응답 본문 원문을 붙이세요. 위 세 종류는 "경로·메서드·Content-Type 이 계약과 다르다" 는 프론트 쪽 실수인 경우가 많습니다(`path` 와 `error` 필드가 단서입니다). 반대로 `{"message": "..."}` 모양은 서버의 업무 규칙 판단입니다. 모양만 봐도 어느 쪽 문제인지 1차 구분이 됩니다.

### 2.6 재현 보고서 템플릿

```markdown
## 장애 제보: 같은 isbn 으로 책 등록이 409

- 환경 / 프론트 빌드: local (http://localhost:3000, 백엔드 직접 호출 아님: /api 프록시 경유)
- 발생 시각: 2026-10-22T13:48:44Z (= 22:48:44 +09:00)  <- 응답 Date 헤더 기준
- 요청: POST /books
- 요청 헤더: X-Request-Id: fe-20261022-0001, 로그인 상태(세션 쿠키 있음)
- 요청 본문: {"isbn":"978-1","title":"Clean Code","author":"Martin"}
- 응답: 409, {"message":"이미 등록된 isbn 입니다: 978-1"}
- 기대한 결과: 신규 isbn 이라 201 일 것으로 생각함
- 재현: 항상 (같은 isbn 을 두 번 등록)
- 서버 로그에서 확인 부탁: 접근 로그 rid=fe-20261022-0001, 또는 22:48:44 전후 POST /books
```

이 예시는 정상 동작(중복 isbn 은 409)이지만, 보고서의 모양은 실제 장애에서도 같습니다. 중요한 것은 백엔드가 **질문을 되묻지 않고 곧바로 로그를 검색**할 수 있는가입니다.

## 3. 배포 환경이 달라지면

### 3.1 Docker 개념

**TS 와 비교** (비유이므로 완전히 같지는 않습니다)

| 백엔드·배포 용어 | 프론트에서 비슷한 것 |
|---|---|
| 이미지(image) | `pnpm build` 결과물 + 실행에 필요한 모든 것을 얼려 놓은 것. 불변이고 버전(태그)을 가짐 |
| 컨테이너(container) | 이미지를 실행한 프로세스. `node server.js` 가 떠 있는 상태 |
| `Dockerfile` | 이미지를 만드는 스크립트. `package.json` 의 `build` 스크립트와 비슷하게 위에서 아래로 실행 |
| 포트 매핑 `-p 8090:8080` | 호스트 포트 `8090` 을 컨테이너 안 `8080` 으로 연결. Vite 의 `server.proxy` 와 비슷하지만 **왼쪽이 밖, 오른쪽이 안** |
| 환경 변수 (`-e KEY=value`) | `process.env`·`.env`. Spring 은 `application.properties` 값을 환경 변수로 덮어씀 |
| `localhost` (컨테이너 안) | 컨테이너 **자기 자신**. 다른 컨테이너나 호스트가 아님 |
| `docker compose` | 여러 컨테이너(프론트·백엔드·DB)를 한 파일로 정의. 서비스 이름이 호스트 이름이 됨 (개념 설명이며 실행해 확인하지 않음) |
| 변경 요청서의 "breaking change" | semver 의 major 올림 |
| `deprecated: true` | JSDoc 의 `@deprecated` (실제로 생성된 타입에 그대로 옮겨짐) |
| 상관 ID | 한 요청에 붙이는 이벤트 ID. 에러 추적 도구(Sentry 등)의 이벤트 ID 와 같은 용도 |
| 접근 로그 | 서버 쪽 DevTools 네트워크 탭 |

개념 세 가지만 기억하면 됩니다.

1. **이미지는 한 번 만들고, 환경마다 컨테이너를 따로 띄운다.** 같은 이미지가 dev/staging/prod 에서 환경 변수만 다르게 돌아가는 것이 이상적입니다.
2. **컨테이너 안은 격리된 네트워크다.** 컨테이너 안에서 `localhost:8080` 은 그 컨테이너 자신이므로, 프론트 컨테이너가 백엔드 컨테이너를 부르려면 `http://backend:8080` 처럼 compose 서비스 이름이나 실제 주소를 써야 합니다. `rewrites` 의 목적지 `http://localhost:8080` 은 이 점에서 컨테이너에서는 동작하지 않습니다.
3. **포트 매핑은 밖에서 안으로 단방향 연결이다.** `-p 8090:8080` 은 "밖의 8090 으로 오는 요청을 컨테이너의 8080 으로" 입니다.

**직접 실행해 확인한 부분:** 이 절의 명령과 출력은 Apple Silicon 맥(`uname -m` 이 `arm64`)에서 Colima(CPU 2개, 메모리 2GiB)와 `docker` 29.9.2 로 실제 실행한 결과입니다. 받는 이미지가 `linux/arm64` 이므로 `--platform linux/amd64` 는 쓰지 않았습니다(이미지가 arm64 를 지원하지 않을 때만 필요합니다). 이 머신의 호스트 포트 `8081` 은 다른 프로세스가 쓰고 있어서, 예시는 바깥 `8090` 에서 안 `8080` 으로 매핑합니다. 명령은 별도 언급이 없으면 저장소 루트에서 실행합니다. `docker compose` 는 `docker compose version` 으로 설치만 확인했고(v5.6.1) 실행해 보지 않았으므로 위 표의 설명은 **개념**입니다.

#### 준비: Colima 와 docker context

```bash
docker --version
docker context ls
docker context use colima
docker run --rm hello-world
```

```text
Docker version 29.9.2, build a82e528901
NAME              DESCRIPTION                               DOCKER ENDPOINT                                     ERROR
colima            colima                                    unix:///Users/haseung/.colima/default/docker.sock
default           Current DOCKER_HOST based configuration   unix:///var/run/docker.sock
desktop-linux *   Docker Desktop                            unix:///Users/haseung/.docker/run/docker.sock
colima
Current context is now "colima"

Hello from Docker!
This message shows that your installation appears to be working correctly.
```

- `*` 가 현재 context 입니다. Docker Desktop 을 쓰다가 Colima 로 옮긴 환경에서는 `desktop-linux` 가 현재로 남아 있어 `docker` 명령이 Colima 에 닿지 않습니다. `docker context use colima` 는 `~/.docker/config.json` 에 `"currentContext": "colima"` 를 기록하고, 이후 `docker context ls` 에서 `colima *` 가 됩니다. 이 문서를 쓴 머신에서는 사용자 설정을 바꾸지 않으려고 `~/.docker` 를 다른 디렉터리에 복사하고 환경 변수 `DOCKER_CONFIG` 로 가리킨 뒤 위 명령을 실행해 확인했습니다. 독자의 실제 `~/.docker` 에서 실행한 것은 아닙니다.
- 이미지를 받을 때 아래 오류가 나면 `~/.docker/config.json` 의 `"credsStore": "desktop"` 이 원인입니다. Docker Desktop 의 credential helper(`docker-credential-desktop`)를 찾지 못한다는 뜻입니다.

```text
error getting credentials - err: exec: "docker-credential-desktop": executable file not found in $PATH, out: ``
```

이 오류는 `credsStore` 가 `desktop` 인 설정에서 helper 를 `PATH` 에서 숨기고(`PATH=/opt/homebrew/bin:/usr/bin:/bin docker pull alpine:3.20`) 재현했습니다. 이 머신에는 Docker Desktop 이 아직 설치되어 있어 평소에는 같은 설정으로도 pull 이 성공하므로, 오류는 Docker Desktop 을 지운 환경에서 나는 것입니다. 같은 설정 파일에서 `credsStore` 줄만 지우자 같은 `docker pull alpine:3.20` 이 성공했습니다. 줄을 지울 때 앞 줄 끝의 쉼표 때문에 JSON 이 깨지지 않게 주의하세요. 다른 방법은 `brew install docker-credential-helper` 로 helper 를 설치하고 `credsStore` 를 `osxkeychain` 으로 바꾸는 것인데, 이 머신에는 `docker-credential-osxkeychain` 이 이미 있어서 `brew install` 은 실행하지 않았고, `credsStore` 를 `osxkeychain` 으로 바꾼 설정에서 pull 이 성공하는 것만 확인했습니다.

#### 백엔드 이미지

Dockerfile 은 [`labs/docker/backend.Dockerfile`](../labs/docker/backend.Dockerfile) 입니다. `labs/backend/week25` 안에는 아무것도 추가하지 않고, jar 가 들어 있는 폴더 `labs/backend/week25/build/libs` 만 빌드 컨텍스트로 씁니다. 컨텍스트가 작아야 `docker build` 가 빨라지고 `node_modules` 같은 파일이 이미지로 새어 들어가지 않습니다.

```dockerfile
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY week25.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

jar 가 없으면 `cd labs/backend && ./gradlew :week25:bootJar` 로 먼저 만듭니다.

```bash
docker build -t library-api -f labs/docker/backend.Dockerfile labs/backend/week25/build/libs
docker run -d --name library-api -p 8090:8080 library-api
docker ps
docker logs library-api
```

```text
Sending build context to Docker daemon  62.47MB
...
Successfully tagged library-api:latest
CONTAINER ID   IMAGE         COMMAND               CREATED          STATUS          PORTS                                         NAMES
8b9989b84d0d   library-api   "java -jar app.jar"   23 seconds ago   Up 23 seconds   0.0.0.0:8090->8080/tcp, [::]:8090->8080/tcp   library-api
```

- 빌드 컨텍스트가 `62.47MB`(jar 한 개)입니다. 이 출력은 옛 빌더의 `Step 1/5 ...` 형태입니다. buildx(BuildKit)가 설치된 환경에서는 출력 모양이 다릅니다.
- `PORTS` 열의 `0.0.0.0:8090->8080/tcp` 가 포트 매핑입니다. 컨테이너 ID 는 실행할 때마다 다릅니다.
- 이 머신(CPU 2개 VM)에서 Spring 이 뜨는 데 20초가 걸렸습니다(`Started LibraryApplication in 20.785 seconds`). 띄운 직후 8초 시점에는 `curl` 이 실패했고, `docker logs library-api` 에 이 줄이 보인 뒤에는 성공했습니다. "컨테이너가 Up" 과 "서버가 요청을 받는다" 는 다릅니다.

이제 호스트에서 같은 API 를 부릅니다. 로그인과 쿠키 흐름은 8주와 같습니다. 쿠키 jar 경로 `/tmp/w13d.jar` 는 예시입니다.

```bash
B=localhost:8090
curl -s $B/books
curl -si -X POST $B/books -H 'Content-Type: application/json' -d '{"isbn":"1","title":"t","author":"a"}' | sed -n '1p;$p'
curl -si -c /tmp/w13d.jar -X POST $B/auth/login -H 'Content-Type: application/json' -d '{"username":"demo","password":"demo1234"}' | sed -n '1p;/^Set-Cookie/p;$p'
curl -s -b /tmp/w13d.jar $B/auth/me
curl -si -b /tmp/w13d.jar -X POST $B/books -H 'Content-Type: application/json' -d '{"isbn":"978-1","title":"Clean Code","author":"Martin"}' | sed -n '1p;$p'
curl -s $B/books
```

```text
[]
HTTP/1.1 401 
{"message":"로그인이 필요합니다"}
HTTP/1.1 200 
Set-Cookie: JSESSIONID=9DE48F316792B54C40269533067394A7; Path=/; HttpOnly; SameSite=Lax
{"username":"demo"}
{"username":"demo"}
HTTP/1.1 201 
{"id":1,"isbn":"978-1","title":"Clean Code","author":"Martin"}
[{"id":1,"isbn":"978-1","title":"Clean Code","author":"Martin"}]
```

jar 를 `java -jar` 로 직접 띄웠을 때와 응답이 같습니다. 컨테이너는 그저 같은 프로세스를 격리해서 실행한 것입니다. 이제 로그를 읽습니다(2절의 2-A 와 같은 내용입니다). 서버가 기록하는 요청은 `Content-Type` 이 틀린 요청 정도이므로, 일부러 하나 보냅니다.

```bash
curl -s -o /dev/null -w '%{http_code}\n' -X POST $B/auth/login -H 'Content-Type: text/plain' -d x
docker logs library-api 2>&1 | tail -1
```

```text
415
2026-10-23T13:49:04.057Z  WARN 1 --- [nio-8080-exec-5] .w.s.m.s.DefaultHandlerExceptionResolver : Resolved [org.springframework.web.HttpMediaTypeNotSupportedException: Content-Type 'text/plain;charset=UTF-8' is not supported]
```

- 앞에서 보낸 401·201 요청은 로그를 남기지 않았고(`docker logs` 의 마지막 두 줄이 첫 요청 때 찍히는 `Completed initialization in 3 ms` 줄과 이 줄이었습니다), 415 한 줄만 새로 생겼습니다. 2-A 의 결과와 같습니다.
- 시각 끝의 `Z` 는 UTC 입니다. 컨테이너의 시간대가 UTC 라서, 호스트에서 jar 를 직접 띄운 로그의 `+09:00` 과 9시간 차이로 보입니다(2.4절). 프로세스 번호가 `1` 인 것은 컨테이너 안에서 `java` 가 첫 프로세스이기 때문입니다. `nio-8080-exec-5` 의 `8080` 은 컨테이너 **안**의 포트입니다.

환경 변수로 비밀번호를 바꾸는 것(3-C)은 `-e` 로 합니다. 이름이 같은 컨테이너를 먼저 지워야 합니다.

```bash
docker stop library-api && docker rm library-api
docker run -d --name library-api -p 8090:8080 -e LIBRARY_DEMO_PASSWORD=s3cret library-api
for p in demo1234 s3cret; do curl -s -o /dev/null -w "$p -> %{http_code}\n" -X POST localhost:8090/auth/login -H 'Content-Type: application/json' -d "{\"username\":\"demo\",\"password\":\"$p\"}"; done
```

```text
library-api
library-api
demo1234 -> 401
s3cret -> 200
```

같은 이미지가 환경 변수만 다르게 받아 다른 비밀번호로 동작합니다. 3-C 의 `java -jar` 결과와 같습니다.

#### 포트 매핑의 방향과 컨테이너 안의 localhost

위 컨테이너(`-p 8090:8080`)가 떠 있을 때, 바깥과 안에서 각각 두 포트를 불러 봅니다. 이 이미지(`eclipse-temurin:21-jre`)에는 `curl` 과 `wget` 이 모두 있는 것을 `docker exec library-api which curl wget` 으로 먼저 확인했습니다(`/usr/bin/curl`, `/usr/bin/wget`). 바깥 `8080` 확인은 호스트의 `8080` 이 비어 있을 때만 의미가 있습니다.

```bash
curl -s -o /dev/null -w '바깥 8090: %{http_code}\n' localhost:8090/books
curl -s -o /dev/null -w '바깥 8080: %{http_code}\n' localhost:8080/books
docker exec library-api curl -s -o /dev/null -w '안 8080: %{http_code}\n' localhost:8080/books
docker exec library-api curl -s -o /dev/null -w '안 8090: %{http_code}\n' localhost:8090/books
docker port library-api
```

```text
바깥 8090: 200
바깥 8080: 000
안 8080: 200
안 8090: 000
8080/tcp -> 0.0.0.0:8090
8080/tcp -> [::]:8090
```

`000` 은 연결 자체가 실패했다는 뜻입니다. 바깥에서는 `8090` 만, 안에서는 `8080` 만 열려 있습니다. `docker port` 도 "컨테이너의 `8080/tcp` 가 호스트 `8090` 에 연결됨" 이라고 말합니다. 왼쪽이 밖, 오른쪽이 안입니다.

"컨테이너 안의 `localhost` 는 컨테이너 자기 자신" 도 반증할 수 있는 실험으로 확인합니다. 다른 컨테이너(`alpine:3.20`)에서 `localhost:8080` 을 부르면 실패하지만, 네트워크를 `library-api` 와 **공유**(`--network container:library-api`)하면 같은 `localhost` 가 `library-api` 를 가리킵니다. `host.docker.internal` 은 호스트를 가리킵니다(Colima 에서 확인).

```bash
docker run --rm alpine:3.20 wget -qO- -T 3 http://localhost:8080/books
docker run --rm --network container:library-api alpine:3.20 wget -qO- http://localhost:8080/books
docker run --rm alpine:3.20 wget -qO- -T 5 http://host.docker.internal:8080/books
```

```text
wget: can't connect to remote host: Connection refused
[{"id":1,"isbn":"978-1","title":"Clean Code","author":"Martin"}]
[]
```

두 번째 줄에 책이 보이는 것은 그 컨테이너에 미리 책 한 권을 등록했기 때문이고(H2 메모리 DB 라서 컨테이너마다 데이터가 따로입니다), 새 컨테이너라면 `[]` 입니다. 세 번째 줄은 호스트의 `8080` 에서 응답하는 서버가 있어야 하는데, 이 실행에서는 호스트 `8080` 에 다른 백엔드 컨테이너가 떠 있었고 빈 목록 `[]` 이 왔습니다. 이 실험들이 보여 주는 것은 **같은 `localhost:8080` 이라도 어느 네트워크 안에서 부르느냐에 따라 다른 곳을 가리킨다** 는 점입니다.

#### 일상 시나리오: 프론트는 호스트에서, 백엔드는 컨테이너로

프론트를 개발하는 평소 모습입니다. 백엔드 팀이 컨테이너 이미지를 주면 `-p 8080:8080` 으로 띄우고, 프론트는 호스트에서 평소처럼 개발 서버를 켭니다. 프론트의 `rewrites` 는 `localhost:8080` 만 보므로 백엔드가 컨테이너인지 `java -jar` 인지 모릅니다.

```bash
docker run -d --name library-api -p 8080:8080 library-api
cd labs/frontend && pnpm dev          # 다른 터미널
```

```bash
curl -si localhost:3000/api/books | sed -n '1p;$p'
curl -si -c /tmp/w13d.jar -X POST localhost:3000/api/auth/login -H 'Content-Type: application/json' -d '{"username":"demo","password":"demo1234"}' | sed -n '1p;$p'
curl -si -b /tmp/w13d.jar -X POST localhost:3000/api/books -H 'Content-Type: application/json' -d '{"isbn":"978-2","title":"Refactoring","author":"Fowler"}' | sed -n '1p;$p'
curl -s localhost:8080/books
```

```text
HTTP/1.1 200 OK
[]
HTTP/1.1 200 OK
{"username":"demo"}
HTTP/1.1 201 Created
{"id":1,"isbn":"978-2","title":"Refactoring","author":"Fowler"}
[{"id":1,"isbn":"978-2","title":"Refactoring","author":"Fowler"}]
```

3000 의 `/api/books` 가 컨테이너 백엔드의 응답을 돌려주고, 마지막 줄처럼 컨테이너에 직접 물어도 같은 데이터가 보입니다. `pnpm dev` 는 `Ctrl+C` 로, 컨테이너는 `docker stop library-api && docker rm library-api` 로 정리합니다.

#### 프론트 이미지

Dockerfile 은 [`labs/frontend/Dockerfile`](../labs/frontend/Dockerfile), 컨텍스트에서 뺄 목록은 [`labs/frontend/.dockerignore`](../labs/frontend/.dockerignore) 입니다. `.dockerignore` 는 `node_modules`, `.next`, `test-results` 같은 폴더를 컨텍스트에서 빼서, 이미지 안에서 `pnpm install` 과 `pnpm build` 를 새로 하게 합니다. 이미지를 만드는 시점에 `NEXT_PUBLIC_*` 가 정해집니다(3.2절). `labs/frontend` 의 소스는 바꾸지 않았습니다.

```dockerfile
FROM node:22
WORKDIR /app
RUN npm install -g pnpm@10
COPY package.json pnpm-lock.yaml ./
RUN pnpm install --frozen-lockfile
COPY . .
ARG NEXT_PUBLIC_API_BASE=
ENV NEXT_PUBLIC_API_BASE=$NEXT_PUBLIC_API_BASE
RUN pnpm build
ENV NODE_ENV=production
EXPOSE 3000
CMD ["node_modules/.bin/next", "start"]
```

(파일에는 위에 더해 용도를 설명하는 주석이 있습니다.)

```bash
docker build -t library-web labs/frontend
docker build -t library-web:api --build-arg NEXT_PUBLIC_API_BASE=https://api.example.com labs/frontend
```

- 컨텍스트가 `Sending build context to Docker daemon  224.8kB` 입니다. `.dockerignore` 가 없으면 `node_modules` 가 통째로 들어갑니다.
- Colima 메모리 2GiB 로도 빌드가 성공했습니다(`✓ Compiled successfully in 7.6s`). 처음에는 `node:22` 이미지를 받고 의존성을 설치하느라 수 분이 걸렸습니다. 메모리가 부족해 빌드가 죽는 환경이라면 `colima stop && colima start --memory 4` 로 VM 메모리를 늘립니다. 이 머신에서는 필요하지 않아 실행하지 않았고, 사용자의 Colima 설정은 건드리지 않았습니다.

빌드 인자가 브라우저 번들에 들어가는지 이미지 안의 산출물에서 확인합니다.

```bash
docker run --rm --entrypoint sh library-web -c "grep -rhoE '.{20}credentials:\"include\"' .next/static | sort -u"
docker run --rm --entrypoint sh library-web:api -c "grep -rhoE '.{20}credentials:\"include\"' .next/static | sort -u"
docker run --rm -e NEXT_PUBLIC_API_BASE=https://other.example.com --entrypoint sh library-web:api -c "grep -rl 'other.example.com' .next/static | wc -l; grep -rl 'api.example.com' .next/static | wc -l"
docker run --rm --entrypoint sh library-web -c "grep -o '\"destination\": *\"[^\"]*\"' .next/routes-manifest.json"
```

```text
}}}({baseUrl:"/api",credentials:"include"
://api.example.com",credentials:"include"
0
1
"destination": "/:path+"
"destination": "http://localhost:8080/:path*"
```

- 첫 줄이 인자 없이 빌드한 `library-web`, 둘째 줄이 `--build-arg NEXT_PUBLIC_API_BASE=https://api.example.com` 으로 빌드한 `library-web:api` 입니다. 값이 번들 안에 **문자열로 박혔습니다.**
- 셋째 명령은 `library-web:api` 를 `docker run -e NEXT_PUBLIC_API_BASE=https://other.example.com` 으로 실행해도 번들이 그대로라는 것을 보입니다. `other.example.com` 을 담은 파일은 `0` 개, 빌드 때 준 `api.example.com` 을 담은 파일은 `1` 개입니다. 3.2절의 "실행 때 환경 변수를 줘도 효과가 없다" 를 확인한 것입니다.
- 이 이미지의 Dockerfile 은 `ENV NEXT_PUBLIC_API_BASE=` 로 빈 값을 주므로 `process.env.NEXT_PUBLIC_API_BASE||"/api"` 가 빈 값으로 접혀 `"/api"` 만 남았습니다. 4단계에서 호스트에 변수를 설정하지 않고 빌드했을 때(`||"/api"` 식이 그대로 남음)와 모양이 다르지만 동작은 같습니다.
- `rewrites` 목적지 `http://localhost:8080` 은 이미지 안의 `.next/routes-manifest.json` 에 고정되어 있습니다(마지막 줄. 위의 `/:path+` 는 Next 가 기본으로 넣는 다른 규칙입니다).

이제 이 이미지를 컨테이너로 띄워 `/api` 를 부르면 `rewrites` 목적지 때문에 실패합니다. 백엔드가 호스트 `8080` 에서 멀쩡히 떠 있는데도 그렇습니다.

```bash
docker run -d --name library-web -p 3000:3000 library-web
curl -s -o /dev/null -w '홈: %{http_code}\n' localhost:3000/
curl -si localhost:3000/api/books | sed -n '1p;$p'
docker logs library-web

docker run -d --name library-api -p 8080:8080 library-api       # 호스트 8080 에 백엔드 컨테이너
curl -s localhost:8080/books
curl -si localhost:3000/api/books | sed -n '1p;$p'
```

```text
홈: 200
HTTP/1.1 500 Internal Server Error
Internal Server Error
▲ Next.js 16.4.2
- Local:         http://localhost:3000
- Network:       http://172.17.0.2:3000
✓ Ready in 236ms
✓ Running next.config.ts took 122ms
Failed to proxy http://localhost:8080/books Error: connect ECONNREFUSED 127.0.0.1:8080
    at ignore-listed frames {
  errno: -111,
  code: 'ECONNREFUSED',
  syscall: 'connect',
  address: '127.0.0.1',
  port: 8080
}
[]
HTTP/1.1 500 Internal Server Error
Internal Server Error
```

- 홈(`/`)은 `200` 으로 뜨지만 `/api/books` 는 `500`, 본문은 `Internal Server Error` 한 줄입니다. 백엔드의 `{"message": ...}` 모양이 아닙니다(2.5절). 원인은 프론트 컨테이너의 로그(`ECONNREFUSED 127.0.0.1:8080`)에 있습니다. **프론트 컨테이너 안의 `localhost:8080` 에는 아무도 없습니다.**
- 호스트 `8080` 에 백엔드 컨테이너를 띄운 뒤(`curl localhost:8080/books` 는 `[]`)에도 `/api/books` 는 같은 `500` 입니다. 호스트의 `8080` 과 프론트 컨테이너 안의 `localhost:8080` 은 서로 다른 곳이기 때문입니다.
- 앞에서 쓴 네트워크 공유로 반증합니다. 프론트 컨테이너를 백엔드 컨테이너와 같은 네트워크에 두면 같은 요청이 성공합니다. 포트 `3000` 은 공유하는 쪽(`library-api`)에 속하므로 `library-api` 안에서 부릅니다.

```bash
docker run -d --name library-web-shared --network container:library-api library-web
docker exec library-api curl -s -w '\n%{http_code}\n' localhost:3000/api/books
```

```text
[]
200
```

같은 이미지, 같은 `rewrites` 인데 `localhost` 가 가리키는 곳이 바뀌자 성공했습니다. 이 공유는 실험용입니다. 실제 환경에서는 3.2절의 해결 방향처럼 `/api` 를 백엔드로 넘기는 일을 서버 앞단에 맡깁니다. 그 방법은 실행해 보지 않았습니다. 컨테이너에서 호스트를 부르려면 `host.docker.internal` 을 쓸 수 있지만, 이 저장소의 `next.config.ts` 는 목적지를 `localhost:8080` 으로 고정해 두었으므로 쓰려면 소스를 바꿔 다시 빌드해야 하고, 이 문서에서는 바꾸지 않았습니다.

정리합니다. 만든 컨테이너와 이미지를 지우고, 받은 베이스 이미지는 남겨도 됩니다.

```bash
docker stop library-web library-web-shared library-api; docker rm library-web library-web-shared library-api
docker rmi library-web:api library-web library-api
docker ps -a
```

### 3.2 환경 변수: 백엔드는 실행 때, 프론트는 빌드 때

**백엔드(Spring):** `application.properties` 의 키는 환경 변수로 덮어쓸 수 있습니다. 점은 밑줄로, 대문자로 바꾸면 됩니다(Spring 의 "relaxed binding"). 3-B, 3-C 에서 직접 확인했습니다.

| `application.properties` 의 키 | 환경 변수 | 확인한 효과 |
|---|---|---|
| `library.demo.password` | `LIBRARY_DEMO_PASSWORD` | 데모 로그인 비밀번호가 바뀜 |
| `server.port` | `SERVER_PORT` | 서버 포트가 바뀜 (`Tomcat started on port 8083`) |
| `server.servlet.session.cookie.domain` | `SERVER_SERVLET_SESSION_COOKIE_DOMAIN` | `Set-Cookie` 에 `Domain=` 추가 |
| `server.servlet.session.cookie.secure` | `SERVER_SERVLET_SESSION_COOKIE_SECURE` | `Set-Cookie` 에 `Secure` 추가 |

그래서 백엔드 이미지는 환경마다 다시 만들 필요가 없습니다. 다만 `SecurityConfig` 의 CORS 허용 출처(`http://localhost:3000`)는 **코드에 박혀 있어** 환경 변수로 바꿀 수 없습니다. 환경마다 프론트 출처가 달라지면 이 부분은 "설정으로 빼 달라" 는 변경 요청의 대상입니다.

**프론트(Next):** 사정이 반대입니다. `NEXT_PUBLIC_` 접두사가 붙은 변수는 브라우저로 내려가는 JS 에 `next build` 시점에 **값으로 치환**됩니다. 공식 문서의 표현입니다.

> After being built, your app will no longer respond to changes to these environment variables. For instance, if you use a Heroku pipeline to promote slugs built in one environment to another environment, or if you build and deploy a single Docker image to multiple environments, all `NEXT_PUBLIC_` variables will be frozen with the value evaluated at build time, so these values need to be set appropriately when the project is built.

4단계에서 확인했듯 값을 주지 않은 빌드에서는 `process.env.NEXT_PUBLIC_API_BASE||"/api"` 가 번들에 남아 항상 `"/api"` 가 되고, `rewrites` 의 목적지도 `.next/routes-manifest.json` 에 기록되어 서버를 띄운 뒤에는 바뀌지 않습니다.

여기서 흔한 사고가 나옵니다.

- 이미지를 dev 에서 한 번 빌드하고 staging·prod 에 **같은 이미지**로 올렸는데, 브라우저가 계속 dev 주소를 부른다. (`NEXT_PUBLIC_*` 가 빌드 때 얼었기 때문)
- 서버를 띄우는 `docker run -e NEXT_PUBLIC_API_BASE=...` 로 바꾸려 했는데 효과가 없다. (이미 만들어진 JS 는 환경 변수를 읽지 않음)
- 스테이징의 프론트 컨테이너에서 `/api` 호출이 실패한다. (`rewrites` 목적지가 `http://localhost:8080` 로 얼어 있어 컨테이너 안에서 자기 자신을 부름) 3.1절에서 재현했을 때 상태 코드는 `500` 이었고, 앞단에 프록시가 있으면 `502` 처럼 다르게 보일 수 있습니다.

해결 방향은 세 가지이고, 이 프론트는 첫 번째가 가장 자연스럽습니다. 아래는 개념 설명이며 이 저장소에 구현되어 있지 않고 실행 확인도 안 했습니다.

1. **브라우저는 항상 같은 출처 `/api` 만 부르고, 환경마다 다른 것은 서버 앞단(리버스 프록시·인그레스)이 `/api` 를 백엔드로 넘기는 규칙으로 처리한다.** 그러면 `NEXT_PUBLIC_API_BASE` 가 필요 없고 같은 이미지를 어디서나 씁니다. Next 의 `rewrites` 는 개발용으로만 두고, 운영에서는 nginx 같은 리버스 프록시가 맡습니다(Next 공식 self-hosting 가이드도 운영에서는 Next 서버 앞에 리버스 프록시를 두라고 권합니다).
2. **환경마다 따로 빌드한다.** 환경 개수만큼 이미지가 생깁니다. 간단하지만 "테스트한 이미지를 그대로 운영에 올린다" 는 원칙이 깨집니다.
3. **런타임 설정을 서버가 알려 준다.** 브라우저가 시작할 때 `/config` 같은 엔드포인트를 불러 값을 받습니다. Next 공식 문서가 말하는 "런타임 값이 필요하면 자체 API 로 제공하라" 입니다.

### 3.3 프록시, CORS, 쿠키 도메인

환경에 따라 브라우저, 프론트 서버, 백엔드의 위치 관계가 바뀌고, 그에 따라 필요한 설정이 달라집니다.

| 환경 | 브라우저가 부르는 곳 | 필요한 것 | 이 저장소에서 |
|---|---|---|---|
| 로컬 (기본) | `http://localhost:3000/api/...` (프론트와 같은 출처) → Next 가 `localhost:8080` 으로 전달 | CORS 불필요. 쿠키는 `localhost:3000` 호스트에 저장 | `NEXT_PUBLIC_API_BASE` 비움 |
| 로컬 (직접 호출) | `http://localhost:8080/...` (다른 출처, 같은 사이트) | 백엔드 CORS + `credentials: "include"` | 허용 출처 `http://localhost:3000` 한 개 |
| 같은 도메인의 리버스 프록시 (`https://app.example.com` 에서 `/` 는 프론트, `/api` 는 백엔드) | 브라우저 입장에서 같은 출처 | CORS 불필요. 쿠키는 호스트 전용 | 가장 단순하고 권장 |
| 서브도메인 분리 (`app.example.com` ↔ `api.example.com`) | 다른 출처, **같은 사이트** | 백엔드 CORS 에 프론트 출처 등록. 쿠키 `Domain=example.com` 이 필요할 수 있음 | 허용 출처가 코드에 박혀 있어 변경 필요. `Domain` 은 환경 변수로 가능 |
| 완전히 다른 도메인 | 다른 출처, **다른 사이트** | CORS + 쿠키에 `SameSite=None; Secure`. 브라우저의 서드파티 쿠키 제한으로 막힐 수 있음 | 가능하면 피함 |

이 저장소에서 직접 확인한 것과 개념으로만 아는 것을 구분합니다.

- **확인함:** 허용되지 않은 출처의 요청은 403 `Invalid CORS request`(3-A). 서버가 낸 `Domain=example.com` 쿠키를 `localhost` 클라이언트가 저장하지 않아 다음 요청이 401(3-B). `Secure` 쿠키는 `localhost` 에서는 http 로도 동작(3-B).
- **개념:** 프록시 뒤의 서버는 원래 요청이 https 였는지 모르므로 `X-Forwarded-Proto` 헤더와 서버의 `forward-headers` 설정이 필요합니다. 그렇지 않으면 리다이렉트 주소가 `http://` 로 나오는 일이 있습니다. 이 저장소는 프록시 뒤에서 실행해 보지 않았습니다.
- **개념:** 쿠키 `SameSite=Lax` 는 같은 사이트의 요청과 최상위 탐색(링크 클릭)에는 실리지만, 다른 사이트가 보낸 `fetch` 에는 실리지 않습니다. 완전히 다른 도메인 구성에서 로그인이 풀리는 이유입니다(8주).

### 3.4 환경 차이로 생긴 문제를 구분하는 질문

로컬에서는 되는데 스테이징에서 안 되는 문제를 만나면, 코드를 열기 전에 순서대로 확인합니다.

1. 요청이 **어디로** 갔는가? 네트워크 탭의 Request URL 의 호스트·포트·경로. (빌드에 얼어 있는 `NEXT_PUBLIC_*`, `rewrites` 목적지가 원인인 경우)
2. 응답이 **누구에게서** 왔는가? 상태 코드와 `Server`·본문 모양. `{"message"}` 면 백엔드 앱, `{"timestamp","status","error","path"}` 면 프레임워크, HTML 이면 프록시나 CDN 의 오류 페이지입니다(2.5절).
3. 요청 직전에 `OPTIONS` 가 있었는가? 있다면 CORS 설정(3-A).
4. 로그인 응답 뒤 요청에 쿠키가 실렸는가? 네트워크 탭 Cookies 탭의 "this cookie was blocked" 이유 표시(3-B).
5. 시각이 어긋난 로그를 보고 있지는 않은가? UTC 와 `+09:00` (2.4절).

## 연습문제

1. `LoanResponse` 에 `dueAt`(대출 기한, `Instant`)을 추가하는 변경 요청서를 1.6절 양식으로 작성하세요. 분류, 영향 범위, 일정을 채우고, `try` 함수로 필드를 추가했을 때 오류 개수를 직접 확인해 요청서에 쓰세요. 필수 필드로 추가하는 경우와 선택 필드로 추가하는 경우의 차이도 적으세요.
2. `BookResponse.title` 을 `name` 으로 바꾸는 깨지는 변경을 1.4절의 4단계로 풀어 쓰세요. 각 단계에서 스펙·서버·프론트가 각각 무엇을 하는지 표로 만드세요. 3단계(프론트가 옮긴 것을 서버가 확인)를 접근 로그로 하려면 로그 패턴에 무엇을 추가해야 할지도 적으세요.
3. 2.6절의 보고서 양식으로, 2단계의 요청 3번(없는 회원 대출, 404)을 제보하세요. 응답 본문은 실제로 받은 값을 쓰고, 같은 요청이 서버 로그에 어떻게 남는지(또는 남지 않는지) 직접 확인한 뒤 "서버 로그에서 확인 부탁" 줄을 적절히 고치세요.
4. 2.2절의 미들웨어를 `labs/frontend` 에 임시 파일로 붙여 `pnpm typecheck` 를 통과시키세요. 이어서 이 헤더를 브라우저에서 보내려면 백엔드의 어느 파일의 어느 설정을 바꿔야 하는지 찾아서 변경 요청서의 "변경 내용" 표로 쓰세요(실제 코드는 바꾸지 않습니다). 확인이 끝나면 임시 파일을 지웁니다.
5. 스테이징 프론트의 주소가 `https://staging.example.com`, 백엔드가 `https://api-staging.example.com` 이라고 가정합니다. 3.3절 표에서 어느 행에 해당하는지 고르고, 백엔드와 프론트가 각각 바꿔야 할 것(CORS 허용 출처, 쿠키 `Domain`, `NEXT_PUBLIC_API_BASE` 또는 프록시 규칙)을 체크리스트로 쓰세요.
6. 3.1절의 프론트 `Dockerfile` 을 읽고 `ARG NEXT_PUBLIC_API_BASE` 가 어느 줄에서 효과를 내고 어느 줄 이후에는 의미가 없는지 설명하세요. 이 Docker 파일로 만든 이미지를 dev 와 prod 에 모두 쓰면 무슨 일이 생기는지 3.2절 용어로 설명하세요.
7. 3.1절의 백엔드 이미지를 만들고 `docker run -d --name library-api -p 9090:8080 library-api` 로 띄우세요. 컨테이너가 준비된 뒤 호스트에서 `localhost:9090/books` 와 `localhost:8080/books`, 컨테이너 안에서 `localhost:8080/books` 와 `localhost:9090/books` 의 상태 코드를 먼저 예측하고 `curl` 로 확인하세요(호스트의 `8080` 이 비어 있어야 합니다). 끝나면 컨테이너를 지우세요.

   <details>
   <summary>정답</summary>

   호스트에서는 `9090` 이 `200`, `8080` 이 연결 실패(`000`)입니다. 컨테이너 안(`docker exec library-api curl ...`)에서는 `8080` 이 `200`, `9090` 이 `000` 입니다. `-p 9090:8080` 의 왼쪽이 밖, 오른쪽이 안이기 때문입니다. 3.1절에서 같은 구성으로 `8090` 과 `8080` 을 확인한 결과와 같습니다. 정리는 `docker stop library-api && docker rm library-api` 입니다.

   </details>

## 퀴즈

1. 응답에 선택 필드를 추가하는 것은 호환 변경이고, 요청에 필수 필드를 추가하는 것은 깨지는 변경이다. 왜 방향에 따라 달라지는가?
2. `try` 로 `author` 를 `["string","null"]` 로 바꿨는데 `pnpm typecheck` 오류가 0 개였다. 이 변경은 호환인가?
3. 참조 API 가 409 를 낼 때 서버 로그에 "이미 등록된 isbn 입니다" 문장이 있는가? 있다면 어떤 로그에, 없다면 그 문장은 어디에서만 볼 수 있는가?
4. 응답의 `Date: Thu, 22 Oct 2026 13:48:44 GMT` 와 서버 로그의 `2026-10-22T22:48:44.573+09:00` 은 같은 순간인가?
5. 프론트가 `X-Request-Id` 를 요청에 붙이면 참조 API 는 그 값을 응답에 돌려주는가? 브라우저에서 직접 호출할 때 이 헤더는 문제없이 가는가?
6. `NEXT_PUBLIC_API_BASE` 를 `docker run -e` 로 주면 이미 만들어진 프론트 이미지의 브라우저 동작이 바뀌는가?
7. 같은 `week25.jar` 로 `Domain=example.com` 쿠키를 낸 서버에 `localhost` 클라이언트가 로그인하면 로그인 응답은 몇 번이고, 바로 다음 `/auth/me` 는 몇 번인가? 왜인가?
8. `GET /nope` 의 404 본문이 `{"message": ...}` 가 아닌 이유는 무엇이고, 그것이 장애 제보에서 어떤 정보를 주는가?

<details>
<summary>정답</summary>

1. 응답은 서버가 쓰고 프론트가 읽는데, 읽는 쪽은 모르는 필드를 무시하면 되므로 추가는 안전합니다. 요청은 프론트가 쓰고 서버가 읽는데, 서버는 필수 필드가 없으면 거부하므로 필수 필드 추가는 옛 프론트의 요청을 400 으로 만듭니다.
2. 호환이 아닙니다. 응답이 `null` 일 수 있게 된 것은 소비자가 가정하던 계약을 깨뜨립니다. 이 저장소의 화면이 `author` 를 JSX 에 찍기만 해서 컴파일러가 못 잡았을 뿐이며, `author.toLowerCase()` 같은 코드가 있었다면 런타임에 터집니다.
3. 없습니다. 접근 로그에도 `DispatcherServlet` 로그에도 상태 코드(`409`)만 있습니다. 문장은 응답 본문에서만 볼 수 있으므로 제보에 본문을 원문 그대로 붙여야 합니다.
4. 같은 순간입니다. `GMT` 는 UTC+0 이고 `+09:00` 은 UTC 보다 9시간 빠르므로 `13:48:44 GMT` = `22:48:44 +09:00` 입니다.
5. 돌려주지 않습니다(접근 로그 패턴에 헤더를 적어 넣어야 로그에만 보입니다). 브라우저가 직접 호출하면 서버의 CORS 허용 헤더가 `Content-Type` 하나라서 preflight 응답에 `x-request-id` 가 빠지고, 브라우저가 본 요청을 막습니다. `/api` 프록시 경유(같은 출처)에서는 CORS 가 없습니다.
6. 바뀌지 않습니다. `NEXT_PUBLIC_*` 는 `next build` 때 JS 번들에 치환되고, 이미 만들어진 번들은 실행 환경 변수를 읽지 않습니다. 값을 바꾸려면 다시 빌드해야 합니다(또는 런타임 설정 방식으로 바꿔야 합니다).
7. 로그인 응답은 200, 다음 `/auth/me` 는 401 입니다. 클라이언트가 `localhost` 응답의 `Domain=example.com` 쿠키를 도메인 불일치로 저장하지 않아 다음 요청에 쿠키가 실리지 않기 때문입니다.
8. 컨트롤러에 닿기 전에 프레임워크의 기본 오류 처리가 만든 응답이라서 `{"timestamp","status","error","path"}` 모양입니다. 업무 규칙이 아니라 "경로·메서드·Content-Type 이 계약과 맞지 않다" 는 뜻이므로 먼저 프론트 쪽 URL·메서드·헤더를 의심하라는 단서입니다.

</details>

## 체크리스트

- [ ] 1단계의 변경 11가지를 먼저 분류하고, 측정 결과와 비교해 틀린 것의 이유(특히 e, f)를 적었다
- [ ] 응답 방향과 요청 방향에서 호환/깨짐이 반대로 되는 이유를 설명할 수 있다
- [ ] enum 값 추가가 "합의가 필요한 변경" 인 이유를 설명할 수 있다
- [ ] deprecation 4단계와 `deprecated: true` 가 컴파일러를 깨뜨리지 않는다는 점을 설명할 수 있다
- [ ] URL 버전과 헤더 버전의 차이를 프론트 구현(`baseUrl` 한 곳 vs 미들웨어와 CORS 허용 헤더) 관점에서 비교할 수 있다
- [ ] 1.6절 양식으로 변경 요청서를 직접 작성했다
- [ ] 기본 설정에서 참조 API 가 어떤 요청에 로그를 남기고 어떤 요청에 남기지 않는지 직접 확인했다
- [ ] 접근 로그와 `DispatcherServlet` 로그를 켜서 내 요청 하나를 두 로그에서 찾았고, 로그 한 줄의 구성 요소를 설명할 수 있다
- [ ] 응답의 `Date`(GMT), 서버 로그(`+09:00`), 프론트의 `toISOString()`(UTC)이 같은 순간임을 계산할 수 있다
- [ ] 참조 API 에는 상관 ID 기능이 없다는 것과, 합의가 필요한 내용을 구분해서 말할 수 있다
- [ ] 2.6절 양식으로 재현 보고서를 작성했다
- [ ] 이미지·컨테이너·포트 매핑·환경 변수를 TS 용어로 설명할 수 있다
- [ ] 백엔드 이미지를 직접 빌드해 `-p 8090:8080` 으로 띄우고, 바깥과 안에서 어느 포트가 열려 있는지 확인했다
- [ ] 프론트 이미지의 번들에서 `NEXT_PUBLIC_API_BASE` 가 빌드 때 박히는 것과, 컨테이너 안에서 `/api` 호출이 `localhost:8080` 때문에 실패하는 것을 확인했다
- [ ] `NEXT_PUBLIC_*` 와 `rewrites` 목적지가 빌드 시점에 정해지는 이유와, 같은 이미지를 여러 환경에서 쓸 때의 문제를 설명할 수 있다
- [ ] 3-A, 3-B 로 CORS 403 과 쿠키 도메인 불일치를 직접 재현했고, 3.3절 표에서 내 구성이 어느 행인지 고를 수 있다
