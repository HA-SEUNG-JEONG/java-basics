package catalog;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * 해야 할 일: 각 메서드 위의 @Query 한 줄을 지우세요. 메서드 이름만으로 쿼리가 만들어집니다.
 * (@Query 는 "항상 빈 결과" 를 돌려주는 임시 자리표시입니다. 지우기 전에는 테스트가 실패합니다.)
 */
public interface BookRepository extends JpaRepository<Book, Long> {

    @Query("select b from Book b where 1 = 0")
    List<Book> findByAuthorName(String name);

    @Query("select b from Book b where 1 = 0")
    List<Book> findByTitleContainingIgnoreCaseOrderByTitleAsc(String keyword);

    @Query("select b from Book b where 1 = 0")
    List<Book> findByPriceBetween(int min, int max);

    @Query("select count(b) from Book b where 1 = 0")
    long countByAuthorName(String name);

    @Query("select count(b) > 0 from Book b where 1 = 0")
    boolean existsByTitle(String title);
}
