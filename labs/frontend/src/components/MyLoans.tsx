"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useState } from "react";
import { library } from "@/api/library";
import { useMe } from "@/lib/useMe";
import { Button } from "./Button";
import { ErrorNotice } from "./ErrorNotice";
import { TextField } from "./TextField";

export function MyLoans() {
  const queryClient = useQueryClient();
  const loggedIn = Boolean(useMe().data);
  const [memberId, setMemberId] = useState("1");
  const id = Number(memberId);
  const loans = useQuery({
    queryKey: ["loans", id],
    queryFn: () => library.activeLoans(id),
    enabled: memberId.trim() !== "" && Number.isInteger(id),
  });
  const giveBack = useMutation({
    mutationFn: library.giveBack,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["loans"] }),
  });

  return (
    <section aria-labelledby="loans-heading" className="flex flex-col gap-gutter">
      <h2 id="loans-heading" className="text-xl font-bold">
        내 대출 목록
      </h2>
      <TextField label="회원 id" name="loansMemberId" inputMode="numeric" value={memberId} onChange={(e) => setMemberId(e.target.value)} />
      {loans.isFetching && <p className="text-muted">불러오는 중...</p>}
      <ErrorNotice error={loans.error} />
      <ErrorNotice error={giveBack.error} />
      {loans.data?.length === 0 && <p className="text-muted">대출 중인 책이 없습니다</p>}
      <ul className="flex flex-col gap-field">
        {loans.data?.map((loan) => (
          <li key={loan.id} className="flex items-center justify-between gap-gutter rounded-card border border-line bg-surface p-gutter">
            <div>
              <p className="font-semibold">{loan.book.title}</p>
              <p className="text-muted">대출 #{loan.id} · {new Date(loan.loanedAt).toLocaleDateString("ko-KR")}</p>
            </div>
            <Button type="button" disabled={!loggedIn || giveBack.isPending} onClick={() => giveBack.mutate(loan.id)}>
              반납
            </Button>
          </li>
        ))}
      </ul>
      {!loggedIn && <p className="text-muted">반납은 로그인 후 사용할 수 있습니다</p>}
    </section>
  );
}
