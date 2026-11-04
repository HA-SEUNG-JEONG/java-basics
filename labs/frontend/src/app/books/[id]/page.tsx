import Link from "next/link";
import { notFound } from "next/navigation";
import { ApiError } from "@/api/client";
import { getBook, getMe } from "@/api/server";

export default async function BookDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;
  if (!/^\d+$/.test(id)) notFound(); // 숫자가 아닌 id 는 백엔드에서도 400 이다: 사용자가 고칠 입력이 아니라 틀린 주소

  // 서로 기다릴 이유가 없는 두 요청은 함께 시작한다.
  const [book, me] = await Promise.all([
    getBook(Number(id)).catch((e: unknown) => {
      if (e instanceof ApiError && e.kind === "notFound") notFound(); // 예상된 실패: not-found.tsx
      throw e; // 예상 못 한 실패: error.tsx
    }),
    getMe(),
  ]);

  return (
    <section aria-labelledby="book-heading" className="flex flex-col gap-gutter">
      <h2 id="book-heading" className="text-xl font-bold">
        {book.title}
      </h2>
      <p className="text-muted">
        {book.author} · isbn {book.isbn} · #{book.id}
      </p>
      <p data-testid="viewer">{me ? `${me.username} 님으로 로그인한 요청입니다` : "로그인하지 않은 요청입니다"}</p>
      <Link href="/" className="font-semibold text-primary underline-offset-4 hover:underline">
        책 목록으로
      </Link>
    </section>
  );
}
