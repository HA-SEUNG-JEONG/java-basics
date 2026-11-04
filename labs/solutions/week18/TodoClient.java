import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** 18주차 모범 풀이. */
public class TodoClient {
    private final HttpClient http;
    private final String baseUrl;
    private final ObjectMapper mapper = new ObjectMapper();

    public TodoClient(HttpClient http, String baseUrl) {
        this.http = http;
        this.baseUrl = baseUrl;
    }

    public List<Todo> list() throws IOException, InterruptedException {
        HttpResponse<String> res = http.send(
                HttpRequest.newBuilder(URI.create(baseUrl + "/todos")).GET().build(),
                HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) throw new ApiException(res.statusCode(), res.body());
        return mapper.readValue(res.body(), new TypeReference<List<Todo>>() {});
    }

    public Todo create(String title) throws IOException, InterruptedException {
        String json = mapper.writeValueAsString(Map.of("title", title));
        HttpResponse<String> res = http.send(
                HttpRequest.newBuilder(URI.create(baseUrl + "/todos"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build(),
                HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 201) throw new ApiException(res.statusCode(), res.body());
        return mapper.readValue(res.body(), Todo.class);
    }

    public Optional<Todo> find(long id) throws IOException, InterruptedException {
        HttpResponse<String> res = http.send(
                HttpRequest.newBuilder(URI.create(baseUrl + "/todos/" + id)).GET().build(),
                HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() == 404) return Optional.empty();
        if (res.statusCode() != 200) throw new ApiException(res.statusCode(), res.body());
        return Optional.of(mapper.readValue(res.body(), Todo.class));
    }
}
