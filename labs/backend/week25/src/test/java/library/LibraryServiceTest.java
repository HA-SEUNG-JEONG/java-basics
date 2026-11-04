package library;

import static org.junit.jupiter.api.Assertions.*;

import library.Dto.BookRequest;
import library.Dto.LoanResponse;
import library.Dto.MemberRequest;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/** Spring 없이: 가짜 리포지토리와 고정 시계를 new 로 넣어 규칙만 검사한다. */
class LibraryServiceTest {
    static final Instant NOW = Instant.parse("2026-10-01T09:00:00Z");

    FakeBooks books;
    FakeMembers members;
    FakeLoans loans;
    LibraryService service;

    @BeforeEach
    void setUp() {
        books = new FakeBooks();
        members = new FakeMembers();
        loans = new FakeLoans();
        service = new LibraryService(books, members, loans, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    long newBook(String isbn) {
        return service.createBook(new BookRequest(isbn, "책 " + isbn, "저자")).id();
    }

    long newMember(String name) {
        return service.createMember(new MemberRequest(name)).id();
    }

    @Test
    void createBook_duplicateIsbn_conflicts() {
        newBook("111");
        assertThrows(ConflictException.class, () -> newBook("111"));
        assertEquals(1, books.findAllByOrderByIdAsc().size());
    }

    @Test
    void lend_unknownMemberOrBook_notFound() {
        long book = newBook("111");
        long member = newMember("하승");
        assertThrows(NotFoundException.class, () -> service.lend(99, book));
        assertThrows(NotFoundException.class, () -> service.lend(member, 99));
        assertTrue(loans.all.isEmpty());
    }

    @Test
    void lend_bookAlreadyOnLoan_conflicts() {
        long book = newBook("111");
        service.lend(newMember("하승"), book);
        assertThrows(ConflictException.class, () -> service.lend(newMember("민지"), book));
    }

    @Test
    void lend_fourthBook_conflictsAndSavesNothing() {
        long member = newMember("하승");
        for (int i = 1; i <= LibraryService.LOAN_LIMIT; i++) service.lend(member, newBook("b" + i));
        long fourth = newBook("b4");
        assertThrows(ConflictException.class, () -> service.lend(member, fourth));
        assertEquals(LibraryService.LOAN_LIMIT, loans.all.size(), "한도 초과 대출은 저장되면 안 된다");
    }

    @Test
    void giveBack_setsReturnedAtFromClock_andSecondReturnConflicts() {
        LoanResponse loan = service.lend(newMember("하승"), newBook("111"));
        assertNull(loan.returnedAt());
        assertEquals(NOW, loan.loanedAt());

        assertEquals(NOW, service.giveBack(loan.id()).returnedAt());
        assertThrows(ConflictException.class, () -> service.giveBack(loan.id()));
        assertThrows(NotFoundException.class, () -> service.giveBack(99));
    }

    @Test
    void returnedBook_canBeLentAgain_andActiveLoansSkipReturned() {
        long book = newBook("111");
        long haseung = newMember("하승");
        long minji = newMember("민지");
        service.giveBack(service.lend(haseung, book).id());
        service.lend(minji, book);
        assertEquals(0, service.activeLoans(haseung).size());
        assertEquals(1, service.activeLoans(minji).size());
        assertThrows(NotFoundException.class, () -> service.activeLoans(99));
    }

    // ---- 가짜 리포지토리: 메모리 리스트. save 가 DB 처럼 id 를 매긴다 ----

    static <T> T assignId(T entity, List<T> all) {
        if (ReflectionTestUtils.getField(entity, "id") == null) {
            ReflectionTestUtils.setField(entity, "id", (long) all.size() + 1);
            all.add(entity);
        }
        return entity;
    }

    static class FakeBooks implements BookRepository {
        final List<Book> all = new ArrayList<>();
        public Book save(Book b) { return assignId(b, all); }
        public Optional<Book> findById(Long id) { return all.stream().filter(b -> b.getId().equals(id)).findFirst(); }
        public boolean existsByIsbn(String isbn) { return all.stream().anyMatch(b -> b.getIsbn().equals(isbn)); }
        public List<Book> findAllByOrderByIdAsc() { return List.copyOf(all); }
        public List<Book> findByTitleContainingIgnoreCaseOrderByIdAsc(String k) {
            return all.stream().filter(b -> b.getTitle().toLowerCase().contains(k.toLowerCase())).toList();
        }
    }

    static class FakeMembers implements MemberRepository {
        final List<Member> all = new ArrayList<>();
        public Member save(Member m) { return assignId(m, all); }
        public Optional<Member> findById(Long id) { return all.stream().filter(m -> m.getId().equals(id)).findFirst(); }
        public boolean existsById(Long id) { return findById(id).isPresent(); }
    }

    static class FakeLoans implements LoanRepository {
        final List<Loan> all = new ArrayList<>();
        public Loan save(Loan l) { return assignId(l, all); }
        public Optional<Loan> findById(Long id) { return all.stream().filter(l -> l.getId().equals(id)).findFirst(); }
        public boolean existsByBookIdAndReturnedAtIsNull(Long bookId) {
            return all.stream().anyMatch(l -> l.getBook().getId().equals(bookId) && l.getReturnedAt() == null);
        }
        public long countByMemberIdAndReturnedAtIsNull(Long memberId) {
            return findByMemberIdAndReturnedAtIsNullOrderByIdAsc(memberId).size();
        }
        public List<Loan> findByMemberIdAndReturnedAtIsNullOrderByIdAsc(Long memberId) {
            return all.stream()
                    .filter(l -> Objects.equals(l.getMember().getId(), memberId) && l.getReturnedAt() == null)
                    .sorted(Comparator.comparing(Loan::getId)).toList();
        }
    }
}
