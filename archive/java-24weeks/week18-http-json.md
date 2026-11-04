# 18주차. HTTP와 JSON

**예상 시간:** 10시간 | **목표:** HTTP 요청과 응답이 실제로 어떤 모양인지 손으로 확인하고, Jackson으로 JSON을 읽고 쓰며, 작은 REST 서버와 클라이언트를 직접 만든다. 다음 주부터 Spring이 해 줄 일을 먼저 직접 합니다.

> 명령은 모두 `../../labs/backend/` 에서 실행합니다. 처음 실행 시 Gradle 과 라이브러리(Jackson)를 내려받습니다.

## 🖐 먼저 손으로 (읽기 전에 하세요)

> 이 주차의 핵심은 아래 단계를 **직접 타이핑하면서** 깨닫는 것입니다. 아래쪽 설명 문서는 먼저 읽지 않아도 됩니다. 막혔을 때 사전처럼 펼쳐 보세요.

### 1단계. 예측하고 → 치고 → 비교하기 (30분)

1. 아래 코드를 **실행하기 전에** 4줄의 출력을 노트에 먼저 적습니다. (`/nope` 의 응답 본문은 자바 내장 서버의 문구라 맞히기 어렵습니다. **상태 코드와 Content-Type** 을 맞히는 데 집중하세요.)
2. 한 글자씩 타이핑하고 `java Predict18.java` 로 실행합니다. 외부 라이브러리 없이 JDK 만 씁니다.
3. 틀린 줄에 ★를 치고 이유를 한 문장으로 적습니다.

```java
// File: Predict18.java
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class Predict18 {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/hello", ex -> {
            byte[] body = ("{\"method\":\"" + ex.getRequestMethod() + "\"}").getBytes(StandardCharsets.UTF_8);
            ex.getResponseHeaders().add("Content-Type", "application/json");
            ex.sendResponseHeaders(ex.getRequestMethod().equals("POST") ? 201 : 200, body.length);
            ex.getResponseBody().write(body);
            ex.close();
        });
        server.start();
        int port = server.getAddress().getPort();

        HttpClient client = HttpClient.newHttpClient();
        for (String method : new String[] {"GET", "POST"}) {
            HttpRequest req = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/hello"))
                    .method(method, HttpRequest.BodyPublishers.ofString(method.equals("POST") ? "{}" : ""))
                    .build();
            HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
            System.out.println(res.statusCode() + " " + res.body());
        }
        HttpResponse<String> missing = client.send(
                HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/nope")).build(),
                HttpResponse.BodyHandlers.ofString());
        System.out.println(missing.statusCode() + " [" + missing.body() + "]");
        System.out.println(missing.headers().firstValue("content-type").orElse("(없음)"));
        server.stop(0);
    }
}
```

<details>
<summary>실행 결과 (예측을 적은 뒤에 여세요)</summary>

```text
200 {"method":"GET"}
201 {"method":"POST"}
404 [<h1>404 Not Found</h1>No context found for request]
text/html
```

등록하지 않은 경로(`/nope`)는 코드를 한 줄도 안 썼는데 서버가 404 를 돌려줍니다. 상태 코드는 "요청이 어떻게 됐는가"를 알려 주는 약속이고, 본문과 `Content-Type` 은 서버가 정한 것입니다.

</details>

### 2단계. 일부러 망가뜨리기 (30분)

JSON 은 문자열이라 모양이 틀려도 컴파일은 됩니다. 실행해야 알 수 있습니다. 아래 입력을 `NewTodo` (필드가 `title` 하나인 record)로 읽어 보고, 예외 이름과 첫 줄을 노트에 적으세요. 먼저 어떻게 될지 예상하세요. (`ObjectMapper#readValue(String, Class)`)

