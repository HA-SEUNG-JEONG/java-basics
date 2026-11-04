package library;

import jakarta.persistence.*;
import java.time.Instant;

/** 외래 키(book_id, member_id)를 가진 쪽이 관계의 주인이다. */
@Entity
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Member member;

    @Column(nullable = false)
    private Instant loanedAt;

    private Instant returnedAt; // 반납 전에는 null

    protected Loan() {}

    public Loan(Book book, Member member, Instant loanedAt) {
        this.book = book;
        this.member = member;
        this.loanedAt = loanedAt;
    }

    void markReturned(Instant at) {
        this.returnedAt = at;
    }

    public Long getId() { return id; }
    public Book getBook() { return book; }
    public Member getMember() { return member; }
    public Instant getLoanedAt() { return loanedAt; }
    public Instant getReturnedAt() { return returnedAt; }
}
