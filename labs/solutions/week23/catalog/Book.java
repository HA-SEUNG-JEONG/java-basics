package catalog;

import jakarta.persistence.*;

@Entity
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private int price;

    @ManyToOne(fetch = FetchType.LAZY)
    private Author author;

    protected Book() {}

    public Book(String title, int price) {
        this.title = title;
        this.price = price;
    }

    void setAuthor(Author author) { this.author = author; }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public int getPrice() { return price; }
    public Author getAuthor() { return author; }
}