| 입력 JSON | 어떻게 될까요? |
|---|---|
| `{"title":"a","extra":1}` | `UnrecognizedPropertyException: Unrecognized field "extra" ...` 모르는 필드는 기본 설정에서 **오류** |
| `[]` | `MismatchedInputException: Cannot deserialize value of type ... from Array value` 객체 자리에 배열 |
| `{'title':'a'}` | `JsonParseException: Unexpected character (''' (code 39)): was expecting double-quote ...` JSON 은 작은따옴표 불가 |
| `{"title":"a"` (닫는 괄호 없음) | `JsonEOFException: Unexpected end-of-input: expected close marker for Object` |
| 서버에서 `getResponseBody().write(...)` 를 `sendResponseHeaders` 보다 먼저 호출 (3단계 `send` 를 구현할 때 일부러 해 보세요) | `IOException: response headers not sent yet` |
| `{}` | **오류가 아니다.** `NewTodo[title=null]`. 빠진 필드는 `null` — 그래서 서버가 `title` 을 직접 검사해야 한다 |

마지막 줄이 중요합니다. 파싱이 성공했다는 것은 "내용이 올바르다"는 뜻이 아닙니다. 22주차의 `@Valid` 가 이 검사를 대신합니다.

### 3단계. 빈칸에서 만들기 — 자동 채점 (3~5시간)

`../../labs/backend/week18/src/main/java/` 의 두 파일을 채웁니다. 라우팅(`handle`)과 `Todo`/`NewTodo`/`ApiException` 은 주어진 코드입니다.

1. `TodoServer.java` — `send`, `list`, `create`, `get`, `delete` 5개
2. `TodoClient.java` — `list`, `create`, `find` 3개

```bash
cd ../../labs/backend
./gradlew :week18:test
```

- 처음에는 18개가 모두 실패합니다. 정상입니다. `TodoServerTest` → `TodoClientTest` → `TodoEndToEndTest` 순서로 초록이 늘어납니다. 마지막 테스트는 **내가 만든 서버를 내가 만든 클라이언트로 호출**합니다.
- 정답은 `../../labs/solutions/week18/` 에 있습니다. **15분 넘게 막혔을 때만** 열고, 본 뒤에는 덮고 **직접 다시 치세요.**

---

## 1. HTTP 요청과 응답

| 요청의 구성 | 예 |
|---|---|
| 메서드 + 경로 | `POST /todos` |
| 헤더 | `Content-Type: application/json` |
| 본문 | `{"title":"우유 사기"}` |

| 응답의 구성 | 예 |
|---|---|
| 상태 코드 | `201 Created` |
| 헤더 | `Location: /todos/1`, `Content-Type: application/json` |
| 본문 | `{"id":1,"title":"우유 사기","done":false}` |

**메서드의 의미:** `GET` 조회, `POST` 생성, `PUT` 통째로 교체, `DELETE` 삭제. **자주 쓰는 상태 코드**

| 코드 | 의미 | 쓰는 때 |
|---|---|---|
| 200 | OK | 조회·수정 성공 |
| 201 | Created | 생성 성공 (+ `Location` 헤더로 새 주소) |
| 204 | No Content | 성공했지만 돌려줄 본문 없음 (삭제) |
| 400 | Bad Request | 클라이언트가 보낸 요청이 잘못됨 (JSON 깨짐, 필수 값 누락) |
| 404 | Not Found | 그런 자원이 없음 |
| 405 | Method Not Allowed | 그 경로에서 쓸 수 없는 메서드 |
| 500 | Internal Server Error | 서버 쪽 버그 |

4xx 는 "당신이 잘못 보냈다", 5xx 는 "내가 고장 났다"입니다. 이 구분이 API 설계의 핵심입니다.

> **JS와 비교:** 브라우저의 `fetch('/todos', {method:'POST', body: JSON.stringify(...)})` 가 보내는 것이 바로 이 요청입니다. 이번 주에는 `fetch` 쪽(클라이언트)과 Express 쪽(서버)을 둘 다 자바로 만듭니다.

## 2. JSON 과 Jackson

JSON 은 문자열로 된 데이터 교환 형식입니다. 자바 객체 ↔ JSON 변환은 Jackson 의 `ObjectMapper` 가 합니다. (2단계의 예외들이 모두 여기서 나옵니다.)

```java
ObjectMapper mapper = new ObjectMapper();

mapper.writeValueAsString(new Todo(1, "우유 사기", true));
// {"id":1,"title":"우유 사기","done":true}

Todo t = mapper.readValue("{\"id\":2,\"title\":\"b\"}", Todo.class);
// Todo[id=2, title=b, done=false]    빠진 boolean 은 false, 빠진 객체는 null
```

