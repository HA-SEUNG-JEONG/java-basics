# 5주차. DB·JPA 개념: 테이블, 트랜잭션, 쿼리 수

**예상 시간:** 6~8시간 (하루 1시간 세션 8회) | **목표:** SQL 기본(`SELECT`/`WHERE`/`JOIN`)을 읽고, 엔티티가 테이블이 되는 모습과 메서드 이름이 SQL 이 되는 모습을 설명한다. 트랜잭션이 왜 필요한지, N+1 이 프론트에서 "응답 지연" 으로 어떻게 보이는지, 인덱스가 무엇을 바꾸는지 감을 잡는다. 마지막의 **마일스톤 체크** 에서 백엔드 PR 을 읽고 요청·응답·테이블 영향을 설명한다.

> 4주에서 요청 한 건이 컨트롤러 → 서비스 → 리포지토리로 흘러가는 것을 봤습니다. 이번 주는 그 맨 끝, 리포지토리 아래의 DB 를 봅니다. 프론트 개발자가 DB 를 직접 만질 일은 드물지만, 409(제약 위반), 500(트랜잭션 오류), "목록이 길어질수록 느려짐" 같은 증상의 원인이 모두 여기에 있습니다.
>
> 준비물: JDK 21, `curl`, `jq`, `sqlite3`(macOS 기본 설치, `sqlite3 --version` 으로 확인). 참조 API(`../labs/backend/week25`)는 H2 라는 **메모리 DB** 를 쓰므로 DB 를 따로 설치하지 않습니다. 서버를 껐다 켜면 데이터가 비어 있습니다.
>
> 명령은 별도 언급이 없으면 저장소 루트에서 실행합니다. 서버는 `cd labs/backend && ./gradlew :week25:bootRun` 으로 8080 에 띄웁니다. 코드를 고쳐 보는 실험은 4주 2단계에서 만든 복사본 `~/scratch/backend-copy` 에서 합니다(없다면 4주 2단계의 `rsync` 명령으로 만드세요). 이 문서에서 실행 결과로 적은 값은 직접 실행해 얻은 것이고, 시각·소요 시간은 실행할 때마다 다릅니다.

## 하루 1시간으로 나누면

이 주차는 4단계가 선택이라서 하루 1시간씩 8번의 세션으로 나눕니다. 한 세션은 50~70분이고, 개념 절은 절 번호 단위로, 마일스톤 체크는 PR 단위로 세션에 배정했습니다.

| 세션 | 할 일 | 끝났을 때 할 수 있어야 하는 것 |
|---|---|---|
| 1 | 1단계(SQL 예측하고 → 실행하고 → 비교하기, 30분)를 하고, 1절(테이블, 행, 열: 프론트 데이터와 비교)과 2절(엔티티 ↔ 테이블)을 읽는다 | SQL 11줄의 결과(특히 `= NULL` 과 `LEFT JOIN`)를 설명하고, 엔티티의 어노테이션에서 열과 제약을 읽을 수 있다 |
| 2 | 2단계(쿼리 수 예측하고 → 실제로 세기, 40분)를 하고, 3절(메서드 이름이 SQL 이 된다)과 4절(트랜잭션)을 읽는다 | 리포지토리 메서드 이름에서 SQL 을 대략 읽고, 트랜잭션이 왜 필요한지 말할 수 있다 |
| 3 | 3단계(일부러 망가뜨리기: `@EntityGraph` 한 줄 지우기, 40분)를 하고, 5절(쿼리 수가 곧 응답 시간이다: N+1)과 6절(제약이 오류가 되는 길)을 읽는다 | `@EntityGraph` 를 지우면 쿼리 횟수가 2에서 6으로 늘어나는 것을 보여 주고, N+1 이 프론트에서 항목 수에 비례하는 대기 시간으로 보이는 이유를 설명할 수 있다 |
| 4 | 4단계(실제 테이블 들여다보기, 선택, 30분)를 하고, 7절(인덱스 감각)을 읽는다 (압축 코스에서는 건너뜀) | 인덱스 유무에 따른 `EXPLAIN` 의 차이를 설명할 수 있다 |
| 5 | 마일스톤 체크의 PR 1("회원에 이메일 추가")을 한다. 풀이를 보기 전에 네 가지를 서면으로 완성한다. 60~70분 안에 못 끝나면 같은 단계에서 이어서 한다 | PR 1 에 대해 요청·응답·테이블 영향·프론트에서 깨지는 곳·작성자에게 물을 질문을 서면으로 작성할 수 있다 |
| 6 | 마일스톤 체크의 PR 2("불필요해 보이는 어노테이션과 주석 정리")를 하고, 통과 기준을 확인한다 | PR 2 에 대한 서면 설명을 쓰고, 통과 기준의 항목을 모두 채울 수 있다 |
| 7 | 연습문제와 퀴즈를 푼다 (압축 코스에서는 건너뜀) | `LEFT JOIN` 과 `JOIN` 의 차이를 화면 하나로 설명하고, `Loan.java` 의 `optional = false` 가 DDL 의 어느 부분에 대응하는지 말할 수 있다 |
| 8 | 체크리스트의 각 항목을 내 말로 설명해 보고, 막히는 항목은 해당 절을 다시 읽은 뒤 학습 노트를 정리한다 | 백엔드 PR 을 읽고 요청·응답·테이블 영향을 설명할 수 있다 |

세션을 시작하기 전에 이전 세션의 마지막 결과를 1~2분 훑어봅니다. 막히면 아래 해당 개념 절을 사전처럼 펼쳐 보세요.

## 🖐 먼저 손으로 (읽기 전에 하세요)

> 이 주차의 핵심은 아래 단계를 **직접 해 보면서** 깨닫는 것입니다. 아래쪽 설명 문서는 먼저 읽지 않아도 됩니다. 막혔을 때 사전처럼 펼쳐 보세요.

### 1단계. SQL 예측하고 → 실행하고 → 비교하기 (30분)

아래 스크립트를 `predict05.sql` 로 **한 글자씩 타이핑**합니다. 앞의 6줄은 도서관 API 의 세 테이블(`book`, `members`, `loan`)과 데이터를 만드는 부분입니다. 그 아래 `SELECT`/`UPDATE`/`INSERT` 각 줄의 출력을 **실행 전에** 노트에 적으세요. 여러 줄이 나오는 쿼리는 줄 순서까지 적습니다. 실행해서 비교하고, 틀린 줄에 ★를 치고 왜 그렇게 나왔는지 한 문장으로 적습니다.

```sql
CREATE TABLE book (id INTEGER PRIMARY KEY, isbn TEXT NOT NULL UNIQUE, title TEXT NOT NULL, author TEXT NOT NULL);
CREATE TABLE members (id INTEGER PRIMARY KEY, name TEXT NOT NULL);
CREATE TABLE loan (id INTEGER PRIMARY KEY, book_id INTEGER NOT NULL REFERENCES book(id), member_id INTEGER NOT NULL REFERENCES members(id), loaned_at TEXT NOT NULL, returned_at TEXT);
INSERT INTO book VALUES (1, '111', '이펙티브 자바', '조슈아 블로크'), (2, '222', 'Clean Code', '로버트 마틴'), (3, '333', 'TDD', '켄트 벡'), (4, '444', '리팩터링', '마틴 파울러');
INSERT INTO members VALUES (1, '하승'), (2, '민지'), (3, '준호');
INSERT INTO loan VALUES (1, 1, 1, '2026-10-01', '2026-10-05'), (2, 2, 1, '2026-10-02', NULL), (3, 3, 2, '2026-10-03', NULL);

SELECT title FROM book WHERE author LIKE '%마틴%' ORDER BY id;
SELECT COUNT(*) FROM loan WHERE returned_at IS NULL;
SELECT COUNT(*) FROM loan WHERE returned_at = NULL;
SELECT m.name, b.title FROM loan l JOIN members m ON m.id = l.member_id JOIN book b ON b.id = l.book_id WHERE l.returned_at IS NULL ORDER BY l.id;
SELECT m.name, COUNT(l.id) FROM members m LEFT JOIN loan l ON l.member_id = m.id AND l.returned_at IS NULL GROUP BY m.id ORDER BY m.id;
SELECT m.name, COUNT(l.id) FROM members m JOIN loan l ON l.member_id = m.id WHERE l.returned_at IS NULL GROUP BY m.id ORDER BY m.id;
SELECT title FROM book WHERE id NOT IN (SELECT book_id FROM loan WHERE returned_at IS NULL) ORDER BY id;
UPDATE loan SET returned_at = '2026-10-10' WHERE member_id = 1;
SELECT COUNT(*) FROM loan WHERE returned_at IS NULL;
INSERT INTO book VALUES (5, '111', '다른 책', '누군가');
SELECT COUNT(*) FROM book;
```

```bash
sqlite3 :memory: < predict05.sql 2>&1
```

<details>
<summary>실행 결과 (예측을 적은 뒤에 여세요)</summary>

```text
Clean Code
리팩터링
2
0
하승|Clean Code
민지|TDD
하승|1
민지|1
준호|0
하승|1
민지|1
이펙티브 자바
리팩터링
1
Error near line 17: UNIQUE constraint failed: book.isbn
4
```

