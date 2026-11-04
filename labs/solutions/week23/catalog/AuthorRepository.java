package catalog;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuthorRepository extends JpaRepository<Author, Long> {

    @Query("select a from Author a where size(a.books) >= :n order by a.name")
    List<Author> findPopularAuthors(@Param("n") int n);

    @EntityGraph(attributePaths = "books")
    @Query("select a from Author a")
    List<Author> findAllWithBooks();
}
