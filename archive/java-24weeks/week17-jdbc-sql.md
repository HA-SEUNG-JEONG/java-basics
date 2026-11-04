# 17주차. SQL과 JDBC

**예상 시간:** 10시간 | **목표:** 데이터베이스에 SQL을 직접 던져 보고, 자바에서 JDBC로 같은 일을 한다. 17주차부터는 **Phase 5. 백엔드 준비**입니다. Spring이 대신해 주는 일을 먼저 손으로 해 봅니다.

> **준비:** 17주차부터는 `../../labs/backend/` 의 Gradle 프로젝트에서 실습합니다. 처음 한 번 `./gradlew --version` 을 실행하면 Gradle 이 내려받아집니다. (인터넷 필요) 명령은 모두 `../../labs/backend/` 에서 실행합니다. SQL 예측 실습에는 `sqlite3` 가 필요합니다. macOS 에는 기본으로 있고, 없으면 `brew install sqlite` 로 설치하세요.

## 🖐 먼저 손으로 (읽기 전에 하세요)

> 이 주차의 핵심은 아래 단계를 **직접 타이핑하면서** 깨닫는 것입니다. 아래쪽 설명 문서는 먼저 읽지 않아도 됩니다. 막혔을 때 사전처럼 펼쳐 보세요.

### 1단계. SQL 예측하고 → 치고 → 비교하기 (30분)

1. 아래 스크립트를 `predict17.sql` 로 **한 글자씩 타이핑**합니다. 앞의 네 줄은 테이블과 데이터를 만드는 부분입니다.
2. 아래쪽 `SELECT`/`UPDATE`/`DELETE` 각 줄의 결과를 **실행 전에** 노트에 적습니다. 여러 줄이 나오는 쿼리는 줄 순서까지 적으세요.
3. `sqlite3 :memory: < predict17.sql` 로 실행해 비교합니다. 틀린 줄에 ★를 치고 왜 그렇게 나왔는지 한 문장으로 적습니다.

```sql
CREATE TABLE users (id INTEGER PRIMARY KEY, name TEXT, age INTEGER);
CREATE TABLE orders (id INTEGER PRIMARY KEY, user_id INTEGER, price INTEGER);
INSERT INTO users VALUES (1, 'ha', 25), (2, 'min', 17), (3, 'jun', 31);
INSERT INTO orders VALUES (1, 1, 100), (2, 1, 250), (3, 3, 40);

SELECT name FROM users WHERE age >= 19 ORDER BY name;
SELECT COUNT(*) FROM orders;
SELECT user_id, SUM(price) FROM orders GROUP BY user_id;
SELECT u.name, o.price FROM users u JOIN orders o ON o.user_id = u.id ORDER BY o.id;
SELECT u.name, o.price FROM users u LEFT JOIN orders o ON o.user_id = u.id ORDER BY u.id, o.id;
SELECT name FROM users WHERE name = 'ha' OR 1 = 1 ORDER BY id;
UPDATE users SET age = age + 1 WHERE name = 'min';
SELECT age FROM users WHERE id = 2;
DELETE FROM users WHERE id = 99;
SELECT COUNT(*) FROM users;
```

<details>
<summary>실행 결과 (예측을 적은 뒤에 여세요)</summary>

```text
ha
jun
3
1|350
3|40
ha|100
ha|250
jun|40
ha|100
ha|250
min|
jun|40
ha
min
jun
18
3
```

`JOIN` 은 주문이 없는 `min` 을 빼고, `LEFT JOIN` 은 왼쪽(users) 행을 모두 남기고 주문이 없으면 빈 값을 채웁니다. `WHERE name = 'ha' OR 1 = 1` 은 항상 참이라 **모든 행**이 나옵니다. 이 줄이 바로 아래 2단계의 SQL 인젝션입니다.

</details>

### 2단계. 일부러 망가뜨리기 (30분)

| 이렇게 바꿔 보세요 | 어떻게 될까요? |
|---|---|
| `UPDATE users SET age = age + 1` 에서 `WHERE` 를 지우고 실행 후 `SELECT * FROM users` | **모든 행**의 나이가 바뀐다 (`26`, `18`, `32`). `WHERE` 없는 `UPDATE`/`DELETE` 는 사고의 단골 원인 |
| `INSERT INTO users VALUES (1, 'dup', 1)` (같은 id 로 다시) | `UNIQUE constraint failed: users.id`. 제약 조건이 잘못된 데이터를 막아 준다 |
| `SELECT * FROM userz` | `no such table: userz`. SQL 오타는 **실행해야** 알 수 있다 (컴파일 시점이 아니다) |

