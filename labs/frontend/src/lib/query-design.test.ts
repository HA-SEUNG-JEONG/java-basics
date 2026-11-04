import { InfiniteQueryObserver, MutationObserver, QueryClient, QueryObserver, keepPreviousData } from "@tanstack/react-query";
import { http, HttpResponse, delay } from "msw";
import { describe, expect, it, vi } from "vitest";
import { ApiError, createApiClient, type Book, type Loan } from "@/api/client";
import { createLibrary } from "@/api/library";
import { server } from "@/mocks/server";

// 데이터 패칭 설계 실습: TanStack Query 의 캐시 키·staleTime·무효화·낙관적 업데이트·페이지네이션 동작을
// React 없이 QueryClient 로 직접 확인한다. 참조 API 에는 페이지네이션이 없으므로 그 부분의 서버는 이 파일 안의 MSW 핸들러(흉내)다.

const library = createLibrary(createApiClient("http://localhost:8080"));
const newClient = () => new QueryClient({ defaultOptions: { queries: { retry: false } } }); // providers.tsx 와 같은 기본값
const tick = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms));

describe("캐시 키와 무효화", () => {
  it("키가 다르면 캐시 항목도 다르고, 접두사가 같은 키는 한꺼번에 무효화된다", async () => {
    const client = newClient();
    await client.fetchQuery({ queryKey: ["books", ""], queryFn: () => library.listBooks() });
    await client.fetchQuery({ queryKey: ["books", "clean"], queryFn: () => library.listBooks("clean") });
    expect(client.getQueryCache().findAll({ queryKey: ["books"] })).toHaveLength(2);

    await client.invalidateQueries({ queryKey: ["books", "clean"], exact: true, refetchType: "none" });
    expect(client.getQueryState(["books", "clean"])?.isInvalidated).toBe(true);
    expect(client.getQueryState(["books", ""])?.isInvalidated).toBe(false);

    await client.invalidateQueries({ queryKey: ["books"], refetchType: "none" });
    expect(client.getQueryState(["books", ""])?.isInvalidated).toBe(true);
  });

  it("invalidate 는 '지금 화면에 쓰이는(active)' 쿼리만 바로 다시 가져온다", async () => {
    const client = newClient();
    const queryFn = vi.fn(() => library.listBooks());
    await client.fetchQuery({ queryKey: ["books", ""], queryFn }); // 관찰자가 없다 = inactive
    const observer = new QueryObserver(client, { queryKey: ["books", "clean"], queryFn });
    const unsubscribe = observer.subscribe(() => {}); // 구독 = active
    await tick(20);
    expect(queryFn).toHaveBeenCalledTimes(2);

    await client.invalidateQueries({ queryKey: ["books"] });
    expect(queryFn).toHaveBeenCalledTimes(3); // active 인 ["books","clean"] 만 다시 가져왔다
    unsubscribe();
  });
});

describe("로딩 상태", () => {
  it("enabled: false 인 쿼리는 영원히 pending 이지만 fetching 은 아니다 (MyLoans 가 isPending 이 아니라 isFetching 을 쓰는 이유)", () => {
    const observer = new QueryObserver(newClient(), { queryKey: ["loans", NaN], queryFn: () => library.activeLoans(NaN), enabled: false });
    const result = observer.getCurrentResult();
    expect([result.status, result.fetchStatus]).toEqual(["pending", "idle"]);
    expect([result.isPending, result.isFetching, result.isLoading]).toEqual([true, false, false]);
  });

  it("처음 가져오는 중이면 pending + fetching(= isLoading), 데이터가 있는 뒤의 재조회는 success + fetching", async () => {
    const client = newClient();
    const observer = new QueryObserver(client, { queryKey: ["books", ""], queryFn: () => library.listBooks() });
    const unsubscribe = observer.subscribe(() => {});
    expect([observer.getCurrentResult().status, observer.getCurrentResult().isLoading]).toEqual(["pending", true]);
    await tick(30);
    expect(observer.getCurrentResult().status).toBe("success");
    void observer.refetch();
    expect([observer.getCurrentResult().status, observer.getCurrentResult().fetchStatus]).toEqual(["success", "fetching"]);
    unsubscribe();
  });
});

describe("staleTime", () => {
  it("기본값 0 은 매번 다시 가져오고, staleTime 안에서는 캐시를 그대로 쓴다", async () => {
    const client = newClient();
    const fresh = vi.fn(() => library.listBooks());
    await client.fetchQuery({ queryKey: ["a"], queryFn: fresh });
    await client.fetchQuery({ queryKey: ["a"], queryFn: fresh });
    expect(fresh).toHaveBeenCalledTimes(2);

    const cached = vi.fn(() => library.listBooks());
    await client.fetchQuery({ queryKey: ["b"], queryFn: cached, staleTime: 60_000 });
    await client.fetchQuery({ queryKey: ["b"], queryFn: cached, staleTime: 60_000 });
    expect(cached).toHaveBeenCalledTimes(1);
  });
});

