# 10주차. 데이터 패칭 설계: 캐시 키, 무효화, 낙관적 업데이트, 페이지네이션

**예상 시간:** 6~8시간 (하루 1시간 세션 8회) | **목표:** TanStack Query 의 캐시가 "언제 요청을 보내고 언제 보내지 않는가" 를 요청 횟수로 예측하고 확인한다. `../labs/frontend` 가 이미 내린 결정(캐시 키, 기본 `staleTime`, 무효화 방식, 로딩·에러 표시)을 코드에서 읽어 이유를 설명하고, 반납 버튼에 낙관적 업데이트를 직접 구현해 성공·401·409 세 경우를 확인한다. 참조 API 에 없는 페이지네이션은 흉내 낸 서버로 동작을 확인하고, 백엔드와 협의할 질문을 정리한다.

> 이 주차의 전제입니다. 7주의 `ApiError` 와 상태 코드 처리, 8주의 세션 쿠키 로그인, 9주의 기동 순서와 MSW 를 알고 있어야 합니다. 2주의 [페이지네이션 개념](week02-rest-json-contract.md)(오프셋과 커서)을 한 번 읽었다고 가정합니다. 이 주차는 그 개념을 프론트의 캐시 설계에 연결합니다.
>
> 준비물: JDK 21, Node 22.12+, pnpm 10+, `curl`, 브라우저 DevTools(Network 탭). 백엔드는 터미널 A 에서 `cd labs/backend && ./gradlew :week25:bootRun`(8080), 프론트는 터미널 B 에서 `cd labs/frontend && pnpm install && pnpm dev`(3000)로 띄웁니다. 아래 명령은 별도 언급이 없으면 저장소 루트에서 실행합니다.
>
> **참조 API 에는 페이지네이션이 없습니다.** `GET /books` 는 `keyword` 쿼리만 받고 항상 전부를 돌려줍니다. 백엔드는 이 주차에서도 건드리지 않습니다. 페이지네이션 부분은 테스트 안의 MSW 핸들러와 임시 Route Handler 가 **흉내 낸 서버**이며, 실서버가 `cursor`, `offset` 을 지원한다는 뜻이 아닙니다.

## 하루 1시간으로 나누면

이 주차는 4단계가 선택이라서 하루 1시간씩 8번의 세션으로 나눕니다. 한 세션은 50~70분이고, 2시간짜리 2단계는 문서의 준비·예측, 구현, 실험 구간으로 나눕니다.

| 세션 | 할 일 | 끝났을 때 할 수 있어야 하는 것 |
|---|---|---|
| 1 | 1단계(요청 횟수를 예측하기 → 세어 보기 → 비교하기, 1시간)를 한다 | 9줄의 요청 수를 예측과 비교해 세고, 어느 줄의 예측이 틀렸는지 말할 수 있다 |
| 2 | 1절(서버 상태는 "내 것이 아닌 데이터의 사본"), 2절(지금 코드가 내린 결정 읽기), 3절(로딩·에러 상태 설계)을 읽고, 2단계(낙관적 업데이트 직접 구현하기, 2시간)의 준비와 예측을 한다 | `["books", keyword]`, `["loans", id]`, `["me"]` 세 키가 무엇을 구분하는지와 무효화가 접두사로 동작하는 것을 설명할 수 있다 |
| 3 | 2단계의 구현(`MyLoans.tsx` 의 `giveBack` 빈칸 채우기)을 하고, 4절(낙관적 업데이트)을 읽는다 | `giveBack` 에 낙관적 업데이트를 구현하고, 낙관적 업데이트가 `lend` 와 `createBook` 에 맞지 않는 이유를 말할 수 있다 |
| 4 | 2단계의 실험(성공·401·409 세 경우)을 하고 `MyLoans.tsx` 를 원복한 뒤, 5절(페이지네이션)을 읽는다 | 성공·401·409 에서 목록이 어떻게 움직이는지 확인하고 `MyLoans.tsx` 를 원복하며, 페이지네이션 계약에 대해 백엔드에 물을 질문 5개 이상을 쓸 수 있다 |
| 5 | 3단계(오프셋과 커서를 예측하기 → 테스트로 확인하기, 1~1.5시간)를 한다. 60~70분 안에 못 끝나면 같은 단계에서 이어서 한다 | 오프셋이 중복을 만드는 순간과 커서가 만들지 않는 이유를 숫자로 설명할 수 있다 |
| 6 | 4단계(선택, 30분, 화면에서 보기)를 한다 (압축 코스에서는 건너뜀) | 흉내 낸 페이지네이션 서버 위에서 "페이지 번호" 화면과 "더 보기" 화면이 어떻게 다르게 동작하는지 확인하고 임시 폴더를 삭제할 수 있다 |
| 7 | 연습문제와 퀴즈를 푼다 (압축 코스에서는 건너뜀) | `staleTime` 을 바꿨을 때 요청 수가 어떻게 바뀌는지 예측하고, 캐시 키의 타입이 어긋났을 때 무슨 일이 생기는지 설명할 수 있다 |
| 8 | 6절(한계: 이 주차가 다루지 않은 것)과 TS 와 비교 절을 읽고, 체크리스트의 각 항목을 내 말로 설명해 보고, 막히는 항목은 해당 절을 다시 읽은 뒤 학습 노트를 정리한다 | 캐시 키, `staleTime`, 무효화, 낙관적 업데이트, 페이지네이션 각각에서 백엔드와 맞춰야 할 것을 한 문장씩 말할 수 있다 |

세션을 시작하기 전에 이전 세션의 마지막 결과를 1~2분 훑어봅니다. 막히면 아래 해당 개념 절을 사전처럼 펼쳐 보세요.

## 🖐 먼저 손으로 (읽기 전에 하세요)

> 이 주차의 핵심은 아래 단계를 **직접 해 보면서** 깨닫는 것입니다. 아래쪽 설명 문서는 먼저 읽지 않아도 됩니다. 막혔을 때 사전처럼 펼쳐 보세요.

### 1단계. 요청 횟수를 예측하기 → 세어 보기 → 비교하기 (1시간)

백엔드를 **새로 켠 뒤**(재시작해서 DB 를 비웁니다) 책 2권과 회원 1명을 등록합니다.

```bash
B=localhost:8080; J=/tmp/week10.cookies
curl -s -c $J -X POST -H 'Content-Type: application/json' -d '{"username":"demo","password":"demo1234"}' $B/auth/login
curl -s -b $J -X POST -H 'Content-Type: application/json' -d '{"isbn":"111","title":"이펙티브 자바","author":"조슈아 블로크"}' $B/books
curl -s -b $J -X POST -H 'Content-Type: application/json' -d '{"isbn":"222","title":"Clean Code","author":"Robert C. Martin"}' $B/books
curl -s -b $J -X POST -H 'Content-Type: application/json' -d '{"name":"하승"}' $B/members
```

브라우저에서 `http://localhost:3000` 을 열기 **전에** 아래 표의 예측을 노트에 적으세요. 각 줄은 "이 조작 한 번에 `/api/` 로 나가는 요청이 무엇이고 몇 번인가" 입니다. 코드는 아직 읽지 않아도 됩니다. 7~9주에서 본 화면 동작과 TanStack Query 를 써 본 경험으로 추측하세요.

| # | 조작 (앞 조작에 이어서) | 내 예측: 나가는 `/api/` 요청 |
|---|------|------|
| 1 | `/` 를 새로 연다 | |
| 2 | 제목 검색에 `clean` 을 넣고 검색한다 | |
| 3 | 검색어를 지우고 다시 검색한다. 클릭 직후 "불러오는 중..." 이 보이는가? 목록은? | |
| 4 | 상단 링크 `회원` 을 누른다 | |
| 5 | 다시 `책` 링크를 누른다 | |
| 6 | 책 목록 화면에서 다른 브라우저 탭으로 갔다가 돌아온다 | |
| 7 | 로그인한다(`demo`/`demo1234`) | |
| 8 | 제목 검색에 `clean` 을 넣어 검색한 채로, 아래 "책 등록" 폼에 책 한 권(아무 isbn, 제목 `Clean Architecture`)을 등록한다 | |
| 9 | 그 뒤 검색어를 지우고 검색한다. 클릭 직후 목록은 몇 권이고, 잠시 뒤에는? | |

