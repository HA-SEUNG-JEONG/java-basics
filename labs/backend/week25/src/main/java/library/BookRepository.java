package library;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.Repository;

/**
 * JpaRepository 대신 Repository 를 상속해 쓰는 메서드만 선언한다.
 * 그래서 서비스 단위 테스트의 가짜 리포지토리가 이 몇 개만 구현하면 된다.
 */
public interface BookRepository extends Repository<Book, Long> {
    Book save(Book book);

    Optional<Book> findById(Long id);

    boolean existsByIsbn(String isbn);

    List<Book> findAllByOrderByIdAsc();

    List<Book> findByTitleContainingIgnoreCaseOrderByIdAsc(String keyword);
}
