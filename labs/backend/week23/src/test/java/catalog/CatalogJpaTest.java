package catalog;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.List;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
class CatalogJpaTest {
    @Autowired TestEntityManager em;
    @Autowired AuthorRepository authors;
    @Autowired BookRepository books;
    @Autowired EntityManagerFactory emf;

    Statistics stats;

    @BeforeEach
    void seed() {
        Author joshua = new Author("Joshua");
        joshua.addBook(new Book("Effective Java", 40000));
        joshua.addBook(new Book("Java Puzzlers", 30000));
        Author robert = new Author("Robert");
        robert.addBook(new Book("Clean Code", 33000));
        robert.addBook(new Book("Clean Architecture", 28000));
        robert.addBook(new Book("The Clean Coder", 25000));
        Author kent = new Author("Kent");
        kent.addBook(new Book("TDD", 20000));
        authors.saveAll(List.of(joshua, robert, kent));   // cascade: 책도 함께 저장
        em.flush();
        em.clear();                                       // 이후 조회는 1차 캐시가 아니라 DB 에서
        stats = emf.unwrap(SessionFactory.class).getStatistics();
        stats.clear();
    }

    // ---- 매핑 ----

    @Test
    void cascadeSavesBooksAndAssignsIds() {
        assertEquals(6, books.count(), "저자를 저장하면 책 6권도 저장되어야 합니다 (@Id, 엔티티 표시 확인)");
        assertTrue(books.findAll().stream().allMatch(b -> b.getId() != null));
    }

    @Test
    void bookToAuthorIsLazy() {
        Book b = books.findAll().get(0);
        assertFalse(Persistence.getPersistenceUtil().isLoaded(b, "author"),
                "책을 읽을 때 저자는 아직 읽지 않아야 합니다 (fetch = LAZY)");
        assertNotNull(b.getAuthor().getName());
        assertTrue(Persistence.getPersistenceUtil().isLoaded(b, "author"));
    }

    // ---- 이름으로 만드는 쿼리 ----

    @Test
    void findByAuthorName() {
        assertEquals(3, books.findByAuthorName("Robert").size());
        assertEquals(0, books.findByAuthorName("Nobody").size());
    }

    @Test
    void findByTitleContainingIgnoreCaseOrderByTitleAsc() {
        List<String> titles = books.findByTitleContainingIgnoreCaseOrderByTitleAsc("clean").stream()
                .map(Book::getTitle).toList();
        assertEquals(List.of("Clean Architecture", "Clean Code", "The Clean Coder"), titles);
    }

    @Test
    void findByPriceBetween_includesBothEnds() {
        List<String> titles = books.findByPriceBetween(28000, 33000).stream()
                .map(Book::getTitle).sorted().toList();
        assertEquals(List.of("Clean Architecture", "Clean Code", "Java Puzzlers"), titles);
    }

    @Test
    void countByAuthorName() {
        assertEquals(2, books.countByAuthorName("Joshua"));
        assertEquals(0, books.countByAuthorName("Nobody"));
    }

    @Test
    void existsByTitle() {
        assertTrue(books.existsByTitle("TDD"));
        assertFalse(books.existsByTitle("없는 책"));
    }

    // ---- 직접 쓴 JPQL ----

    @Test
    void findPopularAuthors() {
        assertEquals(List.of("Joshua", "Robert"),
                authors.findPopularAuthors(2).stream().map(Author::getName).toList());
        assertEquals(List.of("Robert"),
                authors.findPopularAuthors(3).stream().map(Author::getName).toList());
    }

    // ---- N+1 ----

    @Test
    void findAll_thenTouchingBooks_isNPlusOne() {
        List<Author> all = authors.findAll();          // 쿼리 1번
        all.forEach(a -> a.getBooks().size());          // 저자마다 쿼리 1번씩 (3명)
        assertEquals(1 + 3, stats.getPrepareStatementCount(),
                "저자 3명이면 쿼리 4번이 나가는 것이 N+1 입니다");
    }

    @Test
    void findAllWithBooks_isOneQuery() {
        List<Author> all = authors.findAllWithBooks();
        all.forEach(a -> a.getBooks().size());
        assertEquals(3, all.size(), "저자는 3명이어야 합니다 (책 수만큼 중복되면 안 됨)");
        assertEquals(1, stats.getPrepareStatementCount(), "저자와 책이 쿼리 한 번에 와야 합니다");
    }
}