확인 방법: DevTools 의 Network 탭에서 필터에 `/api/` 를 입력하고, 1번 전에 로그(Clear)를 비운 뒤 한 조작마다 새로 생긴 줄만 셉니다. 3번과 9번은 클릭 직후 화면과 잠시 뒤 화면을 모두 봅니다.

<details>
<summary>정답 (예측을 적은 뒤에 여세요)</summary>

`pnpm dev` 상태에서 같은 조작을 Playwright 로 하며 센 값입니다. 사람이 DevTools 로 세어도 같아야 합니다.

| # | 나가는 요청 |
|---|------|
| 1 | `GET /api/auth/me` 1번, `GET /api/books` 1번 |
| 2 | `GET /api/books?keyword=clean` 1번 |
| 3 | `GET /api/books` 1번. 클릭 직후 "불러오는 중..." 은 **보이지 않고** 목록은 이미 그려져 있습니다 |
| 4 | `GET /api/auth/me` 1번 |
| 5 | `GET /api/books` 1번, `GET /api/auth/me` 1번 |
| 6 | `GET /api/auth/me` 1번, `GET /api/books` 1번 |
| 7 | `POST /api/auth/login` 1번만. `/api/auth/me` 는 다시 나가지 않습니다 |
| 8 | `POST /api/books` 1번, `GET /api/books?keyword=clean` 1번 |
| 9 | `GET /api/books` 1번. 클릭 직후에는 **등록 전의 2권**이 보이다가 잠시 뒤 3권이 됩니다 |

- 3번: 키 `["books", ""]` 의 캐시가 이미 있어서 화면이 바로 채워지고, 캐시가 오래된 것으로 취급돼서 **뒤에서** 다시 가져옵니다. "캐시를 쓴다" 와 "요청을 안 보낸다" 는 다릅니다.
- 4~6번: 아무도 `staleTime` 을 정하지 않았기 때문입니다. 화면이 새로 마운트되거나 탭에 돌아올 때마다 오래된 캐시를 다시 가져옵니다. `/auth/me` 는 로그인 상태가 거의 바뀌지 않는데도 페이지를 옮길 때마다 나갑니다.
- 7번: 로그인 성공 응답을 `["me"]` 캐시에 **직접 써 넣기**(`setQueryData`) 때문에 다시 물을 필요가 없습니다.
- 8번: 책을 등록하면 `["books"]` 로 시작하는 쿼리를 모두 무효화합니다. 그런데 지금 화면에 쓰이는 키는 `["books", "clean"]` 하나뿐이라 요청도 하나만 나갑니다.
- 9번: 8번에서 무효화만 되고 가져오지는 않은 `["books", ""]` 캐시가 오래된 채로 남아 있었습니다. 그래서 돌아오자마자 오래된 2권을 보여 주고, 다시 가져와서 3권으로 바꿉니다.

</details>

### 2단계. 낙관적 업데이트 직접 구현하기 (2시간)

지금의 "반납" 은 서버 응답이 올 때까지 목록이 그대로입니다. 응답을 기다리지 않고 목록에서 먼저 지운 뒤, 실패하면 되돌리게 바꿉니다. 실습 대상 파일을 고치므로 **원복이 필수**입니다. 이 작업 디렉터리는 git 이 아니므로 먼저 백업을 만드세요.

```bash
cp labs/frontend/src/components/MyLoans.tsx /tmp/MyLoans.backup.tsx
```

**준비.** 백엔드는 1단계 상태 그대로입니다. 대출 한 건을 만들고, 브라우저에서 로그인한 뒤 `http://localhost:3000/loans` 를 엽니다. 목록에 대출이 1건 보여야 합니다.

```bash
curl -s -b $J -X POST -H 'Content-Type: application/json' -d '{"memberId":1,"bookId":1}' $B/loans
```

**예측.** 아래 두 줄을 먼저 적으세요. DevTools Network 탭의 Throttling 을 `Slow 3G` 로 바꾸면 응답이 몇 초 걸려서 중간 상태를 볼 수 있습니다. (이 문서의 값은 Playwright 로 응답을 1초 늦춰서 확인했습니다.)

1. 지금 코드에서 "반납" 을 누르고 응답이 오기 **전에** 그 대출은 목록에 남아 있습니까?
2. 낙관적 업데이트를 넣은 뒤, 로그인이 끊긴 상태에서 "반납" 을 누르면 목록은 어떻게 움직입니까? (사라졌다가? 그대로?)

**구현.** `MyLoans.tsx` 의 `"use client";` 줄 **아래** import 묶음에 `import type { Loan } from "@/api/client";` 를 추가하고(`"use client"` 는 파일의 첫 문장이어야 하므로 그 앞에 import 를 두면 안 됩니다), `giveBack` 정의를 아래로 바꿉니다. `____` 를 채우세요. 힌트는 주석에 있습니다.

```tsx
const giveBack = useMutation({
  mutationFn: library.giveBack,
  onMutate: async (loanId: number) => {
    await queryClient.____({ queryKey: ["loans", id] }); // (1) 진행 중인 조회가 낙관적 값을 덮어쓰지 못하게 멈춘다
    const previous = queryClient.getQueryData<Loan[]>(["loans", id]);
    queryClient.____<Loan[]>(["loans", id], (old) => old?.filter((loan) => loan.id !== ____)); // (2) 캐시를 직접 고친다 (3) 무엇을 뺄까
    return { previous };
  },
  onError: (_error, _loanId, context) => queryClient.setQueryData(["loans", id], context?.____), // (4) 되돌릴 값
  onSettled: () => queryClient.____({ queryKey: ["loans"] }), // (5) 끝나면(성공이든 실패든) 서버의 사실로 다시 맞춘다
});
```

<details>
<summary>정답과 예측 확인 (구현·실험을 마친 뒤에 여세요)</summary>

```tsx
const giveBack = useMutation({
  mutationFn: library.giveBack,
  onMutate: async (loanId: number) => {
    await queryClient.cancelQueries({ queryKey: ["loans", id] }); // 진행 중인 조회가 낙관적 값을 덮어쓰지 못하게
    const previous = queryClient.getQueryData<Loan[]>(["loans", id]);
    queryClient.setQueryData<Loan[]>(["loans", id], (old) => old?.filter((loan) => loan.id !== loanId));
    return { previous };
  },
  onError: (_error, _loanId, context) => queryClient.setQueryData(["loans", id], context?.previous),
  onSettled: () => queryClient.invalidateQueries({ queryKey: ["loans"] }),
});
```

이 코드로 `pnpm typecheck` 가 통과하는 것을 확인했습니다. 아래는 Playwright 로 반납 응답을 1초 늦추고, 클릭 300밀리초 뒤와 응답 뒤의 목록 항목 수를 센 결과입니다.

| 경우 | 원래 코드 | 낙관적 코드 |
|---|---|---|
| 성공 (대출 1건 반납) | 클릭 300ms 뒤 **1건**, 응답 뒤 0건 | 클릭 300ms 뒤 **0건**, 응답 뒤 0건 |
| 로그인이 끊긴 뒤 반납 (401) | 300ms 뒤 1건, 응답 뒤 1건. 알림 "로그인이 필요합니다", 반납 버튼 비활성 | 300ms 뒤 **0건**, 응답 뒤 **1건**(되돌아옴). 같은 알림, 같은 버튼 비활성 |
| 다른 탭에서 이미 반납됨 (409) | 300ms 뒤 1건, 응답 뒤 **1건**(이미 반납된 항목이 남음). 알림 "이미 반납된 대출입니다: 번호", 반납 버튼은 계속 활성 | 300ms 뒤 0건, 응답 뒤 **0건**. 같은 알림 |