그리고 Java 쪽에서 직접 느껴 봅니다. 3단계에서 `UserDao.findByEmail` 을 완성한 뒤, 일부러 이렇게 바꿔서 테스트를 돌려 보세요.

```java
// 위험한 버전: 사용자 입력을 문자열로 이어 붙였다
String sql = "SELECT id, name, email, age FROM users WHERE email = '" + email + "'";
```

`findByEmail("x' OR '1'='1")` 이 **다른 사람의 행을 돌려준다**는 것을 확인하세요. 1단계의 `OR 1 = 1` 과 같은 원리입니다. `?` 와 `PreparedStatement` 로 바꾸면 막힙니다.

### 3단계. 빈칸에서 만들기 — 자동 채점 (3~5시간)

`../../labs/backend/week17/src/main/java/UserDao.java` 의 `// 여기를 고치세요` 를 하나씩 구현합니다. (`Db.java` 는 수정 금지)

```bash
cd ../../labs/backend
./gradlew :week17:test
```

- 처음에는 11개가 모두 실패합니다. 정상입니다. 실패 메시지의 `expected / but was` 를 읽고 위에서부터 하나씩 고치세요.
- 정답은 `../../labs/solutions/week17/UserDao.java` 에 있습니다. **15분 넘게 막혔을 때만** 열고, 본 뒤에는 덮고 **직접 다시 치세요.**
- 이 주차의 테스트는 `PreparedStatement` 를 쓰지 않으면 통과할 수 없는 항목(인젝션)과, 중간에 실패하면 되돌아가야 하는 항목(트랜잭션)을 포함합니다.

---

## 1. 관계형 DB와 SQL 한눈에

| 개념 | 설명 | JS/TS 와 비교 |
|------|------|---------------|
| 테이블, 행(row), 열(column) | 스프레드시트처럼 생각하면 됩니다 | 객체 배열 `[{id, name}, ...]` |
| 기본 키(PK) | 행을 하나로 특정하는 값 | `id` 필드 |
| 외래 키(FK) | 다른 테이블의 PK를 가리키는 열. `orders.user_id → users.id` | 객체 간 참조 |
| `JOIN` | 외래 키로 두 테이블을 이어 붙여 조회 | 두 배열을 `id` 로 합치는 `map`/`find` |
| `GROUP BY` + 집계 | 그룹별 `COUNT`/`SUM`/`AVG` | `reduce` 로 그룹핑 |

가장 자주 쓰는 네 문장: `SELECT`(조회), `INSERT`(추가), `UPDATE`(수정), `DELETE`(삭제). `WHERE` 가 빠지면 전체가 대상이 됩니다.

## 2. JDBC 의 기본 흐름

JDBC 는 자바가 DB 와 대화하는 표준 API 입니다. 흐름은 항상 같습니다.

1. `Connection` 을 얻는다 (`DriverManager.getConnection(url)`)
2. `PreparedStatement` 에 SQL 을 넣고 `?` 자리에 값을 채운다 (`setString(1, ...)` 번호는 **1부터**)
3. 조회는 `executeQuery()` → `ResultSet` 을 `next()` 로 한 행씩 읽는다. 변경은 `executeUpdate()` → 영향받은 행 수
4. 다 쓴 `ResultSet`/`PreparedStatement`/`Connection` 은 닫는다 → **14주차의 try-with-resources**

아래 조각은 `../../labs/solutions/week17/UserDao.java` 에서 가져온 것으로, 채점 테스트를 통과한 코드입니다.

```java
public Optional<User> findById(long id) throws SQLException {
    try (PreparedStatement ps = conn.prepareStatement("SELECT id, name, email, age FROM users WHERE id = ?")) {
        ps.setLong(1, id);
        try (ResultSet rs = ps.executeQuery()) {
            return rs.next() ? Optional.of(map(rs)) : Optional.empty();
        }
    }
}

private User map(ResultSet rs) throws SQLException {
    return new User(rs.getLong("id"), rs.getString("name"), rs.getString("email"), rs.getInt("age"));
}
```

- 없을 수 있는 한 건 조회는 12주차의 `Optional` 로 돌려줍니다. `null` 을 돌려주지 않습니다.
- 행을 객체로 바꾸는 코드(`map`)는 한 곳에 모아 둡니다. 나중에 JPA(23주차)가 이 반복을 없애 줍니다.
- `INSERT` 후 자동 생성된 id 가 필요하면 `prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)` 와 `getGeneratedKeys()` 를 씁니다.

