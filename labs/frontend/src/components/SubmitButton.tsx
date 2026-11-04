"use client";

import { useMe } from "@/lib/useMe";
import { Button } from "./Button";

/** 쓰기 요청 버튼. 로그인하지 않았으면 눌러 보기 전에 막고 이유를 보여 준다(서버는 어차피 401). */
export function SubmitButton({ children, pending }: { children: string; pending: boolean }) {
  const me = useMe();
  const loggedIn = Boolean(me.data);
  return (
    <div className="flex items-center gap-gutter">
      <Button type="submit" disabled={!loggedIn || pending}>
        {pending ? "처리 중..." : children}
      </Button>
      {!me.isPending && !loggedIn && <span className="text-muted">로그인 후 사용할 수 있습니다</span>}
    </div>
  );
}