- 예측 1번: 남아 있습니다. 응답이 와서 `["loans"]` 를 다시 가져온 뒤에야 사라집니다.
- 예측 2번: 낙관적 코드에서는 먼저 사라졌다가 401 응답이 오면 **되돌아옵니다.** `onError` 가 `onMutate` 가 저장해 둔 `previous` 로 되돌리기 때문입니다. 이때 `providers.tsx` 의 `MutationCache.onError` 가 `["me"]` 를 `null` 로 바꿔서 반납 버튼이 비활성이 되는 것까지 같이 보입니다.
- 409 줄이 `onSettled` 의 역할을 보여 줍니다. 원래 코드는 성공(`onSuccess`)일 때만 다시 가져와서, 실패하면 이미 반납된 항목이 목록에 **그대로 남고** 버튼도 활성이라 또 눌러도 같은 409 가 납니다. 낙관적 코드는 되돌린 뒤에도 `onSettled` 가 다시 가져와서 서버의 사실(이미 반납됨)에 맞춰집니다. 낙관적 값이 틀렸더라도 최종 화면은 서버가 정합니다.
- 이 세 경우는 `labs/frontend/src/lib/query-design.test.ts` 의 "낙관적 업데이트" 테스트(성공, 401)로도 고정해 두었습니다. `pnpm test` 에 포함됩니다.

</details>

**실험.** 구현한 뒤 세 경우를 직접 만들어 봅니다. 각 실험 전에 `curl ... $B/loans` 로 대출을 새로 만들고 `/loans` 를 새로고침하세요.

- 성공: Throttling 을 `Slow 3G` 로 바꾸고 "반납" 을 누릅니다. 항목이 곧바로 사라지는지 봅니다.
- 401: DevTools 의 Application 탭 → Cookies → `localhost` 에서 `JSESSIONID` 를 지웁니다. 화면은 아직 로그인 상태로 보입니다. "반납" 을 누르고 항목이 어떻게 움직이는지 봅니다. (이 문서의 값은 Playwright 로 쿠키를 지워서 확인했고, 백엔드에서 `POST /auth/logout` 으로 세션을 끊어도 같은 결과였습니다.)
- 409: 대출을 만들고 `/loans` 에 목록이 보이는 상태에서, 터미널에서 `curl -s -b $J -X POST $B/loans/대출번호/return` 으로 먼저 반납합니다. 그 다음 화면에서 "반납" 을 누릅니다. 대출 번호는 `/loans` 목록의 "대출 #번호" 입니다.

끝나면 원복합니다.

```bash
cp /tmp/MyLoans.backup.tsx labs/frontend/src/components/MyLoans.tsx
pnpm -C labs/frontend typecheck       # 오류 없이 끝나야 함
```

### 3단계. 오프셋과 커서를 예측하기 → 테스트로 확인하기 (1~1.5시간)

2주에서 "페이지를 넘기는 사이 데이터가 바뀌면 오프셋은 중복이 생긴다" 를 글로 봤습니다. 이번에는 숫자로 확인합니다. 책이 id `1~5` 이고 목록은 **최근 등록이 앞**(id 내림차순)이며 한 페이지에 2개입니다. 사용자가 1페이지(`[5, 4]`)를 본 뒤에, 누군가 책을 한 권 등록해서 id `6` 이 생겼습니다.

| 방식 | 2페이지를 요청하는 법 | 내 예측: 2페이지에 오는 id |
|---|---|---|
| 오프셋 | `offset=2&limit=2` | |
| 커서 | "마지막으로 본 id `4` 보다 오래된 것" 을 `limit=2` 개 | |

예측을 적은 뒤, 이 주차에 추가한 테스트를 읽지 말고 **먼저 실행**해서 통과를 확인하고, 그 다음 테스트 본문의 `expect` 를 읽어 예측과 비교하세요.

```bash
pnpm -C labs/frontend exec vitest run src/lib/query-design.test.ts -t "오프셋은"
```

<details>
<summary>정답</summary>

- 오프셋: `[4, 3]`. 앞에 6 이 생겨서 모든 항목이 한 칸씩 밀리므로, 1페이지에서 이미 본 4 가 2페이지의 첫 항목으로 다시 옵니다. 1~2페이지를 이어 붙이면 `[5, 4, 4, 3]` 이고, 같은 `id` 를 `key` 로 쓰는 항목이 두 번 나옵니다.
- 커서: `[3, 2]`. "4 보다 오래된 것" 이라는 기준이 데이터가 밀려도 변하지 않습니다. `[5, 4, 3, 2]` 로 중복도 누락도 없습니다.
- 반대로 새로 등록된 6 은 커서 방식에서 이미 지나온 쪽에 있어서 이번 스크롤에서는 보이지 않습니다. 새 항목을 보려면 맨 앞부터 다시 가져와야 합니다.
- 테스트 이름과 위치: `src/lib/query-design.test.ts` 의 "오프셋은 페이지 사이에 새 항목이 생기면 같은 항목이 두 번 나오고, 커서는 그렇지 않다". 서버는 같은 파일의 `fakePagedServer` 가 흉내 냅니다.

</details>

**빈칸 채우기.** 위 커서 응답 모양 `{ items, nextCursor }` 를 `useInfiniteQuery` 에 연결합니다. `____` 를 채우세요. (`nextCursor` 가 `null` 이면 마지막 페이지입니다.)

```ts
useInfiniteQuery({
  queryKey: ["books", "infinite"],
  queryFn: ({ pageParam }) => getPage(`limit=2${pageParam ? `&cursor=${pageParam}` : ""}`),
  initialPageParam: null as number | null,
  getNextPageParam: (last) => ____,
});
```

<details>
<summary>정답</summary>

```ts
getNextPageParam: (last) => last.nextCursor, // null(또는 undefined)이면 "더 없음" 이고 hasNextPage 가 false 가 된다
```

서버가 "마지막" 을 `nextCursor: null` 로 알려 주므로 그 값을 그대로 돌려주면 됩니다. 반환값이 `null` 이나 `undefined` 이면 끝이고, 그 밖의 값은 다음 요청의 `pageParam` 이 됩니다. 서버가 "마지막" 을 빈 배열이나 `hasMore: false` 로 알려 준다면 `last.hasMore ? last.nextCursor : undefined` 처럼 그 규칙을 이 함수가 번역해야 합니다. 테스트의 "커서를 이어 붙여 끝까지 가져온다" 가 이 코드로 `[[5, 4], [3, 2], [1]]` 세 페이지를 가져오고 `hasNextPage` 가 `false` 가 되는 것을 확인합니다.

</details>

### 4단계 (선택, 30분). 화면에서 보기

백엔드는 페이지네이션을 모르므로, 임시 Route Handler 가 `GET /books` 를 **전부 받아서 잘라 주는** 흉내 서버를 만들고 화면 두 개를 얹습니다. 책이 5권 이상 있어야 하므로 3권을 더 등록합니다.

```bash
for i in 3 4 5; do curl -s -o /dev/null -b $J -X POST -H 'Content-Type: application/json' -d "{\"isbn\":\"P$i\",\"title\":\"책 $i\",\"author\":\"a\"}" $B/books; done
```

세 파일을 만듭니다. 끝나면 `labs/frontend/src/app/scratch`, `labs/frontend/src/app/scratch-api` 폴더를 **삭제**하세요.