## 3. SQL 인젝션과 PreparedStatement

문자열을 이어 붙인 SQL 은 입력값이 **SQL 문장의 일부**가 됩니다. `?` 로 넘긴 값은 **데이터로만** 취급됩니다. 이름도 해결책도 이 한 줄입니다: **사용자 입력을 SQL 문자열에 이어 붙이지 않는다.** (`ORDER BY` 의 열 이름처럼 `?` 로 못 넘기는 부분은 허용 목록으로 검사합니다.)

## 4. 트랜잭션

"주문 두 건을 함께 넣는다. 하나라도 실패하면 둘 다 없던 일로" 가 트랜잭션입니다. JDBC 는 기본이 **자동 커밋**(문장 하나마다 확정)이라 직접 끄고 묶어야 합니다.

```java
boolean original = conn.getAutoCommit();
conn.setAutoCommit(false);              // 여기서부터 한 덩어리
try {
    insertOrder(userId, item1, price1);
    if (price2 < 0) throw new IllegalArgumentException("가격은 0 이상이어야 합니다: " + price2);
    insertOrder(userId, item2, price2);
    conn.commit();                      // 모두 성공 -> 확정
} catch (SQLException | RuntimeException e) {
    conn.rollback();                    // 하나라도 실패 -> 전부 취소
    throw e;
} finally {
    conn.setAutoCommit(original);       // 연결을 쓴 사람을 위해 원래 상태로
}
```

실패했는데 `rollback()` 을 빠뜨리면 첫 번째 주문이 남아 있는 채로 다음 코드가 실행됩니다. 테스트가 `countOrders()` 로 이것을 잡아냅니다.

> **Spring 과의 연결:** 24주차까지 가면 `@Transactional` 한 줄이 위의 `setAutoCommit`/`commit`/`rollback` 을 대신합니다. 손으로 한 번 해 봤기 때문에 그 한 줄이 무엇을 하는지 알 수 있습니다.

## 연습문제

1. `UserDao` 에 이름의 일부로 검색하는 `findByNameContaining(String keyword)` 를 추가하세요. (`LIKE ?` 에 `"%" + keyword + "%"` 를 `setString` 으로 넘깁니다. `%` 를 SQL 문자열에 이어 붙이지 않는 이유를 설명하세요.)
2. `SELECT` 한 번으로 "주문이 한 건도 없는 사용자"를 찾는 쿼리를 `LEFT JOIN` 과 `WHERE o.id IS NULL` 로 sqlite3 에서 먼저 만들어 보세요.
3. `placeTwoOrders` 에서 `conn.commit()` 직전에 예외를 던져 보고, `rollback()` 을 주석 처리하면 테스트가 어떻게 되는지 확인하세요.
4. `Db.java` 의 `jdbc:h2:mem:` 을 `jdbc:h2:file:./data/test` 로 바꾸면 프로그램을 껐다 켜도 데이터가 남는 것을 확인하세요. (확인 후 원복, 생긴 `data` 폴더는 삭제)

## 퀴즈

1. `PreparedStatement` 의 `setString(1, ...)` 에서 번호가 0이 아니라 1부터 시작하는 것과 관련해 흔한 실수는?
2. SQL 인젝션이 일어나는 이유와 막는 방법은?
3. 자동 커밋이 켜진 상태에서 `INSERT` 두 개를 실행하고 두 번째가 실패하면 첫 번째는?
4. `JOIN` 과 `LEFT JOIN` 의 차이는?

<details>
<summary>정답</summary>

1. 배열 습관대로 0을 쓰면 `Invalid value "0" for parameter "parameterIndex"` 오류가 난다 (H2 기준)
2. 입력값이 SQL 문장 구조의 일부가 되기 때문. `?` 와 `PreparedStatement` 로 값을 데이터로만 넘긴다
3. 이미 확정되어 남는다. 함께 취소하려면 자동 커밋을 끄고 하나의 트랜잭션으로 묶어야 한다
4. `JOIN` 은 양쪽에 짝이 있는 행만, `LEFT JOIN` 은 왼쪽 행을 모두 남기고 짝이 없으면 빈 값으로 채운다

</details>

## 체크리스트

- [ ] `WHERE` 없는 `UPDATE`/`DELETE` 가 왜 위험한지 직접 확인했다
- [ ] 문자열 이어 붙이기 SQL 이 뚫리는 것을 직접 재현했다
- [ ] `UserDao` 테스트 11개를 모두 통과했다
- [ ] 트랜잭션의 commit/rollback 이 필요한 이유를 내 말로 설명할 수 있다
