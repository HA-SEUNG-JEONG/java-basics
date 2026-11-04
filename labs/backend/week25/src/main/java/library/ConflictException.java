package library;

/** 409 로 바뀌는 업무 예외 (isbn 중복, 이미 대출 중, 한도 초과, 이미 반납) */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