```ts
// labs/frontend/src/app/scratch-api/books/route.ts
import { createApiClient } from "@/api/client";
import { createLibrary } from "@/api/library";

const library = createLibrary(createApiClient("http://localhost:8080")); // 서버에서 백엔드를 직접 부른다

// 실습용 흉내 서버. 백엔드는 페이지네이션을 모르므로 전부 받아서 id 내림차순으로 자르는 임시방편이다(실서버가 지원하는 모양이 아니다).
export async function GET(request: Request) {
  const params = new URL(request.url).searchParams;
  const limit = Number(params.get("limit") ?? 2);
  const all = (await library.listBooks()).sort((a, b) => b.id - a.id);
  if (params.has("offset")) {
    const offset = Number(params.get("offset"));
    return Response.json({ items: all.slice(offset, offset + limit), total: all.length });
  }
  const before = params.get("cursor"); // "이 id 보다 오래된 것부터"
  const rest = before ? all.filter((b) => b.id < Number(before)) : all;
  const items = rest.slice(0, limit);
  return Response.json({ items, nextCursor: rest.length > limit ? items.at(-1)!.id : null });
}
```

```tsx
// labs/frontend/src/app/scratch/infinite/page.tsx
"use client";

import { useInfiniteQuery } from "@tanstack/react-query";
import type { Book } from "@/api/client";
import { Button } from "@/components/Button";

type Page = { items: Book[]; nextCursor: number | null };

export default function InfiniteBooksPage() {
  const books = useInfiniteQuery({
    queryKey: ["books", "infinite"],
    queryFn: async ({ pageParam }): Promise<Page> => {
      const res = await fetch(`/scratch-api/books?limit=2${pageParam ? `&cursor=${pageParam}` : ""}`);
      return res.json();
    },
    initialPageParam: null as number | null, // 첫 페이지는 커서 없이
    getNextPageParam: (last) => last.nextCursor, // null 이면 더 없음
  });

  if (books.isPending) return <p className="text-muted">불러오는 중...</p>;
  if (books.isError) return <p className="text-danger">{books.error.message}</p>;
  return (
    <section className="flex flex-col gap-gutter">
      <ul className="flex flex-col gap-field">
        {books.data.pages.flatMap((page) => page.items).map((b) => (
          <li key={b.id} className="rounded-card border border-line bg-surface p-gutter">
            #{b.id} {b.title}
          </li>
        ))}
      </ul>
      <Button type="button" disabled={!books.hasNextPage || books.isFetchingNextPage} onClick={() => books.fetchNextPage()}>
        {books.isFetchingNextPage ? "불러오는 중..." : books.hasNextPage ? "더 보기" : "마지막입니다"}
      </Button>
    </section>
  );
}
```

```tsx
// labs/frontend/src/app/scratch/paged/page.tsx
"use client";

import { keepPreviousData, useQuery } from "@tanstack/react-query";
import { useState } from "react";
import type { Book } from "@/api/client";
import { Button } from "@/components/Button";

const SIZE = 2;

export default function PagedBooksPage() {
  const [page, setPage] = useState(1);
  const books = useQuery({
    queryKey: ["books", { page }], // 페이지 번호도 키의 일부
    queryFn: async (): Promise<{ items: Book[]; total: number }> => {
      const res = await fetch(`/scratch-api/books?offset=${(page - 1) * SIZE}&limit=${SIZE}`);
      return res.json();
    },
    placeholderData: keepPreviousData, // 다음 페이지가 오기 전까지 이전 페이지를 그대로 보여 준다
  });

  if (books.isPending) return <p className="text-muted">불러오는 중...</p>;
  if (books.isError) return <p className="text-danger">{books.error.message}</p>;
  const last = Math.ceil(books.data.total / SIZE);
  return (
    <section className="flex flex-col gap-gutter">
      <ul className="flex flex-col gap-field" data-stale={books.isPlaceholderData}>
        {books.data.items.map((b) => (
          <li key={b.id} className="rounded-card border border-line bg-surface p-gutter">
            #{b.id} {b.title}
          </li>
        ))}
      </ul>
      <div className="flex items-center gap-gutter">
        <Button type="button" disabled={page === 1} onClick={() => setPage(page - 1)}>
          이전
        </Button>
        <span>
          {page} / {last}
        </span>
        <Button type="button" disabled={books.isPlaceholderData || page >= last} onClick={() => setPage(page + 1)}>
          다음
        </Button>
      </div>
    </section>
  );
}
```

`http://localhost:3000/scratch/infinite` 에서 "더 보기" 를 끝까지 누르고, `http://localhost:3000/scratch/paged` 에서 "다음" 을 누르며 Network 탭의 `/scratch-api/books` 요청을 봅니다. 먼저 예측하세요. 책이 6권(id 1~6)일 때 "더 보기" 는 몇 번 누르면 "마지막입니다" 가 되고 요청은 몇 번 나갑니까? 페이지 번호 화면에서 다음 → 이전을 누르면 요청은 몇 번 더 나갑니까?

<details>
<summary>확인 결과</summary>

책이 6권(id 1~6: 1단계 8번에서 한 권을 등록했고 4단계에서 3권을 더한 상태)일 때 Playwright 로 확인한 값입니다. 5권이어도 요청 횟수와 누르는 횟수는 같습니다(페이지 3개).

- 무한: 처음 2건(`#6`, `#5`), "더 보기" 두 번에 4건 → 6건이 되고 버튼이 "마지막입니다" 로 바뀝니다. 요청은 `limit=2`, `limit=2&cursor=5`, `limit=2&cursor=3` 세 번입니다.
- 페이지 번호: 다음을 누르고(이 요청을 0.8초 늦췄습니다) 200밀리초 뒤에는 쪽 표시가 `2 / 3` 인데 목록은 아직 1페이지 항목이고 `data-stale="true"` 입니다. 응답이 오면 2페이지 항목으로 바뀌고 `data-stale="false"` 가 됩니다. 이전을 누르면 1페이지 항목이 곧바로 보이고(캐시) `offset=0` 요청이 한 번 더 나갑니다(오래된 캐시를 뒤에서 갱신). 요청은 모두 `offset=0`, `offset=2`, `offset=0` 세 번입니다.
- 같은 책 목록인데 키가 `["books", "infinite"]` 와 `["books", { page }]` 로 달라서 캐시 항목도 따로입니다. 그런데 둘 다 `["books"]` 로 시작하므로 책을 등록하면 한꺼번에 무효화됩니다. 키를 계층으로 설계하는 이유입니다.

</details>

---

## 1. 서버 상태는 "내 것이 아닌 데이터의 사본"

`useState` 의 값은 이 화면이 주인입니다. 반면 책 목록, 대출 목록은 **서버가 주인**이고 프론트가 가진 것은 사본입니다. 사본에는 반드시 따라오는 질문이 있습니다.

| 질문 | TanStack Query 의 대답 |
|---|---|
| 이 사본이 어느 요청의 결과인가? | **쿼리 키**(`["books", keyword]`). 같은 키는 같은 항목을 가리킵니다 |
| 사본이 얼마나 오래돼도 믿을 만한가? | **`staleTime`**. 이 시간 안이면 신선(fresh)하고 요청하지 않습니다 |
| 오래됐으면 언제 다시 가져오나? | 컴포넌트가 마운트될 때, 창에 다시 포커스가 올 때, 네트워크가 다시 연결될 때(기본값이 모두 켜져 있습니다), 그리고 **무효화**될 때 |
| 아무도 안 쓰는 사본은 언제 버리나? | **`gcTime`**. 기본 5분입니다 |
| 서버의 데이터를 내가 바꿨다면? | 다시 가져오게 하거나(**무효화**), 캐시를 직접 고칩니다(**`setQueryData`**, 낙관적 업데이트) |
| 가져오는 중이거나 실패했을 때 화면은? | `status`, `fetchStatus`, `error` |

