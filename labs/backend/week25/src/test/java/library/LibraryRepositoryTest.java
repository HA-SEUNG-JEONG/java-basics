package library;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.persistence.EntityManagerFactory;
import java.time.Instant;
import java.util.List;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
class LibraryRepositoryTest {
    @Autowired TestEntityManager em;
    @Autowired BookRepository books;
    @Autowired LoanRepository loans;
    @Autowired EntityManagerFactory emf;

    Member haseung;
    Book effective;
    Book clean;
    Book tdd;

    @BeforeEach
    void seed() {
        effective = em.persist(new Book("111", "이펙티브 자바", "조슈아 블로크"));
        clean = em.persist(new Book("222", "Clean Code", "로버트 마틴"));
        tdd = em.persist(new Book("333", "TDD", "켄트 벡"));
        haseung = em.persist(new Member("하승"));
        em.persist(new Loan(effective, haseung, Instant.parse("2026-01-01T00:00:00Z")));
        em.persist(new Loan(clean, haseung, Instant.parse("2026-01-02T00:00:00Z")));
        Loan returned = em.persist(new Loan(tdd, haseung, Instant.parse("2026-01-03T00:00:00Z")));
        returned.markReturned(Instant.parse("2026-01-04T00:00:00Z"));
        em.flush();
        em.clear();
    }

    @Test
    void findByTitleContainingIgnoreCase() {
        List<String> titles = books.findByTitleContainingIgnoreCaseOrderByIdAsc("CLEAN").stream()
                .map(Book::getTitle).toList();
        assertEquals(List.of("Clean Code"), titles);
        assertEquals(3, books.findAllByOrderByIdAsc().size());
    }

    @Test
    void existsByIsbn() {
        assertTrue(books.existsByIsbn("111"));
        assertFalse(books.existsByIsbn("999"));
    }

    @Test
    void activeLoanQueries_ignoreReturnedLoans() {
        assertEquals(2, loans.countByMemberIdAndReturnedAtIsNull(haseung.getId()));
        assertTrue(loans.existsByBookIdAndReturnedAtIsNull(effective.getId()));
        assertFalse(loans.existsByBookIdAndReturnedAtIsNull(tdd.getId()), "반납된 책은 대출 중이 아니다");
    }

    @Test
    void activeLoans_fetchBookAndMemberInOneQuery() {
        Statistics stats = emf.unwrap(SessionFactory.class).getStatistics();
        stats.clear();
        List<Loan> active = loans.findByMemberIdAndReturnedAtIsNullOrderByIdAsc(haseung.getId());
        active.forEach(l -> l.getBook().getTitle().length());
        active.forEach(l -> l.getMember().getName().length());
        assertEquals(List.of("이펙티브 자바", "Clean Code"), active.stream().map(l -> l.getBook().getTitle()).toList());
        assertEquals(1, stats.getPrepareStatementCount(), "대출·책·회원이 쿼리 한 번에 와야 한다");
    }
}
