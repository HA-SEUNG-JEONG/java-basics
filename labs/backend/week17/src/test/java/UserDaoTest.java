import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserDaoTest {
    Connection conn;
    UserDao dao;

    @BeforeEach
    void setUp() throws SQLException {
        conn = Db.connect();
        dao = new UserDao(conn);
    }

    @AfterEach
    void tearDown() throws SQLException {
        conn.close();
    }

    @Test
    void insert_returnsGeneratedIdAndIncreases() throws SQLException {
        long a = dao.insert("하승", "ha@example.com", 30);
        long b = dao.insert("민지", "min@example.com", 17);
        assertTrue(a > 0, "id 는 1 이상이어야 합니다");
        assertEquals(a + 1, b, "id 는 1씩 증가합니다");
    }

    @Test
    void findById_found_and_missing() throws SQLException {
        long id = dao.insert("하승", "ha@example.com", 30);
        assertEquals(new User(id, "하승", "ha@example.com", 30), dao.findById(id).orElseThrow());
        assertTrue(dao.findById(9999).isEmpty());
    }

    @Test
    void findByEmail_works() throws SQLException {
        dao.insert("하승", "ha@example.com", 30);
        assertEquals("하승", dao.findByEmail("ha@example.com").orElseThrow().name());
        assertTrue(dao.findByEmail("none@example.com").isEmpty());
    }

    @Test
    void findByEmail_isSafeFromSqlInjection() throws SQLException {
        dao.insert("하승", "ha@example.com", 30);
        dao.insert("민지", "min@example.com", 17);
        assertTrue(dao.findByEmail("' OR '1'='1").isEmpty(),
                "SQL 인젝션: 이 입력으로 회원이 조회되면 안 됩니다. ? 를 쓰세요");
    }

    @Test
    void findByEmail_acceptsApostrophe() throws SQLException {
        dao.insert("오브라이언", "o'brien@example.com", 40);
        assertEquals("오브라이언", dao.findByEmail("o'brien@example.com").orElseThrow().name());
    }

    @Test
    void findAdults_filtersAndSorts() throws SQLException {
        dao.insert("다영", "c@example.com", 19);
        dao.insert("가은", "a@example.com", 25);
        dao.insert("나래", "b@example.com", 18);
        List<User> adults = dao.findAdults();
        assertEquals(List.of("가은", "다영"), adults.stream().map(User::name).toList());
    }

    @Test
    void updateEmail_returnsAffectedRows() throws SQLException {
        long id = dao.insert("하승", "ha@example.com", 30);
        assertEquals(1, dao.updateEmail(id, "new@example.com"));
        assertEquals("new@example.com", dao.findById(id).orElseThrow().email());
        assertEquals(0, dao.updateEmail(9999, "x@example.com"));
    }

    @Test
    void delete_returnsWhetherDeleted() throws SQLException {
        long id = dao.insert("하승", "ha@example.com", 30);
        assertTrue(dao.delete(id));
        assertFalse(dao.delete(id));
        assertTrue(dao.findById(id).isEmpty());
    }

    @Test
    void orderTotalByUser_joinsAndGroups() throws SQLException {
        long ha = dao.insert("하승", "ha@example.com", 30);
        long min = dao.insert("민지", "min@example.com", 17);
        dao.insert("주문없음", "none@example.com", 50);
        try (Statement st = conn.createStatement()) {
            st.execute("INSERT INTO orders(user_id, item, price) VALUES (" + ha + ", '책', 15000)");
            st.execute("INSERT INTO orders(user_id, item, price) VALUES (" + ha + ", '펜', 2000)");
            st.execute("INSERT INTO orders(user_id, item, price) VALUES (" + min + ", '노트', 3000)");
        }
        assertEquals(Map.of("하승", 17000, "민지", 3000), dao.orderTotalByUser());
    }

    @Test
    void placeTwoOrders_commitsBoth() throws SQLException {
        long id = dao.insert("하승", "ha@example.com", 30);
        dao.placeTwoOrders(id, "책", 15000, "펜", 2000);
        assertEquals(2, dao.countOrders());
        assertTrue(conn.getAutoCommit(), "끝난 뒤 autoCommit 은 원래대로 true 여야 합니다");
    }

    @Test
    void placeTwoOrders_rollsBackWhenSecondFails() throws SQLException {
        long id = dao.insert("하승", "ha@example.com", 30);
        assertThrows(IllegalArgumentException.class,
                () -> dao.placeTwoOrders(id, "책", 15000, "펜", -1));
        assertEquals(0, dao.countOrders(), "둘째 주문이 실패하면 첫째 주문도 취소(rollback)되어야 합니다");
        assertTrue(conn.getAutoCommit(), "실패한 뒤에도 autoCommit 은 원래대로 true 여야 합니다");
    }
}
