import { api, createApiClient, unwrap, type BookRequest } from "./client";

// 화면이 쓰는 호출 모음. 경로·본문·응답 타입은 모두 schema.d.ts(생성 파일)에서 온다.
// 테스트에서 baseUrl 을 바꿔 쓰려면 createLibrary(createApiClient("http://localhost:8080")).
export function createLibrary(client: ReturnType<typeof createApiClient>) {
  return {
    listBooks: (keyword?: string) =>
      unwrap(client.GET("/books", { params: { query: { keyword: keyword || undefined } } })),
    createBook: (body: BookRequest) => unwrap(client.POST("/books", { body })),
    createMember: (name: string) => unwrap(client.POST("/members", { body: { name } })),
    lend: (memberId: number, bookId: number) => unwrap(client.POST("/loans", { body: { memberId, bookId } })),
    giveBack: (loanId: number) =>
      unwrap(client.POST("/loans/{id}/return", { params: { path: { id: loanId } } })),
    activeLoans: (memberId: number) =>
      unwrap(client.GET("/members/{id}/loans", { params: { path: { id: memberId } } })),
    login: (username: string, password: string) =>
      unwrap(client.POST("/auth/login", { body: { username, password } })),
    logout: () => unwrap(client.POST("/auth/logout")).then(() => undefined), // 204: 본문 없음
    me: () => unwrap(client.GET("/auth/me")),
  };
}

export const library = createLibrary(api);
