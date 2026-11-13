# 프론트엔드 협업 트랙 (Phase 6, 25~29주)

**날짜**: 2026-10-22  
**유형**: feat  
**관련 파일**: `README.md`, `week25-api-contract.md`, `week26-error-handling.md`, `week27-auth-cors.md`, `week28-local-integration.md`, `week29-final-frontend.md`, `labs/backend/week25/**`, `labs/backend/settings.gradle.kts`, `labs/frontend/**`

## 변경 내용

기존 커리큘럼은 Java/Spring 으로 백엔드를 만드는 데 초점이 있다. 학습자는 React·TypeScript·Next.js 를 주로 쓰는 프론트엔드 개발자이고 목표는 백엔드 전문가가 아니라 **백엔드와 협업하며 프론트와 함께 쓰는 것**이다. 1~24주는 그대로이고, 24주차 도서관 API 를 "백엔드 팀이 준 API"로 보고 그 위에 Next.js 클라이언트를 얹는 Phase 6 이 있다.

## 주요 구현 사항

- `labs/backend/week25`: 24주차 명세의 참조 API. springdoc 으로 `/v3/api-docs` 노출, CORS(`localhost:3000`, credentials), 세션 쿠키 로그인(`demo` / `demo1234`, HttpOnly·SameSite=Lax), 쓰기 엔드포인트 401 보호, 모든 오류 `{"message"}` 통일. 테스트 31개.
- `labs/frontend`: Next.js App Router + TypeScript + Tailwind(토큰 기반). `openapi-typescript` 로 `schema.d.ts` 생성, `openapi-fetch` + `ApiError`/`uiKindOf` 로 400/401/404/409 를 UI 종류로 매핑, `/api` rewrites 프록시, MSW 목킹, Vitest 18개, Playwright e2e 2개.
- 문서 5개(25 API 계약 · 26 에러 처리 · 27 인증·CORS · 28 로컬 통합 · 29 최종 프로젝트). 문서의 명령·출력은 실제 실행으로 확인된 값뿐이다.
- `README.md`: Phase 6 표, 실습 안내, 페이스 표·마일스톤·보조 자료 행 포함. 기존 문장 중 "다음 단계" 절의 두 곳만 다르다.

## 검증 결과

작성자와 분리된 verifier 의 재실행 결과: 백엔드 31개, 프론트 typecheck·test 18·build·e2e(프록시/직접 호출/MSW) 통과, `schema.d.ts` 재생성 해시 동일, 새 파일의 TODO·skip 0건, Tailwind px 임의값·하드코딩 색상 0건, 기존 week01~24 파일 무변경.

## 알려진 한계

- Node 22.12 이상 필요 (vitest·msw 요구).
- CSRF 비활성(데모). 운영 대안은 문서에 개념으로만 서술.
- BFF(Route Handler), 서버 컴포넌트 에러 처리(`error.tsx`)는 저장소에 구현 없음.
- MSW 목의 메시지 언어·로그인 상태 모델이 실서버와 다름.
- README "학습 방법" 6번의 마일스톤 목록(4·9·13·16·24주)에는 29주 미포함. 기존 문장 수정에 해당해 보류 상태.

## 관련 커밋

없음 (기록 당시 git 저장소 아님).