- 첫 쿼리: 저자 이름에 `마틴` 이 들어간 책은 `로버트 마틴`(Clean Code)과 `마틴 파울러`(리팩터링) 둘입니다.
- `IS NULL` 은 2, `= NULL` 은 **0** 입니다. SQL 에서 `NULL` 은 값이 아니라 "모름" 이어서 `= NULL` 은 어떤 행에도 참이 아닙니다. 반납 전(`returned_at` 이 비어 있음)을 찾는 코드는 반드시 `IS NULL` 입니다. 참조 API 의 메서드 이름 `...ReturnedAtIsNull` 이 이것입니다.
- `JOIN` 은 대출 두 건(반납 전)에 대해 회원 이름과 책 제목을 이어 붙입니다. 외래 키(`member_id`, `book_id`)로 테이블을 잇는 것이 `JOIN` 입니다.
- `LEFT JOIN` 은 대출이 없는 `준호` 도 `0` 으로 남기고, 그냥 `JOIN` 은 `준호` 를 **빼 버립니다.** "대출이 0권인 회원도 목록에 보여야 한다" 면 `LEFT JOIN` 입니다.
- `NOT IN (...)` 은 지금 대출 중이 아닌 책(반납된 `이펙티브 자바`, 한 번도 안 빌린 `리팩터링`)입니다.
- `UPDATE ... WHERE member_id = 1` 은 회원 1 의 **모든** 대출 행을 바꿉니다. 이미 반납되어 있던 1번 대출의 반납일도 `2026-10-05` 에서 `2026-10-10` 으로 덮어써졌습니다. 결과적으로 반납 전 건수는 `1`(회원 2 의 것)입니다. `WHERE` 가 느슨하면 의도하지 않은 행이 바뀝니다.
- 중복 `isbn` 의 `INSERT` 는 `UNIQUE constraint failed` 로 거부되었고 책은 여전히 4권입니다. 참조 API 의 `409` 와 `DataIntegrityViolationException` 이 이 DB 오류에서 옵니다(6절).

</details>

### 2단계. 쿼리 수 예측하고 → 실제로 세기 (40분)

참조 API 는 요청 하나가 **SQL 을 몇 번** 실행할까요? 아래 요청마다 예측을 적으세요. 서버를 새로 켠 직후, 위에서 아래로 차례로 보낸다고 가정합니다. (힌트: 4주에서 본 `LibraryService.lend` 의 검사 4가지와 저장 1번)

| # | 요청 | 내 예측 (SQL 횟수) |
|---|------|------|
| 1 | `POST /books` (새 `isbn`) | |
| 2 | `POST /books` (이미 있는 `isbn`) | |
| 3 | `POST /books` (`isbn` 이 빈 문자열) | |
| 4 | `POST /members` | |
| 5 | `GET /books` | |
| 6 | `GET /books?keyword=java` | |
| 7 | `GET /books/1` | |
| 8 | `POST /loans` (정상) | |
| 9 | `GET /members/1/loans` | |
| 10 | `POST /loans/1/return` | |

터미널 A 에서 서버를 **SQL 로그를 켜고** 띄웁니다. `show-sql` 은 서버가 실행하는 SQL 을 `Hibernate:` 로 시작하는 줄로 터미널에 찍는 설정이고, 명령줄 인자로만 켜므로 설정 파일은 바뀌지 않습니다.

```bash
mkdir -p ~/scratch
cd labs/backend
./gradlew :week25:bootRun --args='--spring.jpa.show-sql=true' 2>&1 | tee ~/scratch/server.log
```

서버가 뜨면 처음 몇 줄은 `Hibernate: drop table ...`, `Hibernate: create table ...` 입니다. 2절에서 읽을 테이블 정의입니다. 터미널 B 에서 요청을 보내고, 요청 전후의 `Hibernate:` 줄 수 차이를 셉니다.

```bash
B=localhost:8080
H='Content-Type: application/json'
LOG=~/scratch/server.log
n() { grep -c 'Hibernate:' "$LOG"; }
s() { local a=$(n); local label=$1; shift; curl -s -o /dev/null "$@"; sleep 0.3; echo "$label -> $(( $(n) - a ))"; }
curl -s -c ~/scratch/cj.txt -H "$H" -d '{"username":"demo","password":"demo1234"}' $B/auth/login > /dev/null
s "POST /books (새 isbn)"        -b ~/scratch/cj.txt -H "$H" -d '{"isbn":"111","title":"이펙티브 자바","author":"조슈아 블로크"}' $B/books
s "POST /books (중복 isbn)"      -b ~/scratch/cj.txt -H "$H" -d '{"isbn":"111","title":"다른 책","author":"누군가"}' $B/books
s "POST /books (isbn 빈 문자열)" -b ~/scratch/cj.txt -H "$H" -d '{"isbn":"","title":"x","author":"y"}' $B/books
s "POST /members"                -b ~/scratch/cj.txt -H "$H" -d '{"name":"하승"}' $B/members
s "GET /books"                   $B/books
s "GET /books?keyword=java"      "$B/books?keyword=java"
s "GET /books/1"                 $B/books/1
s "POST /loans"                  -b ~/scratch/cj.txt -H "$H" -d '{"memberId":1,"bookId":1}' $B/loans
s "GET /members/1/loans"         $B/members/1/loans
s "POST /loans/1/return"         -b ~/scratch/cj.txt -X POST $B/loans/1/return
```

<details>
<summary>실행 결과 (예측을 적은 뒤에 여세요)</summary>

```text
POST /books (새 isbn) -> 2
POST /books (중복 isbn) -> 1
POST /books (isbn 빈 문자열) -> 0
POST /members -> 1
GET /books -> 1
GET /books?keyword=java -> 1
GET /books/1 -> 1
POST /loans -> 5
GET /members/1/loans -> 2
POST /loans/1/return -> 4
```

서버 터미널에서 요청별로 실제 실행된 SQL 도 확인하세요. 아래는 이 서버가 찍은 줄입니다(긴 줄은 앞부분만).

- `POST /books` 는 2번: `select ... from book where isbn=?` 로 중복을 확인하고, 없으면 `insert into book ...`. 중복이면 `select` 한 번으로 끝나고 409 입니다.
- 빈 `isbn` 은 **0번** 입니다. `@Valid` 검증이 컨트롤러 메서드에 들어오기 전에 400 으로 막기 때문에 DB 까지 가지 않습니다.
- `POST /loans` 는 5번: 회원 조회, 책 조회, 책이 대출 중인지 확인, 회원의 대출 건수 확인, 그리고 `insert into loan`. 4주 3단계에서 읽은 규칙 4가지 + 저장 1번과 정확히 같습니다.
- `GET /members/1/loans` 는 2번: 회원이 있는지 `select count(*)`, 그리고 대출·책·회원을 `join` 으로 **한 번에** 가져오는 `select` 한 번. 대출이 몇 건이든 2번입니다(5절).
- `POST /loans/1/return` 은 4번: 대출 조회, **책 조회, 회원 조회**, 그리고 `update loan ...`. 응답에 책과 회원 정보를 담으려고 필요할 때마다 따로 가져오는 모습입니다(지연 로딩, 5절).

</details>

### 3단계. 일부러 망가뜨리기: `@EntityGraph` 한 줄 지우기 (40분)

`GET /members/1/loans` 는 2번인데, 어노테이션 한 줄을 지우면 몇 번이 될까요? 먼저 예측을 적고, 4주의 복사본 `~/scratch/backend-copy` 에서 해 봅니다. 8080 의 서버는 끕니다.

1. `~/scratch/backend-copy/week25/src/main/java/library/LoanRepository.java` 에서 `@EntityGraph(attributePaths = {"book", "member"})` 줄을 지웁니다. (위의 `import` 와 주석은 남겨도 컴파일됩니다.)
2. 복사본을 8081 로 SQL 로그를 켜고 띄웁니다.

   ```bash
   cd ~/scratch/backend-copy
   ./gradlew :week25:bootRun --args='--server.port=8081 --spring.jpa.show-sql=true' 2>&1 | tee ~/scratch/server.log
   ```

3. 회원 1명, 책 3권, 대출 3건을 만들고 `GET /members/1/loans` 의 SQL 횟수를 셉니다.

   ```bash
   B=localhost:8081
   H='Content-Type: application/json'
   LOG=~/scratch/server.log
   curl -s -c ~/scratch/cj.txt -H "$H" -d '{"username":"demo","password":"demo1234"}' $B/auth/login > /dev/null
   curl -s -b ~/scratch/cj.txt -H "$H" -d '{"name":"하승"}' $B/members > /dev/null
   for i in 1 2 3; do
     curl -s -b ~/scratch/cj.txt -H "$H" -d "{\"isbn\":\"i$i\",\"title\":\"책 $i\",\"author\":\"저자\"}" $B/books > /dev/null
     curl -s -b ~/scratch/cj.txt -H "$H" -d "{\"memberId\":1,\"bookId\":$i}" $B/loans > /dev/null
   done
   sleep 1; a=$(grep -c 'Hibernate:' "$LOG"); curl -s $B/members/1/loans | jq 'length'; sleep 1; echo "SQL $(( $(grep -c 'Hibernate:' "$LOG") - a ))번"
   ```

   출력은 `3` 과 `SQL 6번` 이어야 합니다. (`@EntityGraph` 가 있는 원본이라면 `SQL 2번` 입니다.)

4. 원복합니다: `cp labs/backend/week25/src/main/java/library/LoanRepository.java ~/scratch/backend-copy/week25/src/main/java/library/`

<details>
<summary>실제 결과 (예측을 적은 뒤에 여세요)</summary>

`@EntityGraph` 가 있을 때 2번이던 것이 **6번** 이 됩니다. (이 문서를 쓰면서 같은 변경을 가한 복사본으로 확인한 값입니다.) 서버가 찍은 SQL 은 이렇게 달라집니다.

```text
Hibernate: select count(*) from members m1_0 where m1_0.id=?
Hibernate: select l1_0.id,l1_0.book_id,l1_0.loaned_at,l1_0.member_id,l1_0.returned_at from loan l1_0 join memb...
Hibernate: select b1_0.id,b1_0.author,b1_0.isbn,b1_0.title from book b1_0 where b1_0.id=?
Hibernate: select m1_0.id,m1_0.name from members m1_0 where m1_0.id=?
Hibernate: select b1_0.id,b1_0.author,b1_0.isbn,b1_0.title from book b1_0 where b1_0.id=?
Hibernate: select b1_0.id,b1_0.author,b1_0.isbn,b1_0.title from book b1_0 where b1_0.id=?
```