HTTP 캐시(`Cache-Control`, `ETag`)와 비슷한 고민이지만 위치가 다릅니다. 브라우저 HTTP 캐시는 요청 단위로 응답을 재사용하고, 쿼리 캐시는 **화면이 쓰는 데이터 단위**로 재사용합니다. 참조 API 는 `Cache-Control: no-cache, no-store, max-age=0, must-revalidate` 를 내리고(`curl -i localhost:8080/books` 로 확인할 수 있습니다) `ETag` 는 내리지 않으므로 브라우저 HTTP 캐시는 일부러 꺼져 있고, 재사용의 책임은 전부 쿼리 캐시에 있습니다.

## 2. 지금 코드가 내린 결정 읽기

`../labs/frontend/src` 에서 데이터 패칭에 관한 선택은 놀랄 만큼 적습니다. 아래가 전부입니다. (`grep -rn 'staleTime\|gcTime\|useInfiniteQuery\|placeholderData' labs/frontend/src` 는 `query-design.test.ts` 를 빼면 아무것도 찾지 못합니다.)

| 결정 | 위치 | 내용 |
|---|---|---|
| 기본 옵션 | `app/providers.tsx` | `queries: { retry: false }` 하나. 이유 주석: "4xx 는 다시 해도 같다" |
| 쓰기 401 → 로그아웃 표시 | `app/providers.tsx` | `MutationCache.onError` 가 `["me"]` 를 `null` 로 |
| `staleTime` | (없음) | **기본값 0.** 데이터는 받자마자 오래된 것으로 취급됩니다 |
| 책 목록 키 | `components/BookList.tsx` | `["books", keyword]` |
| 대출 목록 키 | `components/MyLoans.tsx` | `["loans", id]`, 그리고 `enabled` 로 입력이 정수일 때만 요청 |
| 내 정보 키 | `lib/useMe.ts` | `["me"]`. 401 은 에러가 아니라 `null` |
| 책 등록 후 | `components/BookForm.tsx` | `invalidateQueries({ queryKey: ["books"] })` |
| 대출·반납 후 | `LendForm.tsx`, `MyLoans.tsx` | `invalidateQueries({ queryKey: ["loans"] })` |
| 로그인·로그아웃 후 | `components/AuthBar.tsx` | `setQueryData(["me"], user \| null)` |

먼저 실제 코드를 봅니다.

```tsx
// app/providers.tsx
function makeQueryClient() {
  const client: QueryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } }, // 4xx 는 다시 해도 같다
    mutationCache: new MutationCache({
      // 쓰기 요청이 401 이면(세션 만료 등) 로그인 상태 표시도 즉시 로그아웃으로
      onError: (error) => {
        if (error instanceof ApiError && error.status === 401) client.setQueryData(["me"], null);
      },
    }),
  });
  return client;
}
```

```tsx
// components/BookList.tsx
const books = useQuery({ queryKey: ["books", keyword], queryFn: () => library.listBooks(keyword) });
```

```tsx
// components/MyLoans.tsx
const loans = useQuery({
  queryKey: ["loans", id],
  queryFn: () => library.activeLoans(id),
  enabled: memberId.trim() !== "" && Number.isInteger(id),
});
const giveBack = useMutation({
  mutationFn: library.giveBack,
  onSuccess: () => queryClient.invalidateQueries({ queryKey: ["loans"] }),
});
```

### 2-1. 키에 "결과를 바꾸는 모든 입력" 이 들어 있다

`["books", keyword]` 에서 `keyword` 가 바뀌면 다른 키, 곧 다른 캐시 항목입니다. 그래서 1단계 2~3번에서 `clean` 검색은 새 요청이었고, 빈 검색으로 돌아오자 이전 결과가 즉시 보였습니다. **쿼리 함수가 읽는 값은 모두 키에 넣습니다.** 키에 빠진 입력이 있으면 입력이 바뀌어도 같은 캐시를 보여 주는 버그가 됩니다.

키는 값으로 비교합니다. 내부적으로 JSON 문자열로 바꿔 비교하므로 숫자 `1` 과 문자열 `"1"` 은 **다른 키**이고, 객체 안의 속성 순서는 상관없습니다. (`hashKey(["loans", 1])` 는 `["loans",1]`, `hashKey(["loans", "1"])` 는 `["loans","1"]` 입니다. `["loans", 1]` 로 저장한 항목을 `["loans", "1"]` 로 읽으면 `undefined` 입니다. 직접 확인했습니다.) `MyLoans` 가 입력 문자열이 아니라 `Number(memberId)` 를 키에 넣는 것이 이 때문에 의미가 있습니다. 같은 회원이 `"1"` 과 `"01"` 로 두 항목이 되지 않습니다.

키는 **계층**으로 짓습니다. `["loans"]` 는 모든 대출 목록의 접두사이고 `["loans", id]` 는 한 회원의 것입니다. 무효화와 조회 필터가 접두사로 동작하기 때문입니다. `LendForm` 이 `["loans"]` 하나로 어느 회원의 목록이든 무효화하는 것이 그 예입니다.

한계도 있습니다. 키가 **문자열 리터럴 배열**이라서 `["book", ...]` 처럼 오타를 내면 컴파일러도 런타임도 조용합니다. 무효화가 아무것도 맞추지 못한 채 지나갑니다. 키가 여러 파일에 흩어지기 시작하면 키를 만드는 함수 한 곳에 모으는 방법이 흔합니다(개념 소개이며 이 저장소에는 없습니다).

### 2-2. `staleTime` 기본값 0 의 결과

1단계 4~6번이 이 결정의 결과입니다. 데이터가 받자마자 오래된 것이므로, 화면이 새로 마운트될 때(링크 이동), 탭에 돌아올 때마다 **다시 가져옵니다.** 화면은 캐시를 먼저 그리고 뒤에서 갱신하므로 사용자는 느끼지 못하지만, 서버에는 요청이 쌓입니다. `useMe` 를 쓰는 `SubmitButton` 이 페이지마다 마운트되어서 `/auth/me` 가 페이지 이동마다 나갑니다.

이 결정은 나쁜 것이 아닙니다. 책 목록은 다른 사람이 언제든 바꿀 수 있어서 오래된 값을 보여 주는 비용이 크고, 데이터가 작아서 재요청도 쌉니다. 반대로 `["me"]` 는 로그인·로그아웃 때 이 앱이 직접 `setQueryData` 로 맞추므로 신선하다고 믿어도 됩니다. 실제로 확인합니다. `useMe.ts` 의 `queryKey: ["me"],` 아래에 `staleTime: 60_000,` 를 임시로 넣고 같은 조작을 하면, 1단계 4번(`책 → 회원`)은 `/api/auth/me` 요청이 **0번**, 5번은 `/api/books` 1번만, 6번(탭 복귀)도 `/api/books` 1번만 나갑니다.

대신 치르는 값은 **서버에서 일어난 변화를 최대 `staleTime` 동안 못 본다** 는 것입니다. 로그인 세션이 서버에서 먼저 끊겨도 한동안 화면은 로그인 상태입니다. 이 앱은 그 틈을 `MutationCache.onError` 의 401 처리가 메웁니다. 쓰기를 시도하는 순간 `["me"]` 를 `null` 로 바꿉니다. 결정은 항상 짝으로 읽으세요. `staleTime` 을 늘리는 선택과 "틀렸을 때 어디서 바로잡는가" 가 함께 있어야 합니다.

`gcTime`(기본 5분)은 **관찰하는 컴포넌트가 하나도 없는** 캐시 항목을 얼마나 두는지입니다. 1단계 3번에서 `["books", ""]` 로 돌아왔을 때 이전 결과가 즉시 보인 것은 이 항목이 5분 안에 있었기 때문입니다.

### 2-3. 무효화와 직접 쓰기, 지금 코드의 구분

