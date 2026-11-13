# 3주차 Predict03 문법 정리

**날짜**: 2026-11-13  
**유형**: docs (학습 기록)  
**관련 파일**: `fe-collab/week3/Predict03.java`, `docs/2026-11-13-week3-predict03-review.md`

## 변경 내용

`Predict03.java`에 쓰인 문법 중 예측이 틀린 부분의 규칙을 정리한다. 예측과 실제 출력의 줄별 비교는 `2026-11-13-week3-predict03-review.md`에 있다.

## 1. `record` (7, 15~17번 줄)

```java
record Book(long id, String title) {}
```

- 선언 한 줄로 필드, 생성자, getter, `equals`, `hashCode`, `toString`이 생긴다. 필드는 `final`이다.
- getter 이름은 `getId()`가 아니라 `id()`, `title()`이다.
- `toString` 형식은 `타입명[필드명=값, 필드명=값]`이고 문자열 값에 따옴표가 없다.
  ```
  Book[id=1, title=Clean Code]
  ```
- `equals`는 모든 필드가 같으면 `true`다. 객체 안의 값을 비교한다.
- `new`는 항상 새 객체를 만들어 참조가 다르므로 `==`는 `false`다.
- TS 대응: `type Book = { readonly id: number; title: string }`. TS의 `{...} === {...}`는 `false`이고, Java record는 `equals`로 값 비교가 된다.

## 2. `Optional`과 `stream` (9~10, 18~19번 줄)

```java
books.stream().filter(b -> b.id() == id).findFirst()   // Optional<Book>
find(books, 2).map(Book::title).orElse("없음")          // String
```

- `Optional<T>`는 값이 있을 수도 없을 수도 있음을 나타내는 상자다. 값이 없으면 `Optional.empty()`다.
- `findFirst()`는 조건에 맞는 첫 요소를 `Optional`에 담는다. 없으면 empty다.
- `b -> b.id() == id`는 람다다. TS의 `b => b.id === id`에 대응한다.
- `Book::title`은 메서드 참조이며 `b -> b.title()`과 같다.
- `map(f)`는 값이 있을 때만 `f`를 적용한다. empty면 empty가 그대로 넘어간다.
- `orElse(x)`는 값이 있으면 그 값을, 없으면 `x`를 반환한다. `Optional<String>`이 `String`이 된다.

| 단계 | id=2 | id=9 |
|---|---|---|
| `find(...)` | `Optional[Book(2,"TDD")]` | `Optional.empty` |
| `.map(Book::title)` | `Optional["TDD"]` | `Optional.empty` |
| `.orElse("없음")` | `"TDD"` | `"없음"` |

TS 대응: `books.find(b => b.id === id)?.title ?? "없음"`.

## 3. `Map<K, V>`와 제네릭 (21~24번 줄)

```java
Map<String, Integer> stock = new HashMap<>();
stock.put("TDD", 2);
stock.get("TDD");          // 2
stock.get("Clean Code");   // null
```

- `<String, Integer>`는 키 `String`, 값 `Integer`다. `get`의 반환 타입은 값 타입 `V`다.
- `HashMap.get`은 없는 키에 예외 없이 `null`을 반환한다. TS `Map.get`의 `undefined`에 대응한다.
- `System.out.println(null 참조)`는 문자열 `null`을 출력한다.
- `new HashMap<>()`의 `<>`는 다이아몬드 연산자이며 왼쪽 선언에서 타입을 추론한다.

## 4. 불변 컬렉션과 `try/catch` (14, 26~30번 줄)

```java
List<Book> books = List.of(...);   // 불변
try { books.add(...); }            // UnsupportedOperationException
catch (UnsupportedOperationException e) { ... }
```

- `List.of`, `Set.of`, `Map.of`가 만드는 컬렉션은 불변이다. `add`, `remove`, `set` 호출 시 예외가 난다.
- 수정 가능한 컬렉션은 `new ArrayList<>(List.of(...))`로 복사해서 얻는다.
- `UnsupportedOperationException`은 `RuntimeException`(unchecked)이라 `try/catch` 없이도 컴파일된다.
- 예외가 발생하면 `try` 안의 남은 코드는 건너뛰고 일치하는 `catch`가 실행된다.

## 5. 래퍼 타입 `Long`과 `==` (32~34번 줄)

```java
Long a = 127L, b = 127L;      // 오토박싱: long → Long
Long c = 1000L, d = 1000L;
(a == b)     // true
(c == d)     // false
c.equals(d)  // true
```

- `long`은 원시 타입, `Long`은 객체(래퍼) 타입이다. 원시값을 객체로 바꾸는 오토박싱은 내부에서 `Long.valueOf`를 호출한다.
- `valueOf`는 -128~127 범위를 캐시해 같은 객체를 재사용한다. `a == b`는 같은 참조라 `true`다.
- 범위 밖(1000)은 호출마다 새 객체이므로 `c == d`는 참조 비교로 `false`다.
- `equals`는 값을 비교해 `true`다.
- `Integer`, `Short`, `Byte`, `Character`에도 같은 캐시 규칙이 있다.
- 원시 타입끼리의 `==`(`long x = 1000, y = 1000`)는 값 비교다.

## 6. 문자열 `+` 연결 (34, 38번 줄)

```java
System.out.println((a == b) + " " + (c == d) + " " + c.equals(d));
// "true" + " " + "false" + " " + "true"  →  true false true
```

- `boolean + String`은 문자열 연결이다. `boolean`은 `"true"`/`"false"`가 된다.
- 34번 줄은 출력 한 번에 값이 3개, 38번 줄은 2개다.
- `+`가 `==`보다 우선순위가 높아 비교식에는 괄호가 필요하다. 괄호가 없으면 `a == (b + " ")`로 해석되어 컴파일 오류가 난다.

## 7. `String`의 `==` vs `equals` (36~38번 줄)

```java
String s1 = "TDD";                // 문자열 풀의 리터럴
String s2 = new String("TDD");    // 힙의 새 객체
s1 == s2        // false
s1.equals(s2)   // true
```

- 리터럴 `"TDD"`는 문자열 풀에서 같은 객체를 공유한다. `"TDD" == "TDD"`는 `true`다.
- `new String(...)`은 항상 풀과 별개인 새 객체다.
- `s2.intern()`은 풀의 객체를 반환하므로 `s1 == s2.intern()`은 `true`다.
- TS의 `===`는 문자열 값을 비교하지만 Java의 `==`는 객체 참조를 비교한다. 문자열 비교는 `equals`다.

## 8. 주석 문법

- 한 줄 주석은 `// ...`, 여러 줄 주석은 `/* ... */`다.
- `//` 없이 적힌 `Book[2='Clean Code']`는 코드로 읽혀 컴파일할 수 없다.

## 요약

| 개념 | 규칙 |
|---|---|
| `==` | 객체는 참조 비교, 원시 타입은 값 비교 |
| `equals` | 값 비교 (record, `String`, 래퍼 타입) |
| 캐시 | `Long`·`Integer`는 -128~127, `String`은 리터럴 풀 |
| `Optional` | `map`은 값이 있을 때만 적용, `orElse`로 꺼냄 |
| `Map.get` | 없는 키는 `null` |
| `List.of` | 불변, `add`는 `UnsupportedOperationException` |
| 문자열 `+` | 다른 타입과 이으면 문자열 연결, 비교식은 괄호 필요 |

