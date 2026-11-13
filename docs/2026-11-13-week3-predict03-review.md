# 3주차 Predict03 예측 vs 실제 출력

**날짜**: 2026-11-13  
**유형**: docs (학습 기록)  
**관련 파일**: `fe-collab/week3/Predict03.md`, `fe-collab/week3/Predict03.java`

## 변경 내용

3주차 "먼저 손으로" 실습 `Predict03`의 예측 10줄 중 정답은 2줄(16, 17번 줄)이다. 아래 실제 출력은 JDK 21 기준이다.

예측 작성본 `Predict03.md`의 24번 줄에는 `//` 없이 `Book[2='Clean Code']`가 남아 있어 그대로는 컴파일되지 않는다.

## 실제 출력

```
Book[id=1, title=Clean Code]
true
false
TDD
없음
2
null
읽기 전용 목록
true false true
false true
```

## 예측 비교

| 줄 | 내 예측 | 실제 | 판정 | 원인 |
|----|---------|------|------|------|
| 15 | `Book[1='Clean Code']` | `Book[id=1, title=Clean Code]` | 오답 | record `toString()`은 `타입명[필드명=값, ...]`이고 값에 따옴표가 붙지 않는다 |
| 16 | `true` | `true` | 정답 | record `equals`는 필드 값 비교 |
| 17 | `false` | `false` | 정답 | 서로 다른 객체라 `==`(참조 비교)는 `false` |
| 18 | `true` | `TDD` | 오답 | `find` → `Optional<Book>`, `map(Book::title)` → `Optional<String>`, `orElse` → `String` |
| 19 | `true` | `없음` | 오답 | `id==9`가 없으면 `Optional.empty()`이고, `map`은 실행되지 않으며 `orElse`의 기본값이 나온다 |
| 23 | `Book[2='TDD']` | `2` | 오답 | `Map<String, Integer>`의 값 타입은 `Integer` |
| 24 | `Book[2='Clean Code']` (`//` 누락) | `null` | 오답 | 없는 키의 `get`은 `null`. `//` 누락은 컴파일 오류 |
| 29 | (예측 없음) | `읽기 전용 목록` | 오답 | `List.of`는 불변이라 `add`에서 `UnsupportedOperationException` 발생 |
| 34 | `true` | `true false true` | 오답 | 값 3개. `Long` 캐시 범위는 -128~127이라 `127L == 127L`은 `true`, `1000L == 1000L`은 `false`, `equals`는 `true` |
| 38 | `true` | `false true` | 오답 | 값 2개. `new String("TDD")`는 힙의 새 객체라 `==`는 `false`, `equals`는 `true` |

## 원칙

1. **출력 개수:** `println`에 이어 붙은 식의 수만큼 값이 출력된다 (34, 38번 줄).
2. **반환 타입:** 메서드 체인과 제네릭은 단계마다 타입이 정해진다 (18, 19, 23번 줄).
3. **record `toString`:** `필드명=값`을 쉼표로 나열한다 (15번 줄).
4. **참조 비교와 값 비교:** `==`는 참조, `equals`는 값이다. 캐시된 `Long`과 리터럴 `String`은 같은 객체일 수 있어 `==`가 `true`가 된다 (34, 38번 줄).
5. **불변 컬렉션:** `List.of` · `Map.of` · `Set.of`는 수정 시 예외가 난다 (29번 줄).

## 관련 커밋

- `bec784d` docs: 3주차 Predict03 예측과 실제 출력 비교 기록
