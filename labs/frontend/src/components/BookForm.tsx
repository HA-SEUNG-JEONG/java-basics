"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useState, type FormEvent } from "react";
import { ApiError } from "@/api/client";
import { library } from "@/api/library";
import { ErrorNotice } from "./ErrorNotice";
import { SubmitButton } from "./SubmitButton";
import { TextField } from "./TextField";

const EMPTY = { isbn: "", title: "", author: "" };

export function BookForm() {
  const queryClient = useQueryClient();
  const [form, setForm] = useState(EMPTY);
  const create = useMutation({
    mutationFn: library.createBook,
    onSuccess: () => {
      setForm(EMPTY);
      return queryClient.invalidateQueries({ queryKey: ["books"] });
    },
  });

  // 400 은 입력칸 옆에, 그 외(401/409/...)는 폼 위쪽 알림으로 — 서로 다르게 보인다.
  const fieldErrors = create.error instanceof ApiError ? create.error.fieldErrors : {};
  const showNotice = create.error instanceof ApiError ? create.error.kind !== "field" : Boolean(create.error);
  const set = (name: keyof typeof EMPTY) => (e: { target: { value: string } }) =>
    setForm({ ...form, [name]: e.target.value });

  const submit = (e: FormEvent) => {
    e.preventDefault();
    create.mutate(form);
  };

  return (
    <section aria-labelledby="book-form-heading" className="flex flex-col gap-gutter">
      <h2 id="book-form-heading" className="text-xl font-bold">
        책 등록
      </h2>
      {/* noValidate: 브라우저 검증이 400 응답을 가리지 않도록 서버 검증을 그대로 보여 준다 */}
      <form onSubmit={submit} noValidate className="flex flex-col gap-gutter">
        {showNotice && <ErrorNotice error={create.error} />}
        <TextField label="isbn" name="isbn" value={form.isbn} onChange={set("isbn")} error={fieldErrors.isbn} />
        <TextField label="제목" name="title" value={form.title} onChange={set("title")} error={fieldErrors.title} />
        <TextField label="저자" name="author" value={form.author} onChange={set("author")} error={fieldErrors.author} />
        <SubmitButton pending={create.isPending}>책 등록</SubmitButton>
        {create.isSuccess && <p className="text-success">등록했습니다: {create.data.title} (#{create.data.id})</p>}
      </form>
    </section>
  );
}
