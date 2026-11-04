package library;

import jakarta.persistence.*;

@Entity
@Table(name = "members") // MEMBER 는 SQL 예약어와 겹칠 수 있어 복수형으로
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    protected Member() {}

    public Member(String name) {
        this.name = name;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
}
