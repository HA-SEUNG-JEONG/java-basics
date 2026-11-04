package library;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.jayway.jsonpath.JsonPath;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

/** 엔드포인트·상태 코드·오류 본문. 로그인 자체는 SecurityAndDocsTest 가 본다. */
@SpringBootTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@AutoConfigureMockMvc
@Transactional
@WithMockUser
class LibraryApiTest {
    @Autowired MockMvc mvc;
    @Autowired EntityManager em;
    @Autowired EntityManagerFactory emf;

    ResultActions postJson(String url, String json) throws Exception {
        return mvc.perform(post(url).contentType(APPLICATION_JSON).content(json));
    }

    static long id(ResultActions result) throws Exception {
        String body = result.andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    long book(String isbn, String title) throws Exception {
        return id(postJson("/books", "{\"isbn\":\"" + isbn + "\",\"title\":\"" + title + "\",\"author\":\"저자\"}")
                .andExpect(status().isCreated()));
    }

    long member(String name) throws Exception {
        return id(postJson("/members", "{\"name\":\"" + name + "\"}").andExpect(status().isCreated()));
    }

    ResultActions lend(long memberId, long bookId) throws Exception {
        return postJson("/loans", "{\"memberId\":" + memberId + ",\"bookId\":" + bookId + "}");
    }

    // ---- 책 ----

    @Test
    void createBook_returns201WithLocationAndBody() throws Exception {
        postJson("/books", "{\"isbn\":\"111\",\"title\":\"이펙티브 자바\",\"author\":\"조슈아 블로크\"}")
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern(".*/books/\\d+")))
                .andExpect(jsonPath("$.isbn").value("111"))
                .andExpect(jsonPath("$.title").value("이펙티브 자바"));
    }

    @Test
    void createBook_duplicateIsbn_409() throws Exception {
        book("111", "이펙티브 자바");
        postJson("/books", "{\"isbn\":\"111\",\"title\":\"다른 책\",\"author\":\"누군가\"}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("111")));
    }

    @Test
    void createBook_blankField_400WithFieldName() throws Exception {
        postJson("/books", "{\"isbn\":\"\",\"title\":\"x\",\"author\":\"y\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", startsWith("isbn: ")));
    }

    @Test
    void malformedJson_400() throws Exception {
        postJson("/books", "{\"isbn\":")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isString());
    }

    @Test
    void getBook_404AndBadId400() throws Exception {
        long id = book("111", "이펙티브 자바");
        mvc.perform(get("/books/" + id)).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(id));
        mvc.perform(get("/books/999999")).andExpect(status().isNotFound()).andExpect(jsonPath("$.message").isString());
        mvc.perform(get("/books/abc")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").isString());
    }

    @Test
    void searchBooks_byKeywordIgnoringCase() throws Exception {
        book("1", "Clean Code");
        book("2", "The Clean Coder");
        book("3", "TDD");
        mvc.perform(get("/books").param("keyword", "clean"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].title", contains("Clean Code", "The Clean Coder")));
        mvc.perform(get("/books")).andExpect(jsonPath("$", hasSize(3)));
    }

    // ---- 회원 ----

    @Test
    void createMember_201AndBlank400() throws Exception {
        postJson("/members", "{\"name\":\"하승\"}")
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern(".*/members/\\d+")))
                .andExpect(jsonPath("$.name").value("하승"));
        postJson("/members", "{\"name\":\" \"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", startsWith("name: ")));
    }

    // ---- 대출·반납 ----

    @Test
    void lend_201WithBookAndMember() throws Exception {
        long b = book("111", "이펙티브 자바");
        long m = member("하승");
        lend(m, b).andExpect(status().isCreated())
                .andExpect(jsonPath("$.book.id").value(b))
                .andExpect(jsonPath("$.member.name").value("하승"))
                .andExpect(jsonPath("$.loanedAt").isString())
                .andExpect(jsonPath("$.returnedAt").value(nullValue()));
    }

    @Test
    void lend_missingFieldOrUnknown_400And404() throws Exception {
        long m = member("하승");
        postJson("/loans", "{\"memberId\":" + m + "}").andExpect(status().isBadRequest());
        lend(m, 999999).andExpect(status().isNotFound()).andExpect(jsonPath("$.message", containsString("책")));
        lend(999999, book("111", "x")).andExpect(status().isNotFound()).andExpect(jsonPath("$.message", containsString("회원")));
    }

    @Test
    void lend_alreadyOnLoanOrOverLimit_409() throws Exception {
        long haseung = member("하승");
        long minji = member("민지");
        long first = book("1", "a");
        lend(haseung, first).andExpect(status().isCreated());
        lend(minji, first).andExpect(status().isConflict()).andExpect(jsonPath("$.message", containsString("대출 중")));
        lend(haseung, book("2", "b")).andExpect(status().isCreated());
        lend(haseung, book("3", "c")).andExpect(status().isCreated());
        lend(haseung, book("4", "d")).andExpect(status().isConflict()).andExpect(jsonPath("$.message", containsString("3권")));
    }

    @Test
    void giveBack_200Then409_unknown404() throws Exception {
        long loan = id(lend(member("하승"), book("111", "x")));
        mvc.perform(post("/loans/" + loan + "/return"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.returnedAt").isString());
        mvc.perform(post("/loans/" + loan + "/return")).andExpect(status().isConflict());
        mvc.perform(post("/loans/999999/return")).andExpect(status().isNotFound());
    }

    @Test
    void memberLoans_onlyActive_unknown404() throws Exception {
        long m = member("하승");
        long returned = id(lend(m, book("1", "a")));
        lend(m, book("2", "b"));
        mvc.perform(post("/loans/" + returned + "/return"));
        mvc.perform(get("/members/" + m + "/loans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].book.title").value("b"));
        mvc.perform(get("/members/999999/loans")).andExpect(status().isNotFound());
    }

    /** 24주차 1단계 시나리오 13개를 위에서 아래로. */
    @Test
    void week24Scenario_13Steps() throws Exception {
        long b1 = id(postJson("/books", "{\"isbn\":\"111\",\"title\":\"이펙티브 자바\",\"author\":\"조슈아 블로크\"}")
                .andExpect(status().isCreated()).andExpect(header().exists("Location")));          // 1
        postJson("/books", "{\"isbn\":\"111\",\"title\":\"이펙티브 자바\",\"author\":\"조슈아 블로크\"}")
                .andExpect(status().isConflict());                                                   // 2
        postJson("/books", "{\"isbn\":\"\",\"title\":\"x\",\"author\":\"y\"}").andExpect(status().isBadRequest()); // 3
        long haseung = member("하승");                                                               // 4
        long minji = member("민지");                                                                 // 5
        long loan1 = id(lend(haseung, b1).andExpect(status().isCreated()));                         // 6
        lend(minji, b1).andExpect(status().isConflict());                                           // 7
        lend(haseung, 999999).andExpect(status().isNotFound());                                     // 8
        lend(haseung, book("112", "b")).andExpect(status().isCreated());
        lend(haseung, book("113", "c")).andExpect(status().isCreated());
        lend(haseung, book("114", "d")).andExpect(status().isConflict());                           // 9
        mvc.perform(post("/loans/" + loan1 + "/return"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.returnedAt").isString());         // 10
        mvc.perform(post("/loans/" + loan1 + "/return")).andExpect(status().isConflict());          // 11
        lend(minji, b1).andExpect(status().isCreated());                                            // 12
        mvc.perform(get("/members/" + haseung + "/loans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].book.title", contains("b", "c")));                       // 13
    }

    // ---- N+1 ----

    long countQueriesForMemberLoans(long memberId) throws Exception {
        em.flush();
        em.clear(); // 1차 캐시를 비워야 실제 쿼리 수가 보인다
        Statistics stats = emf.unwrap(SessionFactory.class).getStatistics();
        stats.clear();
        mvc.perform(get("/members/" + memberId + "/loans")).andExpect(status().isOk());
        return stats.getPrepareStatementCount();
    }

    @Test
    void memberLoans_queryCountDoesNotGrowWithLoans() throws Exception {
        Member m = new Member("하승");
        em.persist(m);
        Book a = new Book("q1", "a", "x");
        em.persist(a);
        em.persist(new Loan(a, m, Instant.now()));
        long withOne = countQueriesForMemberLoans(m.getId());

        Member same = em.find(Member.class, m.getId());
        for (String isbn : new String[] {"q2", "q3"}) {
            Book b = new Book(isbn, isbn, "x");
            em.persist(b);
            em.persist(new Loan(b, same, Instant.now()));
        }
        long withThree = countQueriesForMemberLoans(m.getId());

        assertEquals(withOne, withThree, "대출 1건과 3건의 쿼리 수가 같아야 한다");
        assertTrue(withThree <= 2, "회원 확인 1번 + 대출·책·회원 함께 1번이어야 한다: " + withThree);
    }
}
