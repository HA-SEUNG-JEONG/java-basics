"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useState, type FormEvent } from "react";
import { ApiError } from "@/api/client";
import { library } from "@/api/library";
import { ErrorNotice } from "./ErrorNotice";
import { SubmitButton } from "./SubmitButton";
import { TextField } from "./TextField";

export function LendForm() {
  const queryClient = useQueryClient();
  const [memberId, setMemberId] = useState("");
  const [bookId, setBookId] = useState("");
  const lend = useMutation({
    mutationFn: () => library.lend(Number(memberId), Number(bookId)),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["loans"] }),
  });
  const fieldErrors = lend.error instanceof ApiError ? lend.error.fieldErrors : {};
  const showNotice = lend.error instanceof ApiError ? lend.error.kind !== "field" : Boolean(lend.error);

  const submit = (e: FormEvent) => {
    e.preventDefault();
    lend.mutate();
  };

  return (
    <section aria-labelledby="lend-heading" className="flex flex-col gap-gutter">
      <h2 id="lend-heading" className="text-xl font-bold">
        대출
      </h2>
      <form onSubmit={submit} noValidate className="flex flex-col gap-gutter">
        {/* 404(회원/책 없음)와 409(이미 대출 중/3권 한도)는 ErrorNotice 가 서로 다른 제목·색으로 보여 준다 */}
        {showNotice && <ErrorNotice error={lend.error} />}
        <TextField label="회원 id" name="memberId" inputMode="numeric" value={memberId} onChange={(e) => setMemberId(e.target.value)} error={fieldErrors.memberId} />
        <TextField label="책 id" name="bookId" inputMode="numeric" value={bookId} onChange={(e) => setBookId(e.target.value)} error={fieldErrors.bookId} />
        <SubmitButton pending={lend.isPending}>대출</SubmitButton>
        {lend.isSuccess && <p className="text-success">대출했습니다: {lend.data.book.title} (대출 #{lend.data.id})</p>}
      </form>
    </section>
  );
}
