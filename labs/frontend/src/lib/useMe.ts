import { useQuery } from "@tanstack/react-query";
import { ApiError } from "@/api/client";
import { library } from "@/api/library";

/** 로그인 사용자. 로그인 안 했으면(401) 에러가 아니라 null — 화면이 "비로그인" 을 정상 상태로 다루게. */
export function useMe() {
  return useQuery({
    queryKey: ["me"],
    queryFn: async () => {
      try {
        return await library.me();
      } catch (e) {
        if (e instanceof ApiError && e.status === 401) return null;
        throw e;
      }
    },
  });
}
