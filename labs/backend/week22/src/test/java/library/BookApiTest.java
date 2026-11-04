package library;

import static org.hamcrest.Matchers.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class BookApiTest {
    static final String CLEAN = "{\"title\":\"클린 코드\",\"author\":\"로버트 마틴\"}";

    @Autowired MockMvc mvc;
    @Autowired BookService service;

    @BeforeEach
    void reset() {
        service.clear();
    }

    @Test
    void list_isEmptyArrayAtFirst() throws Exception {
        mvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void create_returns201WithLocationAndBody() throws Exception {
        mvc.perform(post("/books").contentType(APPLICATION_JSON).content(CLEAN))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/books/1")))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("클린 코드"))
                .andExpect(jsonPath("$.author").value("로버트 마틴"));
    }

    @Test
    void list_showsCreatedBooksInOrder() throws Exception {
        service.create(new BookRequest("a", "x"));
        service.create(new BookRequest("b", "y"));
        mvc.perform(get("/books"))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title").value("a"))
                .andExpect(jsonPath("$[1].title").value("b"));
    }

    @Test
    void get_returnsTheBook() throws Exception {
        service.create(new BookRequest("a", "x"));
        mvc.perform(get("/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("a"));
    }

    @Test
    void get_missingBook_is404WithMessage() throws Exception {
        mvc.perform(get("/books/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(containsString("99")));
    }

    @Test
    void get_nonNumericId_is400() throws Exception {
        mvc.perform(get("/books/abc")).andExpect(status().isBadRequest());
    }

    @Test
    void create_blankTitle_is400_andNothingIsSaved() throws Exception {
        mvc.perform(post("/books").contentType(APPLICATION_JSON).content("{\"title\":\"  \",\"author\":\"x\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("title")));
        org.junit.jupiter.api.Assertions.assertEquals(0, service.findAll().size());
    }

    @Test
    void create_missingAuthor_is400() throws Exception {
        mvc.perform(post("/books").contentType(APPLICATION_JSON).content("{\"title\":\"a\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("author")));
    }

    @Test
    void update_replacesTheBook() throws Exception {
        service.create(new BookRequest("a", "x"));
        mvc.perform(put("/books/1").contentType(APPLICATION_JSON).content(CLEAN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("클린 코드"));
        mvc.perform(get("/books/1")).andExpect(jsonPath("$.author").value("로버트 마틴"));
    }

    @Test
    void update_missingBook_is404() throws Exception {
        mvc.perform(put("/books/5").contentType(APPLICATION_JSON).content(CLEAN))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("5")));
    }

    @Test
    void update_invalidBody_is400() throws Exception {
        service.create(new BookRequest("a", "x"));
        mvc.perform(put("/books/1").contentType(APPLICATION_JSON).content("{\"title\":\"\",\"author\":\"x\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delete_is204_thenGetIs404() throws Exception {
        service.create(new BookRequest("a", "x"));
        mvc.perform(delete("/books/1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        mvc.perform(get("/books/1")).andExpect(status().isNotFound());
    }

    @Test
    void delete_missingBook_is404() throws Exception {
        mvc.perform(delete("/books/7"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("7")));
    }
}