대출 목록을 가져온 뒤, 응답을 만들면서 **책을 한 권마다 따로**(`where b1_0.id=?` 가 3번) 조회하고, 회원은 같은 사람이라 한 번만 조회했습니다. 응답 JSON 은 `@EntityGraph` 가 있을 때와 **완전히 같았습니다.** 눈으로 보는 결과는 그대로인데 DB 로 가는 횟수만 늘었습니다. 이것이 N+1 문제이고 5절에서 정리합니다.

참조 API 는 이 실수를 막는 테스트를 이미 갖고 있습니다. 같은 변경을 가한 복사본에서 `./gradlew :week25:test` 를 돌리면 31개 중 아래 둘이 실패합니다.

```text
LibraryRepositoryTest > activeLoans_fetchBookAndMemberInOneQuery() FAILED
LibraryApiTest > memberLoans_queryCountDoesNotGrowWithLoans() FAILED
```

</details>

### 4단계. 실제 테이블 들여다보기 (선택, 30분)

메모리 DB 는 서버가 꺼지면 사라져서 안을 볼 수 없습니다. 이번에는 DB 를 **파일에 저장하도록** 실행 인자를 바꿔서 서버를 띄우고, 서버를 끈 뒤 SQL 로 직접 조회합니다. 8080 으로 띄웁니다. 데이터는 `~/scratch/library-db.mv.db` 파일에 저장됩니다.

```bash
cd labs/backend
./gradlew :week25:bootRun --args="--spring.datasource.url=jdbc:h2:file:$HOME/scratch/library-db --spring.jpa.hibernate.ddl-auto=create"
```

`ddl-auto=create` 는 시작할 때 테이블을 새로 만들라는 뜻입니다(파일 DB 는 이것이 없으면 테이블을 만들지 않아 500 이 납니다). 다른 터미널에서 책 4권, 회원 3명, 대출 3건을 만들고 하나를 반납합니다.

```bash
B=localhost:8080; H='Content-Type: application/json'; CJ=~/scratch/cj.txt
curl -s -c $CJ -H "$H" -d '{"username":"demo","password":"demo1234"}' $B/auth/login > /dev/null
for t in '111|이펙티브 자바|조슈아 블로크' '222|Clean Code|로버트 마틴' '333|TDD|켄트 벡' '444|리팩터링|마틴 파울러'; do
  IFS='|' read -r i ti a <<< "$t"
  curl -s -b $CJ -H "$H" -d "{\"isbn\":\"$i\",\"title\":\"$ti\",\"author\":\"$a\"}" $B/books > /dev/null
done
for m in 하승 민지 준호; do curl -s -b $CJ -H "$H" -d "{\"name\":\"$m\"}" $B/members > /dev/null; done
for p in '1 1' '1 2' '2 3'; do
  read -r mid bid <<< "$p"
  curl -s -b $CJ -H "$H" -d "{\"memberId\":$mid,\"bookId\":$bid}" $B/loans > /dev/null
done
curl -s -b $CJ -X POST $B/loans/1/return > /dev/null
```

서버를 `Ctrl+C` 로 끈 뒤, H2 가 들어 있는 jar 를 Gradle 캐시에서 찾아 `real.sql` 을 실행합니다.

```bash
cat > ~/scratch/real.sql <<'EOF'
SELECT id, isbn, title FROM book ORDER BY id;
SELECT id, name FROM members ORDER BY id;
SELECT id, book_id, member_id, returned_at IS NULL AS on_loan FROM loan ORDER BY id;
SELECT m.name, b.title FROM loan l JOIN members m ON m.id = l.member_id JOIN book b ON b.id = l.book_id WHERE l.returned_at IS NULL ORDER BY l.id;
SELECT m.name, COUNT(l.id) AS active FROM members m LEFT JOIN loan l ON l.member_id = m.id AND l.returned_at IS NULL GROUP BY m.id, m.name ORDER BY m.id;
EOF
H2=$(find ~/.gradle/caches -name 'h2-2.3.232.jar' | head -1)
java -cp "$H2" org.h2.tools.RunScript -url "jdbc:h2:file:$HOME/scratch/library-db" -user "" -password "" -script ~/scratch/real.sql -showResults
```

<details>
<summary>실행 결과</summary>

```text
SELECT id, isbn, title FROM book ORDER BY id;
--> 1 111 이펙티브 자바
--> 2 222 Clean Code
--> 3 333 TDD
--> 4 444 리팩터링
;
SELECT id, name FROM members ORDER BY id;
--> 1 하승
--> 2 민지
--> 3 준호
;
SELECT id, book_id, member_id, returned_at IS NULL AS on_loan FROM loan ORDER BY id;
--> 1 1 1 FALSE
--> 2 2 1 TRUE
--> 3 3 2 TRUE
;
SELECT m.name, b.title FROM loan l JOIN members m ON m.id = l.member_id JOIN book b ON b.id = l.book_id WHERE l.returned_at IS NULL ORDER BY l.id;
--> 하승 Clean Code
--> 민지 TDD
;
SELECT m.name, COUNT(l.id) AS active FROM members m LEFT JOIN loan l ON l.member_id = m.id AND l.returned_at IS NULL GROUP BY m.id, m.name ORDER BY m.id;
--> 하승 1
--> 민지 1
--> 준호 0
;
```

앞의 SQL 연습과 같은 모양의 쿼리가 **실제 API 가 만든 데이터** 에서 같은 모양으로 답합니다. 대출 1번만 `ON_LOAN = FALSE`(반납됨)입니다. 이 결과를 `GET /members/1/loans` 의 응답(하승의 반납 전 대출은 `Clean Code` 한 권)과 비교해 보세요.

</details>

---

## 1. 테이블, 행, 열: 프론트 데이터와 비교

| DB 개념 | 설명 | 프론트에서 비슷한 것 |
|---|---|---|
| 테이블 | 같은 모양의 행들의 모음 | 같은 타입의 객체 배열 `Book[]` |
| 행(row) | 테이블의 한 줄 | 배열의 객체 하나 |
| 열(column) | 모든 행이 가진 항목과 그 타입 | 객체 타입의 필드 |
| 기본 키(PK) | 행을 하나로 특정하는 값 (`id`) | 객체의 `id` |
| 외래 키(FK) | 다른 테이블의 PK 를 가리키는 열 (`loan.book_id` → `book.id`) | 다른 객체의 `id` 를 들고 있는 필드 (정규화된 상태) |
| `JOIN` | 외래 키로 두 테이블을 이어 붙여 조회 | 두 배열을 `id` 로 `find` 해서 합치는 코드 |
| `NOT NULL` / `UNIQUE` | 열에 거는 제약. 어기면 DB 가 거부 | 스키마 검증. 단, 우회할 수 없는 최종 관문 |
| 인덱스 | 특정 열로 빨리 찾게 하는 색인 | `Map` 으로 `id` 를 키로 만들어 두는 것 |

SQL 의 핵심은 선언적이라는 점입니다. "어떻게 찾을지" 가 아니라 "어떤 행을 원하는지" 를 적으면 DB 가 방법을 정합니다. 가장 자주 읽게 될 네 문장은 `SELECT`(조회), `INSERT`(추가), `UPDATE`(수정), `DELETE`(삭제)이고, **`WHERE` 가 빠지거나 느슨하면 의도하지 않은 행까지 대상이 됩니다**(1단계의 `UPDATE`).

SQL 에서 자주 실수하는 곳을 SQLite 에서 직접 해 보세요. 1단계의 앞 6줄(테이블과 데이터)을 `base05.sql` 로 저장하고, 그 위에 아래를 `brk05.sql` 로 만들어 실행합니다.

```sql
.read base05.sql
PRAGMA foreign_keys = ON;
INSERT INTO loan VALUES (9, 99, 1, '2026-10-01', NULL);
DELETE FROM book WHERE id = 1;
INSERT INTO book (id, isbn, title) VALUES (6, '666', '제목만');
SELECT * FROM userz;
UPDATE loan SET returned_at = '2026-10-10';
SELECT COUNT(*) FROM loan WHERE returned_at IS NULL;
```

```bash
sqlite3 :memory: < brk05.sql 2>&1
```

```text
Error near line 3: FOREIGN KEY constraint failed
Error near line 4: FOREIGN KEY constraint failed
Error near line 5: NOT NULL constraint failed: book.author
Parse error near line 6: no such table: userz
0
```

- 3번째 줄: 없는 책(99)에 대한 대출은 외래 키가 막습니다. 4번째 줄: 대출이 걸려 있는 책은 지울 수 없습니다. (SQLite 는 `PRAGMA foreign_keys = ON` 을 켜야 외래 키를 검사합니다. 참조 API 가 쓰는 H2 는 기본으로 검사합니다.)
- `author` 를 빼고 넣으면 `NOT NULL` 제약이 막습니다. 참조 API 에서 이것은 `@NotBlank` 검증이 먼저 400 으로 막고, 그 검증을 통과해도 DB 가 마지막으로 막는 이중 안전장치입니다.
- 오타 난 테이블 이름은 **실행해야** 알 수 있습니다. SQL 은 컴파일 단계의 타입 검사가 없습니다.
- `WHERE` 없는 `UPDATE` 는 모든 대출을 반납 처리했고, 반납 전 건수는 `0` 입니다.

## 2. 엔티티 ↔ 테이블

3주에서 읽은 `Book.java` 의 어노테이션이 곧 테이블 정의입니다. 4주 2단계에서 서버를 띄웠을 때 맨 앞에 찍힌 `Hibernate: create table ...` 이 그 결과입니다. (`--spring.jpa.show-sql=true` 로 띄웠을 때 실제 출력)

```text
create table book (id bigint generated by default as identity, author varchar(255) not null, isbn varchar(255) not null unique, title varchar(255) not null, primary key (id))
create table loan (book_id bigint not null, id bigint generated by default as identity, loaned_at timestamp(6) with time zone not null, member_id bigint not null, returned_at timestamp(6) with time zone, primary key (id))
create table members (id bigint generated by default as identity, name varchar(255) not null, primary key (id))
alter table if exists loan add constraint FK88c0ydlo57pcgp137tntrgqx1 foreign key (book_id) references book
alter table if exists loan add constraint FKic64pdg1lt4sufuld1oxc89e7 foreign key (member_id) references members
```

