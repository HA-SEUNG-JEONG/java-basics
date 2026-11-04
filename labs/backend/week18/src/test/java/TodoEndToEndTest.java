import static org.junit.jupiter.api.Assertions.*;

import java.net.http.HttpClient;
import org.junit.jupiter.api.Test;

/** 내가 만든 서버와 내가 만든 클라이언트를 직접 연결합니다. */
class TodoEndToEndTest {
    @Test
    void myClientTalksToMyServer() throws Exception {
        TodoServer server = new TodoServer();
        int port = server.start(0);
        try {
            TodoClient client = new TodoClient(HttpClient.newHttpClient(), "http://127.0.0.1:" + port);
            assertTrue(client.list().isEmpty());
            Todo a = client.create("서버와 클라이언트");
            assertEquals("서버와 클라이언트", a.title());
            assertEquals(a, client.find(a.id()).orElseThrow());
            assertTrue(client.find(12345).isEmpty());
            assertEquals(1, client.list().size());
            ApiException e = assertThrows(ApiException.class, () -> client.create(""));
            assertEquals(400, e.status());
        } finally {
            server.stop();
        }
    }
}
