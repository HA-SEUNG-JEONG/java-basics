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