| 엔티티 코드 | 테이블에서 |
|---|---|
| `@Entity public class Book` | 테이블 `book` |
| `@Table(name = "members")` (`Member`) | 테이블 이름을 `members` 로 지정. 소스의 주석은 "MEMBER 는 SQL 예약어와 겹칠 수 있어 복수형으로" |
| `@Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;` | `id bigint generated by default as identity` + `primary key (id)`. 값은 DB 가 자동으로 매김 |
| `@Column(nullable = false, unique = true) private String isbn;` | `isbn varchar(255) not null unique` |
| `private String title;` + `@Column(nullable = false)` | `title varchar(255) not null` |
| `private Instant returnedAt; // 반납 전에는 null` | `returned_at timestamp(6) with time zone` — `not null` 이 **없는** 열 |
| `@Column(nullable = false) private Instant loanedAt;` | `loaned_at ... not null`. 필드 이름 `loanedAt` 이 열 이름 `loaned_at` 으로 |
| `@ManyToOne(fetch = FetchType.LAZY, optional = false) private Book book;` | `book_id bigint not null` 열 + `foreign key (book_id) references book` |

- **`Instant` 는 시각 열**, `Long`/`long` 은 `bigint`, `String` 은 `varchar(255)` 입니다. 길이 255 라는 기본값은 요청 `record` 에 길이 규칙(`@Size`)이 없다는 점과 만나면 문제가 됩니다. 300자짜리 `title` 로 `POST /books` 를 호출하면 `{"message":"이미 존재하는 데이터와 충돌합니다"} [409]` 가 돌아오고, 서버 로그에는 `Value too long for column "TITLE CHARACTER VARYING(255)"` 가 남습니다. 255자 제목은 201 이었습니다. 중복이 아닌데도 "중복" 처럼 읽히는 응답이므로 PR 리뷰에서 물어볼 만한 지점입니다.
- **`loan` 이 `book_id` 와 `member_id` 를 갖고 있습니다.** 소스 주석 "외래 키(book_id, member_id)를 가진 쪽이 관계의 주인이다" 가 이것입니다. 대출 한 건이 책 한 권과 회원 한 명을 가리키고, 한 책이 여러 번 대출될 수 있는(1:N) 모양입니다.
- **`returned_at` 이 비어 있으면 대출 중** 입니다. 별도의 `status` 열이 없어서, "대출 중인가" 를 알려면 늘 `returned_at IS NULL` 로 조회합니다(`existsByBookIdAndReturnedAtIsNull`).
- 엔티티는 **응답이 아닙니다.** `Loan` 엔티티는 `book` 필드가 엔티티 `Book` 이지만, 응답 `LoanResponse` 는 `BookResponse` 로 바꿔서 내보냅니다. 3주의 "엔티티는 DB 모양, `record` 는 API 모양" 입니다. 엔티티에 열이 추가되어도 응답 `record` 가 안 바뀌면 프론트는 모릅니다(마일스톤 체크의 PR).
- 이 DB 는 서버가 켜질 때 엔티티를 보고 테이블을 **새로 만들고** 꺼지면 사라집니다(`drop table ... create table ...`). 실제 운영 DB 에서는 열을 바꾸려면 기존 행을 어떻게 할지 정해야 하고, 그 방법이 "마이그레이션" 입니다. 이 저장소에는 마이그레이션이 없어서 이 문제가 눈에 안 보일 뿐입니다.

## 3. 메서드 이름이 SQL 이 된다

3주에서 읽은 리포지토리 메서드가 실제로 실행하는 SQL 입니다. 2단계에서 서버가 찍은 값입니다(긴 줄은 줄여 적음).

| 메서드 (`BookRepository`/`LoanRepository`/`MemberRepository`) | 실행되는 SQL |
|---|---|
| `existsByIsbn(isbn)` | `select b1_0.id from book b1_0 where b1_0.isbn=? fetch first ? rows only` |
| `findAllByOrderByIdAsc()` | `select b1_0.id,b1_0.author,b1_0.isbn,b1_0.title from book b1_0 order by b1_0.id` |
| `findByTitleContainingIgnoreCaseOrderByIdAsc(k)` | `select ... from book b1_0 where upper(b1_0.title) like upper(?) escape '\' order by b1_0.id` |
| `findById(id)` (책) | `select b1_0.id,b1_0.author,b1_0.isbn,b1_0.title from book b1_0 where b1_0.id=?` |
| `existsByBookIdAndReturnedAtIsNull(bookId)` | `select l1_0.id from loan l1_0 join book b1_0 on b1_0.id=l1_0.book_id where b1_0.id=? and l1_0.returned_at is null fetch first ? rows only` |
| `countByMemberIdAndReturnedAtIsNull(memberId)` | `select count(l1_0.id) from loan l1_0 join members m1_0 on m1_0.id=l1_0.member_id where m1_0.id=? and l1_0.returned_at is null` |
| `save(...)` (새 책) | `insert into book (author,isbn,title,id) values (?,?,?,default)` |

- 이름의 조각이 SQL 의 조각입니다. `ExistsBy` → `select ... where ... 한 행만`, `CountBy` → `count`, `Containing` → `like`, `IgnoreCase` → `upper(...)`, `OrderByIdAsc` → `order by id`, `...IsNull` → `is null`, `BookId` → 외래 키 `book_id` 를 따라가는 `join`.
- **`Containing` 은 `like` 로 바뀝니다.** 값 앞뒤를 모두 와일드카드로 비교하므로(제목에 `keyword` 가 "포함") 7절에서 보듯 인덱스를 못 씁니다. 서버 로그에서 `org.hibernate.orm.jdbc.bind` 를 `trace` 로 켜면(`--logging.level.org.hibernate.orm.jdbc.bind=trace`) `GET /books?keyword=java` 가 `binding parameter (1:VARCHAR) <- [%java%]` 로 값 앞뒤에 `%` 를 붙여 보내는 것을 볼 수 있습니다. `GET /books?keyword=` 가 데이터가 커질수록 느려질 수 있는 이유입니다.
- 쿼리는 항상 `?` 자리표시자로 값을 넘깁니다. 값이 SQL 문장의 일부가 되지 않으므로 사용자 입력으로 SQL 이 바뀌는 공격(SQL 인젝션)을 막습니다.

JDBC 로 이 과정을 직접 쓰던 방식과 JPA 가 대신해 주는 일을 비교하려면 [SQL 과 JDBC](../archive/java-24weeks/week17-jdbc-sql.md) 와 [JPA 와 데이터베이스 연동](../archive/java-24weeks/week23-jpa.md) 을 참고하세요. 그 문서들은 직접 만드는 관점입니다.

## 4. 트랜잭션: 여러 작업을 하나로 묶기

"책을 대출 처리하면서 동시에 다른 기록도 남긴다" 처럼 DB 변경이 여러 개인 작업이 중간에 실패하면, 앞의 변경만 남아 데이터가 어긋납니다. 트랜잭션은 **전부 성공하거나 전부 없던 일** 로 만드는 묶음입니다. SQLite 에서 직접 확인하세요. `tx2.sql` 로 저장합니다.

```sql
.headers on
CREATE TABLE book (id INTEGER PRIMARY KEY, isbn TEXT NOT NULL UNIQUE, title TEXT NOT NULL);
CREATE TABLE loan (id INTEGER PRIMARY KEY, book_id INTEGER NOT NULL REFERENCES book(id), returned_at TEXT);
INSERT INTO book VALUES (1, '111', 'TDD'), (2, '222', 'Clean Code');

BEGIN;
INSERT INTO loan VALUES (1, 1, NULL);
INSERT INTO book VALUES (3, '111', '중복 isbn');
ROLLBACK;
SELECT COUNT(*) AS 롤백한_뒤 FROM loan;

INSERT INTO loan VALUES (1, 1, NULL);
INSERT INTO book VALUES (3, '111', '중복 isbn');
SELECT COUNT(*) AS 묶지_않은_뒤 FROM loan;
```

```bash
sqlite3 :memory: < tx2.sql 2>&1
```

```text
Error near line 8: UNIQUE constraint failed: book.isbn
롤백한_뒤
0
Error near line 13: UNIQUE constraint failed: book.isbn
묶지_않은_뒤
1
```

`BEGIN` 으로 묶은 경우 두 번째 `INSERT` 가 실패해 `ROLLBACK` 하면 첫 번째 `INSERT` 도 취소되어 대출이 0건입니다. 묶지 않으면(기본은 문장마다 즉시 확정) 첫 번째가 이미 확정되어 1건이 남습니다.

참조 API 에서는 `LibraryService` 클래스 위의 `@Transactional` 이 이 일을 합니다. **서비스의 공개 메서드 하나가 시작부터 끝까지 한 트랜잭션** 이고, 메서드 안에서 예외(`RuntimeException`)가 던져지면 그때까지의 DB 변경이 취소됩니다.

```java
@Service
@Transactional
public class LibraryService {
```

- `@Transactional(readOnly = true)` 가 붙은 조회 메서드(`searchBooks`, `getBook`, `activeLoans`)는 "읽기만 한다" 는 표시입니다.
- **`@Transactional` 이 없으면 500 이 납니다.** 4주 2단계 h 에서 직접 확인한 `LazyInitializationException: Could not initialize proxy [library.Book#1] - no session` 이 그것입니다. 엔티티의 `book` 필드는 `FetchType.LAZY` 라서 처음에는 비어 있고, 응답을 만드는 순간 DB 에서 가져오는데 이때 열려 있는 트랜잭션(세션)이 있어야 합니다. `application.properties` 의 `spring.jpa.open-in-view=false` 와 주석 "엔티티의 지연 로딩은 서비스(트랜잭션) 안에서만. 응답 DTO 변환도 서비스가 한다" 가 같은 이야기입니다.
- `markReturned` 로 필드만 바꾸는 `giveBack` 에는 `save(...)` 호출이 없는데도 `update loan ...` 이 실행됩니다(2단계 10번). 트랜잭션 안에서 엔티티가 바뀐 것을 JPA 가 알아채고(변경 감지) 트랜잭션이 끝날 때 반영하기 때문입니다.
- `lend` 는 검사를 모두 끝낸 뒤 마지막에 저장하므로, 한도 초과로 409 가 날 때는 저장 자체가 일어나지 않습니다. 이것을 테스트가 확인합니다(3주 12절의 `lend_fourthBook_conflictsAndSavesNothing`).

