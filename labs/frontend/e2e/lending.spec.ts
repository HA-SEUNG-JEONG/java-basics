import { expect, test } from "@playwright/test";

test("대출: 없는 책은 404(notFound), 이미 대출 중이면 409(conflict), 반납하면 목록에서 사라진다", async ({ page }) => {
  const isbn = `e2e-loan-${Date.now()}`;
  const notice = page.locator('[role="alert"][data-kind]');

  await page.goto("/");
  await page.getByLabel("아이디").fill("demo");
  await page.getByLabel("비밀번호").fill("demo1234");
  await page.getByRole("button", { name: "로그인", exact: true }).click();
  await expect(page.getByTestId("current-user")).toBeVisible();

  // 책 한 권과 회원 한 명 등록 (id 는 화면에 나온 값을 읽는다)
  await page.getByLabel("isbn", { exact: true }).fill(isbn);
  await page.getByLabel("제목", { exact: true }).fill("대출용 책");
  await page.getByLabel("저자", { exact: true }).fill("테스터");
  await page.getByRole("button", { name: "책 등록" }).click();
  const bookId = (await page.getByText(/등록했습니다: 대출용 책/).textContent())!.match(/#(\d+)/)![1];

  await page.getByRole("link", { name: "회원" }).click();
  await page.getByLabel("이름").fill("E2E 회원");
  await page.getByRole("button", { name: "회원 등록" }).click();
  const memberId = (await page.getByText(/회원 id \d+/).textContent())!.match(/회원 id (\d+)/)![1];

  await page.getByRole("link", { name: "대출·반납" }).click();
  const lend = async (member: string, book: string) => {
    await page.getByLabel("회원 id", { exact: true }).first().fill(member);
    await page.getByLabel("책 id").fill(book);
    await page.getByRole("button", { name: "대출", exact: true }).click();
  };

  await lend(memberId, "999999");
  await expect(notice).toHaveAttribute("data-kind", "notFound");

  await lend(memberId, bookId);
  await expect(page.getByText("대출했습니다: 대출용 책")).toBeVisible();

  await lend(memberId, bookId);
  await expect(notice).toHaveAttribute("data-kind", "conflict");

  // 내 대출 목록에서 반납
  await page.getByLabel("회원 id", { exact: true }).last().fill(memberId);
  const row = page.getByRole("listitem").filter({ hasText: "대출용 책" });
  await expect(row).toBeVisible();
  await row.getByRole("button", { name: "반납" }).click();
  await expect(row).toHaveCount(0);
});