describe("낙관적 업데이트 (반납)", () => {
  // MyLoans 의 giveBack 을 낙관적으로 바꾼 버전. 직접 해 보기 2단계의 코드와 같다.
  function giveBackOptions(client: QueryClient, memberId: number) {
    const key = ["loans", memberId];
    return {
      mutationFn: library.giveBack,
      onMutate: async (loanId: number) => {
        await client.cancelQueries({ queryKey: key }); // 진행 중인 조회가 낙관적 값을 덮어쓰지 못하게
        const previous = client.getQueryData<Loan[]>(key);
        client.setQueryData<Loan[]>(key, (old) => old?.filter((loan) => loan.id !== loanId));
        return { previous };
      },
      onError: (_error: Error, _loanId: number, context?: { previous?: Loan[] }) => client.setQueryData(key, context?.previous),
      onSettled: () => client.invalidateQueries({ queryKey: ["loans"] }),
    };
  }

  /** 대출 1건이 있고 ["loans", 1] 캐시가 채워진 상태. */
  async function lentOne() {
    await library.login("demo", "demo1234");
    const loan = await library.lend(1, 1);
    const client = newClient();
    await client.fetchQuery({ queryKey: ["loans", 1], queryFn: () => library.activeLoans(1) });
    return { client, loan };
  }

  it("서버가 응답하기 전에 목록에서 사라지고, 성공하면 그대로 유지된다", async () => {
    const { client, loan } = await lentOne();
    server.use(
      http.post("*/loans/:id/return", async () => {
        await delay(100); // 응답이 느린 서버
        return HttpResponse.json({ ...loan, returnedAt: new Date().toISOString() });
      }),
    );
    const pending = new MutationObserver(client, giveBackOptions(client, 1)).mutate(loan.id);
    await tick(20);
    expect(client.getQueryData<Loan[]>(["loans", 1])).toEqual([]); // 아직 응답 전
    await pending;
    expect(client.getQueryData<Loan[]>(["loans", 1])).toEqual([]); // 응답이 끝나도 그대로
  });

  it("서버가 거절하면(401) 목록이 원래대로 돌아온다", async () => {
    const { client, loan } = await lentOne();
    server.use(
      http.post("*/loans/:id/return", async () => {
        await delay(100);
        return HttpResponse.json({ message: "로그인이 필요합니다" }, { status: 401 }); // 세션이 끊겼다고 가정
      }),
    );
    const pending = new MutationObserver(client, giveBackOptions(client, 1)).mutate(loan.id).catch((e: unknown) => e);
    await tick(20);
    expect(client.getQueryData<Loan[]>(["loans", 1])).toEqual([]); // 일단 사라졌다
    expect(await pending).toBeInstanceOf(ApiError);
    expect(client.getQueryData<Loan[]>(["loans", 1])?.map((l) => l.id)).toEqual([loan.id]); // 되돌아왔다
  });
});

