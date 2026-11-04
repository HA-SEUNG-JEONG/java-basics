package catalog;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 해야 할 일 두 가지
 *  1) findPopularAuthors: 책이 n 권 이상인 저자를 이름순으로. 이름만으로는 만들 수 없으니
 *     @Query 안의 JPQL 을 직접 고쳐 쓰세요.  (힌트: size(a.books))
 *  2) findAllWithBooks: 저자와 책을 "쿼리 한 번" 에 같이 가져오게 하세요.
 *     (@EntityGraph(attributePaths = "books") 또는 left join fetch)
 */
public interface AuthorRepository extends JpaRepository<Author, Long> {

    @Query("select a from Author a where 1 = 0")
    List<Author> findPopularAuthors(@Param("n") int n);

    @Query("select a from Author a")
    List<Author> findAllWithBooks();
}
