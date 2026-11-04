import type { NextConfig } from "next";

// 브라우저는 /api/* 만 호출한다(same-origin). 이 요청을 Next 서버가 Spring(8080)으로 넘겨 주므로
// CORS preflight 도, 쿠키 도메인 문제도 생기지 않는다. /api 접두사는 넘길 때 떼어낸다.
const nextConfig: NextConfig = {
  turbopack: { root: import.meta.dirname }, // 상위 폴더의 lockfile 을 프로젝트 루트로 착각하지 않게
  async rewrites() {
    return [{ source: "/api/:path*", destination: "http://localhost:8080/:path*" }];
  },
};

export default nextConfig;
