import { ApiError } from "@/api/client";
import { createLibrary } from "@/api/library";
import { getMe, serverClient } from "@/api/server";

// BFF 맛보기: 화면 하나가 필요로 하는 호출 둘(책 목록, 내 정보)을 서버에서 묶고, 응답 모양도 프론트가 정한다.
export async function GET() {
  try {
    const [books, me] = await Promise.all([createLibrary(await serverClient()).listBooks(), getMe()]);
    return Response.json({ bookCount: books.length, username: me?.username ?? null });
  } catch (e) {
    if (e instanceof ApiError) return Response.json({ message: e.message }, { status: e.status });
    return Response.json({ message: "백엔드에 연결할 수 없습니다" }, { status: 502 }); // 502: 우리 뒤의 서버가 응답하지 않음
  }
}
