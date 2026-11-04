package library;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

/** 세션 로그인, CORS preflight, OpenAPI 스펙. 쿠키 속성은 실제 서버(톰캣)를 띄워 확인한다. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class SecurityAndDocsTest {
    static final String LOGIN = "{\"username\":\"demo\",\"password\":\"demo1234\"}";

    @Autowired MockMvc mvc;
    @LocalServerPort int port;

    @Test
    void writeWithoutLogin_401WithMessage_readIsPublic() throws Exception {
        mvc.perform(post("/books").contentType(APPLICATION_JSON).content("{\"isbn\":\"1\",\"title\":\"a\",\"author\":\"b\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("로그인이 필요합니다"));
        mvc.perform(post("/loans/1/return")).andExpect(status().isUnauthorized());
        mvc.perform(get("/auth/me")).andExpect(status().isUnauthorized());
        mvc.perform(get("/books")).andExpect(status().isOk());
    }

    @Test
    void wrongPassword_401() throws Exception {
        mvc.perform(post("/auth/login").contentType(APPLICATION_JSON).content("{\"username\":\"demo\",\"password\":\"nope\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("아이디 또는 비밀번호가 올바르지 않습니다"));
    }

    @Test
    void login_me_write_logout() throws Exception {
        MockHttpSession session = (MockHttpSession) mvc.perform(post("/auth/login").contentType(APPLICATION_JSON).content(LOGIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("demo"))
                .andReturn().getRequest().getSession();

        mvc.perform(get("/auth/me").session(session)).andExpect(status().isOk()).andExpect(jsonPath("$.username").value("demo"));
        mvc.perform(post("/members").session(session).contentType(APPLICATION_JSON).content("{\"name\":\"하승\"}"))
                .andExpect(status().isCreated());

        mvc.perform(post("/auth/logout").session(session)).andExpect(status().isNoContent());
        assertTrue(session.isInvalid(), "로그아웃하면 세션이 무효화된다");
        mvc.perform(get("/auth/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void realServer_sessionCookieIsHttpOnlyAndLax() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> login = client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(LOGIN)).build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(200, login.statusCode());
        String setCookie = login.headers().firstValue("Set-Cookie").orElseThrow();
        assertTrue(setCookie.startsWith("JSESSIONID="), setCookie);
        assertTrue(setCookie.contains("HttpOnly"), setCookie);
        assertTrue(setCookie.contains("SameSite=Lax"), setCookie);

        String cookie = setCookie.split(";", 2)[0];
        HttpResponse<String> me = client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/auth/me"))
                .header("Cookie", cookie).GET().build(), HttpResponse.BodyHandlers.ofString());
        assertEquals(200, me.statusCode());
        assertTrue(me.body().contains("\"username\":\"demo\""), me.body());
    }

    @Test
    void corsPreflight_allowsFrontendOriginWithCredentials() throws Exception {
        mvc.perform(options("/loans")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"))
                .andExpect(header().string("Access-Control-Allow-Methods", containsString("POST")));
    }

    @Test
    void corsPreflight_rejectsOtherOrigin() throws Exception {
        mvc.perform(options("/loans")
                        .header("Origin", "http://evil.example")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    @Test
    void openApiSpec_documentsErrorCodes() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.paths['/loans'].post.responses", allOf(
                        hasKey("201"), hasKey("400"), hasKey("401"), hasKey("404"), hasKey("409"))))
                .andExpect(jsonPath("$.paths['/books'].post.responses", allOf(hasKey("400"), hasKey("409"))))
                .andExpect(jsonPath("$.paths['/books/{id}'].get.responses", hasKey("404")))
                .andExpect(jsonPath("$.components.schemas.ApiError.required", contains("message")));
    }
}
