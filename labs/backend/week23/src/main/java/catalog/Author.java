package catalog;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/** 주어진 코드입니다. 고치지 않고 읽기만 합니다. (1 : N 에서 "1" 쪽) */
@Entity
public class Author {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    // mappedBy = "author": 외래 키는 Book.author 쪽이 가진다. cascade: Author 를 저장하면 책도 같이 저장.
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL)
    private List<Book> books = new ArrayList<>();

    protected Author() {}

    public Author(String name) {
        this.name = name;
    }

    /** 양쪽 관계를 한 번에 맞춰 준다. */
    public void addBook(Book book) {
        books.add(book);
        book.setAuthor(this);
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public List<Book> getBooks() { return books; }
}
