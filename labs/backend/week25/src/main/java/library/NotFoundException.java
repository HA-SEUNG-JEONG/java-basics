package library;

/** 404 로 바뀌는 업무 예외 */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
