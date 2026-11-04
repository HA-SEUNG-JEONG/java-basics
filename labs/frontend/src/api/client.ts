import createClient from "openapi-fetch";
import type { components, paths } from "./schema";

export type Book = components["schemas"]["BookResponse"];
export type Member = components["schemas"]["MemberResponse"];
export type Loan = components["schemas"]["LoanResponse"];
export type BookRequest = components["schemas"]["BookRequest"];

/** 상태 코드가 UI 에서 어떤 종류의 표시로 이어지는지. */
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

/** 서버 오류 본문은 항상 {"message": "..."} (components["schemas"]["ApiError"]). */
export class ApiError extends Error {
  constructor(
    readonly status: number,
    message: string,
  ) {
    super(message);
    this.name = "ApiError";
  }

  get kind(): UiKind {
    return uiKindOf(this.status);
  }

  /** 400 의 "isbn: must not be blank, title: ..." 를 { isbn: "...", title: "..." } 로 나눈다. */
  get fieldErrors(): Record<string, string> {
    if (this.kind !== "field") return {};
    const fields: Record<string, string> = {};
    for (const part of this.message.split(", ")) {
      const match = /^(\w+): (.+)$/.exec(part);
      if (match) fields[match[1]] = match[2];
    }
    return fields;
  }
}

type Result = { data?: unknown; error?: unknown; response: Response };

function messageOf(error: unknown, status: number): string {
  if (typeof error === "object" && error !== null && "message" in error && typeof error.message === "string") {
    return error.message;
  }
  // 프록시 오류(백엔드 꺼짐 등)처럼 {"message"} 형식이 아닌 응답
  return `요청에 실패했습니다 (${status})`;
}

/** openapi-fetch 의 { data, error } 결과를 "성공이면 data, 아니면 ApiError 를 throw" 로 바꾼다. TanStack Query 와 맞는 모양. */
export async function unwrap<R extends Result>(request: Promise<R>): Promise<Exclude<R["data"], undefined>> {
  const { data, error, response } = await request;
  if (error !== undefined || !response.ok) throw new ApiError(response.status, messageOf(error, response.status));
  return data as Exclude<R["data"], undefined>;
}

export function createApiClient(baseUrl: string) {
  return createClient<paths>({ baseUrl, credentials: "include" });
}

/** 기본값 "/api" = next.config.ts 의 rewrites 프록시. NEXT_PUBLIC_API_BASE 가 있으면 백엔드를 직접 호출. */
export const api = createApiClient(process.env.NEXT_PUBLIC_API_BASE || "/api");
