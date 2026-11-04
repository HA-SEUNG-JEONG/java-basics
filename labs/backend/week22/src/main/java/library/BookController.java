package library;

import java.util.List;
import org.springframework.http.ResponseEntity;

/**
 * 해야 할 일: 이 클래스를 REST 컨트롤러로 만들고 아래 API 를 완성하세요.
 *   GET    /books        -> 200, 책 목록 (JSON 배열)
 *   GET    /books/{id}   -> 200, 책 하나. 없으면 BookService 가 던지는 예외가 404 로 바뀌어야 함
 *   POST   /books        -> 201, 만들어진 책 + Location 헤더(/books/{id}). 본문 검증 실패는 400
 *   PUT    /books/{id}   -> 200, 수정된 책. 본문 검증 실패는 400
 *   DELETE /books/{id}   -> 204, 본문 없음
 * 예외 -> 상태 코드 변환은 ApiExceptionHandler 에서 합니다.
 */
public class BookController {
    private final BookService service;

    public BookController(BookService service) {
        this.service = service;
    }

    public List<Book> list() {
        // 여기를 고치세요
        throw new UnsupportedOperationException("list 를 구현하세요");
    }

    public Book get(long id) {
        // 여기를 고치세요
        throw new UnsupportedOperationException("get 을 구현하세요");
    }

    public ResponseEntity<Book> create(BookRequest req) {
        // 여기를 고치세요
        throw new UnsupportedOperationException("create 를 구현하세요");
    }

    public Book update(long id, BookRequest req) {
        // 여기를 고치세요
        throw new UnsupportedOperationException("update 를 구현하세요");
    }

    public void delete(long id) {
        // 여기를 고치세요
        throw new UnsupportedOperationException("delete 를 구현하세요");
    }
}
