import "server-only";
import { cookies } from "next/headers";
import createClient from "openapi-fetch";
import { ApiError, unwrap } from "./client";
import { createLibrary } from "./library";
import type { paths } from "./schema";

// 서버(Node)가 백엔드를 직접 부른다. 브라우저의 /api 프록시를 거치지 않으므로 CORS 가 없고,
// 주소는 NEXT_PUBLIC_ 이 아닌 서버 전용 변수라서 브라우저 번들에 들어가지 않는다.
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

export async function getBook(id: number) {
  return unwrap((await serverClient()).GET("/books/{id}", { params: { path: { id } } }));
}

/** useMe 와 같은 규칙: 로그인하지 않았으면(401) 오류가 아니라 null. */
export async function getMe() {
  try {
    return await createLibrary(await serverClient()).me();
  } catch (e) {
    if (e instanceof ApiError && e.status === 401) return null;
    throw e;
  }
}
