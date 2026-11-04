package catalog;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findByAuthorName(String name);
    List<Book> findByTitleContainingIgnoreCaseOrderByTitleAsc(String keyword);
    List<Book> findByPriceBetween(int min, int max);
    long countByAuthorName(String name);
    boolean existsByTitle(String title);
}