| 상황 | 이 앱의 선택 | 이유 |
|---|---|---|
| 책 등록 | `invalidateQueries(["books"])` | 서버가 `id` 를 정하고 정렬·필터 결과가 어떻게 될지 프론트가 모릅니다. 서버에게 다시 묻는 편이 안전합니다 |
| 대출·반납 | `invalidateQueries(["loans"])` | 같은 이유. 한도(3권)나 중복 같은 규칙은 서버가 압니다 |
| 로그인 | `setQueryData(["me"], user)` | 응답이 곧 `["me"]` 가 돌려줄 값입니다. 다시 물을 필요가 없습니다 |
| 로그아웃·401 | `setQueryData(["me"], null)` | "비로그인" 은 서버 응답이 아니라 우리가 정한 정상 상태입니다 |

기준 한 줄: **서버가 계산한 결과이면 무효화하고, 내가 이미 정확한 값을 가지고 있으면 직접 씁니다.** 무효화는 "이 캐시는 이제 오래됐다" 고 표시하고, 지금 화면에 쓰이는(active) 쿼리만 즉시 다시 가져옵니다. 쓰이지 않는 쿼리는 표시만 해 두고 다음에 쓰일 때 가져옵니다. 1단계 8~9번이 정확히 이것입니다. `query-design.test.ts` 의 "invalidate 는 '지금 화면에 쓰이는(active)' 쿼리만 바로 다시 가져온다" 가 같은 동작을 고정합니다.

## 3. 로딩·에러 상태 설계

쿼리의 상태는 두 축입니다. 헷갈리는 이유는 한 축으로 생각하기 때문입니다.

| 축 | 값 | 뜻 |
|---|---|---|
| `status` | `pending` / `success` / `error` | **데이터가 있는가** (한 번도 못 가져왔나, 가져왔나, 실패했나) |
| `fetchStatus` | `fetching` / `paused` / `idle` | **지금 요청이 나가는 중인가** |

여기서 파생되는 값입니다. `isPending` 은 `status === "pending"`, `isFetching` 은 `fetchStatus === "fetching"`, `isLoading` 은 둘 다(처음 가져오는 중)입니다. 이 값들을 `query-design.test.ts` 의 "로딩 상태" 테스트로 확인했습니다.

| 상황 | status | fetchStatus |
|---|---|---|
| 처음 가져오는 중 | pending | fetching (`isLoading`) |
| 가져온 뒤 뒤에서 갱신하는 중 | success | fetching |
| **`enabled: false` 라서 아예 시작도 안 함** | **pending** | **idle** |
| 가져오기 실패 | error | idle |

세 번째 줄이 함정입니다. `isPending` 은 "데이터가 아직 없다" 일 뿐이라서 `enabled: false` 인 쿼리에서는 **영원히 true** 입니다. 두 화면이 서로 다른 선택을 했습니다.

```tsx
// BookList.tsx: 키워드는 비어 있어도 요청하므로 pending 은 곧 "처음 가져오는 중"
{books.isPending && <p className="text-muted">불러오는 중...</p>}
// MyLoans.tsx: 회원 id 입력이 비면 쿼리가 꺼지므로 isFetching 을 쓴다
{loans.isFetching && <p className="text-muted">불러오는 중...</p>}
```

`MyLoans` 가 `isPending` 을 썼다면 회원 id 칸을 지웠을 때 "불러오는 중..." 이 영원히 보입니다. 반면 `isFetching` 은 뒤에서 갱신할 때도 켜져서, 1단계 8번 이후 `/loans` 에서 대출을 등록하면 목록이 있는 채로 "불러오는 중..." 이 잠깐 같이 보입니다. 화면마다 "처음 로딩" 과 "갱신 중" 을 같은 문구로 보여 줄지, 다르게 보여 줄지(스켈레톤 vs 작은 표시)를 정해야 합니다. 이 결정에 정답은 없지만 **의식적으로** 정해야 합니다.

빈 상태도 같은 문제입니다. `BookList` 는 `books.data?.length === 0` 일 때만 "검색 결과가 없습니다" 를 보여 줍니다. `data` 가 `undefined`(아직 모름)인 순간과 `[]`(없음이 확인됨)를 구분하는 장치입니다. `!books.data?.length` 로 썼다면 로딩 중에도 "결과 없음" 이 번쩍였을 것입니다.

**에러 표시**는 7주에서 정했습니다. 쿼리의 `error` 상태를 `ErrorNotice` 로 그대로 그립니다(`<ErrorNotice error={books.error} />`). 이 주차에서 더할 것은 **재시도 정책**입니다. 전역 `retry: false` 는 4xx 에는 맞지만, 서버 일시 장애(5xx)나 네트워크 끊김까지 한 번도 다시 시도하지 않는 것은 아깝습니다. 다음과 같이 5xx 와 네트워크 오류만 재시도하는 규칙을 쓸 수 있습니다. `query-design.test.ts` 가 404 는 1번, 500 은 3번(처음 1번 + 재시도 2번)만 호출하는 것을 확인합니다.

```ts
const retry = (count: number, error: Error) => !(error instanceof ApiError && error.status < 500) && count < 2;
```

이 저장소는 이 규칙을 쓰지 않습니다. 쓰려면 `providers.tsx` 의 `retry: false` 를 바꾸고, 9주에서 본 "백엔드가 꺼진 화면" 이 즉시 오류가 아니라 몇 초 뒤에 오류로 바뀌는 부작용도 같이 받아들여야 합니다. 에러가 화면이 아니라 **서버에서 그리는 경우**(`error.tsx`)는 [11주](week11-next-server-boundary.md)에서 다룹니다.

## 4. 낙관적 업데이트

"응답을 기다리지 않고 성공할 것으로 가정해 화면을 먼저 바꾸고, 실패하면 되돌린다." 흐름은 세 단계이고 2단계 실습 코드가 그것입니다.

| 시점 | 하는 일 | 빠뜨리면 |
|---|---|---|
| `onMutate` | 진행 중인 조회를 취소 → 현재 캐시를 저장 → 캐시를 낙관적 값으로 고침 | 취소를 빠뜨리면 이미 나간 조회의 응답이 도착해서 낙관적 값을 덮어씁니다 |
| `onError` | 저장해 둔 값으로 캐시를 되돌림 | 실패했는데 화면은 성공한 것처럼 남습니다 |
| `onSettled` | 무효화해서 서버의 사실로 다시 맞춤 | 낙관적 값이 틀렸거나 그 사이 다른 변화가 있었어도 화면이 계속 틀립니다 |

`onSettled` 는 `onSuccess` 가 아니라 **성공이든 실패든** 항상 실행됩니다. 2단계 표의 409 줄에서 본 것처럼, 되돌린 뒤 다시 가져와서 최종 화면은 서버가 정합니다.

**언제 쓰는가.** 세 조건이 모두 맞을 때만 쓰세요.

1. 실패가 드뭅니다. 실패할 때마다 화면이 튀는 비용을 사용자가 치릅니다.
2. 성공했을 때의 결과를 **프론트가 정확히 안다**. 반납은 "목록에서 이 항목이 빠진다" 로 결과가 확정적입니다.
3. 되돌리기가 쉽습니다. 이전 캐시를 저장해 두는 것으로 충분합니다.

이 앱에서 `giveBack` 은 맞고, 나머지는 맞지 않습니다. 대출(`lend`)은 서버가 `id` 를 정하고 이미 대출 중(409)이나 3권 한도(409)를 서버가 판단하므로 프론트가 결과를 모릅니다. 책 등록(`createBook`)도 `id` 와 isbn 중복(409)이 서버 몫입니다. 이런 것까지 낙관적으로 만들면 "일단 목록에 보였다가 사라지는" 화면이 자주 생깁니다.

낙관적 업데이트의 다른 방식도 있습니다. 캐시를 건드리지 않고 `mutation.variables` 와 `isPending` 으로 "이 항목은 반납 처리 중" 을 그 자리에서만 보여 주는 방식입니다(개념 소개이며 이 저장소에서 실행해 보지 않았습니다). 되돌릴 캐시가 없어서 `onError` 가 필요 없지만, 목록을 여러 화면에서 같이 쓸 때는 캐시를 고치는 쪽이 일관됩니다.

