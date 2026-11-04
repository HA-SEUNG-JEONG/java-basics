import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 17주차 실습. "여기를 고치세요" 가 붙은 메서드를 채웁니다.
 * 규칙: 값은 반드시 PreparedStatement 의 ? 로 넣습니다. 문자열 + 로 SQL 을 만들지 마세요.
 * 테스트: ./gradlew :week17:test
 */
public class UserDao {
    private final Connection conn;

    public UserDao(Connection conn) {
        this.conn = conn;
    }

    /** users 에 한 줄 넣고, 자동 생성된 id 를 돌려준다. (힌트: Statement.RETURN_GENERATED_KEYS) */
    public long insert(String name, String email, int age) throws SQLException {
        // 여기를 고치세요
        throw new UnsupportedOperationException("insert 를 구현하세요");
    }

    /** id 로 한 명 찾기. 없으면 Optional.empty() */
    public Optional<User> findById(long id) throws SQLException {
        // 여기를 고치세요
        throw new UnsupportedOperationException("findById 를 구현하세요");
    }

    /** email 로 한 명 찾기. 없으면 Optional.empty(). 입력값에 ' 같은 문자가 있어도 안전해야 한다. */
    public Optional<User> findByEmail(String email) throws SQLException {
        // 여기를 고치세요
        throw new UnsupportedOperationException("findByEmail 을 구현하세요");
    }

    /** 나이가 19 이상인 사람을 이름 오름차순으로. */
    public List<User> findAdults() throws SQLException {
        // 여기를 고치세요
        throw new UnsupportedOperationException("findAdults 를 구현하세요");
    }

    /** 이메일을 바꾸고, 바뀐 행 수를 돌려준다. (없는 id 면 0) */
    public int updateEmail(long id, String email) throws SQLException {
        // 여기를 고치세요
        throw new UnsupportedOperationException("updateEmail 을 구현하세요");
    }

    /** 지운 행이 있으면 true. */
    public boolean delete(long id) throws SQLException {
        // 여기를 고치세요
        throw new UnsupportedOperationException("delete 를 구현하세요");
    }

    /**
     * 회원 이름 -> 주문 금액 합계. 주문이 하나도 없는 회원은 결과에 넣지 않는다.
     * (힌트: JOIN + GROUP BY + SUM)
     */
    public Map<String, Integer> orderTotalByUser() throws SQLException {
        // 여기를 고치세요
        throw new UnsupportedOperationException("orderTotalByUser 를 구현하세요");
    }

    /**
     * 주문 2건을 "둘 다 저장되거나, 둘 다 저장되지 않게" 넣는다.
     * price2 가 음수면 IllegalArgumentException 을 던지고, 그때 첫 번째 주문도 남아 있으면 안 된다.
     * (힌트: setAutoCommit(false), commit, rollback, finally 에서 autoCommit 원복)
     */
    public void placeTwoOrders(long userId, String item1, int price1, String item2, int price2)
            throws SQLException {
        // 여기를 고치세요
        throw new UnsupportedOperationException("placeTwoOrders 를 구현하세요");
    }

    /** 테스트가 상태를 확인할 때 쓰는 도우미. 고치지 않습니다. */
    public int countOrders() throws SQLException {
        try (var st = conn.createStatement(); var rs = st.executeQuery("SELECT COUNT(*) FROM orders")) {
            rs.next();
            return rs.getInt(1);
        }
    }
}