### 동시 요청은 트랜잭션만으로 못 막는다

`LibraryService.lend` 안의 주석이 한계를 밝히고 있습니다(4주 3절). "같은 책·같은 회원의 동시 요청은 검사와 저장 사이에서 둘 다 통과할 수 있다." 서비스가 먼저 중복 `isbn` 을 검사해서 409 를 돌려주고, 동시에 도착한 두 요청이 이 검사를 함께 통과하면 **DB 의 `unique` 제약이 마지막으로** 하나를 거부합니다. `ApiExceptionHandler` 가 `DataIntegrityViolationException` 을 409 `이미 존재하는 데이터와 충돌합니다` 로 바꾸는 이유입니다. 프론트가 "등록" 버튼을 연타했을 때 같은 isbn 으로 두 번 요청이 나가는 경우를 떠올리세요. 동시에 나간 두 요청은 한쪽이 201 을 받고 다른 쪽이 `이미 등록된 isbn 입니다: ...` 또는 `이미 존재하는 데이터와 충돌합니다` 중 하나의 409 를 받을 수 있습니다.

## 5. 쿼리 수가 곧 응답 시간이다: N+1

3단계에서 `@EntityGraph` 한 줄을 지우자 쿼리가 2번에서 6번이 되었습니다. 패턴은 이렇습니다.

1. 대출 목록을 가져오는 쿼리 1번.
2. 각 대출의 `book`(그리고 `member`)이 필요할 때마다 따로 쿼리. 대출이 N건이면 N번.

그래서 `1 + N` 이고 "N+1 문제" 라고 부릅니다. JPA 는 관계(`@ManyToOne`)를 **지연 로딩** 으로 두어 필요할 때 가져오는데, 목록을 돌며 매번 필요하게 되면 이렇게 쿼리가 늘어납니다. `LoanRepository.java` 의 주석이 이 문제를 설명합니다.

```java
    /** 책·회원을 같은 쿼리에서 함께 가져온다. 없으면 대출 건수만큼 쿼리가 더 나간다(N+1). */
    @EntityGraph(attributePaths = {"book", "member"})
    List<Loan> findByMemberIdAndReturnedAtIsNullOrderByIdAsc(Long memberId);
```

`@EntityGraph(attributePaths = {"book", "member"})` 는 "이 쿼리에서 `book` 과 `member` 를 같이 가져와라(`join`)" 입니다. 이 한 줄이 쿼리를 `1 + N` 에서 상수 2번으로 줄입니다.

### 쿼리 수가 늘면 어떻게 보이나

참조 API 는 한 회원이 빌릴 수 있는 책을 3권으로 제한해서(`LOAN_LIMIT = 3`) 이 문제가 커지지 않습니다. 쿼리 수가 대출 건수에 비례한다는 것을 보려고, 복사본에서 한도 상수를 `1000` 으로 올리고(`LibraryService.java` 의 `LOAN_LIMIT = 3;` → `1000`) 같은 회원이 N건을 빌린 상태에서 `GET /members/1/loans` 를 호출했습니다. `@EntityGraph` 가 있는 쪽(원본)과 지운 쪽을 비교한 값입니다. 쿼리 횟수는 위 2단계의 세는 방법으로 셉니다(3단계의 `for i in 1 2 3` 을 `for i in $(seq 1 100)` 처럼 바꾸면 N 을 늘릴 수 있습니다). 요청 소요 시간은 `curl -w '%{time_total}'` 값입니다.

| 대출 건수 N | 원본(`@EntityGraph` 있음) SQL 횟수 | 지운 쪽 SQL 횟수 | 지운 쪽의 공식 |
|---|---|---|---|
| 3 | 2 | 6 | N + 3 |
| 100 | 2 | 103 | N + 3 |
| 300 | 2 | 303 | N + 3 |

`N + 3` 은 회원 확인 1번, 대출 목록 1번, 책 N번, 회원 1번(모두 같은 회원이라 한 번)입니다. 응답 JSON 은 두 쪽이 같았습니다(N=3 에서 `loanedAt` 을 뺀 본문을 `diff` 로 비교).

소요 시간은 **정직하게 말하면 거의 차이가 나지 않았습니다.** N=300 에서 8번 반복 호출한 값입니다(1회차는 JVM 이 아직 데워지지 않아 두 쪽 모두 0.16~0.32 초로 느렸습니다).

| 호출 회차 | 4 | 5 | 6 | 7 | 8 |
|---|---|---|---|---|---|
| 원본 (SQL 2번) | 17.6ms | 15.6ms | 12.6ms | 16.1ms | 12.2ms |
| 지운 쪽 (SQL 303번) | 16.5ms | 14.5ms | 15.8ms | 22.5ms | 14.9ms |

이 DB 는 서버와 같은 프로세스 안에 있는 메모리 DB 라서 쿼리 한 번의 비용이 거의 0 이기 때문입니다. 이것이 **N+1 을 개발 중에 못 알아채는 이유** 입니다. 로컬에서는 멀쩡하다가 운영에서 느려집니다. 운영의 DB 는 네트워크 너머에 있어서 쿼리마다 왕복 시간이 더해집니다. 계산 예시입니다(측정이 아니라 가정). 왕복에 1ms 가 든다고 하면 303번은 300ms 이상, 2번은 2ms 입니다.

### 프론트에서 이렇게 보인다

N+1 이 있는 API 를 프론트가 만나면 다음과 같은 증상으로 나타납니다.

- 응답 JSON 의 **크기는 작은데** 브라우저 개발자 도구 Network 탭에서 `Waiting for server response`(서버가 응답을 보내기 시작할 때까지의 시간)가 길다.
- 목록의 항목 수에 **비례해서** 느려진다. 항목이 3건일 때는 빠르고 300건일 때 느리다.
- 같은 데이터를 보여 주는 다른 화면은 빠른데 특정 화면의 API 만 느리다.
- 백엔드의 단위 테스트는 모두 통과하고, 개발 서버에서는 문제가 없다.

PR 리뷰에서 할 수 있는 질문은 이렇습니다. "이 목록 API 는 항목 수가 N 일 때 쿼리가 몇 번 나가나요?", "응답에 중첩된 객체(`book`, `member`)를 같은 쿼리에서 가져오나요?", "쿼리 수를 확인하는 테스트가 있나요?" 참조 API 는 마지막 질문에 이미 답이 있습니다.

```java
    @Test
    void memberLoans_queryCountDoesNotGrowWithLoans() throws Exception {
```

이 테스트(`LibraryApiTest`)는 대출이 1건일 때와 3건일 때의 쿼리 수가 같고 2 이하여야 한다고 단언합니다. 3단계에서 `@EntityGraph` 를 지웠을 때 이 테스트가 실패했습니다.

그리고 한 가지 더. `GET /books` 는 **모든 행을 한 번에** 돌려줍니다(페이지네이션 없음). 책이 수만 권이 되면 쿼리 수가 아니라 쿼리 한 번이 무거워지고 응답 JSON 이 거대해집니다. 이 API 는 "목록은 전부 준다" 는 단순한 계약이라는 점을 기억해 두세요.

## 6. 제약이 오류가 되는 길

DB 제약을 어기면 어떤 응답이 되는지 정리합니다. 프론트의 오류 처리 표(7주)로 이어집니다.

| 어긴 것 | 누가 먼저 막나 | 프론트가 받는 것 |
|---|---|---|
| `@NotBlank` 인 필드가 비었다 | 요청 검증(`@Valid`) | 400 `필드: 공백일 수 없습니다`. **DB 까지 가지 않음** (SQL 0번) |
| 이미 있는 `isbn` | 서비스의 `existsByIsbn` 검사 | 409 `이미 등록된 isbn 입니다: 111`. SQL 1번 |
| 동시 요청이 위 검사를 함께 통과 | DB 의 `unique` 제약 | 409 `이미 존재하는 데이터와 충돌합니다` |
| 없는 회원/책으로 대출 | 서비스의 `orElseThrow` | 404 |
| 같은 책을 이미 대출 중 / 회원이 3권 대출 중 | 서비스의 검사 | 409 |
| 열 길이(255자)를 초과한 `title` | 요청 검증에 길이 규칙이 없어서 DB 가 막음 | 409 `이미 존재하는 데이터와 충돌합니다` (중복이 아닌데도 같은 문장) |

DB 제약은 **최후의 방어선** 이고, 정상 흐름에서는 서비스와 검증이 먼저 의미 있는 메시지로 막습니다. 마지막 줄처럼 아무도 막지 않은 위반이 DB 까지 가면, 처리기가 모든 DB 제약 위반을 한 문장으로 뭉뚱그려 409 로 바꾸기 때문에 **원인과 다른 메시지** 가 나갈 수 있습니다. 프론트가 "409 면 이미 있는 데이터" 라고 가정하면 길이 초과를 중복으로 안내하게 됩니다.

## 7. 인덱스 감각

인덱스는 특정 열의 값으로 행을 빨리 찾게 하는 색인입니다. 책의 색인처럼, 없으면 처음부터 끝까지 읽어야 합니다. SQLite 에서 50만 행으로 직접 확인합니다. `idx.sql` 로 저장하세요. `member_id` 로 반납 전 대출을 찾는 쿼리를 인덱스 없이 / 있이 실행합니다.