## 5. 페이지네이션

**참조 API 에는 없습니다.** `GET /books` 는 `keyword` 만 받고 `page`, `size` 는 조용히 무시합니다(2주). 이 절은 다른 백엔드 API 를 만났을 때를 위한 설계 지식이고, 코드 확인은 테스트와 4단계의 흉내 서버에서만 했습니다.

### 5-1. 두 가지 UI, 두 가지 쿼리 모델

| UI | 서버 방식 | 쿼리 모델 | 키 |
|---|---|---|---|
| 페이지 번호(`이전 1 2 3 다음`) | 오프셋(총 개수 필요) | `useQuery`, 페이지마다 **다른 키** | `["books", { page }]` |
| "더 보기" / 무한 스크롤 | 커서(또는 오프셋) | `useInfiniteQuery`, 페이지를 **한 항목 안에 이어 붙임** | `["books", "infinite"]` |

**페이지 번호**에서는 이동할 때 새 키라서 `isPending` 이 되어 목록이 깜빡 비는 것이 문제입니다. `placeholderData: keepPreviousData` 를 주면 다음 페이지가 도착하기 전까지 **이전 페이지를 그대로 보여 주고**, 그동안 `isPlaceholderData` 가 `true` 입니다. 이 값으로 "다음" 버튼을 잠그거나 목록을 흐리게 표시합니다. 4단계에서 `data-stale="true"` 로 확인했고, 테스트(`keepPreviousData: ...`)도 같은 동작을 고정합니다.

**더 보기**에서는 `useInfiniteQuery` 가 `data.pages` 배열에 페이지를 쌓습니다. 세 가지를 정합니다.

- `initialPageParam`: 첫 요청의 파라미터. 커서 방식이면 "커서 없음"(`null`)
- `getNextPageParam(lastPage)`: 마지막 페이지에서 다음 요청의 파라미터를 꺼냅니다. `undefined` 를 돌려주면 끝이고 `hasNextPage` 가 `false` 가 됩니다
- 화면은 `data.pages.flatMap(page => page.items)` 로 펼칩니다

주의할 점입니다. 이 캐시 항목은 **모든 페이지가 한 덩어리**라서 무효화하면 가져온 페이지를 **처음부터 순서대로 전부 다시** 가져옵니다. `query-design.test.ts` 의 "무효화하면 이미 가져온 페이지를 처음부터 전부 다시 가져온다" 가 페이지 3개를 가져온 뒤 무효화하면 호출이 3번에서 6번이 되는 것을 확인합니다. 20페이지를 내려갔다면 20번의 요청이 한꺼번에 나간다는 뜻입니다. 페이지 수가 많아질 수 있는 목록이면 `maxPages` 옵션으로 보관 페이지 수를 제한하는 방법이 있습니다.

### 5-2. 오프셋 vs 커서를 프론트 관점에서

2주 표에 프론트의 비용을 더합니다. 3단계에서 확인한 사실이 맨 위입니다.

| | 오프셋 | 커서 |
|---|---|---|
| 페이지 사이에 새 항목이 생기면 | **같은 항목이 두 번** 오거나 건너뜁니다(`[5, 4, 4, 3]`) | 중복도 누락도 없습니다(`[5, 4, 3, 2]`) |
| 임의 페이지로 이동 | 가능(`page=7`) | 불가능. 앞에서부터 따라가야 합니다 |
| "총 N 페이지" 표시 | 총 개수(`total`)가 필요합니다 | 어렵습니다 |
| 어울리는 UI | 페이지 번호 | 더 보기, 무한 스크롤 |
| 프론트가 할 일 | 페이지 번호를 URL 쿼리나 상태에 두고 키에 넣기 | 서버가 준 커서를 **그대로** 다음 요청에 넣기(내용을 해석하지 않습니다) |

오프셋으로 "더 보기" 를 만들면 3단계의 중복이 그대로 화면에 나오고 `key` 로 쓰는 id 도 겹칩니다. 서버가 오프셋만 준다면 id 로 중복을 걸러 내는 코드를 프론트가 떠안게 됩니다.

### 5-3. 백엔드와 협의할 질문

페이지네이션이 생기는 API 를 받을 때, 응답 모양 말고 **계약**으로 확인합니다.

1. 정렬 기준이 무엇이고, 같은 값일 때의 **두 번째 기준**(예: `id`)이 있어서 순서가 안정적인가요? (없으면 페이지 사이에서 항목이 흔들립니다.)
2. `limit` 의 기본값과 **최대값**은? 최대값을 넘기면 에러인가요, 조용히 잘리나요?
3. 총 개수(`total`)를 주나요? 주는 비용(느린 `count`)을 서버가 감수하나요?
4. 커서는 **불투명한 문자열**인가요? 만료되나요? 만료되면 어떤 상태 코드인가요?
5. 마지막 페이지의 표시는? (`nextCursor: null`, 빈 배열, `hasMore: false`)
6. 모르는 파라미터를 보내면 조용히 무시하나요, 400 인가요? (이 API 는 조용히 무시합니다. 그래서 서버가 페이지네이션을 아직 지원하지 않는데 `page=2` 를 보내면 **조용히 전부가 돌아옵니다.** 2주에서 본 함정입니다.)
7. OpenAPI 스펙(6주)에 `limit`, `cursor` 가 **쿼리 파라미터로 선언**되어 있나요? 선언이 있어야 `client.GET("/books", { params: { query: { cursor } } })` 가 타입 검사를 받습니다. 4단계의 화면은 스펙에 없는 파라미터라서 생성된 클라이언트가 아니라 평범한 `fetch` 를 썼습니다. 이것이 "계약에 없는 호출" 의 모습입니다.

## 6. 한계: 이 주차가 다루지 않은 것

- 서버가 가져온 데이터를 TanStack Query 캐시의 초기값으로 넘기는 방법(`HydrationBoundary`)은 [11주](week11-next-server-boundary.md)의 서버 경계와 함께 이름만 소개합니다. 이 저장소에서 실행해 보지 않았습니다.
- 실시간 갱신(폴링, WebSocket), 쿼리 간 의존(`enabled` 로 앞 결과를 기다리기), 오프라인 지원은 다루지 않았습니다.
- 낙관적 업데이트의 동시성(같은 항목에 여러 변경이 겹칠 때)은 다루지 않았습니다. 2단계 코드는 한 번에 한 건을 반납하는 상황만 다룹니다. 반납 버튼이 `giveBack.isPending` 동안 모두 비활성이 되는 것이 그 가정을 지켜 줍니다.
- DevTools 패키지(`@tanstack/react-query-devtools`)는 이 저장소의 의존성이 아닙니다. 이 문서는 Network 탭으로만 확인합니다.

## TS 와 비교

백엔드 용어를 프론트의 익숙한 것에 대응시킨 표입니다. 완전히 같지는 않습니다.

| 백엔드(Java·Spring)·HTTP 쪽 | 프론트에서 만나는 모습 |
|---|---|
| `Cache-Control: max-age` | `staleTime`. 단, 브라우저가 아니라 쿼리 캐시가 지킵니다 |
| `ETag` + `If-None-Match` (바뀌지 않았으면 304) | 없음. `staleTime` 이 지나면 응답 전체를 다시 받습니다. 참조 API 도 `ETag` 를 내리지 않습니다 |
| `@CacheEvict` | `invalidateQueries` |
| `@CachePut` | `setQueryData` |
| `@Transactional` 롤백 | 낙관적 업데이트의 `onError` 되돌리기. 비유일 뿐이고 서버 쪽 변경과는 아무 관계가 없습니다 |
| Spring Data 의 `Pageable` 과 `Page<T>` (총 개수 포함) | 오프셋 페이지, `useQuery` + `["books", { page }]` |
| Spring Data 의 `Slice<T>` (다음 페이지 유무만) | "더 보기" 의 `hasNextPage` |
| 키셋(keyset) 페이지네이션 (`where id < ?`) | 커서 페이지네이션, `useInfiniteQuery` |
| 서버 응답이 정렬되어 있지 않을 때의 `ORDER BY` 누락 | 페이지 사이에서 항목이 흔들리는 버그(5-3 의 1번 질문) |

