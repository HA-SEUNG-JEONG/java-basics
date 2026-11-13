# 프론트엔드 협업 커리큘럼 (Java 24주 + 협업 5주 → 협업 14주)

**날짜**: 2026-10-22  
**유형**: feat  
**관련 파일**: `README.md`, `../../fe-collab/week1/week01-http-devtools.md` ~ `fe-collab/week14-final-project.md`, `archive/java-24weeks/` (30개), `labs/frontend/**`

## 변경 내용

기존 커리큘럼은 Java 24주 과정과 협업 5주 과정으로 구성되어 있었다. 학습 대상은 React·TypeScript·Next.js 로 일하는 프론트엔드 개발자다. 이들의 목표는 백엔드 전문가가 되는 것이 아니라, 백엔드 개발자와 같은 말로 협업하고 백엔드를 프론트와 함께 로컬에서 띄워 디버깅하는 것이다.

이 목표에 맞는 과정은 협업 중심 14주다. 1~5주는 백엔드 코드를 읽는 법, 6~8주는 API 계약, 9~12주는 통합, 13~14주는 실무를 다룬다. 실습 대상은 저장소의 참조 API(도서관 API)이며, 이 API 는 "백엔드 팀이 준 API"로 취급한다.

| 구분 | 주차 | 주제 |
|------|------|------|
| A. 백엔드 읽기 | 1~5주 | HTTP·DevTools·curl, REST·JSON 계약, Java 코드 읽기, Spring 코드 읽기, DB·JPA 개념 |
| B. API 계약 | 6~8주 | OpenAPI 로 타입 만들기, 에러 처리, 인증·CORS |
| C. 통합 | 9~12주 | 로컬 통합 개발 환경, 데이터 패칭 설계, Next 서버 경계, 테스트 전략 |
| D. 실무 | 13~14주 | 협업 실무, 최종 프로젝트 |

마일스톤은 5주, 8주, 14주 끝에 있다.

## 주요 구현 사항

### 1. 문서 구조

- `fe-collab/` 에 14주 문서가 있다. 파일명은 `week01-http-devtools.md` 부터 `week14-final-project.md` 까지다.
- 루트 `README.md` 는 새 커리큘럼 안내다. 페이스 선택(주당 세션 수 기준), 전체 로드맵, 마일스톤 표를 담는다.
- 옛 `week01` ~ `week29` 문서와 옛 `README.md` 는 `archive/java-24weeks/` 에 있다. 모두 30개 파일이다.
- 아카이브 문서 안의 `labs/` 참조는 `../../labs/` 다. 아카이브 위치에서 실습 폴더를 가리킨다.
- `labs/` 는 원래 위치에 있다. `labs/backend/settings.gradle.kts` 가 `week17`~`week23`, `week25` 를 Gradle include 로 참조하므로 위치를 바꾸면 빌드가 깨진다.

### 2. 실습 코드 (`labs/frontend`)

| 경로 | 내용 |
|------|------|
| `src/api/server.ts` | 서버 컴포넌트와 Route Handler 에서 백엔드를 호출하는 클라이언트(`serverClient`, `getMe`) |
| `src/app/books/[id]/page.tsx` | 도서 상세 페이지(서버 컴포넌트) |
| `src/app/books/[id]/not-found.tsx` | 없는 도서를 보여 주는 화면 |
| `src/app/books/[id]/error.tsx` | 도서 상세 페이지의 오류 화면 |
| `src/app/bff/summary/route.ts` | 책 목록과 내 정보 두 호출을 묶는 Route Handler(BFF) 예시 |
| `src/lib/query-design.test.ts` | Vitest 테스트 12개. 캐시 키와 무효화, 로딩 상태, `staleTime`, 반납의 낙관적 업데이트, 페이지네이션, 재시도(5xx 만 재시도)를 다룬다 |
| `README.md` (`labs/frontend`) | 서버 경로 예외 설명. 서버 컴포넌트와 BFF 는 백엔드를 직접 호출하므로 MSW 목킹이 적용되지 않고, 백엔드가 떠 있어야 한다 |
| `.env.example` | `API_BASE_URL` 항목(주석 처리된 예시 `# API_BASE_URL=http://localhost:8080`) |

### 3. 그대로인 것

- 백엔드 코드(`labs/backend`)와 `labs/backend/settings.gradle.kts` 는 그대로다.
- `labs/week*` 와 `labs/solutions` 도 그대로다.

## 검증 결과

출처: 작성자와 분리된 검증자(opus)의 보고.

- 문서 내부 링크가 끊어진 곳 0건.
- 백엔드 `:week25:test` 31개 통과.
- 프론트엔드 `typecheck` 통과, `pnpm test` 30개 통과(18 + 12), `build` 통과, `e2e` 2개 통과(프록시 경로, 직접 호출 경로, MSW 목킹).
- 문서의 명령과 재실행 출력의 불일치는 9건이 발견되어 잔여 0건이다.
- 새 파일에 금지 패턴(TODO, `test.skip`, `.only`)이 0건이다.
- Tailwind 에서 px 임의값과 하드코딩 색상이 0건이다.
- 한글 문자가 깨진 곳이 0건이다.

기록 당시 이 저장소는 git 저장소가 아니어서 변경 전후를 diff 로 확인할 수 없었다. 백엔드와 기존 `labs` 파일이 그대로라는 사실은 검증자 보고에 근거한다.

## 알려진 한계

- 13주의 Docker 실습(3.1절)은 Colima 와 docker CLI 로 실제 실행 확인된 범위다. 3.2절의 해결 방향 세 가지, 프록시 뒤 `X-Forwarded-Proto`, `docker compose` 는 실행 확인 없는 개념 설명이다.
- 14개 주차 문서에 1시간 단위 세션 표(전체 118세션)가 있고 README 페이스는 세션 수 기준이다. 하루 1시간 학습 시간에 맞춘 구성이며 내용 분량은 줄어들지 않았다.
- DevTools 화면 설명은 실제 화면과 대조되지 않았다.
- 참조 API 에는 페이지네이션이 없다. 그래서 오프셋과 커서 페이지네이션은 개념과 비교로만 다루고, 테스트 파일의 핸들러가 흉내 낸 동작으로 검증한다.
- 옛 Java 실습의 채점기 안내는 `archive/java-24weeks/` 문서에만 남아 있다. 새 커리큘럼에는 안내가 없다.
- 파일 이동은 git 이력에 없다. 복원 방법: `archive/java-24weeks/` 에서 원래 위치로 역방향 이동.

## 관련 커밋

없음 (기록 당시 git 저장소 아님).