```sql
CREATE TABLE loan (id INTEGER PRIMARY KEY, book_id INTEGER NOT NULL, member_id INTEGER NOT NULL, loaned_at TEXT NOT NULL, returned_at TEXT);
WITH RECURSIVE n(i) AS (SELECT 1 UNION ALL SELECT i + 1 FROM n WHERE i < 500000)
INSERT INTO loan SELECT i, i, i % 50000, '2026-10-01', CASE WHEN i % 10 = 0 THEN NULL ELSE '2026-10-05' END FROM n;
SELECT COUNT(*) FROM loan;
.timer on
EXPLAIN QUERY PLAN SELECT * FROM loan WHERE member_id = 770 AND returned_at IS NULL;
SELECT COUNT(*) FROM loan WHERE member_id = 770 AND returned_at IS NULL;
CREATE INDEX idx_loan_member ON loan(member_id);
EXPLAIN QUERY PLAN SELECT * FROM loan WHERE member_id = 770 AND returned_at IS NULL;
SELECT COUNT(*) FROM loan WHERE member_id = 770 AND returned_at IS NULL;
```

```bash
sqlite3 :memory: < idx.sql
```

이 컴퓨터에서 실행한 결과입니다. 시간은 컴퓨터마다 다릅니다.

```text
500000
QUERY PLAN
`--SCAN loan
Run Time: real 0.000037 user 0.000037 sys 0.000002
10
Run Time: real 0.035065 user 0.015860 sys 0.004569
Run Time: real 0.173202 user 0.080585 sys 0.026305
QUERY PLAN
`--SEARCH loan USING INDEX idx_loan_member (member_id=?)
Run Time: real 0.000076 user 0.000073 sys 0.000005
10
Run Time: real 0.000712 user 0.000132 sys 0.000092
```

- `SCAN loan`(전체를 읽음) → `SEARCH loan USING INDEX ...`(색인으로 바로 찾음). 같은 쿼리가 약 35ms 에서 약 0.7ms 로 줄었습니다. 이 차이는 행 수에 비례해서 벌어집니다.
- 인덱스를 만드는 데는 0.17 초가 들었습니다. 인덱스는 공짜가 아니어서, 행을 추가·수정할 때마다 색인도 고쳐야 하고 저장 공간도 씁니다. 모든 열에 걸지 않습니다.
- `EXPLAIN` 은 쿼리가 인덱스를 쓰는지 DB 에 물어보는 명령입니다. 백엔드 개발자가 느린 쿼리를 확인할 때 가장 먼저 쓰는 방법이고, 프론트는 PR 리뷰에서 "이 조건에 인덱스가 있나요?" 라고 물을 수 있습니다.

참조 API 의 H2 에는 어떤 인덱스가 있을까요? 4단계의 파일 DB 에 대해 아래 SQL 을 `~/scratch/index.sql` 로 만들어 실행한 결과입니다.

```sql
SELECT i.TABLE_NAME, i.INDEX_NAME, i.INDEX_TYPE_NAME, c.COLUMN_NAME FROM INFORMATION_SCHEMA.INDEXES i JOIN INFORMATION_SCHEMA.INDEX_COLUMNS c ON c.INDEX_NAME = i.INDEX_NAME AND c.INDEX_SCHEMA = i.INDEX_SCHEMA WHERE i.TABLE_SCHEMA = 'PUBLIC' ORDER BY i.TABLE_NAME, i.INDEX_NAME;
EXPLAIN SELECT * FROM loan WHERE member_id = 1 AND returned_at IS NULL;
EXPLAIN SELECT * FROM book WHERE isbn = '111';
EXPLAIN SELECT * FROM book WHERE title = 'TDD';
```

```text
--> BOOK CONSTRAINT_INDEX_1 UNIQUE INDEX ISBN
--> BOOK PRIMARY_KEY_1 PRIMARY KEY ID
--> LOAN FK88C0YDLO57PCGP137TNTRGQX1_INDEX_2 INDEX BOOK_ID
--> LOAN FKIC64PDG1LT4SUFULD1OXC89E7_INDEX_2 INDEX MEMBER_ID
--> LOAN PRIMARY_KEY_2 PRIMARY KEY ID
--> MEMBERS PRIMARY_KEY_6 PRIMARY KEY ID
```

- 기본 키(`id`), `unique` 열(`isbn`), 외래 키 열(`loan.book_id`, `loan.member_id`)에는 **자동으로** 인덱스가 만들어져 있습니다. 그래서 `isbn` 으로 찾는 쿼리와 `member_id` 로 대출을 찾는 쿼리는 색인을 씁니다(`EXPLAIN` 결과의 `/* PUBLIC.CONSTRAINT_INDEX_1: ISBN = '111' */`, `/* PUBLIC.FKIC64PDG1LT4SUFULD1OXC89E7_INDEX_2: MEMBER_ID = ... */`).
- `title` 로 찾는 쿼리의 계획은 `/* PUBLIC.BOOK.tableScan */`, 즉 전체를 읽습니다. `title` 에는 인덱스가 없고, `keyword` 검색은 `like` 로 앞뒤 와일드카드라서 인덱스를 만들어도 쓰지 못합니다. 제목 검색은 책이 많아질수록 느려집니다.
- 외래 키 열에 인덱스를 자동으로 만들어 주는지는 DB 제품마다 다릅니다. 이 결과는 H2 에서 확인한 것입니다. 운영 DB 에서는 `EXPLAIN` 으로 직접 확인해야 합니다.

프론트가 알아 두면 좋은 인덱스 감각 세 가지입니다. (1) "이 조건으로 검색하는 화면" 을 요청하면 그 열에 인덱스가 있는지가 백엔드의 일이 됩니다. (2) 정렬·필터 조건을 하나 더 얹는 요청은 쿼리 계획을 바꿀 수 있습니다. (3) 부분 일치 검색(`keyword`)은 인덱스로 해결되지 않으니, 데이터가 커지면 검색 전용 방법(전문 검색)이 필요하다는 이야기가 나올 수 있습니다.

---

## 마일스톤 체크: 백엔드 PR 을 읽고 요청·응답·테이블 영향 설명하기

여기까지 오면 3~5주의 목표인 "백엔드 코드를 읽고 PR 의 영향을 말한다" 를 확인할 수 있습니다. 아래 PR 두 개는 **실제 PR 이 아니라 참조 API 의 소스에 대고 이 문서를 위해 만든 가상의 diff** 입니다. 4주 8절의 절차(층 분류 → 테스트 읽기 → 변화표 → 프론트 타입 영향 → 질문)와 이번 주에 배운 테이블·쿼리 수 관점으로, 각 PR 에 대해 **아래 네 가지를 서면으로** 작성하세요. 60~90분을 쓰고, 풀이를 보기 전에 완성합니다.

1. 요청이 어떻게 바뀌는가 (경로, 파라미터, 본문 필드, 필수 여부, 새로 생기는 오류)
2. 응답이 어떻게 바뀌는가 (필드, 상태 코드, 누구에게 보이는가)
3. 테이블에 어떤 영향이 있는가 (열, 제약, 기존 데이터, 인덱스, 쿼리 수)
4. 프론트 타입과 화면에 어떤 영향이 있고, 작성자에게 물을 질문 3개 이상

### PR 1. "회원에 이메일 추가"

이 diff 에는 테스트 파일의 수정도 함께 들어 있습니다. 4주에서 배운 대로 테스트 diff 부터 읽으세요.

