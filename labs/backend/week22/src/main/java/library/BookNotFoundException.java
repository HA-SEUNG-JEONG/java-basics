package library;

public class BookNotFoundException extends RuntimeException {
    public BookNotFoundException(long id) {
        super("책을 찾을 수 없습니다: " + id);
    }
}
