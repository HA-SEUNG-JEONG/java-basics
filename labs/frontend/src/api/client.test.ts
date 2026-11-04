import { http, HttpResponse } from "msw";
import { describe, expect, it } from "vitest";
import { server } from "@/mocks/server";
import { ApiError, createApiClient, uiKindOf } from "./client";
import { createLibrary } from "./library";

const library = createLibrary(createApiClient("http://localhost:8080"));

async function loggedIn() {
  await library.login("demo", "demo1234");
  return library;
}

/** 실패해야 하는 호출이 던진 ApiError 를 돌려준다(던지지 않으면 테스트 실패). */
async function failure(call: Promise<unknown>): Promise<ApiError> {
  const error = await call.then(
    () => null,
    (e: unknown) => e,
  );
  expect(error).toBeInstanceOf(ApiError);
  return error as ApiError;
}

describe("상태 코드 -> UI 종류 매핑", () => {
  it.each([
    [400, "field"],
    [401, "auth"],
    [404, "notFound"],
    [409, "conflict"],
  ] as const)("%i 는 %s", (status, kind) => {
    expect(uiKindOf(status)).toBe(kind);
  });

  it("규약에 없는 상태 코드는 unknown", () => {
    expect(uiKindOf(500)).toBe("unknown");
    expect(uiKindOf(418)).toBe("unknown");
  });
});

describe("ApiError.fieldErrors", () => {
  it("400 메시지를 필드별로 나눈다", () => {
    const e = new ApiError(400, "author: must not be blank, isbn: must not be blank");
    expect(e.fieldErrors).toEqual({ author: "must not be blank", isbn: "must not be blank" });
  });

  it("400 이 아니면 비어 있다(409 메시지에 ': ' 가 있어도 필드로 오해하지 않는다)", () => {
    expect(new ApiError(409, "이미 등록된 isbn 입니다: 111").fieldErrors).toEqual({});
  });
});

describe("클라이언트가 4xx 를 ApiError 로 바꾼다", () => {
  it("성공 응답은 생성된 타입의 data 를 그대로 돌려준다", async () => {
    const books = await library.listBooks();
    expect(books.map((b) => b.title)).toEqual(["이펙티브 자바", "Clean Code"]);
  });

  it("keyword 는 제목 부분 일치, 대소문자 무시", async () => {
    const books = await library.listBooks("CLEAN");
    expect(books).toHaveLength(1);
    expect(books[0].isbn).toBe("222");
  });

  it("로그인 없이 책 등록 -> 401 auth", async () => {
    const e = await failure(library.createBook({ isbn: "9", title: "t", author: "a" }));
    expect(e.status).toBe(401);
    expect(e.kind).toBe("auth");
    expect(e.message).toBe("로그인이 필요합니다");
  });

  it("로그인 후 필드가 비면 400 field + 필드별 메시지", async () => {
    const api = await loggedIn();
    const e = await failure(api.createBook({ isbn: "", title: "x", author: "" }));
    expect(e.kind).toBe("field");
    expect(Object.keys(e.fieldErrors)).toEqual(["author", "isbn"]);
  });

  it("같은 isbn 으로 다시 등록 -> 409 conflict + 메시지에 isbn", async () => {
    const api = await loggedIn();
    const e = await failure(api.createBook({ isbn: "111", title: "x", author: "y" }));
    expect(e.status).toBe(409);
    expect(e.kind).toBe("conflict");
    expect(e.message).toContain("111");
  });

  it("없는 책으로 대출 -> 404 notFound", async () => {
    const api = await loggedIn();
    const e = await failure(api.lend(1, 99));
    expect(e.kind).toBe("notFound");
    expect(e.message).toContain("99");
  });

  it("이미 대출 중인 책 -> 409, 없는 회원의 대출 목록 -> 404", async () => {
    const api = await loggedIn();
    const minji = await api.createMember("민지");
    await api.lend(1, 1);
    expect((await failure(api.lend(minji.id, 1))).kind).toBe("conflict");
    expect((await failure(api.activeLoans(99))).kind).toBe("notFound");
  });

  it("4권째 대출 -> 409, 반납 후에는 다시 빌릴 수 있고 같은 대출을 또 반납하면 409", async () => {
    const api = await loggedIn();
    for (const [i, isbn] of ["3", "4"].entries()) await api.createBook({ isbn, title: `책${i}`, author: "저자" });
    const first = await api.lend(1, 1);
    await api.lend(1, 2);
    await api.lend(1, 3);
    expect((await failure(api.lend(1, 4))).message).toContain("3권");

    const returned = await api.giveBack(first.id);
    expect(returned.returnedAt).not.toBeNull();
    expect((await failure(api.giveBack(first.id))).status).toBe(409);
    expect(await api.lend(1, 4)).toMatchObject({ book: { id: 4 }, returnedAt: null });
    expect((await api.activeLoans(1)).map((l) => l.book.id)).toEqual([2, 3, 4]);
  });

  it("로그아웃하면 다시 쓰기가 401", async () => {
    const api = await loggedIn();
    expect(await api.me()).toEqual({ username: "demo" });
    await api.logout();
    expect((await failure(api.createMember("x"))).status).toBe(401);
  });
});

describe("{\"message\"} 형식이 아닌 실패", () => {
  it("프록시가 돌려준 텍스트 502 도 ApiError(unknown) 로 바뀐다", async () => {
    server.use(http.get("*/books", () => new HttpResponse("Bad Gateway", { status: 502 })));
    const e = await failure(library.listBooks());
    expect(e.status).toBe(502);
    expect(e.kind).toBe("unknown");
    expect(e.message).toContain("502");
  });

  it("네트워크 자체가 실패하면 ApiError 가 아니라 fetch 의 TypeError", async () => {
    server.use(http.get("*/books", () => HttpResponse.error()));
    await expect(library.listBooks()).rejects.toBeInstanceOf(TypeError);
  });
});
