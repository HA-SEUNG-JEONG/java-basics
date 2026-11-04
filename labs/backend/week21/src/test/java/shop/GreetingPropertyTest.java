package shop;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

/** 설정 값만 바꿨는데 코드 수정 없이 동작이 바뀌어야 합니다. */
@SpringBootTest(properties = "app.greeting=반갑습니다")
@AutoConfigureMockMvc
class GreetingPropertyTest {
    @Autowired MockMvc mvc;

    @Test
    void greetingComesFromProperty() throws Exception {
        String body = mvc.perform(get("/greet").param("name", "하승")).andReturn()
                .getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertEquals("반갑습니다, 하승!", body);
    }
}