describe("페이지네이션 (참조 API 에는 없다: 이 파일의 핸들러가 흉내 낸다)", () => {
  // 가장 최근 등록이 앞에 오는 목록(id 내림차순). 실서버가 지원하는 모양이 아니라 개념 확인용이다.
  const book = (id: number): Book => ({ id, isbn: `i${id}`, title: `책 ${id}`, author: "a" });
  const newestFirst = (ids: number[]) => [...ids].sort((a, b) => b - a).map(book);

  function fakePagedServer(ids: number[]) {
    server.use(
      http.get("*/books", ({ request }) => {
        const url = new URL(request.url);
        const limit = Number(url.searchParams.get("limit"));
        const all = newestFirst(ids);
        if (url.searchParams.has("offset")) {
          const offset = Number(url.searchParams.get("offset"));
          return HttpResponse.json({ items: all.slice(offset, offset + limit), total: all.length });
        }
        const before = url.searchParams.get("cursor"); // "이 id 보다 오래된 것부터"
        const rest = before ? all.filter((b) => b.id < Number(before)) : all;
        const items = rest.slice(0, limit);
        return HttpResponse.json({ items, nextCursor: rest.length > limit ? items.at(-1)!.id : null });
      }),
    );
  }
  const getPage = async (query: string) => (await fetch(`http://localhost:8080/books?${query}`)).json() as Promise<{ items: Book[]; nextCursor?: number | null }>;

  it("useInfiniteQuery 의 기반: 커서를 이어 붙여 끝까지 가져온다", async () => {
    fakePagedServer([1, 2, 3, 4, 5]);
    const observer = new InfiniteQueryObserver(newClient(), {
      queryKey: ["books", "infinite"],
      queryFn: ({ pageParam }) => getPage(`limit=2${pageParam ? `&cursor=${pageParam}` : ""}`),
      initialPageParam: null as number | null,
      getNextPageParam: (last) => last.nextCursor, // null(또는 undefined)이면 더 없음
    });
    let result = await observer.refetch();
    while (result.data?.pages.at(-1)?.nextCursor) result = await observer.fetchNextPage();
    expect(result.data?.pages.map((p) => p.items.map((b) => b.id))).toEqual([[5, 4], [3, 2], [1]]);
    expect(observer.getCurrentResult().hasNextPage).toBe(false);
  });

  it("무효화하면 이미 가져온 페이지를 처음부터 전부 다시 가져온다", async () => {
    fakePagedServer([1, 2, 3, 4, 5]);
    const client = newClient();
    const queryFn = vi.fn(({ pageParam }: { pageParam: number | null }) => getPage(`limit=2${pageParam ? `&cursor=${pageParam}` : ""}`));
    const observer = new InfiniteQueryObserver(client, {
      queryKey: ["books", "infinite"],
      queryFn,
      initialPageParam: null as number | null,
      getNextPageParam: (last) => last.nextCursor,
    });
    const unsubscribe = observer.subscribe(() => {}); // 화면에 쓰이는(active) 상태
    await tick(30);
    await observer.fetchNextPage();
    await observer.fetchNextPage();
    expect(queryFn).toHaveBeenCalledTimes(3); // 페이지 3개

    await client.invalidateQueries({ queryKey: ["books"] });
    expect(queryFn).toHaveBeenCalledTimes(6); // 3개를 처음부터 다시
    unsubscribe();
  });

  it("오프셋은 페이지 사이에 새 항목이 생기면 같은 항목이 두 번 나오고, 커서는 그렇지 않다", async () => {
    const ids = [1, 2, 3, 4, 5];
    fakePagedServer(ids);
    const offsetPage1 = await getPage("offset=0&limit=2"); // [5, 4]
    const cursorPage1 = await getPage("limit=2"); // [5, 4]
    ids.push(6); // 사용자가 1페이지를 보는 동안 누군가 책을 등록했다 -> 맨 앞에 6 이 생긴다
    const offsetPage2 = await getPage("offset=2&limit=2");
    const cursorPage2 = await getPage(`limit=2&cursor=${cursorPage1.nextCursor}`);
    const idsOf = (...pages: { items: Book[] }[]) => pages.flatMap((p) => p.items.map((b) => b.id));
    expect(idsOf(offsetPage1, offsetPage2)).toEqual([5, 4, 4, 3]); // 4 가 중복
    expect(idsOf(cursorPage1, cursorPage2)).toEqual([5, 4, 3, 2]); // 중복도 누락도 없다
  });

  it("keepPreviousData: 다음 페이지가 도착하기 전까지 이전 페이지를 placeholder 로 보여 준다", async () => {
    fakePagedServer([1, 2, 3, 4, 5]);
    const client = newClient();
    const options = (page: number) => ({
      queryKey: ["books", { page }],
      queryFn: () => getPage(`offset=${(page - 1) * 2}&limit=2`),
      placeholderData: keepPreviousData,
    });
    const observer = new QueryObserver(client, options(1));
    const unsubscribe = observer.subscribe(() => {});
    await tick(50);
    observer.setOptions(options(2)); // 2페이지로 이동. 응답은 아직
    const during = observer.getCurrentResult();
    expect(during.isPlaceholderData).toBe(true);
    expect(during.data?.items.map((b) => b.id)).toEqual([5, 4]);
    await tick(50);
    const after = observer.getCurrentResult();
    expect(after.isPlaceholderData).toBe(false);
    expect(after.data?.items.map((b) => b.id)).toEqual([3, 2]);
    unsubscribe();
  });
});

describe("재시도", () => {
  it("5xx 만 재시도하는 규칙: 4xx 는 다시 해도 같다", async () => {
    const retry = (count: number, error: Error) => !(error instanceof ApiError && error.status < 500) && count < 2;
    const run = async (status: number) => {
      server.use(http.get("*/books", () => HttpResponse.json({ message: "x" }, { status })));
      const queryFn = vi.fn(() => library.listBooks());
      await newClient().fetchQuery({ queryKey: ["r", status], queryFn, retry, retryDelay: 0 }).catch(() => {});
      return queryFn.mock.calls.length;
    };
    expect(await run(404)).toBe(1); // 재시도 없음
    expect(await run(500)).toBe(3); // 처음 1번 + 재시도 2번
  });
});
