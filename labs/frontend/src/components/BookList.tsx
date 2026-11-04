"use client";

import { useQuery } from "@tanstack/react-query";
import { useState, type FormEvent } from "react";
import { library } from "@/api/library";
import { Button } from "./Button";
import { ErrorNotice } from "./ErrorNotice";
import { TextField } from "./TextField";

export function BookList() {
  const [input, setInput] = useState("");
  const [keyword, setKeyword] = useState("");
  const books = useQuery({ queryKey: ["books", keyword], queryFn: () => library.listBooks(keyword) });

  const search = (e: FormEvent) => {
    e.preventDefault();
    setKeyword(input.trim());
  };

  return (
    <section aria-labelledby="books-heading" className="flex flex-col gap-gutter">
      <h2 id="books-heading" className="text-xl font-bold">
        책 목록
      </h2>
      <form onSubmit={search} role="search" className="flex items-end gap-field">
        <div className="grow">
          <TextField label="제목 검색" name="keyword" type="search" value={input} onChange={(e) => setInput(e.target.value)} />
        </div>
        <Button type="submit">검색</Button>
      </form>
      {books.isPending && <p className="text-muted">불러오는 중...</p>}
      <ErrorNotice error={books.error} />
      {books.data?.length === 0 && <p className="text-muted">검색 결과가 없습니다</p>}
      <ul className="flex flex-col gap-field">
        {books.data?.map((b) => (
          <li key={b.id} className="rounded-card border border-line bg-surface p-gutter">
            <p className="font-semibold">{b.title}</p>
            <p className="text-muted">
              {b.author} · isbn {b.isbn} · #{b.id}
            </p>
          </li>
        ))}
      </ul>
    </section>
  );
}