```diff
--- a/src/main/java/library/Dto.java
+++ b/src/main/java/library/Dto.java
@@ -16,7 +16,7 @@
 
     public record BookRequest(@NotBlank String isbn, @NotBlank String title, @NotBlank String author) {}
 
-    public record MemberRequest(@NotBlank String name) {}
+    public record MemberRequest(@NotBlank String name, @NotBlank String email) {}
 
     public record LoanRequest(@NotNull Long memberId, @NotNull Long bookId) {}
 
@@ -34,9 +34,10 @@
 
     public record MemberResponse(
             @Schema(requiredMode = REQUIRED) long id,
-            @Schema(requiredMode = REQUIRED) String name) {
+            @Schema(requiredMode = REQUIRED) String name,
+            @Schema(requiredMode = REQUIRED) String email) {
         static MemberResponse from(Member m) {
-            return new MemberResponse(m.getId(), m.getName());
+            return new MemberResponse(m.getId(), m.getName(), m.getEmail());
         }
     }
 
--- a/src/main/java/library/Member.java
+++ b/src/main/java/library/Member.java
@@ -12,12 +12,17 @@
     @Column(nullable = false)
     private String name;
 
+    @Column(nullable = false)
+    private String email;
+
     protected Member() {}
 
-    public Member(String name) {
+    public Member(String name, String email) {
         this.name = name;
+        this.email = email;
     }
 
     public Long getId() { return id; }
     public String getName() { return name; }
+    public String getEmail() { return email; }
 }
--- a/src/main/java/library/LibraryService.java
+++ b/src/main/java/library/LibraryService.java
@@ -49,7 +49,7 @@
     }
 
     public MemberResponse createMember(MemberRequest req) {
-        return MemberResponse.from(members.save(new Member(req.name())));
+        return MemberResponse.from(members.save(new Member(req.name(), req.email())));
     }
 
     public LoanResponse lend(long memberId, long bookId) {
--- a/src/test/java/library/LibraryApiTest.java
+++ b/src/test/java/library/LibraryApiTest.java
@@ -48,7 +48,7 @@
     }
 
     long member(String name) throws Exception {
-        return id(postJson("/members", "{\"name\":\"" + name + "\"}").andExpect(status().isCreated()));
+        return id(postJson("/members", "{\"name\":\"" + name + "\",\"email\":\"" + name + "@example.com\"}").andExpect(status().isCreated()));
     }
 
     ResultActions lend(long memberId, long bookId) throws Exception {
@@ -111,13 +111,16 @@
 
     @Test
     void createMember_201AndBlank400() throws Exception {
-        postJson("/members", "{\"name\":\"하승\"}")
+        postJson("/members", "{\"name\":\"하승\",\"email\":\"h@example.com\"}")
                 .andExpect(status().isCreated())
                 .andExpect(header().string("Location", matchesPattern(".*/members/\\d+")))
                 .andExpect(jsonPath("$.name").value("하승"));
-        postJson("/members", "{\"name\":\" \"}")
+        postJson("/members", "{\"name\":\" \",\"email\":\"h@example.com\"}")
                 .andExpect(status().isBadRequest())
                 .andExpect(jsonPath("$.message", startsWith("name: ")));
+        postJson("/members", "{\"name\":\"민지\"}")
+                .andExpect(status().isBadRequest())
+                .andExpect(jsonPath("$.message", startsWith("email: ")));
     }
 
     // ---- 대출·반납 ----
@@ -214,7 +217,7 @@
 
     @Test
     void memberLoans_queryCountDoesNotGrowWithLoans() throws Exception {
-        Member m = new Member("하승");
+        Member m = new Member("하승", "h@example.com");
         em.persist(m);
         Book a = new Book("q1", "a", "x");
         em.persist(a);
--- a/src/test/java/library/LibraryRepositoryTest.java
+++ b/src/test/java/library/LibraryRepositoryTest.java
@@ -30,7 +30,7 @@
         effective = em.persist(new Book("111", "이펙티브 자바", "조슈아 블로크"));
         clean = em.persist(new Book("222", "Clean Code", "로버트 마틴"));
         tdd = em.persist(new Book("333", "TDD", "켄트 벡"));
-        haseung = em.persist(new Member("하승"));
+        haseung = em.persist(new Member("하승", "h@example.com"));
         em.persist(new Loan(effective, haseung, Instant.parse("2026-01-01T00:00:00Z")));
         em.persist(new Loan(clean, haseung, Instant.parse("2026-01-02T00:00:00Z")));
         Loan returned = em.persist(new Loan(tdd, haseung, Instant.parse("2026-01-03T00:00:00Z")));
--- a/src/test/java/library/LibraryServiceTest.java
+++ b/src/test/java/library/LibraryServiceTest.java
@@ -39,7 +39,7 @@
     }
 
     long newMember(String name) {
-        return service.createMember(new MemberRequest(name)).id();
+        return service.createMember(new MemberRequest(name, name + "@example.com")).id();
     }
 
     @Test
--- a/src/test/java/library/SecurityAndDocsTest.java
+++ b/src/test/java/library/SecurityAndDocsTest.java
@@ -52,7 +52,7 @@
                 .andReturn().getRequest().getSession();
 
         mvc.perform(get("/auth/me").session(session)).andExpect(status().isOk()).andExpect(jsonPath("$.username").value("demo"));
-        mvc.perform(post("/members").session(session).contentType(APPLICATION_JSON).content("{\"name\":\"하승\"}"))
+        mvc.perform(post("/members").session(session).contentType(APPLICATION_JSON).content("{\"name\":\"하승\",\"email\":\"h@example.com\"}"))
                 .andExpect(status().isCreated());
 
         mvc.perform(post("/auth/logout").session(session)).andExpect(status().isNoContent());
```

<details>
<summary>풀이 (서면으로 작성한 뒤에 여세요)</summary>

이 diff 를 복사본에 적용해 서버를 띄우고 확인한 값입니다.

**1. 요청**

- `POST /members` 의 본문에 `email` 이 **필수** 로 추가됩니다(`@NotBlank`). 기존처럼 `{"name":"하승"}` 만 보내면 `{"message":"email: 공백일 수 없습니다"} [400]` 입니다. 지금 프론트의 `createMember` 호출은 이 필드를 보내지 않으므로 **PR 이 배포되는 순간 회원 등록이 전부 400 이 됩니다.**
- 이메일 형식을 검사하는 규칙(`@Email`)은 없고, 중복 검사도 없습니다. `a` 같은 값도 통과합니다.

**2. 응답**

- `MemberResponse` 에 필수 필드 `email` 이 추가됩니다. `POST /members` 의 201 응답은 `{"id":1,"name":"하승","email":"h@example.com"}` 입니다.
- `LoanResponse` 가 `MemberResponse` 를 중첩해 쓰므로 `POST /loans`, `POST /loans/{id}/return`, `GET /members/{id}/loans` 의 응답 안 `member` 에도 `email` 이 실립니다. 그리고 `GET /members/{id}/loans` 는 **로그인 없이 호출되는 읽기** 입니다(4주 6절). 로그인하지 않고 호출한 실제 응답입니다.

```text
[{"id":1,"book":{"id":1,"isbn":"1","title":"TDD","author":"켄트 벡"},"member":{"id":1,"name":"하승","email":"h@example.com"},"loanedAt":"2026-10-22T14:06:10.703445Z","returnedAt":null}]
```

회원 번호만 알면 누구나 그 회원의 이메일을 읽을 수 있게 됩니다. **개인정보 노출** 이 이 PR 의 가장 중요한 발견입니다.

**3. 테이블**

- 서버 시작 로그의 DDL: `create table members (id bigint generated by default as identity, email varchar(255) not null, name varchar(255) not null, primary key (id))`. `members` 에 **`not null` 열** 이 추가됩니다.
- 이 참조 API 는 메모리 DB 라서 매번 새로 만들어 문제가 보이지 않습니다. 이미 회원이 들어 있는 운영 DB 에 `not null` 열을 추가하려면 기존 행의 값을 채우는 마이그레이션이 필요합니다. PR 에 마이그레이션이 없습니다.
- `unique` 가 아니라서 같은 이메일로 여러 회원이 등록됩니다. 인덱스도 없어서 이메일로 회원을 찾는 기능이 나중에 추가되면 전체를 읽게 됩니다.
- 쿼리 횟수는 변하지 않습니다. `GET /members/1/loans` 는 여전히 2번이고, `select` 하는 열에 `m2_0.email` 이 하나 늘었습니다.

**4. 프론트 타입과 화면**

- 스펙: `MemberRequest.email`(`minLength: 1`, 필수)과 `MemberResponse.email`(필수)이 생깁니다. 생성된 `schema.d.ts` 의 차이는 두 줄입니다.

```text
156a157
>             email: string;
164a166
>             email: string;
```

- `pnpm typecheck` 는 세 곳을 가리킵니다. 호출하는 쪽 한 곳(`createMember` 가 `email` 을 안 보냄)과, MSW 가짜 서버에서 회원 객체를 직접 만드는 두 곳입니다. `pnpm test` 는 `client.test.ts` 의 18개 모두 통과했습니다.

```text
src/api/library.ts(10,70): error TS2322: Type '{ name: string; }' is not assignable to type '{ name: string; email: string; } & {}'.
src/mocks/handlers.ts(24,14): error TS2741: Property 'email' is missing in type '{ id: number; name: string; }' but required in type '{ id: number; name: string; email: string; }'.
src/mocks/handlers.ts(106,11): error TS2741: Property 'email' is missing in type '{ id: number; name: string; }' but required in type '{ id: number; name: string; email: string; }'.
```

- 회원 등록 폼에 이메일 입력칸이 필요하고, 400 의 `email: ...` 오류를 입력칸 옆에 보여 줘야 합니다(`fieldErrors`).
- 백엔드 테스트: 이 diff 를 적용한 상태에서 `./gradlew :week25:test` 는 31개가 모두 통과했습니다. 테스트를 같이 고친 PR 입니다. 단 이메일 형식·중복·응답에 노출되는 범위를 검사하는 테스트는 없습니다.

**질문 예시**

- 회원 공개 조회(`GET /members/{id}/loans`)의 응답에 이메일을 실어도 되나요? 응답용 `record` 를 나누거나 `email` 을 로그인한 본인에게만 보여 줘야 하지 않나요?
- 이미 있는 회원의 `email` 은 어떻게 채우나요? 마이그레이션이 PR 에 있나요?
- 프론트의 회원 등록 호출을 바꾸는 배포와 이 PR 의 배포 순서는 어떻게 되나요? 한쪽이 먼저 나가면 등록이 400 이 됩니다.
- `@Email` 검증과 중복 검사는 필요 없나요? 오류 문장은 어떤 모양인가요?
- 이메일을 열 검색에 쓸 계획이 있나요? 있다면 인덱스가 필요합니다.

</details>

### PR 2. "불필요해 보이는 어노테이션과 주석 정리"

```diff
--- a/src/main/java/library/LoanRepository.java
+++ b/src/main/java/library/LoanRepository.java
@@ -2,7 +2,6 @@
 
 import java.util.List;
 import java.util.Optional;
-import org.springframework.data.jpa.repository.EntityGraph;
 import org.springframework.data.repository.Repository;
 
 public interface LoanRepository extends Repository<Loan, Long> {
@@ -14,7 +13,5 @@
 
     long countByMemberIdAndReturnedAtIsNull(Long memberId);
 
-    /** 책·회원을 같은 쿼리에서 함께 가져온다. 없으면 대출 건수만큼 쿼리가 더 나간다(N+1). */
-    @EntityGraph(attributePaths = {"book", "member"})
     List<Loan> findByMemberIdAndReturnedAtIsNullOrderByIdAsc(Long memberId);
 }
```

<details>
<summary>풀이 (서면으로 작성한 뒤에 여세요)</summary>

**1. 요청**: 변화 없음.

**2. 응답**: 변화 없음. 이 diff 를 적용한 복사본과 원본의 `GET /members/1/loans` 응답(`loanedAt` 제외)을 `diff` 로 비교했을 때 **완전히 같았습니다.** 상태 코드도 같습니다.

**3. 테이블**: 변화 없음(열·제약 변화 없음). 바뀌는 것은 **쿼리 횟수** 입니다. `GET /members/1/loans` 가 원본에서 2번이던 것이, 대출 3건일 때 6번이 됩니다(`N + 3`). 대출 건수를 늘린 복사본에서는 100건에서 103번, 300건에서 303번이었습니다(5절).

**4. 프론트 타입과 화면**

