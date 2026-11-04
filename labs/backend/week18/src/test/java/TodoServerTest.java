import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TodoServerTest {
    TodoServer server;
    String base;
    HttpClient http = HttpClient.newHttpClient();
    ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void start() throws Exception {
        server = new TodoServer();
        base = "http://127.0.0.1:" + server.start(0);
    }

    @AfterEach
    void stop() {
        server.stop();
    }

    HttpResponse<String> send(String method, String path, String body) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(base + path));
        if (body == null) {
            b.method(method, BodyPublishers.noBody());
        } else {
            b.header("Content-Type", "application/json")
                    .method(method, BodyPublishers.ofString(body, StandardCharsets.UTF_8));
        }
        return http.send(b.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    @Test
    void list_isEmptyArrayAtFirst() throws Exception {
        var res = send("GET", "/todos", null);
        assertEquals(200, res.statusCode());
        assertEquals("[]", res.body());
    }

    @Test
    void responsesAreJsonUtf8() throws Exception {
        var res = send("GET", "/todos", null);
        String type = res.headers().firstValue("Content-Type").orElse("");
        assertTrue(type.startsWith("application/json"), "Content-Type 이 application/json 이어야 합니다: " + type);
        assertTrue(type.toLowerCase().contains("utf-8"), "charset=utf-8 이 필요합니다: " + type);
    }

    @Test
    void create_returns201WithLocationAndBody() throws Exception {
        var res = send("POST", "/todos", "{\"title\":\"자바 공부\"}");
        assertEquals(201, res.statusCode());
        JsonNode json = mapper.readTree(res.body());
        assertEquals("자바 공부", json.get("title").asText());
        assertFalse(json.get("done").asBoolean());
        long id = json.get("id").asLong();
        assertTrue(id > 0);
        assertEquals("/todos/" + id, res.headers().firstValue("Location").orElse("(Location 없음)"));
    }

    @Test
    void create_thenListAndGet() throws Exception {
        send("POST", "/todos", "{\"title\":\"하나\"}");
        send("POST", "/todos", "{\"title\":\"둘\"}");
        JsonNode all = mapper.readTree(send("GET", "/todos", null).body());
        assertEquals(2, all.size());
        long secondId = all.get(1).get("id").asLong();
        var one = send("GET", "/todos/" + secondId, null);
        assertEquals(200, one.statusCode());
        assertEquals("둘", mapper.readTree(one.body()).get("title").asText());
    }

    @Test
    void create_koreanTitleSurvivesRoundTrip() throws Exception {
        send("POST", "/todos", "{\"title\":\"한글 할 일 ✔\"}");
        var res = send("GET", "/todos", null);
        assertEquals("한글 할 일 ✔", mapper.readTree(res.body()).get(0).get("title").asText());
    }

    @Test
    void create_brokenJson_is400WithErrorField() throws Exception {
        var res = send("POST", "/todos", "{title:");
        assertEquals(400, res.statusCode());
        assertTrue(mapper.readTree(res.body()).has("error"), "본문에 error 필드가 있어야 합니다");
    }

    @Test
    void create_blankTitle_is400() throws Exception {
        assertEquals(400, send("POST", "/todos", "{\"title\":\"  \"}").statusCode());
        assertEquals(400, send("POST", "/todos", "{}").statusCode());
    }

    @Test
    void get_missing_is404WithErrorField() throws Exception {
        var res = send("GET", "/todos/999", null);
        assertEquals(404, res.statusCode());
        assertTrue(mapper.readTree(res.body()).has("error"));
    }

    @Test
    void delete_is204_thenGone() throws Exception {
        long id = mapper.readTree(send("POST", "/todos", "{\"title\":\"지울 것\"}").body()).get("id").asLong();
        var del = send("DELETE", "/todos/" + id, null);
        assertEquals(204, del.statusCode());
        assertEquals("", del.body());
        assertEquals(404, send("GET", "/todos/" + id, null).statusCode());
        assertEquals(204, send("DELETE", "/todos/" + id, null).statusCode());
    }

    @Test
    void unsupportedMethod_is405() throws Exception {
        assertEquals(405, send("PUT", "/todos", "{}").statusCode());
    }

    @Test
    void unknownPath_is404() throws Exception {
        assertEquals(404, send("GET", "/nothing", null).statusCode());
    }
}
