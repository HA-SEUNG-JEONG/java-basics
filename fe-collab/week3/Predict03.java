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
        System.out.println(books.get(0));
        System.out.println(books.get(0).equals(new Book(1, "Clean Code")));
        System.out.println(books.get(0) == new Book(1, "Clean Code"));
        System.out.println(find(books, 2).map(Book::title).orElse("없음"));
        System.out.println(find(books, 9).map(Book::title).orElse("없음"));

        Map<String, Integer> stock = new HashMap<>();
        stock.put("TDD", 2);
        System.out.println(stock.get("TDD"));
        System.out.println(stock.get("Clean Code"));

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
        System.out.println((s1 == s2) + " " + s1.equals(s2));
    }
}