- 스펙과 생성 타입은 달라지지 않습니다. `pnpm typecheck` 와 `pnpm test` 는 아무것도 알려 주지 않습니다. 이 PR 은 **프론트의 어떤 검사로도 드러나지 않습니다.**
- 눈에 보이는 증상은 "대출이 많은 회원의 목록 화면이 느려진다" 입니다. 운영의 네트워크 DB 에서 대출 건수에 비례해 느려지고, 로컬 개발에서는 못 알아챕니다(5절).
- 백엔드 테스트: 이 diff 를 적용해 `./gradlew :week25:test` 를 돌리면 31개 중 둘이 실패합니다. 이 PR 은 테스트를 고치지도, 지우지도 않았습니다.

```text
LibraryApiTest > memberLoans_queryCountDoesNotGrowWithLoans() FAILED
LibraryRepositoryTest > activeLoans_fetchBookAndMemberInOneQuery() FAILED
```

**질문 예시**

- 지운 주석은 "없으면 대출 건수만큼 쿼리가 더 나간다(N+1)" 라고 경고하던 것입니다. 왜 `@EntityGraph` 까지 같이 지웠나요?
- 이 변경으로 `memberLoans_queryCountDoesNotGrowWithLoans` 와 `activeLoans_fetchBookAndMemberInOneQuery` 가 실패합니다. CI 를 통과했나요? 테스트를 비활성화한 것은 아닌가요?
- 대출이 많은 회원(한도를 늘릴 계획이 있다면)의 응답 시간을 측정했나요?

</details>

### 통과 기준

두 PR 모두에서 아래를 서면으로 적었으면 이 마일스톤을 통과한 것입니다. 풀이와 문장이 같을 필요는 없습니다.

- [ ] PR 1: 기존 `POST /members` 호출이 **400 이 된다** 는 것(요청 호환성)을 짚었다
- [ ] PR 1: 이메일이 **로그인 없이 읽는 응답에도** 실린다는 것(개인정보)을 짚었다
- [ ] PR 1: `members` 에 `not null` 열이 생기고 **기존 행** 이 문제라는 것(테이블)을 짚었다
- [ ] PR 1: 프론트에서 깨지는 곳(호출 한 곳, 가짜 서버 두 곳)과 화면에 입력칸이 필요하다는 것을 짚었다
- [ ] PR 2: 요청·응답·테이블이 **그대로** 라는 것과, 바뀌는 것은 **쿼리 횟수** 라는 것을 설명했다
- [ ] PR 2: 프론트의 타입 검사와 테스트로는 드러나지 않는다는 것과, 증상이 "목록이 길수록 느림" 이라는 것을 설명했다
- [ ] 두 PR 에서 각각 작성자에게 물을 질문을 3개 이상 적었다
- [ ] 4주 8절의 절차(층 분류, 테스트 diff 먼저, 변화표, 타입 영향, 질문)를 순서대로 따랐다

## 연습문제

1. 1단계의 SQL 에서 `SELECT m.name, COUNT(l.id) ... LEFT JOIN` 과 `JOIN` 의 결과 차이를 프론트 화면 하나로 설명하세요. "대출 현황 표에 대출이 0권인 회원도 나와야 한다" 면 어느 쪽인가요?
2. 4단계(또는 SQLite)에서 "반납하지 않은 대출을 가진 회원의 이름" 만 중복 없이 구하는 SQL 을 작성하세요. (`DISTINCT` 를 쓰면 됩니다.)
3. 2단계의 표에서 `POST /loans` 의 SQL 5번 중 어느 것이 `unique`/외래 키 같은 제약과 관련 있나요? 어느 것이 서비스의 규칙 검사인가요?
4. `Loan.java` 의 `@ManyToOne(fetch = FetchType.LAZY, optional = false)` 에서 `optional = false` 는 DDL 의 어느 부분과 대응하나요?
5. `GET /books?keyword=` 의 `like` 검색을 빠르게 만들고 싶다는 요청이 왔습니다. 백엔드 개발자에게 어떤 질문을 하겠습니까? (힌트: 7절)
6. `LibraryService` 의 `lend` 에서 `loans.existsByBookIdAndReturnedAtIsNull(bookId)` 검사를 건너뛰는 PR 이 있다고 가정하세요. 이 검사를 건너뛰어도 DB 가 막아 주는 제약이 있나요? 이 DB 에서 같은 책이 두 사람에게 동시에 대출되는 것을 막는 방법은 무엇인가요? (`ponytail` 주석)

<details>
<summary>정답 예시</summary>

1. 표에 "회원 이름 / 대출 중인 권수" 를 보여 준다면 `LEFT JOIN` 이어야 `준호 0` 같은 행이 나옵니다. `JOIN` 은 대출이 없는 회원을 표에서 빼 버립니다. 같은 데이터인데 쿼리 하나로 화면에서 사람이 사라집니다.
2. `SELECT DISTINCT m.name FROM members m JOIN loan l ON l.member_id = m.id WHERE l.returned_at IS NULL ORDER BY m.name;` 입니다. 4단계 데이터에서는 `민지`, `하승` 이 나옵니다.
3. 5번 중 앞의 4번이 규칙 검사입니다(회원 조회, 책 조회, 책 대출 여부, 회원의 대출 건수). 마지막 `insert into loan` 에서 외래 키(`book_id`, `member_id` 가 실제 존재해야 함)와 `not null` 제약이 작동합니다. 다만 "책이 이미 대출 중인가" 와 "3권 한도" 는 DB 제약이 아니라 서비스 코드의 규칙이어서 DB 가 대신 막지 못합니다.
4. `book_id bigint not null` 입니다. `optional = false` 면 `not null` 열이 되고, 대출은 반드시 책을 가져야 합니다.
5. "검색 조건이 제목의 일부 일치(`like '%...%'`)라서 인덱스를 못 쓰는데 어떻게 하실 건가요? 데이터 규모가 얼마나 되면 문제가 되나요? 시작 글자 일치로 제한해도 되나요? 전문 검색이 필요한가요?" 같은 질문입니다.
6. 건너뛰면 DB 는 막지 못합니다. `loan` 테이블에는 "한 책의 반납 전 대출은 하나" 라는 제약이 없기 때문입니다. 소스 주석이 말하는 대로 책·회원 행에 락을 걸거나 DB 의 부분 유니크 인덱스를 두는 방법이 있습니다.

</details>

## 퀴즈

1. `WHERE returned_at = NULL` 은 왜 항상 0건인가?
2. `@Valid` 검증에 실패한 `POST /books` 가 DB 에 SQL 을 몇 번 보내는가?
3. N+1 이 로컬 개발에서는 눈에 안 띄고 운영에서만 느려지는 이유는?
4. `@Transactional` 이 없으면 응답을 만드는 중에 `LazyInitializationException` 이 나는 이유는?
5. `isbn` 중복 검사를 서비스가 하는데도 DB 에 `unique` 제약이 있는 이유는?
6. 필드 하나만 추가한 엔티티 변경 PR 에서 `not null` 열 추가가 위험한 이유는?
7. PR 이 응답의 JSON 을 전혀 바꾸지 않았는데도 프론트에 영향을 줄 수 있는 경우를 하나 드세요.

<details>
<summary>정답</summary>

1. SQL 에서 `NULL` 은 "모르는 값" 이어서 `= NULL` 은 어떤 행에도 참이 되지 않는다. `IS NULL` 을 써야 한다
2. 0번. 검증은 컨트롤러 메서드에 들어오기 전에 실패해서 서비스와 DB 까지 가지 않는다
3. 로컬의 DB 는 같은 프로세스 안이거나 가까워서 쿼리 한 번의 비용이 거의 없다. 운영 DB 는 네트워크 너머에 있어 쿼리마다 왕복 시간이 더해지고, 운영 데이터는 항목 수 N 도 크다
4. 엔티티의 관계 필드는 처음에는 비어 있고(지연 로딩) 필요할 때 DB 에서 가져오는데, 그때 열려 있는 트랜잭션(세션)이 있어야 하기 때문이다
5. 동시에 도착한 두 요청이 서비스의 검사를 함께 통과할 수 있어서, DB 의 제약이 마지막 안전장치가 된다
6. 기존 행에는 그 열의 값이 없는데 `not null` 이면 열을 추가할 수 없거나 모든 기존 행의 값을 채우는 마이그레이션이 필요하다
7. 쿼리 횟수만 늘어난 변경(PR 2), 상태 코드 조건이 바뀐 변경(4주 PR 3), 새 검증 규칙이 추가된 변경 등. 응답 JSON 의 모양이 같아도 느려지거나, 오류가 나는 조건이 바뀌거나, 400 이 새로 나올 수 있다

</details>

## 체크리스트

- [ ] 1단계의 SQL 11줄에 먼저 예측을 적었고, 특히 `= NULL` 과 `LEFT JOIN` 의 결과를 설명할 수 있다
- [ ] 2단계의 요청별 SQL 횟수를 예측하고 실제로 센 뒤, 틀린 것의 이유를 적었다
- [ ] 3단계에서 `@EntityGraph` 를 지워 쿼리 횟수가 2에서 6으로 늘어나는 것을 직접 봤고 원복했다
- [ ] 엔티티의 어노테이션을 보고 테이블의 열·제약(`not null`, `unique`, 외래 키)을 말할 수 있다
- [ ] 리포지토리 메서드 이름을 보고 어떤 SQL 이 나가는지 대략 말할 수 있다
- [ ] 트랜잭션이 왜 필요하고 `@Transactional` 이 없으면 어떤 일이 생기는지 설명할 수 있다
- [ ] N+1 이 프론트에서 어떻게 보이는지(항목 수에 비례하는 대기 시간), 왜 로컬에서는 안 보이는지 설명할 수 있다
- [ ] 인덱스가 있을 때와 없을 때 `EXPLAIN` 의 차이를 보고, `like '%...%'` 검색이 왜 느려질 수 있는지 설명할 수 있다
- [ ] 마일스톤 체크의 두 PR 에 대해 요청·응답·테이블·프론트 영향·질문을 서면으로 작성했고, 통과 기준을 모두 채웠다
