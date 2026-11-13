import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Predict03 {
record Book(long id, String title) {}

    static Optional<Book> find(List<Book> books, long id) {
        return books.stream().filter(b -> b.id() == id).findFirst();
    }

    public static void main(String[] args) {
        List<Book> books = List.of(new Book(1, "Clean Code"), new Book(2, "TDD"));
        System.out.println(books.get(0)); // "Clean Code"
        System.out.println(books.get(0).equals(new Book(1, "Clean Code"))); // true
        System.out.println(books.get(0) == new Book(1, "Clean Code")); // true
        System.out.println(find(books, 2).map(Book::title).orElse("없음")); // true
        System.out.println(find(books, 9).map(Book::title).orElse("없음")); // true

        Map<String, Integer> stock = new HashMap<>();
        stock.put("TDD", 2);
        System.out.println(stock.get("TDD")); // true
        System.out.println(stock.get("Clean Code")); // true

        try {
            books.add(new Book(3, "Refactoring"));
        } catch (UnsupportedOperationException e) {
            System.out.println("읽기 전용 목록");
        }

        Long a = 127L, b = 127L;
        Long c = 1000L, d = 1000L;
        System.out.println((a == b) + " " + (c == d) + " " + c.equals(d));

        String s1 = "TDD";
        String s2 = new String("TDD");
        System.out.println((s1 == s2) + " " + s1.equals(s2)); // true
    }
}

---

## 예측 vs 실제 (10줄)

| # | 코드 | 내 예측 | 실제 출력 | ★ | 왜 그렇게 나왔나 |
|---|------|------|------|---|------|
| 1 | `println(books.get(0))` | `"Clean Code"` | `Book[id=1, title=Clean Code]` | ★ | record 는 `toString()` 을 자동으로 만들어서 `Book[id=1, title=Clean Code]` 모양으로 출력한다. |
| 2 | `books.get(0).equals(new Book(1, "Clean Code"))` | `true` | `true` | | record 의 `equals` 는 필드 내용을 비교한다. |
| 3 | `books.get(0) == new Book(1, "Clean Code")` | `true` | `false` | ★ | `==` 는 같은 객체인지 비교하는데 `new` 는 항상 새 객체를 만든다. |
| 4 | `find(books, 2).map(Book::title).orElse("없음")` | `true` | `TDD` | ★ | id 2 인 책이 있어서 `Optional` 안의 `title` 이 나온다. |
| 5 | `find(books, 9).map(Book::title).orElse("없음")` | `true` | `없음` | ★ | id 9 인 책이 없어 빈 `Optional` 이 되고 `orElse` 의 기본값이 나온다. |
| 6 | `stock.get("TDD")` | `true` | `2` | ★ | `put("TDD", 2)` 로 넣은 값 `Integer` 2 가 나온다. |
| 7 | `stock.get("Clean Code")` | `true` | `null` | ★ | `Map.get` 은 없는 키에 예외나 `undefined` 가 아니라 `null` 을 돌려준다. |
| 8 | `books.add(...)` 의 catch | (적지 않음) | `읽기 전용 목록` | ★ | `List.of` 로 만든 목록은 수정 불가라서 `add` 가 `UnsupportedOperationException` 을 던진다. |
| 9 | `(a == b) + " " + (c == d) + " " + c.equals(d)` | (적지 않음) | `true false true` | ★ | `Long` 은 -128~127 만 캐시를 재사용해서 127 은 `==` 가 `true`, 1000 은 새 객체라 `false` 이고, `equals` 는 값 비교라 `true` 다. |
| 10 | `(s1 == s2) + " " + s1.equals(s2)` | `true` | `false true` | ★ | `new String("TDD")` 는 새 객체라 `==` 는 `false` 이고, `equals` 는 내용 비교라 `true` 다. |