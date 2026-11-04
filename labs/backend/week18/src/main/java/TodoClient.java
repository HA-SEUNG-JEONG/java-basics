import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;

/**
 * 18주차 실습 2. 위 서버(또는 같은 규칙의 서버)를 호출하는 클라이언트.
 * 2xx 가 아닌 응답은 ApiException 으로 바꿔 던집니다. (find 의 404 만 예외)
 */
public class TodoClient {
    private final HttpClient http;
    private final String baseUrl;
    private final ObjectMapper mapper = new ObjectMapper();

    public TodoClient(HttpClient http, String baseUrl) {
        this.http = http;
        this.baseUrl = baseUrl;
    }

    /** GET /todos 를 호출해 List<Todo> 로 돌려준다. (힌트: new TypeReference<List<Todo>>() {}) */
    public List<Todo> list() throws IOException, InterruptedException {
        // 여기를 고치세요
        throw new UnsupportedOperationException("list 를 구현하세요");
    }

    /** POST /todos, 본문 {"title": ...}, Content-Type: application/json. 201 이 아니면 ApiException. */
    public Todo create(String title) throws IOException, InterruptedException {
        // 여기를 고치세요
        throw new UnsupportedOperationException("create 를 구현하세요");
    }

    /** GET /todos/{id}. 200 이면 값, 404 면 Optional.empty(), 그 밖에는 ApiException. */
    public Optional<Todo> find(long id) throws IOException, InterruptedException {
        // 여기를 고치세요
        throw new UnsupportedOperationException("find 를 구현하세요");
    }
}
