/** 서버가 2xx 가 아닌 응답을 줬을 때 클라이언트가 던지는 예외. */
public class ApiException extends RuntimeException {
    private final int status;
    private final String body;

    public ApiException(int status, String body) {
        super("HTTP " + status + ": " + body);
        this.status = status;
        this.body = body;
    }

    public int status() { return status; }
    public String body() { return body; }
}
