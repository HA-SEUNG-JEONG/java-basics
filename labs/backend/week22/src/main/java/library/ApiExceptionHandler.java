package library;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;

/**
 * 해야 할 일: 모든 컨트롤러의 예외를 한 곳에서 응답으로 바꾸는 클래스로 만드세요.
 *   BookNotFoundException          -> 404, 본문 {"message": 예외 메시지}
 *   MethodArgumentNotValidException -> 400, 본문 {"message": "title: ..." 처럼 틀린 필드 이름이 들어간 문장}
 */
public class ApiExceptionHandler {

    public ResponseEntity<ApiError> notFound(BookNotFoundException e) {
        // 여기를 고치세요
        throw new UnsupportedOperationException("notFound 를 구현하세요");
    }

    public ResponseEntity<ApiError> invalid(MethodArgumentNotValidException e) {
        // 여기를 고치세요
        throw new UnsupportedOperationException("invalid 를 구현하세요");
    }
}
