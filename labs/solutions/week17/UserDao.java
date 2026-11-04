import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** 17주차 모범 풀이. 15분 넘게 막혔을 때만 보고, 덮은 뒤 직접 다시 치세요. */
public class UserDao {
    private final Connection conn;

    public UserDao(Connection conn) {
        this.conn = conn;
    }

    public long insert(String name, String email, int age) throws SQLException {
        String sql = "INSERT INTO users(name, email, age) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setInt(3, age);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getLong(1);
            }
        }
    }

    public Optional<User> findById(long id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT id, name, email, age FROM users WHERE id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public Optional<User> findByEmail(String email) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT id, name, email, age FROM users WHERE email = ?")) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public List<User> findAdults() throws SQLException {
        String sql = "SELECT id, name, email, age FROM users WHERE age >= ? ORDER BY name";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, 19);
            try (ResultSet rs = ps.executeQuery()) {
                List<User> list = new ArrayList<>();
                while (rs.next()) list.add(map(rs));
                return list;
            }
        }
    }

    public int updateEmail(long id, String email) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE users SET email = ? WHERE id = ?")) {
            ps.setString(1, email);
            ps.setLong(2, id);
            return ps.executeUpdate();
        }
    }

    public boolean delete(long id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE id = ?")) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public Map<String, Integer> orderTotalByUser() throws SQLException {
        String sql = """
            SELECT u.name, SUM(o.price) AS total
            FROM users u JOIN orders o ON o.user_id = u.id
            GROUP BY u.id, u.name
            ORDER BY u.name""";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            Map<String, Integer> result = new LinkedHashMap<>();
            while (rs.next()) result.put(rs.getString("name"), rs.getInt("total"));
            return result;
        }
    }

    public void placeTwoOrders(long userId, String item1, int price1, String item2, int price2)
            throws SQLException {
        boolean original = conn.getAutoCommit();
        conn.setAutoCommit(false);
        try {
            insertOrder(userId, item1, price1);
            if (price2 < 0) throw new IllegalArgumentException("가격은 0 이상이어야 합니다: " + price2);
            insertOrder(userId, item2, price2);
            conn.commit();
        } catch (SQLException | RuntimeException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(original);
        }
    }

    private void insertOrder(long userId, String item, int price) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO orders(user_id, item, price) VALUES (?, ?, ?)")) {
            ps.setLong(1, userId);
            ps.setString(2, item);
            ps.setInt(3, price);
            ps.executeUpdate();
        }
    }

    private User map(ResultSet rs) throws SQLException {
        return new User(rs.getLong("id"), rs.getString("name"), rs.getString("email"), rs.getInt("age"));
    }

    public int countOrders() throws SQLException {
        try (var st = conn.createStatement(); var rs = st.executeQuery("SELECT COUNT(*) FROM orders")) {
            rs.next();
            return rs.getInt(1);
        }
    }
}
