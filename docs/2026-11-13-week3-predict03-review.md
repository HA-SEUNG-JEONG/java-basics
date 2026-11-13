# 3주차 Predict03 예측 vs 실제 출력 비교

**날짜**: 2026-11-13  
**유형**: docs (학습 기록)  
**관련 파일**: `fe-collab/week3/Predict03.md`, `fe-collab/week3/Preditct03.java`

## 변경 내용

3주차 "먼저 손으로" 실습(`Predict03`)에서 결과를 먼저 예측한 뒤 JDK 21로 실제 실행해 비교했다. 예측 10줄 중 맞은 것은 2줄(16, 17번 줄)이다. 틀린 예측을 원인별로 정리해 다음 예측에서 같은 실수를 줄이는 것이 목적이다.

실행은 `Predict03.md` 원본이 아니라 사본으로 했다. 원본 24번 줄에 `//` 없이 `Book[2='Clean Code']`가 남아 있어 그대로는 컴파일되지 않기 때문이다. 사본에서는 그 줄의 잔여 텍스트만 지웠다.

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
| 15 | `Book[1='Clean Code']` | `Book[id=1, title=Clean Code]` | 오답 | record `toString()`은 `타입명[필드명=값, ...]`이고 값에 따옴표를 붙이지 않는다 |
| 16 | `true` | `true` | 정답 | record `equals`는 필드 값 비교 |
| 17 | `false` | `false` | 정답 | 서로 다른 객체라 `==`(참조 비교)는 `false` |
| 18 | `true` | `TDD` | 오답 | `find` → `Optional<Book>`, `map(Book::title)` → `Optional<String>`, `orElse` → `String` |
| 19 | `true` | `없음` | 오답 | `id==9`가 없어 `Optional.empty()`, `map`은 실행되지 않고 `orElse`의 기본값이 나온다 |
| 23 | `Book[2='TDD']` | `2` | 오답 | `Map<String, Integer>`의 값 타입은 `Integer` |
| 24 | `Book[2='Clean Code']` (`//` 누락) | `null` | 오답 | 넣지 않은 키는 `null`. `//` 누락은 컴파일 오류 |
| 29 | (예측 없음) | `읽기 전용 목록` | 오답 | `List.of`는 불변이라 `add`에서 `UnsupportedOperationException` |
| 34 | `true` | `true false true` | 오답 | 값이 3개. `Long` 캐시는 -128~127이라 `127L == 127L`은 `true`, `1000L == 1000L`은 `false`, `equals`는 `true` |
| 38 | `true` | `false true` | 오답 | 값이 2개. `new String("TDD")`는 힙의 새 객체라 `==`는 `false`, `equals`는 `true` |

## 틀린 패턴

1. **출력 개수를 세지 않음 (34, 38번 줄):** `println`에 이어 붙은 식이 몇 개인지 먼저 센다.
2. **반환 타입을 따라가지 않음 (18, 19, 23번 줄):** 메서드 체인과 제네릭은 한 단계마다 타입을 적으며 읽는다.
3. **record `toString` 형식을 잘못 기억 (15번 줄):** `필드명=값`을 쉼표로 나열한다.
4. **참조 비교와 값 비교 구분 (34, 38번 줄):** `==`는 참조, `equals`는 값이다. 캐시된 `Long`과 리터럴 `String`은 우연히 같은 객체라 `==`가 `true`일 수 있다.
5. **예외 흐름 누락 (29번 줄):** `List.of` · `Map.of` · `Set.of`는 불변이다.

## 관련 커밋

(이 기록과 관련된 커밋 없음 — `Predict03.md`, `Preditct03.java`는 아직 커밋되지 않은 상태)
