import { expect, test } from "@playwright/test";

test("로그인 -> 책 등록 -> 같은 isbn 은 409 -> 로그아웃하면 쓰기 차단", async ({ page }) => {
  const isbn = `e2e-${Date.now()}`; // 백엔드 DB 는 실행 중 계속 남으므로 매번 새 isbn
  const submit = page.getByRole("button", { name: "책 등록" });

  await page.goto("/");

  // 1) 로그인 전: 쓰기 버튼이 막혀 있다
  await expect(submit).toBeDisabled();
  await expect(page.getByText("로그인 후 사용할 수 있습니다")).toBeVisible();

  // 2) 로그인 (세션 쿠키는 브라우저가 /api 프록시를 통해 받는다)
  await page.getByLabel("아이디").fill("demo");
  await page.getByLabel("비밀번호").fill("demo1234");
  await page.getByRole("button", { name: "로그인", exact: true }).click();
  await expect(page.getByTestId("current-user")).toHaveText("demo 님");
  await expect(submit).toBeEnabled();

  // 3) 등록 성공 -> 목록에 나타난다
  await page.getByLabel("isbn", { exact: true }).fill(isbn);
  await page.getByLabel("제목", { exact: true }).fill("E2E 책");
  await page.getByLabel("저자", { exact: true }).fill("테스터");
  await submit.click();
  await expect(page.getByText("등록했습니다: E2E 책")).toBeVisible();
  await expect(page.getByRole("listitem").filter({ hasText: isbn })).toBeVisible();

  // 4) 같은 isbn 다시 -> 409: 폼 위 "conflict" 알림 (입력칸 오류가 아니다)
  await page.getByLabel("isbn", { exact: true }).fill(isbn);
  await page.getByLabel("제목", { exact: true }).fill("E2E 책 2");
  await page.getByLabel("저자", { exact: true }).fill("테스터");
  await submit.click();
  const notice = page.locator('[role="alert"][data-kind]'); // Next 의 라우트 안내(role=alert)와 구분
  await expect(notice).toHaveAttribute("data-kind", "conflict");
  await expect(notice).toContainText(isbn);
  await expect(page.getByLabel("isbn", { exact: true })).not.toHaveAttribute("aria-invalid", "true");

  // 5) 빈 isbn -> 400: 입력칸 옆 오류 (409 알림과 다른 표시)
  await page.getByLabel("isbn", { exact: true }).fill("");
  await submit.click();
  await expect(page.getByLabel("isbn", { exact: true })).toHaveAttribute("aria-invalid", "true");
  await expect(notice).toHaveCount(0);

  // 6) 로그아웃 -> 쓰기 버튼이 다시 막힌다
  await page.getByRole("button", { name: "로그아웃" }).click();
  await expect(page.getByRole("button", { name: "로그인", exact: true })).toBeVisible();
  await expect(submit).toBeDisabled();
});
