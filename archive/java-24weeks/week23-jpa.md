# 23주차. JPA와 데이터베이스 연동

**예상 시간:** 10시간 | **목표:** 17주차에 손으로 한 JDBC 반복 작업(`PreparedStatement`, `ResultSet` 매핑)을 JPA 가 어떻게 없애 주는지 보고, 그 대가인 **N+1 문제**를 직접 일으켜 측정하고 해결한다.

> 명령은 모두 `../../labs/backend/` 에서 실행합니다. 17주차(SQL·JDBC)와 21주차(Spring 기초)를 먼저 끝내세요. 이번 주의 DB 는 테스트가 알아서 띄우는 H2 메모리 DB 입니다. 설치할 것은 없습니다.

## 🖐 먼저 손으로 (읽기 전에 하세요)

> 이 주차의 핵심은 아래 단계를 **직접 타이핑하면서** 깨닫는 것입니다. 아래쪽 설명 문서는 먼저 읽지 않아도 됩니다. 막혔을 때 사전처럼 펼쳐 보세요.

### 1단계. 쿼리 횟수 예측하기 (20분)

저자(`Author`)와 책(`Book`)이 1 : N 관계입니다. 저자 3명(Joshua 2권, Robert 3권, Kent 1권)이 저장되어 있고, 저자의 책 목록(`books`)은 **지연 로딩**(실제로 쓸 때 가져옴)입니다. 아래 상황에서 DB 로 나가는 **SQL 문장 수**를 노트에 먼저 적으세요.

| # | 코드 | 내 예측 (SQL 몇 번?) |
|---|------|------|
| A | `List<Author> all = authorRepository.findAll();` 만 실행 | |
| B | A 에 이어, 저자마다 `a.getBooks().size()` 를 호출 | |
| C | B 와 같은 일을 저자 100명으로 | |
| D | `authorRepository.findAllWithBooks()` 후 저자마다 `getBooks().size()` (책을 같이 가져오는 쿼리) | |

<details>
<summary>정답 (예측을 적은 뒤에 여세요)</summary>

```text
A  1
B  4     (저자 목록 1번 + 저자마다 책 목록 1번씩 3번)
C  101   (1 + N)
D  1
```

B 와 C 가 **N+1 문제**입니다. 코드는 평범한 `for` 문 하나인데 저자 수만큼 쿼리가 늘어납니다. 개발할 때는 데이터가 적어 눈치채지 못하다가 운영에서 느려집니다. 3단계 테스트가 B 는 4, D 는 1 이라고 직접 검사합니다.

</details>

### 2단계. 일부러 망가뜨리기 (40분)

3단계 실습이 끝난 뒤에 하나씩 고장 내고, 먼저 어떤 테스트가 실패할지 **예측**한 뒤 확인하세요. 에러 메시지는 첫 줄을 노트에 옮겨 적습니다. 원복은 필수입니다.

| 이렇게 바꿔 보세요 | 확인할 것 |
|---|---|
| `Book` 의 `@ManyToOne(fetch = FetchType.LAZY)` 에서 `fetch = ...` 삭제 | 1개 실패: `bookToAuthorIsLazy`. `@ManyToOne` 의 기본은 즉시 로딩이라 저자가 이미 읽혀 있다 (`expected: <false> but was: <true>`) |
| `AuthorRepository.findAllWithBooks` 에서 `@EntityGraph` 삭제 | 1개 실패: `findAllWithBooks_isOneQuery`. `expected: <1> but was: <4>` — `findAll` 과 같은 N+1 로 돌아간다 |
| `Author` 의 `cascade = CascadeType.ALL` 삭제 | 8개 실패. 저자만 저장되고 책은 저장되지 않아 `expected: <2> but was: <0>` 같은 메시지가 줄줄이 나온다. 증상이 원인에서 멀리 떨어져 나타나는 예 |
| `Book` 의 `@Id` 삭제 | 10개 모두 실패. 컨텍스트가 시작도 못 한다. 긴 메시지 속에서 `has no identifier (every '@Entity' class must declare or inherit at least one '@Id' ...)` 를 찾아 보기 |
| `BookRepository` 에 `List<Book> findByTitel(String t);` 추가 (오타) | 10개 모두 실패. 컨텍스트 시작 오류: `PropertyReferenceException: No property 'titel' found for type 'Book'; Did you mean 'title'`. 메서드 이름이 곧 쿼리라서 오타가 컴파일이 아니라 **시작할 때** 드러난다 |

SQL 을 눈으로 보려면 테스트 클래스의 `@DataJpaTest(properties = ...)` 에 `"spring.jpa.show-sql=true"` 를 추가하고 한 테스트만 골라 `-i` 로 실행합니다.

```bash
./gradlew :week23:test -i --tests 'CatalogJpaTest.findAll_thenTouchingBooks_isNPlusOne'
```

