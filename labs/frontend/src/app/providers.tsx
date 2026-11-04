"use client";

import { MutationCache, QueryClient, QueryClientProvider } from "@tanstack/react-query";
import dynamic from "next/dynamic";
import { useState, type ReactNode } from "react";
import { ApiError } from "@/api/client";

const MOCKING = process.env.NEXT_PUBLIC_API_MOCKING === "enabled";
// msw/browser 는 node 에서 못 불러오므로(SSR 번들 실패) 브라우저 전용으로 따로 불러온다.
const MockGate = dynamic(() => import("@/mocks/MockGate"), { ssr: false });

function makeQueryClient() {
  const client: QueryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } }, // 4xx 는 다시 해도 같다
    mutationCache: new MutationCache({
      // 쓰기 요청이 401 이면(세션 만료 등) 로그인 상태 표시도 즉시 로그아웃으로
      onError: (error) => {
        if (error instanceof ApiError && error.status === 401) client.setQueryData(["me"], null);
      },
    }),
  });
  return client;
}

export function Providers({ children }: { children: ReactNode }) {
  const [client] = useState(makeQueryClient);
  return <QueryClientProvider client={client}>{MOCKING ? <MockGate>{children}</MockGate> : children}</QueryClientProvider>;
}