## 연습문제

1. `useMe.ts` 에 `staleTime: 60_000` 을 임시로 넣고 1단계 4~6번을 다시 세세요. 요청 수가 어떻게 바뀌는지 적고, 이 값이 **틀려서 문제가 되는 시나리오**(서버에서 세션이 만료됐는데 화면은 로그인 상태)를 한 문장으로 쓰세요. 그 틈을 이 앱의 어느 코드가 메우는지도 쓰세요. 끝나면 원복합니다.
2. `["loans", 1]` 로 저장한 캐시를 `["loans", "1"]` 로 읽으면 어떻게 됩니까? `MyLoans` 가 `Number(memberId)` 를 키에 넣는 것의 의미를 이 사실과 연결해 한 문장으로 쓰세요.
3. `MyLoans` 의 회원 id 칸에 `12` 를 입력(없는 회원)하면 어떤 요청이 몇 번 나가고 화면에는 무엇이 보입니까? `retry: false` 가 아니었다면 무엇이 달랐을까요?
4. `LendForm` 의 대출 성공 뒤 무효화 대상이 `["loans"]` 입니다. 만약 책 목록이 "대출 중" 표시를 보여 준다면 무효화 대상을 어떻게 늘려야 합니까? 낙관적 업데이트를 쓰지 않는 이유와 함께 쓰세요.
5. 2단계 코드에서 `await queryClient.cancelQueries(...)` 줄을 지우면 어떤 순서로 사고가 날 수 있는지(요청 두 개와 응답 도착 순서) 말로 설명하세요.
6. 어떤 백엔드가 `GET /orders?offset=0&limit=20` 만 제공하는데 화면은 "더 보기" 입니다. 프론트가 겪을 문제 두 가지와, 백엔드에 요청할 변경 한 가지를 쓰세요.
7. 5-3 의 질문 7개 중, 답을 **OpenAPI 스펙만 보고** 알 수 있는 것과 **사람에게 물어야** 아는 것을 나누세요.

<details>
<summary>1번과 2번, 3번 확인 결과</summary>

- 1번: `/api/auth/me` 는 4번(`책 → 회원`)에서 0번, 5번에서 0번(`/api/books` 1번만), 6번(탭 복귀)에서 0번(`/api/books` 1번만)으로 줄어듭니다. 첫 로드의 1번은 그대로입니다. 틈은 `providers.tsx` 의 `MutationCache.onError` 가 메웁니다. 쓰기가 401 이면 `["me"]` 를 `null` 로 바꿉니다.
- 2번: `undefined` 입니다. 다른 키이기 때문입니다. `Number(memberId)` 덕분에 `"1"` 과 `"01"` 이 모두 `1` 로 정리됩니다.
- 3번: `1` 이 이미 입력된 칸 뒤에 `2` 를 입력하면 `GET /api/members/12/loans` 가 **1번** 나가고, 화면에는 `notFound` 종류 알림(제목 "찾을 수 없습니다", 본문 "회원을 찾을 수 없습니다: 12")이 보입니다. `retry` 기본값은 브라우저에서 3번이라서, `retry: false` 가 아니었다면 같은 404 를 몇 번 더 보낸 뒤(재시도 사이에 간격이 있습니다) 오류가 나타났을 것입니다. 4xx 는 다시 해도 같다는 주석이 이 이야기입니다.

</details>

## 퀴즈

1. 쿼리 키에 쿼리 함수가 읽는 값이 빠지면 어떤 버그가 생깁니까?
2. `isPending` 이 `true` 인데 요청이 한 번도 안 나가는 경우는 언제입니까?
3. `invalidateQueries({ queryKey: ["books"] })` 는 `["books", "clean"]` 과 `["books", ""]` 중 무엇을 **즉시** 다시 가져옵니까?
4. 낙관적 업데이트의 `onSettled` 에서 무효화를 하는 이유는? `onSuccess` 에서만 하면 어떤 경우가 빠집니까?
5. `staleTime: 0` 인 쿼리가 있는 화면으로 돌아왔을 때 화면은 "불러오는 중..." 을 보여 줍니까? 요청은 나갑니까?
6. `useInfiniteQuery` 에서 `getNextPageParam` 이 `undefined` 를 돌려주면 무슨 일이 생깁니까?
7. 오프셋 페이지네이션이 "더 보기" 에 어울리지 않는 이유는?

<details>
<summary>정답</summary>

1. 입력이 바뀌어도 같은 캐시 항목을 보여 줍니다(또는 서로 다른 입력의 결과가 한 항목에 섞입니다).
2. `enabled: false` 처럼 쿼리가 꺼져 있을 때입니다. `status` 는 `pending`, `fetchStatus` 는 `idle` 입니다. 그래서 `MyLoans` 는 `isFetching` 을 씁니다.
3. 지금 화면에 쓰이는(active) 키만 즉시 가져옵니다. 쓰이지 않는 키는 오래됨 표시만 되고 다음에 쓰일 때 가져옵니다. 1단계 8~9번에서 본 동작입니다.
4. 실패했을 때도(되돌린 뒤) 서버의 사실로 다시 맞추기 위해서입니다. `onSuccess` 에서만 하면 실패(401, 409) 뒤에 낙관적 값이 틀린 채 남거나 되돌린 값이 서버와 어긋난 채 남습니다.
5. 아니요. 캐시된 데이터를 먼저 그리고(`isPending` 이 아닙니다) 뒤에서 요청을 보냅니다. 1단계 3번과 5번에서 확인했습니다.
6. `hasNextPage` 가 `false` 가 되고, `fetchNextPage()` 는 더 이상 새 페이지를 가져오지 않습니다. 테스트의 "커서를 이어 붙여 끝까지 가져온다" 가 이 조건으로 끝납니다.
7. 페이지 사이에 새 항목이 생기면 이미 본 항목이 다음 페이지에 다시 와서 중복이 생기기 때문입니다.

</details>

## 체크리스트

- [ ] 1단계 9줄의 요청 수를 예측했고, 틀린 것의 이유를 적었다
- [ ] `staleTime` 이 정해지지 않은 결과(페이지 이동마다 `/auth/me`)를 Network 탭에서 직접 보았다
- [ ] `["books", keyword]`, `["loans", id]`, `["me"]` 세 키가 무엇을 구분하는지, 무효화가 접두사로 동작하는 것을 설명할 수 있다
- [ ] 무효화와 `setQueryData` 를 어느 때 쓰는지 이 앱의 예로 설명할 수 있다
- [ ] `isPending` 과 `isFetching` 의 차이와 `enabled: false` 의 함정을 설명할 수 있다
- [ ] `giveBack` 에 낙관적 업데이트를 구현해 성공·401·409 를 모두 확인하고 `MyLoans.tsx` 를 원복했다
- [ ] 낙관적 업데이트가 `lend` 와 `createBook` 에 맞지 않는 이유를 말할 수 있다
- [ ] 오프셋이 중복을 만드는 순간과 커서가 안 만드는 이유를 숫자로 설명할 수 있다
- [ ] `useInfiniteQuery` 의 `initialPageParam`, `getNextPageParam`, `hasNextPage` 의 관계를 설명할 수 있다
- [ ] 페이지네이션 계약에 대해 백엔드에 물을 질문 5개 이상을 쓸 수 있다
- [ ] 4단계를 했다면 `src/app/scratch`, `src/app/scratch-api` 를 삭제했다
