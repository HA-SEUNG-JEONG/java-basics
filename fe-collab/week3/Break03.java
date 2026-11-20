import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Break03 {
    record Book(long id, String title) {}

    enum Status { AVAILABLE, ON_LOAN, LOST }

    static Optional<Book> find(long id) {
        return id == 1 ? Optional.of(new Book(1, "TDD")) : Optional.empty();
    }

    static String label(Status s) {
        return switch (s) {
            case AVAILABLE -> "대출 가능";
            case ON_LOAN -> "대출 중";
            case LOST -> "분실";
        };
    }

    public static void main(String[] args) {
        if(args.length == 0) throw new Exception("x");
        Book b = find(1).orElseThrow();
        List<String> titles = new ArrayList<>();
        titles.add(b.title());
        System.out.println(titles + " " + label(Status.ON_LOAN));
    }
}