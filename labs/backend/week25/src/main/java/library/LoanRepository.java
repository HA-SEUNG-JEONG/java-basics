package library;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.Repository;

public interface LoanRepository extends Repository<Loan, Long> {
    Loan save(Loan loan);

    Optional<Loan> findById(Long id);

    boolean existsByBookIdAndReturnedAtIsNull(Long bookId);

    long countByMemberIdAndReturnedAtIsNull(Long memberId);

    /** 책·회원을 같은 쿼리에서 함께 가져온다. 없으면 대출 건수만큼 쿼리가 더 나간다(N+1). */
    @EntityGraph(attributePaths = {"book", "member"})
    List<Loan> findByMemberIdAndReturnedAtIsNullOrderByIdAsc(Long memberId);
}
