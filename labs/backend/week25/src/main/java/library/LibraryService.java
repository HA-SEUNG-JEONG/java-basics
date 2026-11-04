package library;

import library.Dto.BookRequest;
import library.Dto.BookResponse;
import library.Dto.LoanResponse;
import library.Dto.MemberRequest;
import library.Dto.MemberResponse;
import java.time.Clock;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 업무 규칙은 모두 여기서 판단한다. 컨트롤러는 HTTP 변환만 한다. */
@Service
@Transactional
public class LibraryService {
    static final int LOAN_LIMIT = 3;

    private final BookRepository books;
    private final MemberRepository members;
    private final LoanRepository loans;
    private final Clock clock;

    public LibraryService(BookRepository books, MemberRepository members, LoanRepository loans, Clock clock) {
        this.books = books;
        this.members = members;
        this.loans = loans;
        this.clock = clock;
    }

    public BookResponse createBook(BookRequest req) {
        if (books.existsByIsbn(req.isbn())) {
            throw new ConflictException("이미 등록된 isbn 입니다: " + req.isbn());
        }
        return BookResponse.from(books.save(new Book(req.isbn(), req.title(), req.author())));
    }

    @Transactional(readOnly = true)
    public List<BookResponse> searchBooks(String keyword) {
        List<Book> found = (keyword == null || keyword.isBlank())
                ? books.findAllByOrderByIdAsc()
                : books.findByTitleContainingIgnoreCaseOrderByIdAsc(keyword);
        return found.stream().map(BookResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public BookResponse getBook(long id) {
        return BookResponse.from(findBook(id));
    }

    public MemberResponse createMember(MemberRequest req) {
        return MemberResponse.from(members.save(new Member(req.name())));
    }

    public LoanResponse lend(long memberId, long bookId) {
        Member member = members.findById(memberId)
                .orElseThrow(() -> new NotFoundException("회원을 찾을 수 없습니다: " + memberId));
        Book book = findBook(bookId);
        if (loans.existsByBookIdAndReturnedAtIsNull(bookId)) {
            throw new ConflictException("이미 대출 중인 책입니다: " + bookId);
        }
        if (loans.countByMemberIdAndReturnedAtIsNull(memberId) >= LOAN_LIMIT) {
            throw new ConflictException("한 사람은 " + LOAN_LIMIT + "권까지 빌릴 수 있습니다");
        }
        // ponytail: 같은 책·같은 회원의 동시 요청은 검사와 저장 사이에서 둘 다 통과할 수 있다.
        // 동시성이 문제가 되면 책·회원 행에 비관적 락(@Lock)을 걸거나 DB 부분 유니크 인덱스를 둔다.
        return LoanResponse.from(loans.save(new Loan(book, member, clock.instant())));
    }

    public LoanResponse giveBack(long loanId) {
        Loan loan = loans.findById(loanId)
                .orElseThrow(() -> new NotFoundException("대출을 찾을 수 없습니다: " + loanId));
        if (loan.getReturnedAt() != null) {
            throw new ConflictException("이미 반납된 대출입니다: " + loanId);
        }
        loan.markReturned(clock.instant());
        return LoanResponse.from(loan);
    }

    @Transactional(readOnly = true)
    public List<LoanResponse> activeLoans(long memberId) {
        if (!members.existsById(memberId)) {
            throw new NotFoundException("회원을 찾을 수 없습니다: " + memberId);
        }
        return loans.findByMemberIdAndReturnedAtIsNullOrderByIdAsc(memberId).stream()
                .map(LoanResponse::from).toList();
    }

    private Book findBook(long id) {
        return books.findById(id).orElseThrow(() -> new NotFoundException("책을 찾을 수 없습니다: " + id));
    }
}