출력에는 `Hibernate: create table ...`, `Hibernate: insert into ...`(테스트 준비 코드가 저자·책을 저장한 것)도 섞여 있습니다. **`Hibernate: select` 로 시작하는 줄만** 세어 1단계의 예측 B 와 맞는지 확인하세요. 같은 방법으로 `findAllWithBooks_isOneQuery` 를 실행하면 `select ... from author ... left join book ...` 한 줄뿐입니다. (확인 후 원복)

### 3단계. 빈칸에서 만들기 — 자동 채점 (3~5시간)

`../../labs/backend/week23/src/main/java/catalog/` 의 파일을 채웁니다. `Author` 는 읽기 전용 예시이고, 만드는 것은 아래 세 곳입니다.

| 파일 | 해야 할 일 |
|---|---|
| `Book` | 엔티티로 표시, `@Id` + 자동 생성, `author` 를 지연 로딩 `@ManyToOne` 으로 |
| `BookRepository` | 각 메서드의 임시 `@Query` 한 줄을 지우고 **이름만으로** 쿼리가 만들어지게 |
| `AuthorRepository` | `findPopularAuthors` 의 JPQL 을 직접 쓰고, `findAllWithBooks` 에서 N+1 해결 |

```bash
cd ../../labs/backend
./gradlew :week23:test
```

- 처음에는 10개가 모두 실패합니다. `Book` 이 아직 엔티티가 아니라 컨텍스트가 시작되지 못하기 때문입니다. 에러 메시지를 위에서부터 읽고 `Book` 부터 고치세요.
- `Book` 을 고치면 임시 `@Query` 때문에 개별 테스트가 하나씩 실패합니다. 임시 `@Query` 는 "항상 빈 결과"를 돌려주는 자리표시입니다.
- 정답은 `../../labs/solutions/week23/catalog/` 에 있습니다. **15분 넘게 막혔을 때만** 열고, 본 뒤에는 덮고 **직접 다시 치세요.**

---

## 1. JPA 는 무엇을 대신하나

17주차에 직접 쓴 코드를 떠올려 보세요.

```java
PreparedStatement ps = conn.prepareStatement("SELECT id, name, email, age FROM users WHERE id = ?");
ps.setLong(1, id);
ResultSet rs = ps.executeQuery();
// ... rs.getLong("id"), rs.getString("name") ... -> new User(...)
```

JPA 에서는 같은 일이 한 줄입니다.

```java
Optional<User> u = userRepository.findById(id);
```

| 17주차 JDBC | 23주차 JPA / Spring Data |
|---|---|
| 테이블 ↔ 객체 변환을 직접 (`map(rs)`) | 엔티티 클래스에 매핑 정보를 표시 |
| SQL 문자열을 직접 | 메서드 이름 또는 JPQL 에서 SQL 생성 |
| `insert`/`update`/`delete` 를 직접 | `save`, `delete` |
| 트랜잭션 `setAutoCommit`/`commit`/`rollback` 을 직접 | `@Transactional` (테스트에서는 자동으로 감싸고 끝나면 롤백) |

대가가 있습니다. **어떤 SQL 이 나가는지 눈에 보이지 않습니다.** 이번 주의 N+1 이 그 대표 사례입니다. JPA 를 쓸수록 SQL 을 읽을 줄 알아야 하는 이유입니다.

## 2. 엔티티 매핑

```java
@Entity
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)   // 키는 DB 가 자동 생성
    private Long id;

    private String title;
    private int price;

    @ManyToOne(fetch = FetchType.LAZY)                    // 여러 책 : 한 저자. 저자는 쓸 때 가져옴
    private Author author;

    protected Book() {}                                   // JPA 가 요구하는 기본 생성자
    ...
}
```

- `Author` 쪽의 `@OneToMany(mappedBy = "author")` 는 "외래 키는 `Book.author` 가 가진다"는 뜻입니다. **외래 키가 있는 쪽이 관계의 주인**입니다. `Author.addBook` 이 양쪽을 함께 맞추는 이유입니다.
- `@ManyToOne` 의 기본 로딩은 즉시(EAGER)입니다. 실무에서는 **LAZY 를 명시**하는 습관이 안전합니다.
- 기본 생성자가 필요한 이유는 JPA 가 DB 에서 읽은 값으로 객체를 만들 때 리플렉션으로 생성하기 때문입니다. (21주차 미니 컨테이너와 같은 원리)

## 3. 메서드 이름으로 쿼리 만들기

`findBy…` 의 이름을 Spring Data 가 해석해서 JPQL → SQL 로 바꿉니다.

| 메서드 이름 | 만들어지는 조건 |
|---|---|
| `findByAuthorName(name)` | `author.name = ?` (관계를 따라감) |
| `findByTitleContainingIgnoreCaseOrderByTitleAsc(k)` | 제목에 k 포함(대소문자 무시), 제목 오름차순 |
| `findByPriceBetween(min, max)` | `price between ? and ?` (양 끝 포함) |
| `countByAuthorName(name)`, `existsByTitle(t)` | 개수, 존재 여부 |

