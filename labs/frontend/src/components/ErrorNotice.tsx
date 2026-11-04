import { ApiError, type UiKind } from "@/api/client";

// 오류 종류마다 색과 제목이 달라서, 사용자가 "내가 고칠 것인지 / 로그인할 것인지 / 대상이 없는 것인지" 바로 구분한다.
const STYLE: Record<UiKind, { title: string; box: string }> = {
  field: { title: "입력을 확인하세요", box: "bg-warning-soft text-warning border-warning" },
  auth: { title: "로그인이 필요합니다", box: "bg-info-soft text-info border-info" },
  notFound: { title: "찾을 수 없습니다", box: "bg-info-soft text-info border-info" },
  conflict: { title: "이미 처리되었거나 규칙에 어긋납니다", box: "bg-danger-soft text-danger border-danger" },
  unknown: { title: "요청에 실패했습니다", box: "bg-danger-soft text-danger border-danger" },
};

export function ErrorNotice({ error }: { error: unknown }) {
  if (!error) return null;
  const kind = error instanceof ApiError ? error.kind : "unknown";
  const message = error instanceof Error ? error.message : "알 수 없는 오류";
  const { title, box } = STYLE[kind];
  return (
    <div role="alert" data-kind={kind} className={`rounded-card border p-gutter ${box}`}>
      <p className="font-semibold">{title}</p>
      <p>{message}</p>
    </div>
  );
}
