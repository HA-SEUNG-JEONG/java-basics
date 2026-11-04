import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 18주차 실습 1. JDK 에 들어 있는 HttpServer 로 만드는 작은 REST 서버.
 * 라우팅(handle)은 이미 되어 있습니다. "여기를 고치세요" 가 붙은 메서드 5개를 채우세요.
 * 테스트: ./gradlew :week18:test
 *
 * 응답 규칙
 *   GET    /todos       200 + JSON 배열
 *   POST   /todos       201 + Location: /todos/{id} + 생성된 Todo.  JSON 이 깨졌거나 title 이 비면 400 {"error":"..."}
 *   GET    /todos/{id}  200 + Todo,  없으면 404 {"error":"..."}
 *   DELETE /todos/{id}  204 (본문 없음). 없는 id 여도 204
 *   그 밖의 메서드      405 {"error":"..."}
 */
public class TodoServer {
    private final ObjectMapper mapper = new ObjectMapper();
    private final List<Todo> todos = new CopyOnWriteArrayList<>();
    private final AtomicLong seq = new AtomicLong();
    private HttpServer server;

    /** 서버를 켜고 실제 포트 번호를 돌려준다. port 가 0 이면 빈 포트를 OS 가 골라 준다. */
    public int start(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        server.createContext("/", this::handle);
        server.start();
        return server.getAddress().getPort();
    }

    public void stop() {
        if (server != null) server.stop(0);
    }

    // ---- 라우팅: 고치지 않습니다 ----
    private void handle(HttpExchange ex) throws IOException {
        try {
            String method = ex.getRequestMethod();
            String path = ex.getRequestURI().getPath();
            if (path.equals("/todos")) {
                switch (method) {
                    case "GET" -> list(ex);
                    case "POST" -> create(ex);
                    default -> send(ex, 405, Map.of("error", "허용되지 않는 메서드: " + method));
                }
            } else if (path.matches("/todos/\\d+")) {
                long id = Long.parseLong(path.substring("/todos/".length()));
                switch (method) {
                    case "GET" -> get(ex, id);
                    case "DELETE" -> delete(ex, id);
                    default -> send(ex, 405, Map.of("error", "허용되지 않는 메서드: " + method));
                }
            } else {
                send(ex, 404, Map.of("error", "없는 경로: " + path));
            }
        } catch (Exception e) {
            send(ex, 500, Map.of("error", String.valueOf(e)));
        } finally {
            ex.close();
        }
    }

    // ---- 아래 5개를 채웁니다 ----

    /**
     * status 와 JSON 본문을 보낸다.
     * 힌트: mapper.writeValueAsBytes(body), Content-Type 은 "application/json; charset=utf-8",
     * ex.sendResponseHeaders(status, 바이트길이) 다음에 ex.getResponseBody() 에 쓴다.
     */
    private void send(HttpExchange ex, int status, Object body) throws IOException {
        // 여기를 고치세요
        throw new UnsupportedOperationException("send 를 구현하세요");
    }

    private void list(HttpExchange ex) throws IOException {
        // 여기를 고치세요
        throw new UnsupportedOperationException("list 를 구현하세요");
    }

    /**
     * 본문을 NewTodo 로 읽는다 (mapper.readValue(ex.getRequestBody(), NewTodo.class)).
     * 읽다가 예외가 나거나 title 이 null/공백이면 400. 성공하면 id 는 seq.incrementAndGet().
     */
    private void create(HttpExchange ex) throws IOException {
        // 여기를 고치세요
        throw new UnsupportedOperationException("create 를 구현하세요");
    }

    private void get(HttpExchange ex, long id) throws IOException {
        // 여기를 고치세요
        throw new UnsupportedOperationException("get 을 구현하세요");
    }

    /** 본문 없는 응답은 sendResponseHeaders(204, -1) 입니다. 길이 -1 은 "본문 없음" 이라는 뜻. */
    private void delete(HttpExchange ex, long id) throws IOException {
        // 여기를 고치세요
        throw new UnsupportedOperationException("delete 를 구현하세요");
    }

    // 풀이에서 쓸 수 있는 도우미 (필요하면 사용)
    private Optional<Todo> find(long id) {
        return todos.stream().filter(t -> t.id() == id).findFirst();
    }
}