- `record` 는 그대로 읽고 쓸 수 있습니다. 필드 이름이 JSON 키가 됩니다.
- 제네릭 컬렉션은 타입 정보가 지워지기 때문에 `TypeReference` 로 알려 줍니다. 안 알려 주면 `Todo` 가 아니라 `LinkedHashMap` 이 들어 있는 리스트를 받습니다 (12주차 제네릭 소거의 실제 사례).

  ```java
  List<Todo> list = mapper.readValue(json, new TypeReference<List<Todo>>() {});
  ```

## 3. JDK 만으로 만드는 서버와 클라이언트

`com.sun.net.httpserver.HttpServer` 는 JDK 에 들어 있는 작은 서버이고, `java.net.http.HttpClient` 는 Java 11 부터의 표준 클라이언트입니다. 실습 코드의 구조는 이렇습니다.

- 서버: 요청이 오면 `handle` 이 메서드와 경로로 갈라서 `list`/`create`/`get`/`delete` 를 호출한다. **이 "갈라서 보내는" 일을 24주차의 Spring MVC 가 어노테이션으로 해 줍니다.** (`@GetMapping("/todos/{id}")`)
- 응답을 보낼 때: `sendResponseHeaders(상태, 본문길이)` **다음에** 본문을 씁니다. 순서가 바뀌면 서버에서 `IOException: response headers not sent yet` 가 나고, 클라이언트는 빈 응답을 받습니다. 본문이 없으면 길이에 `-1`.
- 길이는 글자 수가 아니라 `getBytes(UTF_8)` 의 **바이트 수**여야 합니다. `"안녕".length()` 는 2 이지만 UTF-8 로는 6바이트라서, 글자 수로 보내면 서버에서 `IOException: too many bytes to write to stream` 이 납니다.

## 연습문제

1. `TodoServer` 에 `Main` 클래스를 만들어 8080 포트로 서버를 띄우고, 터미널에서 `curl -i localhost:8080/todos` 와 `curl -i -X POST -d '{"title":"a"}' localhost:8080/todos` 를 실행해 응답 전체(상태 줄·헤더·본문)를 눈으로 확인하세요.
2. 응답 `Content-Type` 에서 `; charset=utf-8` 을 지우고 한글 제목을 생성·조회하면 어떻게 보이는지 `curl` 로 확인하세요. (확인 후 원복)
3. `PUT /todos/{id}` (제목 교체)를 서버와 클라이언트에 추가하세요. 없는 id 는 404.
4. `TodoClient.create` 가 서버의 400 응답을 `ApiException` 으로 바꿔 던질 때, 예외 메시지에 응답 본문이 들어가는지 확인하세요.

## 퀴즈

1. `POST` 로 자원을 만들었을 때 `200` 대신 `201` 과 `Location` 을 쓰는 이유는?
2. JSON 에 필드가 하나 빠져 있으면 Jackson 은 어떻게 하나요? 그래서 필요한 것은?
3. 400 과 500 의 차이를 책임 소재로 설명하세요.
4. `List<Todo>` 로 읽을 때 `TypeReference` 가 필요한 이유는?

<details>
<summary>정답</summary>

1. "새로 만들어졌다"는 의미를 정확히 전하고, 새 자원의 주소를 알려 주기 위해
2. 오류 없이 `null`(기본형은 기본값)로 둔다. 서버가 직접 검증해야 한다 (Spring 에서는 `@Valid`)
3. 400 은 클라이언트가 잘못 보낸 것(고쳐서 다시 보내면 됨), 500 은 서버 코드의 문제
4. 제네릭 타입 인자가 컴파일 후 지워져서 런타임에 `Todo` 인지 알 수 없다. 알려 주지 않으면 `Map` 이 담긴 리스트가 된다

</details>

## 체크리스트

- [ ] 요청과 응답을 구성 요소별로 말할 수 있다 (메서드·경로·헤더·본문·상태 코드)
- [ ] 4xx 와 5xx 를 구분해서 상태 코드를 고를 수 있다
- [ ] 깨진 JSON 이 만드는 예외 4가지를 직접 봤다
- [ ] `TodoServer`/`TodoClient` 테스트 18개를 모두 통과했다
