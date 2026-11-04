import { http, HttpResponse } from "msw";
import type { Book, Loan, Member } from "@/api/client";
import type { components } from "@/api/schema";

// week25 백엔드와 같은 상태 코드·메시지를 흉내 낸다. 경로의 "*/" 는 브라우저의 "/api/books" 와
// 테스트(node)의 "http://localhost:8080/books" 를 둘 다 맞추기 위한 와일드카드다.
// ponytail: 로그인 상태는 모듈 전역 하나(쿠키 흉내 안 냄). 여러 사용자가 필요하면 쿠키 기반으로.

const LOAN_LIMIT = 3;
const DEMO = { username: "demo", password: "demo1234" };

let books: Book[] = [];
let members: Member[] = [];
let loans: Loan[] = [];
let nextId = { book: 1, member: 1, loan: 1 };
let loggedIn = false;

/** 테스트마다 초기 상태로 되돌린다. 책 2권·회원 1명이 들어 있다(실제 백엔드는 빈 DB 로 시작). */
export function resetMockDb() {
  books = [
    { id: 1, isbn: "111", title: "이펙티브 자바", author: "조슈아 블로크" },
    { id: 2, isbn: "222", title: "Clean Code", author: "Robert C. Martin" },
  ];
  members = [{ id: 1, name: "하승" }];
  loans = [];
  nextId = { book: 3, member: 2, loan: 1 };
  loggedIn = false;
}
resetMockDb();

const error = (status: number, message: string) => HttpResponse.json<components["schemas"]["ApiError"]>({ message }, { status });
const needLogin = () => error(401, "로그인이 필요합니다");

/** @NotBlank 검증 실패 메시지: "isbn: must not be blank, title: ..." (필드 이름순) */
function blankFields(body: Record<string, unknown>, fields: string[]): string | null {
  const bad = fields
    .filter((f) => typeof body[f] !== "string" || (body[f] as string).trim() === "")
    .map((f) => `${f}: must not be blank`)
    .sort();
  return bad.length ? bad.join(", ") : null;
}

async function readJson(request: Request): Promise<Record<string, unknown> | null> {
  try {
    const body = await request.json();
    return typeof body === "object" && body !== null ? (body as Record<string, unknown>) : null;
  } catch {
    return null;
  }
}

const unreadable = () => error(400, "요청 본문을 읽을 수 없습니다 (JSON 형식 확인)");
const isId = (raw: string | readonly string[] | undefined) => typeof raw === "string" && /^\d+$/.test(raw);
const badId = () => error(400, "id: 형식이 올바르지 않습니다");

export const handlers = [
  http.post("*/auth/login", async ({ request }) => {
    const body = await readJson(request);
    if (!body) return unreadable();
    const invalid = blankFields(body, ["password", "username"]);
    if (invalid) return error(400, invalid);
    if (body.username !== DEMO.username || body.password !== DEMO.password) {
      return error(401, "아이디 또는 비밀번호가 올바르지 않습니다");
    }
    loggedIn = true;
    return HttpResponse.json({ username: DEMO.username });
  }),

  http.post("*/auth/logout", () => {
    loggedIn = false;
    return new HttpResponse(null, { status: 204 });
  }),

  http.get("*/auth/me", () => (loggedIn ? HttpResponse.json({ username: DEMO.username }) : needLogin())),

  http.get("*/books", ({ request }) => {
    const keyword = new URL(request.url).searchParams.get("keyword")?.trim().toLowerCase();
    return HttpResponse.json(keyword ? books.filter((b) => b.title.toLowerCase().includes(keyword)) : books);
  }),

  http.post("*/books", async ({ request }) => {
    if (!loggedIn) return needLogin();
    const body = await readJson(request);
    if (!body) return unreadable();
    const invalid = blankFields(body, ["author", "isbn", "title"]);
    if (invalid) return error(400, invalid);
    const { isbn, title, author } = body as Record<"isbn" | "title" | "author", string>;
    if (books.some((b) => b.isbn === isbn)) return error(409, `이미 등록된 isbn 입니다: ${isbn}`);
    const book: Book = { id: nextId.book++, isbn, title, author };
    books.push(book);
    return HttpResponse.json(book, { status: 201, headers: { Location: `/books/${book.id}` } });
  }),

  http.get("*/books/:id", ({ params }) => {
    if (!isId(params.id)) return badId();
    const book = books.find((b) => b.id === Number(params.id));
    return book ? HttpResponse.json(book) : error(404, `책을 찾을 수 없습니다: ${params.id}`);
  }),

  http.post("*/members", async ({ request }) => {
    if (!loggedIn) return needLogin();
    const body = await readJson(request);
    if (!body) return unreadable();
    const invalid = blankFields(body, ["name"]);
    if (invalid) return error(400, invalid);
    const member: Member = { id: nextId.member++, name: body.name as string };
    members.push(member);
    return HttpResponse.json(member, { status: 201, headers: { Location: `/members/${member.id}` } });
  }),

  http.post("*/loans", async ({ request }) => {
    if (!loggedIn) return needLogin();
    const body = await readJson(request);
    if (!body) return unreadable();
    const missing = ["bookId", "memberId"].filter((f) => typeof body[f] !== "number").map((f) => `${f}: must not be null`);
    if (missing.length) return error(400, missing.join(", "));
    const member = members.find((m) => m.id === body.memberId);
    if (!member) return error(404, `회원을 찾을 수 없습니다: ${body.memberId}`);
    const book = books.find((b) => b.id === body.bookId);
    if (!book) return error(404, `책을 찾을 수 없습니다: ${body.bookId}`);
    if (loans.some((l) => l.book.id === book.id && l.returnedAt === null)) {
      return error(409, `이미 대출 중인 책입니다: ${book.id}`);
    }
    if (loans.filter((l) => l.member.id === member.id && l.returnedAt === null).length >= LOAN_LIMIT) {
      return error(409, `한 사람은 ${LOAN_LIMIT}권까지 빌릴 수 있습니다`);
    }
    const loan: Loan = { id: nextId.loan++, book, member, loanedAt: new Date().toISOString(), returnedAt: null };
    loans.push(loan);
    return HttpResponse.json(loan, { status: 201 });
  }),

  http.post("*/loans/:id/return", ({ params }) => {
    if (!loggedIn) return needLogin();
    if (!isId(params.id)) return badId();
    const loan = loans.find((l) => l.id === Number(params.id));
    if (!loan) return error(404, `대출을 찾을 수 없습니다: ${params.id}`);
    if (loan.returnedAt !== null) return error(409, `이미 반납된 대출입니다: ${params.id}`);
    loan.returnedAt = new Date().toISOString();
    return HttpResponse.json(loan);
  }),

  http.get("*/members/:id/loans", ({ params }) => {
    if (!isId(params.id)) return badId();
    const memberId = Number(params.id);
    if (!members.some((m) => m.id === memberId)) return error(404, `회원을 찾을 수 없습니다: ${params.id}`);
    return HttpResponse.json(loans.filter((l) => l.member.id === memberId && l.returnedAt === null));
  }),
];