이름으로 만들기 어려운 조건은 JPQL(엔티티 기준의 SQL)을 직접 씁니다.

```java
@Query("select a from Author a where size(a.books) >= :n order by a.name")
List<Author> findPopularAuthors(@Param("n") int n);
```

테이블 이름이 아니라 **엔티티 이름(`Author`)과 필드 이름(`a.books`)** 을 씁니다.

## 4. N+1 과 해결

저자 N명을 가져온 뒤 저자마다 `getBooks()` 를 부르면 쿼리 1 + N 번. 해결은 **필요한 연관을 처음 쿼리에서 함께 가져오는 것**입니다.

```java
@EntityGraph(attributePaths = "books")
@Query("select a from Author a")
List<Author> findAllWithBooks();           // 쿼리 1번
```

또는 JPQL 에서 `left join fetch a.books`. 어떤 방법이든 "저자 목록을 읽는 지점에서 무엇을 함께 쓸지"를 미리 아는 코드만 가능합니다.

주의:
- 컬렉션을 함께 가져오는 쿼리는 행이 책 수만큼 불어나 저자가 중복될 수 있습니다. 테스트가 "저자는 3명"인지 검사하는 이유입니다.
- 컬렉션 두 개를 한꺼번에 가져오거나 페이징과 섞으면 문제가 생깁니다. 이럴 때 배치 크기 설정(`default_batch_fetch_size`)이나 쿼리 분리를 씁니다. 입문 단계에서는 "문제가 있다는 것"만 알아 두세요.
- **측정 없이 최적화하지 않습니다.** 이번 주의 테스트처럼 쿼리 횟수를 세어(Hibernate Statistics) 숫자로 확인합니다.

## 5. 테스트

`@DataJpaTest` 는 JPA 관련 빈(엔티티, 리포지토리)만 띄우고 임베디드 H2 를 연결합니다. 각 테스트는 트랜잭션 안에서 실행되고 **끝나면 롤백**되어 서로 영향을 주지 않습니다. `TestEntityManager` 의 `flush()` 와 `clear()` 로 "DB 에 반영하고 1차 캐시를 비운다"를 직접 제어해, 다음 조회가 정말 DB 로 가게 합니다. (이 둘을 빼면 N+1 이 캐시에 가려져 보이지 않습니다.)

## 연습문제

1. `Book` 에 `findByAuthorNameAndPriceGreaterThan(String name, int price)` 를 추가하고 테스트를 작성하세요.
2. 책 목록 조회에서 책마다 `getAuthor().getName()` 을 부르는 코드를 작성하고, 쿼리가 몇 번 나가는지 센 뒤 `join fetch` 로 줄여 보세요. (책 6권, 저자 3명일 때 예측 후 확인)
3. `Pageable` 을 받는 `Page<Book> findByAuthorName(String name, Pageable pageable)` 로 바꾸고, 첫 페이지 2권만 가져오는 테스트를 쓰세요.
4. 22주차의 인메모리 `BookService` 를 JPA 리포지토리를 쓰도록 바꾸는 계획을 글로 적어 보세요. 어떤 클래스가 바뀌고 어떤 클래스는 바뀌지 않아야 하나요? (바뀌지 않는 쪽이 19주차에서 만든 구조의 장점입니다)

## 퀴즈

1. N+1 문제란? 그리고 왜 개발 중에는 발견하기 어려운가?
2. 엔티티에 기본 생성자(`protected Book() {}`)가 필요한 이유는?
3. `@ManyToOne` 의 기본 로딩 방식은 무엇이고, 왜 명시적으로 지정하나?
4. 메서드 이름 쿼리와 `@Query` 중 어느 것을 쓸지 고르는 기준은?

<details>
<summary>정답</summary>

1. 목록 1번 조회 후 각 항목의 연관을 접근하며 쿼리가 항목 수(N)만큼 더 나가는 문제. 개발 데이터는 적어서 쿼리 수가 늘어나도 느껴지지 않기 때문
2. JPA 가 DB 값을 읽어 객체를 만들 때 리플렉션으로 기본 생성자를 호출하기 때문
3. 즉시(EAGER). 읽을 때마다 연관을 함께 가져와 의도치 않은 쿼리가 늘기 쉬우므로 LAZY 로 명시하고 필요한 곳에서 함께 가져온다
4. 이름이 읽기 쉽고 조건이 두세 개까지면 메서드 이름, 조건이 복잡하거나 이름이 길어지면 `@Query`

</details>

## 체크리스트

- [ ] 1단계의 쿼리 횟수 A~D 를 예측하고 테스트로 확인했다
- [ ] 17주차 코드의 어느 부분을 JPA 가 대신하는지 설명할 수 있다
- [ ] N+1 을 일으키고 `@EntityGraph` 로 1번으로 줄였다
- [ ] 10개 테스트를 모두 통과했다
- [ ] "JPA 를 써도 SQL 을 읽을 줄 알아야 한다"는 말의 이유를 내 말로 설명할 수 있다
