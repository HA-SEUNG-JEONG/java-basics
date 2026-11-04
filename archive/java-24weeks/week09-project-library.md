# 9주차. 중간 프로젝트: 도서 대출 시스템

**예상 시간:** 10~12시간 | **목표:** 5~8주차에 배운 객체지향을 하나의 프로그램에 적용한다. **마일스톤 점검 주차**입니다.

## 🖐 먼저 손으로 (기반 코드를 보기 전에)

> 이 주차는 **아래의 기반 코드를 읽는 주차가 아니라, 같은 것을 스스로 만들어 보는 주차**입니다. 기반 코드는 막혔을 때 한 메서드씩만 엿보는 용도입니다.

### 1단계. 시나리오를 먼저 적기 (15분)

코드를 치기 전에 아래 시나리오의 "기대 결과" 칸을 노트에 직접 채우세요. 책: `111 이펙티브 자바`, `222 클린 코드` / 회원: `m1 하승`, `m2 민지`.

| # | 행동 | 기대 결과 (직접 적기) |
|---|------|------|
| 1 | 책 `111` 을 두 번 등록 | |
| 2 | 회원 `m1` 을 두 번 등록 | |
| 3 | `m1` 이 `111` 대출 | |
| 4 | 이어서 `m2` 가 `111` 대출 | |
| 5 | `m1` 이 책 3권을 빌린 뒤 4번째 대출 시도 | |
| 6 | `m2` 가 빌리지 않은 책 `111` 을 반납 | |
| 7 | `m1` 이 `111` 반납 후 `m2` 가 `111` 대출 | |
| 8 | 존재하지 않는 ISBN `999` 로 대출 | |

### 2단계. 가장 작은 것부터 직접 타이핑 (2~4시간)

1. `Book` (record), `LibraryException` 부터 만듭니다.
2. `Member` 에 `borrow` 와 한도 규칙을 넣고 `main` 에서 시나리오 5번을 실행해 보세요.
3. `Library` 에 `addBook`, `addMember`, `lend` 순으로 추가합니다. **한 메서드를 쓸 때마다 `main` 에서 실행해 확인**하세요. 전부 쓰고 나서 한 번에 돌리지 마세요.
4. 막힌 부분만 아래 기반 코드의 **해당 메서드 하나**를 읽고, 덮은 뒤 직접 다시 치세요.

### 3단계. 시나리오를 코드로 옮기기 (1시간)

위 8개 시나리오를 `main` 에서 모두 실행하는 `Scenarios.java` 를 만드세요. 테스트 프레임워크 없이, 아래처럼 직접 만든 도우미 하나면 충분합니다. (16주차에서 JUnit 이 이 도우미의 정식 버전이라는 것을 알게 됩니다.)

```java
static void check(String name, boolean ok) {
    System.out.println((ok ? "PASS " : "FAIL ") + name);
}
```

### 4단계. 버그를 심어서 시나리오가 잡아내는지 확인 (30분)

완성한 `Library.lend` 에서 `member.borrow(book)` 과 `loans.put(isbn, member)` 의 **순서를 바꿔** 보세요. 어느 시나리오가 FAIL 로 바뀌는지 확인하고, 왜 그런지 한 문장으로 적습니다. 그런 다음 순서를 원복합니다. 이것이 "테스트가 안전망"이라는 말의 실체입니다.

---


## 요구사항

콘솔에서 동작하는 도서관 시스템을 만듭니다.

| 기능 | 규칙 |
|------|------|
| 도서 등록 | ISBN, 제목, 저자로 등록. 같은 ISBN은 중복 등록 불가 |
| 회원 등록 | 회원 ID, 이름. 같은 ID는 중복 불가 |
| 대출 | 이미 대출 중인 책은 불가. 한 회원당 최대 3권 |
| 반납 | 대출 중이 아닌 책을 반납하면 오류 |
| 조회 | 전체 도서 목록(대출 상태 포함), 회원별 대출 목록 |

## 설계 가이드

먼저 클래스의 **책임**을 나눕니다. 이 연습의 핵심은 코드 양이 아니라 "누가 무엇을 아는가"입니다.

