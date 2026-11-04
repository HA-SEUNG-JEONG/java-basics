import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/** 18주차 모범 풀이. 15분 넘게 막혔을 때만 보고, 덮은 뒤 직접 다시 치세요. */
public class TodoServer {
    private final ObjectMapper mapper = new ObjectMapper();
    private final List<Todo> todos = new CopyOnWriteArrayList<>();
    private final AtomicLong seq = new AtomicLong();
    private HttpServer server;

    public int start(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        server.createContext("/", this::handle);
        server.start();
        return server.getAddress().getPort();
    }

    public void stop() {
        if (server != null) server.stop(0);
    }

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

    private void send(HttpExchange ex, int status, Object body) throws IOException {
        byte[] bytes = mapper.writeValueAsBytes(body);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = ex.getResponseBody()) {
            out.write(bytes);
        }
    }

    private void list(HttpExchange ex) throws IOException {
        send(ex, 200, todos);
    }

    private void create(HttpExchange ex) throws IOException {
        NewTodo in;
        try {
            in = mapper.readValue(ex.getRequestBody(), NewTodo.class);
        } catch (IOException e) {
            send(ex, 400, Map.of("error", "JSON 을 읽을 수 없습니다"));
            return;
        }
        if (in == null || in.title() == null || in.title().isBlank()) {
            send(ex, 400, Map.of("error", "title 은 비어 있을 수 없습니다"));
            return;
        }
        Todo todo = new Todo(seq.incrementAndGet(), in.title(), false);
        todos.add(todo);
        ex.getResponseHeaders().set("Location", "/todos/" + todo.id());
        send(ex, 201, todo);
    }

    private void get(HttpExchange ex, long id) throws IOException {
        Optional<Todo> found = find(id);
        if (found.isPresent()) send(ex, 200, found.get());
        else send(ex, 404, Map.of("error", "없는 할 일: " + id));
    }

    private void delete(HttpExchange ex, long id) throws IOException {
        todos.removeIf(t -> t.id() == id);
        ex.sendResponseHeaders(204, -1);
    }

    private Optional<Todo> find(long id) {
        return todos.stream().filter(t -> t.id() == id).findFirst();
    }
}
