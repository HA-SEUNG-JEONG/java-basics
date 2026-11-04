package catalog;

/**
 * 해야 할 일: 이 클래스를 DB 테이블과 연결(엔티티)하세요.
 *   - 클래스를 엔티티로 표시
 *   - id 를 기본 키로 표시하고, 값은 DB 가 자동으로 만들게 하기 (IDENTITY)
 *   - author 는 "여러 책 : 한 저자" 관계로 표시하되, 책을 읽을 때 저자를 곧바로 가져오지 않고
 *     실제로 쓸 때 가져오게(지연 로딩) 하기
 */
public class Book {
    private Long id;
    private String title;
    private int price;
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