| 클래스 | 책임 |
|--------|------|
| `Book` (record) | ISBN·제목·저자 값 보관. 불변 |
| `Member` | 회원 ID·이름, 현재 대출 중인 책 목록 관리, 대출 한도 규칙 |
| `Loan` 상태 | 어떤 책이 누구에게 대출 중인지 |
| `Library` | 책과 회원을 관리하고 대출·반납 규칙을 조율 |
| `LibraryException` | 업무 규칙 위반을 알리는 예외 (`RuntimeException`) |
| `Main` | 입력·출력만 담당. 규칙은 알지 못한다 |

규칙(3권 제한, 중복 불가)은 **`Library`/`Member` 안에서** 지키고, `Main`은 예외를 잡아 사용자에게 보여 주기만 합니다.

## 기반 코드

아래는 핵심 로직만 구현한 **동작하는 기반**입니다. 위의 손 실습 1~4단계를 스스로 마친 뒤에, **자신의 코드와 비교**하는 용도로 읽으세요. 콘솔 메뉴(`Scanner` 입력)는 비워 두었으니, 직접 만들어 연결하세요. 비교가 끝나면 아래 확장 과제를 구현합니다.

```java
// File: LibraryDemo.java
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

record Book(String isbn, String title, String author) {}

class LibraryException extends RuntimeException {
    LibraryException(String message) { super(message); }
}

class Member {
    static final int MAX_LOANS = 3;

    private final String id;
    private final String name;
    private final List<Book> borrowed = new ArrayList<>();

    Member(String id, String name) {
        this.id = id;
        this.name = name;
    }

    String getId() { return id; }
    String getName() { return name; }

    // 외부에는 읽기 전용 복사본만 노출한다 (캡슐화)
    List<Book> getBorrowed() { return List.copyOf(borrowed); }

    void borrow(Book book) {
        if (borrowed.size() >= MAX_LOANS) {
            throw new LibraryException(name + "님은 최대 " + MAX_LOANS + "권까지만 빌릴 수 있습니다");
        }
        borrowed.add(book);
    }

    void giveBack(Book book) {
        if (!borrowed.remove(book)) {
            throw new LibraryException(name + "님이 빌린 책이 아닙니다: " + book.title());
        }
    }
}

class Library {
    private final Map<String, Book> books = new HashMap<>();        // isbn -> Book
    private final Map<String, Member> members = new HashMap<>();    // id -> Member
    private final Map<String, Member> loans = new HashMap<>();      // isbn -> 빌린 회원

    void addBook(Book book) {
        if (books.putIfAbsent(book.isbn(), book) != null) {
            throw new LibraryException("이미 등록된 ISBN: " + book.isbn());
        }
    }

    void addMember(Member member) {
        if (members.putIfAbsent(member.getId(), member) != null) {
            throw new LibraryException("이미 등록된 회원: " + member.getId());
        }
    }

    void lend(String isbn, String memberId) {
        Book book = findBook(isbn);
        Member member = findMember(memberId);
        if (loans.containsKey(isbn)) {
            throw new LibraryException("이미 대출 중인 책: " + book.title());
        }
        member.borrow(book);          // 한도 초과면 여기서 예외, 아래 줄은 실행되지 않는다
        loans.put(isbn, member);
    }

    void giveBack(String isbn, String memberId) {
        Book book = findBook(isbn);
        Member member = findMember(memberId);
        member.giveBack(book);
        loans.remove(isbn);
    }

    String status() {
        StringBuilder sb = new StringBuilder();
        for (Book b : books.values()) {
            Member m = loans.get(b.isbn());
            sb.append(b.title()).append(" - ")
              .append(m == null ? "대출 가능" : m.getName() + " 대출 중")
              .append('\n');
        }
        return sb.toString();
    }

    private Book findBook(String isbn) {
        Book b = books.get(isbn);
        if (b == null) throw new LibraryException("없는 도서: " + isbn);
        return b;
    }

    private Member findMember(String id) {
        Member m = members.get(id);
        if (m == null) throw new LibraryException("없는 회원: " + id);
        return m;
    }
}

public class LibraryDemo {
    public static void main(String[] args) {
        Library lib = new Library();
        lib.addBook(new Book("111", "이펙티브 자바", "조슈아 블로크"));
        lib.addBook(new Book("222", "클린 코드", "로버트 마틴"));
        lib.addMember(new Member("m1", "하승"));
        lib.addMember(new Member("m2", "민지"));

        lib.lend("111", "m1");
        try {
            lib.lend("111", "m2");               // 이미 대출 중
        } catch (LibraryException e) {
            System.out.println("오류: " + e.getMessage());
        }
        System.out.print(lib.status());

        lib.giveBack("111", "m1");
        System.out.print(lib.status());
    }
}
```

