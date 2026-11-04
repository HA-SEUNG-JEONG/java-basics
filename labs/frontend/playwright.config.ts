import { defineConfig } from "@playwright/test";

// 백엔드(./gradlew :week25:bootRun, 8080)는 미리 띄워 둔다. 여기서는 Next dev 서버만 기동한다.
export default defineConfig({
  testDir: "./e2e",
  workers: 1, // 백엔드 DB(H2 메모리)를 모든 테스트가 공유한다
  use: { baseURL: "http://localhost:3000" },
  projects: [{ name: "chromium", use: { browserName: "chromium" } }],
  webServer: {
    command: "pnpm dev",
    url: "http://localhost:3000",
    reuseExistingServer: true,
  },
});
