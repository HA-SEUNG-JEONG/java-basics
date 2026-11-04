import static org.junit.jupiter.api.Assertions.*;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** 가짜 서버(테스트 안에서 만든 HttpServer)를 상대로 TodoClient 만 따로 검증합니다. */
class TodoClientTest {
    HttpServer fake;
    TodoClient client;
    final AtomicReference<String> lastMethod = new AtomicReference<>();
    final AtomicReference<String> lastBody = new AtomicReference<>();
    final AtomicReference<String> lastContentType = new AtomicReference<>();
    volatile int cannedStatus = 200;
    volatile String cannedBody = "[]";

    @BeforeEach
    void start() throws Exception {
        fake = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        fake.createContext("/", ex -> {
            lastMethod.set(ex.getRequestMethod() + " " + ex.getRequestURI().getPath());
            lastBody.set(new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            lastContentType.set(ex.getRequestHeaders().getFirst("Content-Type"));
            byte[] out = cannedBody.getBytes(StandardCharsets.UTF_8);
            ex.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
            ex.sendResponseHeaders(cannedStatus, out.length);
            ex.getResponseBody().write(out);
            ex.close();
        });
        fake.start();
        client = new TodoClient(HttpClient.newHttpClient(), "http://127.0.0.1:" + fake.getAddress().getPort());
    }

    @AfterEach
    void stop() {
        fake.stop(0);
    }

    @Test
    void list_parsesArrayIntoRecords() throws Exception {
        cannedBody = "[{\"id\":1,\"title\":\"하나\",\"done\":false},{\"id\":2,\"title\":\"둘\",\"done\":true}]";
        List<Todo> todos = client.list();
        assertEquals("GET /todos", lastMethod.get());
        assertEquals(List.of(new Todo(1, "하나", false), new Todo(2, "둘", true)), todos);
    }

    @Test
    void create_postsJsonAndParsesResult() throws Exception {
        cannedStatus = 201;
        cannedBody = "{\"id\":7,\"title\":\"새 일\",\"done\":false}";
        Todo created = client.create("새 일");
        assertEquals("POST /todos", lastMethod.get());
        assertTrue(lastContentType.get() != null && lastContentType.get().startsWith("application/json"),
                "요청 Content-Type 이 application/json 이어야 합니다: " + lastContentType.get());
        assertTrue(lastBody.get().contains("\"title\"") && lastBody.get().contains("새 일"),
                "요청 본문에 title 이 있어야 합니다: " + lastBody.get());
        assertEquals(new Todo(7, "새 일", false), created);
    }

    @Test
    void create_titleWithQuoteIsEscapedProperly() throws Exception {
        cannedStatus = 201;
        cannedBody = "{\"id\":1,\"title\":\"x\",\"done\":false}";
        client.create("그는 \"안녕\"이라고 했다");
        assertTrue(new com.fasterxml.jackson.databind.ObjectMapper().readTree(lastBody.get()).has("title"),
                "따옴표가 들어간 제목도 올바른 JSON 이어야 합니다 (문자열 + 로 JSON 을 만들지 마세요): " + lastBody.get());
    }

    @Test
    void find_returnsValueOr404Empty() throws Exception {
        cannedBody = "{\"id\":3,\"title\":\"셋\",\"done\":false}";
        assertEquals(new Todo(3, "셋", false), client.find(3).orElseThrow());
        assertEquals("GET /todos/3", lastMethod.get());

        cannedStatus = 404;
        cannedBody = "{\"error\":\"없음\"}";
        assertTrue(client.find(99).isEmpty());
    }

    @Test
    void errorStatus_becomesApiException() {
        cannedStatus = 500;
        cannedBody = "{\"error\":\"boom\"}";
        ApiException e = assertThrows(ApiException.class, () -> client.list());
        assertEquals(500, e.status());
        assertTrue(e.body().contains("boom"));
    }

    @Test
    void create_non201IsApiException() {
        cannedStatus = 400;
        cannedBody = "{\"error\":\"title 이 비었습니다\"}";
        ApiException e = assertThrows(ApiException.class, () -> client.create(""));
        assertEquals(400, e.status());
    }
}
