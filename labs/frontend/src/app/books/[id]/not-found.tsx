import { ApiError } from "@/api/client";
import { ErrorNotice } from "@/components/ErrorNotice";

// 클라이언트의 404 와 같은 ErrorNotice 를 쓴다: 서버에서 그려도 화면 규약은 하나다.
export default function BookNotFound() {
  return <ErrorNotice error={new ApiError(404, "요청한 책이 없습니다")} />;
}
