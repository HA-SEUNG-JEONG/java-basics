"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useState, type FormEvent } from "react";
import { library } from "@/api/library";
import { useMe } from "@/lib/useMe";
import { Button } from "./Button";
import { ErrorNotice } from "./ErrorNotice";
import { TextField } from "./TextField";

export function AuthBar() {
  const queryClient = useQueryClient();
  const me = useMe();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");

  const login = useMutation({
    mutationFn: () => library.login(username, password),
    onSuccess: (user) => {
      queryClient.setQueryData(["me"], user);
      setPassword("");
    },
  });
  const logout = useMutation({
    mutationFn: library.logout,
    onSuccess: () => queryClient.setQueryData(["me"], null),
  });

  if (me.isPending) return <p className="text-muted">확인 중...</p>;
  if (me.isError) return <ErrorNotice error={me.error} />;

  if (me.data) {
    return (
      <div className="flex items-center gap-gutter">
        <span data-testid="current-user">{me.data.username} 님</span>
        <Button type="button" onClick={() => logout.mutate()} disabled={logout.isPending}>
          로그아웃
        </Button>
      </div>
    );
  }

  const submit = (e: FormEvent) => {
    e.preventDefault();
    login.mutate();
  };
  return (
    <form onSubmit={submit} noValidate className="flex flex-col gap-field">
      <div className="flex flex-wrap items-end gap-field">
        <TextField label="아이디" name="username" value={username} onChange={(e) => setUsername(e.target.value)} autoComplete="username" />
        <TextField label="비밀번호" name="password" type="password" value={password} onChange={(e) => setPassword(e.target.value)} autoComplete="current-password" />
        <Button type="submit" disabled={login.isPending}>
          로그인
        </Button>
      </div>
      <ErrorNotice error={login.error} />
    </form>
  );
}
