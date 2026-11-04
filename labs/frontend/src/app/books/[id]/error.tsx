"use client"; // 에러 바운더리는 클라이언트 컴포넌트여야 한다

import { Button } from "@/components/Button";
import { ErrorNotice } from "@/components/ErrorNotice";

// 예상 못 한 실패(백엔드 꺼짐, 500 ...)의 마지막 화면. 운영 빌드에서는 error.message 가 일반 문구로 바뀐다.
export default function BookError({ error, retry }: { error: unknown; retry: () => void }) {
  return (
    <div className="flex flex-col gap-gutter">
      <ErrorNotice error={error} />
      <div>
        <Button type="button" onClick={() => retry()}>
          다시 시도
        </Button>
      </div>
    </div>
  );
}
