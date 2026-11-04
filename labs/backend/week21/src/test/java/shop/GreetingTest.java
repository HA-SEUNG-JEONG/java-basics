package shop;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class GreetingTest {
    @Autowired MockMvc mvc;
    @Autowired ApplicationContext ctx;
    @Autowired VisitCounter counter;

    @BeforeEach
    void reset() {
        counter.reset();
    }

    String body(MvcResult r) throws Exception {
        return r.getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    @Test
    void bothGreetersAreBeans() {
        assertEquals(2, ctx.getBeansOfType(Greeter.class).size(), "Greeter 구현체 2개가 모두 빈이어야 합니다");
    }

    @Test
    void defaultGreeterIsKorean() {
        assertInstanceOf(KoreanGreeter.class, ctx.getBean(Greeter.class),
                "타입만으로 Greeter 를 달라고 했을 때 KoreanGreeter 가 와야 합니다 (@Primary)");
    }

    @Test
    void greet_usesKoreanByDefault_andTheDefaultNameWhenMissing() throws Exception {
        assertEquals("안녕, 하승!", body(mvc.perform(get("/greet").param("name", "하승")).andReturn()));
        assertEquals("안녕, 손님!", body(mvc.perform(get("/greet")).andReturn()));
    }

    @Test
    void greetEnglish_usesEnglishGreeter() throws Exception {
        assertEquals("Hello, Ha!", body(mvc.perform(get("/greet/en").param("name", "Ha")).andReturn()));
    }

    @Test
    void counterIsASingletonSharedWithTheController() throws Exception {
        mvc.perform(get("/greet").param("name", "a"));
        mvc.perform(get("/greet/en").param("name", "b"));
        mvc.perform(get("/greet").param("name", "c"));
        assertEquals(3, counter.current(), "컨트롤러가 쓰는 VisitCounter 와 컨텍스트의 VisitCounter 는 같은 객체여야 합니다");
        assertSame(ctx.getBean(VisitCounter.class), ctx.getBean(VisitCounter.class));
    }

    @Test
    void visits_returnsJson() throws Exception {
        mvc.perform(get("/greet").param("name", "a"));
        mvc.perform(get("/greet").param("name", "b"));
        MvcResult res = mvc.perform(get("/visits")).andReturn();
        assertEquals(200, res.getResponse().getStatus());
        assertTrue(res.getResponse().getContentType().startsWith("application/json"));
        assertEquals("{\"count\":2}", body(res));
    }
}