> 여기서 `Map`, `List`, `HashMap`을 처음 보더라도 이번 주에는 "키로 값을 찾는 상자" 정도로만 이해하면 됩니다. 11주차에서 제대로 다룹니다. 이 프로젝트를 다시 열어 `Map`을 직접 설계해 보는 것이 11주차 복습 과제입니다.

## 확장 과제 (필수 3개 이상)

1. **콘솔 메뉴:** `Scanner`로 "1. 책 등록 2. 회원 등록 3. 대출 4. 반납 5. 현황 0. 종료"를 반복하는 `Main`을 작성하세요. 예외는 `Main`에서 잡아 메시지만 출력합니다.
2. **대출 기한:** `Loan` record(`Book`, `Member`, 대출일)를 도입하고, 대출 기간(14일)이 지나면 `연체`로 표시하세요. (날짜는 10주차에 배우는 `LocalDate`를 미리 써 보세요)
3. **도서 분류:** `enum Category`를 추가해 `Book`에 넣고, 분류별 도서 목록을 출력하세요.
4. **회원 등급:** `Member`를 추상 클래스(또는 인터페이스)로 바꿔 `GeneralMember`(3권), `PremiumMember`(10권)로 한도를 다르게 하세요. 다형성을 활용합니다.
5. **검색:** 제목에 특정 문자열이 포함된 책을 찾는 `searchByTitle(String keyword)`를 추가하세요.
6. **대출 이력:** 누가 언제 어떤 책을 빌리고 반납했는지 이력 리스트를 남기세요.

## 점검 질문 (자기 설명)

코드를 다 짠 뒤 소리 내어 설명해 보세요.

1. 대출 한도 규칙이 `Main`이 아니라 `Member`에 있는 이유는?
2. `Member.getBorrowed()`가 원본 리스트가 아니라 `List.copyOf`를 돌려주는 이유는?
3. `Library.lend`에서 `member.borrow`를 `loans.put`보다 **먼저** 호출한 이유는? 순서를 바꾸면 어떤 버그가 생기나?
4. `Book`을 `record`로 한 이유는? `class`였다면 `member.giveBack(book)`의 `remove`가 동작하기 위해 무엇이 필요한가?

<details>
<summary>답변 힌트</summary>

1. 규칙은 그 규칙의 데이터를 가진 객체가 지켜야 하고, 입출력 코드가 바뀌어도 규칙이 유지되어야 한다
2. 바깥에서 리스트를 직접 수정해 한도 규칙을 우회하는 것을 막기 위해
3. `borrow`가 예외를 던지면 `loans`에 기록되지 않아야 한다. 순서를 바꾸면 한도 초과인데도 "대출 중"으로 기록이 남는다
4. 값 비교 `equals`/`hashCode`가 자동으로 생기기 때문이다. `class`라면 8주차처럼 직접 재정의해야 한다

</details>

## 마일스톤 통과 기준 (9주)

- [ ] 클래스가 4개 이상이고, 각 클래스의 책임을 한 문장으로 말할 수 있다
- [ ] 필드는 모두 `private`이고 규칙이 객체 안에서 지켜진다
- [ ] 업무 규칙 위반은 사용자 정의 예외로 표현한다
- [ ] 확장 과제 3개 이상을 구현했다
- [ ] `Main`에는 규칙을 판단하는 `if`가 없다
