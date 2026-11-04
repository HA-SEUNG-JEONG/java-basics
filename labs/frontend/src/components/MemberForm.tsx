"use client";

import { useMutation } from "@tanstack/react-query";
import { useState, type FormEvent } from "react";
import { ApiError } from "@/api/client";
import { library } from "@/api/library";
import { ErrorNotice } from "./ErrorNotice";
import { SubmitButton } from "./SubmitButton";
import { TextField } from "./TextField";

export function MemberForm() {
  const [name, setName] = useState("");
  const create = useMutation({
    mutationFn: library.createMember,
    onSuccess: () => setName(""),
  });
  const fieldErrors = create.error instanceof ApiError ? create.error.fieldErrors : {};
  const showNotice = create.error instanceof ApiError ? create.error.kind !== "field" : Boolean(create.error);

  const submit = (e: FormEvent) => {
    e.preventDefault();
    create.mutate(name);
  };

  return (
    <section aria-labelledby="member-form-heading" className="flex flex-col gap-gutter">
      <h2 id="member-form-heading" className="text-xl font-bold">
        회원 등록
      </h2>
      <form onSubmit={submit} noValidate className="flex flex-col gap-gutter">
        {showNotice && <ErrorNotice error={create.error} />}
        <TextField label="이름" name="name" value={name} onChange={(e) => setName(e.target.value)} error={fieldErrors.name} />
        <SubmitButton pending={create.isPending}>회원 등록</SubmitButton>
        {create.isSuccess && (
          <p className="text-success">
            등록했습니다: {create.data.name} (회원 id {create.data.id}). 대출 화면에서 이 id 를 쓰세요.
          </p>
        )}
      </form>
    </section>
  );
}
